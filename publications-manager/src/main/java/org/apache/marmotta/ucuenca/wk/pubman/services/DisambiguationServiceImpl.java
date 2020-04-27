/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.DisambiguationService;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Person;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Provider;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.utils.PublicationUtils;
import org.apache.marmotta.ucuenca.wk.commons.function.Cache;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.apache.marmotta.ucuenca.wk.commons.service.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.commons.util.BoundedExecutor;
import org.apache.marmotta.ucuenca.wk.commons.util.LongUpdateQueryExecutor;
import org.apache.marmotta.ucuenca.wk.commons.util.ModifiedJaccardMod;
import org.apache.marmotta.ucuenca.wk.commons.util.SPARQLUtils;
import org.openrdf.model.Model;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.semarglproject.vocab.RDF;
import org.semarglproject.vocab.RDFS;

/**
 *
 * @author Jose Ortiz
 */
@ApplicationScoped
public class DisambiguationServiceImpl implements DisambiguationService {

  final int MAXTHREADS = 5;

  @Inject
  private org.slf4j.Logger log;

  @Inject
  private ConstantService constantService;

  @Inject
  private ExternalSPARQLService sparqlService;
  @Inject
  private QueriesService queriesService;

  @Inject
  private CommonsServices commonsServices;

  @Inject
  private TaskManagerService taskManagerService;

  @Inject
  private SparqlFunctionsService sparqlFunctionsService;

  //@Inject
  //private SesameService sesameService;
  private Task task;

  private Thread DisambiguationWorker;
  private Thread CentralGraphWorker;

  private static final String TEMP_CA = "1";
  private static final String SAFE_MERGE = "2";
  private static final String FIX_MERGE = "2Fix";
  private static final String FINAL_MERGE = "F";

  private String queryMatches(String org) throws MarmottaException, RepositoryException {

    String querymatchs = "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
            + "SELECT     ?org ?nauthor "
            + "WHERE {\n"
            + "        GRAPH <" + constantService.getSameAuthorsGraph() + "> {                \n"
            + "        Select ?org (COUNT(Distinct ?author) as ?nauthor) where {\n"
            + "            ?author owl:sameAs  ?c . \n"
            + "           GRAPH <" + constantService.getAuthorsGraph() + "> {\n"
            + "        ?author dct:provenance ?end\n"
            + "        }             \n"
            + "          GRAPH <" + constantService.getEndpointsGraph() + "> {\n"
            + "        ?end  <" + REDI.BELONGTO.toString() + ">  ?org .\n"
            + "        } "
            + "        VALUES ?org {<" + org + ">}    "
            + "        }GROUP BY ?org  "
            + "        }      "
            + "} limit 1";
    log.info("Query mathc " + querymatchs);
    List<Map<String, Value>> response = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, querymatchs);
    return response.get(0).get("nauthor").stringValue();

  }

  private void registerDate(String org, Provider prov, String status, String n) throws MarmottaException {

    Date date = new Date();
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    String uriEvent = createEventUri(prov.Name, org.substring(org.lastIndexOf("/") + 1));
    sparqlFunctionsService.executeInsert(prov.Graph, uriEvent, RDF.TYPE, REDI.DISAMBIGUATION_EVENT.toString());
    sparqlFunctionsService.executeInsert(prov.Graph, prov.Graph, REDI.BELONGTO.toString(), uriEvent);
    sparqlFunctionsService.executeInsert(constantService.getOrganizationsGraph(), org, REDI.BELONGTO.toString(), uriEvent);
//         sparqlFunctionsService.executeInsert(prov.Graph, uriEvent, REDI.EXTRACTIONDATE.toString(), dateFormat.format(date), STR);
    sparqlFunctionsService.executeInsert(prov.Graph, uriEvent, RDFS.LABEL, dateFormat.format(date) + " | " + status + " " + n + "  matchs find", "string");
    log.info("Finish  register");
  }

  private String createEventUri(String prov, String org) {
    return constantService.getEndpointBaseEvent() + "disambiguation/" + prov.replace(' ', '_') + "_" + org.replace(' ', '_');

  }

  private void updateLogs(List<Map<String, Map<Provider, Integer>>> providersResult) throws MarmottaException, RepositoryException {
    for (Map<String, Map<Provider, Integer>> mp : providersResult) {
      for (Entry<String, Map<Provider, Integer>> orgmap : mp.entrySet()) {
        for (Entry<Provider, Integer> prov : orgmap.getValue().entrySet()) {
          if (prov.getValue() > 0) {
            String n = queryMatches(orgmap.getKey());
            registerDate(orgmap.getKey(), prov.getKey(), "Success", n);
          }
        }

      }
    }
  }

  private List<Provider> getProviders() throws MarmottaException, RepositoryException {
    List<Provider> Providers = new ArrayList<>();
    Providers.add(new Provider("Authors", constantService.getAuthorsProviderGraph(), sparqlService.getSparqlService()));
    Providers.add(new Provider("Scopus", constantService.getScopusGraph(), sparqlService.getSparqlService()));
    Providers.add(new Provider("ORCID", constantService.getORCIDGraph(), sparqlService.getSparqlService()));
    Providers.add(new Provider("DOAJ", constantService.getDOAJGraph(), sparqlService.getSparqlService()));
    Providers.add(new Provider("Scholar", constantService.getGoogleScholarGraph(), sparqlService.getSparqlService()));
    Providers.add(new Provider("Academics", constantService.getAcademicsKnowledgeGraph(), sparqlService.getSparqlService()));
    Providers.add(new Provider("Scielo", constantService.getScieloGraph(), sparqlService.getSparqlService()));
    Providers.add(new Provider("DBLP", constantService.getDBLPGraph(), sparqlService.getSparqlService()));
    Providers.add(new Provider("Springer", constantService.getSpringerGraph(), sparqlService.getSparqlService()));
    Providers.add(new Provider("Crossref", constantService.getCrossrefGraph(), sparqlService.getSparqlService()));
    return Providers;
  }

  @Override
  public void Process(String[] orgs) {
    try {
      SPARQLUtils sparqlUtils = new SPARQLUtils(sparqlService.getSparqlService());
      task = taskManagerService.createSubTask(String.format("%s Disambiguation", "Author"), "Disambiguation Process");
      InitAuthorsProvider();
      List<Provider> Providers = getProviders();
      List<Map<String, Map<Provider, Integer>>> providersResult = new ArrayList();
      if (orgs != null) {
        for (String org : orgs) {
          task.updateMessage(String.format("Disambiguate  author from %s organization", org));
          log.debug("Procesing" + org);
          Map<String, Map<Provider, Integer>> mp = new HashMap();
          Map<Provider, Integer> ProvidersElements = ProcessAuthors(Providers, org);
          mp.put(org, ProvidersElements);
          providersResult.add(mp);
        }
      } else {
//                ProcessAuthors(Providers, null);
//                sparqlService.getGraphDBInstance().dumpBuffer();
      }
      for (int w0 = 0; w0 < 4; w0++) {
        ProcessCoauthors(Providers, true);
        sparqlService.getGraphDBInstance().dumpBuffer();
        sparqlUtils.addAll(constantService.getAuthorsSameAsGraph(), constantService.getAuthorsSameAsGraph() + TEMP_CA);
        sparqlUtils.deleteGraph(constantService.getAuthorsSameAsGraph() + TEMP_CA);
      }
//            mergeAuthors();
//            sparqlService.getGraphDBInstance().dumpBuffer();
////      /**/
//            sparqlUtils.clearSameAs(constantService.getAuthorsSameAsGraph(), constantService.getAuthorsSameAsGraph() + FIX_MERGE);
//            sparqlUtils.replaceSameAsSubject(constantService.getAuthorsSameAsGraph(), constantService.getAuthorsSameAsGraph() + FINAL_MERGE, constantService.getAuthorsSameAsGraph() + SAFE_MERGE);
//            sparqlUtils.deleteGraph(constantService.getAuthorsSameAsGraph());
//            sparqlUtils.copyGraph(constantService.getAuthorsSameAsGraph() + FINAL_MERGE, constantService.getAuthorsSameAsGraph());
//            sparqlUtils.deleteGraph(constantService.getAuthorsSameAsGraph() + FINAL_MERGE);
//////      /**/
//            ProcessCoauthors(Providers, false);
//            sparqlService.getGraphDBInstance().dumpBuffer();
//            ProcessPublications(Providers);
//            sparqlService.getGraphDBInstance().dumpBuffer();
    } catch (Exception ex) {
      try {
        sparqlService.getGraphDBInstance().dumpBuffer();
      } catch (Exception ex1) {
        ex1.printStackTrace();
      }
      log.error("Unknown error while disambiguating");
      ex.printStackTrace();
    }
    taskManagerService.endTask(task);
  }

  public void subjectsMerger(List<Provider> AuthorsProviderslist) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException, RepositoryException, RDFHandlerException {
    String providersGraphs = "  ";
    for (Provider aProvider : AuthorsProviderslist) {
      providersGraphs += " <" + aProvider.Graph + "> ";
    }
    String q = "select distinct ?s ?l {\n"
            + "    graph <" + constantService.getPublicationsSameAsGraph() + "> {\n"
            + "        ?h <http://www.w3.org/2002/07/owl#sameAs> ?p .\n"
            + "    }\n"
            + "    values ?g { " + providersGraphs + " } .\n"
            + "    graph ?g {\n"
            + "        ?p <http://purl.org/dc/terms/subject> ?s .\n"
            + "        ?s <http://www.w3.org/2000/01/rdf-schema#label> ?l .\n"
            + "    }\n"
            + "}";
    List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, q);
    Map<String, String> group = new HashMap<>();
    for (Map<String, Value> aa : query) {
      if (!group.containsKey(aa.get("s").stringValue())) {
        group.put(aa.get("s").stringValue(), aa.get("l").stringValue());
      }
    }

    ArrayList<Map.Entry<String, String>> arrayList = new ArrayList(group.entrySet());
    for (int i = 0; i < arrayList.size(); i++) {
      Entry<String, String> get = arrayList.get(i);
      String toUpperCase = ModifiedJaccardMod.specialCharactersClean(get.getValue()).replaceAll("\\s+", "").trim().toUpperCase();
      String PossibleNewURI = constantService.getSubjectResource() + Cache.getMD5(toUpperCase);
      registerSameAs(constantService.getAuthorsSameAsGraph(), PossibleNewURI, get.getKey());
    }
  }

  public void compareSubSet(Person a, Person b, Map<String, Set<String>> g, Set<String> usedT) {
    Boolean checkName = a.checkName(b, true);
    if (checkName != null && checkName) {
      if (!g.containsKey(a.URI)) {
        g.put(a.URI, new HashSet<String>());
      }
      g.get(a.URI).add(b.URI);
      g.get(a.URI).add(a.URI);
      usedT.add(a.URI);
      usedT.add(b.URI);
    }
  }

  public boolean compareSubSet(Entry<String, Set<String>> a, Entry<String, Set<String>> b, Map<String, Set<String>> g) {
    boolean f = false;
    if (a.getValue().containsAll(b.getValue())) {
      if (a.getValue().containsAll(b.getValue()) && b.getValue().containsAll(a.getValue())) {
        f = true;
      }
      if (!g.containsKey(a.getKey())) {
        g.put(a.getKey(), new HashSet<String>());
      }
      g.get(a.getKey()).add(b.getKey());
    }
    return f;
  }

  public Map<String, Set<String>> clearGroups(Map<String, Set<String>> g) {
    Map<String, Set<String>> gn = new HashMap<>(g);
    Set<Entry<String, Set<String>>> entrySet = g.entrySet();
    List<Entry<String, Set<String>>> ls = new ArrayList<>(entrySet);
    Map<String, Set<String>> sa = new HashMap<>();
    for (int i = 0; i < ls.size(); i++) {
      for (int j = i + 1; j < ls.size(); j++) {
        Entry<String, Set<String>> get = ls.get(i);
        Entry<String, Set<String>> get1 = ls.get(j);
        boolean compareSubSet = compareSubSet(get, get1, sa);
        if (!compareSubSet) {
          compareSubSet(get1, get, sa);
        }
      }
    }
    Set<String> rm = new HashSet<>();
    for (Entry<String, Set<String>> s : sa.entrySet()) {
      Set<String> get = gn.get(s.getKey());
      for (String ss : s.getValue()) {
        get.addAll(gn.get(ss));
        if (s.getKey().compareTo(ss) != 0) {
          rm.add(ss);
        }
      }
    }
    for (String ss : rm) {
      gn.remove(ss);
    }
    return gn;
  }

  public static Set<String> intersection(Set<String> a, Set<String> b) {
    // unnecessary; just an optimization to iterate over the smaller set
    if (a.size() > b.size()) {
      return intersection(b, a);
    }

    Set<String> results = new HashSet<>();

    for (String element : a) {
      if (b.contains(element)) {
        results.add(element);
      }
    }

    return results;
  }

  public Set<String> getAmb(Map<String, Set<String>> g) {
    Set<String> am = new HashSet<>();
    Set<Entry<String, Set<String>>> entrySet = g.entrySet();
    List<Entry<String, Set<String>>> ls = new ArrayList<>(entrySet);
    for (int i = 0; i < ls.size(); i++) {
      for (int j = i + 1; j < ls.size(); j++) {
        Entry<String, Set<String>> get = ls.get(i);
        Entry<String, Set<String>> get1 = ls.get(j);
        am.addAll(intersection(get.getValue(), get1.getValue()));
      }
    }
    return am;
  }

  public void mergeAuthors() throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException, RepositoryException, RDFHandlerException {
    String qryDisambiguatedCoauthors = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "select distinct ?a  { \n"
            + "  graph <" + constantService.getAuthorsSameAsGraph() + "> { \n"
            //                + "     values ?a { <https://redi.cedia.edu.ec/resource/authors/SENESCYT/oai-pmh/WONG_DE_BALZAR__SARA> } ."
            + "    ?a <http://www.w3.org/2002/07/owl#sameAs> [] . \n"
            + "  }\n"
            + "}";

    List<Map<String, Value>> queryResponsec = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, qryDisambiguatedCoauthors);
    int ixx = 0;
    for (Map<String, Value> rq : queryResponsec) {
      log.info("merging {} - {}", ixx, rq.get("a").stringValue());
      ixx++;
      do {
        try {
          String group_ = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                  + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                  + "\n"
                  + "select distinct ?rr {\n"
                  + "    {\n"
                  + "        select distinct ?rr {\n"
                  + "    {\n"
                  + "        select distinct ?rr {\n"
                  + "    {\n"
                  + "        select (?b as ?rr)  {\n"
                  + "            graph <" + constantService.getAuthorsSameAsGraph() + "> {\n"
                  + "				bind (<" + rq.get("a").stringValue() + "> as ?ssd) .\n"
                  + "        		{\n"
                  + "            		?ssd owl:sameAs* ?b .\n"
                  + "        		} union {\n"
                  + "            		?b owl:sameAs* ?ssd  .\n"
                  + "        		} union {\n"
                  + "            		?ssd owl:sameAs* ?b .\n"
                  + "            		?x owl:sameAs* ?b .\n"
                  + "        		} union {\n"
                  + "            		?ssd owl:sameAs* ?b .\n"
                  + "            		?b owl:sameAs* ?x .\n"
                  + "        		} union {\n"
                  + "            		?b owl:sameAs* ?ssd  .\n"
                  + "            		?x owl:sameAs* ?b .\n"
                  + "        		} union {\n"
                  + "            		?b owl:sameAs* ?ssd  .\n"
                  + "            		?b owl:sameAs* ?x .\n"
                  + "        		}	\n"
                  + "        	}\n"
                  + "		}\n"
                  + "    } union {\n"
                  + "        select (?x as ?rr)  {\n"
                  + "            graph <" + constantService.getAuthorsSameAsGraph() + "> {\n"
                  + "                bind (<" + rq.get("a").stringValue() + "> as ?ssd) .\n"
                  + "        		{\n"
                  + "            		?ssd owl:sameAs* ?b .\n"
                  + "        		} union {\n"
                  + "            		?b owl:sameAs* ?ssd  .\n"
                  + "        		} union {\n"
                  + "            		?ssd owl:sameAs* ?b .\n"
                  + "            		?x owl:sameAs* ?b .\n"
                  + "        		} union {\n"
                  + "            		?ssd owl:sameAs* ?b .\n"
                  + "            		?b owl:sameAs* ?x .\n"
                  + "        		} union {\n"
                  + "            		?b owl:sameAs* ?ssd  .\n"
                  + "            		?x owl:sameAs* ?b .\n"
                  + "        		} union {\n"
                  + "            		?b owl:sameAs* ?ssd  .\n"
                  + "            		?b owl:sameAs* ?x .\n"
                  + "        		}	\n"
                  + "        	}\n"
                  + "		}\n"
                  + "    }\n"
                  + "}\n"
                  + "    } .\n"
                  + "    filter (str(?rr)!='' ) .\n"
                  + "}\n"
                  + "    }  .\n"
                  + "    graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                  + "        ?rr a foaf:Person .\n"
                  + "    }\n"
                  + "} order by ?rr\n"
                  + "";
          List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, group_);
          Set<String> myGroup = new HashSet<>();
          String groq = "";
          for (Map<String, Value> rqq : query) {
            myGroup.add(rqq.get("rr").stringValue());
            groq += "<" + rqq.get("rr").stringValue() + "> ";
          }
          String qryDisambiguatedCoauthors2 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                  + "select ?a ?n ?fn ?ln { \n"
                  + "  graph <" + constantService.getAuthorsProviderGraph() + "> { \n"
                  + "  values ?a { " + groq + " } . \n"
                  + "    optional { ?a <http://xmlns.com/foaf/0.1/name> ?n . }\n"
                  + "    optional { ?a <http://xmlns.com/foaf/0.1/givenName> ?fn . }\n"
                  + "    optional { ?a <http://xmlns.com/foaf/0.1/familyName> ?ln . }\n"
                  + "  }\n"
                  + "}";
          List<Map<String, Value>> rx = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, qryDisambiguatedCoauthors2);
          Map<String, Person> persons = getPersons(rx);
          Set<String> usedT = new HashSet<>();
          Map<String, Set<String>> groups = new HashMap<>();
          List<String> ls = new ArrayList<>(myGroup);
          for (int i = 0; i < ls.size(); i++) {
            for (int j = i + 1; j < ls.size(); j++) {
              Person get1 = persons.get(ls.get(i));
              Person get2 = persons.get(ls.get(j));
              compareSubSet(get1, get2, groups, usedT);
              compareSubSet(get2, get1, groups, usedT);
            }
          }
          Set<String> alones = new HashSet<>(myGroup);
          alones.removeAll(usedT);
          for (String a : alones) {
            groups.put(a, new HashSet<String>());
            groups.get(a).add(a);
          }
          int size;
          do {
            size = groups.size();
            groups = clearGroups(groups);
          } while (groups.size() < size);
          Set<String> amb = getAmb(groups);
          for (Entry<String, Set<String>> next : groups.entrySet()) {
            for (String next1 : next.getValue()) {
              if (amb.contains(next.getKey()) || amb.contains(next1)) {
                registerSameAs(constantService.getAuthorsSameAsGraph() + FIX_MERGE, next.getKey(), next1);
              } else {
                registerSameAs(constantService.getAuthorsSameAsGraph() + SAFE_MERGE, next.getKey(), next1);
              }
            }
          }
          break;
        } catch (Exception e) {
          e.printStackTrace();
          log.info("Retrying {} - {}", ixx, rq.get("a").stringValue());
        }
      } while (true);
    }

  }

  public Set<Set<String>> getGroupsAuthors(List<Map<String, Value>> query) {
    Set<Set<String>> g = new HashSet<>();
    Set<Map<String, Value>> usedT = new HashSet<>();
    for (Map<String, Value> a : query) {
      if (!usedT.contains(a)) {
        usedT.add(a);
        Set<String> ans = new HashSet<>();
        String UA = a.get("a").stringValue();
        String UB = a.get("b").stringValue();
        ans.add(UA);
        ans.add(UB);
        int size;
        do {
          size = ans.size();
          for (Map<String, Value> b : query) {
            if (!usedT.contains(b)) {
              String UAx = b.get("a").stringValue();
              String UBx = b.get("b").stringValue();
              if (ans.contains(UAx) || ans.contains(UBx)) {
                usedT.add(b);
                ans.add(UAx);
                ans.add(UBx);
              }
            }
          }
        } while (ans.size() > size);
        g.add(ans);
      }
    }

    return g;
  }

  public void InitAuthorsProvider() throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException, RepositoryException {
    boolean ask = sparqlService.getSparqlService().ask(QueryLanguage.SPARQL, "ask from <" + constantService.getAuthorsProviderGraph() + "> { ?a ?b ?c }");
    if (ask) {
      return;
    }
    //copy
    SPARQLUtils sparqlUtils = new SPARQLUtils(sparqlService.getSparqlService());
    sparqlUtils.addAll(constantService.getAuthorsProviderGraph(), constantService.getAuthorsGraph());

    //delete provider triple
    String deleteProviderType = "delete where {\n"
            + "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
            + "		?a a <http://ucuenca.edu.ec/ontology#Provider> .\n"
            + "	}\n"
            + "}";
    sparqlService.getSparqlService().update(QueryLanguage.SPARQL, deleteProviderType);

    //givName
    new LongUpdateQueryExecutor(sparqlService.getSparqlService(),
            "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
            + "		?a foaf:firstName ?c .\n"
            + "	}\n",
            "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
            + "		?a foaf:givenName ?c .\n"
            + "	}\n", null, "prefix foaf: <http://xmlns.com/foaf/0.1/>\n", "?a ?c").execute();
    //famName
    new LongUpdateQueryExecutor(sparqlService.getSparqlService(),
            "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
            + "		?a foaf:lastName ?c .\n"
            + "	}\n",
            "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
            + "		?a foaf:familyName ?c .\n"
            + "	}\n", null, "prefix foaf: <http://xmlns.com/foaf/0.1/>\n", "?a ?c").execute();

    //org
    new LongUpdateQueryExecutor(sparqlService.getSparqlService(),
            "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
            + "		?a <http://purl.org/dc/terms/provenance> ?p .\n"
            + "	}\n"
            + "	graph <" + constantService.getEndpointsGraph() + "> {\n"
            + "		?p <http://ucuenca.edu.ec/ontology#belongTo> ?o .\n"
            + "	}\n",
            "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
            + "		?a schema:memberOf ?o .\n"
            + "	}\n", null, "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "prefix  schema: <http://schema.org/>\n", "?a ?o").execute();

    new LongUpdateQueryExecutor(sparqlService.getSparqlService(),
            "	graph <" + constantService.getOrganizationsGraph() + "> {\n"
            + "		?o <http://ucuenca.edu.ec/ontology#fullName> ?n .\n"
            + "		?o <http://ucuenca.edu.ec/ontology#name> ?nn .\n"
            + "	}\n",
            "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
            + "		?o a foaf:Organization .\n"
            + "		?o <http://ucuenca.edu.ec/ontology#memberOf> <https://redi.cedia.edu.ec/> .\n"
            + "		?o foaf:name ?n .\n"
            + "		?o foaf:name ?nn .\n"
            + "	}\n", null, "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "prefix  schema: <http://schema.org/>\n", "?o ?n ?nn").execute();

    //alias
    String orgAlias = "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "prefix  schema: <http://schema.org/>\n"
            + "select ?o ?n  {\n"
            + "	graph <" + constantService.getOrganizationsGraph() + "> {\n"
            + "		?o <http://ucuenca.edu.ec/ontology#alias> ?n .\n"
            + "	}\n"
            + "}";
    List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, orgAlias);
    for (Map<String, Value> ar : query) {
      String URI = ar.get("o").stringValue();
      String[] split = ar.get("n").stringValue().split(";");
      for (int k = 0; k < split.length; k++) {
        split[k] = split[k].trim();
      }
      ArrayList<String> newArrayList = Lists.newArrayList(split);
      newArrayList.removeAll(Arrays.asList("", null));
      for (String nn : newArrayList) {
        String buildInsertQuery = buildInsertQuery(constantService.getAuthorsProviderGraph(), URI, "http://xmlns.com/foaf/0.1/name", nn);
        sparqlService.getSparqlService().update(QueryLanguage.SPARQL, buildInsertQuery);
      }
    }

    String q = "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "insert {\n"
            + "    graph <https://redi.cedia.edu.ec/context/fi> { \n"
            + "            ?p dct:title ?t .\n"
            + "    } \n"
            + "} where {    \n"
            + "    graph <" + constantService.getAuthorsProviderGraph() + "> { \n"
            + "        ?p dct:title ?t_ .\n"
            + "        bind (replace (replace (lcase(?t_), 'ü|ñ|á|é|í|ó|ú|a|e|i|o|u|,|;|:|-|\\\\(|\\\\)|\\\\||\\\\.' ,' '), ' ' ,'') as ?t) .\n"
            + "    } \n"
            + "} ";
    String q2 = "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "insert {\n"
            + "    graph <https://redi.cedia.edu.ec/context/f1> { \n"
            + "            ?p dct:title ?t .\n"
            + "    } \n"
            + "} where {\n"
            + "    {\n"
            + "        select ?p { \n"
            + "            graph <https://redi.cedia.edu.ec/context/fi> { \n"
            + "                    ?p dct:title ?t .\n"
            + "            }\n"
            + "        } group by ?p having (count(distinct ?t) > 1 )\n"
            + "    } .\n"
            + "    graph <https://redi.cedia.edu.ec/context/fi> { \n"
            + "        ?p dct:title ?t .\n"
            + "    }\n"
            + "}";
    String q3 = "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "insert {\n"
            + "    graph <https://redi.cedia.edu.ec/context/f2> { \n"
            + "            ?p dct:title ?t .\n"
            + "    } \n"
            + "} where  {\n"
            + "    {\n"
            + "        select ?t { \n"
            + "            graph <https://redi.cedia.edu.ec/context/f1> { \n"
            + "                    ?p dct:title ?t .\n"
            + "            }\n"
            + "        } group by ?t having (count(?p) > 1 )\n"
            + "    } .\n"
            + "    graph <https://redi.cedia.edu.ec/context/f1> { \n"
            + "        ?p dct:title ?t .\n"
            + "    }\n"
            + "}";
    String q4 = "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "insert{\n"
            + "    graph <https://redi.cedia.edu.ec/context/f3> { \n"
            + "        ?p dct:title ?p .\n"
            + "    }\n"
            + "} where {\n"
            + "    select ?p  {\n"
            + "        graph <https://redi.cedia.edu.ec/context/f2> { \n"
            + "            ?p dct:title ?t1 .\n"
            + "        }\n"
            + "        graph <https://redi.cedia.edu.ec/context/f1> { \n"
            + "            ?p dct:title ?t2 .\n"
            + "        }\n"
            + "    } group by ?p having ((count(distinct ?t1) ) = (count(distinct ?t2)))\n"
            + "}";
    String q5 = "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "delete{\n"
            + "    graph <https://redi.cedia.edu.ec/context/f2> { \n"
            + "        ?p dct:title ?t .\n"
            + "    }\n"
            + "} where {\n"
            + "        graph <https://redi.cedia.edu.ec/context/f3> { \n"
            + "            ?p dct:title [] .\n"
            + "        }\n"
            + "        graph <https://redi.cedia.edu.ec/context/f2> { \n"
            + "            ?p dct:title ?t .\n"
            + "        }\n"
            + "}";
    String q6 = "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "delete{\n"
            + "    graph <https://redi.cedia.edu.ec/context/f3> { \n"
            + "        ?p dct:title ?p .\n"
            + "    }\n"
            + "}insert {\n"
            + "    graph <https://redi.cedia.edu.ec/context/f2> { \n"
            + "        ?p dct:title ?t .\n"
            + "    }\n"
            + "} where {\n"
            + "    {\n"
            + "        select ?t { \n"
            + "            graph <https://redi.cedia.edu.ec/context/f1> { \n"
            + "                    ?p dct:title ?t .\n"
            + "            }\n"
            + "        } group by ?t having (count(?p) > 2 )\n"
            + "    } .\n"
            + "    graph <https://redi.cedia.edu.ec/context/f1> { \n"
            + "        ?p dct:title ?t .\n"
            + "    }\n"
            + "    graph <https://redi.cedia.edu.ec/context/f3> { \n"
            + "        ?p dct:title ?p .\n"
            + "    }\n"
            + "}";
    String q7 = "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "delete {\n"
            + "    graph <https://redi.cedia.edu.ec/context/f1> { \n"
            + "            ?p dct:title ?t .\n"
            + "    }\n"
            + "} where {\n"
            + "    graph <https://redi.cedia.edu.ec/context/f2> { \n"
            + "            ?p dct:title ?t .\n"
            + "    }\n"
            + "}";
    String q8 = "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "insert {\n"
            + "    graph <https://redi.cedia.edu.ec/context/f1x> { \n"
            + "            ?p dct:title ?t .\n"
            + "    } \n"
            + "} where {\n"
            + "    {\n"
            + "        select ?p { \n"
            + "            graph <https://redi.cedia.edu.ec/context/f1> { \n"
            + "                    ?p dct:title ?t .\n"
            + "            }\n"
            + "        } group by ?p having (count(distinct ?t) > 1 )\n"
            + "    } .\n"
            + "    graph <https://redi.cedia.edu.ec/context/f1> { \n"
            + "        ?p dct:title ?t .\n"
            + "    }\n"
            + "}";
    String q9 = "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "delete {\n"
            + "    graph <https://redi.cedia.edu.ec/context/f1> { \n"
            + "            ?p dct:title ?t2 .\n"
            + "    }\n"
            + "}\n"
            + "insert{\n"
            + "    graph <https://redi.cedia.edu.ec/context/f2> { \n"
            + "            ?p dct:title ?t2 .\n"
            + "    }\n"
            + "}\n"
            + "where {\n"
            + "    graph <https://redi.cedia.edu.ec/context/f1x> { \n"
            + "            ?p dct:title ?t1 .\n"
            + "            ?p dct:title ?t2 .\n"
            + "            bind(strlen(str(?t1)) as ?lt1) .\n"
            + "            bind(strlen(str(?t2)) as ?lt2) .\n"
            + "            bind(?lt2 / ?lt1 as ?tot ) .\n"
            + "            filter (?lt1 > ?lt2) .\n"
            + "            filter (?tot < 0.50) .\n"
            + "    } \n"
            + "} ";
    String q10 = "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "insert{\n"
            + "    graph <https://redi.cedia.edu.ec/context/f2fix> {\n"
            + "        ?p dct:title ?t_ .\n"
            + "    }\n"
            + "} where {\n"
            + "    graph <https://redi.cedia.edu.ec/context/f2> { \n"
            + "            ?p dct:title ?t .\n"
            + "    }\n"
            + "    graph <" + constantService.getAuthorsProviderGraph() + "> { \n"
            + "        ?p dct:title ?t_ .\n"
            + "        bind (replace (replace (lcase(?t_), 'ü|ñ|á|é|í|ó|ú|a|e|i|o|u|,|;|:|-|\\\\(|\\\\)|\\\\||\\\\.' ,' '), ' ' ,'') as ?t) .\n"
            + "    } \n"
            + "} ";
    String q11f = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "PREFIX bibo: <http://purl.org/ontology/bibo/>\n"
            + "delete {\n"
            + "    graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
            + "      ?p dct:title ?tq .\n"
            + "    }\n"
            + "}\n"
            + "insert {\n"
            + "  graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
            + "    ?r a bibo:Journal .\n"
            + "    ?r rdfs:label ?tq .\n"
            + "    ?p dct:isPartOf ?r .\n"
            + "  }\n"
            + "} where {\n"
            + "    graph <https://redi.cedia.edu.ec/context/f2fix> {\n"
            + "          ?p dct:title ?tq .\n"
            + "    }\n"
            + "    graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
            + "          ?p dct:title ?tq .\n"
            + "          bind(md5(replace (replace (lcase(?tq), 'ü|ñ|á|é|í|ó|ú|a|e|i|o|u|,|;|:|-|\\\\(|\\\\)|\\\\||\\\\.' ,' '), ' ' ,'')) as ?t).\n"
            + "          bind ( iri(concat('https://redi.cedia.edu.ec/resource/journal/',?t)) as ?r ) .\n"
            + "    }\n"
            + "}";

    sparqlService.getSparqlService().update(QueryLanguage.SPARQL, q);
    sparqlService.getSparqlService().update(QueryLanguage.SPARQL, q2);
    sparqlService.getSparqlService().update(QueryLanguage.SPARQL, q3);
    sparqlService.getSparqlService().update(QueryLanguage.SPARQL, q4);
    sparqlService.getSparqlService().update(QueryLanguage.SPARQL, q5);
    sparqlService.getSparqlService().update(QueryLanguage.SPARQL, q6);
    sparqlService.getSparqlService().update(QueryLanguage.SPARQL, q7);
    sparqlService.getSparqlService().update(QueryLanguage.SPARQL, q8);
    sparqlService.getSparqlService().update(QueryLanguage.SPARQL, q9);
    sparqlService.getSparqlService().update(QueryLanguage.SPARQL, q10);
    sparqlService.getSparqlService().update(QueryLanguage.SPARQL, q11f);
//        sparqlService.getSparqlService().update(QueryLanguage.SPARQL, q12);
    sparqlUtils.delete("https://redi.cedia.edu.ec/context/fi");
    sparqlUtils.delete("https://redi.cedia.edu.ec/context/f1");
    sparqlUtils.delete("https://redi.cedia.edu.ec/context/f2");
    sparqlUtils.delete("https://redi.cedia.edu.ec/context/f3");
    sparqlUtils.delete("https://redi.cedia.edu.ec/context/f1x");
    sparqlUtils.delete("https://redi.cedia.edu.ec/context/f2fix");
//        sparqlUtils.delete("https://redi.cedia.edu.ec/context/authorsJHHD");
//        sparqlUtils.delete("https://redi.cedia.edu.ec/context/authorsJHC");
//        sparqlUtils.delete("https://redi.cedia.edu.ec/context/authorsJH1");
//        sparqlUtils.delete("https://redi.cedia.edu.ec/context/authorsJH2");
//        sparqlUtils.delete("https://redi.cedia.edu.ec/context/authorsJH3");
    String qdw = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "insert {\n"
            + "  graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
            + "	?a <http://schema.org/memberOf> ?affu .\n"
            + "    ?affu a foaf:Organization .\n"
            + "  	?affu foaf:name ?ff .\n"
            + "  }\n"
            + "} where {\n"
            + "    graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
            + "    	?a <http://schema.org/affiliation> ?ff .\n"
            + "      	bind (iri(concat('https://redi.cedia.edu.ec/resource/affiliation/', encode_for_uri(?ff))) as ?affu) .\n"
            + "    }\n"
            + "}";
    sparqlService.getSparqlService().update(QueryLanguage.SPARQL, qdw);
  }

  public Map<Provider, Integer> ProcessAuthors(List<Provider> AuthorsProviderslist, String organization) throws MarmottaException, RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException, InvalidArgumentException, UpdateExecutionException, InterruptedException {
    Map<Provider, Integer> ProvidersElements = new HashMap();
    BoundedExecutor bexecutorService = BoundedExecutor.getThreadPool(MAXTHREADS);
    Provider MainAuthorsProvider = AuthorsProviderslist.get(0);
    List<Person> allAuthors = null;
    if (organization != null) {
      allAuthors = MainAuthorsProvider.getAuthorsByOrganization(organization);
    } else {
      allAuthors = MainAuthorsProvider.getAuthors();
    }
    String harvestedProvidersList = "";
    for (int j = 1; j < AuthorsProviderslist.size(); j++) {
      harvestedProvidersList += " <" + AuthorsProviderslist.get(j).Graph + "> ";
    }
    for (int i = 0; i < allAuthors.size(); i++) {
      final int ix = i;
      final int allx = allAuthors.size();
      final Person aSeedAuthor = allAuthors.get(i);
      final List<Map.Entry<Provider, List<Person>>> Candidates = new ArrayList<>();
      Candidates.add(new AbstractMap.SimpleEntry<Provider, List<Person>>(MainAuthorsProvider, Lists.newArrayList(aSeedAuthor)));
      List<Provider> providersHarvested = new ArrayList<>();
      //Check Harvested Data
      List<Map<String, Value>> queryResponse = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, "select ?g (if (sum(?xc)>0,1,0) as ?p)  {\n"
              + "    values ?g { " + harvestedProvidersList + " } .\n"
              + "    graph ?g {\n"
              + "        optional {\n"
              + "   			?x <http://www.w3.org/2002/07/owl#oneOf> <" + aSeedAuthor.URI + "> .\n"
              + "   			bind (1 as ?xc) .\n"
              + "        }\n"
              + "    }\n"
              + "} group by ?g");
      String harvestedProvidersListRes = "";

      for (int j = 1; j < AuthorsProviderslist.size(); j++) {
        for (Map<String, Value> aresh : queryResponse) {
          if (AuthorsProviderslist.get(j).Graph.compareTo(aresh.get("g").stringValue()) == 0) {
            harvestedProvidersListRes += aresh.get("p").stringValue();
            if (aresh.get("p").stringValue().compareTo("1") == 0) {
              providersHarvested.add(AuthorsProviderslist.get(j));
            }
          }
        }
      }
      final String harvestedProvidersListURI = constantService.getDisambiguationStatusResource() + harvestedProvidersListRes;
      boolean alreadyProcessed = sparqlService.getSparqlService().ask(QueryLanguage.SPARQL, "ask from <" + constantService.getAuthorsSameAsGraph() + "> { <" + aSeedAuthor.URI + "> <http://dbpedia.org/ontology/status> <" + harvestedProvidersListURI + "> }");
      if (alreadyProcessed) {
        //No need to disambiguate again
        continue;
      } else {
        //Get candidates and disambiguate
        for (int j = 1; j < AuthorsProviderslist.size(); j++) {
          Provider aSecondaryProvider = AuthorsProviderslist.get(j);
          if (providersHarvested.contains(aSecondaryProvider)) {
            List<Person> aProviderCandidates = aSecondaryProvider.getCandidates(aSeedAuthor.URI);
            if (!aProviderCandidates.isEmpty()) {
              Candidates.add(new AbstractMap.SimpleEntry<>(aSecondaryProvider, aProviderCandidates));
            }
            ProvidersElements.put(AuthorsProviderslist.get(j), aProviderCandidates.size());
          }
        }
        task.updateDetailMessage("Threads", bexecutorService.workingThreads() + "");
        final ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
        bexecutorService.submitTask(new Runnable() {
          @Override
          public void run() {
            try {
              task.updateDetailMessage("Status", String.format("Start disambiguating %s  out of %s  authors", ix, allx));
              log.info("Start disambiguating {} out of {} authors", ix, allx);
              log.info("{}-{}-Load data", ix, allx);
              for (Map.Entry<Provider, List<Person>> aCandidateList : Candidates) {
                aCandidateList.getKey().FillData(aCandidateList.getValue());
              }
              List<Entry<Provider, List<Person>>> subList = Candidates.subList(1, Candidates.size());
              Candidates.addAll(Lists.reverse(subList));
              log.info("{}-{}-Recursive exploring", ix, allx);
              Model Disambiguate = Disambiguate(Candidates, 0, new Person());
              log.info("{}-{}-Store links", ix, allx);
              boolean alreadyHasPublications = sparqlService.getSparqlService().ask(QueryLanguage.SPARQL, "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                      + "ask from <" + constantService.getAuthorsProviderGraph() + "> {\n"
                      + "	<" + aSeedAuthor.URI + "> foaf:publications [] .\n"
                      + "}");
              if (alreadyHasPublications || Disambiguate.size() > 0) {
                Disambiguate.add(instance.createURI(aSeedAuthor.URI), instance.createURI("http://www.w3.org/2002/07/owl#sameAs"), instance.createURI(aSeedAuthor.URI));
              }
              Disambiguate.add(instance.createURI(aSeedAuthor.URI), instance.createURI("http://dbpedia.org/ontology/status"), instance.createURI(harvestedProvidersListURI));
              sparqlService.getGraphDBInstance().addBuffer(instance.createURI(constantService.getAuthorsSameAsGraph()), Disambiguate);
              task.updateDetailMessage("Status", String.format("Finish disambiguating %s out of %s authors", ix, allx));
              log.info("Finish disambiguating {} out of {} authors", ix, allx);

            } catch (Exception ex) {
              log.error("Unknown error while disambiguating");
              ex.printStackTrace();
              log.info("Retrying...");
              run();
            }
          }
        });
        task.updateDetailMessage("Threads", bexecutorService.workingThreads() + "");
      }
    }
    bexecutorService.end();
    return ProvidersElements;
  }

  public Model Disambiguate(List<Map.Entry<Provider, List<Person>>> Candidates, int level, Person superAuthor) throws MarmottaException, RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException, InvalidArgumentException, UpdateExecutionException {
    Model r = new LinkedHashModel();
    if (level >= Candidates.size()) {
      return r;
    }
    Person enrich = superAuthor;
    List<Person> CandidateListLevel = Candidates.get(level).getValue();
    for (Person aCandidate : CandidateListLevel) {
      if (superAuthor.check(aCandidate, true)) {
        registerSameAsModel(r, superAuthor.URI, aCandidate.URI);
        enrich = enrich.enrich(aCandidate, true);
      }
    }
    Model Disambiguate = Disambiguate(Candidates, level + 1, enrich);
    r.addAll(Disambiguate);
    return r;
  }

  public void ProcessCoauthors(final List<Provider> ProvidersList, final boolean onlySameAs) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException, InterruptedException, RepositoryException {
    BoundedExecutor bexecutorService = BoundedExecutor.getThreadPool(MAXTHREADS);
    String qryDisambiguatedCoauthors = " select distinct ?p { "
            + " graph <" + constantService.getAuthorsSameAsGraph() + "> { "
            + "     ?p <http://www.w3.org/2002/07/owl#sameAs> ?o "
            //+ "     values ?p { <https://redi.cedia.edu.ec/resource/authors/UCUENCA/ojs/LUPERCIO__LUCIA> <https://redi.cedia.edu.ec/resource/authors/UCUENCA/ojs/SAQUICELA__VICTOR> } . "
            + " } "
            + "}";
    final List<Map<String, Value>> queryResponse = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, qryDisambiguatedCoauthors);
    Collections.shuffle(queryResponse);
    int i = 0;
    for (Map<String, Value> anAuthor : queryResponse) {
      final int ix = i;
      final String authorURI = anAuthor.get("p").stringValue();
      log.info("Start disambiguating coauthors {} out of {}", i, queryResponse.size());
      task.updateDetailMessage("Threads", bexecutorService.workingThreads() + "");
      bexecutorService.submitTask(new Runnable() {
        @Override
        public void run() {
          try {
            groupCoauthors(ProvidersList, authorURI, onlySameAs);
            log.info("Finish disambiguating coauthors {} out of {}", ix, queryResponse.size());
          } catch (Exception ex) {
            log.error("Unknown exception while disambiguating coauthors");
            log.info("Retrying ... {}", authorURI);
            ex.printStackTrace();
            run();
          }
        }
      });
      task.updateDetailMessage("Threads", bexecutorService.workingThreads() + "");
      i++;
    }
    bexecutorService.end();
  }

  public Map<String, Person> getPersons(String query) throws MarmottaException, RepositoryException {
    List<Map<String, Value>> queryResponsex = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, query);
    return getPersons(queryResponsex);
  }

  public Map<String, Person> getPersons(List<Map<String, Value>> list) {
    Map<String, Person> mp = new HashMap<>();
    for (Map<String, Value> a : list) {
      boolean b = a.get("aup") != null && !a.get("aup").stringValue().trim().isEmpty() && a.get("aup").stringValue().trim().compareTo("1") == 0;
      Person auxPerson = getAuxPerson(a);
      if (mp.containsKey(auxPerson.URI)) {
        Person get = mp.get(auxPerson.URI);
        Person enrich = get.enrich(auxPerson, false);
        mp.put(auxPerson.URI, enrich);
      } else {
        mp.put(auxPerson.URI, auxPerson);
      }
      if (b) {
        mp.get(auxPerson.URI).Origin = new Provider("mock", "mock", null);
      }
    }
    return mp;
  }

  public Person getAuxPerson(Map<String, Value> ar) {
    Person n = new Person();
    n.Name = new ArrayList<>();
    n.URI = ar.get("a").stringValue();
    if (ar.get("n") != null) {
      n.Name.add(Lists.newArrayList(ar.get("n").stringValue()));
    }
    if (ar.get("fn") != null && ar.get("ln") != null) {
      ArrayList<String> names = Lists.newArrayList(ar.get("fn").stringValue());
      names.add(ar.get("ln").stringValue());
      n.Name.add(names);
    }
    n.Affiliations = new ArrayList<>();
    n.Coauthors = new ArrayList<>();
    n.Publications = new ArrayList<>();
    n.Topics = new ArrayList<>();
    n.ORCIDs = new ArrayList<>();
    return n;
  }

  public Map<String, Set<String>> getGroups(List<Map<String, Value>> ar) {
    Map<String, Set<String>> mp = new HashMap<>();
    for (Map<String, Value> i : ar) {
      String g = i.get("pq").stringValue();
      String u = i.get("af").stringValue();
      if (!mp.containsKey(g)) {
        mp.put(g, new LinkedHashSet<String>());
      }
      mp.get(g).add(u);
    }
    return mp;
  }

  public List<Entry<Map<String, Person>, Set<Set<String>>>> obtainGroupCoauthors(List<Provider> ProvidersList, String authorURI) throws MarmottaException, RepositoryException {
    List<Entry<Map<String, Person>, Set<Set<String>>>> res = new ArrayList<>();
    Set<Set<String>> obtainGroupPublications = obtainGroupPublications(ProvidersList, authorURI);
    for (Set<String> mypub : obtainGroupPublications) {
      String pubs = "  ";
      for (String aPub : mypub) {
        pubs += " <" + aPub + "> ";
      }
      String providersGraphs = "  ";
      for (Provider aProvider : ProvidersList) {
        providersGraphs += " <" + aProvider.Graph + "> ";
      }
      String qryAllAuthors = "select distinct ?a ?n ?fn ?ln ?aup {\n"
              + "		graph <" + constantService.getAuthorsSameAsGraph() + "> {\n"
              + "			values ?pu { <" + authorURI + "> } .\n"
              + "			?pu <http://www.w3.org/2002/07/owl#sameAs> ?ax .\n"
              + "			optional {\n"
              + "                         ?a <http://www.w3.org/2002/07/owl#sameAs> [] .\n"
              + "                         bind('1' as ?aup) .\n"
              + "			}\n"
              + "		}\n"
              + "		values ?g { " + providersGraphs + " } graph ?g {\n"
              + "			values ?p { " + pubs + " } .\n"
              + "			?ax <http://xmlns.com/foaf/0.1/publications> ?p .\n"
              + "			?a <http://xmlns.com/foaf/0.1/publications> ?p .\n"
              + "			optional { ?a <http://xmlns.com/foaf/0.1/name> ?n}\n"
              + "			optional { ?a <http://xmlns.com/foaf/0.1/givenName> ?fn}\n"
              + "			optional { ?a <http://xmlns.com/foaf/0.1/familyName> ?ln}\n"
              + "		}\n"
              + "}";
      Map<String, Person> queryResponse_mp = getPersons(qryAllAuthors);
      List<Person> queryResponse = new ArrayList<>(queryResponse_mp.values());
      Set<Set<String>> coauthorsGroups = new HashSet<>();
      for (int i = 0; i < queryResponse.size(); i++) {
        for (int j = i + 1; j < queryResponse.size(); j++) {
          Person auxPersoni = queryResponse.get(i);
          Person auxPersonj = queryResponse.get(j);
          Boolean checkName = null;
          if (auxPersoni.Origin == null && auxPersonj.Origin == null) {
            checkName = auxPersoni.checkName(auxPersonj, false);
          } else if (auxPersoni.Origin != null && auxPersonj.Origin == null) {
            checkName = auxPersoni.checkName(auxPersonj, true);
          } else if (auxPersoni.Origin == null && auxPersonj.Origin != null) {
            checkName = auxPersonj.checkName(auxPersoni, true);
          } else if (auxPersoni.Origin != null && auxPersonj.Origin != null) {
            checkName = auxPersoni.checkName(auxPersonj, true) || auxPersonj.checkName(auxPersoni, true);
          }
          if (checkName != null && checkName) {
            Set<String> aGroup = new HashSet<>();
            aGroup.add(auxPersoni.URI);
            aGroup.add(auxPersonj.URI);
            boolean alreadyProcessed = false;
            for (Set<String> potentialGroup : coauthorsGroups) {
              if (potentialGroup.contains(auxPersoni.URI) || potentialGroup.contains(auxPersonj.URI)) {
                potentialGroup.addAll(aGroup);
                alreadyProcessed = true;
              }
            }
            if (!alreadyProcessed) {
              coauthorsGroups.add(aGroup);
            }
          }
        }
      }
      Set<Set<String>> ls_alone = new HashSet<>();
      for (int i = 0; i < queryResponse.size(); i++) {
        Person auxPersoni = queryResponse.get(i);
        boolean alone = true;
        for (Set<String> ung : coauthorsGroups) {
          if (ung.contains(auxPersoni.URI)) {
            alone = false;
            break;
          }
        }
        if (alone) {
          Set<String> hsalone = new HashSet<>();
          hsalone.add(auxPersoni.URI);
          ls_alone.add(hsalone);
        }
      }
      coauthorsGroups.addAll(ls_alone);
      res.add(new AbstractMap.SimpleEntry<>(queryResponse_mp, coauthorsGroups));
    }
    return res;
  }

  public void groupCoauthors(List<Provider> ProvidersList, String authorURI, boolean onlySameAs) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException, RepositoryException, RDFHandlerException {
    List<Entry<Map<String, Person>, Set<Set<String>>>> obtainGroupCoauthorss = obtainGroupCoauthors(ProvidersList, authorURI);
    for (Entry<Map<String, Person>, Set<Set<String>>> obtainGroupCoauthors : obtainGroupCoauthorss) {
      Map<String, Person> queryResponse_mp = obtainGroupCoauthors.getKey();
      Set<Set<String>> coauthorsGroups = obtainGroupCoauthors.getValue();
      for (Set<String> eachGroup : coauthorsGroups) {
        String UUID = (new TreeSet<String>(eachGroup)).toString();
        String eachGroupUUID = Cache.getMD5(UUID);
        String PossibleNewURI = constantService.getAuthorResource() + eachGroupUUID;
        String autUris = " ";
        for (String groupIndex : eachGroup) {
          autUris += " <" + groupIndex + "> ";
        }
        String au = "select distinct ?a ?n ?fn ?ln {\n"
                + "    values ?p { " + autUris + " }. \n"
                + "    graph <" + constantService.getAuthorsSameAsGraph() + "> {\n"
                + "        ?a <http://www.w3.org/2002/07/owl#sameAs> ?p .\n"
                + "    }\n"
                + "    graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                + "        optional { ?a <http://xmlns.com/foaf/0.1/name> ?n} \n"
                + "        optional { ?a <http://xmlns.com/foaf/0.1/givenName> ?fn}\n"
                + "        optional { ?a <http://xmlns.com/foaf/0.1/familyName> ?ln}\n"
                + "    }\n"
                + "}";
        Map<String, Person> queryResponse_mp_au = getPersons(au);
        for (String groupIndex : eachGroup) {
          Person get = queryResponse_mp.get(groupIndex);
          boolean pros = false;
          for (Person p : queryResponse_mp_au.values()) {
            Boolean checkName = p.checkName(get, true);
            if (checkName != null && checkName) {
              pros = true;
              if (onlySameAs) {
                registerSameAs(constantService.getAuthorsSameAsGraph() + TEMP_CA, p.URI, groupIndex);
              }
            }
          }
          if (!pros) {
            if (!onlySameAs) {
              registerSameAs(constantService.getCoauthorsSameAsGraph(), PossibleNewURI, groupIndex);
            }
          }
        }
      }
    }
  }

  private synchronized void log(String text) {
    try {
      Files.write(Paths.get("/tmp/logredi.txt"), text.getBytes(), StandardOpenOption.APPEND);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private Set<String> get() throws MarmottaException, RepositoryException {
    String q = "select ?c {\n"
            + "	graph <https://redi.cedia.edu.ec/context/authorsSameAs>{\n"
            + "    	<https://redi.cedia.edu.ec/resource/authors/UCUENCA/oai-pmh/GARCIA_ALVEAR__JORGE_LUIS> <http://www.w3.org/2002/07/owl#sameAs> ?c\n"
            + "    }\n"
            + "} ";
    List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, q);
    Set<String> ls = new LinkedHashSet<>();
    for (Map<String, Value> v : query) {
      ls.add(v.get("c").stringValue());
    }
    return ls;
  }

  public void ProcessPublications(final List<Provider> ProvidersList) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException, InterruptedException, RepositoryException {
    BoundedExecutor bexecutorService = BoundedExecutor.getThreadPool(MAXTHREADS);
    //String qryDisambiguatedAuthors = " select distinct ?p { graph <" + constantService.getAuthorsSameAsGraph() + "> { values ?p { <https://redi.cedia.edu.ec/resource/authors/UCUENCA/oai-pmh/SAQUICELA_GALARZA__VICTOR_HUGO> } . } }";
    String qryDisambiguatedAuthors = " select distinct ?p { graph <" + constantService.getAuthorsSameAsGraph() + "> { ?p <http://www.w3.org/2002/07/owl#sameAs> ?o } }";
    final List<Map<String, Value>> queryResponse = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, qryDisambiguatedAuthors);
    int i = 0;
    for (Map<String, Value> anAuthor : queryResponse) {
      final int ix = i;
      final String authorURI = anAuthor.get("p").stringValue();
      log.info("Start disambiguating publications {} out of {} authors", i, queryResponse.size());
      task.updateDetailMessage("Threads", bexecutorService.workingThreads() + "");
      bexecutorService.submitTask(new Runnable() {
        @Override
        public void run() {
          try {
            groupPublications(ProvidersList, authorURI);
            log.info("Finish disambiguating publications {} out of {} authors", ix, queryResponse.size());
          } catch (Exception ex) {
            log.error("Unknown exception while disambiguating publications");
            ex.printStackTrace();
            run();
          }
        }
      });
      task.updateDetailMessage("Threads", bexecutorService.workingThreads() + "");
      i++;
    }
    bexecutorService.end();
  }

  private Set<Set<String>> obtainGroupPublications(List<Provider> ProvidersList, String personURI) throws MarmottaException {
    String providersGraphs = "  ";
    for (Provider aProvider : ProvidersList) {
      providersGraphs += " <" + aProvider.Graph + "> ";
    }
    String qryAllPublications = "select distinct ?p ?t {\n"
            + "  	graph <" + constantService.getAuthorsSameAsGraph() + ">{\n"
            + "		<" + personURI + "> <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
            + "    }\n"
            + "    values ?g { " + providersGraphs + " } graph ?g {\n"
            + "  	 ?c <http://xmlns.com/foaf/0.1/publications> ?p.\n"
            + "  	 ?p <http://purl.org/dc/terms/title> ?t.\n"
            + "    }\n"
            + "} ";
    List<Map<String, Value>> queryResponse = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, qryAllPublications);
    Set<Set<String>> publicationsGroups = new HashSet<>();
    for (int i = 0; i < queryResponse.size(); i++) {
      for (int j = i + 1; j < queryResponse.size(); j++) {
        final String t_i = queryResponse.get(i).get("t").stringValue();
        final String t_j = queryResponse.get(j).get("t").stringValue();
        double titleSimilarity = PublicationUtils.compareTitle(t_i, t_j);
        if (titleSimilarity >= Person.thresholdTitle) {
          Set<String> aGroup = new HashSet<>();
          aGroup.add(queryResponse.get(i).get("p").stringValue());
          aGroup.add(queryResponse.get(j).get("p").stringValue());
          Set<String> auxGroup = null;
          for (Set<String> potentialGroup : publicationsGroups) {
            if (potentialGroup.contains(queryResponse.get(i).get("p").stringValue()) || potentialGroup.contains(queryResponse.get(j).get("p").stringValue())) {
              auxGroup = potentialGroup;
              break;
            }
          }
          if (auxGroup == null) {
            publicationsGroups.add(aGroup);
          } else {
            auxGroup.addAll(aGroup);
          }
        }
      }
    }
    Set<Set<String>> ls_alone = new HashSet<>();
    for (int i = 0; i < queryResponse.size(); i++) {
      boolean alone = true;
      for (Set<String> ung : publicationsGroups) {
        if (ung.contains(queryResponse.get(i).get("p").stringValue())) {
          alone = false;
          break;
        }
      }
      if (alone) {
        Set<String> hsalone = new HashSet<>();
        hsalone.add(queryResponse.get(i).get("p").stringValue());
        ls_alone.add(hsalone);
      }
    }
    publicationsGroups.addAll(ls_alone);
    return publicationsGroups;
  }

  public void groupPublications(List<Provider> ProvidersList, String personURI) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException, RepositoryException, RDFHandlerException {
    Set<Set<String>> publicationsGroups = obtainGroupPublications(ProvidersList, personURI);
    for (Set<String> eachGroup : publicationsGroups) {
      String UUID = (new TreeSet<String>(eachGroup)).toString();
      String eachGroupUUID = Cache.getMD5(UUID);
      String PossibleNewURI = constantService.getPublicationResource() + eachGroupUUID;
      for (String groupIndex : eachGroup) {
        registerSameAs(constantService.getPublicationsSameAsGraph(), PossibleNewURI, groupIndex);
      }
    }
  }

  public void registerSameAsCheck(String graph, String URIO, String URIP) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException, RepositoryException {
    if (URIO != null && URIP != null && URIO.compareTo(URIP) != 0) {
      String q = "select distinct ?o { graph <" + graph + "> { values ?p { <" + URIP + "> } . ?o <http://www.w3.org/2002/07/owl#sameAs> ?p. } }";
      List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, q);
      if (query.isEmpty()) {
        String buildInsertQuery = buildInsertQuery(graph, URIO, "http://www.w3.org/2002/07/owl#sameAs", URIP);
        sparqlService.getSparqlService().update(QueryLanguage.SPARQL, buildInsertQuery);
      } else {
        for (Map<String, Value> mp : query) {
          String stringValue = mp.get("o").stringValue();
          String buildInsertQuery = buildInsertQuery(graph, stringValue, "http://www.w3.org/2002/07/owl#sameAs", URIP);
          sparqlService.getSparqlService().update(QueryLanguage.SPARQL, buildInsertQuery);
        }
      }
    }
  }

  public void registerSameAs(String graph, String URIO, String URIP) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException, RepositoryException, RDFHandlerException {
    if (URIO != null && URIP != null && URIO.compareTo(URIP) != 0) {
      //String buildInsertQuery = buildInsertQuery(graph, URIO, "http://www.w3.org/2002/07/owl#sameAs", URIP);
      //sparqlService.getSparqlService().update(QueryLanguage.SPARQL, buildInsertQuery);
      sparqlService.getGraphDBInstance().addBuffer(graph, URIO, "http://www.w3.org/2002/07/owl#sameAs", URIP);
    }
  }

  public void registerSameAsModel(Model graph, String URIO, String URIP) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {
    if (URIO != null && URIP != null && URIO.compareTo(URIP) != 0) {
      ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
      graph.add(instance.createURI(URIO), instance.createURI("http://www.w3.org/2002/07/owl#sameAs"), instance.createURI(URIP));
    }
  }

  private String buildInsertQuery(String grapfhProv, String sujeto, String predicado, String objeto) {
    if (commonsServices.isURI(objeto)) {
      return queriesService.getInsertDataUriQuery(grapfhProv, sujeto, predicado, objeto);
    } else {
      return queriesService.getInsertDataLiteralQuery(grapfhProv, sujeto, predicado, objeto);
    }
  }

  @Override
  public void Merge() {
    try {
    } catch (Exception ex) {
      log.error("Unknown error while merging Central Graph");
      ex.printStackTrace();
    }
  }

  @Override
  public String startDisambiguation() {
    String State = "";
    if (DisambiguationWorker != null && DisambiguationWorker.isAlive()) {
      State = "Process running.. check the marmotta main log for further details";
    } else {
      State = "Process starting.. check the marmotta main log for further details";
      DisambiguationWorker = new Thread() {
        @Override
        public void run() {
          try {
            log.info("Starting Disambiguation process ...");
            Process();
          } catch (Exception ex) {
            log.warn("Unknown error while disambiguating, please check the catalina log for further details.");
            ex.printStackTrace();
          }
        }

      };
      DisambiguationWorker.start();
    }
    return State;
  }

  @Override
  public String startDisambiguation(String[] orgs) {
    final String[] orgss = orgs;
    String State = "";
    if (DisambiguationWorker != null && DisambiguationWorker.isAlive()) {
      State = "Process running.. check the marmotta main log for further details";
    } else {
      State = "Process starting.. check the marmotta main log for further details";
      DisambiguationWorker = new Thread() {
        @Override
        public void run() {
          try {
            log.info("Starting Disambiguation process ...");
            Process(orgss);
          } catch (Exception ex) {
            log.warn("Unknown error while disambiguating, please check the catalina log for further details.");
            ex.printStackTrace();
          }
        }
      };
      DisambiguationWorker.start();
    }
    return State;
  }

  @Override
  public String startMerge() {
    String State = "";
    if (CentralGraphWorker != null && CentralGraphWorker.isAlive()) {
      State = "Process running.. check the marmotta main log for further details";
    } else {
      State = "Process starting.. check the marmotta main log for further details";
      CentralGraphWorker = new Thread() {
        @Override
        public void run() {
          try {
            log.info("Starting Central Graph process ...");
            Merge();
          } catch (Exception ex) {
            log.warn("Unknown error Central Graph , please check the catalina log for further details.");
            ex.printStackTrace();
          }
        }

      };
      CentralGraphWorker.start();
    }
    return State;
  }

  @Override
  public void Process() {
    Process(null);
  }
}
