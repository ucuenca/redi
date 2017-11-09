/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import javax.inject.Inject;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.pubman.exceptions.PubException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;

/**
 *
 * @author Fernando Baculima
 */
public class SparqlFunctionsServiceImpl implements SparqlFunctionsService {

    @Inject
    private org.slf4j.Logger log;
    @Inject
    private CommonsServices commonsServices;
    @Inject
    private SparqlService sparqlService;
    @Inject
    private QueriesService queriesService;

    @Override
    public boolean updatePub(String querytoUpdate) throws PubException {
        try {

            /**
             * El siguiente codigo documentado puede ser habilitado para CARGAR
             * datos en otro sparql endpoint
             *
             * this.connection = endpointUpdate.getConnection();
             * this.connection.begin(); Update update =
             * this.connection.prepareUpdate(QueryLanguage.SPARQL,querytoUpdate);
             * update.execute(); this.connection.commit();
             */
            sparqlService.update(QueryLanguage.SPARQL, querytoUpdate);
            return true;
        } catch (InvalidArgumentException | MarmottaException | UpdateExecutionException | MalformedQueryException ex) {
            log.error("Fail to Insert Triplet: " + querytoUpdate);
            return false;
        }
    }

    @Override
    public boolean executeInsert(String graph, String sujeto, String predicado, String objeto) {
        return executeInsert(graph, sujeto, predicado, objeto, null);
    }

    @Override
    public boolean executeInsert(String graph, String sujeto, String predicado, String objeto, String datatype) {
        String query;
        if (commonsServices.isURI(objeto)) {
            query = queriesService.getInsertDataUriQuery(graph, sujeto, predicado, objeto);
        } else {
            query = queriesService.getInsertDataLiteralQuery(graph, sujeto, predicado, objeto, datatype);
        }

        try {
            sparqlService.update(QueryLanguage.SPARQL, query);
            return true;
        } catch (InvalidArgumentException | MarmottaException | MalformedQueryException | UpdateExecutionException ex) {
            log.error("Cannot execute query \n" + query, ex);
            return false;
        }
    }
}
