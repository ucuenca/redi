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
import com.mashape.unirest.http.Unirest;
import edu.emory.mathcs.backport.java.util.Collections;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.ucuenca.wk.pubman.api.AbstractProviderService;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;

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
        int offset = Integer.parseInt(split[1]);
        List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, "select distinct ?id ?lm { graph <" + constantService.getOrganizationsGraph() + "> {"
                + "<" + organization + "> <http://vivoweb.org/ontology/core#scopusId> ?id . "
                + " optional { <" + organization + "> <http://purl.org/dc/terms/modified> ?lm . }"
                + "} }");
        String scg = null;
        Date lm = new Date(Long.MIN_VALUE);
        for (Map<String, Value> mp : query) {
          String get = mp.get("id").stringValue();
          scg = get;
          String get2 = mp.get("lm").stringValue();
          if (get2 != null) {
            long r = Long.parseLong(get2);
            lm = new Date(r);
          }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        int pos = 1;
        do {
          Unirest.get("http://api.elsevier.com/content/search/scopus?view=COMPLETE&apiKey=" + apikey + "&query=AF-ID(" + scg + ")+AND+orig-load-date+aft+" + sdf.format(lm) + "&start=" + pos + "&count=25");
          if (false) {
            break;
          }
          //process
          pos += 25;

        } while (true);

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
