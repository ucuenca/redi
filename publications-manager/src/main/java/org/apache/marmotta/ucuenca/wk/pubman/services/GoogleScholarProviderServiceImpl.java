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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.pubman.exceptions.PubException;
import org.apache.marmotta.ucuenca.wk.pubman.api.GoogleScholarProviderService;

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
//import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.model.Value;
import org.openrdf.query.UpdateExecutionException;

//import org.openrdf.query.impl.TupleQueryResultImpl;
//import org.openrdf.repository.RepositoryException;
//import org.openrdf.rio.RDFFormat;
//import org.openrdf.rio.RDFHandlerException;
//import org.openrdf.rio.RDFWriter;
//import org.openrdf.rio.Rio;
//import org.openrdf.model.Model;
//import org.openrdf.model.Statement;
/**
 * Default Implementation of {@link PubVocabService} Get Data From Google
 * Scholar using Google Scholar Provider
 *
 * Fernando Baculima CEDIA - Universidad de Cuenca
 *
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
    private SparqlFunctionsService sparqlFunctionsService;

    private String namespaceGraph = "http://ucuenca.edu.ec/wkhuska/";
    private String authorGraph = namespaceGraph + "authors";

    private int processpercent = 0;

    /* graphByProvider
     Graph to save publications data by provider
     Example: http://ucuenca.edu.ec/wkhuska/dblp
     */
    private String graphByProviderNS = namespaceGraph +  "/provider/";

    @Inject
    private SparqlService sparqlService;

    //for Microsoft Academics
    @Override
    public String runPublicationsTaskImpl(String param) {

        try {

            String providerGraph = "";
            //String getAuthorsQuery = queriesService.getAuthorsQuery();
            String getGraphsListQuery = queriesService.getGraphsQuery();
            List<Map<String, Value>> resultGraph = sparqlService.query(QueryLanguage.SPARQL, getGraphsListQuery);
            /* FOR EACH GRAPH*/

            for (Map<String, Value> map : resultGraph) {
                providerGraph = map.get("grafo").toString();
                KiWiUriResource providerGraphResource = new KiWiUriResource(providerGraph);

                if (providerGraph.contains("provider")) {

                    Properties propiedades = new Properties();
                    InputStream entrada = null;
                    Map<String, String> mapping = new HashMap<String, String>();
                    try {
                        ClassLoader classLoader = getClass().getClassLoader();
                        //File file = new File(classLoader.getResource("DBLPProvider.properties").getFile());

                        entrada = classLoader.getResourceAsStream(providerGraphResource.getLocalName() + ".properties");
                        // mappings file loaded
                        propiedades.load(entrada);

                        for (String source : propiedades.stringPropertyNames()) {
                            String target = propiedades.getProperty(source);
                            mapping.put(source.replace("..", ":"), target.replace("..", ":"));
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        if (entrada != null) {
                            try {
                                entrada.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    List<Map<String, Value>> resultPublications = sparqlService.query(QueryLanguage.SPARQL, queriesService.getPublicationsMAQuery(providerGraph));
                    for (Map<String, Value> pubresource : resultPublications) {
                        String authorResource = pubresource.get("authorResource").toString();
                        String publicationResource = pubresource.get("publicationResource").toString();
                        String publicationProperty = pubVocabService.getPubProperty();

                        //verificar existencia de la publicacion y su author sobre el grafo general
                        String askTripletQuery = queriesService.getAskQuery(authorGraph, authorResource, publicationProperty, publicationResource);
                        if (!sparqlService.ask(QueryLanguage.SPARQL, askTripletQuery)) {
                            String insertPubQuery = buildInsertQuery(authorGraph, authorResource, publicationProperty, publicationResource);
                            try {
                                sparqlService.update(QueryLanguage.SPARQL, insertPubQuery);
                            } catch (MalformedQueryException ex) {
                                log.error("Malformed Query:  " + insertPubQuery);
                            } catch (UpdateExecutionException ex) {
                                log.error("Update Query :  " + insertPubQuery);
                            } catch (MarmottaException ex) {
                                log.error("Marmotta Exception:  " + insertPubQuery);
                            }
                        }

                        List<Map<String, Value>> resultPubProperties = sparqlService.query(QueryLanguage.SPARQL, queriesService.getPublicationsPropertiesQuery(providerGraph, publicationResource));
                        for (Map<String, Value> pubproperty : resultPubProperties) {
                            String nativeProperty = pubproperty.get("publicationProperties").toString();
                            if (mapping.get(nativeProperty) != null) {

                                String newPublicationProperty = mapping.get(nativeProperty);
                                String publicacionPropertyValue = pubproperty.get("publicationPropertyValue").toString();
                                String insertPublicationPropertyQuery = buildInsertQuery(authorGraph, publicationResource, newPublicationProperty, publicacionPropertyValue);

                                try {
                                    sparqlService.update(QueryLanguage.SPARQL, insertPublicationPropertyQuery);
                                } catch (MalformedQueryException ex) {
                                    log.error("Malformed Query:  " + insertPublicationPropertyQuery);
                                } catch (UpdateExecutionException ex) {
                                    log.error("Update Query:  " + insertPublicationPropertyQuery);
                                } catch (MarmottaException ex) {
                                    log.error("Marmotta Exception:  " + insertPublicationPropertyQuery);
                                }
                            }
                        }
                        //compare properties with the mapping and insert new properties
                        //mapping.get(map)
                    }
                }

                //in this part, for each graph
            }
            return "Los datos de las publicaciones se han cargado exitosamente.";
        } catch (InvalidArgumentException ex) {
            return "error:  " + ex;
        } catch (MarmottaException ex) {
            return "error:  " + ex;
        }
    }

    @Override
    public String runPublicationsProviderTaskImpl(String param) {
        try {

            //new AuthorVersioningJob(log).proveSomething();
            ClientConfiguration conf = new ClientConfiguration();
            //conf.addEndpoint(new DBLPEndpoint());
            LDClient ldClient = new LDClient(conf);
            //ClientResponse response = ldClient.retrieveResource("http://rdf.dblp.com/ns/m.0wqhskn");

            int allMembers = 0;
            String nameProviderGraph = "http://ucuenca.edu.ec/wkhuska/provider/GoogleScholarProvider";
            String getAllAuthorsDataQuery = queriesService.getAuthorsDataQuery(authorGraph);

            // TupleQueryResult result = sparqlService.query(QueryLanguage.SPARQL, getAuthors);
            String nameToFind = "";
            String authorResource = "";
            int priorityToFind = 0;
            List<Map<String, Value>> resultAllAuthors = sparqlService.query(QueryLanguage.SPARQL, getAllAuthorsDataQuery);

            /*To Obtain Processed Percent*/
            int allPersons = resultAllAuthors.size();
            int processedPersons = 0;

            RepositoryConnection conUri = null;
            ClientResponse response = null;
            List<Map<String, Value>> resultAllAuthorsAux = new ArrayList<>();
            for (Map<String, Value> map : resultAllAuthors) {
                processedPersons++;
                log.info("Autores procesados con GoogleScholar: " + processedPersons + " de " + allPersons);
                authorResource = map.get("subject").stringValue();
                String firstName = map.get("fname").stringValue();
                String lastName = map.get("lname").stringValue();
                priorityToFind = 1;
                boolean AuthorDataisLoad = false;
                int waitTime = 0;
                if (!sparqlService.ask(QueryLanguage.SPARQL, queriesService.getAskResourceQuery(nameProviderGraph, authorResource))) {
                    do {
                        try {
                            boolean existNativeAuthor = true;
                            allMembers = 0;
                            nameToFind = priorityFindQueryBuilding(priorityToFind, firstName, lastName).replace("_", "+");
                            //response = ldClient.retrieveResource(NS_DBLP + nameToFind);
                            String URL_TO_FIND = "https://scholar.google.com/scholar?start=0&q=author:%22" + nameToFind + "%22&hl=en&as_sdt=1%2C15&as_vis=1";
                            existNativeAuthor = sparqlService.ask(QueryLanguage.SPARQL, queriesService.getAskResourceQuery(nameProviderGraph, URL_TO_FIND));
                            if (nameToFind != "" && !existNativeAuthor) {
                                boolean dataretrievee = true;//( Data Retrieve Exception )
                                waitTime = 0;
                                //do {
                                try {
                                    response = ldClient.retrieveResource(URL_TO_FIND);
                                    dataretrievee = true;
                                } catch (DataRetrievalException e) {
                                    log.error("Data Retrieval Exception: " + e);
                                    log.info("Wating: " + waitTime + " seconds for new query");
                                    dataretrievee = false;
                                    try {
                                        Thread.sleep(waitTime * 1000);               //1000 milliseconds is one second.
                                    } catch (InterruptedException ex) {
                                        Thread.currentThread().interrupt();
                                    }
                                    waitTime += 5;
                                }
                                if (response.getHttpStatus() == 503) {
                                    try {
                                        log.info("Wating 1 day for new Google Scholar Query ");
                                        Thread.sleep(86400000);  // 1 day                                            
                                    } catch (InterruptedException ex) {
                                        Thread.currentThread().interrupt();
                                    }
                                }
                                // } while (true);
                                //(!dataretrievee && response.getHttpStatus() == 503);

                            }//end  if  nameToFind != ""

                            //String nameEndpointofPublications = ldClient.getEndpoint(NS_DBLP + nameToFind).getName();
                            String nameEndpointofPublications = ldClient.getEndpoint(URL_TO_FIND).getName();
                            String providerGraph = graphByProviderNS + nameEndpointofPublications.replace(" ", "");
                            if (response != null) {
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
                                existNativeAuthor = sparqlService.ask(QueryLanguage.SPARQL, queriesService.getAskResourceQuery(providerGraph, authorNativeResource));

                                if (!existNativeAuthor) {
                                    // sameAs triplet    <http://190.15.141.102:8080/dspace/contribuidor/autor/SaquicelaGalarza_VictorHugo> owl:sameAs <http://dblp.org/pers/xr/s/Saquicela:Victor> 
                                    String sameAsInsertQuery = buildInsertQuery(providerGraph, authorResource, "http://www.w3.org/2002/07/owl#sameAs", authorNativeResource);
                                    updatePub(sameAsInsertQuery);
                                    //SPARQL obtain all data publications of author from Google Scholar Provider
                                    String getPublicationsFromProviderQuery = queriesService.getRetrieveResourceQuery();
                                    TupleQuery pubquery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getPublicationsFromProviderQuery); //
                                    TupleQueryResult tripletasResult = pubquery.evaluate();

                                    while (tripletasResult.hasNext()) {
                                        AuthorDataisLoad = true;
                                        BindingSet tripletsResource = tripletasResult.next();
                                        String subject = tripletsResource.getValue("s").toString();
                                        String predicate = tripletsResource.getValue("p").toString();
                                        String object = tripletsResource.getValue("o").toString();

                                        //String publicationProperty = tripletsResource.getValue("publicationProperty").toString();
                                        ///insert sparql query, 
                                        String publicationInsertQuery = buildInsertQuery(providerGraph, subject, predicate, object);
                                        updatePub(publicationInsertQuery);
                                    }
                                }//end if existNativeAuthor
                                conUri.commit();
                                conUri.close();
                            }//fin    IF RESPONDE!= NULL
                        } catch (Exception e) {
                            log.error("ioexception " + e.toString());
                        }
                        priorityToFind++;
                        //} while (true);   !AuthorDataisLoad &&
                    } while (priorityToFind < 5);//end do while
                }//end if ( authorResource not exist)
                printPercentProcess(processedPersons, allPersons, "Google Scholar");
            }
            return "True for GS publications";
        } catch (MarmottaException ex) {
            log.error("Marmotta Exception: " + ex);
        }
        return "fail GS";
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
//            case 5:
//                return fnamelname[3];
            case 4:
                return fnamelname[0] + "_" + fnamelname[2];
            case 3:
                return fnamelname[1] + "_" + fnamelname[2] + "_" + fnamelname[3];
            case 2:
                return fnamelname[0] + "_" + fnamelname[2] + "_" + fnamelname[3];
            case 1:
                return fnamelname[0] + "_" + fnamelname[1] + "_" + fnamelname[2] + "_" + fnamelname[3];
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
        if (queriesService.isURI(objeto)) {
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
