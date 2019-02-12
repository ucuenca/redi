/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.disambiguation;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.utils.NameUtils;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.utils.PublicationUtils;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;

/**
 *
 * @author Jose Ortiz
 */
@SuppressWarnings("PMD")
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
                + "	}\n"
                + "}";
        List<Map<String, Value>> persons = sparql.query(QueryLanguage.SPARQL, qry);
        List<Person> lsp = new ArrayList<>();
        for (Map<String, Value> row : persons) {
            Person p = new Person();
            p.Origin = this;
            p.URI = row.get("a").stringValue();
            p.URIS.add(p.URI);
            lsp.add(p);
        }
        return lsp;
    }

    public List<Person> getAuthors(String uri) throws MarmottaException {
        String qry = "select ?a \n"
                + "{\n"
                + "	graph <" + Graph + "> {\n"
                + "  		values ?a {<" + uri + ">} . \n"
                + "	}\n"
                + "}";
        List<Map<String, Value>> persons = sparql.query(QueryLanguage.SPARQL, qry);
        List<Person> lsp = new ArrayList<>();
        for (Map<String, Value> row : persons) {
            Person p = new Person();
            p.Origin = this;
            p.URI = row.get("a").stringValue();
            p.URIS.add(p.URI);
            lsp.add(p);
        }
        return lsp;
    }

    public List<Person> getAuthorsByOrganization(String organization) throws MarmottaException {
        String qry = "select ?a \n"
                + "{\n"
                + "	graph <" + Graph + "> {\n"
                + "  		?a a <http://xmlns.com/foaf/0.1/Person> . \n"
                + "  		?a <http://schema.org/memberOf> ?o . \n"
                + "  		values ?o { <" + organization + "> } . \n"
                + "	}\n"
                + "}";
        List<Map<String, Value>> persons = sparql.query(QueryLanguage.SPARQL, qry);
        List<Person> lsp = new ArrayList<>();
        for (Map<String, Value> row : persons) {
            Person p = new Person();
            p.Origin = this;
            p.URI = row.get("a").stringValue();
            p.URIS.add(p.URI);
            lsp.add(p);
        }
        return lsp;
    }

    public List<Person> getCandidates(String URI) throws MarmottaException {
        String qry = "select ?q {\n"
                + "	graph <" + Graph + "> {\n"
                + "  		?q <http://www.w3.org/2002/07/owl#oneOf> <URI> .\n"
                + "	}\n"
                + "}";
        List<Map<String, Value>> queries = sparql.query(QueryLanguage.SPARQL, qry.replaceAll("URI", URI));
        Set<String> candidatesURIs = new LinkedHashSet();
        for (Map<String, Value> mp : queries) {
            String queryURI = mp.get("q").stringValue();
            List<Map<String, Value>> query = sparql.query(QueryLanguage.SPARQL, qry.replaceAll("URI", queryURI));
            for (Map<String, Value> mp2 : query) {
                String candidateURI = mp2.get("q").stringValue();
                candidatesURIs.add(candidateURI);
            }
        }
        List<Person> lsp = new ArrayList<>();
        for (String row : candidatesURIs) {
            Person p = new Person();
            p.Origin = this;
            p.URI = row;
            p.URIS.add(p.URI);
            lsp.add(p);
        }
        return lsp;
    }

    public boolean isHarvested(String URI) throws MarmottaException {
        String qry = "ask { \n"
                + "  graph <" + Graph + "> {\n"
                + "  	[] <http://www.w3.org/2002/07/owl#oneOf> <" + URI + "> .\n"
                + "  }\n"
                + "} ";
        return sparql.ask(QueryLanguage.SPARQL, qry);
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
        String qryPCA = "prefix dct: <http://purl.org/dc/terms/>\n"
                + "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "select distinct ?p {\n"
                + "     graph <" + Graph + ">{\n"
                + "             values ?per { <URI> } . \n"
                + "            ?per      foaf:publications  ?p .\n"
                + "    }\n"
                + "}";

        String qryPCAA = "prefix dct: <http://purl.org/dc/terms/>\n"
                + "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "select distinct ?p {\n"
                + "     graph <" + Graph + ">{\n"
                + "             values ?per { <URI> } . \n"
                + "             values ?pub { <PIRI> } . \n"
                + "             ?p      foaf:publications  ?pub .\n"
                + "            filter (?p != ?per) .\n"
                + "    }\n"
                + "} limit 20";

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
        String qryORCID = "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "prefix  scoro: <http://purl.org/spar/scoro/>\n"
                + "select distinct ?o {\n"
                + "     graph <" + Graph + ">{\n"
                + "             values ?per { <URI> } . \n"
                + "            ?per scoro:hasORCID ?o .\n"
                + "    }\n"
                + "}";
        for (Person n : lsa) {
            String qryName_ = qryName.replaceAll("URI", n.URI);
            String qryName2_ = qryName2.replaceAll("URI", n.URI);
            String qryCA_ = qryPCA.replaceAll("URI", n.URI);
            String qryP_ = qryP.replaceAll("URI", n.URI);
            String qryA_ = qryA.replaceAll("URI", n.URI);
            String qryT_ = qryT.replaceAll("URI", n.URI);
            String qryORCID_ = qryORCID.replaceAll("URI", n.URI);

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
            Set<String> lspub = new LinkedHashSet<>();
            for (Map<String, Value> ar : rsCA) {
                String pubURI = ar.get("p").stringValue();
                String qryCA_S = qryPCAA.replaceAll("URI", n.URI).replaceAll("PIRI", pubURI);
                List<Map<String, Value>> query = sparql.query(QueryLanguage.SPARQL, qryCA_S);
                for (Map<String, Value> arp : query) {
                    String pURI = arp.get("p").stringValue();
                    lspub.add(pURI);
                }
            }
            for (String arp : lspub) {
                List<List<String>> r = new ArrayList<>();
                String qryName_C = qryName.replaceAll("URI", arp);
                String qryName2_C = qryName2.replaceAll("URI", arp);
                List<Map<String, Value>> rsCAN1 = sparql.query(QueryLanguage.SPARQL, qryName_C);
                for (Map<String, Value> arN : rsCAN1) {
                    r.add(Lists.newArrayList(arN.get("fun").stringValue()));
                }
                List<Map<String, Value>> rsCAN2 = sparql.query(QueryLanguage.SPARQL, qryName2_C);
                for (Map<String, Value> arN : rsCAN2) {
                    ArrayList<String> names = Lists.newArrayList(arN.get("fn").stringValue());
                    names.add(arN.get("ln").stringValue());
                    r.add(names);
                }
                n.Coauthors.addAll(r);
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
            //get ORCIDS
            List<Map<String, Value>> rsOR = sparql.query(QueryLanguage.SPARQL, qryORCID_);
            n.ORCIDs = new ArrayList<>();
            for (Map<String, Value> ar : rsOR) {
                if (ar.get("o") != null) {
                    n.ORCIDs.add(ar.get("o").stringValue());
                }
            }
            //
            n.Coauthors = NameUtils.uniqueName(n.Coauthors);
            n.Publications = PublicationUtils.uniqueTitle(n.Publications);
            //
        }
    }

}
