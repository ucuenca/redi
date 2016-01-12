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

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
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
import org.apache.marmotta.ucuenca.wk.commons.service.PropertyPubService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;

import org.apache.marmotta.ucuenca.wk.pubman.api.SparqlFunctionsService;

import org.apache.marmotta.ucuenca.wk.pubman.exceptions.PubException;
import org.apache.marmotta.ucuenca.wk.pubman.api.DBLPProviderService;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResults;

import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.GraphQueryResult;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.model.Value;
import org.openrdf.query.UpdateExecutionException;

/**
 * Default Implementation of {@link PubVocabService}
 */
@ApplicationScoped
public class DBLPProviderServiceImpl implements DBLPProviderService, Runnable {

    @Inject
    private Logger log;

    @Inject
    private QueriesService queriesService;

    @Inject
    private PropertyPubService pubVocabService;

    @Inject
    private SparqlFunctionsService sparqlFunctionsService;

    private String namespaceGraph = "http://ucuenca.edu.ec/wkhuska/";
    private String authorGraph = namespaceGraph + "authors";
    private String externalAuthorGraph = namespaceGraph + "wkhuska/externalauthors";
    private int processpercent = 0;

    /* graphByProvider
     Graph to save publications data by provider
     Example: http://ucuenca.edu.ec/wkhuska/dblp
     */
    private String graphByProviderNS = namespaceGraph + "wkhuska" + "/provider/";

    @Inject
    private SparqlService sparqlService;

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
                        // cargamos el archivo de propiedades
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

                    List<Map<String, Value>> resultPublications = sparqlService.query(QueryLanguage.SPARQL, queriesService.getPublicationsQuery(providerGraph));
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
            String getAllAuthorsDataQuery = queriesService.getAuthorsDataQuery(authorGraph);

            // TupleQueryResult result = sparqlService.query(QueryLanguage.SPARQL, getAuthors);
            String nameToFind = "";
            String authorResource = "";
            int priorityToFind = 0;
            List<Map<String, Value>> resultAllAuthors = sparqlService.query(QueryLanguage.SPARQL, getAllAuthorsDataQuery);

            /*To Obtain Processed Percent*/
            int allPersons = resultAllAuthors.size();
            int processedPersons = 0;

            String NS_DBLP = "http://rdf.dblp.com/ns/search/";
            RepositoryConnection conUri = null;
            ClientResponse response = null;
            for (Map<String, Value> map : resultAllAuthors) {
                processedPersons++;
                log.info("Autores procesados con DBLP: " + processedPersons + " de " + allPersons);
                authorResource = map.get("subject").stringValue();
                String firstName = map.get("fname").stringValue();
                String lastName = map.get("lname").stringValue();
                priorityToFind = 1;
                do {
                    try {
                        boolean existNativeAuthor = true;
                        allMembers = 0;
                        nameToFind = priorityFindQueryBuilding(priorityToFind, firstName, lastName);

                        boolean dataretrievee = true;//( Data Retrieve Exception )
                        int waitTime = 30;
                        do {
                            dataretrievee = true;
                            try {
                                response = ldClient.retrieveResource(NS_DBLP + nameToFind);
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
                        } while (!dataretrievee && waitTime < 40);

                        if (response.getHttpStatus() == 503) {
                            log.error("ErrorCode: " + response.getHttpStatus());
                        }

                        String nameEndpointofPublications = ldClient.getEndpoint(NS_DBLP + nameToFind).getName();
                        String providerGraph = graphByProviderNS + nameEndpointofPublications.replace(" ", "");

//                        Model model = response.getData();
//                        FileOutputStream out = new FileOutputStream("C:\\Users\\Satellite\\Desktop\\" + nameToFind + "_test.ttl");
//                        RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
//                        try {
//                            writer.startRDF();
//                            for (Statement st : model) {
//                                writer.handleStatement(st);
//                            }
//                            writer.endRDF();
//                        } catch (RDFHandlerException e) {
//                            // oh no, do something!
//                        }
                        conUri = ModelCommons.asRepository(response.getData()).getConnection();
                        conUri.begin();
                        String authorNativeResource = null;
                        //verifying the number of persons retrieved. if it has recovered more than one persons then the filter is changed and search anew,
                        String getMembersQuery = queriesService.getMembersQuery();
                        TupleQueryResult membersResult = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getMembersQuery).evaluate();
                        //  allMembers = Iterations.asList(membersResult).size();
                        while (membersResult.hasNext()) {
                            allMembers++;
                            BindingSet bindingCount = membersResult.next();
                            authorNativeResource = bindingCount.getValue("members").toString();
                            existNativeAuthor = sparqlService.ask(QueryLanguage.SPARQL, queriesService.getAskResourceQuery(providerGraph, authorNativeResource));
                        }
                        //the author data was already loaded into the repository, only a sameAs property is associated 
                        if (allMembers == 1 && existNativeAuthor) {
                            //insert sameAs triplet    <http://190.15.141.102:8080/dspace/contribuidor/autor/SaquicelaGalarza_VictorHugo> owl:sameAs <http://dblp.org/pers/xr/s/Saquicela:Victor> 
                            String sameAsInsertQuery = buildInsertQuery(providerGraph, authorResource, "http://www.w3.org/2002/07/owl#sameAs", authorNativeResource);
                            updatePub(sameAsInsertQuery);
                        }
                        if (allMembers == 1 && !existNativeAuthor) {
                            //SPARQL obtain all publications of author
                            String getPublicationsFromProviderQuery = queriesService.getPublicationFromProviderQuery();
                            TupleQuery pubquery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getPublicationsFromProviderQuery); //
                            TupleQueryResult tripletasResult = pubquery.evaluate();
                            while (tripletasResult.hasNext()) {
                                BindingSet tripletsResource = tripletasResult.next();
                                authorNativeResource = tripletsResource.getValue("authorResource").toString();
                                String publicationResource = tripletsResource.getValue("publicationResource").toString();
                                String publicationProperty = tripletsResource.getValue("publicationProperty").toString();
                                ///insert sparql query, 
                                String publicationInsertQuery = buildInsertQuery(providerGraph, authorNativeResource, publicationProperty, publicationResource);
                                updatePub(publicationInsertQuery);

                                // sameAs triplet    <http://190.15.141.102:8080/dspace/contribuidor/autor/SaquicelaGalarza_VictorHugo> owl:sameAs <http://dblp.org/pers/xr/s/Saquicela:Victor> 
                                String sameAsInsertQuery = buildInsertQuery(providerGraph, authorResource, "http://www.w3.org/2002/07/owl#sameAs", authorNativeResource);
                                updatePub(sameAsInsertQuery);
                            }

                            // SPARQL to obtain all data of a publication
                            String getPublicationPropertiesQuery = queriesService.getPublicationPropertiesQuery();
                            TupleQuery resourcequery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getPublicationPropertiesQuery); //
                            tripletasResult = resourcequery.evaluate();
                            while (tripletasResult.hasNext()) {
                                BindingSet tripletsResource = tripletasResult.next();
                                String publicationResource = tripletsResource.getValue("publicationResource").toString();
                                String publicationProperties = tripletsResource.getValue("publicationProperties").toString();
                                String publicationPropertiesValue = tripletsResource.getValue("publicationPropertiesValue").toString();
                                ///insert sparql query, 
                                String publicationPropertiesInsertQuery = buildInsertQuery(providerGraph, publicationResource, publicationProperties, publicationPropertiesValue);
                                //load values publications to publications resource
                                updatePub(publicationPropertiesInsertQuery);
                            }

                        }//end if numMembers=1
                        conUri.commit();
                        conUri.close();
                    } catch (QueryEvaluationException | MalformedQueryException | RepositoryException ex) {
                        log.error("Evaluation Exception: " + ex);
                    } catch (Exception e) {
                        log.error("ioexception " + e.toString());
                    }
                    priorityToFind++;
                } while (allMembers != 1 && priorityToFind < 5);//end do while
                //** end View Data
                printPercentProcess(processedPersons, allPersons, "DBLP");
            }
            return "True for publications";
        } catch (MarmottaException ex) {
            log.error("Marmotta Exception: " + ex);
        }

        return "fail";
    }

    @Override
    public JsonArray SearchAuthorTaskImpl(String uri) {

        JsonParser parser = new JsonParser();

        try {
//new AuthorVersioningJob(log).proveSomething();
            ClientConfiguration conf = new ClientConfiguration();
            //conf.addEndpoint(new DBLPEndpoint());
            LDClient ldClient = new LDClient(conf);
            //ClientResponse response = ldClient.retrieveResource("http://rdf.dblp.com/ns/m.0wqhskn");

            String AuthorTofind = uri;
            String providerName = ldClient.getEndpoint(AuthorTofind).getName();
            String providerGraph = graphByProviderNS + providerName.replace(" ", "");

            Properties propiedades = new Properties();
            InputStream entrada = null;
            Map<String, String> mapping = new HashMap<String, String>();

            ClassLoader classLoader = getClass().getClassLoader();
            //File file = new File(classLoader.getResource("DBLPProvider.properties").getFile());
            entrada = classLoader.getResourceAsStream(providerName.replace(" ", "") + ".properties");
            // cargamos el archivo de propiedades
            propiedades.load(entrada);
            for (String source : propiedades.stringPropertyNames()) {
                String target = propiedades.getProperty(source);
                mapping.put(source.replace("..", ":"), target.replace("..", ":"));
            }

            int allMembers = 0;
            String getAllAuthorsDataQuery = queriesService.getAuthorsDataQuery(authorGraph);

            // TupleQueryResult result = sparqlService.query(QueryLanguage.SPARQL, getAuthors);
            String authorResource = "";
            int priorityToFind = 0;

            /*To Obtain Processed Percent*/
            int processedPersons = 0;

            String NS_DBLP = "http://rdf.dblp.com/ns/search/";
            RepositoryConnection conUri = null;
            ClientResponse response = null;
            processedPersons++;
            log.info("Buscando Informacion de: " + uri);

            try {
                boolean existNativeAuthor = true;
                allMembers = 0;
                AuthorTofind = uri.replace("\"", "");

                boolean dataretrievee = true;//( Data Retrieve Exception )

                dataretrievee = true;
                try {
                    response = ldClient.retrieveResource(AuthorTofind);
                } catch (DataRetrievalException e) {
                    log.error("Data Retrieval Exception: " + e);
                    dataretrievee = false;

                }

                if (response.getHttpStatus() == 503) {
                    log.error("ErrorCode: " + response.getHttpStatus());
                }

                Model model = response.getData();

                OutputStream out = new FileOutputStream("C:\\Users\\Satellite\\Desktop\\searchauthor_test.ttl");
                RDFWriter writer = Rio.createWriter(RDFFormat.N3, out);
                String json = "";
                try {
                    writer.startRDF();
                    for (Statement st : model) {
                        writer.handleStatement(st);
                        json = json + st.toString();
                    }
                    writer.endRDF();
                } catch (RDFHandlerException e) {
                    // oh no, do something!
                }
                out.close();
                conUri = ModelCommons.asRepository(response.getData()).getConnection();
                conUri.begin();
                String authorNativeResource = null;

                //SPARQL obtain all publications of author
                String getPublicationsFromProviderQuery = " PREFIX dc: <http://purl.org/dc/elements/1.1/> "
                        + " PREFIX dct: <http://purl.org/dc/terms/> "
                        + " PREFIX bibo: <http://purl.org/ontology/bibo/> "
                        + " PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                        + " CONSTRUCT  { <" + AuthorTofind + "> foaf:publications ?uripub. "
                        + " ?uripub  a bibo:Document. ?uripub bibo:uri ?uri. "
                        + " ?uripub  bibo:abstract ?abstract. ?uripub bibo:Quote ?keyword. "
                        + " ?uripub dct:title ?title. ?uripub dct:contributor ?coauthor. "
                        + " ?uripub bibo:numPages ?numPages. ?uripub dct:isPartOf ?isPartOf. ?uripub dct:publisher ?publisher. "
                        + " } "
                        + " WHERE "
                        + " { "
                        + " <" + AuthorTofind + ">  <" + mapping.get("publicationProperty") + "> ?publication. "
                        + " ?publication <" + mapping.get("title") + "> ?title. "
                        + " OPTIONAL { ?publication <" + mapping.get("uri") + "> ?uri. } "
                        + " OPTIONAL { ?publication <" + mapping.get("abstract") + ">  ?abstract. }"
                        + " OPTIONAL { ?publication <" + mapping.get("keyword") + ">  ?keyword. }"
                        + " OPTIONAL { ?publication <" + mapping.get("contributor") + "> ?coauthor. }"
                        + " OPTIONAL { ?publication <" + mapping.get("numPages") + "> ?numPages. }"
                        + " OPTIONAL { ?publication <" + mapping.get("isPartOf") + "> ?isPartOf. }"
                        + " OPTIONAL { ?publication <" + mapping.get("publisher") + "> ?publisher. }"
                        + " BIND (REPLACE(?title,\" \", \"_\",\"i\") as ?newtitle) "
                        + " BIND (IRI(CONCAT(\"http://ucuenca.edu.ec/wkhuska/publication/\",?newtitle)) as ?uripub) "
                        + " } LIMIT 170 ";
                //
                //  String getPublicationsFromProviderQueryto = "Construct {?s ?p ?o} where { ?s ?p ?o}";
                GraphQueryResult graphQueryResult = conUri.prepareGraphQuery(QueryLanguage.SPARQL, getPublicationsFromProviderQuery).evaluate();
                Model resultModel = QueryResults.asModel(graphQueryResult);
                //Getting data in JSONLD format 
                StringWriter writerdata = new StringWriter();
                RDFWriter writerjld = Rio.createWriter(RDFFormat.JSONLD, writerdata);
                try {
                    writerjld.startRDF();
                    for (Statement st : resultModel) {
                        writerjld.handleStatement(st);
                    }
                    writerjld.endRDF();
                } catch (RDFHandlerException e) {
                    // oh no, do something!
                }

                conUri.commit();
                conUri.close();

                return parser.parse(writerdata.toString()).getAsJsonArray();
                // return writerdata.toString();

            } catch (QueryEvaluationException | MalformedQueryException | RepositoryException ex) {
                log.error("Evaluation Exception: " + ex);
            } catch (Exception e) {
                log.error("ioexception " + e.toString());
            }
            priorityToFind++;
            //** end View Data

        } catch (Exception ex) {
            log.error("Marmotta Exception: " + ex);
        }

        return parser.parse(" [{\"Fail\":\"Any Data\"}]").getAsJsonArray();
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
