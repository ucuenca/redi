/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.authors.services;

import org.apache.marmotta.ucuenca.wk.authors.api.UTPLAuthorService;
import java.util.logging.Level;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.endpoint.rdf.SPARQLEndpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ucuenca.wk.authors.api.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.AskException;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.UpdateException;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
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
 * Default Implementation of {@link UTPLAuthorService} Fernando B. CEDIA
 */
@ApplicationScoped
public class UTPLAuthorServiceImpl implements UTPLAuthorService {

    @Inject
    private Logger log;

    @Inject
    private SparqlFunctionsService sparqlFunctionsService;

    @Inject
    private QueriesService queriesService;

    @Inject
    private CommonsServices commonsService;

    /**
     * authorDocumentProperty : http://rdaregistry.info/Elements/a/P50161 |
     * http://rdaregistry.info/Elements/a/P50195
     */
    private String authorDocumentProperty = "http://rdaregistry.info";

    @Override
    public String runAuthorsSplit(String sparqlEndpoint, String graphUri) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ClientConfiguration config = new ClientConfiguration();
        config.addEndpoint(new SPARQLEndpoint("UTPL", sparqlEndpoint, "^" + "http://" + ".*"));
        LDClientService ldClientEndpoint = new LDClient(config);
        Repository endpointTemp = new SPARQLRepository(sparqlEndpoint);
        endpointTemp.initialize();
        //After that you can use the endpoint like any other Sesame Repository, by creating a connection and doing queries on that:
        RepositoryConnection conn = endpointTemp.getConnection();
        String getSources = queriesService.getSourcesfromUniqueEndpoit(graphUri);
        TupleQueryResult sourcesResult = conn.prepareTupleQuery(QueryLanguage.SPARQL, getSources).evaluate();
        while (sourcesResult.hasNext()) {
            BindingSet binding = sourcesResult.next();
            String dataset = String.valueOf(binding.getValue("dataset"));
            String nameu = String.valueOf(binding.getValue("nameu")).replace(" ", "");
            String targetgraph = getGraphName(nameu);
            log.info(dataset);
            try {
                String getDocumentsAuthorsQuery = queriesService.getDocumentsAuthors(dataset, graphUri);
                TupleQueryResult documentsAuthorsResult = conn.prepareTupleQuery(QueryLanguage.SPARQL, getDocumentsAuthorsQuery).evaluate();
                while (documentsAuthorsResult.hasNext()) {
                    BindingSet bindingdocuments = documentsAuthorsResult.next();
                    String document = String.valueOf(bindingdocuments.getValue("document"));
                    String author = String.valueOf(bindingdocuments.getValue("author"));
                    if (!sparqlFunctionsService.askAuthor(queriesService.getAskResourceQuery(targetgraph, author))) {
                        try {
                            // Getting Author Data
                            ClientResponse respUri = ldClientEndpoint.retrieveResource(author);
                            RepositoryConnection conUri = ModelCommons.asRepository(respUri.getData()).getConnection();
                            conUri.begin();
                            // SPARQL to get all data of a Resource
                            String getRetrieveResourceQuery = queriesService.getRetrieveResourceQuery();
                            TupleQuery resourcequery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getRetrieveResourceQuery); //
                            TupleQueryResult tripletasResult = resourcequery.evaluate();
                            while (tripletasResult.hasNext()) {
                                //obtengo name, lastname, firstname, a foaf, dct:subject, type, etc.,   para formar tripletas INSERT
                                BindingSet tripletsResource = tripletasResult.next();
                                String sujeto = tripletsResource.getValue("x").toString();
                                String predicado = tripletsResource.getValue("y").toString();
                                String objeto = tripletsResource.getValue("z").toString();
                                ///insert data,
                                predicado = predicado.replace("givenName", "firstName");
                                predicado = predicado.replace("familyName", "lastName");
                                String queryAuthorInsert = buildInsertQuery(targetgraph, sujeto, predicado, objeto);
                                updateAuthor(queryAuthorInsert);
                            }
                            conUri.commit();
                            conUri.close();
                            // Getting Documents Data
                            respUri = ldClientEndpoint.retrieveResource(document);
                            conUri = ModelCommons.asRepository(respUri.getData()).getConnection();
                            conUri.begin();
                            // SPARQL to get all data of a Resource
                            getRetrieveResourceQuery = queriesService.getRetrieveResourceQuery();
                            TupleQuery documentquery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getRetrieveResourceQuery); //
                            TupleQueryResult tripletasDocumentResult = documentquery.evaluate();
                            while (tripletasDocumentResult.hasNext()) {
                                //obtengo name, lastname, firstname, a foaf, dct:subject, type, etc.,   para formar tripletas INSERT
                                BindingSet tripletsResource = tripletasDocumentResult.next();
                                String sujeto = tripletsResource.getValue("x").toString();
                                String predicado = tripletsResource.getValue("y").toString();
                                String objeto = tripletsResource.getValue("z").toString();
                                ///insert data,
                                predicado = predicado.replace("http://vivoweb.org/ontology/core#freetextKeyword", "http://purl.org/dc/terms/subject");
                                String queryAuthorInsert = buildInsertQuery(targetgraph, sujeto, predicado, objeto);
                                updateAuthor(queryAuthorInsert);
                            }
                            conUri.commit();
                            conUri.close();
                        } catch (QueryEvaluationException | RepositoryException | DataRetrievalException ex) {
                            log.error("Al evaluar la consulta de documentos" + author);
                        }
                        /**
                         * Insert Property between Author and Document
                         * <http://rdaregistry.info/Elements/a/P50161>
                         */
                        String queryAuthorInsert = buildInsertQuery(targetgraph, author, authorDocumentProperty, document);
                        updateAuthor(queryAuthorInsert);
                    }//end if
                }
            } catch (QueryEvaluationException | RepositoryException | AskException ex) {
                log.error("Al evaluar la consulta de getDocumentsAuthorsQuery");
            }
        }
        return "Finish: ok!";
    }

    private String getGraphName(String nameu) {
        String namesource = nameu.replace("@es", "");
        String namegraph = nameu.substring(1, namesource.length() - 1);
        return "http://data.utpl.edu.ec/" + commonsService.removeAccents(namegraph);
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

    /**
     * building sparql query insert
     *
     */
    public String buildInsertQuery(String... args) {
        String graph = args[0];
        String sujeto = args[1];
        String predicado = args[2];
        String objeto = args[3];
        if (commonsService.isURI(objeto)) {
            return queriesService.getInsertDataUriQuery(graph, sujeto, predicado, objeto);
        } else {
            return queriesService.getInsertDataLiteralQuery(graph, sujeto, predicado, objeto);
        }
    }
}
