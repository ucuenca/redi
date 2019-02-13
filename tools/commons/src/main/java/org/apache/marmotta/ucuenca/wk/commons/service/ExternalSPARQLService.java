/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.service;

import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.util.GraphDB;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author José Ortiz
 */
public interface ExternalSPARQLService {

    SparqlService getSparqlService() throws MarmottaException;

    RepositoryConnection getRepositoryConnetion() throws RepositoryException;

    GraphDB getGraphDBInstance() throws RepositoryException;

}
