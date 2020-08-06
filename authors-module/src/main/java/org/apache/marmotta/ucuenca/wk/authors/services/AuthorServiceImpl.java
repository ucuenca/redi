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
package org.apache.marmotta.ucuenca.wk.authors.services;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import java.io.BufferedReader;
import java.io.InputStream;
//import java.io.BufferedWriter;
//import java.io.FileWriter;
//import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.text.Normalizer;
//import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
//import java.net.URL;
//import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
//import java.util.HashSet;
//import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
//import java.util.Map.Entry;
//import java.util.Set;
//import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.apache.marmotta.commons.vocabulary.SCHEMA;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
//import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
//import org.apache.marmotta.ldclient.exception.DataRetrievalException;
//import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.authors.api.AuthorService;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointFile;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointOAI;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointObject;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointSPARQL;
//import org.apache.marmotta.ucuenca.wk.authors.api.EndpointObject;
//import org.apache.marmotta.ucuenca.wk.authors.api.EndpointService;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointsService;
//import org.apache.marmotta.ucuenca.wk.authors.api.SparqlEndpoint;
import org.apache.marmotta.ucuenca.wk.authors.api.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.AskException;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.UpdateException;
import org.apache.marmotta.ucuenca.wk.authors.services.utils.VIVOExtractor;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Person;
//import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.DisambiguationUtilsService;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
//import org.apache.marmotta.ucuenca.wk.commons.service.DistanceService;
//import org.apache.marmotta.ucuenca.wk.commons.service.KeywordsService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.commons.util.SPARQLUtils;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.BIBO;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.apache.tika.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
//import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
//import org.openrdf.query.BindingSet;
//import org.openrdf.query.MalformedQueryException;
//import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
//import org.openrdf.query.TupleQuery;
//import org.openrdf.query.TupleQueryResult;
//import org.openrdf.repository.RepositoryConnection;
//import org.openrdf.repository.RepositoryException;
//import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
//import org.openrdf.repository.Repository;

/**
 * Default Implementation of {@link AuthorService} Fernando B. CEDIA
 *
 * @author Xavier Sumba
 * @author Jose Cullcay
 * @author Jose Segarra
 */
@ApplicationScoped
public class AuthorServiceImpl implements AuthorService {

    @Inject
    private Logger log;

    @Inject
    private SparqlFunctionsService sparqlFunctionsService;

//    @Inject
//    private ConfigurationService configurationService;
    @Inject
    private QueriesService queriesService;

    @Inject
    private EndpointsService endpointService;

    @Inject
    private ConstantService constantService;

    @Inject
    private ExternalSPARQLService sparqlService;

    @Inject
    private DisambiguationUtilsService disambiguationUtils;
    //@Inject
    //private ProfileValidation pv;
    private static final String STR = "string";

    private static final String OAIPROVNAME = "Dspace";

    private static final String OJSPROVNAME = "Ojs";

    private static final String CERIFPROVNAME = "Cerif";
    /* @Inject 
     private EndpointObject endpointObject;*/

    private static final int LIMIT = 5000;
    //   private static final int MAX_SUBJECTS = 15;
    //   private static List<SparqlEndpoint> endpoints;
    private final List<String> stopwords = new ArrayList<>();
    private int processpercent = 0;

    private final static String COUNTWORD = "count";

    //  private static int upperLimitKey = 5; //Check 6 keywords
    //  private static int lowerLimitKey = upperLimitKey - 1; //Not less than 4 keywords
    //  private PrintWriter out;
//    private static double tolerance = 0.9;
//    private Set<String> setExplored = new HashSet<String>();
//
//    private static int one = 1;
    //  private static final String FILENAME = "DesambiguacionAutoresLog.csv";
    //   private Set<Entry> pairsCompared = null;
//    private BufferedWriter bw = null;
    //private FileWriter fw = null;
    @PostConstruct
    public void init() {
        BufferedReader input = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("helpers/stoplist.txt")));
        LineIterator it = new LineIterator(input);
        String line;
        while (it.hasNext()) {
            line = it.nextLine();
            String[] words = line.split("\\s+");
            stopwords.addAll(Arrays.asList(words));
        }
        it.close();

//        filterProperties = Arrays.asList("http://www.w3.org/2004/02/skos/core#prefLabel",
//                "http://www.w3.org/2000/01/rdf-schema#comment",
//                "http://www.w3.org/ns/dcat#contactPoint",
//                "http://www.w3.org/ns/dcat#landingPage",
//                "http://vivoweb.org/ontology/core#freetextKeyword",
//                "http://www.w3.org/2002/07/owl#disjointWith", "http://rdaregistry.info",
//                "http://www.w3.org/2000/01/rdf-schema#label", "http://purl.org");
    }

    private String extractAuthorsORCID(String l, String org, String end) {
        String rs = "";
        try {
            SSLContext sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                    .build();

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setSSLSocketFactory(sslsf)
                    .build();
            Unirest.setHttpClient(httpclient);
            HttpResponse<JsonNode> asJson = Unirest.get(l + "profileval/obtainAuthors")
                    .queryString("org", org)
                    .asJson();
            if (asJson.getStatus() == HttpURLConnection.HTTP_OK) {
                JsonNode body = asJson.getBody();
                JSONArray jsonArray = body.getObject().getJSONArray("data");
                Model m = new LinkedHashModel();
                ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String orcid = jsonObject.getString("_id");
                    JSONObject jsonObject1 = jsonObject.getJSONObject("profile");
                    String fullname = jsonObject1.getString("name");
                    String fname = jsonObject1.getString("fname");
                    String lname = jsonObject1.getString("lname");
                    String mail = jsonObject1.getString("email");
                    String orgx = org.replaceAll(constantService.getOrganizationBaseUri(), "");
                    URI createURI = instance.createURI(constantService.getAuthorResource() + "orcid/" + orgx + "/" + orcid);
                    m.add(createURI, RDF.TYPE, FOAF.PERSON);
                    m.add(createURI, FOAF.NAME, instance.createLiteral(fullname));
                    m.add(createURI, FOAF.FIRST_NAME, instance.createLiteral(fname));
                    m.add(createURI, FOAF.LAST_NAME, instance.createLiteral(lname));
                    m.add(createURI, instance.createURI("http://www.w3.org/2006/vcard/ns#hasEmail"), instance.createLiteral(mail));
                    m.add(createURI, DCTERMS.PROVENANCE, instance.createURI(end));
                    m.add(createURI, instance.createURI("http://purl.org/spar/scoro/hasORCID"), instance.createURI("https://orcid.org/" + orcid));
                }
                RepositoryConnection repositoryConnetion = sparqlService.getRepositoryConnetion();
                repositoryConnetion.begin();
                sparqlService.getGraphDBInstance().runSplitAddOp(repositoryConnetion, m, instance.createURI(constantService.getAuthorsGraph()));
                repositoryConnetion.commit();
                repositoryConnetion.close();
                rs = "Success " + jsonArray.length() + "/" + jsonArray.length();
            }
        } catch (Exception ex) {
            log.debug("Error {}", ex);
            rs = "Fail " + ex;
        }
        return rs;
    }

    @SuppressWarnings("PMD")
    private String extractAuthorsVIVO(String org, String end, String url) {
        ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
        String rs = "" + org + end + url;
        try {
            Model output = new LinkedHashModel();
            Model output2 = new LinkedHashModel();
            SSLContext sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                    .build();

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setSSLSocketFactory(sslsf)
                    .build();
            Unirest.setHttpClient(httpclient);

            //Obatin researchers URIS
            HttpResponse<String> respn = Unirest.get(url + "/listrdf?vclass=http%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2FPerson")
                    .asString();
            RepositoryConnection repositoryConnetion = sparqlService.getRepositoryConnetion();
            if (respn.getStatus() == HttpURLConnection.HTTP_OK) {
                InputStream toInputStream = IOUtils.toInputStream(respn.getBody());
                Model parse = Rio.parse(toInputStream, constantService.getBaseContext(), RDFFormat.RDFXML);
                Set<Resource> subjects = parse.filter(null, RDF.TYPE, FOAF.PERSON).subjects();
                String orgx = org.replaceAll(constantService.getOrganizationBaseUri(), "");
                String authBase = constantService.getAuthorResource() + "vivo/" + orgx + "/";
                String projBase = constantService.getProjectResource() + "vivo/" + orgx + "/";
                Model sameAsMappins = new LinkedHashModel();
                int co = 0;
                for (Resource rsx : subjects) {
                    log.info("Extracting author {}, {}/{}", rsx.stringValue(), co, subjects.size());
                    VIVOExtractor.download("person", url, output, rsx, null, end, true, authBase, projBase, sameAsMappins);
                    //
                    VIVOExtractor.linkOrganizations(disambiguationUtils, output, sameAsMappins);
                    VIVOExtractor.linkOrganizations2(disambiguationUtils, output, output2, end);
                    //
                    repositoryConnetion.begin();
                    sparqlService.getGraphDBInstance().runSplitAddOp(repositoryConnetion, output, instance.createURI(constantService.getAuthorsGraph() + "_Beta"));
                    sparqlService.getGraphDBInstance().runSplitAddOp(repositoryConnetion, output2, instance.createURI(constantService.getEndpointsGraph() + "_Beta"));
                    sparqlService.getGraphDBInstance().runSplitAddOp(repositoryConnetion, sameAsMappins, instance.createURI(constantService.getAuthorsGraph() + "_SA"));
                    repositoryConnetion.commit();
                    output.clear();
                    co++;
                }
                //Delete
                SPARQLUtils sparqlUtils = new SPARQLUtils(sparqlService.getSparqlService());
                sparqlUtils.replaceSameAs(constantService.getAuthorsGraph() + "_Beta", constantService.getAuthorsGraph() + "_SA");

            } else {
                throw new Exception("Invalid VIVO URL " + url);
            }
            repositoryConnetion.close();
            rs = "Success " + output.size() + "/" + output.size();
        } catch (Exception ex) {
            ex.printStackTrace();
            log.debug("Error {}", ex);
            rs = "Fail " + ex;
        }
        return rs;
    }

    /**
     * authorDocumentProperty : http://rdaregistry.info/Elements/a/P50161 |
     * http://rdaregistry.info/Elements/a/P50195
     *
     * // * @throws
     * org.apache.marmotta.ucuenca.wk.authors.exceptions.DaoException //
     *
     *
     * @throws org.apache.marmotta.ucuenca.wk.authors.exceptions.UpdateException
     */
    //private String documentProperty = "http://rdaregistry.info";
    @Override
    @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
    public String extractAuthorsGeneric(String l, String... endpoints) {

        ConcurrentHashMap msg = new ConcurrentHashMap();
        for (String endpoint : endpoints) {
            try {
                String queryEndpoint = queriesService.getListEndpointsByUri(endpoint);
                List<Map<String, Value>> result = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, queryEndpoint);
                if (!result.isEmpty()) {
                    Map<String, Value> map = result.get(0);
                    String type = map.get("type").stringValue();
                    String status = map.get("status").stringValue();
                    String org = map.get("org").stringValue();
                    String graph = map.get("graph").stringValue();
                    String url = map.get("url").stringValue();
                    Boolean mode = false;
                    if (map.containsKey("mode")) {
                        mode = Boolean.valueOf(map.get("mode").stringValue());
                    }
                    // String uri = map.get("URI").stringValue();
                    EndpointObject e;

                    String extractResult = "";
                    if ("file".equals(type)) {
                        e = new EndpointFile(status, org, url, type, endpoint);
                        //  EndpointsObject.add(e);
                        extractResult = extractAuthorGeneric(e, "0", false, false);

                    } else if ("sparql".equals(type)) {
                        e = new EndpointSPARQL(status, org, url, type, graph, endpoint);
                        extractResult = extractAuthorGeneric(e, "1", false, false);
                        // EndpointsObject.add(e);
                    } else if ("orcid".equals(type)) {
                        //Read from mongo cache.
                        extractResult = extractAuthorsORCID(l, org, endpoint);
                    } else if ("vivo".equals(type)) {
                        //Read from mongo cache.
                        extractResult = extractAuthorsVIVO(org, endpoint, url);
                    } else {

                        String min = "1";
                        String[] urls = url.split(";");
                        for (String u : urls) {
                            e = new EndpointOAI(status, org, u, type, endpoint, mode);
                            if ("cerif".equals(type)) {
                                min = "0";
                            }
                            extractResult = extractAuthorGeneric(e, min, false, mode);
                            String nametype = type;
                            if (extractResult.contains("Success")) {
                                if ("oai-pmh".equals(type)) {
                                    nametype = OAIPROVNAME;
                                } else if ("ojs".equals(type)) {
                                    nametype = OJSPROVNAME;
                                } else if ("cerif".equals(type)) {
                                    nametype = CERIFPROVNAME;
                                }
                                String providerUri = createProvider(nametype, constantService.getAuthorsGraph(), true);
                                registerDate(org, providerUri, extractResult, nametype, constantService.getAuthorsGraph());
                            } else {
                                break;
                            }
                        }
                        // EndpointsObject.add(e);
                    }

                    if (extractResult.contains("Success")) {
                        Date date = new Date();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                        endpointService.updateExtractionDate(endpoint, dateFormat.format(date));
                        // "Success: " + processedAuthors + "/" + totalAuthors

                    }

                    msg.put(endpoint, extractResult);

                } else {
                    msg.put(endpoint, "Not found");
                }

                // msg.put(endpoint, "Success");
            } catch (MarmottaException ex) {
                java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                msg.put(endpoint, ex);
            } catch (UpdateException ex) {
                java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                msg.put(endpoint, ex);
            }

        }
        try {
            return mapTojson(msg);
        } catch (JSONException ex) {
            java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return "fail";
        }
    }

    private String createProvider(String providerName, String providerGraph, Boolean main) throws UpdateException {
        String providerUri = constantService.getProviderBaseUri() + "/" + providerName.toUpperCase().replace(" ", "_");
        String queryProvider = queriesService.getAskResourceQuery(providerGraph, providerUri);
        try {
            boolean result = sparqlService.getSparqlService().ask(QueryLanguage.SPARQL, queryProvider);

            if (!result) {
                executeInsert(providerGraph, providerUri, RDF.TYPE.toString(), REDI.PROVIDER.toString());
                executeInsert(providerGraph, providerUri, RDFS.LABEL.toString(), providerName, "string");

                if (main) {
                    //   sparqlFunctionsService.executeInsert(providerGraph, providerUri, REDI.MAIN.toString(), "True", "boolean");
                    executeInsert(providerGraph, providerUri, REDI.MAIN.toString(), "true", "boolean");

                } else {
                    executeInsert(providerGraph, providerUri, REDI.MAIN.toString(), "false", "boolean");

                }
            }

            return providerUri;
        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    private void registerDate(String org, String providerUri, String detail, String getProviderName, String getProviderGraph) throws UpdateException {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        String uriEvent = createExtractEventUri(getProviderName, org);
        executeInsert(getProviderGraph, uriEvent, RDF.TYPE.toString(), REDI.EXTRACTION_EVENT.toString());
        executeInsert(getProviderGraph, providerUri, REDI.BELONGTO.toString(), uriEvent);
        executeInsert(constantService.getOrganizationsGraph(), org, REDI.BELONGTO.toString(), uriEvent);
        executeInsert(getProviderGraph, uriEvent, REDI.EXTRACTIONDATE.toString(), dateFormat.format(date), STR);
        executeInsert(getProviderGraph, uriEvent, RDFS.LABEL.toString(), dateFormat.format(date) + " | " + detail, STR);

    }

    public void executeInsert(String graph, String subject, String property, String object, String type) throws UpdateException {
        String query = queriesService.buildInsertQuery(graph, subject, property, object, type);
        sparqlFunctionsService.updateAuthor(query);
    }

    public void executeInsert(String graph, String subject, String property, String object) throws UpdateException {
        String query = queriesService.buildInsertQuery(graph, subject, property, object);
        sparqlFunctionsService.updateAuthor(query);
    }

    private String createExtractEventUri(String providerName, String org) {
        char slash = '/';
        String orgName = org.substring(org.lastIndexOf(slash) + 1);

        return constantService.getEndpointBaseEvent() + providerName.replace(' ', '_') + "_" + orgName.replace(' ', '_');

    }

    public String mapTojson(Map<String, String> map) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            try {
                jsonObj.put(key, value);

            } catch (org.json.JSONException ex) {
                java.util.logging.Logger.getLogger(OrganizationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jsonObj.toString();
    }

    @SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.UnusedPrivateMethod", "PMD.AvoidDuplicateLiterals", "PMD.NcssMethodCount", "PMD.NPathComplexity"})
    private String extractAuthorGeneric(EndpointObject endpoint, String min, Boolean mode, Boolean tempGraph) {
        int tripletasCargadas = 0; //cantidad de tripletas actualizadaas
        int contAutoresNuevosNoCargados = 0; //cantidad de actores nuevos no cargados
        int contAutoresNuevosEncontrados = 0; //hace referencia a la cantidad de actores existentes en el archivo temporal antes de la actualizacion

        if (endpoint.prepareQuery()) {
            log.info("Endpoint listo");
            int authorsSize = 0;
            String query = queriesService.getCountPersonQuery(endpoint.getGraph(), min, mode);
            List<HashMap> result = endpoint.querySource(query);
            if (!result.isEmpty()) {
                authorsSize = Integer.parseInt((String) result.get(0).get(COUNTWORD));
                log.info("N. Autores: " + authorsSize);
            } else {
                return "Problema en las consultas";
            }
            String getAuthorsQuery = queriesService.getAuthorsQuery(endpoint.getGraph(), min, mode);

            for (int offset = 0; offset < authorsSize; offset += 5000) {

                // TupleQueryResult authorsResult = executelocalquery (getAuthorsQuery + getLimitOffset(LIMIT, offset) , repo );//conn.prepareTupleQuery(QueryLanguage.SPARQL, getAuthorsQuery + getLimitOffset(limit, offset)).evaluate();
                List<HashMap> listAuthors = endpoint.querySource(getAuthorsQuery + getLimitOffset(LIMIT, offset));
                String resource = "";
                for (HashMap resultmap : listAuthors) {
                    try {
                        // resource = authorsResult.next().getValue("s").stringValue();
                        resource = resultmap.get("s").toString();
                        String localResource = buildLocalURI(resource, endpoint.getType(), endpoint.getName());
                        int npub = 0;
                        int localnpub = 0;
                        boolean askauthor = sparqlFunctionsService.askAuthor(queriesService.getAskObjectQuery(constantService.getAuthorsGraph(), resource));
                        if (askauthor) {
                            // int npub = (int) endpoint.querySource(countPublications(resource)).get(0);
                            List<HashMap> pub = endpoint.querySource(countPublications(resource));
                            npub = Integer.parseInt(pub.get(0).get("npub").toString());
                            localnpub = Integer.parseInt(sparqlFunctionsService.querylocal(countPublicationslocal(localResource)).get(0).get("npub").stringValue());
                        }
                        String provenance = endpoint.getResourceId();
                        if (!askauthor || npub > localnpub) {
                            contAutoresNuevosEncontrados++;
                            printPercentProcess(contAutoresNuevosEncontrados, authorsSize, endpoint.getName());
                            log.info("Update : " + localResource + " NPub:" + localnpub + " ->" + npub);
                            //  String localResource = buildLocalURI(resource);
                            // String localResource = buildLocalURI(resource, endpoint.getType(), endpoint.getName());
                            //String   queryAuthor = "Select * where {<"+resource+"> ?y ?z}";
                            // TupleQueryResult tripletasResult =  executelocalquery ( queryAuthor , repo);

                            List<HashMap> describeAuthor = endpoint.querySource(queriesService.getAuthorsPropertiesQuery(resource));
                            for (HashMap des : describeAuthor) {
                                //BindingSet tripletsResource = tripletasResult.next();                   
                                String predicate = des.get("property").toString();
                                // tripletsResource.getValue("y").stringValue();
                                String object = des.get("object").toString();
                                log.info(des + "");
                                // tripletsResource.getValue("z").stringValue();
                                log.info(predicate + "-" + object + "-" + des.get("type"));

                                String insert = "";
                                switch (predicate) {
                                    case "http://xmlns.com/foaf/0.1/givenName":// store foaf:firstName
                                    case "http://xmlns.com/foaf/0.1/firstName":
                                        insert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, FOAF.FIRST_NAME.toString(), object);
                                        //  sparqlFunctionsService.updateAuthor(insert);
                                        break;
                                    case "http://xmlns.com/foaf/0.1/familyName": // store foaf:lastName
                                    case "http://xmlns.com/foaf/0.1/lastName":
                                        insert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, FOAF.LAST_NAME.toString(), object);
                                        //   sparqlFunctionsService.updateAuthor(insert);
                                        break;
                                    case "http://xmlns.com/foaf/0.1/name": // store foaf:name
                                        insert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, FOAF.NAME.toString(), object);
                                        //  sparqlFunctionsService.updateAuthor(insert);
                                        break;
                                    case "http://purl.org/spar/scoro/hasORCID": // store foaf:name
                                        insert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, "http://purl.org/spar/scoro/hasORCID", object);
                                        //  sparqlFunctionsService.updateAuthor(insert);
                                        break;
                                    case "http://www.w3.org/2006/vcard/ns#hasEmail":
                                        insert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, "http://www.w3.org/2006/vcard/ns#hasEmail", object);
                                        //  sparqlFunctionsService.updateAuthor(insert);
                                        break;
                                    case "http://rdaregistry.info/Elements/u/P60095": // store foaf:name
                                        insert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, SCHEMA.affiliation.toString(), object);
                                        if (tempGraph) {
                                            String resp = generateTempEndpoint(object, endpoint.getType(), endpoint.getAccess());
                                            if (resp != null) {
                                                provenance = resp;
                                            }
                                            // sparqlFunctionsService.updateAuthor(insert);
                                        }
                                        break;
                                    case "http://purl.org/dc/terms/isVersionOf":
                                        insert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, DCTERMS.IS_VERSION_OF.toString(), buildLocalURI(object, endpoint.getType(), endpoint.getName()));
                                        // sparqlFunctionsService.updateAuthor(insert);
                                        break;

                                    case "https://www.openaire.eu/cerif-profile/1.1/MemberOf":
                                        createproject(localResource, object, endpoint);

                                        break;
                                    case "http://www.w3.org/2002/07/owl#sameAs": // If sameas found include the provenance
                                        //SparqlEndpoint newEndpoint = matchWithProvenance(object);
                                        /* if (newEndpoint != null) {
                                         String provenanceQueryInsert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, DCTERMS.PROVENANCE.toString(), newEndpoint);
                                         sparqlFunctionsService.updateAuthor(provenanceQueryInsert);
                                         }*/
                                        insert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, OWL.SAMEAS.toString(), object);

                                        break;
                                    case "http://rdaregistry.info/Elements/a/P50195":

                                    case "http://rdaregistry.info/Elements/a/P50161":

                                        if (des.containsKey("type")) {
                                            String type = des.get("type").toString();
                                            createDoc(localResource, object, type, endpoint, predicate);
                                        }
                                        break;

                                    default:

                                }
                                if (!("http://rdaregistry.info/Elements/a/P50161".equals(predicate) || "http://rdaregistry.info/Elements/a/P50161".equals(predicate))) {
                                    sparqlFunctionsService.updateAuthor(insert);
                                }
                            }

                            String sameAs = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, OWL.SAMEAS.toString(), resource);
                            sparqlFunctionsService.updateAuthor(sameAs);

                            String provenanceQueryInsert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, DCTERMS.PROVENANCE.toString(), provenance);
                            sparqlFunctionsService.updateAuthor(provenanceQueryInsert);

                            String foafPerson = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, RDF.TYPE.toString(), FOAF.PERSON.toString());
                            sparqlFunctionsService.updateAuthor(foafPerson);

                            //conn.commit();
                            //conn.close();
                        } else {
                            log.info("Autor ya procesado : " + localResource + " NPub:" + localnpub + " ->" + npub);
                        }
                    } catch (AskException | UnsupportedEncodingException | UpdateException ex) {
                        java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        return "Fail" + ex;
                    }

                }

            }
            log.info(endpoint.getName() + " . Se detectaron " + contAutoresNuevosEncontrados + " autores nuevos ");
            log.info(endpoint.getName() + " . Se cargaron " + (contAutoresNuevosEncontrados - contAutoresNuevosNoCargados) + " autores nuevos exitosamente");
            log.info(endpoint.getName() + "  . Se cargaron " + tripletasCargadas + " tripletas ");
            log.info(endpoint.getName() + " . No se pudieron cargar " + contAutoresNuevosNoCargados + " autores");
            //    List<HashMap> describeAuthor0 = endpoint.querySource("Select * where {?a ?b ?c }limit 100");
            //    List<HashMap> describeAuthor1 = endpoint.querySource("PREFIX bibo: <http://purl.org/ontology/bibo/> Select * where {?a  a bibo:Document}limit 10");
            // log.info ("Extrayendo Subjects");
            /*  try {
             //   extractSubjects (endpoint);
             } catch (    RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
             java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
             return "Problema Extrayendo Subjects";
             }*/
            endpoint.closeconnection();
            return "Success: " + authorsSize + "/" + authorsSize;
        } else {
            return "Fail: Access";
        }
    }

    @SuppressWarnings({"PMD.AvoidLiteralsInIfCondition"})
    private String generateTempEndpoint(String org, String type, String url) {

        List<String> lo = new ArrayList();
        String nurl = url + "-" + org;
        try {
            lo.add(org.replace("-", ""));
            List<String> uriorg = disambiguationUtils.lookForOrganizations(lo);
            if (!(uriorg == null || uriorg.isEmpty() || uriorg.get(0).isEmpty())) {
                String resp = endpointService.registerOAI(type, uriorg.get(0).substring(uriorg.get(0).lastIndexOf('/') + 1), nurl, false, true);
                log.info("Temporal Endpoint" + resp);
                if ("Successfull Registration".equals(resp) || "Endpoint Already Exist".equals(resp)) {
                    return constantService.getEndpointBaseUri() + type + "/" + org + "_temp";
                }
            }
        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private String buildLocalURI(String resource, String type, String name) {
        return constantService.getAuthorResource() + name + "/" + type + "/" + resource.substring(resource.lastIndexOf('/') + 1);
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String countPublications(String resource) {
        String autor = "<" + resource + ">";
        return "SELECT   (COUNT (DISTINCT  ?pub) as ?npub)\n"
                + " WHERE { \n"
                + autor + " a <http://xmlns.com/foaf/0.1/Person> . \n"
                + "   {\n"
                + autor + " <http://rdaregistry.info/Elements/a/P50195> ?pub .\n"
                + " } UNION {\n"
                + autor + "  <http://rdaregistry.info/Elements/a/P50161>  ?pub .   \n"
                + "   }"
                + " ?pub a  <http://purl.org/ontology/bibo/Article>"
                + "} ";
    }

    private String countPublicationslocal(String resource) {
        return "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "SELECT ( COUNT (Distinct  ?pub) as ?npub ) WHERE {\n"
                + "  Graph <" + constantService.getAuthorsGraph() + "> {\n"
                + " <" + resource + "> foaf:publications ?pub\n"
                + "  }\n"
                + "} ";

    }

    @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
    private void createproject(String uri, String object, EndpointObject endpoint) throws UpdateException {
        String query = queriesService.getPublicationDetails(object);
        List<HashMap> describeProject = endpoint.querySource(query);
        String property = "";
        String value = "";
        for (HashMap result : describeProject) {

            if (result.containsKey("property") && result.containsKey("hasValue")) {
                property = result.get("property").toString();
                value = result.get("hasValue").toString();
                executeInsert(constantService.getAuthorsGraph(), object, property, value);
            }

        }

        executeInsert(constantService.getAuthorsGraph(), uri, "https://www.openaire.eu/cerif-profile/1.1/linksToProject", object);
    }

    @SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.ExcessiveMethodLength"})
    private void createDoc(String uri, String object, String type, EndpointObject e, String relation) throws UpdateException, UnsupportedEncodingException {
        if ("http://purl.org/ontology/bibo/Article".equals(type)) {

            String query = queriesService.getPublicationDetails(object);
            List<HashMap> describePub = e.querySource(query);
            executeInsert(constantService.getAuthorsGraph(), object, RDF.TYPE.toString(), BIBO.ACADEMIC_ARTICLE.toString());
            ConcurrentHashMap journal = new ConcurrentHashMap();
            for (HashMap result : describePub) {
                String property = "";
                String value = "";
                if (result.containsKey("property") && result.containsKey("hasValue")) {
                    property = result.get("property").toString();
                    value = result.get("hasValue").toString();

                    switch (property) {
                        case "http://purl.org/ontology/bibo/uri":
                            executeInsert(constantService.getAuthorsGraph(), object, BIBO.URI.toString(), value);

                            break;
                        case "http://purl.org/dc/terms/abstract":
                            executeInsert(constantService.getAuthorsGraph(), object, BIBO.ABSTRACT.toString(), value.replaceAll("[&@;^\"\\\\]", ""));
                            String abst = value.toLowerCase();
                            extractAbstractSub("keywords:", abst, object);
                            extractAbstractSub("palabras clave:", abst, object);

                            break;
                        case "http://purl.org/dc/terms/title":
                            executeInsert(constantService.getAuthorsGraph(), object, DCTERMS.TITLE.toString(), value.replaceAll("[&@;^\"\\\\]", ""));

                            break;
                        case "http://purl.org/dc/terms/subject":
                            generateSubjects(object, value);

                            break;

                        case "http://purl.org/ontology/bibo/doi":
                            executeInsert(constantService.getAuthorsGraph(), object, BIBO.DOI.toString(), value);

                            break;
                        case "http://purl.org/ontology/bibo/conference":
                            executeInsert(constantService.getAuthorsGraph(), object, BIBO.CONFERENCE.toString(), value);

                            break;

                        case "http://purl.org/dc/terms/publisher":
                            executeInsert(constantService.getAuthorsGraph(), object, DCTERMS.PUBLISHER.toString(), value);

                            break;
                        case "http://purl.org/ontology/bibo/issn":
                            if (!journal.containsKey(property)) {
                                journal.put(property, value);
                            } else {
                                journal.put(property, journal.get(property) + ";" + value);
                            }
                            executeInsert(constantService.getAuthorsGraph(), object, BIBO.ISSN.toString(), value, "integer");

                            break;

                        case "http://purl.org/ontology/bibo/isbn":
                            if (!journal.containsKey(property)) {
                                journal.put(property, value);
                            } else {
                                journal.put(property, journal.get(property) + ";" + value);
                            }
                            executeInsert(constantService.getAuthorsGraph(), object, BIBO.ISBN.toString(), value, "integer");

                            break;
                        case "http://purl.org/ontology/bibo/issue":
                            if (object.matches("^[0-9]+-[0-9]+-[0-9]+")) {
                                executeInsert(constantService.getAuthorsGraph(), object, BIBO.ISSUE.toString(), value, "date");

                            }
                            break;

                        case "http://purl.org/dc/terms/source":
                            journal.put(property, value);
                            break;
                        default:

                            break;
                    }
                }

            }

            if (journal.containsKey(DCTERMS.SOURCE.toString()) && (journal.containsKey(BIBO.ISSN.toString()) || journal.containsKey(BIBO.ISBN))) {
                createJournal(journal, object);
            }

            String rel;
            if ("http://rdaregistry.info/Elements/a/P50195".equals(relation)) {
                rel = DCTERMS.CREATOR.toString();

            } else {
                rel = DCTERMS.CONTRIBUTOR.toString();
            }

            executeInsert(constantService.getAuthorsGraph(), uri, FOAF.PUBLICATIONS.toString(), object);
            executeInsert(constantService.getAuthorsGraph(), object, rel, uri);

        }
    }

    public void generateSubjects(String object, String value) throws UnsupportedEncodingException, UpdateException {
        String uriSubject = constantService.getSubjectResource() + URLEncoder.encode(value.toUpperCase().replace(" ", "_"), "UTF-8");
        executeInsert(constantService.getAuthorsGraph(), object, DCTERMS.SUBJECT.toString(), uriSubject);
        executeInsert(constantService.getAuthorsGraph(), uriSubject, RDFS.LABEL.toString(), value.toUpperCase().replaceAll("[&@;^\"\\\\]", ""), STR);
    }

    public void extractAbstractSub(String k, String abst, String object) throws UnsupportedEncodingException, UpdateException {
        if (abst.contains(k)) {
            String subjects = StringUtils.substringBetween(abst, k, ".");
            if (subjects != null) {
                for (String s : subjects.split(",")) {
                    generateSubjects(object, s.trim());
                }
            }
        }

    }

    public void createJournal(ConcurrentHashMap datajournal, String uriArticle) throws UpdateException {

        String name = datajournal.get("http://purl.org/dc/terms/source").toString();

        String[] issnList = {};
        String[] isbnList = {};

        if (datajournal.containsKey(BIBO.ISSN.toString())) {
            issnList = datajournal.get(BIBO.ISSN.toString()).toString().split(";");
        }

        if (datajournal.containsKey(BIBO.ISBN.toString())) {
            isbnList = datajournal.get(BIBO.ISBN.toString()).toString().split(";");
        }

        String base = "http://REDI/Temporal/OJS/Journal/";
        String url = base + Normalizer.normalize(name.toUpperCase().replace(" ", "_").replaceAll("[,.]", ""), Normalizer.Form.NFD);
        executeInsert(constantService.getAuthorsGraph(), url, RDF.TYPE.toString(), BIBO.JOURNAL.toString());
        executeInsert(constantService.getAuthorsGraph(), url, RDFS.LABEL.toString(), name);
        for (String issn : issnList) {
            executeInsert(constantService.getAuthorsGraph(), url, BIBO.ISSN.toString(), issn);
        }

        for (String isbn : isbnList) {
            executeInsert(constantService.getAuthorsGraph(), url, BIBO.ISBN.toString(), isbn);
        }

        executeInsert(constantService.getAuthorsGraph(), uriArticle, DCTERMS.IS_PART_OF.toString(), url);

    }

    /*
     private void refineSubjectTopic(String localSubject, Set<String> subjects, Set<String> topic) {
     List listTopic = new ArrayList(topic);
     List<String>[] resultTopics = findTopics(new ArrayList(topic), 5, 15);
     Set<String> selectedSubjects = new HashSet<>(getWeightedSubjects(subjects, listTopic));

     // Insert subjects
     for (String keyword : selectedSubjects) {
     if ((!commonsService.isURI(keyword))) {
     try {
     String insertKeywords = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localSubject, DCTERMS.SUBJECT.toString(), kservice.cleaningText(keyword).toUpperCase());
     sparqlFunctionsService.updateAuthor(insertKeywords);
     } catch (UpdateException ex) {
     log.error("Cannot insert new subjects. Error: {}", ex.getMessage());
     }
     }
     }

     // Insert some topics
     for (Object top  : listTopic ) {
     try {
     String insertTopic = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localSubject, FOAF.topic.toString(), top.toString().trim().toUpperCase());
     sparqlFunctionsService.updateAuthor(insertTopic);
     } catch (UpdateException ex) {
     log.error("Cannot insert topics. Error: {}", ex.getMessage());
     }
     }
     //log.info("Resource {} has {} documents and {} subjects ", localSubject, documents.size(), selectedSubjects.size());
       
     }*/
    public boolean compareExactStrings(String string1, String string2) {
        return (string1.matches("^" + string2 + "$") || string2.matches("^" + string1 + "$"));
    }

    public String cleaningTextAuthor(String value) {
        value = value.replace("??", ".*");
        value = value.replace("?", ".*").toLowerCase();
        value = value.replace(" de ", " ");
        value = value.replace("^del ", " ");
        value = value.replace(" del ", " ");
        value = value.replace(" los ", " ");
        value = value.replace(" y ", " ");
        value = value.replace(" las ", " ");
        value = value.replace(" la ", " ");
        // Cadena de caracteres original a sustituir.
        String original = "áàäéèëíìïóòöúùñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
        String output = value;
        for (int i = 0; i < original.length(); i++) {
            // Reemplazamos los caracteres especiales.
            output = output.replace("" + original.charAt(i), ".*");
        }//end for i
        return output.trim();
    }

    public String removeAccents(String value) {
        value = value.replace(".", "");
        value = value.replace("??", ".*").trim();
        value = value.replace("?", ".*");
        // Cadena de caracteres original a sustituir.
        String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
        // Cadena de caracteres ASCII que reemplazarán los originales.
        String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
        String output = value;
        for (int i = 0; i < original.length(); i++) {
            // Reemplazamos los caracteres especiales.
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }//end for i
        return output;
    }//removeAccents
/*
     private TupleQueryResult executeQuery(Repository repository, String query) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
     if (!repository.isInitialized()) {
     repository.initialize();
     }
     RepositoryConnection conn = repository.getConnection();
     conn.begin();
     TupleQueryResult result = conn.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate();
     conn.close();
     return result;
     }*/
 /*
     @Deprecated
     @SuppressWarnings("PMD.UnusedPrivateMethod")
     private Set<String>[] extractSubjectsAndDocuments(LDClientService ldClient, String documentURI)
     throws DataRetrievalException, RepositoryException, MalformedQueryException, QueryEvaluationException {
     Set<String> subjects = new HashSet<>();
     Set<String> documents = new HashSet<>();
     Set<String> mentions = new HashSet<>();

     List<Set<String>> result = new ArrayList<>();
     result.add(subjects);
     result.add(documents);
     result.add(mentions);
     ClientResponse respPub = ldClient.retrieveResource(documentURI);
     String document = "";
     for (Statement statement : respPub.getData()) {
     String value = statement.getObject().stringValue().trim();
     switch (statement.getPredicate().getLocalName()) {
     case "subject":
     subjects.add(value);
     break;
     case "mentions":
     mentions.add(value.substring(value.lastIndexOf('/') + 1).replace("_", " ").toUpperCase().trim());
     break;
     case "title":
     case "abstract":
     document += value + " ";
     break;
     default:
     }
     }
     if (!document.trim().equals("")) {
     documents.add(document);
     }
     return result.toArray(new Set[3]);
     }
     /*
     private String buildLocalURI(String endpointURI) {
     return constantService.getAuthorResource() + endpointURI.substring(endpointURI.lastIndexOf('/') + 1);
     }*/
 /*
     private List<String>[] findTopics(List<String> documents, int numTopics, int numWords) {
     Set<String> topics = new TreeSet<>();

     //File stoplist = new File(getClass().getClassLoader().getResource("/helpers/stoplist.txt"));
     ArrayIterator iterator = new ArrayIterator(documents);

     ArrayList<Pipe> workflow = new ArrayList<>();
     workflow.add(new CharSequence2TokenSequence("\\p{L}+"));
     workflow.add(new TokenSequenceLowercase());
     workflow.add(new TokenSequenceRemoveStopwords(false, false).addStopWords(stopwords.toArray(new String[]{})));
     workflow.add(new TokenSequence2FeatureSequenceWithBigrams());

     InstanceList data = new InstanceList(new SerialPipes(workflow));
     data.addThruPipe(iterator);

     ParallelTopicModel lda = new ParallelTopicModel(numTopics);
     lda.addInstances(data);
     try {
     lda.estimate();
     } catch (IOException ex) {
     log.error("Cannot find topics. Error: {}", ex.getMessage());
     } catch (ArrayIndexOutOfBoundsException ex) {
     log.error("Cannot find {} topics and {} words. Error: {}", numTopics, numWords, ex.getMessage());
     }

     for (Object[] words : lda.getTopWords(numWords)) {
     for (Object word : words) {
     topics.add(String.valueOf(word));
     }
     }
     Set<String> topicsToStore = new HashSet<>();
     // store 2 for each topic because sometimes words are repeated among topics
     for (Object[] words : lda.getTopWords(2)) {
     for (Object word : words) {
     topicsToStore.add(String.valueOf(word));
     }
     }
     return new List[]{
     new ArrayList<>(topics),
     new ArrayList<>(topicsToStore)};
     }
     */
 /*
     * 
     * @param contAutoresNuevosEncontrados
     * @param allPersons
     * @param endpointName 
     */

    private void printPercentProcess(int contAutoresNuevosEncontrados, int allPersons, String endpointName) {

        if ((contAutoresNuevosEncontrados * 100 / allPersons) != processpercent) {
            processpercent = contAutoresNuevosEncontrados * 100 / allPersons;
            log.info("Procesado el: " + processpercent + " % del Endpoint: " + endpointName);
        }
    }

    private String getLimitOffset(int limit, int offset) {
        return " " + queriesService.getLimit(String.valueOf(limit)) + " " + queriesService.getOffset(String.valueOf(offset));
    }

    @Override
    public void postProcessAffiliations(String... data) {
        try {
            for (String p : data) {
                postProcessAffiliationsWp(p);
            }

        } catch (Exception ex) {
            log.warn("Unknown error Post Process Affiliations , please check the catalina log for further details.");
            log.warn("Exception {}", ex);
        }
    }

    public void postProcessAffiliationsWp(String p) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        String q = "PREFIX schema: <http://schema.org/>\n"
                + "select ?a ?n ?aff {\n"
                + "	 graph <" + constantService.getAuthorsGraph() + "> {\n"
                + "    	?a <http://purl.org/dc/terms/provenance> ?p .\n"
                + "    	?a <http://purl.org/dc/terms/provenance> <" + p + "> .\n"
                + "        ?a schema:affiliation ?aff .\n"
                + "    }\n"
                + "  graph <" + constantService.getEndpointsGraph() + ">  {\n"
                + "  	   ?p <http://ucuenca.edu.ec/ontology#belongTo> ?o .\n"
                + "    }\n"
                + " graph <" + constantService.getOrganizationsGraph() + ">   {\n"
                + "    	?o <http://ucuenca.edu.ec/ontology#fullName> ?n .\n"
                + "    }\n"
                + "} ";
        List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, q);
        ConcurrentHashMap<String, Map.Entry<Set<String>, Set<String>>> comList = new ConcurrentHashMap<>();
        for (Map<String, Value> a : query) {
            String key = a.get("a").stringValue();
            String stringValue = a.get("n").stringValue();
            String stringValue1 = a.get("aff").stringValue();
            if (!comList.containsKey(key)) {
                comList.put(key, new AbstractMap.SimpleEntry<Set<String>, Set<String>>(new HashSet<String>(), new HashSet<String>()));
            }
            Map.Entry<Set<String>, Set<String>> lis = comList.get(key);
            lis.getKey().add(stringValue);
            lis.getValue().add(stringValue1);
        }
        int i = 0;
        for (Map.Entry<String, Map.Entry<Set<String>, Set<String>>> next : comList.entrySet()) {
            i++;
            log.info("Postprocessing affiliations {}/{}", i, comList.size());
            Set<String> orgs = next.getValue().getKey();
            Set<String> affs = next.getValue().getValue();
            Person ap = new Person();
            ap.Affiliations = new ArrayList<>();
            ap.Affiliations.addAll(orgs);
            Person bp = new Person();
            bp.Affiliations = new ArrayList<>();
            bp.Affiliations.addAll(affs);
            Boolean checkAffiliations = ap.checkAffiliations(bp);
            if (checkAffiliations != null && !checkAffiliations) {
                String q2 = "delete {\n"
                        + "graph <" + constantService.getAuthorsGraph() + "> {\n"
                        + "		?a <http://purl.org/dc/terms/provenance> ?p .\n"
                        + "	}	\n"
                        + "}\n"
                        + "insert {\n"
                        + "	   graph <" + constantService.getAuthorsGraph() + "OJSFix" + ">    {\n"
                        + "		?a <http://purl.org/dc/terms/provenance> ?p .\n"
                        + "	}\n"
                        + "}\n"
                        + "where {\n"
                        + "	 graph  <" + constantService.getAuthorsGraph() + ">        {\n"
                        + "		bind (<" + next.getKey() + "> as ?a) .\n"
                        + "		?a <http://purl.org/dc/terms/provenance> ?p .\n"
                        + "	}\n"
                        + "}";
                sparqlService.getSparqlService().update(QueryLanguage.SPARQL, q2);
            }
        }
    }

    @Override
    public void automaticNameDivision() {
        try {
            String qAuthors = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                    + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                    + "select ?a ?n {\n"
                    + "    graph <" + constantService.getAuthorsGraph() + "> {  \n"
                    + "        ?a a foaf:Person .\n"
                    + "        ?a foaf:name ?n .\n"
                    + "        filter not exists {\n"
                    + "            ?a foaf:firstName | foaf:lastName [] . \n"
                    + "        } \n"
                    + "    }  \n"
                    + " }  ";

            ConcurrentHashMap<String, Set<String>> mp = new ConcurrentHashMap<>();
            List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, qAuthors);
            for (Map<String, Value> m : query) {
                if (m.containsKey("a") && m.containsKey("n")) {
                    String aut = m.get("a").stringValue();
                    String nom = m.get("n").stringValue();
                    if (!mp.containsKey(aut)) {
                        mp.put(aut, new HashSet<String>());
                    }
                    mp.get(aut).add(nom);
                }
            }
            ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
            Model output = new LinkedHashModel();
            for (Map.Entry<String, Set<String>> s : mp.entrySet()) {
                for (String name : s.getValue()) {
                    Map<String, String> separateName = disambiguationUtils.separateName(name);
                    if (!separateName.isEmpty()) {
                        String get = separateName.get("firstName");
                        String get1 = separateName.get("lastName");
                        log.info("Fullname '{}' = firstName '{}' - lastName '{}'", name, get, get1);
                        output.add(instance.createURI(s.getKey()), FOAF.FIRST_NAME, instance.createLiteral(get));
                        output.add(instance.createURI(s.getKey()), FOAF.LAST_NAME, instance.createLiteral(get1));
                    } else {
                        log.info("Fullname '{}' skip...", name);
                    }
                }
            }
            sparqlService.getGraphDBInstance().addBuffer(instance.createURI(constantService.getAuthorsGraph()), output);
            sparqlService.getGraphDBInstance().dumpBuffer();
        } catch (Exception ex) {
            log.warn("Unknown error, automatic detection of fist/last names, please check the catalina log for further details.");
            log.warn("Exception {}", ex);
        }
    }

}
