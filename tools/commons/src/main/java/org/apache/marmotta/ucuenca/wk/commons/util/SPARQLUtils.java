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
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;

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

    public void removeDuplicatedSameAs(String O, String D) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        String OT = O + "__T";
        String q1 = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                + "insert {\n"
                + "    graph <" + OT + "> {\n"
                + "        ?a owl:sameAs ?c .\n"
                + "    }\n"
                + "} where {\n"
                + "    graph <" + O + "> {\n"
                + "        ?a owl:sameAs ?b .\n"
                + "        ?c owl:sameAs ?b .\n"
                + "    }\n"
                + "}";
        runUpdate(q1);
        String OTT = OT + "__T";
        String q2 = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                + "insert {\n"
                + "    graph <" + OTT + "> {\n"
                + "        ?a owl:sameAs ?c .\n"
                + "    }\n"
                + "} where {\n"
                + "    graph <" + OT + "> {\n"
                + "        ?a owl:sameAs* ?c .\n"
                + "    }\n"
                + "}";
        runUpdate(q2);
        String OTTT = OTT + "__T";
        String q3 = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                + "insert {\n"
                + "    graph <" + OTTT + "> {\n"
                + "    	?p owl:sameAs ?q .\n"
                + "	} \n"
                + "} where {\n"
                + "    {\n"
                + "        select ?h (min(?a) as ?p) {\n"
                + "            {\n"
                + "                select ?a (md5(group_concat(?c)) as ?h) {\n"
                + "                    {\n"
                + "                        select ?a ?c {\n"
                + "                            graph <" + OTT + "> {\n"
                + "                                ?a owl:sameAs ?c .\n"
                + "                            } \n"
                + "                        } order by ?a ?c\n"
                + "                    }\n"
                + "                } group by ?a\n"
                + "            }\n"
                + "        } group by ?h\n"
                + "    }\n"
                + "    graph <" + OTT + "> {\n"
                + "    	?p owl:sameAs ?q .\n"
                + "	} \n"
                + "}";
        runUpdate(q3);
        replaceSameAsSubject(O, D, OTTT);
        deleteGraph(OT);
        deleteGraph(OTT);
        deleteGraph(OTTT);

    }

    public void removeDuplicatedPrior(String G1, String G2, String G, String D) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        String q = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                + "insert {\n"
                + "    graph <&D&> {\n"
                + "        ?q2 owl:sameAs ?p2 .\n"
                + "    }\n"
                + "    graph <&I&> {\n"
                + "        ?q1 owl:sameAs ?p2 .\n"
                + "    }\n"
                + "} \n"
                + "where  {\n"
                + "    {   \n"
                + "        select ?c {\n"
                + "            graph <" + G + "> {\n"
                + "                ?a owl:sameAs ?c .\n"
                + "            }\n"
                + "        } group by ?c having (count (distinct ?a ) > 1 ) \n"
                + "    }\n"
                + "    graph <" + G + "> {\n"
                + "        ?q1 owl:sameAs ?c .\n"
                + "        ?q2 owl:sameAs ?c .\n"
                + "    }\n"
                + "    graph <" + G1 + "> {\n"
                + "        ?q1 owl:sameAs [] .\n"
                + "    }\n"
                + "    graph <" + G2 + "> {\n"
                + "        ?q2 owl:sameAs ?p2 .\n"
                + "    }\n"
                + "}";
        transformGraph(G, D, q);
    }

    public void harvestRawData(String S, String D, int up) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        for (int i = 1; i <= up; i++) {
            harvestSameAs(S, D, i, null);
        }
    }

    public void harvestSameAs(String S, String D, int level, List<String> providersx) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        String providers = "";
        for (String p : providersx) {
            providers = " <" + p + "> ";

            String qp = "";
            String last = "";
            for (int i = 0; i < level; i++) {
                String j = i == 0 ? "" : "" + (i - 1);
                last = "    ?c" + j + " ?p" + i + " ?c" + i + " .\n";
                qp += last;
            }

            String q = "insert {\n"
                    + "  graph <" + D + "> {\n"
                    + last
                    + "  }\n"
                    + "}\n"
                    + "where {\n"
                    + "  graph <" + S + "> {\n"
                    + "    ?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                    + "  }\n"
                    + "  values ?g { " + providers + " } .\n"
                    + "  graph ?g {\n"
                    + qp
                    + "  }\n"
                    + "}";
            runUpdate(q);
        }
    }

    public void replaceSameAsObjectExcept(String O, String D, String S, String... E) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        String qe = "";
        if (E.length != 0) {
            qe = "filter ( ";
            for (int i = 0; i < E.length; i++) {
                qe += " ?p != <" + E[i] + "> ";
                if (i != E.length - 1) {
                    qe += " && ";
                }
            }
            qe += " ) .";
        }
        String q = "insert {\n"
                + "        graph <&D&> {\n"
                + "                ?v ?p ?c .\n"
                + "        }\n"
                + "        graph <&I&> {\n"
                + "                ?v ?p ?a .\n"
                + "        }\n"
                + "}\n"
                + "where {\n"
                + "        graph <" + S + "> {\n"
                + "                ?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "        }\n"
                + "        graph <" + O + "> {\n"
                + "                ?v ?p ?c .\n"
                + "                " + qe + "\n"
                + "        }\n"
                + "}";
        transformGraph(O, D, q);
    }

    public void replaceSameAsSubject(String O, String D, String S) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        String q = "insert {\n"
                + "        graph <&D&> {\n"
                + "                ?c ?p ?v .\n"
                + "        }\n"
                + "        graph <&I&> {\n"
                + "                ?a ?p ?v .\n"
                + "        }\n"
                + "}\n"
                + "where {\n"
                + "        graph <" + S + "> {\n"
                + "                ?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "        }\n"
                + "        graph <" + O + "> {\n"
                + "                ?c ?p ?v .\n"
                + "        }\n"
                + "}";
        transformGraph(O, D, q);
    }

    public void transformGraph(String O, String D, String Q) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        String OI = O + "___I";
        String OD = O + "___D";
        String UQ = Q.replaceAll("&I&", OI).replaceAll("&D&", OD);
        runUpdate(UQ);
        copyGraphFilter(O, OD, D);
        copyGraph(OI, D);
        deleteGraph(OI);
        deleteGraph(OD);
    }

    public void deleteGraph(String uri) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        runUpdate("CLEAR GRAPH <" + uri + ">");
    }

    public void copyGraph(String org, String des) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        String q = "insert {\n"
                + "    graph <" + des + "> {\n"
                + "        ?a ?b ?c .\n"
                + "    }\n"
                + "} where {\n"
                + "    graph <" + org + "> {\n"
                + "        ?a ?b ?c .\n"
                + "    }\n"
                + "}";
        runUpdate(q);
    }

    public void copyGraphFilter(String org, String fil, String des) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        String q = "insert {\n"
                + "    graph <" + des + "> {\n"
                + "        ?a ?b ?c .\n"
                + "    }\n"
                + "} where {\n"
                + "    graph <" + org + "> {\n"
                + "        ?a ?b ?c .\n"
                + "        filter not exists {\n"
                + "             graph <" + fil + "> {\n"
                + "                 ?a ?b ?c .\n"
                + "             }\n"
                + "        }\n"
                + "    }\n"
                + "}";
        runUpdate(q);
    }

    public void runUpdate(String query) throws RepositoryException {
        try {
            runUpdateRepository(query);
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    public void runUpdateRepository(String query) throws UpdateExecutionException, MalformedQueryException, InvalidArgumentException, MarmottaException {
        sparqlService.update(QueryLanguage.SPARQL, query);
    }

    public void clearSameAs(String SA, String SAF, String Man) throws RepositoryException, MalformedQueryException, UpdateExecutionException {

        String q_pre = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                + "insert {\n"
                + "    graph <&D&> {\n"
                + "        ?b owl:sameAs ?cx .\n"
                + "        ?a owl:sameAs ?c .\n"
                + "    }\n"
                + "} where {\n"
                + "    graph <" + SAF + "> {\n"
                + "        {?b owl:sameAs ?cx .}\n"
                + "         union \n"
                + "        {?a owl:sameAs ?c .}\n"
                + "    }\n"
                + "    graph <" + Man + "> {\n"
                + "        ?a owl:is ?b .\n"
                + "    }\n"
                + "} ";

        transformGraph(SAF, SAF + "_new", q_pre);
        deleteGraph(SAF);
        copyGraph(SAF + "_new", SAF);
        deleteGraph(SAF + "_new");

        String q = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                + "insert {\n"
                + "    graph <&D&> {\n"
                + "        ?a owl:sameAs ?c .\n"
                + "        ?a owl:sameAs ?b .\n"
                + "    }\n"
                + "} where {\n"
                + "    graph <" + SA + "> {\n"
                + "        ?a owl:sameAs ?c .\n"
                + "        ?b owl:sameAs ?c .\n"
                + "    }\n"
                + "    graph <" + SAF + "> {\n"
                + "        ?a owl:sameAs ?b .\n"
                + "    }\n"
                + "}";
        transformGraph(SA, SA + "_new", q);
        deleteGraph(SA);
        copyGraph(SA + "_new", SA);
        deleteGraph(SA + "_new");
    }

    public void clearDifferent(String graph, String graph2) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        String q = "insert {\n"
                + "        graph <&D&> {\n"
                + "                ?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "        }\n"
                + "}\n"
                + "where {\n"
                + "        graph <" + graph + "> {\n"
                + "                ?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "        }\n"
                + "        graph <" + graph2 + "> {\n"
                + "                ?b <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "        }\n"
                + "}";
        transformGraph(graph, graph + "__X", q);
        deleteGraph(graph);
        copyGraph(graph + "__X", graph);
        deleteGraph(graph + "__X");
    }

    public void insertGraph(String graph, String graph2, String prop) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        String q = "insert {\n"
                + "        graph <&I&> {\n"
                + "                ?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "        }\n"
                + "}\n"
                + "where {\n"
                + "        graph <" + graph2 + "> {\n"
                + "                ?a <" + prop + "> ?c .\n"
                + "        }\n"
                + "}";
        transformGraph(graph, graph + "__X", q);
        deleteGraph(graph);
        copyGraph(graph + "__X", graph);
        deleteGraph(graph + "__X");
    }

    public void minusGraph(String graph, String graph2, String prop) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        String q = "insert {\n"
                + "        graph <&D&> {\n"
                + "                ?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "        }\n"
                + "}\n"
                + "where {\n"
                + "        graph <" + graph + "> {\n"
                + "                ?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "        }\n"
                + "        graph <" + graph2 + "> {\n"
                + "                ?a <" + prop + "> ?c .\n"
                + "        }\n"
                + "}";
        transformGraph(graph, graph + "__X", q);
        deleteGraph(graph);
        copyGraph(graph + "__X", graph);
        deleteGraph(graph + "__X");
    }

    public void minusGraph(String graph, String graph2) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        String q = "insert {\n"
                + "        graph <&D&> {\n"
                + "                ?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "        }\n"
                + "}\n"
                + "where {\n"
                + "        graph <" + graph + "> {\n"
                + "                ?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "        }\n"
                + "        graph <" + graph2 + "> {\n"
                + "                ?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "        }\n"
                + "}";
        transformGraph(graph, graph + "__X", q);
        deleteGraph(graph);
        copyGraph(graph + "__X", graph);
        deleteGraph(graph + "__X");
    }

    public void mergeSameAs(String graph) throws MarmottaException, RepositoryException, MalformedQueryException, UpdateExecutionException {
        int c = 0;
        int c1 = 0;
        do {
            c = c1;
            removeLayerSameAs(graph);
            c1 = count(graph);
        } while (c != c1);
    }

    public void removeLayerSameAs(String graph) throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        String q = "insert {\n"
                + "        graph <&D&> {\n"
                + "                ?b <http://www.w3.org/2002/07/owl#sameAs> ?cx .\n"
                + "        }\n"
                + "        graph <&I&> {\n"
                + "                ?a <http://www.w3.org/2002/07/owl#sameAs> ?cx .\n"
                + "        }\n"
                + "}\n"
                + "where {\n"
                + "        graph <" + graph + "> {\n"
                + "                ?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "                ?b <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "                ?b <http://www.w3.org/2002/07/owl#sameAs> ?cx .\n"
                + "                filter (str(?a) < str(?b)) .\n"
                + "        }\n"
                + "}";
        transformGraph(graph, graph + "__X", q);
        deleteGraph(graph);
        copyGraph(graph + "__X", graph);
        deleteGraph(graph + "__X");
    }

    public void replaceSameAs(String data, String sameAs) throws RepositoryException, MalformedQueryException, UpdateExecutionException, MarmottaException {
        replaceSameAsSubject(data, data + "__S", sameAs);
        deleteGraph(data);
        replaceSameAsObjectExcept(data + "__S", data, sameAs, FOAF.HOLDS_ACCOUNT.toString());
        deleteGraph(data + "__S");
    }

}
