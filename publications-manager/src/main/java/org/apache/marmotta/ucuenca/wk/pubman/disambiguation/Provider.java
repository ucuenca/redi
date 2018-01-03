/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.disambiguation;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;

/**
 *
 * @author Jose Ortiz
 */
public class Provider {

    public String Name;
    public String Graph;

    private SparqlService sparql;

    public Provider(String Name, String Graph, SparqlService sparql) {
        this.Name = Name;
        this.Graph = Graph;
        this.sparql = sparql;
    }

    public List<Person> getAuthors() throws MarmottaException {
        String qry = "select ?a \n"
                + "{\n"
                + "	graph <" + Graph + "> {\n"
                + "  		?a a <http://xmlns.com/foaf/0.1/Person> . \n"
                //+ "  		?a <http://purl.org/dc/terms/provenance> <http://redi.cedia.edu.ec/resource/endpoint/file/UCUENCA> . \n"
                + "	}\n"
                + "}";
        List<Map<String, Value>> persons = sparql.query(QueryLanguage.SPARQL, qry);
        List<Person> lsp = new ArrayList<>();
        for (Map<String, Value> row : persons) {
            Person p = new Person();
            p.Origin = this;
            p.URI = row.get("a").stringValue();
            lsp.add(p);
        }
        return lsp;
    }

    public List<Person> getCandidates(String URI) throws MarmottaException {
        String qry = "select distinct ?a {\n"
                + "	graph <" + Graph + "> {\n"
                + "             values ?per { <" + URI + "> } . \n"
                + "  		?q <http://www.w3.org/2002/07/owl#oneOf> ?per .\n"
                + "      	?b <http://www.w3.org/2002/07/owl#oneOf> ?q .\n"
                + "      	?b <http://xmlns.com/foaf/0.1/publications> ?pu .\n"
                + "      	?a <http://xmlns.com/foaf/0.1/publications> ?pu .\n"
                + "	}\n"
                + "}";
        List<Map<String, Value>> persons = sparql.query(QueryLanguage.SPARQL, qry);
        List<Person> lsp = new ArrayList<>();
        for (Map<String, Value> row : persons) {
            Person p = new Person();
            p.Origin = this;
            p.URI = row.get("a").stringValue();
            lsp.add(p);
        }
        return lsp;
    }

    public void FillData(List<Person> lsa) throws MarmottaException {

        String qryName = "select distinct ?fun {\n"
                + "	graph <" + Graph + ">{\n"
                + "             values ?per1 { <URI> } . \n"
                + "        	?per1 <http://xmlns.com/foaf/0.1/name> ?fun .\n"
                + "    }\n"
                + "}";

        String qryName2 = "select distinct ?fn ?ln {\n"
                + "	graph <" + Graph + ">{\n"
                + "             values ?per1 { <URI> } . \n"
                + "          	?per1 <http://xmlns.com/foaf/0.1/givenName> ?fn .\n"
                + "          	?per1 <http://xmlns.com/foaf/0.1/familyName> ?ln .\n"
                + "    }\n"
                + "}";

        String qryCA = "prefix dct: <http://purl.org/dc/terms/>\n"
                + "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "select distinct ?fun ?fn ?ln {\n"
                + "     graph <" + Graph + ">{\n"
                + "             values ?per { <URI> } . \n"
                + "            ?per      foaf:publications  ?publication .\n"
                + "      		?publication  dct:contributor | dct:creator   ?p .\n"
                + "            filter (?p != ?per) .\n"
                + "          	optional { ?p foaf:name ?fun . }\n"
                + "       		optional { ?p foaf:givenName ?fn . }\n"
                + "          	optional { ?p foaf:familyName ?ln . }\n"
                + "    }\n"
                + "}";
        String qryP = "prefix dct: <http://purl.org/dc/terms/>\n"
                + "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "select distinct ?p {\n"
                + "     graph <" + Graph + ">{\n"
                + "             values ?per { <URI> } . \n"
                + "            ?per      foaf:publications  ?publication .\n"
                + "      		?publication  dct:title   ?p .\n"
                + "    }\n"
                + "}";
        String qryA = "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "prefix  schema: <http://schema.org/>\n"
                + "select distinct ?p {\n"
                + "     graph <" + Graph + ">{\n"
                + "             values ?per { <URI> } . \n"
                + "            ?per schema:memberOf ?o .\n"
                + "       		?o foaf:name ?p\n"
                + "    }\n"
                + "}";
        String qryT = "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "select distinct ?p {\n"
                + "     graph <" + Graph + ">{\n"
                + "             values ?per { <URI> } . \n"
                + "            ?per foaf:publications  ?publication .\n"
                + "       		?publication foaf:topic_interest ?po .\n"
                + "       		?po rdfs:label ?p\n"
                + "    }\n"
                + "}";

        for (Person n : lsa) {
            String qryName_ = qryName.replaceAll("URI", n.URI);
            String qryName2_ = qryName2.replaceAll("URI", n.URI);
            String qryCA_ = qryCA.replaceAll("URI", n.URI);
            String qryP_ = qryP.replaceAll("URI", n.URI);
            String qryA_ = qryA.replaceAll("URI", n.URI);
            String qryT_ = qryT.replaceAll("URI", n.URI);

            //get Names
            List<Map<String, Value>> rsName = sparql.query(QueryLanguage.SPARQL, qryName_);
            List<Map<String, Value>> rsName2 = sparql.query(QueryLanguage.SPARQL, qryName2_);
            rsName.addAll(rsName2);
            n.Name = new ArrayList<>();
            for (Map<String, Value> ar : rsName) {
                if (ar.get("fun") != null) {
                    n.Name.add(Lists.newArrayList(ar.get("fun").stringValue()));
                }
                if (ar.get("fn") != null && ar.get("ln") != null) {
                    ArrayList<String> names = Lists.newArrayList(ar.get("fn").stringValue());
                    names.add(ar.get("ln").stringValue());
                    n.Name.add(names);
                }
            }
            //get CA
            List<Map<String, Value>> rsCA = sparql.query(QueryLanguage.SPARQL, qryCA_);
            n.Coauthors = new ArrayList<>();
            for (Map<String, Value> ar : rsCA) {
                if (ar.get("fun") != null) {
                    n.Coauthors.add(Lists.newArrayList(ar.get("fun").stringValue()));
                }

                if (ar.get("fn") != null && ar.get("ln") != null) {
                    ArrayList<String> names = Lists.newArrayList(ar.get("fn").stringValue());
                    names.add(ar.get("ln").stringValue());
                    n.Coauthors.add(names);
                }
            }
            //get Publications
            List<Map<String, Value>> rsP = sparql.query(QueryLanguage.SPARQL, qryP_);
            n.Publications = new ArrayList<>();
            for (Map<String, Value> ar : rsP) {
                if (ar.get("p") != null) {
                    n.Publications.add(ar.get("p").stringValue());
                }
            }
            //get Affiliations
            List<Map<String, Value>> rsA = sparql.query(QueryLanguage.SPARQL, qryA_);
            n.Affiliations = new ArrayList<>();
            for (Map<String, Value> ar : rsA) {
                if (ar.get("p") != null) {
                    n.Affiliations.add(ar.get("p").stringValue());
                }
            }
            //get Topics
            List<Map<String, Value>> rsT = sparql.query(QueryLanguage.SPARQL, qryT_);
            n.Topics = new ArrayList<>();
            for (Map<String, Value> ar : rsT) {
                if (ar.get("p") != null) {
                    n.Topics.add(ar.get("p").stringValue());
                }
            }
        }
    }

}
