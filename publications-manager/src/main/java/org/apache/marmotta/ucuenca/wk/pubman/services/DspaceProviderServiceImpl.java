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
import java.io.BufferedReader;
import java.io.FileReader;
//import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.commons.vocabulary.FOAF;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.endpoint.rdf.SPARQLEndpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.impl.ConstantServiceImpl;
import org.apache.marmotta.ucuenca.wk.commons.service.DistanceService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.KeywordsService;
import org.apache.marmotta.ucuenca.wk.commons.service.GetAuthorsGraphData;

import org.apache.marmotta.ucuenca.wk.authors.api.EndpointService;
import org.apache.marmotta.ucuenca.wk.authors.api.SparqlEndpoint;

import org.apache.marmotta.ucuenca.wk.pubman.api.SparqlFunctionsService;

import org.apache.marmotta.ucuenca.wk.pubman.exceptions.PubException;
import org.apache.marmotta.ucuenca.wk.pubman.api.DspaceProviderService;
//import org.openrdf.model.Model;
//import org.openrdf.model.Statement;

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;

import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sparql.SPARQLRepository;
//import org.openrdf.rio.RDFFormat;
//import org.openrdf.rio.RDFHandlerException;
//import org.openrdf.rio.RDFWriter;
//import org.openrdf.rio.Rio;
import org.semarglproject.vocab.OWL;

/**
 * Default Implementation of {@link PubVocabService}
 *
 * @author Jose Luis Cullcay
 */
@ApplicationScoped
public class DspaceProviderServiceImpl implements DspaceProviderService, Runnable {

    @Inject
    private Logger log;

    @Inject
    private QueriesService queriesService;

    @Inject
    private ConstantService constantService;

    @Inject
    private CommonsServices commonsServices;

    @Inject
    private GetAuthorsGraphData getauthorsData;

    @Inject
    private DistanceService distance;

    @Inject
    private SparqlFunctionsService sparqlFunctionsService;
    
    @Inject
    private EndpointService authorsendpointService;

    List<Map<String, Value>> uniNames = new ArrayList<>(); //nombre de universidades
    
    //@Inject
    //private SparqlService sparqlService;


    @Override
    public String runPublicationsProviderTaskImpl(String param) {
        
        try {
            String allAuthorsQuery = queriesService.getAuthors();
            Repository repository = new SPARQLRepository(constantService.getSPARQLEndpointURL());
            TupleQueryResult allAuthors = executeQuery(repository, allAuthorsQuery);
            
            int authorCount = 0;
            while (allAuthors.hasNext()) {
                String authorResource = allAuthors.next().getBinding("s").getValue().stringValue();
                authorCount++;
                
                //guardar en la variable sameAuthors los autores que ya tienen sameAs;
                Set<String> sameAuthors = getAllSameAuthors(repository, authorResource);
                
                for (String author : sameAuthors) {
                    try {
                        // 1. get the endpointurl from the author
                        //Encontramos el provenance del autor actual
                        String getNamesQuery = queriesService.getAuthorDataQuery(authorResource);
                        TupleQueryResult resultAuthor = executeQuery(repository, getNamesQuery);
                        String endpointId = null;
                        if (resultAuthor.hasNext()) {
                            BindingSet next = resultAuthor.next();
                            endpointId = next.getBinding("provenance").getValue().stringValue();
                            //lastName = next.getBinding("lname").getValue().stringValue();
                            //fullName = namesAuthor.next().getBinding("name").getValue().stringValue();
                        }
                        //provenance
                        SparqlEndpoint endpoint = authorsendpointService.getEndpoint(endpointId);

                        /* Conecting to repository using LDC ( Linked Data Client ) Library */
                        ClientConfiguration config = new ClientConfiguration();
                        config.addEndpoint(new SPARQLEndpoint(endpoint.getName(), endpoint.getEndpointUrl(), "^" + "http://" + ".*"));
                        LDClientService ldClientEndpoint = new LDClient(config);

                        Repository endpointTempRepo = new SPARQLRepository(endpoint.getEndpointUrl());
                        TupleQueryResult resultArticles = executeQuery(endpointTempRepo, queriesService.getArticlesFromDspaceQuery(endpoint.getGraph(), author));

                        // 2. get the publications from author and save them with authorResource link
                        String articleId = null;
                        if (resultArticles.hasNext()) {
                            BindingSet next = resultArticles.next();
                            articleId = next.getBinding("docu").getValue().stringValue();

                            saveArticleFromDspace(endpointTempRepo, repository, articleId, authorResource, ldClientEndpoint);

                        }

                        endpointTempRepo.shutDown();
                    } catch (Exception ex) {
                        log.error("Exception: " + ex);
                    }
                }
                
                
                
            }
            repository.shutDown();
            log.info("Finished to search publications from authors DSpace.");
            return "True for publications from Dspace";
            
        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
            log.error("Exception: " + ex);
        }
        return "fail";
    }
    
    private Set<String> getAllSameAuthors(Repository repository, String authorResource) {
        Set<String> sameAuthors = new HashSet<String>();
        sameAuthors.add(authorResource);
        try {
            
            String sameAsAuthorsQuery = queriesService.getSameAsAuthors(constantService.getAuthorsGraph(), authorResource);
            TupleQueryResult sameAsAuthors = executeQuery(repository, sameAsAuthorsQuery);
            while (sameAsAuthors.hasNext()) { // for each author
                String sameAsResource = sameAsAuthors.next().getBinding("o").getValue().stringValue();
                sameAuthors.add(sameAsResource);
                
                sameAsAuthorsQuery = queriesService.getSameAsAuthors(constantService.getSameAuthorsGraph(), sameAsResource);
                TupleQueryResult sameAsAuthors2 = executeQuery(repository, sameAsAuthorsQuery);
                while (sameAsAuthors2.hasNext()) { // for each author
                    String sameAsResource2 = sameAsAuthors2.next().getBinding("o").getValue().stringValue();
                    sameAuthors.add(sameAsResource2);
                }
            }
            
        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
            log.error("Exception: " + ex);
        }
        return sameAuthors;
    }
    
    private void saveArticleFromDspace(Repository DspaceRepo, Repository localRepo, String articleId, 
            String authorResource, LDClientService ldClientEndpoint) {
        //TupleQueryResult result = executeQuery(endpointTemp, queriesService.getArticlesFromDspaceQuery(endpoint.getGraph(), author));
        try {
            String getResourcePropertyQuery = queriesService.getRetrieveResourceQuery();
            ClientResponse response = ldClientEndpoint.retrieveResource(articleId);
            Repository repository = ModelCommons.asRepository(response.getData());
            RepositoryConnection conn = repository.getConnection();
            TupleQueryResult tripletasResult = conn.prepareTupleQuery(QueryLanguage.SPARQL, getResourcePropertyQuery).evaluate();

            while (tripletasResult.hasNext()) {
                BindingSet tripletsResource = tripletasResult.next();
                String predicate = tripletsResource.getValue("y").stringValue();
                String object = tripletsResource.getValue("z").stringValue();

                /*if (predicate.contains("http://rdaregistry.info")
                        || predicate.contains("http://www.w3.org/2000/01/rdf-schema#label")
                        || object.contains("http://xmlns.com/foaf/0.1/Agent")) {
                    continue;
                }*/
                if ("http://purl.org/dc/terms/title".equals(predicate)) { 
                    String queryPublicationInsert = queriesService.buildInsertQuery(constantService.getDspaceGraph(), authorResource, predicate, object);
                    sparqlFunctionsService.updatePub(queryPublicationInsert);
                    continue;
                }
                
            }
            conn.commit();
            conn.close();
        } catch (Exception ex) {
            log.error("Exception: " + ex);
        }

    }
    
    private TupleQueryResult executeQuery(Repository repository, String query) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        if (!repository.isInitialized()) {
            repository.initialize();
        }
        RepositoryConnection conn = repository.getConnection();
        conn.begin();
        TupleQueryResult result = conn.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate();
        conn.close();
        return result;
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

    public void insertSubResources(RepositoryConnection conUri, TupleQueryResult tripletasResult, String providerGraph) {
        try {
            String getPublicationPropertiesQuery = queriesService.getPublicationPropertiesAsResourcesQuery();
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
        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
            java.util.logging.Logger.getLogger(DspaceProviderServiceImpl.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
}
