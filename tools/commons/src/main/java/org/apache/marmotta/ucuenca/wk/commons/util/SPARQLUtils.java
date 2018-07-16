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
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Provider;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;

/**
 *
 * @author Jose Ortiz
 */
@SuppressWarnings("PMD")
public class SPARQLUtils {

    private SparqlService sparqlService;

    public SPARQLUtils(SparqlService sparql) {
        this.sparqlService = sparql;
    }

    public int count(String graph) throws MarmottaException {
        String c = "select (count (*) as ?co) { graph <" + graph + "> { ?a ?b ?c }}";
        List<Map<String, Value>> query = sparqlService.query(QueryLanguage.SPARQL, c);
        return Integer.parseInt(query.get(0).get("co").stringValue());
    }

    public void delete(String graph) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        new LongUpdateQueryExecutor(sparqlService).deleteGraph(graph);
    }

    public void addAll(String graphTarget, String graphSource) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        new LongUpdateQueryExecutor(sparqlService).copyGraph(graphTarget, graphSource);
    }

    public void minus(String graphTarget, String graphUniverse, String graphNot) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        new LongUpdateQueryExecutor(sparqlService,
                "	graph <" + graphUniverse + "> {\n"
                + "		?a ?b ?c \n"
                + "		filter not exists {\n"
                + "			graph <" + graphNot + "> {\n"
                + "				?a ?b ?c .\n"
                + "			}\n"
                + "		}		\n"
                + "	}\n",
                "	graph <" + graphTarget + "> {\n"
                + "		?a ?b ?c .\n"
                + "	}\n",
                null, "", "?a ?b ?c").execute();
    }

    public void replaceSameAs(String d, String sag, String dg, String ig, boolean s) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {
        if (s) {
            new LongUpdateQueryExecutor(sparqlService,
                    "	graph <" + sag + "> {\n"
                    + "		?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                    + "	}\n"
                    + "	graph <" + d + "> {\n"
                    + "		?c ?p ?v .\n"
                    + "	}\n",
                    "	graph <" + dg + "> {\n"
                    + "		?c ?p ?v .\n"
                    + "	}\n"
                    + "	graph <" + ig + "> {\n"
                    + "		?a ?p ?v .\n"
                    + "	}\n",
                    null, "", "?c ?p ?v ?a").execute();

        } else {
            new LongUpdateQueryExecutor(sparqlService,
                    "	graph <" + sag + "> {\n"
                    + "		?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                    + "	}\n"
                    + "	graph <" + d + "> {\n"
                    + "		?v ?p ?c .\n"
                    + "	}\n",
                    "	graph <" + dg + "> {\n"
                    + "		?v ?p ?c .\n"
                    + "	}\n"
                    + "	graph <" + ig + "> {\n"
                    + "		?v ?p ?a .\n"
                    + "	}\n",
                    null, "", "?c ?p ?v ?a").execute();
        }
    }

    public void mergeRawDataSameAs(List<Provider> providersList, String graph, String graphOrigin) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {
        String q = "select distinct ?a { graph <" + graphOrigin + "> { ?a <http://www.w3.org/2002/07/owl#sameAs> ?c . } }";
        List<Map<String, Value>> query = sparqlService.query(QueryLanguage.SPARQL, q);
        for (Map<String, Value> ar : query) {
            String aAU = ar.get("a").stringValue();
            for (Provider aProvider : providersList) {
                String providersGraph = aProvider.Graph;
                new LongUpdateQueryExecutor(sparqlService,
                        "    graph <" + graphOrigin + "> {\n"
                        + "        <" + aAU + "> <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                        + "    }\n"
                        + "  graph <" + providersGraph + "> {\n"
                        + "        ?c ?p ?v .\n"
                        + "    }\n",
                        "    graph <" + graph + "> {\n"
                        + "        ?c ?p ?v .\n"
                        + "    }\n",
                        null, "", "?c ?p ?v").execute();

                new LongUpdateQueryExecutor(sparqlService,
                        "    graph <" + graphOrigin + "> {\n"
                        + "        <" + aAU + "> <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                        + "    }\n"
                        + "    graph <" + providersGraph + "> {\n"
                        + "         ?c ?p ?v .\n"
                        + "         ?v ?w ?q .\n"
                        + "    }\n",
                        "    graph <" + graph + "> {\n"
                        + "        ?v ?w ?q .\n"
                        + "    }\n",
                        null, "", "?v ?w ?q").execute();

                new LongUpdateQueryExecutor(sparqlService,
                        "    graph <" + graphOrigin + "> {\n"
                        + "        <" + aAU + "> <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                        + "    }\n"
                        + "    graph <" + providersGraph + "> {\n"
                        + "         ?c ?p ?v .\n"
                        + "         ?v ?w ?q .\n"
                        + "        ?q ?z ?m .\n"
                        + "    }\n",
                        "    graph <" + graph + "> {\n"
                        + "        ?q ?z ?m .\n"
                        + "    }\n",
                        null, "", "?q ?z ?m").execute();
            }
        }
    }

    public void removeDuplicates(String graph) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {
        String q = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                + "\n"
                + "delete {\n"
                + "	graph <" + graph + "> {\n"
                + "		?no owl:sameAs ?p .\n"
                + "	}\n"
                + "}\n"
                + "insert {\n"
                + "	graph <" + graph + "> {\n"
                + "		?r owl:sameAs ?p .\n"
                + "		?r owl:sameAs ?no .\n"
                + "	}\n"
                + "}\n"
                + "where {\n"
                + "	graph <" + graph + "> {\n"
                + "		?no owl:sameAs ?p .\n"
                + "	} .\n"
                + "	{\n"
                + "		select ?p (count(distinct ?o) as ?c) (max(str(?o)) as ?u) ( iri (?u) as ?r) {\n"
                + "	  		graph <" + graph + "> {\n"
                + "	  			?o owl:sameAs ?p .\n"
                + "	  		}\n"
                + "		} group by ?p having (?c > 1)\n"
                + "	}\n"
                + "}";
        String qq = "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n"
                + "insert {	\n"
                + "	graph <" + graph + "> {\n"
                + "		?o owl:sameAs ?o .\n"
                + "	}\n"
                + "} \n"
                + "where {\n"
                + "	graph <" + graph + "> {\n"
                + "		?o owl:sameAs ?p .\n"
                + "	}\n"
                + "}";
        sparqlService.update(QueryLanguage.SPARQL, q);
        sparqlService.update(QueryLanguage.SPARQL, qq);
    }

}
