/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.util;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.QueryType;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.platform.sparql.services.sparqlio.rdf.SPARQLGraphResultWriter;
import org.openrdf.http.client.HTTPClient;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
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
import org.openrdf.repository.sparql.SPARQLConnection;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

/**
 *
 * @author cedia
 */
public final class GraphDB {

    private static GraphDB eta;
    private static final int MAX_TRIPLES_ADD = 1000;
    private static final int MAX_TRIPLES_ADD_2 = 2000;
    private SPARQLRepository data;
    private SPARQLRepository data4solr;
    private SparqlService sps;
    private ConcurrentHashMap<URI, Model> hmmdl = new ConcurrentHashMap<>();
    //private String spqSelect;

    public SparqlService getSps() {
        return sps;
    }

    class CustomRepository extends SPARQLRepository {

        public CustomRepository(String queryEndpointUrl, String updateEndpointUrl) {
            super(queryEndpointUrl, updateEndpointUrl);
        }

        public RepositoryConnection getConnection()
                throws RepositoryException {
            if (!isInitialized()) {
                throw new RepositoryException("SPARQLRepository not initialized.");
            }
            return new SPARQLConnection(this, true);
        }

        @Override
        protected HTTPClient createHTTPClient() {
            HTTPClient createHTTPClient = super.createHTTPClient();
            createHTTPClient.setConnectionTimeout(120 * 1000);
            return createHTTPClient;
        }
    }

    private GraphDB(String server, String database, String user, String pass) throws RepositoryException {
        data = new CustomRepository(server + REPOSITORIES + database, server + REPOSITORIES + database + "/statements");
        data4solr = new CustomRepository(server + REPOSITORIES + database, server + REPOSITORIES + database + "/statements");
        if (user != null && pass != null) {
            data.setUsernameAndPassword(user, pass);
            data4solr.setUsernameAndPassword(user, pass);
        }
        //spqSelect = server + "repositories/" + database;
        ConcurrentHashMap<String, String> additionalHttpHeaders = new ConcurrentHashMap<>();
        additionalHttpHeaders.put("Accept", "application/sparql-results+json,*/*;q=0.9");
        data.setAdditionalHttpHeaders(additionalHttpHeaders);
        data4solr.setAdditionalHttpHeaders(additionalHttpHeaders);
        data.initialize();
        data4solr.initialize();
        SparqlService sparqlService = new SparqlService() {
            @Override
            public Query parseQuery(QueryLanguage ql, String string) throws RepositoryException, MalformedQueryException {
                throw new UnsupportedOperationException(NOT_SUPPORTED_YET); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public QueryType getQueryType(QueryLanguage ql, String string) throws MalformedQueryException {
                throw new UnsupportedOperationException(NOT_SUPPORTED_YET); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void query(QueryLanguage ql, String string, OutputStream pout, String string1, int i) throws MarmottaException, TimeoutException, MalformedQueryException {
                throw new UnsupportedOperationException(NOT_SUPPORTED_YET); //To change body of generated methods, choose Tools | Templates.
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
                    throw new MarmottaException(ex);
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
                        ConcurrentHashMap<String, Value> mp = new ConcurrentHashMap<>();
                        while (iterator.hasNext()) {
                            Binding next = iterator.next();
                            mp.put(next.getName(), next.getValue());
                        }
                        r.add(mp);
                    }
                    connection.close();
                } catch (Exception ex) {
                    throw new MarmottaException(ex + "" + string);
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
                    throw new MarmottaException(ex);
                }
            }

            @Override
            public void query(QueryLanguage ql, String string, TupleQueryResultWriter writer, BooleanQueryResultWriter writer1, SPARQLGraphResultWriter writer2, int i) throws MarmottaException, MalformedQueryException, QueryEvaluationException, TimeoutException {
                throw new UnsupportedOperationException(NOT_SUPPORTED_YET); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void query(QueryLanguage ql, String string, QueryResultWriter writer, int i) throws MarmottaException, MalformedQueryException, QueryEvaluationException, TimeoutException {
                throw new UnsupportedOperationException(NOT_SUPPORTED_YET); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void createServiceDescription(RDFWriter writer, String string, boolean bln) throws RDFHandlerException {
                throw new UnsupportedOperationException(NOT_SUPPORTED_YET); //To change body of generated methods, choose Tools | Templates.
            }
            private static final String NOT_SUPPORTED_YET = "Not supported yet.";
        };
        sps = sparqlService;
    }
    private static final String REPOSITORIES = "repositories/";

    @SuppressWarnings("PMD")
    public static GraphDB get(String server, String database, String user, String pass) throws RepositoryException {
        if (eta == null) {
            eta = new GraphDB(server, database, user, pass);
        }
        return eta;
    }

    public RepositoryConnection getConnection() throws RepositoryException {
        return data.getConnection();
    }

    public RepositoryConnection getConnectionCustom() throws RepositoryException {
        return data4solr.getConnection();
    }

    @SuppressWarnings("PMD")
    public void runSplitAddOp(RepositoryConnection connection, Model data, Resource providerContext) throws RepositoryException, RDFHandlerException {
        LinkedHashModel mp = new LinkedHashModel();
        for (Statement s : data) {
            if (mp.size() > MAX_TRIPLES_ADD) {
                runAddOp(connection, mp, providerContext);
                mp.clear();
            }
            if (s.getObject() instanceof URI && !checkURI(s.getObject().stringValue())
                    || s.getSubject() instanceof URI && !checkURI(s.getSubject().stringValue())) {
                continue;
            }
            boolean ignore = false;
            Value v = s.getObject();
            if (v instanceof Literal) {
                Literal xs = (Literal) v;
                if (xs.getDatatype() != null && xs.getDatatype().equals(RDF.LANGSTRING) && (xs.getLanguage() == null || xs.getLanguage().trim().isEmpty())) {
                    mp.add(s.getSubject(), s.getPredicate(), ValueFactoryImpl.getInstance().createLiteral(xs.stringValue()));
                    ignore = true;
                }
            }
            if (!ignore) {
                mp.add(s);
            }
        }
        runAddOp(connection, mp, providerContext);
    }

    public void runAddOp(RepositoryConnection connection, Model data, Resource providerContext) throws RepositoryException, RDFHandlerException {
        try {
            connection.add(data, providerContext);
        } catch (Exception e) {
            Rio.write(data, System.out, RDFFormat.RDFXML);
            throw e;
        }
    }

    public void runSplitDelOp(RepositoryConnection connection, Model data, Resource providerContext) throws RepositoryException, RDFHandlerException {
        LinkedHashModel mp = new LinkedHashModel();
        for (Statement s : data) {
            if (mp.size() > MAX_TRIPLES_ADD) {
                runDelOp(connection, mp, providerContext);
                mp.clear();
            }
            if (s.getObject() instanceof URI && !checkURI(s.getObject().stringValue())
                    || s.getSubject() instanceof URI && !checkURI(s.getSubject().stringValue())) {
                continue;
            }
            mp.add(s);
        }
        runDelOp(connection, mp, providerContext);
    }

    public void runDelOp(RepositoryConnection connection, Model data, Resource providerContext) throws RepositoryException, RDFHandlerException {
        try {
            connection.remove(data, providerContext);
        } catch (Exception e) {
            Rio.write(data, System.out, RDFFormat.RDFXML);
            throw e;
        }
    }

    public boolean checkURI(String ur) {
        boolean r = false;
        try {
            java.net.URI.create(ur);
            r = true;
        } catch (Exception w) {
            Logger.getLogger(GraphDB.class.getName()).log(Level.SEVERE, null, w);
        }
        return r;
    }

    public synchronized void addBuffer(URI ug, Model mdlAdd) throws RepositoryException, RDFHandlerException {
        Model mdl = hmmdl.get(ug);
        if (mdl == null) {
            mdl = new LinkedHashModel();
            hmmdl.put(ug, mdl);
        }
        mdl.addAll(mdlAdd);
        if (mdl.size() > MAX_TRIPLES_ADD_2) {
            dumpBuffer(ug);
        }

    }

    public synchronized void addBufferBucket(String g, String s, String p, Set<String> o) throws RepositoryException, RDFHandlerException {
        ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
        URI us = instance.createURI(s);
        URI ug = instance.createURI(g);
        URI up = instance.createURI(p);
        Model mdl = new LinkedHashModel();
        for (String oo : o) {
            URI uo = instance.createURI(oo);
            mdl.add(us, up, uo);
        }
        addBuffer(ug, mdl);
    }

    public synchronized void addBuffer(String g, String s, String p, String o) throws RepositoryException, RDFHandlerException {
        ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
        URI ug = instance.createURI(g);
        URI us = instance.createURI(s);
        URI up = instance.createURI(p);
        URI uo = instance.createURI(o);
        Model mdl = new LinkedHashModel();
        mdl.add(us, up, uo);
        addBuffer(ug, mdl);
    }

    public void dumpBuffer() throws RepositoryException, RDFHandlerException {
        Set<URI> keySet = hmmdl.keySet();
        for (URI g : keySet) {
            dumpBuffer(g);
        }
    }

    public void dumpBuffer(URI g) throws RepositoryException, RDFHandlerException {
        Model mdl = hmmdl.get(g);
        if (mdl.isEmpty()) {
            return;
        }
        RepositoryConnection connection = getConnection();
        connection.begin();
        runAddOp(connection, mdl, g);
        connection.commit();
        connection.close();
        mdl.clear();
    }
//
//  public String getSpqSelect() {
//    return spqSelect;
//  }
//
//  public void setSpqSelect(String spqSelect) {
//    this.spqSelect = spqSelect;
//  }
}
