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

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequenceWithBigrams;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;
import info.debatty.java.stringsimilarity.Cosine;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.commons.io.LineIterator;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.endpoint.rdf.SPARQLEndpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ucuenca.wk.authors.api.AuthorService;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointService;
import org.apache.marmotta.ucuenca.wk.authors.api.SparqlEndpoint;
import org.apache.marmotta.ucuenca.wk.authors.api.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.DaoException;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.UpdateException;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.KeywordsService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;

/**
 * Default Implementation of {@link AuthorService} Fernando B. CEDIA
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
    private CommonsServices commonsService;
    
    @Inject
    private KeywordsService kservice;
    
    @Inject
    private EndpointService authorsendpointService;
    
    @Inject
    private ConstantService constantService;
    
    private static final int LIMIT = 5000;
    private static final double COSINE_DISTANCE = 0.1;
    private int processpercent = 0;
    private static List<SparqlEndpoint> endpoints;
    private final List<String> stopwords = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        endpoints = authorsendpointService.listEndpoints();
        
        BufferedReader input = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("helpers/stoplist.txt")));
        LineIterator it = new LineIterator(input);
        String line;
        while (it.hasNext()) {
            line = it.nextLine();
            String[] words = line.split("\\s+");
            stopwords.addAll(Arrays.asList(words));
        }
        it.close();
    }

    /**
     * authorDocumentProperty : http://rdaregistry.info/Elements/a/P50161 |
     * http://rdaregistry.info/Elements/a/P50195
     *
     * @throws org.apache.marmotta.ucuenca.wk.authors.exceptions.DaoException
     * @throws org.apache.marmotta.ucuenca.wk.authors.exceptions.UpdateException
     */
    //private String documentProperty = "http://rdaregistry.info";
    @Override
    public String runAuthorsUpdateMultipleEP() throws DaoException, UpdateException {
        Boolean someUpdate = false;
        StringBuilder response = new StringBuilder();
        if (!endpoints.isEmpty()) {
            for (SparqlEndpoint endpoint : endpoints) {
                if (Boolean.parseBoolean(endpoint.getStatus())) {
                    try {
                        log.info("Extraction started for endpoint {}.", endpoint.getName());
                        response.append(extractAuthors(endpoint));
                    } catch (RepositoryException ex) {
                        log.error("Excepcion de repositorio. Problemas en conectarse a " + endpoint.getName());
                        java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (MalformedQueryException ex) {
                        log.error("Excepcion de forma de consulta. Revise consultas SPARQL y sintaxis. Revise estandar SPARQL");
                        java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (QueryEvaluationException ex) {
                        log.error("Excepcion de ejecucion de consulta. No se ha ejecutado la consulta general para la obtencion de los Authores.");
                        java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    someUpdate = true;
                }
            }
            response.append(extractSubjects());
            
            if (!someUpdate) {
                return "Any  Endpoints";
            }
            return response.toString();
        } else {
            return "No Endpoints";
        }
        
    }
    
    private String extractAuthors(SparqlEndpoint endpoint) throws DaoException, UpdateException, RepositoryException, MalformedQueryException, QueryEvaluationException, Exception {
        int tripletasCargadas = 0; //cantidad de tripletas actualizadaas
        int contAutoresNuevosNoCargados = 0; //cantidad de actores nuevos no cargados
        int contAutoresNuevosEncontrados = 0; //hace referencia a la cantidad de actores existentes en el archivo temporal antes de la actualizacion

        /* Conecting to repository using LDC ( Linked Data Client ) Library */
        ClientConfiguration config = new ClientConfiguration();
        config.addEndpoint(new SPARQLEndpoint(endpoint.getName(), endpoint.getEndpointUrl(), "^" + "http://" + ".*"));
        LDClientService ldClientEndpoint = new LDClient(config);
        
        Repository endpointTemp = new SPARQLRepository(endpoint.getEndpointUrl());
        TupleQueryResult result = executeQuery(endpointTemp, queriesService.getCountPersonQuery(endpoint.getGraph()));
        int authorsSize = Integer.parseInt(result.next().getBinding("count").getValue().stringValue());//getAuthorsSize(conn, endpoint.getGraph());//Integer.parseInt(bindingCount.getValue("count").stringValue());

        //endpointTemp.initialize();
        //After that you can use the endpoint like any other Sesame Repository, by creating a connection and doing queries on that:
        // RepositoryConnection conn = endpointTemp.getConnection();
        //Query that let me obtain all resource related with author from source sparqlendpoint 
        String getAuthorsQuery = queriesService.getAuthorsQuery(endpoint.getGraph());
        String resource = "";
        for (int offset = 0; offset < authorsSize; offset += 5000) {
            try {
                TupleQueryResult authorsResult = executeQuery(endpointTemp, getAuthorsQuery + getLimitOffset(LIMIT, offset));//conn.prepareTupleQuery(QueryLanguage.SPARQL, getAuthorsQuery + getLimitOffset(limit, offset)).evaluate();
                while (authorsResult.hasNext()) {
                    resource = authorsResult.next().getValue("s").stringValue();
                    if (!sparqlFunctionsService.askAuthor(queriesService.getAskObjectQuery(constantService.getAuthorsGraph(), resource))) {
                        contAutoresNuevosEncontrados++;
                        printPercentProcess(contAutoresNuevosEncontrados, authorsSize, endpoint.getName());
                        String localResource = buildLocalURI(resource);
                        
                        String getResourcePropertyQuery = queriesService.getRetrieveResourceQuery();
                        ClientResponse response = ldClientEndpoint.retrieveResource(resource);
                        Repository repository = ModelCommons.asRepository(response.getData());
                        RepositoryConnection conn = repository.getConnection();
                        TupleQueryResult tripletasResult = conn.prepareTupleQuery(QueryLanguage.SPARQL, getResourcePropertyQuery).evaluate();
                        
                        while (tripletasResult.hasNext()) {
                            BindingSet tripletsResource = tripletasResult.next();
                            String predicate = tripletsResource.getValue("y").stringValue();
                            String object = tripletsResource.getValue("z").stringValue();
                            
                            if (predicate.contains("http://rdaregistry.info")) {
                                continue;
                            }
                            if (predicate.contains(OWL.SAMEAS.toString())) { // If sameas found include the provenance
                                SparqlEndpoint newEndpoint = matchWithProvenance(object);
                                if (newEndpoint != null) {
                                    String provenanceQueryInsert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, DCTERMS.PROVENANCE.toString(), newEndpoint.getResourceId());
                                    sparqlFunctionsService.updateAuthor(provenanceQueryInsert);
                                }
                            }
                            if (!tripletasResult.hasNext()) { // Insert sameAs abd provenance in last iteration
                                String sameAs = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, OWL.SAMEAS.toString(), resource);
                                sparqlFunctionsService.updateAuthor(sameAs);
                                
                                String provenanceQueryInsert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, DCTERMS.PROVENANCE.toString(), endpoint.getResourceId());
                                sparqlFunctionsService.updateAuthor(provenanceQueryInsert);
                            }
                            String queryAuthorInsert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, predicate, object);
                            sparqlFunctionsService.updateAuthor(queryAuthorInsert);
                        }
                        conn.commit();
                        conn.close();
                        repository.shutDown();
                    }
                }
            } catch (QueryEvaluationException ex) {
                log.error("Something happened evaluating the query Error: {}", ex.getMessage());
            } catch (DataRetrievalException ex) {
                log.error("Something happened retrieving triples for resource {} Error: {}", resource, ex.getMessage());
                contAutoresNuevosNoCargados++;
            } finally {
                endpointTemp.shutDown();
                ldClientEndpoint.shutdown();
            }
            
        }
        
        log.info(endpoint.getName() + " endpoint. Se detectaron " + contAutoresNuevosEncontrados + " autores nuevos ");
        log.info(endpoint.getName() + " endpoint. Se cargaron " + (contAutoresNuevosEncontrados - contAutoresNuevosNoCargados) + " autores nuevos exitosamente");
        log.info(endpoint.getName() + " endpoint. Se cargaron " + tripletasCargadas + " tripletas ");
        log.info(endpoint.getName() + " endpoint. No se pudieron cargar " + contAutoresNuevosNoCargados + " autores");
        
        return "Carga Finalizada. Revise Archivo Log Para mas detalles";
    }
    
    private String extractSubjects() {
        try {
            String allAuthorsQuery = queriesService.getAuthors();
            Repository repository = new SPARQLRepository(constantService.getSPARQLEndpointURL());
            TupleQueryResult allAuthors = executeQuery(repository, allAuthorsQuery);
            
            while (allAuthors.hasNext()) {
                String authorResource = allAuthors.next().getBinding("s").getValue().stringValue();
                String sameAsAuthorsQuery = queriesService.getSameAsAuthors(authorResource);
                TupleQueryResult sameAsAuthors = executeQuery(repository, sameAsAuthorsQuery);
                while (sameAsAuthors.hasNext()) { // for each author
                    List<String> documents = new ArrayList<>();
                    List<String> subjects = new ArrayList<>();
                    
                    String sameAsResource = sameAsAuthors.next().getBinding("o").getValue().stringValue();
                    SparqlEndpoint endpoint = matchWithProvenance(sameAsResource);
                    if (endpoint == null) {
                        log.warn("There isn't an endpoint for {} resource.", sameAsResource);
                        continue;
                    }
                    ClientConfiguration conf = new ClientConfiguration();
                    conf.addEndpoint(new SPARQLEndpoint(endpoint.getName(), endpoint.getEndpointUrl(), "^http://.*"));
                    LDClient ldc = new LDClient(conf);
                    ClientResponse response = ldc.retrieveResource(sameAsResource);
                    RepositoryConnection connection = ModelCommons.asRepository(response.getData()).getConnection();
                    TupleQueryResult tempResult = connection.prepareTupleQuery(QueryLanguage.SPARQL, queriesService.getRetrieveResourceQuery())
                            .evaluate();
                    
                    while (tempResult.hasNext()) {
                        BindingSet triples = tempResult.next();
                        String predicate = triples.getBinding("y").getValue().stringValue();
                        String object = triples.getBinding("z").getValue().stringValue();
                        
                        if (predicate.contains("http://rdaregistry.info")) {
                            subjects.addAll(extractSubjectsFromDocument(ldc, object));
                            documents.addAll(extractContentFromDocument(ldc, object));
                        }
                    }
                    combineSubjects(authorResource, documents, subjects);
                    connection.commit();
                    connection.close();
                    ldc.shutdown();
                }
            }
            repository.shutDown();
            log.info("Finished to extract subjects");
        } catch (QueryEvaluationException | RepositoryException | MalformedQueryException | DataRetrievalException ex) {
            log.error("Cannnot extract subjects. Error: {}", ex);
            return ex.getMessage();
        }
        return "Subjects Extracted";
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
    
    private List<String> extractContentFromDocument(LDClientService ldClient, String documentURI) throws DataRetrievalException, RepositoryException, MalformedQueryException, QueryEvaluationException {
        
        List<String> documents = new ArrayList<>();
        String getDocQuery = queriesService.getAbstractAndTitleQuery(documentURI);
        ClientResponse response = ldClient.retrieveResource(documentURI);
        Repository repository = ModelCommons.asRepository(response.getData());
        RepositoryConnection conn = repository.getConnection();
        conn.begin();
        TupleQueryResult result = conn.prepareTupleQuery(QueryLanguage.SPARQL, getDocQuery).evaluate();
        while (result.hasNext()) {
            BindingSet tripletsATResource = result.next();
            StringBuilder document = new StringBuilder();
            for (Binding binding : tripletsATResource) {
                document.append(binding.getValue().stringValue()).append(' ');
            }
            
            documents.add(document.toString());
        }
        conn.commit();
        conn.close();
        repository.shutDown();
        return documents;
    }
    
    private List<String> extractSubjectsFromDocument(LDClientService ldClient, String documentURI)
            throws DataRetrievalException, RepositoryException, MalformedQueryException, QueryEvaluationException {
        Set<String> subjects = new HashSet<>();
        String getRetrieveKeysQuery = "";
        ClientResponse respPub = ldClient.retrieveResource(documentURI);
        RepositoryConnection conUriPub = ModelCommons.asRepository(respPub.getData()).getConnection();
        conUriPub.begin();
        getRetrieveKeysQuery = queriesService.getRetrieveKeysQuery();
        TupleQuery keysquery = conUriPub.prepareTupleQuery(QueryLanguage.SPARQL, getRetrieveKeysQuery); //
        TupleQueryResult tripletaskeysResult = keysquery.evaluate();
        while (tripletaskeysResult.hasNext()) {
            BindingSet tripletskeysResource = tripletaskeysResult.next();
            String keyword = tripletskeysResource.getValue("subject").stringValue().trim();
            subjects.add(keyword);
        }
        conUriPub.commit();
        conUriPub.close();
        return new ArrayList<>(subjects);
    }
    
    private String buildLocalURI(String endpointURI) {
        return constantService.getAuthorResource() + endpointURI.substring(endpointURI.lastIndexOf('/') + 1);
    }
    
    private List<String> findTopics(List<String> documents, int numTopics, int numWords) {
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
        return new ArrayList<>(topics);
    }

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
    
    private void combineSubjects(String localSubject, List<String> documents, List<String> subjects) {
        List<String> topics = findTopics(documents, 5, 15);
        Set<String> selectedSubjects = new HashSet<>(getWeightedSubjects(subjects, topics));
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
        log.info("Resource {} has {} documents and {} subjects ", localSubject, documents.size(), selectedSubjects.size());
    }
    
    private List<String> getWeightedSubjects(List<String> subjects, List<String> topics) {
        List<String> result = new ArrayList<>();
        for (String subject : subjects) {
            for (String topic : topics) {
                if (areSimilar(subject.toLowerCase(), topic)) {
                    result.add(subject);
                }
            }
        }
        return result;
    }
    
    private boolean areSimilar(String subject, String topic) {
        Cosine l = new Cosine();
        for (String s : subject.split(" ")) {
            if (l.distance(s, topic) <= COSINE_DISTANCE) {
                return true;
            }
        }
        return false;
    }
    
    private SparqlEndpoint matchWithProvenance(String object) {
        for (SparqlEndpoint endpoint : endpoints) {
            if (object.matches(endpoint.getGraph() + "(.*)")) {
                return endpoint;
            }
        }
        return null;
    }
    
}
