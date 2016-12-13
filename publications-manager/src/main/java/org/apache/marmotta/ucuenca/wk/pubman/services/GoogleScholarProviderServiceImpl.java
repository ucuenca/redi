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

//import info.aduna.iteration.Iterations;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import org.slf4j.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.pubman.exceptions.PubException;
import org.apache.marmotta.ucuenca.wk.pubman.api.GoogleScholarProviderService;
import org.apache.marmotta.ucuenca.wk.commons.service.DistanceService;
import org.apache.marmotta.ucuenca.wk.commons.service.KeywordsService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.model.Value;

/**
 * Default Implementation of {@link PubVocabService} Get Data From Google
 * Scholar using Google Scholar Provider
 *
 * Fernando Baculima CEDIA - Universidad de Cuenca
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
@ApplicationScoped
public class GoogleScholarProviderServiceImpl implements GoogleScholarProviderService, Runnable {

    @Inject
    private Logger log;

    @Inject
    private QueriesService queriesService;

    @Inject
    private ConstantService pubVocabService;

    @Inject
    private CommonsServices commonsServices;

    @Inject
    private DistanceService distance;

    @Inject
    private KeywordsService kservice;

    @Inject
    private SparqlFunctionsService sparqlFunctionsService;

    private String namespaceGraph = "http://ucuenca.edu.ec/wkhuska/";
    private String authorGraph = namespaceGraph + "authors";
    private String endpointsGraph = namespaceGraph + "endpoints";
    private int processpercent = 0;

    /* graphByProvider
     Graph to save publications data by provider
     Example: http://ucuenca.edu.ec/wkhuska/dblp
     */
    private String graphByProviderNS = namespaceGraph + "provider/";

    @Inject
    private SparqlService sparqlService;

    //for Microsoft Academics
    @Override
    public String runPublicationsTaskImpl(String param) {
        return "";
//        try {
//
//            String providerGraph = "";
//            //String getAuthorsQuery = queriesService.getAuthorsQuery();
//            String getGraphsListQuery = queriesService.getGraphsQuery();
//            List<Map<String, Value>> resultGraph = sparqlService.query(QueryLanguage.SPARQL, getGraphsListQuery);
//            /* FOR EACH GRAPH*/
//
//            for (Map<String, Value> map : resultGraph) {
//                providerGraph = map.get("grafo").toString();
//                KiWiUriResource providerGraphResource = new KiWiUriResource(providerGraph);
//
//                if (providerGraph.contains("provider")) {
//
//                    Properties propiedades = new Properties();
//                    InputStream entrada = null;
//                    Map<String, String> mapping = new HashMap<String, String>();
//                    try {
//                        ClassLoader classLoader = getClass().getClassLoader();
//                        //File file = new File(classLoader.getResource("DBLPProvider.properties").getFile());
//
//                        entrada = classLoader.getResourceAsStream(providerGraphResource.getLocalName() + ".properties");
//                        // mappings file loaded
//                        propiedades.load(entrada);
//
//                        for (String source : propiedades.stringPropertyNames()) {
//                            String target = propiedades.getProperty(source);
//                            mapping.put(source.replace("..", ":"), target.replace("..", ":"));
//                        }
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    } finally {
//                        if (entrada != null) {
//                            try {
//                                entrada.close();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//
//                    List<Map<String, Value>> resultPublications = sparqlService.query(QueryLanguage.SPARQL, queriesService.getPublicationsMAQuery(providerGraph));
//                    for (Map<String, Value> pubresource : resultPublications) {
//                        String authorResource = pubresource.get("authorResource").toString();
//                        String publicationResource = pubresource.get("publicationResource").toString();
//                        String publicationProperty = pubVocabService.getPubProperty();
//
//                        //verificar existencia de la publicacion y su author sobre el grafo general
//                        String askTripletQuery = queriesService.getAskQuery(authorGraph, authorResource, publicationProperty, publicationResource);
//                        if (!sparqlService.ask(QueryLanguage.SPARQL, askTripletQuery)) {
//                            String insertPubQuery = buildInsertQuery(authorGraph, authorResource, publicationProperty, publicationResource);
//                            try {
//                                sparqlService.update(QueryLanguage.SPARQL, insertPubQuery);
//                            } catch (MalformedQueryException ex) {
//                                log.error("Malformed Query:  " + insertPubQuery);
//                            } catch (UpdateExecutionException ex) {
//                                log.error("Update Query :  " + insertPubQuery);
//                            } catch (MarmottaException ex) {
//                                log.error("Marmotta Exception:  " + insertPubQuery);
//                            }
//                        }
//
//                        List<Map<String, Value>> resultPubProperties = sparqlService.query(QueryLanguage.SPARQL, queriesService.getPublicationsPropertiesQuery(providerGraph, publicationResource));
//                        for (Map<String, Value> pubproperty : resultPubProperties) {
//                            String nativeProperty = pubproperty.get("publicationProperties").toString();
//                            if (mapping.get(nativeProperty) != null) {
//
//                                String newPublicationProperty = mapping.get(nativeProperty);
//                                String publicacionPropertyValue = pubproperty.get("publicationPropertyValue").toString();
//                                String insertPublicationPropertyQuery = buildInsertQuery(authorGraph, publicationResource, newPublicationProperty, publicacionPropertyValue);
//
//                                try {
//                                    sparqlService.update(QueryLanguage.SPARQL, insertPublicationPropertyQuery);
//                                } catch (MalformedQueryException ex) {
//                                    log.error("Malformed Query:  " + insertPublicationPropertyQuery);
//                                } catch (UpdateExecutionException ex) {
//                                    log.error("Update Query:  " + insertPublicationPropertyQuery);
//                                } catch (MarmottaException ex) {
//                                    log.error("Marmotta Exception:  " + insertPublicationPropertyQuery);
//                                }
//                            }
//                        }
//                        //compare properties with the mapping and insert new properties
//                        //mapping.get(map)
//                    }
//                }
//
//                //in this part, for each graph
//            }
//            return "Los datos de las publicaciones se han cargado exitosamente.";
//        } catch (InvalidArgumentException ex) {
//            return "error:  " + ex;
//        } catch (MarmottaException ex) {
//            return "error:  " + ex;
//        }
    }

    @Override
    public String runPublicationsProviderTaskImpl(String param) {

        try {
            //new AuthorVersioningJob(log).proveSomething();
            ClientConfiguration conf = new ClientConfiguration();
            //conf.addEndpoint(new DBLPEndpoint());
            LDClient ldClient = new LDClient(conf);
            //ClientResponse response = ldClient.retrieveResource("http://rdf.dblp.com/ns/m.0wqhskn");

            String nameProviderGraph = "http://ucuenca.edu.ec/wkhuska/provider/GoogleScholarProvider";
            String getAllAuthorsDataQuery = queriesService.getAuthorsDataQuery(authorGraph, endpointsGraph);

            String nameToFind = "";
            String authorResource = "";
            int priorityToFind = 0;
            List<Map<String, Value>> resultAllAuthors = sparqlService.query(QueryLanguage.SPARQL, getAllAuthorsDataQuery);

            /*To Obtain Processed Percent*/
            int allPersons = resultAllAuthors.size();
            int processedPersons = 0;

            RepositoryConnection conUri = null;
            ClientResponse response = null;
            for (Map<String, Value> map : resultAllAuthors) {
                processedPersons++;
                log.info("Autores procesados con GoogleScholar: " + processedPersons + " de " + allPersons);
                authorResource = map.get("subject").stringValue();
                String firstName = map.get("fname").stringValue();
                String lastName = map.get("lname").stringValue();
                priorityToFind = 1;
                if (!sparqlService.ask(QueryLanguage.SPARQL, queriesService.getAskResourceQuery(nameProviderGraph, authorResource))) {
                    boolean dataretrieve = false;//( Data Retrieve Exception )

                    do {
                        try {
                            boolean existNativeAuthor = false;
                            nameToFind = commonsServices.removeAccents(priorityFindQueryBuilding(priorityToFind, firstName, lastName).replace("_", "+"));

                            //String URL_TO_FIND = "https://scholar.google.com/scholar?start=0&q=author:%22" + nameToFind + "%22&hl=en&as_sdt=1%2C15&as_vis=1";
                            String URL_TO_FIND = "https://scholar.google.com/citations?mauthors=" + nameToFind + "&hl=en&view_op=search_authors";

                            existNativeAuthor = sparqlService.ask(QueryLanguage.SPARQL, queriesService.getAskResourceQuery(nameProviderGraph, URL_TO_FIND));
                            if (nameToFind.compareTo("") != 0 && !existNativeAuthor) {

                                String info = "";
                                List<Map<String, Value>> iesInfo = sparqlService.query(QueryLanguage.SPARQL, queriesService.getIESInfobyAuthor(authorResource));
                                if (!iesInfo.isEmpty()) {
                                    String city = iesInfo.get(0).get("city").stringValue();
                                    String province = iesInfo.get(0).get("province").stringValue();
                                    String ies = iesInfo.get(0).get("ies").stringValue();
                                    String domains = iesInfo.get(0).get("domains").stringValue();

                                    info = ";" + city + "-" + province + "-" + ies + "-" + domains;
                                }

                                //do {
                                try {//waint 1  second between queries
                                    Thread.sleep(1000);
                                } catch (InterruptedException ex) {
                                    Thread.currentThread().interrupt();
                                }
                                try {
                                    response = ldClient.retrieveResource(URL_TO_FIND + info);
                                    dataretrieve = true;
                                } catch (DataRetrievalException e) {
                                    //do {
                                    try {//waint 1 second if fail retrieve data
                                        Thread.sleep(1000);
                                    } catch (InterruptedException ex) {
                                        Thread.currentThread().interrupt();
                                    }
                                    log.error("Error when retrieve: " + URL_TO_FIND + " -  Exception: " + e);
                                    dataretrieve = false;
                                }

                            }

                            //String nameEndpointofPublications = ldClient.getEndpoint(NS_DBLP + nameToFind).getName();
                            String nameEndpointofPublications = ldClient.getEndpoint(URL_TO_FIND).getName();
                            String providerGraph = graphByProviderNS + nameEndpointofPublications.replace(" ", "");
                            if (dataretrieve) {
//                                Model model = response.getData();
//                                FileOutputStream out = new FileOutputStream("C:\\Users\\Satellite\\Desktop\\" + nameToFind.replace("?", "_") + "_test.ttl");
//                                RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
//                                try {
//                                    writer.startRDF();
//                                    for (Statement st : model) {
//                                        writer.handleStatement(st);
//                                    }
//                                    writer.endRDF();
//                                } catch (RDFHandlerException e) {
//                                    // oh no, do something!
//                                }
                                conUri = ModelCommons.asRepository(response.getData()).getConnection();
                                conUri.begin();
                                String authorNativeResource = null;

                                //THIS DRIVER NO RETURN MEMBERS OF A SEARCH, ALL DATA IS RELATED WITH A AUTHOR
                                authorNativeResource = URL_TO_FIND;
                                //existNativeAuthor = sparqlService.ask(QueryLanguage.SPARQL, queriesService.getAskResourceQuery(providerGraph, authorNativeResource));

                                if (!existNativeAuthor) {

                                    /**
                                     * First: Verify resource (publications)
                                     * that contains the NameToFind in
                                     * dc:creator property Second: Compare if
                                     * some keywords of NameToFind author is
                                     * contained into a retrieve publication
                                     */
                                    String getPublicationsFromProviderQuery = queriesService.getSubjectAndObjectByPropertyQuery("dc:creator");
                                    TupleQuery pubquery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getPublicationsFromProviderQuery); //
                                    TupleQueryResult gsPublicationsResult = pubquery.evaluate();

                                    while (gsPublicationsResult.hasNext()) {
                                        BindingSet gsResource = gsPublicationsResult.next();
                                        String publication = gsResource.getValue("subject").toString();
                                        String authorfromGS = gsResource.getValue("object").toString();
                                        /**
                                         * Getting and formating full name from
                                         * google scholar Example ->
                                         * author:%22M+Espinoza+Marin%22 Example
                                         * 2 -> author:%22JM+Espinoza%22. The
                                         * comparisonNames.syntacticComparison
                                         * Service need the format : Name1
                                         * Name2:Name3 Name4
                                         */
                                        String googlescholarfullname = authorfromGS;
                                        googlescholarfullname = googlescholarfullname.substring(googlescholarfullname.indexOf("author:") + 10);
                                        if (googlescholarfullname.indexOf("+") == 0) {
                                            googlescholarfullname = googlescholarfullname.substring(1, googlescholarfullname.indexOf("%22"));
                                        } else {
                                            googlescholarfullname = googlescholarfullname.substring(0, googlescholarfullname.indexOf("%22"));
                                        }
                                        googlescholarfullname = googlescholarfullname.replace('+', ':');
                                        /**
                                         * case JM:Espinoza or JP:Carvallo
                                         * replace with : J:Espinoza or
                                         * J:Carvallo
                                         */
                                        if (googlescholarfullname.substring(0, googlescholarfullname.indexOf(":")).length() == 2) {
                                            googlescholarfullname = googlescholarfullname.substring(0, 1) + googlescholarfullname.substring(2);
                                        }

                                        String localfullname = firstName + ":" + lastName;

                                        /**
                                         * in comparisonNames send local because
                                         * the syntax names are similar
                                         */
                                        if (distance.syntacticComparisonNames("local", localfullname, "local", googlescholarfullname)) {

                                            List<String> listA = kservice.getKeywordsOfAuthor(authorResource);
                                            List<String> listB = new ArrayList<String>();
                                            String getAbstractAndTitleFromProviderQuery = queriesService.getAbstractAndTitleQuery(publication);
                                            TupleQuery abstracttitlequery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getAbstractAndTitleFromProviderQuery); //
                                            TupleQueryResult abstractResult = abstracttitlequery.evaluate();

                                            if (abstractResult.hasNext()) {
                                                BindingSet abstractResource = abstractResult.next();
                                                String abstracttext = abstractResource.getValue("abstract").toString();
                                                String titletext = abstractResource.getValue("title").toString();
                                                listB = kservice.getKeywords(titletext);

                                            }
                                            int cero = 0;
                                            if (listB.size() != cero && listA.size() != cero && distance.semanticComparison(listA, listB)) {
                                                //SPARQL obtain all data publications of author from Google Scholar Provider
                                                String getPublicationDataFromProviderQuery = queriesService.getPublicationsPropertiesQuery(publication);
                                                TupleQuery dataquery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getPublicationDataFromProviderQuery); //
                                                TupleQueryResult tripletasResult = dataquery.evaluate();

                                                while (tripletasResult.hasNext()) {
                                                    BindingSet tripletsResource = tripletasResult.next();
                                                    String predicate = tripletsResource.getValue("property").toString();
                                                    String object = tripletsResource.getValue("value").toString();

                                                    //String publicationProperty = tripletsResource.getValue("publicationProperty").toString();
                                                    ///insert sparql query, 
                                                    String publicationInsertQuery = buildInsertQuery(providerGraph, publication, predicate, object);
                                                    updatePub(publicationInsertQuery);

                                                    // insert dct:contributor      <> dct:contributor <http://dblp.org/pers/xr/s/Saquicela:Victor> 
                                                    String contributorInsertQuery = buildInsertQuery(providerGraph, publication, "http://purl.org/dc/terms/contributor", authorNativeResource);
                                                    updatePub(contributorInsertQuery);

                                                    // sameAs triplet    <http://190.15.141.102:8080/dspace/contribuidor/autor/SaquicelaGalarza_VictorHugo> owl:sameAs <http://dblp.org/pers/xr/s/Saquicela:Victor> 
                                                    String sameAsInsertQuery = buildInsertQuery(providerGraph, authorResource, "http://www.w3.org/2002/07/owl#sameAs", authorNativeResource);
                                                    updatePub(sameAsInsertQuery);

                                                }
                                            }//end if semantic comparison

                                        }//end if syntactic comparison
                                    }
                                }//end if existNativeAuthor
                                conUri.commit();
                                conUri.close();
                            }//fin   if (dataretrieve)
                        } catch (Exception e) {
                            log.error("ioexception " + e.toString());
                        }
                        priorityToFind++;
                    } while (priorityToFind < 3 && !dataretrieve);//end do while
                    printPercentProcess(processedPersons, allPersons, "Google Scholar");
                }
            }
            return "True for GS publications";
        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(GoogleScholarProviderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Fail for GS";
    }

    public String priorityFindQueryBuilding(int priority, String firstName, String lastName) {
        String[] fnamelname = {"", "", "", "", ""};
        /**
         * fnamelname[0] is a firstName A, fnamelname[1] is a firstName B
         * fnamelname[2] is a lastName A, fnamelname[3] is a lastName B
         *
         */

        for (int i = 0; i < firstName.split(" ").length; i++) {
            fnamelname[i] = firstName.split(" ")[i];
        }

        for (int i = 0; i < lastName.split(" ").length; i++) {
            fnamelname[i + 2] = lastName.split(" ")[i];
        }

        switch (priority) {
            case 2:
                return fnamelname[0] + "_" + fnamelname[2];
            case 1:
                return fnamelname[0] + "_" + fnamelname[1] + "_" + fnamelname[2];
        }
        return "";
    }

    /*
     *   UPDATE - with SPARQL MODULE, to load triplet in marmotta plataform
     *   
     */
    public String updatePub(String querytoUpdate) {

        try {
            sparqlFunctionsService.updatePub(querytoUpdate);
        } catch (PubException ex) {
            log.error("No se pudo insertar: " + querytoUpdate);
            //         java.util.logging.Logger.getLogger(PubVocabServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Correcto";

    }

    /*
     * 
     * @param contAutoresNuevosEncontrados
     * @param allPersons
     * @param endpointName 
     */
    public void printPercentProcess(int processedPersons, int allPersons, String provider) {

        if ((processedPersons * 100 / allPersons) != processpercent) {
            processpercent = processedPersons * 100 / allPersons;
            log.info("Procesado el: " + processpercent + " % de " + provider);
        }
    }

    //construyendo sparql query insert 
    public String buildInsertQuery(String grapfhProv, String sujeto, String predicado, String objeto) {
        if (commonsServices.isURI(objeto)) {
            return queriesService.getInsertDataUriQuery(grapfhProv, sujeto, predicado, objeto);
        } else {
            return queriesService.getInsertDataLiteralQuery(grapfhProv, sujeto, predicado, objeto);
        }
    }

    @Override
    public void run() {
        runPublicationsProviderTaskImpl("uri");
    }

}
