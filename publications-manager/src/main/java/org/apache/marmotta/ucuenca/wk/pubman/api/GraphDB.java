/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.api;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.QueryType;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.platform.sparql.services.sparqlio.rdf.SPARQLGraphResultWriter;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.QueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

/**
 *
 * @author cedia
 */
public class GraphDB {

    private static GraphDB eta;

    private SPARQLRepository data;
    private SparqlService sps;

    public SparqlService getSps() {
        return sps;
    }

    
    
    private GraphDB() throws RepositoryException {
        data = new SPARQLRepository("http://201.159.222.25:8180/repositories/data", "http://201.159.222.25:8180/repositories/data/statements");
        data.initialize();
        SparqlService sparqlService = new SparqlService() {
            @Override
            public Query parseQuery(QueryLanguage ql, String string) throws RepositoryException, MalformedQueryException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public QueryType getQueryType(QueryLanguage ql, String string) throws MalformedQueryException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void query(QueryLanguage ql, String string, OutputStream out, String string1, int i) throws MarmottaException, TimeoutException, MalformedQueryException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean ask(QueryLanguage ql, String string) throws MarmottaException {
                boolean t = false;
                try {
                    RepositoryConnection connection = getConnection();
                    connection.begin();
                    BooleanQuery prepareBooleanQuery = connection.prepareBooleanQuery(ql, string);
                    t = prepareBooleanQuery.evaluate();
                    connection.commit();
                    connection.close();

                } catch (Exception ex) {
                    Logger.getLogger(GraphDB.class.getName()).log(Level.SEVERE, null, ex);
                }
                return t;
            }

            @Override
            public List<Map<String, Value>> query(QueryLanguage ql, String string) throws MarmottaException {
                List<Map<String, Value>> r = new ArrayList<>();
                try {
                    RepositoryConnection connection = getConnection();
                    TupleQueryResult evaluate = connection.prepareTupleQuery(ql, string).evaluate();
                    while (evaluate.hasNext()) {
                        Iterator<Binding> iterator = evaluate.next().iterator();
                        Map<String, Value> mp = new HashMap<>();
                        while (iterator.hasNext()) {
                            Binding next = iterator.next();
                            mp.put(next.getName(), next.getValue());
                        }
                        r.add(mp);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(GraphDB.class.getName()).log(Level.SEVERE, null, ex);
                }
                return r;
            }

            @Override
            public void update(QueryLanguage ql, String string) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {
                try {
                    RepositoryConnection connection = getConnection();
                    connection.begin();
                    connection.prepareUpdate(ql, string).execute();
                    connection.commit();
                    connection.close();
                } catch (RepositoryException ex) {
                    Logger.getLogger(GraphDB.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void query(QueryLanguage ql, String string, TupleQueryResultWriter writer, BooleanQueryResultWriter writer1, SPARQLGraphResultWriter writer2, int i) throws MarmottaException, MalformedQueryException, QueryEvaluationException, TimeoutException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void query(QueryLanguage ql, String string, QueryResultWriter writer, int i) throws MarmottaException, MalformedQueryException, QueryEvaluationException, TimeoutException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void createServiceDescription(RDFWriter writer, String string, boolean bln) throws RDFHandlerException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        sps = sparqlService;
    }

    public static GraphDB get() throws RepositoryException {
        if (eta == null) {
            eta = new GraphDB();
        }
        return eta;
    }

    public RepositoryConnection getConnection() throws RepositoryException {
        return data.getConnection();
    }

}
