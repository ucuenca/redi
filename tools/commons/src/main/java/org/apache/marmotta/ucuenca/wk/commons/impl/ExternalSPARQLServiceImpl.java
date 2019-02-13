package org.apache.marmotta.ucuenca.wk.commons.impl;

import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.apache.marmotta.ucuenca.wk.commons.util.GraphDB;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * @author José Ortiz
 */
public class ExternalSPARQLServiceImpl implements ExternalSPARQLService {

    @Override
    public SparqlService getSparqlService() throws MarmottaException {
        try {
            return GraphDB.get().getSps();
        } catch (RepositoryException ex) {
            throw new MarmottaException(ex);
        }
    }

    @Override
    public RepositoryConnection getRepositoryConnetion() throws RepositoryException {
        return GraphDB.get().getConnection();
    }

    @Override
    public GraphDB getGraphDBInstance() throws RepositoryException {
        return GraphDB.get();
    }

}
