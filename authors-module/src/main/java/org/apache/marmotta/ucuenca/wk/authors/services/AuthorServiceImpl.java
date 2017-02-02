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

//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.endpoint.rdf.SPARQLEndpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.ucuenca.wk.authors.api.AuthorService;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointService;
import org.apache.marmotta.ucuenca.wk.authors.api.SparqlEndpoint;
import org.apache.marmotta.ucuenca.wk.authors.api.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.AskException;
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

    @Inject
    private ConfigurationService configurationService;

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

    private int limit = 5000;

    private int processpercent = 0;

    //private boolean provenanceinsert = false; //variable to know if the provenance of an author was already inserted
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
        List<SparqlEndpoint> endpoints = authorsendpointService.listEndpoints();
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
        //configurationService.getHome();
        String lastUpdateUrisFile = configurationService.getHome() + "\\listAuthorsUpdate_" + endpoint.getName() + ".aut";
        /* Conecting to repository using LDC ( Linked Data Client ) Library */
        ClientConfiguration config = new ClientConfiguration();
        config.addEndpoint(new SPARQLEndpoint(endpoint.getName(), endpoint.getEndpointUrl(), "^" + "http://" + ".*"));
        LDClientService ldClientEndpoint = new LDClient(config);

        Repository endpointTemp = new SPARQLRepository(endpoint.getEndpointUrl());
        endpointTemp.initialize();
        //After that you can use the endpoint like any other Sesame Repository, by creating a connection and doing queries on that:
        RepositoryConnection conn = endpointTemp.getConnection();
        int authorsSize = getAuthorsSize(conn, endpoint.getGraph());//Integer.parseInt(bindingCount.getValue("count").stringValue());
        //Query that let me obtain all resource related with author from source sparqlendpoint 
        String getAuthorsQuery = queriesService.getAuthorsQuery(endpoint.getGraph());
        String resource = "";
        for (int offset = 0; offset < authorsSize; offset += 5000) {
            try {
                TupleQueryResult authorsResult = conn.prepareTupleQuery(QueryLanguage.SPARQL, getAuthorsQuery + getLimitOffset(limit, offset)).evaluate();
                while (authorsResult.hasNext()) {
                    resource = authorsResult.next().getValue("s").stringValue();
                    try {
                        if (!sparqlFunctionsService.askAuthor(queriesService.getAskResourceQuery(constantService.getAuthorsGraph(), resource))) {
                            contAutoresNuevosEncontrados++;
                            printPercentProcess(contAutoresNuevosEncontrados, authorsSize, endpoint.getName());
                            String localResource = buildLocalURI(resource);
                            List<String> documents = new ArrayList<>();
                            Set<String> subjects = new HashSet<>();

                            //properties and values quering with LDClient Library de Marmotta
                            String getResourcePropertyQuery = "";
                            try {
                                //ClientResponse respUri = ldClientEndpoint.retrieveResource(utf8DecodeQuery(resource));
                                ClientResponse respUri = ldClientEndpoint.retrieveResource(resource);
                                RepositoryConnection conUri = ModelCommons.asRepository(respUri.getData()).getConnection();
                                conUri.begin();
                                // SPARQL to get all data of a Resource
                                getResourcePropertyQuery = queriesService.getRetrieveResourceQuery();
                                TupleQuery resourcequery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getResourcePropertyQuery); //
                                TupleQueryResult tripletasResult = resourcequery.evaluate();
//                                provenanceinsert = false;
                                while (tripletasResult.hasNext()) {
                                    BindingSet tripletsResource = tripletasResult.next();
                                    String subject = tripletsResource.getValue("x").stringValue();
                                    String predicate = tripletsResource.getValue("y").stringValue();
                                    String object = tripletsResource.getValue("z").stringValue();

                                    if (predicate.contains("http://rdaregistry.info")) {
                                        subjects.addAll(extractSubjectsFromDocument(ldClientEndpoint, object));
                                        documents.addAll(extractDocument(ldClientEndpoint, object));
                                        continue;
                                    }
                                    String queryAuthorInsert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, predicate, object);
                                    sparqlFunctionsService.updateAuthor(queryAuthorInsert);
                                    if (!tripletasResult.hasNext()) {
                                        String sameAs = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, OWL.SAMEAS.toString(), subject);
                                        sparqlFunctionsService.updateAuthor(sameAs);
                                    }
                                }
                                // Insert provenance 
                                String provenanceQueryInsert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, "http://purl.org/dc/terms/provenance", endpoint.getResourceId());
                                sparqlFunctionsService.updateAuthor(provenanceQueryInsert);
                                combineSubjects(localResource, documents, subjects);
                                // insert subjects
                                conUri.commit();
                                conUri.close();
                            } catch (QueryEvaluationException ex) {
                                log.error("Al evaluar la consulta: " + getResourcePropertyQuery);
                            } catch (DataRetrievalException ex) {
                                contAutoresNuevosNoCargados++;
                                //log.error("Al recuperar datos del recurso : " + resource);
                            }
                        }//end  if (!sparqlFunctionsService.askAuthor( ... )
                    } catch (AskException ex) {
                        log.error("Failure to ask existence of: " + resource);
                    }
                }
            } catch (QueryEvaluationException ex) {
                log.error("Fallo consulta ASK de:  " + resource + "error" + ex.getMessage());
            } finally {
                endpointTemp.shutDown();
            }

        }
        /*    
         *    @deprecated
         *    ESCRIBIENDO URIS DE AUTORES EN ARCHIVO TEMPORAL
         *    @param conn, conection endpoint and configuration
         *    @param query, query to obtain all resource uris of authors
         *    @param lastUpdateUrisFile path of temporal file to save last uris update   */
        sparqlFunctionsService.updateLastAuthorsFile(conn, getAuthorsQuery, lastUpdateUrisFile);
        ldClientEndpoint.shutdown();
        log.info(endpoint.getName() + " endpoint. Se detectaron " + contAutoresNuevosEncontrados + " autores nuevos ");
        log.info(endpoint.getName() + " endpoint. Se cargaron " + (contAutoresNuevosEncontrados - contAutoresNuevosNoCargados) + " autores nuevos exitosamente");
        log.info(endpoint.getName() + " endpoint. Se cargaron " + tripletasCargadas + " tripletas ");
        log.info(endpoint.getName() + " endpoint. No se pudieron cargar " + contAutoresNuevosNoCargados + " autores");
        conn.close();
        return "Carga Finalizada. Revise Archivo Log Para mas detalles";
    }

    public List<String> extractDocument(LDClientService ldClient, String documentURI) throws DataRetrievalException, QueryEvaluationException, RepositoryException, MalformedQueryException, IOException, ClassNotFoundException {

        ClientResponse respPub = ldClient.retrieveResource(documentURI);
        RepositoryConnection conUriPub = ModelCommons.asRepository(respPub.getData()).getConnection();
        conUriPub.begin();
        List<String> documents = new ArrayList<>();

        String getAbstractAndTitleQuery = queriesService.getAbstractAndTitleQuery(documentURI);
        TupleQuery abstractTitlequery = conUriPub.prepareTupleQuery(QueryLanguage.SPARQL, getAbstractAndTitleQuery); //
        TupleQueryResult tripletasATResult = abstractTitlequery.evaluate();
        while (tripletasATResult.hasNext()) {
            BindingSet tripletsATResource = tripletasATResult.next();
            StringBuilder document = new StringBuilder();
            for (Binding binding : tripletsATResource) {
                document.append(binding.getValue().stringValue());
            }

            documents.add(document.toString());
        }
        conUriPub.commit();
        conUriPub.close();
        return documents;
    }

    private Set<String> extractSubjectsFromDocument(LDClientService ldClient, String documentURI)
            throws DataRetrievalException, RepositoryException, MalformedQueryException, QueryEvaluationException, Exception {
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
            String keyword = tripletskeysResource.getValue("subject").stringValue();
            subjects.add(keyword);
        }
        conUriPub.commit();
        conUriPub.close();
        return subjects;
    }

    private String buildLocalURI(String endpointURI) {
        return constantService.getAuthorResource() + endpointURI.substring(endpointURI.lastIndexOf('/') + 1);
    }


    /*
     * 
     * @param contAutoresNuevosEncontrados
     * @param allPersons
     * @param endpointName 
     */
    public void printPercentProcess(int contAutoresNuevosEncontrados, int allPersons, String endpointName) {

        if ((contAutoresNuevosEncontrados * 100 / allPersons) != processpercent) {
            processpercent = contAutoresNuevosEncontrados * 100 / allPersons;
            log.info("Procesado el: " + processpercent + " % del Endpoint: " + endpointName);
        }
    }

    public String getLimitOffset(int limit, int offset) {
        return " " + queriesService.getLimit(String.valueOf(limit)) + " " + queriesService.getOffset(String.valueOf(offset));
    }

    private int getAuthorsSize(RepositoryConnection conn, String graph) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        String querytoCount = queriesService.getCountPersonQuery(graph);
        TupleQueryResult countPerson = conn.prepareTupleQuery(QueryLanguage.SPARQL, querytoCount).evaluate();
        BindingSet bindingCount = countPerson.next();
        return Integer.parseInt(bindingCount.getValue("count").stringValue());

    }

    private void combineSubjects(String localSubject, List<String> documents, Set<String> subjects) throws UpdateException {
        // TODO: comnbine repository subjects with documents subjects and store a # x as subject
        for (String keyword : subjects) {
            if ((!commonsService.isURI(keyword)) && (kservice.isValidKeyword(keyword))) {
                String insertKeywords = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localSubject, DCTERMS.SUBJECT.toString(), kservice.cleaningText(keyword).toUpperCase());
                sparqlFunctionsService.updateAuthor(insertKeywords);
            }
        }
        log.info("Resource %s has %d documents", localSubject, documents.size());
    }

}
