/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.authors.api;

import java.util.List;
import java.util.Map;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.AskException;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.UpdateException;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;

/**
 *
 * @author Fernando B.
 * @author Jose Cullcay
 * CEDIA
 */
public interface SparqlFunctionsService {
  
    /**
     * EXECUTE UPDATE QUERY   ( INSERT DATA ... )   to insert the author data in marmotta )
     * @param querytoUpdate
     * @return
     * @throws UpdateException 
     */
     boolean updateAuthor( String querytoUpdate) throws UpdateException;

     /**
      * @deprecated 
      * To update aux file with URIS of resources loaded in marmotta
      * @param con
      * @param querytoUpdate
      * @param endpointUrlUpdate
      * @return
      * @throws UpdateException 
      */
     boolean updateLastAuthorsFile(RepositoryConnection con, String querytoUpdate, String endpointUrlUpdate) throws UpdateException ;

     /**
      * Execute ASK query :  return true if an author resource is in marmotta
      * @param querytoAsk
      * @return
      * @throws AskException 
      */
     boolean askAuthor(String querytoAsk) throws  AskException;
     
  
     List<Map<String, Value>>  querylocal (String query);
}
