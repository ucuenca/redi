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
            //+ " values ?a {<https://redi.cedia.edu.ec/resource/authors/ESPE/oai-pmh/FUERTES__WALTER>}"
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
    String qry = "select distinct ?p {\n"
            + "	graph <" + Graph + "> {\n"
            + "  		?q <http://www.w3.org/2002/07/owl#oneOf> <URI> .\n"
            + "  		?p <http://www.w3.org/2002/07/owl#oneOf> ?q .\n"
            + "	}\n"
            + "}";
    List<Map<String, Value>> queries = sparql.query(QueryLanguage.SPARQL, qry.replaceAll("URI", URI));
    Set<String> candidatesURIs = new LinkedHashSet();
    for (Map<String, Value> mp : queries) {
      String queryURI = mp.get("p").stringValue();
      candidatesURIs.add(queryURI);
    }
    return getCandidates(candidatesURIs);
  }

  public List<Person> getCandidates(Set<String> candidatesURIs) throws MarmottaException {
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

    String qryName = "select * {\n"
            + "	{\n"
            + "		select ?fun {\n"
            + "			graph <" + Graph + ">{\n"
            + "		             values ?per1 { <URI> } . \n"
            + "		        	?per1 <http://xmlns.com/foaf/0.1/name> ?fun .\n"
            + "		        	 bind(strlen(?fun) as ?l) .\n"
            + "		    }\n"
            + "		} order by desc(?l) limit 1\n"
            + "	} union {\n"
            + "		select ?fn ?ln {\n"
            + "			graph <" + Graph + ">{\n"
            + "		             values ?per1 { <URI> } . \n"
            + "		          	?per1 <http://xmlns.com/foaf/0.1/givenName> ?fn .\n"
            + "		          	?per1 <http://xmlns.com/foaf/0.1/familyName> ?ln .\n"
            + "		        	 bind(strlen(concat(?fn,' ', ?ln)) as ?l) .\n"
            + "		    }\n"
            + "		} order by desc(?l) limit 1\n"
            + "	}\n"
            + "}";

    String qryPCA = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>				\n"
            + "\n"
            + "select * {\n"
            + "    {\n"
            + "        select ?fnx ?lnx {\n"
            + "            {\n"
            + "                select ?p (concat(GROUP_CONCAT(?fnx ; separator ='|||'), '|||') as ?fny) (concat(GROUP_CONCAT(?lnx ; separator ='|||'), '|||') as ?lny) {\n"
            + "                        select ?p ?fnx ?lnx {\n"
            + "                            graph <" + Graph + ">{		             \n"
            + "                                values ?per { <URI> } . \n"
            + "                                ?per      foaf:publications  ?px .\n"
            + "                                ?p      foaf:publications  ?px .\n"
            + "                                filter (?p != ?per) .\n"
            + "                                ?p <http://xmlns.com/foaf/0.1/givenName> ?fnx .\n"
            + "                                ?p <http://xmlns.com/foaf/0.1/familyName> ?lnx .\n"
            + "                                bind (strlen(concat(?fnx,' ', ?lnx)) as ?l ) .\n"
            + "                            }\n"
            + "                        } order by desc(?l)  \n"
            + "                } group by ?p\n"
            + "            } . \n"
            + "            bind( strbefore( ?fny, \"|||\" ) as ?fnx ) .\n"
            + "            bind( strbefore( ?lny, \"|||\" ) as ?lnx ) .\n"
            + "        } limit 25\n"
            + "    } union {\n"
            + "        select ?funx {\n"
            + "            {\n"
            + "                select ?p (concat(GROUP_CONCAT(?nx ; separator ='|||'), '|||') as ?ny) {\n"
            + "                        select ?p ?nx {\n"
            + "                            graph <" + Graph + ">{		             \n"
            + "                                values ?per { <URI> } . \n"
            + "                                ?per      foaf:publications  ?px .\n"
            + "                                ?p      foaf:publications  ?px .\n"
            + "                                filter (?p != ?per) .\n"
            + "                                ?p foaf:name ?nx .\n"
            + "                                bind (strlen(?nx) as ?l ) .\n"
            + "                            }\n"
            + "                        } order by desc(?l)  \n"
            + "                } group by ?p\n"
            + "            } . \n"
            + "            bind( strbefore( ?ny, \"|||\" ) as ?funx ) .\n"
            + "        } limit 25\n"
            + "    }\n"
            + "    filter ( bound(?funx) && ?funx!=\"\" || bound(?fnx) && ?fnx!=\"\" && bound(?lnx) && ?lnx!=\"\" ) ."
            + "} limit 25";

//    String qryPCAAsk = "prefix dct: <http://purl.org/dc/terms/>\n"
//            + "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
//            + "ask from <" + Graph + "> {\n"
//            + "				             values ?per { <URI> } . \n"
//            + "				            ?per      foaf:publications  ?px .\n"
//            + "				             ?p      foaf:publications  ?px .\n"
//            + "				            filter (?p != ?per) .\n"
//            + "				            ?p <http://xmlns.com/foaf/0.1/name> ?fn .\n"
//            + "} ";
    String qryP = "prefix dct: <http://purl.org/dc/terms/>\n"
            + "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "select distinct ?p {\n"
            + "     graph <" + Graph + ">{\n"
            + "             values ?per { <URI> } . \n"
            + "            ?per      foaf:publications  ?publication .\n"
            + "      		?publication  dct:title   ?p .\n"
            + "    }\n"
            + "} limit 50";
    String qryA = "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "prefix  schema: <http://schema.org/>\n"
            + "select distinct ?p {\n"
            + "     graph <" + Graph + ">{\n"
            + "             values ?per { <URI> } . \n"
            + "            ?per schema:memberOf ?o .\n"
            + "       		?o foaf:name | <http://eurocris.org/ontology/cerif#acronym> ?p\n"
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
            + "select ?o {\n"
            + "     graph <" + Graph + ">{\n"
            + "             values ?per { <URI> } . \n"
            + "            ?per scoro:hasORCID ?o .\n"
            + "    }\n"
            + "} limit 1";
    for (Person n : lsa) {
      String qryName_ = qryName.replaceAll("URI", n.URI);
      String qryCA_ = qryPCA.replaceAll("URI", n.URI);
      String qryP_ = qryP.replaceAll("URI", n.URI);
      String qryA_ = qryA.replaceAll("URI", n.URI);
      String qryT_ = qryT.replaceAll("URI", n.URI);
      //String nask = qryPCAAsk.replaceAll("URI", n.URI);
      String qryORCID_ = qryORCID.replaceAll("URI", n.URI);

      //get Names
      List<Map<String, Value>> rsName = sparql.query(QueryLanguage.SPARQL, qryName_);
      n.Name = new ArrayList<>();
      for (Map<String, Value> ar : rsName) {
        if (ar.get("fn") != null && ar.get("ln") != null) {
          ArrayList<String> names = Lists.newArrayList(ar.get("fn").stringValue());
          names.add(ar.get("ln").stringValue());
          n.Name.add(names);
        } else if (ar.get("fun") != null) {
          n.Name.add(Lists.newArrayList(ar.get("fun").stringValue()));
        }

      }
      //get CA
      n.Coauthors = new ArrayList<>();
      //if (sparql.ask(QueryLanguage.SPARQL, nask)) {
      List<Map<String, Value>> rsCA = sparql.query(QueryLanguage.SPARQL, qryCA_);
      for (Map<String, Value> ar : rsCA) {
        if (ar.get("fnx") != null && ar.get("lnx") != null) {
          ArrayList<String> names = Lists.newArrayList(ar.get("fnx").stringValue());
          names.add(ar.get("lnx").stringValue());
          n.Coauthors.add(names);
        } else if (ar.get("funx") != null) {
          n.Coauthors.add(Lists.newArrayList(ar.get("funx").stringValue()));
        }

      }
      //}

      //get Publications
      List<Map<String, Value>> rsP = sparql.query(QueryLanguage.SPARQL, qryP_);
      n.Publications = new ArrayList<>();
      for (Map<String, Value> ar : rsP) {
        if (ar.get("p") != null) {
          String aff = ar.get("p").stringValue();
          if (aff != null && !aff.trim().isEmpty()) {
            n.Publications.add(aff);
          }
        }
      }
      //get Affiliations
      List<Map<String, Value>> rsA = sparql.query(QueryLanguage.SPARQL, qryA_);
      n.Affiliations = new ArrayList<>();
      for (Map<String, Value> ar : rsA) {
        if (ar.get("p") != null) {
          String aff = ar.get("p").stringValue();
          if (aff != null && !aff.trim().isEmpty()) {
            n.Affiliations.add(aff);
          }
        }
      }
      //get Topics
//            List<Map<String, Value>> rsT = sparql.query(QueryLanguage.SPARQL, qryT_);
      n.Topics = new ArrayList<>();
//            for (Map<String, Value> ar : rsT) {
//                if (ar.get("p") != null) {
//                    n.Topics.add(ar.get("p").stringValue());
//                }
//            }
      //get ORCIDS
      List<Map<String, Value>> rsOR = sparql.query(QueryLanguage.SPARQL, qryORCID_);
      n.ORCIDs = new ArrayList<>();
      for (Map<String, Value> ar : rsOR) {
        if (ar.get("o") != null) {
          String aff = ar.get("o").stringValue();
          if (aff != null && !aff.trim().isEmpty()) {
            n.ORCIDs.add(aff);
          }
        }
      }
      //
      //n.Coauthors = NameUtils.uniqueName(n.Coauthors);
      //n.Publications = PublicationUtils.uniqueTitle(n.Publications);
      //
    }
  }

}
