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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;

import org.apache.marmotta.ucuenca.wk.pubman.api.AcademicsKnowledgeProviderService;
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
import org.openrdf.repository.RepositoryException;
import org.semarglproject.vocab.OWL;

/**
 * Default Implementation of {@link PubVocabService} Get Data From MICROSOFT
 * ACADEMICS PROVIDER
 *
 * Freddy Sumba CEDIA - Universidad de Cuenca
 *
 */
@ApplicationScoped
public class AcademicsKnowledgeProviderServiceImpl implements AcademicsKnowledgeProviderService, Runnable {

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

    @Override
    public String runPublicationsProviderTaskImpl() {
        try {

            ClientConfiguration conf = new ClientConfiguration();
            LDClient ldClient = new LDClient(conf);

            String getAllAuthorsDataQuery = queriesService.getAuthorsDataQuery(constantService.getAuthorsGraph(), constantService.getEndpointsGraph());

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
                int proceced = 0;
                List<String> keysubscriptions = new ArrayList<>();
                keysubscriptions.add("f66e8b1a39634d9591151a8efd80cfc2");
                int key = 0;
                do {
                    try {
                        nameToFind = priorityFindQueryBuilding(priorityToFind, firstName, lastName);
                        proceced++;

                        String URL_TO_FIND_Microsoft = "https://api.projectoxford.ai/academic/v1.0/evaluate?expr=Composite(AA.AuN==%27" + nameToFind + "%27)&attributes=Id,Ti,Y,D,CC,ECC,AA.AuN,AA.AuId,AA.AfN,AA.AfId,F.FN,F.FId,J.JN,J.JId,C.CN,C.CId,RId,W,E,D&E=DN,D,S,S.Ty,S.U,VFN,VSN,V,I,FP,LP,DOI&subscription-key=" + keysubscriptions.get(key) + "&count=100";

                        boolean dataretrievee = false;

                        if (nameToFind != "" ) {
                            waitTime = 30;
                            try {
                                response = ldClient.retrieveResource(URL_TO_FIND_Microsoft);
                                dataretrievee = true;
                            } catch (DataRetrievalException e) {
                                log.error("Data Retrieval Exception: " + e);
                                log.info("Wating: " + waitTime + " seconds for new Microsoft Academics Query");
                                dataretrievee = false;
                            }

                        }

                        String nameEndpointofPublications = ldClient.getEndpoint(URL_TO_FIND_Microsoft).getName();
                        String providerGraph = constantService.getProviderNsGraph() + "/" + nameEndpointofPublications.replace(" ", "");

                        if (dataretrievee)
                        {
                            conUri = ModelCommons.asRepository(response.getData()).getConnection();
                            conUri.begin();
                            String authorNativeResource = null;

                         
                            authorNativeResource = URL_TO_FIND_Microsoft;
                            boolean existNativeAuthor = sparqlService.ask(QueryLanguage.SPARQL, queriesService.getAskResourceQuery(providerGraph, authorNativeResource));

                            String InsertQueryOneOf = buildInsertQuery(providerGraph, authorNativeResource, OWL.ONE_OF, authorResource);
                            updatePub(InsertQueryOneOf);

                            if (existNativeAuthor) {
                                String sameAsInsertQuery = buildInsertQuery(providerGraph, authorResource, "http://www.w3.org/2002/07/owl#sameAs", authorNativeResource);
                                updatePub(sameAsInsertQuery);
                            }

                            if (!existNativeAuthor) {
                                //SPARQL obtain all publications of author
                                priorityToFind = 5;
                                String getPublicationsFromProviderQuery = queriesService.getSubjectAndObjectByPropertyQuery("http://purl.org/ontology/bibo/Document");
                                TupleQuery pubquery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getPublicationsFromProviderQuery); //
                                TupleQueryResult tripletasResult = pubquery.evaluate();
                                while (tripletasResult.hasNext()) {
                                    AuthorDataisLoad = true;

                                    BindingSet tripletsResource = tripletasResult.next();
                                    authorNativeResource = tripletsResource.getValue("subject").toString();
                                    String publicationResource = tripletsResource.getValue("object").toString();
                                    String publicationInsertQuery = buildInsertQuery(providerGraph, authorNativeResource, "http://xmlns.com/foaf/0.1/publications", publicationResource);
                                    updatePub(publicationInsertQuery);

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

    public String priorityFindQueryBuilding(int priority, String firstName, String lastName) {
        return (firstName + lastName).replace(" ", "%20");
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
        runPublicationsProviderTaskImpl();
        // runPublicationsProviderTaskImpl("uri");
    }

}
