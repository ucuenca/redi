/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.authors.services;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.apache.marmotta.ucuenca.wk.authors.api.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.AskException;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.UpdateException;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;

import org.openrdf.model.Value;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriter;
/**
 *
 * @author Fernando Baculima
 */

public class SparqlFunctionsServiceImpl implements SparqlFunctionsService {

    @Inject
    private org.slf4j.Logger log;
        
    @Inject
    private ExternalSPARQLService sparqlService;

    //@Inject
    //private SesameService sesameService;
            
    @Override
    public boolean updateAuthor(String querytoUpdate) throws UpdateException {
        try {

            /**
             * El siguiente codigo documentado puede ser habilitado para CARGAR
             * datos en otro sparql endpoint
             *
             * this.connection = endpointUpdate.getConnection();
             * this.connection.begin(); 
             * Update update = this.connection.prepareUpdate(QueryLanguage.SPARQL,querytoUpdate); 
             * update.execute(); this.connection.commit();   
             */
            sparqlService.getSparqlService().update(QueryLanguage.SPARQL, querytoUpdate);
            return true;
        } catch (InvalidArgumentException | MarmottaException | UpdateExecutionException | MalformedQueryException ex) {
             log.error("Fail to Insert Triplet: " + querytoUpdate);
            return false;
        }
    }
    
      @Override
    public List<Map<String, Value>> querylocal (String query){
       
        try {         
            return sparqlService.getSparqlService().query(QueryLanguage.SPARQL, query);
        } catch (MarmottaException ex) {
            log.error("Excepcion: Fallo al ejecutar consulta ASK sobre: " + query);
            Logger.getLogger(SparqlFunctionsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @Override
    public boolean askAuthor(String querytoAsk) throws  AskException{
       
        try {
            return sparqlService.getSparqlService().ask(QueryLanguage.SPARQL, querytoAsk);
        } catch (MarmottaException ex) {
            log.error("Excepcion: Fallo al ejecutar consulta ASK sobre: " + querytoAsk);
            Logger.getLogger(SparqlFunctionsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    
    @Override
    @Deprecated
    public boolean updateLastAuthorsFile(RepositoryConnection conn, String query, String lastUpdateFile) throws UpdateException {

        OutputStream out = null;
        try {
            TupleQuery queryt = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            // open a file to write the result to it in JSON format
            out = new FileOutputStream(lastUpdateFile);
            TupleQueryResultHandler writer = new SPARQLResultsCSVWriter(out);
            // execute the query and write the result directly to file
            queryt.evaluate(writer);
            //despues de guardar los datos en el archivo se envian a la plataforma marmotta.
            out.close();
            return true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SparqlFunctionsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SparqlFunctionsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TupleQueryResultHandlerException ex) {
            Logger.getLogger(SparqlFunctionsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SparqlFunctionsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RepositoryException ex) {
            Logger.getLogger(SparqlFunctionsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(SparqlFunctionsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(SparqlFunctionsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }


}
