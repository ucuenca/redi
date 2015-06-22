/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.authors.api;

import org.apache.marmotta.ucuenca.wk.authors.exceptions.AskException;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.UpdateException;
import org.openrdf.repository.RepositoryConnection;

/**
 *
 * @author Satellite
 */
public interface AuthorsUpdateService {
    

    
     Boolean updateAuthor( String querytoUpdate) throws UpdateException;

     boolean updateLastAuthorsFile(RepositoryConnection con, String querytoUpdate, String endpointUrlUpdate) throws UpdateException ;

     boolean askAuthor(String querytoAsk) throws  AskException;
     
     boolean askAuthorVersioning(String resource);
  

}
