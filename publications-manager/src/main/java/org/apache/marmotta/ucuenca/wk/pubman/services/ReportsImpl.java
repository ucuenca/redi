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
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.google.common.base.Joiner;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.marmotta.ucuenca.wk.commons.function.Cache;
import org.apache.marmotta.ucuenca.wk.commons.impl.ConstantServiceImpl;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.apache.marmotta.ucuenca.wk.pubman.api.ReportsService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;

/**
 *
 * @author Jose Luis Cullcay
 *
 */
@ApplicationScoped
public class ReportsImpl implements ReportsService {

  @Inject
  private Logger log;
  @Inject
  protected ConstantService constant;
  @Inject
  private ExternalSPARQLService sesameService;

  protected String TEMP_PATH = "./../research_webapps/ROOT/tmp";
  protected String REPORTS_FOLDER = "./../research_webapps/ROOT/reports/";

  @Override
  public String createReport(String hostname, String realPath, String name, String type, List<String> params) {

    String hash = hostname + "|" + realPath + "|" + name + "|" + type + "|" + Joiner.on(",").join(params);
    hash = Cache.getMD5(hash);

    TEMP_PATH = "/tmp/redi_reports";
    REPORTS_FOLDER = realPath + "/reports/";
    // Make sure the output directory exists.
    File outDir = new File(TEMP_PATH);
    outDir.mkdirs();
    //Name of the file
    //SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy_HHmmss");
    String nameFile = "redi_reports_" + hash;//name + "_" + format.format(new Date());
    String pathFile = TEMP_PATH + "/" + nameFile + "." + type;
    try {
      //Cache
      if (new File(pathFile).exists()) {
        return "/pubman/reportDownload?file=" + hash + "." + type;
      }

      // Compile jrxml file.
      JasperReport jasperReport = JasperCompileManager
              .compileReport(REPORTS_FOLDER + name + ".jrxml");
      //String array with the json string and other parameters required for the report
      String[] json = null;
      //Datasource
      JsonDataSource dataSource = null;
      // Parameters for report
      Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("rutalogo", realPath + "/wkhome/images/logo_wk.png");
      InputStream stream;

      //Parameters for each report
      switch (name) {
        case "ReportAuthor":
          // Get the Json with the list of publications, the name of the researcher and the number of publications.
          json = getJSONAuthor(params.get(0), hostname);

          parameters.put("name", json[1]);
          parameters.put("numero", json[2]);
          break;
        case "ReportAuthorCluster":
          // Get the Json with the list of publications, the name of the researcher and the number of publications.
          json = getJSONAuthorsCluster(params.get(0), hostname);

          parameters.put("name", json[1]);
          parameters.put("numero", json[2]);
          break;
        case "ReportAuthorCluster2":
          // Get the Json with the list of publications, the name of the researcher and the number of publications.

          if (params.size() == 2) {
            json = getJSONAuthorsCluster2(params.get(0), params.get(1), hostname, null, null);
          } else if (params.size() == 4) {
            json = getJSONAuthorsCluster2(params.get(0), params.get(1), hostname, params.get(2), params.get(3));
          }

          parameters.put("name", json[1]);
          parameters.put("numero", json[2]);
          break;
        case "ReportStatistics":
        case "ReportStatisticsPub":
        case "ReportStatisticsRes":
          // Get the Json with the list of publications, the number of researchers and the number of publications.
          json = getJSONStatistics(hostname);
          break;
        case "ReportStatisticsTopResU":
          // Get the Json with the top researchers per university (considering the number of publications).
          json = getJSONTopResearchersUnis(hostname);

          break;
        case "ReportPublicationsByKeyword":
          // Get the Json with the publications related to a keyword.
          json = getJSONPublicationsByKeyword(params.get(0), hostname);

          parameters.put("keyword", json[1]);
          parameters.put("numero", json[2]);

          break;
        case "ReportPublicationsByAuthor":
          // Get the Json with the Authors by Area.
          json = getJSONReportPublicationsByAuthor(params.get(0), hostname);

          parameters.put("name", json[1]);
          parameters.put("numero", json[2]);

          break;
        case "ReportResearchersbyIES":
          // Get the Json with the all authors from an specific IES.
          json = getJSONAuthorsbyIES(params.get(0), hostname);

          parameters.put("universityFullName", json[1]);
          parameters.put("universityName", params.get(0));
          parameters.put("totalAuthors", json[2]);

          break;
        case "ReportPublicationsbyHEI":
          // Get the Json with the all authors from an specific IES.
          json = getJSONPublicationsByHEI(params.get(0), hostname);

          parameters.put("universityFullName", json[1]);
          parameters.put("universityName", params.get(0));
          //parameters.put("totalAuthors", json[2]);

          break;
        case "ReportStatisticsKeywords":
          // Get the Json with the top keywords (considering the number of publications).
          json = getJSONStatisticsTopKeywords(hostname);

          break;
        case "ReportClustersAuthors":
          // Get the Json with the top keywords (considering the number of publications).
          json = getClusterAuthors(hostname);
          parameters.put("name", "Todos");
          parameters.put("numero", json[1]);

          break;
      }
      //Always the first element of the array has the json stream
      stream = new ByteArrayInputStream(json[0].getBytes("UTF-8"));
      dataSource = new JsonDataSource(stream);

      if (dataSource != null) {
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        if (type.equals("pdf")) {
          // 1 - Export to pdf 
          if (jasperPrint != null) {
            JasperExportManager.exportReportToPdfFile(jasperPrint, pathFile);
          }

        } else if (type.equals("xls")) {
          // 2- Export to Excel sheet
          JRXlsExporter exporter = new JRXlsExporter();

          List<JasperPrint> jasperPrintList = new ArrayList<>();
          jasperPrintList.add(jasperPrint);

          exporter.setExporterInput(SimpleExporterInput.getInstance(jasperPrintList));
          exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(pathFile));

          exporter.exportReport();

        }
        // Return the relative online path for the report
        return "/pubman/reportDownload?file=" + hash + "." + type;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }

    return "";
  }

  /**
   * Extract Json with publications of an author
   *
   * @param author Author id
   * @param hostname
   * @return
   */
  public String[] getJSONAuthor(String author, String hostname) {
    String getQuery = "";
    try {
      //Variables to return with name and number of publications
      String name = "";
      Integer cont = 0;
      //Query
      getQuery = ConstantServiceImpl.PREFIX
              + " SELECT ?publications ?authors ( max(str(?name_)) as ?name) (max(str(?title_)) as ?title) (max(str(?abstract_)) as ?abstract) ( max(str(?authorsName_)) as ?authorsName) WHERE { "
              + " graph <" + constant.getCentralGraph() + "> { <" + author + "> foaf:name ?name_. "
              + "  <" + author + "> foaf:publications  ?publications. "
              + "  ?publications dct:title ?title_. "
              + "  OPTIONAL {?publications bibo:abstract ?abstract_ .} "
              + "  ?publications dct:creator|dct:contributor ?authors. "
              + "  ?authors foaf:name ?authorsName_. "
              + "} } group by ?publications ?authors ";

      log.info("Buscando Informacion de: " + author);
      RepositoryConnection con = sesameService.getRepositoryConnetion();
      try {
        // perform operations on the connection
        TupleQueryResult resulta = con.prepareTupleQuery(QueryLanguage.SPARQL, getQuery).evaluate();

        //JSONObject authorJson = new JSONObject();
        Map<String, JSONObject> pubMap = new HashMap<String, JSONObject>();
        Map<String, JSONArray> coautMap = new HashMap<String, JSONArray>();
        JSONArray publications = new JSONArray();

        JSONObject coauthors = new JSONObject();
        while (resulta.hasNext()) {
          BindingSet binding = resulta.next();
          name = binding.getValue("name").stringValue();
          //authorJson.put("name", name);
          String pubTitle = binding.getValue("title").stringValue();
          if (!pubMap.containsKey(pubTitle)) {
            pubMap.put(pubTitle, new JSONObject());
            pubMap.get(pubTitle).put("title", pubTitle);
            cont++;
            if (binding.getValue("abstract") != null) {
              pubMap.get(pubTitle).put("abstract", binding.getValue("abstract").stringValue());
            }
            //Coauthors
            coautMap.put(pubTitle, new JSONArray());
            coautMap.get(pubTitle).add(binding.getValue("authorsName").stringValue());
            //pubMap.get(pubTitle).put("title", pubTitle);
          } else {
            coautMap.get(pubTitle).add(binding.getValue("authorsName").stringValue());
          }
        }

        for (Map.Entry<String, JSONObject> pub : pubMap.entrySet()) {
          pub.getValue().put("coauthors", coautMap.get(pub.getKey()));
          publications.add(pub.getValue());
        }
        //authorJson.put("publications", publications);
        con.close();
        //return new String[] {authorJson.toJSONString(), publications.toString(), authorJson.get("name").toString(), cont.toString()};
        return new String[]{publications.toString(), name, cont.toString()};
      } catch (RepositoryException ex) {
        java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
        con.close();
      }

      return new String[]{"", ""};
    } catch (MalformedQueryException | QueryEvaluationException | RepositoryException ex) {
      java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
      ex.printStackTrace();
    }
    return new String[]{"", ""};
  }

  /**
   * Extract Json with authors related through a cluster
   *
   * @param clusterId Id of the cluster
   * @param hostname Hostname
   * @return Array of strings
   */
  public String[] getJSONAuthorsCluster(String clusterId, String hostname) {
    String getQuery = "";
    try {
      //Variables to return with name and number of publications
      String name = "";
      Integer cont = 0;
      //Query
      getQuery = ConstantServiceImpl.PREFIX
              + " SELECT DISTINCT ?cluster ?author ?keywords "
              + "WHERE "
              + "{ "
              + "  graph <" + constant.getClusterGraph() + "> "
              + "  { "
              + "    <" + clusterId + "> foaf:publications ?publications. "
              + "    <" + clusterId + "> rdfs:label ?cluster . "
              + "     ?publications uc:hasPerson ?subject"
              + "    { "
              + "    	select DISTINCT ?subject ?author ?keywords "
              + "        where "
              + "        { "
              + "        	graph <" + constant.getCentralGraph() + "> "
              + "            { "
              + "                 ?subject foaf:name ?author. "
              + "                 ?subject foaf:publications ?publicationUri. "
              + "                 ?publicationUri dct:title ?title. "
              + "                 ?publicationUri dct:subject [rdfs:label ?keywords].  "
              + "             } "
              + "        } group by ?subject ?author ?keywords "
              + "    }"
              + "  }"
              + "}";

      RepositoryConnection con = sesameService.getRepositoryConnetion();
      try {
        // perform operations on the connection
        TupleQueryResult resulta = con.prepareTupleQuery(QueryLanguage.SPARQL, getQuery).evaluate();

        //JSONObject authorJson = new JSONObject();
        Map<String, JSONObject> autMap = new HashMap<String, JSONObject>();
        Map<String, JSONArray> keyMap = new HashMap<String, JSONArray>();
        JSONArray authors = new JSONArray();

        JSONObject coauthors = new JSONObject();
        while (resulta.hasNext()) {
          BindingSet binding = resulta.next();
          name = binding.getValue("cluster").stringValue();
          String authorName = binding.getValue("author").stringValue();
          if (!autMap.containsKey(authorName)) {
            autMap.put(authorName, new JSONObject());
            autMap.get(authorName).put("author", authorName);
            if (binding.getValue("keywords") != null) {
              autMap.get(authorName).put("keywords", String.valueOf(binding.getValue("keywords")).replace("\"", ""));
            }
            //Keywords
            keyMap.put(authorName, new JSONArray());
            keyMap.get(authorName).add(String.valueOf(binding.getValue("keywords")).replace("\"", "").replace("^^", ""));
          } else {
            keyMap.get(authorName).add(String.valueOf(binding.getValue("keywords")).replace("\"", "").replace("^^", ""));
          }
        }

        for (Map.Entry<String, JSONObject> aut : autMap.entrySet()) {
          aut.getValue().put("keywords", keyMap.get(aut.getKey()));
          authors.add(aut.getValue());
        }
        con.close();
        //Number of authors
        cont = autMap.size();
        return new String[]{authors.toString(), name, cont.toString()};
      } catch (RepositoryException ex) {
        java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
        con.close();
      }

      return new String[]{"", ""};
    } catch (MalformedQueryException | QueryEvaluationException | RepositoryException ex) {
      java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
      ex.printStackTrace();
    }
    return new String[]{"", ""};
  }

  /**
   * Extract Json with authors related through a cluster
   *
   * @param clusterId Id of the cluster
   * @param hostname Hostname
   * @return Array of strings
   */
  public String[] getJSONAuthorsCluster2(String clusterId, String clusterName, String hostname, String scid, String scname) {
    try {
      SSLContext sslcontext = SSLContexts.custom()
              .loadTrustMaterial(null, new TrustSelfSignedStrategy())
              .build();

      SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
      CloseableHttpClient httpclient = HttpClients.custom()
              .setSSLSocketFactory(sslsf)
              .build();
      Unirest.setHttpClient(httpclient);

      HttpResponse<JsonNode> asJson;
      if (scid == null && scname == null) {
        asJson = Unirest.get(hostname + "mongo/clusterDiscipline")
                .queryString("uri", clusterId)
                .asJson();
      } else {
        asJson = Unirest.get(hostname + "mongo/authorByArea")
                .queryString("cluster", clusterId)
                .queryString("subcluster", scid)
                .asJson();
      }
      if (asJson.getStatus() == HttpURLConnection.HTTP_OK) {
        org.json.JSONObject object = asJson.getBody().getObject();
        org.json.JSONArray jsonArray = object.getJSONArray("nodes");
        JSONArray authors = new JSONArray();
        for (int w = 0; w < jsonArray.length(); w++) {
          org.json.JSONObject jsonObject = jsonArray.getJSONObject(w);
          JSONObject jsonObjectw = new JSONObject();
          jsonObjectw.put("mail", jsonObject.isNull("mails") ? "." : jsonObject.getString("mails"));
          jsonObjectw.put("organization", jsonObject.getString("orgs"));
          jsonObjectw.put("author", jsonObject.getString("label"));
          if (scid == null && scname == null) {
            jsonObjectw.put("areas", jsonObject.getString("group"));
          } else {
            jsonObjectw.put("areas", scname);
          }
          jsonObjectw.put("keywords", jsonObject.getString("subject"));
          authors.add(jsonObjectw);
        }
        return new String[]{authors.toJSONString(), clusterName, "" + jsonArray.length()};
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return new String[]{"", ""};
  }

  public String[] getClusterAuthors(String hostname) {

    RepositoryConnection con;
    try {

      con = sesameService.getRepositoryConnetion();

      String getQuery = ConstantServiceImpl.PREFIX
              + "select (?clusterlabel as ?cluster) (group_concat(?name ; separator=' ; ') as ?authors) \n"
              + "  { "
              + "	select ?clusterlabel ?subject (sample(str(?namex)) as ?name) { "
              + "		graph <" + constant.getClusterGraph() + "> "
              + "		                       	{ "
              + "		                         ?subject  dct:isPartOf ?cluster .  "
              + "                                  ?cluster rdfs:label ?clusterlabel ."
              + "		                         ?cluster   a  uc:SubCluster .  "
              + "		                        filter ( lang(?clusterlabel) = 'en')   "
              + "		                        }"
              + "		graph <" + constant.getCentralGraph() + "> "
              + "	                                 { "
              + "	                                      ?subject foaf:name ?namex . "
              + "	                                  } "
              + "	} group by ?clusterlabel ?subject "
              + " } group by ?clusterlabel";

      // perform operations on the connection
      TupleQueryResult resulta = con.prepareTupleQuery(QueryLanguage.SPARQL, getQuery).evaluate();

      JSONObject cluster;
      JSONArray clusters = new JSONArray();

      while (resulta.hasNext()) {
        BindingSet binding = resulta.next();
        String clustername = binding.getValue("cluster").stringValue();
        String totalAuthors = binding.getValue("authors").stringValue();
        cluster = new JSONObject();
        cluster.put("name", clustername);
        cluster.put("authors", totalAuthors);
        clusters.add(cluster);

      }
      con.close();

      return new String[]{clusters.toString(), clusters.size() + ""};

      //return null;
    } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
      java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);

    }

    return new String[]{"", ""};

  }

  /**
   * Retrieve Json with statistics about universities, their authors and
   * publications
   *
   * @param hostname Hostname
   * @return Array of strings
   */
  public String[] getJSONStatistics(String hostname) {
    String getQuery = "";
    try {

      //Query
      getQuery = ConstantServiceImpl.PREFIX
              + " SELECT ?provenance ?name (COUNT(DISTINCT(?s)) AS ?total) (count(DISTINCT ?pub) as ?totalp) "
              + " WHERE "
              + "    { "
              + "    	GRAPH <" + constant.getCentralGraph() + "> { "
              + "          ?s a foaf:Person. "
              + "          ?s foaf:publications ?pub . "
              + "          ?s schema:memberOf ?provenance . "
              + "          { "
              + "              SELECT ?name "
              + "              WHERE { "
              + "                  GRAPH <" + constant.getOrganizationsGraph() + "> { "
              + "                       ?provenance uc:fullName ?namex . "
              + "                       bind (str(?namex) as ?name ) . "
              + "                       FILTER (lang(?namex) = \"es\")."
              + "                  } "
              + "              } "
              + "          } "
              + "    	} "
              + "  	} GROUP BY ?provenance ?name ";

      RepositoryConnection con = sesameService.getRepositoryConnetion();
      try {
        // perform operations on the connection
        TupleQueryResult resulta = con.prepareTupleQuery(QueryLanguage.SPARQL, getQuery).evaluate();

        JSONObject uni;
        JSONArray universities = new JSONArray();

        while (resulta.hasNext()) {
          BindingSet binding = resulta.next();
          uni = new JSONObject();
          String uniName = binding.getValue("name").stringValue();
          String totalAuthors = binding.getValue("total").stringValue();
          String totalPubs = binding.getValue("totalp").stringValue();
          uni.put("university", uniName);
          uni.put("authors", totalAuthors);
          uni.put("pubs", totalPubs);

          universities.add(uni);
        }

        con.close();

        return new String[]{universities.toString()};
      } catch (RepositoryException ex) {
        java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
        con.close();
      }

      return new String[]{"", ""};
    } catch (MalformedQueryException | QueryEvaluationException | RepositoryException ex) {
      java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
      ex.printStackTrace();
    }
    return new String[]{"", ""};
  }

  public String[] getJSONTopResearchersUnis(String hostname) {
    String query1, query2 = "";

    try {
      query1 = ConstantServiceImpl.PREFIX
              + "SELECT ?provenance ?uni "
              + "WHERE "
              + "{ "
              + "  GRAPH <" + constant.getOrganizationsGraph() + "> "
              + "  { "
              + "    ?provenance uc:fullName ?uni . "
              + "     FILTER (lang(?uni) = \"es\")."
              + "  }"
              + "} ORDER BY ?uni";

      RepositoryConnection con = sesameService.getRepositoryConnetion();

      try {
        // perform operations on the connection
        TupleQueryResult resultUnis = con.prepareTupleQuery(QueryLanguage.SPARQL, query1).evaluate();
        JSONArray authors = new JSONArray();
        //Check authors of each university
        while (resultUnis.hasNext()) {
          BindingSet binding = resultUnis.next();
          String uniId = binding.getValue("provenance").stringValue();
          String uniName = binding.getValue("uni").stringValue();

          query2 = ConstantServiceImpl.PREFIX
                  + "SELECT ?researcher (count(DISTINCT ?pub) as ?totalp) "
                  + "WHERE "
                  + "{ "
                  + "  GRAPH <" + constant.getCentralGraph() + "> "
                  + "        { "
                  + "          ?s a foaf:Person. "
                  + "          ?s foaf:name ?researcher. "
                  + "          ?s foaf:publications ?pub . "
                  + "          ?s schema:memberOf <" + uniId + "> "
                  + "        } "
                  + "} GROUP BY ?researcher ORDER BY DESC(?totalp) LIMIT 5";

          JSONObject author;
          // CONSULTA PARA OBTENER LOS CINCO INVESTIGADORES DE CADA U
          TupleQueryResult resultAuthors = con.prepareTupleQuery(QueryLanguage.SPARQL, query2).evaluate();
          Integer contPub = 0;
          while (resultAuthors.hasNext()) {
            BindingSet bind2 = resultAuthors.next();
            contPub++;
            //Form the Json object
            author = new JSONObject();
            String researcherName = bind2.getValue("researcher").stringValue();
            String totalPubs = bind2.getValue("totalp").stringValue();
            author.put("universityName", uniName);
            author.put("numberResearcher", contPub);
            author.put("name", researcherName);
            author.put("numberPublications", totalPubs);

            authors.add(author);
          }

        }

        con.close();

        return new String[]{authors.toString()};

      } catch (RepositoryException ex) {
        java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
        con.close();
      }

      return new String[]{"", ""};

    } catch (MalformedQueryException | QueryEvaluationException | RepositoryException ex) {
      java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
      ex.printStackTrace();
    }
    return new String[]{"", ""};
  }

  /**
   * Extract Json with publications related by cluster
   *
   * @param keyword Keyword to search
   * @param hostname Hostname
   * @return Array of strings
   */
  public String[] getJSONPublicationsByKeyword(String keyword, String hostname) {
    String getQuery = "";
    try {
      //Variables to return with name and number of publications
      String id = "";
      String authors = "";
      String title = "";
      String abst = "";
      String uri = "";
      String universidad = "";
      Integer cont = 0;
      //Query
      getQuery = ConstantServiceImpl.PREFIX
              + "Select ?publicationUri (GROUP_CONCAT(distinct ?name;separator='; ') as ?names) ?title ?abstract ?uri ?provname "
              + "WHERE "
              + "{ "
              + "  GRAPH <" + constant.getCentralGraph() + "> "
              + "  { "
              + "      ?subject foaf:publications ?publicationUri . "
              + "      ?subject foaf:name ?name . "
              + "      ?publicationUri dct:title ?title . "
              + "      OPTIONAL{ ?publicationUri bibo:abstract  ?abstract. } "
              + "      OPTIONAL{ ?publicationUri bibo:uri  ?uri. } "
              + "      ?publicationUri dct:subject [rdfs:label ?quote].  "
              + "      FILTER (mm:fulltext-search(?quote, '" + keyword + "' )) . "
              + "      BIND(REPLACE( '" + keyword + "', ' ', '_', 'i') AS ?key) . "
              + "      BIND(IRI(?key) as ?keyword) "
              + "      ?subject schema:memberOf ?provenance. "
              + "          { "
              + "             SELECT DISTINCT ?provenance (STR(?pname) as ?provname)"
              + "             WHERE"
              + "             {                                                                                                                                                                                                                                                                                                                     "
              + "                graph <" + constant.getOrganizationsGraph() + "> "
              + "                { "
              + "                  ?provenance uc:fullName ?pname. "
              + "                  FILTER (lang(?pname) = \"es\"). "
              + "                } "
              + "             } "
              + "		  }"
              + "  } "
              + "} group by ?publicationUri ?title ?abstract ?uri ?provname";

      RepositoryConnection con = sesameService.getRepositoryConnetion();
      try {
        // perform operations on the connection
        TupleQueryResult resulta = con.prepareTupleQuery(QueryLanguage.SPARQL, getQuery, constant.getSubjectResource()).evaluate();

        JSONArray publications = new JSONArray();
        JSONObject publication = new JSONObject();

        while (resulta.hasNext()) {
          BindingSet binding = resulta.next();
          //Form the Json object
          publication = new JSONObject();

          id = binding.getValue("publicationUri").stringValue();
          authors = binding.getValue("names").stringValue();
          title = binding.getValue("title").stringValue();
          if (binding.hasBinding("abstract")) {
            abst = binding.getValue("abstract").stringValue();
          }
          if (binding.hasBinding("uri")) {
            uri = binding.getValue("uri").stringValue();
          }
          universidad = binding.getValue("provname").stringValue();

          publication.put("id", id);
          publication.put("authors", authors);
          publication.put("title", title);
          publication.put("abstract", abst);
          publication.put("uri", (uri == null || uri == "" || uri == "null") ? null : uri);
          publication.put("universidad", universidad);

          publications.add(publication);

          /*name = String.valueOf(binding.getValue("publicationUri").stringValue();
                     String authorName = String.valueOf(binding.getValue("author").stringValue();
                     if (!autMap.containsKey(authorName)) {
                     autMap.put(authorName, new JSONObject());
                     autMap.get(authorName).put("author", authorName);
                     if (binding.getValue("keywords") != null) {
                     autMap.get(authorName).put("keywords", String.valueOf(binding.getValue("keywords")).replace("\"", ""));
                     }
                     //Keywords
                     keyMap.put(authorName, new JSONArray());
                     keyMap.get(authorName).add(String.valueOf(binding.getValue("keywords")).replace("\"", "").replace("^^", ""));
                     } else {
                     keyMap.get(authorName).add(String.valueOf(binding.getValue("keywords")).replace("\"", "").replace("^^", ""));
                     }*/
        }

        /*for (Map.Entry<String, JSONObject> aut: autMap.entrySet()) {
                 aut.getValue().put("keywords", keyMap.get(aut.getKey()));
                 authors.add(aut.getValue());
                 }*/
        con.close();
        //Number of authors
        cont = publications.size();
        return new String[]{publications.toString(), keyword, cont.toString()};
      } catch (RepositoryException ex) {
        java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
        con.close();
      }

      return new String[]{"", ""};
    } catch (MalformedQueryException | QueryEvaluationException | RepositoryException ex) {
      java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
      ex.printStackTrace();
    }
    return new String[]{"", ""};
  }

  /**
   * Extract Json with publications by author
   *
   * @param author Author to search
   * @param hostname Hostname
   * @return Array of strings
   */
  public String[] getJSONReportPublicationsByAuthor(String author, String hostname) {
    String getQuery = "";
    try {
      //Variables to return with name and number of publications
      String id = "";
      String authorName = "";
      String title = "";
      String abst = "";
      String uri = "";
      String keywords = "";
      Integer cont = 0;
      //Query
      getQuery = ConstantServiceImpl.PREFIX
              + " Select ?publicationUri ?name "
              + " ?title ?abstract ?uri (GROUP_CONCAT(distinct ?quote;separator='; ') as ?keywords) "
              + "WHERE "
              + "{"
              + "  GRAPH <" + constant.getCentralGraph() + ">"
              + "  {"
              + "      <" + author + "> foaf:publications ?publicationUri ."
              + "      <" + author + "> foaf:name ?name ."
              + "      ?publicationUri dct:title ?title . "
              + "      OPTIONAL{ ?publicationUri bibo:abstract  ?abstract. } "
              + "      OPTIONAL{ ?publicationUri bibo:uri  ?uri. } "
              + "      OPTIONAL{?publicationUri dct:subject [rdfs:label ?quote].} "
              + "  }"
              + "} group by ?publicationUri ?title ?abstract ?uri ?name";

      RepositoryConnection con = sesameService.getRepositoryConnetion();
      try {
        // perform operations on the connection
        TupleQueryResult resulta = con.prepareTupleQuery(QueryLanguage.SPARQL, getQuery).evaluate();

        JSONArray publications = new JSONArray();
        JSONObject publication = new JSONObject();

        while (resulta.hasNext()) {
          BindingSet binding = resulta.next();
          //Form the Json object
          publication = new JSONObject();

          id = binding.getValue("publicationUri").stringValue();
          authorName = binding.getValue("name").stringValue();
          title = binding.getValue("title").stringValue();
          if (binding.hasBinding("abstract")) {
            abst = binding.getValue("abstract").stringValue();
          }
          if (binding.hasBinding("uri")) {
            uri = binding.getValue("uri").stringValue();
          }
          if (binding.hasBinding("keywords")) {
            keywords = binding.getValue("keywords").stringValue();
          }

          publication.put("id", id);
          publication.put("title", title);
          publication.put("abstract", (abst == null || abst == "" || abst == "null") ? null : abst);
          publication.put("uri", (uri == null || uri == "" || uri == "null") ? null : uri);
          publication.put("keywords", (keywords == null || keywords == "" || keywords == "null") ? null : keywords);

          publications.add(publication);

        }

        con.close();
        //Number of publications
        cont = publications.size();
        return new String[]{publications.toString(), authorName, cont.toString()};
      } catch (RepositoryException ex) {
        java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
        con.close();
      }

      return new String[]{"", ""};
    } catch (MalformedQueryException | QueryEvaluationException | RepositoryException ex) {
      java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
      ex.printStackTrace();
    }
    return new String[]{"", ""};
  }

  public String[] getJSONAuthorsbyIES(String ies, String hostname) {

    try {
      String queryAuthors = ConstantServiceImpl.PREFIX
              + "SELECT ?author (max(str(?name_)) as ?name ) (COUNT(distinct ?publication ) as ?totalPub)"
              + "WHERE {  "
              + "  GRAPH <" + constant.getCentralGraph() + ">  {"
              + "    ?author foaf:publications ?publication ;"
              + "       schema:memberOf ?endpoint ."
              + "    ?author foaf:name ?name_ ."
              + "    {"
              + "    	SELECT ?endpoint {"
              + "        	GRAPH <" + constant.getOrganizationsGraph() + "> {"
              + "          ?endpoint uc:name ?nameIES .\n"
              + "          FILTER(STR(?nameIES)=\"" + ies + "\")"
              + "            }"
              + "        }"
              + "    }"
              + "  }"
              + "} "
              + "GROUP BY ?author "
              + "ORDER BY DESC(?totalPub)";

      String queryIES = ConstantServiceImpl.PREFIX
              + "SELECT (STR(?name) as ?fname)"
              + "WHERE {"
              + "  GRAPH <" + constant.getOrganizationsGraph() + ">  {"
              + "    ?endpoint uc:name ?acronym ."
              + "    ?endpoint uc:fullName ?name ."
              + "    FILTER (STR(?acronym)=\"" + ies + "\" && lang(?name)=\"es\").  "
              + "  }"
              + "}";

      RepositoryConnection connection = sesameService.getRepositoryConnetion();

      try {
        // perform operations on the connection
        TupleQueryResult resultAuthors = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryAuthors).evaluate();
        JSONArray authors = new JSONArray();
        JSONObject author;
        int totalAuthors = 0;
        String fname = "";

        //Check authors of each university
        while (resultAuthors.hasNext()) {
          BindingSet binding = resultAuthors.next();
          String name = binding.getValue("name").stringValue();
          String totalPub = binding.getValue("totalPub").stringValue();

          author = new JSONObject();
          author.put("author", name);
          author.put("numPub", totalPub);

          authors.add(author);
          totalAuthors++;
        }

        TupleQueryResult resultIES = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryIES).evaluate();
        if (resultIES.hasNext()) {
          BindingSet binding = resultIES.next();
          fname = String.valueOf(binding.getValue("fname"));
        }

        connection.close();

        return new String[]{authors.toString(), fname, String.valueOf(totalAuthors)};

      } catch (RepositoryException ex) {
        java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
        connection.close();
      }

      return new String[]{"", ""};

    } catch (MalformedQueryException | QueryEvaluationException | RepositoryException ex) {
      java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
      ex.printStackTrace();
    }
    return new String[]{"", ""};
  }

  public String[] getJSONPublicationsByHEI(String hei, String hostname) {
    String getQuery = "";
    try {
      /*getQuery = ConstantServiceImpl.PREFIX
             + " SELECT ?name ?title"
             + "WHERE { "
             + "  GRAPH <http://ucuenca.edu.ec/wkhuska>  {"
             + "    ?author foaf:publications ?publication ;"
             + "       dct:provenance ?endpoint."
             + "    ?publication dct:title ?title."
             + "    ?author foaf:name ?name ."
             + "    {"
             + "    	SELECT * {"
             + "        	GRAPH <http://ucuenca.edu.ec/wkhuska/endpoints> {"
             + "              ?endpoint uc:name \"" + hei + "\"^^xsd:string ."
             + "            }"
             + "        }"
             + "    }"
             + "  }"
             + "}"
             + "ORDER BY ASC(?name)";*/

      getQuery = ConstantServiceImpl.PREFIX
              + " select * { "
              + "SELECT  distinct ?name ?title ?year (group_concat(?orig; separator = \" \") AS ?origin)  "
              + "WHERE {   "
              + "  GRAPH <" + constant.getCentralGraph() + ">  {  "
              + "    ?author foaf:publications ?publication.  "
              + "    ?author foaf:name ?name .  "
              + "    ?author schema:memberOf ?endpoint.  "
              + "    ?publication dct:title ?title.  "
              + "    optional{?publication dc:date ?year}.  "
              + "    ?publication <http://ucuenca.edu.ec/ontology#origin> ?orig.  "
              + "   	filter(xsd:integer(?year) > 2010).  "
              + "    {      "
              + "      SELECT * {         	  "
              + "      GRAPH <" + constant.getOrganizationsGraph() + ">   "
              + "            {                 "
              + "              ?endpoint uc:name \"UCUENCA\"^^xsd:string .               "
              + "            }           "
              + "      }  "
              + "    }  "
              + "  }  "
              + "}  "
              + " GROUP BY ?title ?name ?year "//+ "ORDER BY ASC (?year)";
              + " } ORDER BY ASC (?year) ";

      RepositoryConnection connection = sesameService.getRepositoryConnetion();

      try {
        // perform operations on the connection
        TupleQueryResult resultAuthors = connection.prepareTupleQuery(QueryLanguage.SPARQL, getQuery).evaluate();
        JSONArray authors = new JSONArray();
        JSONObject author;
        int totalAuthors = 0;
        String fname = "";
        String year = "";

        //Check authors of each university
        while (resultAuthors.hasNext()) {
          BindingSet binding = resultAuthors.next();
          String name = binding.getValue("name").stringValue();
          String title = binding.getValue("title").stringValue();
          if (binding.hasBinding("year")) {
            year = binding.getValue("year").stringValue();
          }
          String origin = binding.getValue("origin").stringValue();

          author = new JSONObject();
          author.put("author", name);
          author.put("title", title);
          author.put("year", year);
          author.put("origin", origin);

          authors.add(author);
          totalAuthors++;
        }

        /*TupleQueryResult resultIES = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryIES).evaluate();
                 if (resultIES.hasNext()) {
                 BindingSet binding = resultIES.next();
                 fname = String.valueOf(binding.getValue("fname"));
                 }*/
        connection.close();

        return new String[]{authors.toString(), fname, String.valueOf(totalAuthors)};

      } catch (RepositoryException ex) {
        java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
        connection.close();
      }

      return new String[]{"", ""};

    } catch (MalformedQueryException | QueryEvaluationException | RepositoryException ex) {
      java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
      ex.printStackTrace();
    }
    return new String[]{"", ""};

  }

  public String[] getJSONStatisticsTopKeywords(String hostname) {

    String getQuery = "";
    try {
      //Query
      getQuery = ConstantServiceImpl.PREFIX
              + " SELECT "
              + "  ?uriArea ?keyword ?total "
              + "WHERE "
              + "{  "
              + "	SELECT  ?keyword (IRI(REPLACE(?keyword, \" \", \"_\", \"i\")) as ?uriArea) ?total "
              + "    WHERE "
              + "	   { "
              + "    	{ "
              + "            SELECT DISTINCT ?keyword (COUNT(DISTINCT ?s) AS ?total) "
              + "            WHERE "
              + "            { "
              + "              GRAPH <" + constant.getCentralGraph() + "> "
              + "              { "
              + "                ?s foaf:publications ?publications. "
              + "                ?publications dct:subject ?keyword_. "
              + "                ?keyword_ rdfs:label ?keyword. "
              + "              } "
              + "            } "
              + "            GROUP BY ?keyword "
              + "            ORDER BY DESC(?total) "
              + "            LIMIT 10 "
              + "        } "
              + "        FILTER(!REGEX(?keyword,\"TESIS\")) "
              + "    } "
              + "} ";

      RepositoryConnection con = sesameService.getRepositoryConnetion();
      try {
        // perform operations on the connection

        TupleQueryResult resulta = con.prepareTupleQuery(QueryLanguage.SPARQL, getQuery, constant.getSubjectResource()).evaluate();

        JSONArray keywords = new JSONArray();

        String uri, key, total;

        while (resulta.hasNext()) {
          BindingSet binding = resulta.next();
          uri = binding.getValue("uriArea").stringValue();
          key = binding.getValue("keyword").stringValue();
          total = binding.getValue("total").stringValue();

          JSONObject keyword = new JSONObject();
          keyword.put("uri", uri);
          keyword.put("key", key);
          keyword.put("total", total);

          keywords.add(keyword);
        }
        con.close();

        return new String[]{keywords.toString(), ""};
      } catch (RepositoryException ex) {
        java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
        con.close();
      }

      return new String[]{"", ""};
    } catch (MalformedQueryException | QueryEvaluationException | RepositoryException ex) {
      java.util.logging.Logger.getLogger(ReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
      ex.printStackTrace();
    }
    return new String[]{"", ""};
  }

}
