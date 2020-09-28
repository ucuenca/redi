/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.utils;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Provider;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.apache.marmotta.ucuenca.wk.commons.util.BoundedExecutor;
import org.apache.marmotta.ucuenca.wk.pubman.api.IdentificationManager;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;

/**
 *
 * @author cedia
 */
public class CentralGraphGenerator {

    final private String CentralGraph;
    private String BucketsGraph;
    private List<Provider> Providers;

    public CentralGraphGenerator(String CentralGraph, String BucketsGraph, List<Provider> Providers) {
        this.CentralGraph = CentralGraph;
        this.BucketsGraph = BucketsGraph;
        this.Providers = Providers;
    }

    public void run(final IdentificationManager s, final ExternalSPARQLService esp, org.slf4j.Logger log) throws Exception {
        String harvestedProvidersListx = "";
        for (Provider p : Providers) {
            harvestedProvidersListx += " <" + p.Graph + "> ";
        }
        final String harvestedProvidersList = harvestedProvidersListx;
        List<String> buckets = s.getBuckets(BucketType.author);
        final URI createURI = ValueFactoryImpl.getInstance().createURI(CentralGraph);
        int i = 0;
        BoundedExecutor threadPool = BoundedExecutor.getThreadPool(15);
        for (final String b : buckets) {
            i++;
            log.info("Raw data {} / {}", i, buckets.size());
            threadPool.submitTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        String q = "PREFIX redi: <https://redi.cedia.edu.ec/ont#>\n"
                                + "    insert {\n"
                                + "    graph <" + CentralGraph + "> {\n"
                                + "        ?a_n ?b ?c_n .\n"
                                + "    }\n"
                                + "}\n"
                                + "where {\n"
                                + "    select distinct (if (bound(?ax),?ax,?a) as ?a_n) ?b (if (bound(?cx),?cx,?c) as ?c_n) {\n"
                                + "        {\n"
                                + "            select distinct ?a ?b ?c ?ax ?cx {   \n"
                                + "                values ?g {  " + harvestedProvidersList + " } .\n"
                                + "                graph <" + s.getGraph() + "> {\n"
                                + "                    <" + b + "> redi:element ?a .\n"
                                + "                }\n"
                                + "                graph ?g {\n"
                                + "                    ?a ?b ?c .\n"
                                + "                    optional {\n"
                                + "                        graph <" + s.getGraph() + "> {\n"
                                + "                            ?ax redi:element ?a .\n"
                                + "                        }\n"
                                + "                    }\n"
                                + "                    optional {\n"
                                + "                        graph <" + s.getGraph() + "> {\n"
                                + "                            ?cx redi:element ?c .\n"
                                + "                            filter (?b != <http://xmlns.com/foaf/0.1/holdsAccount> ) .\n"
                                + "                        }\n"
                                + "                    }\n"
                                + "                }\n"
                                + "            }\n"
                                + "        } union {\n"
                                + "            select distinct ?a ?b ?c ?ax ?cx {   \n"
                                + "                values ?g { " + harvestedProvidersList + " } .\n"
                                + "                graph <" + s.getGraph() + "> {\n"
                                + "                    <" + b + "> redi:element ?a_ .\n"
                                + "                }\n"
                                + "                graph ?g {\n"
                                + "                    ?a_ ?b_ ?a .\n"
                                + "                    ?a ?b ?c .\n"
                                + "                    optional {\n"
                                + "                        graph <" + s.getGraph() + "> {\n"
                                + "                            ?ax redi:element ?a .\n"
                                + "                        }\n"
                                + "                    }\n"
                                + "                    optional {\n"
                                + "                        graph <" + s.getGraph() + "> {\n"
                                + "                            ?cx redi:element ?c .\n"
                                + "                            filter (?b != <http://xmlns.com/foaf/0.1/holdsAccount> ) .\n"
                                + "                        }\n"
                                + "                    }\n"
                                + "                }\n"
                                + "            }\n"
                                + "        } union {\n"
                                + "            select distinct ?a ?b ?c ?ax ?cx {   \n"
                                + "                values ?g { " + harvestedProvidersList + " } .\n"
                                + "                graph <" + s.getGraph() + "> {\n"
                                + "                    <" + b + "> redi:element ?a__ .\n"
                                + "                }\n"
                                + "                graph ?g {\n"
                                + "                    ?a__ ?b__ ?a_ .\n"
                                + "                    ?a_ ?b_ ?a .\n"
                                + "                    ?a ?b ?c .\n"
                                + "                    optional {\n"
                                + "                        graph <" + s.getGraph() + "> {\n"
                                + "                            ?ax redi:element ?a .\n"
                                + "                        }\n"
                                + "                    }\n"
                                + "                    optional {\n"
                                + "                        graph <" + s.getGraph() + "> {\n"
                                + "                            ?cx redi:element ?c .\n"
                                + "                            filter (?b != <http://xmlns.com/foaf/0.1/holdsAccount> ) .\n"
                                + "                        }\n"
                                + "                    }\n"
                                + "                }\n"
                                + "            }\n"
                                + "        }\n"
                                + "    }"
                                + "    }";
                        esp.getSparqlService().update(QueryLanguage.SPARQL, q);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
        threadPool.end();
    }

    public void replaceBuckets(IdentificationManager s, ExternalSPARQLService esp) throws Exception {

        String q = "PREFIX redi: <https://redi.cedia.edu.ec/ont#>\n"
                + "delete {\n"
                + "    graph <" + this.CentralGraph + "> {\n"
                + "        ?o ?p ?v .    \n"
                + "        ?s ?pr ?o .\n"
                + "    } \n"
                + "} insert {\n"
                + "    graph <" + this.CentralGraph + "> {\n"
                + "        ?b ?p ?v .    \n"
                + "        ?s ?pr ?b .\n"
                + "    } \n"
                + "} where {   \n"
                + "    graph <" + this.CentralGraph + "> {\n"
                + "        {\n"
                + "        	?o ?p ?v .    \n"
                + "            graph <" + s.getGraph() + "> {\n"
                + "             ?b redi:element ?o .\n"
                + "    		}\n"
                + "        } union {\n"
                + "            ?s ?pr ?o .\n"
                + "            filter ( ?pr != <" + FOAF.HOLDS_ACCOUNT.stringValue() + ">) .\n"
                + "            graph <" + s.getGraph() + "> {\n"
                + "             ?b redi:element ?o .\n"
                + "    		}\n"
                + "\n"
                + "        }\n"
                + "    }\n"
                + "}";
        esp.getSparqlService().update(QueryLanguage.SPARQL, q);

    }

}
