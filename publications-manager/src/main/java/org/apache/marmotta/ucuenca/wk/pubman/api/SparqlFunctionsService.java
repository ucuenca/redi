/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.api;

import org.apache.marmotta.ucuenca.wk.pubman.exceptions.PubException;

/**
 *
 * @author Fernando B. CEDIA
 */
public interface SparqlFunctionsService {

    boolean updatePub(String querytoUpdate) throws PubException;

    boolean executeInsert(String graph, String sujeto, String predicado, String objeto);

    boolean executeInsert(String graph, String sujeto, String predicado, String objeto, String datatype);

}
