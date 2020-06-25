/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.apache.marmotta.ucuenca.wk.commons.util.BoundedExecutor;
import org.apache.marmotta.ucuenca.wk.pubman.api.SyncGraphDBMarmotta;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;

/**
 *
 * @author cedia
 */
@ApplicationScoped
public class SyncGraphDBMarmottaImpl implements SyncGraphDBMarmotta {

  @Inject
  private SparqlService ss;
  @Inject
  private ConstantService cs;
  @Inject
  private SesameService ses;
  @Inject
  private Logger log;
  @Inject
  private ExternalSPARQLService ess;

  @Override
  public void init() {

    List<String> graphs = new ArrayList();
    graphs.add(cs.getCentralGraph());
    graphs.add(cs.getAuthorsGraph());
    graphs.add(cs.getEndpointsGraph());
    graphs.add(cs.getOrganizationsGraph());
    for (String g : graphs) {
      SynWorker sin = new SynWorker(g);
      try {
        sin.run();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  private class SynWorker {

    private final int limit = 100;
    private String graph;

    public SynWorker(String graph) {
      super();
      this.graph = graph;
    }

    public Model obtainAdd(int offset) throws MarmottaException {
      List<Map<String, Value>> query1 = ess.getSparqlService().query(QueryLanguage.SPARQL, "select ?a ?b ?c {\n"
              + "    graph <" + graph + "> {\n"
              + "        ?a ?b ?c .\n"
              + "    }\n"
              + "} offset " + offset + " limit " + limit);
      Model m = new LinkedHashModel();
      for (Map<String, Value> a : query1) {
        m.add((Resource) a.get("a"), (URI) a.get("b"), a.get("c"));
      }
      return m;
    }

    public Model obtainDelete(int offset) throws RepositoryException, MarmottaException {
//      String spqSelect = ess.getGraphDBInstance().getSpqSelect();
//      String dl = "select * {\n"
//              + "  {\n"
//              + "    select * {\n"
//              + "        graph <" + graph + "> {\n"
//              + "            ?a ?b ?c .\n"
//              + "        }\n"
//              + "    } limit " + limit + " offset " + offset + "\n"
//              + "  } .\n"
//              + "  \n"
//              + "  filter not exists {\n"
//              + "  	service <" + spqSelect + "> {\n"
//              + "    	graph <" + graph + "> {\n"
//              + "    		?a ?b ?c .\n"
//              + "        }\n"
//              + "    }\n"
//              + "  }\n"
//              + "}";
      Model m = new LinkedHashModel();
//      List<Map<String, Value>> query1 = ss.query(QueryLanguage.SPARQL, dl);
//      for (Map<String, Value> a : query1) {
//        m.add((Resource) a.get("a"), (URI) a.get("b"), a.get("c"));
//      }
      return m;
    }

    public int countLocal() throws MarmottaException {
      List<Map<String, Value>> query2 = ss.query(QueryLanguage.SPARQL, "select (count (*) as ?t) {graph <" + graph + "> {?a ?b ?c}}");
      return Integer.parseInt(query2.get(0).get("t").stringValue());
    }

    public int countExt() throws MarmottaException {
      List<Map<String, Value>> query2 = ess.getSparqlService().query(QueryLanguage.SPARQL, "select (count (*) as ?t) {graph <" + graph + "> {?a ?b ?c}}");
      return Integer.parseInt(query2.get(0).get("t").stringValue());
    }

    public void runDelete(Model m) throws RepositoryException {
      URI createURI = ValueFactoryImpl.getInstance().createURI(graph);
      RepositoryConnection connection = ses.getConnection();
      connection.remove(m, createURI);
      connection.commit();
      connection.close();
    }

    public void runAdd(Model m) throws RepositoryException {
      URI createURI = ValueFactoryImpl.getInstance().createURI(graph);
      RepositoryConnection connection = ses.getConnection();
      connection.add(m, createURI);
      connection.commit();
      connection.close();
    }

    public void runAdd(int ini) throws MarmottaException, RepositoryException {
      Model obtainAdd = obtainAdd(ini);
      runAdd(obtainAdd);
    }

    public void runDelete(int ini) throws MarmottaException, RepositoryException {
      Model obtainAdd = obtainDelete(ini);
      runDelete(obtainAdd);
    }

    public void run() throws InterruptedException, MarmottaException {

      int countExt = countExt();
      int countLocal = countLocal();
      int max = Math.max(countExt, countLocal);
      BoundedExecutor bexecutorService = BoundedExecutor.getThreadPool(5);
      do {
        final int i = new Random().nextInt(max);
        log.info("Mirroring {} - {}/{}", graph, i, max);
        bexecutorService.submitTask(new Runnable() {
          @Override
          public void run() {
            try {
              runAdd(i);
              runDelete(i);
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        });
      } while (true);
    }

  }

}
