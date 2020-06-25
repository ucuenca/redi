package org.apache.marmotta.ucuenca.wk.commons.impl;

import javax.inject.Inject;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
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

  @Inject
  private ConfigurationService conf;

  public String getServer() {
    return conf.getStringConfiguration("graphdb.server", "http://201.159.222.25:8180/");
  }

  public String getDataBase() {
    return conf.getStringConfiguration("graphdb.database", "redi");
  }

  public String getUser() {
    return conf.getStringConfiguration("graphdb.user", "redi");
  }

  public String getPassword() {
    return conf.getStringConfiguration("graphdb.password", "nopass");
  }

  public boolean hasCredentials() {
    return conf.isConfigurationSet("graphdb.user") && conf.isConfigurationSet("graphdb.password");
  }

  @Override
  public SparqlService getSparqlService() throws MarmottaException {
    try {
      return getGraphDBInstance().getSps();
    } catch (RepositoryException ex) {
      throw new MarmottaException(ex);
    }
  }

  @Override
  public RepositoryConnection getRepositoryConnetion() throws RepositoryException {
    return getGraphDBInstance().getConnection();
  }

  @Override
  public GraphDB getGraphDBInstance() throws RepositoryException {
    GraphDB v = null;
    if (hasCredentials()) {
      v = GraphDB.get(getServer(), getDataBase(), getUser(), getPassword());
    } else {
      v = GraphDB.get(getServer(), getDataBase(), null, null);
    }
    return v;
  }

}
