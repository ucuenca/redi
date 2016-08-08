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

import java.io.IOException;
import java.io.InputStream;
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
import org.apache.marmotta.ucuenca.wk.commons.impl.ConstantServiceImpl;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;

import org.apache.marmotta.ucuenca.wk.pubman.api.MicrosoftAcadProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.api.SparqlFunctionsService;

import org.apache.marmotta.ucuenca.wk.pubman.exceptions.PubException;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.model.Value;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;
import org.semarglproject.vocab.OWL;

/**
 * Default Implementation of {@link PubVocabService} Get Data From MICROSOFT
 * ACADEMICS PROVIDER
 *
 * Fernando Baculima CEDIA - Universidad de Cuenca
 *
 */
@ApplicationScoped
public class MicrosoftAcadProviderServiceImpl implements MicrosoftAcadProviderService, Runnable {

    @Inject
    private Logger log;

    @Inject
    private QueriesService queriesService;

    @Inject
    private CommonsServices commonsServices;

    @Inject
    private ConstantService constantService;

    @Inject
    private SparqlFunctionsService sparqlFunctionsService;

    @Inject
    private SparqlService sparqlService;

    private int processpercent = 0;

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

            int allMembers = 0;
//            String nameProviderGraph = "http://ucuenca.edu.ec/wkhuska/provider/MicrosoftAcademicsProvider";
            String getAllAuthorsDataQuery = queriesService.getAuthorsDataQuery(constantService.getAuthorsGraph(), constantService.getEndpointsGraph());

            // TupleQueryResult result = sparqlService.query(QueryLanguage.SPARQL, getAuthors);
            String nameToFind = "";
            String authorResource = "";
            int priorityToFind = 0;
            List<Map<String, Value>> resultAllAuthors = sparqlService.query(QueryLanguage.SPARQL, getAllAuthorsDataQuery);

            /*To Obtain Processed Percent*/
            int allPersons = resultAllAuthors.size();
            int processedPersons = 0;

            //String NS_DBLP = "http://rdf.dblp.com/ns/search/";
            String URL_Academics = "http://academic.research.microsoft.com/json.svc/search?AppId=d4d1924a-5da9-4e8b-a515-093e8a2d1748&AuthorQuery=&ResultObjects=Publication&PublicationContent=AllInfo&StartIdx=1&EndIdx=100";

            RepositoryConnection conUri = null;
            ClientResponse response = null;

            Properties propiedades = new Properties();
            InputStream entrada = null;
            Map<String, String> mapping = new HashMap<String, String>();
            try {
                ClassLoader classLoader = getClass().getClassLoader();
                entrada = classLoader.getResourceAsStream("updatePlatformProcessConfig.properties");
                propiedades.load(entrada);
                for (String source : propiedades.stringPropertyNames()) {
                    String target = propiedades.getProperty(source);
                    mapping.put(source, target);

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
            boolean proccesAllAuthors = Boolean.parseBoolean(mapping.get("proccesAllAuthors").toString());

            for (Map<String, Value> map : resultAllAuthors) {
                if (processedPersons == 100) {
                    break;
                }
                processedPersons++;
                log.info("Autores procesados con MicrosoftA: " + processedPersons + " de " + allPersons);
                authorResource = map.get("subject").stringValue();
                String firstName = map.get("fname").stringValue();
                String lastName = map.get("lname").stringValue();
                priorityToFind = 1;
                boolean AuthorDataisLoad = false;
                int waitTime = 0;
                boolean ask = false;
                if (!proccesAllAuthors) {
                    String askTripletQuery = queriesService.getAskProcessAlreadyAuthorProvider(constantService.getMAGraph(), authorResource);
                    try {

                        ask = sparqlService.ask(QueryLanguage.SPARQL, askTripletQuery);
                        if (ask) {
                            continue;
                        }
                    } catch (Exception ex) {
                        log.error("Marmotta Exception:  " + askTripletQuery);
                    }
                }
                do {
                    try {
                        boolean existNativeAuthor = false;
                        allMembers = 0;
                        nameToFind = priorityFindQueryBuilding(priorityToFind, firstName, lastName);
                        //response = ldClient.retrieveResource(NS_DBLP + nameToFind);
                        String URL_TO_FIND_Microsoft = "http://academic.research.microsoft.com/json.svc/search?AppId=d4d1924a-5da9-4e8b-a515-093e8a2d1748&AuthorQuery=" + nameToFind + "&ResultObjects=Publication&PublicationContent=AllInfo&StartIdx=1&EndIdx=100";

                        boolean dataretrievee = false;
                        if (!proccesAllAuthors) {
                            existNativeAuthor = sparqlService.ask(QueryLanguage.SPARQL, queriesService.getAskResourceQuery(constantService.getMAGraph(), URL_TO_FIND_Microsoft));
                        }
                        if (nameToFind != "" && !existNativeAuthor) {
                            waitTime = 30;
                            try {
                                response = ldClient.retrieveResource(URL_TO_FIND_Microsoft);
                                dataretrievee = true;
                            } catch (DataRetrievalException e) {
                                log.error("Data Retrieval Exception: " + e);
                                log.info("Wating: " + waitTime + " seconds for new Microsoft Academics Query");
                                dataretrievee = false;
//                                        try {
//                                            Thread.sleep(waitTime * 1000);               //1000 milliseconds is one second.
//                                        } catch (InterruptedException ex) {
//                                            Thread.currentThread().interrupt();
//                                        }
                                waitTime += 5;
                            }

                        }//end  if  nameToFind != ""

                        //String nameEndpointofPublications = ldClient.getEndpoint(NS_DBLP + nameToFind).getName();
                        String nameEndpointofPublications = ldClient.getEndpoint(URL_TO_FIND_Microsoft).getName();
                        String providerGraph = constantService.getProviderNsGraph() + "/" + nameEndpointofPublications.replace(" ", "");

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
                        if (dataretrievee)//if the resource data were recovered
                        {
                            conUri = ModelCommons.asRepository(response.getData()).getConnection();
                            conUri.begin();
                            String authorNativeResource = null;

                            //THIS DRIVER NO RETURN MEMBERS OF A SEARCH, ALL DATA IS RELATED WITH 1 AUTHOR
                            //verifying the number of persons retrieved. if it has recovered more than one persons then the filter is changed and search anew,
//                        String getMembersQuery = queriesService.getMembersQuery();
//                        TupleQueryResult membersResult = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getMembersQuery).evaluate();
//                        
//                        while (membersResult.hasNext()) {
//                            allMembers++;
//                            BindingSet bindingCount = membersResult.next();
//                            authorNativeResource = bindingCount.getValue("members").toString();
//                            existNativeAuthor = sparqlService.ask(QueryLanguage.SPARQL, queriesService.getAskResourceQuery(providerGraph, authorNativeResource));
//                        }
                            //the author data was already loaded into the repository, only a sameAs property is associated 
                            //     <http://academic.research.microsoft.com/json.svc/search?AppId=d4d1924a-5da9-4e8b-a515-093e8a2d1748&AuthorQuery=saquicela&ResultObjects=Publication&PublicationContent=AllInfo&StartIdx=1&EndIdx=100> a <http://purl.org/ontology/bibo/Document> ;
                            //    <http://xmlns.com/foaf/0.1/publications>
                            authorNativeResource = URL_TO_FIND_Microsoft;
                            existNativeAuthor = sparqlService.ask(QueryLanguage.SPARQL, queriesService.getAskResourceQuery(providerGraph, authorNativeResource));

                            String InsertQueryOneOf = buildInsertQuery(providerGraph, authorNativeResource, OWL.ONE_OF, authorResource);
                            updatePub(InsertQueryOneOf);

                            if (existNativeAuthor) {
                                String sameAsInsertQuery = buildInsertQuery(providerGraph, authorResource, "http://www.w3.org/2002/07/owl#sameAs", authorNativeResource);
                                updatePub(sameAsInsertQuery);
                            }

                            if (!existNativeAuthor) {
                                //SPARQL obtain all publications of author
                                priorityToFind = 5;
                                String getPublicationsFromProviderQuery = queriesService.getSubjectAndObjectByPropertyQuery("foaf:publications");
                                TupleQuery pubquery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getPublicationsFromProviderQuery); //
                                TupleQueryResult tripletasResult = pubquery.evaluate();
                                while (tripletasResult.hasNext()) {
                                    AuthorDataisLoad = true;

                                    BindingSet tripletsResource = tripletasResult.next();
                                    authorNativeResource = tripletsResource.getValue("subject").toString();
                                    String publicationResource = tripletsResource.getValue("object").toString();
                                    //String publicationProperty = tripletsResource.getValue("publicationProperty").toString();
                                    ///insert sparql query, 
                                    String publicationInsertQuery = buildInsertQuery(providerGraph, authorNativeResource, "http://xmlns.com/foaf/0.1/publications", publicationResource);
                                    updatePub(publicationInsertQuery);

                                    // sameAs triplet    <http://190.15.141.102:8080/dspace/contribuidor/autor/SaquicelaGalarza_VictorHugo> owl:sameAs <http://dblp.org/pers/xr/s/Saquicela:Victor> 
                                    String sameAsInsertQuery = buildInsertQuery(providerGraph, authorResource, "http://www.w3.org/2002/07/owl#sameAs", authorNativeResource);
                                    updatePub(sameAsInsertQuery);

                                }

                                // SPARQL to obtain all data of a publication
                                String getPublicationPropertiesQuery = queriesService.getPublicationPropertiesQuery("foaf:publications");
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
                        }//end IF DATARETRIEVE
                    } catch (Exception e) {
                        log.error("ioexception " + e.toString());
                    }
                    priorityToFind++;
                } while (!AuthorDataisLoad && priorityToFind < 5);//end do while
                //** end View Data
                printPercentProcess(processedPersons, allPersons, "Microsoft Academics");
            }
            return "True for publications";
        } catch (MarmottaException ex) {
            log.error("Marmotta Exception: " + ex);
        }

        return "fail";
    }

    @Override
    public String runTitleProviderTaskImpl() {
        try {

            //new AuthorVersioningJob(log).proveSomething();
            ClientConfiguration conf = new ClientConfiguration();
            //conf.addEndpoint(new DBLPEndpoint());
            LDClient ldClient = new LDClient(conf);

            int allMembers = 0;
            String getAllTitlesDataQuery = queriesService.getAllTitlesDataQuery(constantService.getWkhuskaGraph());

            String titleLiteral = "";
            String publicationResource = "";
            List<Map<String, Value>> resultAllTitles = sparqlService.query(QueryLanguage.SPARQL, getAllTitlesDataQuery);

            /*To Obtain Processed Percent*/
            int allTitles = resultAllTitles.size();
            int processedTitles = 0;

            RepositoryConnection conUri = null;
            ClientResponse response = null;

            Properties propiedades = new Properties();
            InputStream entrada = null;
            Map<String, String> mapping = new HashMap<String, String>();
            try {
                ClassLoader classLoader = getClass().getClassLoader();
                entrada = classLoader.getResourceAsStream("updatePlatformProcessConfig.properties");
                propiedades.load(entrada);
                for (String source : propiedades.stringPropertyNames()) {
                    String target = propiedades.getProperty(source);
                    mapping.put(source, target);

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
            boolean proccesAllTitles = Boolean.parseBoolean(mapping.get("proccesAllAuthors").toString());

            for (Map<String, Value> map : resultAllTitles) {
                processedTitles++;
                log.info("Titulos procesados con Microsoft Academics: " + processedTitles + " de " + allTitles);
                publicationResource = map.get("publications").stringValue();
                titleLiteral = map.get("title").stringValue();
                boolean ask = false;
                if (!proccesAllTitles) {
                    String askTripletQuery = queriesService.getAskResourcePropertieQuery(constantService.getWkhuskaGraph(), publicationResource, "bibo:abstract");

                    try {
                        ask = sparqlService.ask(QueryLanguage.SPARQL, askTripletQuery);
                        if (ask) {
                            continue;
                        }
                    } catch (MarmottaException ex) {
                        log.info("Marmotta Exception: While execute ask query: " + askTripletQuery);

                    } catch (Exception e) {
                        log.info("While execute ask query: " + askTripletQuery);

                    }

                }

                try {

                    String titleToFind = titleLiteral;
                    String URL_TO_FIND_Microsoft = "http://academic.research.microsoft.com/json.svc/search?AppId=d4d1924a-5da9-4e8b-a515-093e8a2d1748&TitleQuery=" + titleToFind.replace(" ", "%20") + "&ResultObjects=Publication&PublicationContent=AllInfo&StartIdx=1&EndIdx=100";

                    allMembers = 0;

                    boolean dataretrievee = false;//( Data Retrieve Exception )

                    try {
                        response = ldClient.retrieveResource(URL_TO_FIND_Microsoft);
                        dataretrievee = true;
                    } catch (DataRetrievalException e) {
                        log.error("Title Retrieval Exception: " + e);
                        dataretrievee = false;
//                               
                    }

                    if (dataretrievee) {
                        conUri = ModelCommons.asRepository(response.getData()).getConnection();
                        conUri.begin();
                        String publicationNativeResource = null;
                        //verifying the number of publications retrieved. if it has recovered more than one publications  then not continue,
                        String getMembersQuery = queriesService.getObjectByPropertyQuery("foaf:publications");
                        TupleQueryResult membersResult = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getMembersQuery).evaluate();
                        //  allMembers = Iterations.asList(membersResult).size();
                        while (membersResult.hasNext()) {
                            allMembers++;
                            BindingSet bindingCount = membersResult.next();
                            publicationNativeResource = bindingCount.getValue("object").toString();
                        }

                        /**
                         * Exception if problems in tripletasResult null.
                         */
                        try {
                            if (allMembers == 1) {
                                //SPARQL to Retrieve and Insert the abstract from MA
                                String getAbstractQuery = queriesService.getObjectByPropertyQuery(publicationNativeResource, "bibo:abstract");
                                TupleQuery pubquery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getAbstractQuery); //
                                TupleQueryResult tripletasResult = pubquery.evaluate();

                                while (tripletasResult.hasNext()) {
                                    BindingSet tripletsResource = tripletasResult.next();
                                    String abstractLiteral = tripletsResource.getValue("object").toString();
                                    // insert sparql query, 
                                    String abstractInsertQuery = buildInsertQuery(constantService.getWkhuskaGraph(), publicationResource, "bibo:abstract", abstractLiteral);
                                    updatePub(abstractInsertQuery);
                                }
                                // SPARQL to Retrieve and Insert keywords ( bibo:Quote) from MA
                                String getKeywordsQuery = queriesService.getObjectByPropertyQuery(publicationNativeResource, "bibo:Quote");
                                TupleQuery keywordsquery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getKeywordsQuery); //
                                TupleQueryResult keywordsResult = keywordsquery.evaluate();
                                while (keywordsResult.hasNext()) {
                                    BindingSet keywordsBs = keywordsResult.next();
                                    String keywordLiteral = keywordsBs.getValue("object").toString();
                                    // insert sparql query, 
                                    String keywordInsertQuery = buildInsertQuery(constantService.getWkhuskaGraph(), publicationResource, "bibo:Quote", keywordLiteral);
                                    updatePub(keywordInsertQuery);
                                }
                            }//end if numMembers=1
                            else if (allMembers > 1) {
                                //SPARQL to Retrieve all publications and titles from MA
                                String getTitlesQuery = queriesService.getSubjectAndObjectByPropertyQuery("dct:title");
                                TupleQuery titlesquery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getTitlesQuery); //
                                TupleQueryResult titlesResult = titlesquery.evaluate();

                                while (titlesResult.hasNext()) {
                                    BindingSet titleResource = titlesResult.next();
                                    String titlefromMA = titleResource.getBinding("object").getValue().stringValue();;
                                    publicationNativeResource = titleResource.getValue("subject").toString();
                                    titlefromMA = titlefromMA.replace(".", "").replace("-", "");
                                    titleToFind = titleToFind.replace(".", "").replace("-", "");

                                    if (titleToFind.compareTo(titlefromMA) == 0) {
                                        //SPARQL to Retrieve and Insert the abstract from MA
                                        String getAbstractQuery = queriesService.getObjectByPropertyQuery(publicationNativeResource, "bibo:abstract");
                                        TupleQuery pubquery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getAbstractQuery); //
                                        TupleQueryResult tripletasResult = pubquery.evaluate();

                                        while (tripletasResult.hasNext()) {
                                            BindingSet tripletsResource = tripletasResult.next();
                                            String abstractLiteral = tripletsResource.getValue("object").toString();
                                            // insert sparql query, 
                                            String abstractInsertQuery = buildInsertQuery(constantService.getWkhuskaGraph(), publicationResource, "bibo:abstract", abstractLiteral);
                                            updatePub(abstractInsertQuery);
                                        }
                                        // SPARQL to Retrieve and Insert keywords ( bibo:Quote) from MA
                                        String getKeywordsQuery = queriesService.getObjectByPropertyQuery(publicationNativeResource, "bibo:Quote");
                                        TupleQuery keywordsquery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getKeywordsQuery); //
                                        TupleQueryResult keywordsResult = keywordsquery.evaluate();
                                        while (keywordsResult.hasNext()) {
                                            BindingSet keywordsBs = keywordsResult.next();
                                            String keywordLiteral = keywordsBs.getValue("object").toString();
                                            // insert sparql query, 
                                            String keywordInsertQuery = buildInsertQuery(constantService.getWkhuskaGraph(), publicationResource, "bibo:Quote", keywordLiteral);
                                            updatePub(keywordInsertQuery);
                                        }
                                        break;
                                    }
                                }// end title.compareto==0
                            }// end else if (allMembers > 1 ) {
                        } catch (Exception e) {
                            log.info("ERROR in full name:" + publicationNativeResource);
                        } finally {
                            conUri.commit();
                            conUri.close();
                        }
                    }
                } catch (QueryEvaluationException | MalformedQueryException | RepositoryException ex) {
                    log.error("Evaluation Exception: " + ex);
                } catch (Exception e) {
                    log.error("ioexception " + e.toString());
                }
                printPercentProcess(processedTitles, allTitles, "Microsoft Academics");
            }
            return "True for enrichment with Microsoft Academics";
        } catch (MarmottaException ex) {
            log.error("Marmotta Exception: " + ex);
        }
        return "fail";
    }

    public String priorityFindQueryBuilding(int priority, String firstName, String lastName) {
        try {
            switch (priority) {

                case 1:
                    return firstName.replace(" ", "_") + "_" + lastName.replace(" ", "_");
                case 2:
                    return firstName.replace(" ", "_") + "_" + lastName.split(" ")[0];
                case 3:
                    return firstName.split(" ")[0] + "_" + lastName.replace(" ", "_");
                case 4:
                    return firstName.split(" ")[0] + "_" + lastName.split(" ")[0];
            }
        } catch (Exception e) {
            log.error("ERRRO WITH MANES: " + firstName + "_" + lastName);
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
        runTitleProviderTaskImpl();
        // runPublicationsProviderTaskImpl("uri");
    }

}
