/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services.providers;

import com.google.common.base.Preconditions;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import edu.emory.mathcs.backport.java.util.Collections;
import java.io.UnsupportedEncodingException;
import org.apache.marmotta.ucuenca.wk.pubman.utils.ScopusMapper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.ucuenca.wk.pubman.api.AbstractProviderService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class ScopusUpdateProviderService extends AbstractProviderService {

  @Inject
  private ConfigurationService configurationService;

  private final String requestTemplate = "http://api.elsevier.com/content/search/author?query=%s&count=100&apiKey=%s";
  private final String expressionTemplateNames = "authfirst(%s) OR authfirst(%s) AND authlast(%s) AND AFFIL(ecuador)";
  private final String expressionTemplateName = "authfirst(%s) AND authlast(%s) AND AFFIL(ecuador)";
  private String expression;

  @Override
  protected List<String> buildURLs(String firstname, String lastname, List<String> organizations) {
    return null;
  }

  @Override
  public void extractAuthors(String[] organizations, boolean force) {
    String apikey = configurationService.getStringConfiguration("publications.scopus.apikey");
    Task task = taskManagerService.createSubTask(String.format("%s Extraction", getProviderName()), "Publication Extractor");
    task.updateMessage(String.format("Extracting publications from %s Provider", getProviderName()));
    task.updateDetailMessage("Graph", getProviderGraph());
    try {
      for (String organizationx : organizations) {
        String[] split = organizationx.split("\\|");
        String organization = split[0];
        task.updateDetailMessage("Organization", organization);
        int offset = Integer.parseInt(split[1]);
        List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, "select distinct ?id ?lm { graph <" + constantService.getOrganizationsGraph() + "> {"
            + "<" + organization + "> <http://vivoweb.org/ontology/core#scopusId> ?id . "
            + " optional { <" + organization + "> <http://purl.org/dc/terms/modified> ?lm . }"
            + "} }");
        String[] affs = new String[0];
        String scg = "(";
        Date lm = new Date(0, 0, 0);
        if (query.isEmpty()) {
          continue;
        }
        for (Map<String, Value> mp : query) {
          String get = mp.get("id").stringValue();
          String[] split1 = get.split(";");
          affs = split1;
          for (int i = 0; i < split1.length; i++) {
            scg += "AF-ID(" + split1[i] + ")";
            if (i != split1.length - 1) {
              scg += "+OR+";
            }
          }

          Value get2 = mp.get("lm");
          if (get2 != null) {
            long r = Long.parseLong(get2.stringValue());
            lm = new Date(r);
          }
        }
        scg += ")";
        String host = "https://api.elsevier.com/content/search/scopus?query=";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String lasu = sdf.format(lm);
        List<String> authurls = new ArrayList<>();
        List<String> authurls2 = new ArrayList<>();
        String myurl = host + scg + "+AND+orig-load-date+aft+" + lasu + "&apiKey=" + apikey + "&view=COMPLETE&start=" + 0 + "&count=25";
        authurls.add(myurl);
        int coun = 1;
        int posc = 0;
        do {
          authurls = new ArrayList<>(new HashSet<>(authurls));
          if (authurls.isEmpty()) {
            break;
          }
          posc++;
          String procurl = authurls.remove(0);
          log.info("Harvesting Scopus {}", procurl);
          HttpResponse<JsonNode> asJson = Unirest.get(procurl)
              .header("Accept", "application/json")
              .asJson();
          if (asJson.getStatus() != 200) {
            break;
          }
          JsonNode body = asJson.getBody();
          JSONObject results = body.getObject().getJSONObject("search-results");

          if (!results.has("entry")) {
            continue;
          }
          JSONArray resarr = results.getJSONArray("entry");
          long total = results.getLong("opensearch:totalResults");
          if (procurl.contains("start=0")) {
            String mc = procurl;
            boolean first = true;
            for (int jkk = 0; jkk < total; jkk += 25) {
              if (first) {
                first = false;
                continue;
              }
              String myurlx = mc.replace("start=0", "start=" + jkk);
              authurls.add(myurlx);
              coun++;
            }
          }

          task.updateTotalSteps(coun);
          for (int w = 0; w < resarr.length(); w++) {
            JSONObject objt = resarr.getJSONObject(w);
            ScopusMapper mpp = new ScopusMapper(objt);
            try {
              mpp.run();
              for (String fx : affs) {
                List<String> obtainAuthors = mpp.obtainAuthors(fx, organization);
                if (procurl.contains("AF-ID")) {
                  for (String idsa : obtainAuthors) {
                    String myurlxq = host + "(AU-ID(" + idsa + "))" + "+AND+orig-load-date+aft+" + lasu + "&apiKey=" + apikey + "&view=COMPLETE&start=" + 0 + "&count=25";
                    authurls2.add(myurlxq);
                    coun++;
                  }
                }

              }

            } catch (Exception e) {
              e.printStackTrace();
              log.info(objt.toString());
            }
            sparqlService.getGraphDBInstance().addBuffer(ValueFactoryImpl.getInstance().createURI(constantService.getAuthorsGraph()), mpp.getAuthors());
            sparqlService.getGraphDBInstance().addBuffer(ValueFactoryImpl.getInstance().createURI(constantService.getScopusGraph()), mpp.getScopus());
            sparqlService.getGraphDBInstance().addBuffer(ValueFactoryImpl.getInstance().createURI(constantService.getEndpointsGraph()), mpp.getEndpoints());
            task.updateProgress(posc);
            if (authurls.isEmpty() && !authurls2.isEmpty()) {
              authurls.addAll(authurls2);
              authurls2.clear();
            }
          }
        } while (!authurls.isEmpty());

        sparqlService.getGraphDBInstance().dumpBuffer();

        String upd = "delete data { "
            + "graph <" + constantService.getOrganizationsGraph() + "> {"
            + " <" + organization + "> <http://purl.org/dc/terms/modified> '" + lm.toInstant().toEpochMilli() + "' . "
            + "}"
            + "} ";
        sparqlService.getSparqlService().update(QueryLanguage.SPARQL, upd);
        upd = "insert data{"
            + "graph <" + constantService.getOrganizationsGraph() + "> {"
            + " <" + organization + "> <http://purl.org/dc/terms/modified> '" + new Date().toInstant().toEpochMilli() + "' . "
            + "}"
            + "}";
        sparqlService.getSparqlService().update(QueryLanguage.SPARQL, upd);
      }
    } catch (Exception e) {
      log.error("Unknown error {}.", e);
    } finally {
      try {
        sparqlService.getGraphDBInstance().dumpBuffer();
      } catch (Exception ex) {
      }
      taskManagerService.endTask(task);
    }

  }

  @Override
  protected String getProviderGraph() {
    return constantService.getScopusGraph();
  }

  @Override
  protected String getProviderName() {
    return "SCOPUS";
  }

  @Override
  protected String filterExpressionSearch() {
    return expression.replace('+', '.');
  }

}
