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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.endpoint.rdf.SPARQLEndpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.provider.rdf.SPARQLProvider;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
//import org.apache.marmotta.platform.core.exception.InvalidArgumentException;

import org.apache.marmotta.ucuenca.wk.authors.api.AuthorService;
import org.apache.marmotta.ucuenca.wk.authors.api.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.AskException;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.DaoException;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.UpdateException;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointService;
import org.apache.marmotta.ucuenca.wk.authors.api.SparqlEndpoint;

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
    private EndpointService authorsendpointService;

    private String wkhuskaGraph = "http://ucuenca.edu.ec/wkhuska";

    private int limit = 5000;

    @Override
    public String runAuthorsUpdateMultipleEP(String endpp, String graph) throws DaoException, UpdateException {
        Boolean someUpdate = false;
        StringBuilder response = new StringBuilder();
        if (authorsendpointService.listEndpoints().size() != 0) {
            for (SparqlEndpoint endpoint : authorsendpointService.listEndpoints()) {
                //       if (endpoint.isActive()) {
                response.append("\n ENDPOINT: ");
                response.append(endpoint.getName());
                response.append(":  ");
                try {
                    response.append(getAuthorsMultipleEP(endpoint));
                } catch (AskException ex) {
                    java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RepositoryException ex) {
                    log.error("Excepcion de repositorio. Problemas en conectarse a " + endpoint.getName());
                    java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedQueryException ex) {
                    log.error("Excepcion de forma de consulta. Revise consultas SPARQL y sintaxis. Revise estandar SPARQL");
                    java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    log.error("Excepcion de ejecucion de consulta. No se ha ejecutado la consulta general para la obtencion de los Authores.");
                    java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                someUpdate = true;
                //     }
            }
            if (!someUpdate) {
                return "Any  Endpoints";
            }
            return response.toString();
        } else {
            return "No Endpoints";
        }

    }

    @Override
    public String runAuthorsUpdateSingleEP(String endpointURL, String graph) throws DaoException, UpdateException {

        try {
            return "ENDPOINT: " + endpointURL + ":  " + getAuthorsSingleEP(endpointURL, "http://190.15.141.102:8080/dspace/");
        } catch (AskException ex) {
            java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getAuthorsMultipleEP(SparqlEndpoint endpoint) throws DaoException, UpdateException, AskException, RepositoryException, MalformedQueryException, QueryEvaluationException {

        int tripletasCargadas = 0; //cantidad de tripletas actualizadaas
        int contAutoresNuevosNoCargados = 0; //cantidad de actores nuevos no cargados
        int contAutoresNuevosEncontrados = 0; //hace referencia a la cantidad de actores existentes en el archivo temporal antes de la actualizacion
        configurationService.getHome();
        String lastUpdateUrisFile = configurationService.getHome() + "\\listAuthorsUpdate_" + endpoint.getName() + ".aut";
        /* Conecting to repository using LDC ( Linked Data Client ) Library */
        DataProvider spqprov = new SPARQLProvider();
        ClientConfiguration config = new ClientConfiguration();
        config.addEndpoint(new SPARQLEndpoint(endpoint.getName(), endpoint.getEndpointUrl(), "^http://190.15.141.102:8080/dspace/contribuidor/autor/.*"));
        config.addProvider(spqprov);
        LDClientService ldclient = new LDClient(config);
        Repository endpointTemp = new SPARQLRepository(endpoint.getEndpointUrl());
        endpointTemp.initialize();
        //After that you can use the endpoint like any other Sesame Repository, by creating a connection and doing queries on that:
        RepositoryConnection conn = endpointTemp.getConnection();
        String querytoCount = "";
        try {
            querytoCount = queriesService.getCountPersonQuery(endpoint.getGraph());
            TupleQueryResult countPerson = conn.prepareTupleQuery(QueryLanguage.SPARQL, querytoCount).evaluate();
            BindingSet bindingCount = countPerson.next();
            int numPersons = Integer.parseInt(bindingCount.getValue("count").stringValue());
            //Query that let me obtain all resource related with author from source sparqlendpoint 
            String getAuthorsQuery = queriesService.getAuthorsQuery(endpoint.getGraph());
            String resource = "";
            for (int offset = 0; offset < numPersons; offset += 5000) {
                TupleQueryResult authorsResult = conn.prepareTupleQuery(QueryLanguage.SPARQL, getAuthorsQuery + getLimitOffset(limit, offset)).evaluate();
                while (authorsResult.hasNext()) {
                    BindingSet binding = authorsResult.next();
                    resource = String.valueOf(binding.getValue("s"));
                    if (!sparqlFunctionsService.askAuthor(queriesService.getAskQuery(resource))) {
                        contAutoresNuevosEncontrados++;
                        //consultando propiedades y sus valores del author con LDClient Library de Marmotta
                        String getResourcePropertyQuery = "";
                        try {
                            ClientResponse respUri = ldclient.retrieveResource(utf8DecodeQuery(resource));
                            RepositoryConnection conUri = ModelCommons.asRepository(respUri.getData()).getConnection();
                            conUri.begin();
                            // SPARQL to get all data of a Resource
                            getResourcePropertyQuery = queriesService.getRetrieveResourceQuery();
                            TupleQuery resourcequery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getResourcePropertyQuery); //
                            TupleQueryResult tripletasResult = resourcequery.evaluate();
                            while (tripletasResult.hasNext()) {
                                //obtengo name, lastname, firstname, type, etc.,   para formar tripletas INSERT
                                BindingSet tripletsResource = tripletasResult.next();
                                String sujeto = tripletsResource.getValue("x").toString();
                                String predicado = tripletsResource.getValue("y").toString();
                                String objeto = tripletsResource.getValue("z").toString();
                                ///insert sparql query,
                                if (!predicado.contains("rdaregistry.info")) {
                                    String queryAuthorInsert = buildInsertQuery(sujeto, predicado, objeto);
                                    //load data related with author
                                    updateAuthor(queryAuthorInsert);
                                    tripletasCargadas++;
                                    //insert provenance triplet query
                                    String provenanceQueryInsert = buildInsertQuery(sujeto, queriesService.getProvenanceProperty(), endpoint.getResourceId());
                                    updateAuthor(provenanceQueryInsert);
                                }
                              }
                            conUri.commit();
                            conUri.close();
                        } catch (QueryEvaluationException ex) {
                            log.error("Fallo al intentar evaluar " + getResourcePropertyQuery);
                            //java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (DataRetrievalException ex) {
                            contAutoresNuevosNoCargados++;
                            log.error("Fallo al intentar recuperar: " + resource);
                            //java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }//END FOR   Obteniedo resultados de acuerdo a LIMIT y OFFSET
                /*    
             *    @deprecated
             *    ESCRIBIENDO URIS DE AUTORES EN ARCHIVO TEMPORAL
             *    @param conn, conection endpoint and configuration
             *    @param query, query to obtain all resource uris of authors
             *    @param lastUpdateUrisFile path of temporal file to save last uris update   */
            sparqlFunctionsService.updateLastAuthorsFile(conn, getAuthorsQuery, lastUpdateUrisFile);
            ldclient.shutdown();
            log.info(endpoint.getName() + " endpoint. Se detectaron" + contAutoresNuevosEncontrados + " autores nuevos ");
            log.info(endpoint.getName() + " endpoint. Se cargaron " + (contAutoresNuevosEncontrados - contAutoresNuevosNoCargados) + " autores nuevos exitosamente");
            log.info(endpoint.getName() + " endpoint. Se cargaron " + tripletasCargadas + " tripletas ");
            log.info(endpoint.getName() + " endpoint. No se pudieron cargar " + contAutoresNuevosNoCargados + " autores");
            return "Carga Finalizada. Revise Archivo Log Para mas detalles";
        } catch (QueryEvaluationException ex) {
            log.error("Error al intentar evaluar : " + querytoCount + " en " + endpoint.getName());
            return "Revise consulta para conteo de Datos de la fuente " + endpoint.getName();
//               java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            conn.close();
        }
    }

    /*
     * return 0 if no load triplet
     * return 1 if load
     */
    public int loadTriplet(String sujeto, String predicado, String objeto) {

        if (!predicado.contains("rdaregistry.info")) {
            String queryAuthorInsert = buildInsertQuery(sujeto, predicado, objeto);
            //Aqui se carga la informacion del actor nuevo en marmotta..
            updateAuthor(queryAuthorInsert);
            return 1;
        }

        return 0;
    }

    @Deprecated
    public String getAuthorsSingleEP(String endpointURL, String graphUri) throws DaoException, UpdateException, AskException {
//        try {
//
//            String lastUpdateUrisFile = "C:\\Users\\Satellite\\Desktop\\result.rdf";
//
//            /*         *Conecting to repository using LDC ( Linked Data Client ) Library
//             */
//            DataProvider spqprov = new SPARQLProvider();
//            ClientConfiguration config = new ClientConfiguration();
//            config.addEndpoint(new SPARQLEndpoint("Unique Endpoint", endpointURL, "^http://190.15.141.102:8080/dspace/contribuidor/autor/.*"));
//            config.addProvider(spqprov);
//            LDClientService ldclient = new LDClient(config);
//            Repository endpointTemp = new SPARQLRepository(endpointURL);
//            endpointTemp.initialize();
//            RepositoryConnection conn = endpointTemp.getConnection();           //After that you can use the endpoint like any other Sesame Repository, by creating a connection and doing queries on that:
//            try {
// //               List listURIS = null;
//                //               listURIS = getLastUpdateUris(lastUpdateUrisFile);                //load last uris saved and insert in marmotta in temporal file
//                /*
//                 * Consultando las uris de los recursos (Autores)  a la fuente ( dspace endpoint )
//                 * para comparar con LAST uris (Archivo donde se almacena todas las uris cargadas a marmotta anteriormente), es decir comparar con la ultima actualizacioin previa
//                 */
//                //Query that let me obtain all resource related with actor from source sparqlendpoint 
//
//                String query = queriesService.getAuthorsQuery("http://190.15.141.102:8080/dspace/");
//                String resource = "";
//
//                TupleQueryResult resulta = conn.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate();
//                while (resulta.hasNext()) {
//                    BindingSet binding = resulta.next();
//                    resource = String.valueOf(binding.getValue("o"));
//                    if (!sparqlFunctionsService.askAuthor(queriesService.getAskQuery(resource))) {
//                        // *****consult a Resource 
//                        //consultando informacion del author nuevo con LDClient Library de Marmotta
//                        //obtiene informacion relacionada de ese author con la URI pasada como parametro
//                        ClientResponse respUri = null;
//                        try {
//                            respUri = ldclient.retrieveResource(utf8DecodeQuery(resource));
//                        } catch (DataRetrievalException ex) {
//                            java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                        RepositoryConnection conUri = ModelCommons.asRepository(respUri.getData()).getConnection();
//                        conUri.begin();
//                        //En este momento ya se obtiene toda la informacion de ese Author
//                        //Esa informacion se la manipula con SPARQL
//
//                        // SPARQL to obtain all data of a Resource
//                        String sparql = "Select ?x ?y ?z where { ?x ?y ?z }";
//                        TupleQuery resourcequery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, sparql); //
//                        TupleQueryResult tripletasResult = resourcequery.evaluate();
//                        while (tripletasResult.hasNext()) {
//                            //La consulta realizada permite obtener todas las tripletas relacionadas con esa URI ( URI DE UN AUTOR )
//                            //obtengo name, lastname, firstname, type    de el recurso, para formar tripletas
//                            BindingSet tripletsResource = tripletasResult.next();
//                            String sujeto = tripletsResource.getValue("x").toString();
//                            String predicado = tripletsResource.getValue("y").toString();
//                            String objeto = tripletsResource.getValue("z").toString();
//                            ///insert sparql query, 
//                            String querytoUpdate = buildInsertQuery(sujeto, predicado, objeto);
//                            //Aqui se carga la informacion del actor nuevo en marmotta..
//                            // se considera a un autor como nuevo, si su respectiva uri no se encuentra en el archivo LastUpdateFile
//                            updateAuthor(querytoUpdate);
//                        }
//                        conUri.commit();
//                        conUri.close();
//                    }
//                }
//                /*    
//                 *    ESCRIBIENDO EN ARCHIVO TEMPORAL
//                 *    NUEVAMENTE TODOS LOS DATOS SON CONSULTADOS A LA FUENTE ( dspace endpoint )
//                 *    y almacenados en archivo temporal
//                 *    @param conn, conection endpoint and configuration
//                 *    @param query, query to obtain all resource uris of authors
//                 *    @param lastUpdateUrisFile path of temporal file to save last uris update
//                 */
//                sparqlFunctionsService.updateLastAuthorsFile(conn, query, lastUpdateUrisFile);
//                ldclient.shutdown();
//                return "Se actualizo correctamente";
//            } catch (InvalidArgumentException ex) {
//                java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//                return "faile 1" + ex;
//            } finally {
//                conn.close();
//            }
//        } catch (RepositoryException ex) {
//            java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//            return "faile 4" + ex;
//        } catch (MalformedQueryException ex) {
//            java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//            return "faile 5" + ex;
//        } catch (QueryEvaluationException ex) {
//            java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//            return "faile 6" + ex;
//        }
        return "";
    }

    public String getLimitOffset(int limit, int offset) {
        return " " + queriesService.getLimit(String.valueOf(limit)) + " " + queriesService.getOffset(String.valueOf(offset));
    }

    /*
     *   ASK - with SPARQL MODULE, to check if the resource already exists in kiwi triple store
     *   
     */
    public Boolean askAuthor(String querytoAsk) throws AskException {
        return sparqlFunctionsService.askAuthor(querytoAsk);
    }

    /*
     *   UPDATE - with SPARQL MODULE, to check if the resource already exists in kiwi triple store
     *   
     */
    public String updateAuthor(String querytoUpdate) {

        try {
            sparqlFunctionsService.updateAuthor(querytoUpdate);
            return "Correcto";
        } catch (UpdateException ex) {
            log.error("Error al intentar cargar al Autor" + querytoUpdate);
            java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);

        }
        return "Error" + querytoUpdate;
    }

    /*
     *   ASK Query, to check if the resource already exists in temporal file  *.aut
     *   
     */
    @Deprecated
    public Boolean existeAuthor(List listURIS, String acthorURIfromEndpoint) {
        String acthorURIfromLastUpdaFile = "";

        if (listURIS != null) {
            for (Iterator it = listURIS.iterator(); it.hasNext();) {
                acthorURIfromLastUpdaFile = it.next().toString();
                //    String individualURI = listURIS.listIterator().next().toString();
                if (acthorURIfromLastUpdaFile.equals(acthorURIfromEndpoint))//elemento existente
                {
                    return true;//la uri del author existe
                }
            }
        }
        return false; //no se ha enocntrado la uri del author, es un author nuevo
    }

    //construyendo sparql query insert 
    public String buildInsertQuery(String sujeto, String predicado, String objeto) {
        if (queriesService.isURI(objeto)) {
            return queriesService.getInsertDataUriQuery(wkhuskaGraph, sujeto, predicado, objeto);
        } else {
            return queriesService.getInsertDataLiteralQuery(wkhuskaGraph, sujeto, predicado, objeto);
        }
    }

    /**
     *
     * Funcion para leer las uris que se han actualizado por ultima vez previo a
     * la presente actualizacion. con el fin de determinar cuales uris de
     * autores son nuevas. las uris de autores nuevas sirven para obtener toda
     * la informacion relacionada con los autores y cargarlos a la plataforma
     *
     * @param csvFile
     * @return
     * @deprecated
     */
//  @Deprecated
    public List getLastUpdateUris(String csvFile) {
        BufferedReader br = null;
        String line = "";
        List listURIS = new ArrayList();
        try {
            File fichero = new File(csvFile);
            if (fichero.exists()) {
                br = new BufferedReader(new FileReader(csvFile));
                line = br.readLine();
                while (line != null) {
                    // use comma as separator
                    //log.info("Read:  " + line);
                    listURIS.add(line);
                    line = br.readLine();
                }
            } else {
                return null;
            }

            return listURIS;
        } catch (FileNotFoundException e) {
            log.info(e.toString());
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.info(e.toString());
                }
            }
        }

        return null;
    }

    /**
     * permite decodificar la uri formato UTF-8
     *
     * @param query
     * @return
     */
    private String utf8DecodeQuery(String query) {
        try {
            byte[] bytes = query.getBytes("UTF-8"); // Charset to encode into
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
