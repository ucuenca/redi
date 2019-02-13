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
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.apache.marmotta.ucuenca.wk.commons.util.SPARQLUtils;
import org.apache.marmotta.ucuenca.wk.pubman.api.SyncGraphDBMarmotta;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
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
        for (String g : graphs) {
            SynWorker sin = new SynWorker(g);
            sin.start();
        }
    }

    private class SynWorker extends Thread {

        private String graph;

        public SynWorker(String graph) {
            super();
            this.graph = graph;
        }

        @Override
        public void run() {
            try {
                URI createURI = ValueFactoryImpl.getInstance().createURI(graph);
                SPARQLUtils su = new SPARQLUtils(ss);
                log.info("Deleting graph {} from KIWI", graph);
                su.delete(graph);
                log.info("Counting graph {} from GraphDB", graph);
                List<Map<String, Value>> query = ess.getSparqlService().query(QueryLanguage.SPARQL, "select (count (*) as ?t) {graph <" + graph + "> {?a ?b ?c}}");
                int total = Integer.parseInt(query.get(0).get("t").stringValue());
                int actual = 0;
                Random rn = new Random();
                do {
                    int offset = rn.nextInt(total - 0 + 1) + 0;
                    List<Map<String, Value>> query1 = ess.getSparqlService().query(QueryLanguage.SPARQL, "select ?a ?b ?c {\n"
                            + "    graph <" + graph + "> {\n"
                            + "        ?a ?b ?c .\n"
                            + "    }\n"
                            + "} offset " + offset + " limit 100");
                    Model m = new LinkedHashModel();
                    for (Map<String, Value> a : query1) {
                        m.add((URI) a.get("a"), (URI) a.get("b"), a.get("c"));
                    }
                    RepositoryConnection connection = ses.getConnection();
                    connection.add(m, createURI);
                    connection.commit();
                    connection.close();
                    List<Map<String, Value>> query2 = ss.query(QueryLanguage.SPARQL, "select (count (*) as ?t) {graph <" + graph + "> {?a ?b ?c}}");
                    actual = Integer.parseInt(query2.get(0).get("t").stringValue());
                    log.info("Copying graph {} / {}", actual, total);
                } while (actual < total);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
