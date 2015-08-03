/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.api;

//import org.apache.marmotta.ucuenca.wk.authors.exceptions.AskException;
import org.apache.marmotta.ucuenca.wk.pubman.exceptions.PubException;
import org.openrdf.repository.RepositoryConnection;

/**
 *
 * @author Fernando B.
 * CEDIA
 */
public interface SparqlFunctionsService {
  
    /**
     * EXECUTE UPDATE QUERY   ( INSERT DATA ... )   to insert the publication data in marmotta )
     * @param querytoUpdate
     * @return
     * @throws UpdateException 
     */
     boolean updatePub( String querytoUpdate) throws PubException;

     /**
      * @deprecated 
      * To update aux file with URIS of resources loaded in marmotta
      * @param con
      * @param querytoUpdate
      * @param endpointUrlUpdate
      * @return
      * @throws UpdateException 
      */
     boolean updateLastAuthorsFile(RepositoryConnection con, String querytoUpdate, String endpointUrlUpdate) throws PubException ;

     /**
      * Execute ASK query :  return true if an author resource is in marmotta
      * @param querytoAsk
      * @return
      * @throws AskException 
      */
  //   boolean askAuthor(String querytoAsk) throws  AskException;
     
     /**
      * @deprecated 
      * Find versions data of an author resource, return true if the author was found
      * @param resource
      * @return 
      */
  //   boolean askAuthorVersioning(String resource);
  

}
