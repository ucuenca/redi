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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
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
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;

/**
 * Default Implementation of {@link AuthorService} Fernando B. CEDIA
 *
 * @author Xavier Sumba
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
    private static final int MAX_SUBJECTS = 15;
    private static List<SparqlEndpoint> endpoints;
    private final List<String> stopwords = new ArrayList<>();
    private int processpercent = 0;

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

        return String.format("Carga Finalizada para %s endpoint. Revise Archivo Log Para mas detalles \n", endpoint.getName());
    }

    private String extractSubjects() {
        try {
            Repository repository = new SPARQLRepository(constantService.getSPARQLEndpointURL());
            int numAuthors = Integer.parseInt(executeQuery(repository, queriesService.getCountAuthors()).next().getBinding("count").getValue().stringValue());
            for (int offset = 0; offset < numAuthors; offset += 5000) {
                String allAuthorsQuery = queriesService.getAuthors() + getLimitOffset(LIMIT, offset);
                TupleQueryResult allAuthors = executeQuery(repository, allAuthorsQuery);

                while (allAuthors.hasNext()) {
                    String authorResource = allAuthors.next().getBinding("s").getValue().stringValue();
                    // Get num subjects 
                    String numSubjectsQuery = queriesService.getCountSubjects(authorResource);
                    int numSubjects = Integer.parseInt(executeQuery(repository, numSubjectsQuery).next().getValue("count").stringValue());

                    // Get SameAsAuthors
                    String sameAsAuthorsQuery = queriesService.getSameAsAuthors(authorResource);
                    TupleQueryResult sameAsAuthors = executeQuery(repository, sameAsAuthorsQuery);
                    while (sameAsAuthors.hasNext() && numSubjects < 5) { // extract subjects for each author
                        Set<String> documents = new HashSet<>();
                        Set<String> subjects = new HashSet<>();
                        //Set<String> mentions = new HashSet<>();

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
                        for (Statement statement : response.getData()) {
                            if (statement.getPredicate().stringValue().contains("http://rdaregistry.info")) {
                                Set<String>[] result = extractSubjectsAndDocuments(ldc, statement.getObject().stringValue());
                                subjects.addAll(result[0]);
                                documents.addAll(result[1]);
                                subjects.addAll(result[2]);
                            }
                        }
                        combineSubjects(authorResource, documents, subjects);
                        ldc.shutdown();
                    }
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

    private void combineSubjects(String localSubject, Set<String> documents, Set<String> subjects) {//, Set<String> mentions) {
        // find topics and weight frequent words
        List<String> topics = findTopics(new ArrayList(documents), 5, 15);
        Set<String> selectedSubjects = new HashSet<>(getWeightedSubjects(subjects, topics));

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
        log.info("Resource {} has {} documents and {} subjects ", localSubject, documents.size(), selectedSubjects.size());
    }

    private List<String> getWeightedSubjects(Set<String> subjects, List<String> topics) {
        List<String> result = new ArrayList<>();
        Map<String, Integer> rank = initializeHash(subjects);

        for (Map.Entry<String, Integer> entry : rank.entrySet()) {
            String subject = entry.getKey();
            for (String topic : topics) {
                //if (areSimilar(subject.toLowerCase(), topic)) {
                if (subject.toLowerCase().contains(topic)) {
                    rank.put(subject, rank.get(subject) + 1);
                }
            }
        }

        for (Entry<String, Integer> entry : selectRankedSubjects(rank)) {
            if (entry.getValue() > 0) {
                result.add(entry.getKey());
            }
            if (result.size() == MAX_SUBJECTS) {
                break;
            }
        }
        return result;
    }

    private List<Entry<String, Integer>> selectRankedSubjects(Map<String, Integer> map) {

        map.values().remove(0);
        List<Entry<String, Integer>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Entry<String, Integer>>() {
            @Override
            public int compare(Entry<String, Integer> o1,
                    Entry<String, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        return list;
    }

    private Map<String, Integer> initializeHash(Set<String> subjects) {
        ConcurrentHashMap<String, Integer> hm = new ConcurrentHashMap<>();
        for (String subject : subjects) {
            hm.put(subject, 0);
        }
        return hm;
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
