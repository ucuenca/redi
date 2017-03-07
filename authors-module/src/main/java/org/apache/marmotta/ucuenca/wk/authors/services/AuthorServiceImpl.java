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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
import org.apache.marmotta.ucuenca.wk.commons.service.DistanceService;
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
    private DistanceService distanceService;

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

    private static int upperLimitKey = 5;
    
    private PrintWriter out;
    
    private static double tolerance = 0.9;
    
    private Set<String> setExplored = new HashSet<String>();
    
    @PostConstruct
    public void init() {
        endpoints = authorsendpointService.listEndpoints();
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
        endpoints = authorsendpointService.listEndpoints();
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
            response.append(searchDuplicates());

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
            Repository repository = new SPARQLRepository("http://localhost:8080/sparql/select");
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
    
    private String searchDuplicates() {
        try {
            String allAuthorsQuery = queriesService.getAuthors();
            Repository repository = new SPARQLRepository("http://localhost:8080/sparql/select");
            TupleQueryResult allAuthors = executeQuery(repository, allAuthorsQuery);
            
            out = new PrintWriter("ListAuthorsPubs.txt");
            int authorCount = 0;
            while (allAuthors.hasNext()) {
                String authorResource = allAuthors.next().getBinding("s").getValue().stringValue();
                authorCount++;
                
                out.println(" Author Number: " + authorCount);
                
                //Encontramos los nombres del autor actual
                String getNamesQuery = queriesService.getAuthorDataQuery(constantService.getAuthorsGraph(), authorResource);
                TupleQueryResult namesAuthor = executeQuery(repository, getNamesQuery);
                String firstName = "";
                String lastName = "";
                //String fullName = "";
                if (namesAuthor.hasNext()) {
                    BindingSet next = namesAuthor.next();
                    firstName = next.getBinding("fname").getValue().stringValue();
                    lastName = next.getBinding("lname").getValue().stringValue();
                    //fullName = namesAuthor.next().getBinding("name").getValue().stringValue();
                }
                //guardar en la variable sameAuthors los autores que ya tienen sameAs;
                Set<String> sameAuthors = new HashSet<String>();
                String sameAsAuthorsQuery = queriesService.getSameAsAuthors(authorResource);
                TupleQueryResult sameAsAuthors = executeQuery(repository, sameAsAuthorsQuery);
                while (sameAsAuthors.hasNext()) { // for each author
                    String sameAsResource = sameAsAuthors.next().getBinding("o").getValue().stringValue();
                    sameAuthors.add(sameAsResource);
                    
                }
                //Encontramos los que pueden ser iguales
                sameAuthors = findSameAuthor(repository, sameAuthors, authorResource, firstName, lastName);
                
                //Agrego la propiedad sameAs a cada uno de los autores identificados
                for (String sameAuthorResource : sameAuthors) {
                    if (!authorResource.equals(sameAuthorResource)) {
                        try {
                            String sameAs = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), authorResource, OWL.SAMEAS.toString(), sameAuthorResource);
                            sparqlFunctionsService.updateAuthor(sameAs);
                        } catch (UpdateException ex) {
                            log.error("Cannot insert sameAs property for: <" + authorResource + "> and <" + sameAuthorResource + ">. Error: {}", ex.getMessage());
                        }
                    }
                }
            }
            repository.shutDown();
            out.close();
            log.info("Finished to search for duplicate authors DSpace.");
        } catch (QueryEvaluationException | RepositoryException | MalformedQueryException ex) {
            log.error("Cannot search for duplicate authors DSpace. Error: {}", ex);
            return ex.getMessage();
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Duplicate authors searched";
    }
    
    private Set<String> findSameAuthor(Repository repository, Set<String> setResult, String authorResource, String nombres, String apellidos) {
        
        setExplored = new HashSet<String>();
            
        String givenName = cleaningTextAuthor(nombres);
        String lastName = cleaningTextAuthor(apellidos);

        //Getting the names
        String givenName1 = givenName.split(" ")[0];
        String givenName2 = null;
        int one = 1;
        int numberGivenNames = givenName.split(" ").length;
        if (numberGivenNames > one) {
            givenName2 = givenName.split(" ")[1];
        }

        String lastName1 = lastName.split(" ")[0];
        /*String lastName2 = null;
        int numberLastNames = lastName.split(" ").length;
        if (numberLastNames > one) {
            lastName2 = lastName.split(" ")[1];
        }*/

        // 1. Busca 4 nombres sin acentos
        setResult.addAll(searchSameAuthor(setResult, repository, authorResource, nombres, apellidos, 
        givenName, lastName, false));

        // 2. primer nombre y apellidos
        setResult.addAll(searchSameAuthor(setResult, repository, authorResource, nombres, apellidos, 
            givenName1, lastName, true));

        // 3. segundo nombre y apellidos
        if (givenName2 != null && !givenName2.trim().isEmpty()) {
            setResult.addAll(searchSameAuthor(setResult, repository, authorResource, nombres, apellidos, 
            givenName2, lastName, false));

            // 5. segundo nombre y primer apellido (si hay mas de un nombre)
            setResult.addAll(searchSameAuthor(setResult, repository, authorResource, nombres, apellidos,
                    givenName2, lastName1, true));
            
            // 8. segunda inicial y apellidos (si hay mas de un nombre)
            String inicial = "" + givenName2.trim().charAt(0);
            setResult.addAll(searchSameAuthor(setResult, repository, authorResource, inicial, apellidos,
                    inicial, lastName, true));

            // 9. segunda inicial y primer apellido (si hay mas de un apellido)
            setResult.addAll(searchSameAuthor(setResult, repository, authorResource, inicial, apellidos,
                    inicial, lastName1, true));
            
        }

        // 4. primer nombre y primer apellido (si hay más de un nombre y un apellido)
        setResult.addAll(searchSameAuthor(setResult, repository, authorResource, nombres, apellidos,
            givenName1, lastName1, true));

        // 6. primera inicial y apellidos (si hay mas de un nombre y el primer nombre no es inicial solamente)
        if (givenName1 != null && !givenName1.trim().isEmpty()) {
            String inicial = "" + givenName1.trim().charAt(0);
            if (!inicial.equals(givenName1)) {
                setResult.addAll(searchSameAuthor(setResult, repository, authorResource, inicial, apellidos,
                        inicial, lastName, true));
            }

            // 7. primera inicial y primer apellido (si hay más de un apellido y el nombre no era solo inicial)
            setResult.addAll(searchSameAuthor(setResult, repository, authorResource, inicial, apellidos,
                    inicial, lastName1, true));
        }

        return setResult;
        
    } 
    
    private Set<String> searchSameAuthor(Set<String> setResult, Repository repository, String authorResource, String nombresOrig, String apellidosOrig, 
            String givenName, String lastName, boolean semanticCheck) {

        try {
            String similarAuthorResource = "";
            String otherGivenName = ""; 
            String otherLastName = "";
            String queryNames = queriesService.getAuthorsByName(
                    constantService.getAuthorsGraph(), "^" + givenName + "$", "^" + lastName + ".*$");
            TupleQueryResult similarAuthors = executeQuery(repository, queryNames);

            while (similarAuthors.hasNext()) {
                BindingSet next = similarAuthors.next();
                similarAuthorResource = next.getBinding("subject").getValue().stringValue();
                otherGivenName = next.getBinding("firstName").getValue().stringValue();
                otherLastName = next.getBinding("lastName").getValue().stringValue();
                
                boolean equalNames = false;
                if (!setResult.contains(similarAuthorResource) && !authorResource.equals(similarAuthorResource) 
                        && !setExplored.contains(similarAuthorResource)) {
                    out.println(" ");
                    equalNames = getEqualNames(authorResource, similarAuthorResource, nombresOrig, apellidosOrig, otherGivenName, otherLastName, semanticCheck, repository);
                    if (equalNames && semanticCheck) {
                        
                        out.println("URI: " + authorResource + " URI2: " + similarAuthorResource);
                        out.println("Nombres originales:   " + nombresOrig);
                        out.println("Apellidos originales: " + apellidosOrig);
                        out.println("Nombres nuevos 2:     " + otherGivenName);
                        out.println("Apellidos nuevos 2:   " + otherLastName);
                        out.println("Sintactic equal?: " + equalNames);
                        
                        equalNames = semanticCheck(authorResource, similarAuthorResource, repository);
                        
                        out.println("Semantic check?: " + semanticCheck);
        
                        out.println("Semantic check Result: " + equalNames);
                        out.println(" ");
                    }
                    if (equalNames) {
                        setResult.add(similarAuthorResource);
                    }
                    setExplored.add(similarAuthorResource);
                }
                
            }
            return setResult;
        } catch (QueryEvaluationException | RepositoryException | MalformedQueryException ex) {
            log.error("Cannot find similar authors for duplicate authors DSpace. Error: {}", ex);
        }

        return setResult;
    }
    
    public boolean getEqualNames(String authorResource, String similarAuthorResource, String nombresOrig, String apellidosOrig, String otherGivenName, String otherLastName, boolean semanticCheck, Repository repository){
        boolean equal = false;
        int one = 1;
        //Getting the original names
        String givenName1 = removeAccents(nombresOrig.split(" ")[0]).toLowerCase();
        String givenName2 = null;
        int numberGivenNames = nombresOrig.split(" ").length;
        if (numberGivenNames > one) {
            givenName2 = removeAccents(nombresOrig.split(" ")[1]).toLowerCase();
        }

        String lastName1 = removeAccents(apellidosOrig.split(" ")[0]).toLowerCase();
        String lastName2 = null;
        int numberLastNames = apellidosOrig.split(" ").length;
        if (numberLastNames > one) {
            lastName2 = removeAccents(apellidosOrig.split(" ")[1]).toLowerCase();
        }
        
        //Getting the other names
        String otherGivenName1 = removeAccents(otherGivenName.split(" ")[0]).toLowerCase();
        String otherGivenName2 = null;
        if (otherGivenName.split(" ").length > one) {
            otherGivenName2 = removeAccents(otherGivenName.split(" ")[1]).toLowerCase();
        }

        String otherLastName1 = removeAccents(otherLastName.split(" ")[0]).toLowerCase();
        String otherLastName2 = null;
        if (otherLastName.split(" ").length > one) {
            otherLastName2 = removeAccents(otherLastName.split(" ")[1]).toLowerCase();
        }
        
        if (lastName2!=null && lastName2.length() == one && otherLastName2!=null && otherLastName2.length() >= one) {
            otherLastName2 = otherLastName2.substring(0, 1);
        }
        
        //Compare given names and surnames
        equal = compareNames(givenName1, givenName2, lastName1, lastName2, 
                otherGivenName1, otherGivenName2, otherLastName1, otherLastName2);
        
        // 1. Busca 4 nombres sin acentos
        // 2. primer nombre y apellidos
        // 3. segundo nombre y apellidos
        // 5. segundo nombre y primer apellido (si hay mas de un nombre)
        // 4. primer nombre y primer apellido (si hay más de un nombre y un apellido)
        // 6. primera inicial y apellidos (si hay mas de un nombre y el primer nombre no es inicial solamente)
        // 7. primera inicial y primer apellido (si hay más de un apellido y el nombre no era solo inicial)
        // 8. segunda inicial y apellidos (si hay mas de un nombre)
        // 9. segunda inicial y primer apellido (si hay mas de un apellido)
                
        return equal;
        
    }
    
    public boolean compareNames(String givenName1, String givenName2, String lastName1, String lastName2, 
            String otherGivenName1, String otherGivenName2, String otherLastName1, String otherLastName2) {
        boolean result = false;
        
        if (givenName2 != null  && lastName2 != null) {
            
            if (otherGivenName2 != null && otherLastName2 != null) {
                if (compareExactStrings(givenName1, otherGivenName1) && compareExactStrings(givenName2, otherGivenName2)
                        && compareExactStrings(lastName1, otherLastName1) && compareExactStrings(lastName2, otherLastName2)) {
                    return true;
                }
            } else if (otherGivenName2 != null && otherLastName2 == null) {
                if (compareExactStrings(givenName1, otherGivenName1) && compareExactStrings(givenName2, otherGivenName2)
                        && compareExactStrings(lastName1, otherLastName1)) {
                    return true;
                }
            } else if (otherGivenName2 == null && otherLastName2 != null) {
                if ((compareExactStrings(otherGivenName1, givenName1) || compareExactStrings(otherGivenName1, givenName2))
                        && compareExactStrings(lastName1, otherLastName1) && compareExactStrings(lastName2, otherLastName2)) {
                    return true;
                }
            } else if (otherGivenName2 == null && otherLastName2 == null
                    && (compareExactStrings(otherGivenName1, givenName1) || compareExactStrings(otherGivenName1, givenName2))
                    && compareExactStrings(lastName1, otherLastName1)) {
                    return true;
                
            }

        } else if (givenName2 == null && lastName2 != null) {
            if (otherGivenName2 != null && otherLastName2 != null) {
                if ((compareExactStrings(givenName1, otherGivenName1) || compareExactStrings(givenName1, otherGivenName2))
                        && compareExactStrings(lastName1, otherLastName1) && compareExactStrings(lastName2, otherLastName2)) {
                    return true;
                }
            } else if (otherGivenName2 != null && otherLastName2 == null) {
                if ((compareExactStrings(givenName1, otherGivenName1) || compareExactStrings(givenName1, otherGivenName2))
                        && compareExactStrings(lastName1, otherLastName1)) {
                    return true;
                }
            } else if (otherGivenName2 == null && otherLastName2 != null) {
                if (compareExactStrings(otherGivenName1, givenName1)
                        && compareExactStrings(lastName1, otherLastName1) && compareExactStrings(lastName2, otherLastName2) 
                   ) {
                    return true;

                }
            } else if (otherGivenName2 == null && otherLastName2 == null &&
                    compareExactStrings(otherGivenName1, givenName1) && compareExactStrings(lastName1, otherLastName1)) {
                    return true;
                
            }

        } else if (givenName2 != null && lastName2 == null) {
            if (otherGivenName2 != null) {
                if (compareExactStrings(givenName1, otherGivenName1) && compareExactStrings(givenName2, otherGivenName2)
                        && compareExactStrings(lastName1, otherLastName1)) {
                    return true;
                }
            } else if (otherGivenName2 == null && 
                    (compareExactStrings(otherGivenName1, givenName1) || compareExactStrings(otherGivenName1, givenName2))
                        && compareExactStrings(lastName1, otherLastName1)) {
                    return true;
                
            }

        } else if (givenName2 == null && lastName2 == null) {
            if (otherGivenName2 != null) {
                if ((compareExactStrings(givenName1, otherGivenName1) || compareExactStrings(givenName1, otherGivenName2))
                        && compareExactStrings(lastName1, otherLastName1)) {
                    return true;
                }
            } else if (otherGivenName2 == null && compareExactStrings(otherGivenName1, givenName1) 
                    && compareExactStrings(lastName1, otherLastName1)) {
                    return true;
                
            }

        }
        return result;
    }
    
    public boolean semanticCheck(String authorResource, String similarAuthorResource, Repository repository) {
        boolean result = false;
        try {
            double coefficient = 1.1;
            
            String getQueryKeys1 = queriesService.getAuthorsKeywordsQuery(authorResource);
            TupleQueryResult keywords1 = executeQuery(repository, getQueryKeys1);
            String getQueryKeys2 = queriesService.getAuthorsKeywordsQuery(similarAuthorResource);
            TupleQueryResult keywords2 = executeQuery(repository, getQueryKeys2);
            
            int cont = 0;
            List<String> keywordsAuthor1 = new ArrayList<>();
            while (keywords1.hasNext() && cont <= upperLimitKey) {
                BindingSet bindingKey = keywords1.next();
                String keyword = String.valueOf(bindingKey.getValue("keyword")).replace("\"", "").replace("^^", "").split("<")[0].trim();
                if (!keywordsAuthor1.contains(keyword)) {
                    keywordsAuthor1.add(keyword);
                    cont++;
                }
            }
            
            cont = 0;
            List<String> keywordsAuthor2 = new ArrayList<>();
            while (keywords2.hasNext() && cont <= upperLimitKey) {
                BindingSet bindingKey = keywords2.next();
                String keyword = String.valueOf(bindingKey.getValue("keyword")).replace("\"", "").replace("^^", "").split("<")[0].trim();
                if (!keywordsAuthor2.contains(keyword)) {
                    keywordsAuthor2.add(keyword);
                    cont++;
                }
            }
            int lowerLimit = upperLimitKey - 2;
            if (!keywordsAuthor1.isEmpty() && !keywordsAuthor2.isEmpty() && keywordsAuthor1.size() >= lowerLimit 
                    && keywordsAuthor2.size() >= lowerLimit) {
                coefficient = distanceService.semanticComparisonValue(keywordsAuthor1, keywordsAuthor2);
                
                out.println("Keywords Author 1: " + keywordsAuthor1.toString());
                out.println("Keywords Author 2: " + keywordsAuthor2.toString());
                out.println("Distance: " + coefficient);
                out.println(" ");
                
                if (coefficient < tolerance) {
                    result = true;
                } else {
                    result = false;}
            } else {
                out.println("Not enough keywords to compare.");
            }
            
        } catch (QueryEvaluationException | RepositoryException | MalformedQueryException ex) {
            log.error("Cannot find similar authors for duplicate authors DSpace. Error: {}", ex);
        }
        return result;
    }
    
    public boolean compareExactStrings(String string1, String string2) {
        return (string1.matches("^" + string2 + "$") || string2.matches("^" + string1 + "$"));
    }

    public String cleaningTextAuthor(String value) {
        value = value.replace("??", ".*");
        value = value.replace("?", ".*");
        // Cadena de caracteres original a sustituir.
        String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
        String output = value;
        for (int i = 0; i < original.length(); i++) {
            // Reemplazamos los caracteres especiales.
            output = output.replace("" + original.charAt(i), ".*");
        }//end for i
        return output;
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

        BufferedReader input = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("helpers/stoplist.txt")));
        LineIterator it = new LineIterator(input);
        String line;
        ArrayList<String> stopwords = new ArrayList<>();
        while (it.hasNext()) {
            line = it.nextLine();
            String[] words = line.split("\\s+");
            stopwords.addAll(Arrays.asList(words));
        }
        it.close();

        //File stoplist = new File(getClass().getClassLoader().getResource("/helpers/stoplist.txt"));
        ArrayIterator iterator = new ArrayIterator(documents);

        ArrayList<Pipe> workflow = new ArrayList<>();
        workflow.add(new CharSequence2TokenSequence("\\p{L}+"));
        workflow.add(new TokenSequenceLowercase());
        //workflow.add(new TokenSequenceRemoveStopwords(stoplist, "UTF-8", false, false, false).addStopWords(stopwords));
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
