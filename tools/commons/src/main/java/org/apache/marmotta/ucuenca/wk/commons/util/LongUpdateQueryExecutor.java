/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.util;

import java.util.List;
import java.util.Map;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;

/**
 *
 * @author Jose Ortiz
 */
public class LongUpdateQueryExecutor {

    private String where;
    private String insert;
    private String delete;
    private String prefix;
    private String project;
    private SparqlService sparql;
    private static final int BULK = 1000;

    public LongUpdateQueryExecutor(SparqlService sparql) {
        this.sparql = sparql;
    }

    public LongUpdateQueryExecutor(SparqlService sparql, String where, String insert, String delete, String prefix, String project) {
        this.where = where;
        this.insert = insert;
        this.sparql = sparql;
        this.delete = delete;
        this.prefix = prefix;
        this.project = project;
    }

    public void execute() throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        String select = prefix + " select (count (*) as ?c_count) { " + where + " }";
        List<Map<String, Value>> query = sparql.query(QueryLanguage.SPARQL, select);
        int parseInt = Integer.parseInt(query.get(0).get("c_count").stringValue());
        for (int i = 0; i < parseInt; i += BULK) {
            String lo = " limit " + BULK + " offset " + i;
            String q = prefix + (delete != null ? " delete {" + delete + "} " : "") + (insert != null ? "insert {" + insert + "} " : "")
                    + " where { select " + project + " {" + where + "} " + lo + " }";
            sparql.update(QueryLanguage.SPARQL, q);
        }
    }

    public void deleteGraph(String graph) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        String data = "ask from <" + graph + "> { ?a ?b ?c }";
        boolean thereIsData = true;
        while (thereIsData) {
            String del = "delete {\n"
                    + "	graph <" + graph + "> {\n"
                    + "    	?a ?b ?c\n"
                    + "    }\n"
                    + "} where {\n"
                    + "  select ?a ?b ?c {\n"
                    + "      graph <" + graph + "> {\n"
                    + "          ?a ?b ?c\n"
                    + "      }\n"
                    + "  } limit " + BULK + "\n"
                    + "} ";
            sparql.update(QueryLanguage.SPARQL, del);
            thereIsData = sparql.ask(QueryLanguage.SPARQL, data);
        }
    }

}
