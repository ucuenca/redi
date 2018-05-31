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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.DisambiguationService;
import org.apache.marmotta.ucuenca.wk.pubman.api.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Person;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Provider;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.utils.PublicationUtils;
import org.apache.marmotta.ucuenca.wk.commons.function.Cache;
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
import org.openrdf.repository.RepositoryConnection;
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

    final int MAXTHREADS = 7;

    @Inject
    private org.slf4j.Logger log;

    @Inject
    private ConstantService constantService;

    @Inject
    private SparqlService sparqlService;

    @Inject
    private QueriesService queriesService;

    @Inject
    private CommonsServices commonsServices;

    @Inject
    private TaskManagerService taskManagerService;

    @Inject
    private SparqlFunctionsService sparqlFunctionsService;

    @Inject
    private SesameService sesameService;

    private Task task;

    private Thread DisambiguationWorker;
    private Thread CentralGraphWorker;

    private String queryMatches(String org) throws MarmottaException {

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
        List<Map<String, Value>> response = sparqlService.query(QueryLanguage.SPARQL, querymatchs);
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

    private void updateLogs(List<Map<String, Map<Provider, Integer>>> providersResult) throws MarmottaException {
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

    private List<Provider> getProviders() throws MarmottaException {
        List<Provider> Providers = new ArrayList<>();
        Providers.add(new Provider("Authors", constantService.getAuthorsProviderGraph(), sparqlService));
        Providers.add(new Provider("Scopus", constantService.getScopusGraph(), sparqlService));
        Providers.add(new Provider("Scholar", constantService.getGoogleScholarGraph(), sparqlService));
        Providers.add(new Provider("Academics", constantService.getAcademicsKnowledgeGraph(), sparqlService));
        Providers.add(new Provider("Scielo", constantService.getScieloGraph(), sparqlService));
        Providers.add(new Provider("DBLP", constantService.getDBLPGraph(), sparqlService));
        Providers.add(new Provider("Springer", constantService.getSpringerGraph(), sparqlService));
        return Providers;
    }

    @Override
    public void Proccess(String[] orgs) {
        try {
            SPARQLUtils sparqlUtils = new SPARQLUtils(sparqlService);
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
                ProcessAuthors(Providers, null);
            }
            int iasa = sparqlUtils.count(constantService.getAuthorsSameAsGraph());
            do {
                ProcessCoauthors(Providers, true);
                sparqlUtils.addAll(constantService.getAuthorsSameAsGraph(), constantService.getAuthorsSameAsGraph() + "1");
                int asa = sparqlUtils.count(constantService.getAuthorsSameAsGraph());
                if (asa != iasa) {
                    iasa = asa;
                } else {
                    break;
                }
            } while (true);
            mergeAuthors();
            //sparqlUtils.delete(constantService.getAuthorsSameAsGraph() + "1");
            //task.updateDetailMessage("Status", String.format("%s Disambiguation", "Coauthors"));
            //sparqlUtils.delete(constantService.getCoauthorsSameAsGraph());
            //ProcessCoauthors(Providers, false);
//            task.updateDetailMessage("Status", String.format("%s Disambiguation", "Publications"));
//            sparqlUtils.delete(constantService.getPublicationsSameAsGraph());
            //ProcessPublications(Providers);

            //sparqlUtils.replaceSameAs(constantService.getAuthorsSameAsGraph(), constantService.getAuthorsSameAsGraph() + "2",
            //        constantService.getAuthorsSameAsGraph() + "2d", constantService.getAuthorsSameAsGraph() + "2i", true);
            //sparqlUtils.minus(constantService.getAuthorsSameAsGraph() + "3", constantService.getAuthorsSameAsGraph(), constantService.getAuthorsSameAsGraph() + "2d");
            //sparqlUtils.addAll(constantService.getAuthorsSameAsGraph() + "3", constantService.getAuthorsSameAsGraph() + "2i");
//            task.updateDetailMessage("Status", String.format("%s Remove", "Duplicates"));
//            log.info("Remove Duplicates");
//            //sparqlUtils.removeDuplicates(constantService.getAuthorsSameAsGraph());
            //sparqlUtils.removeDuplicates(constantService.getPublicationsSameAsGraph());
            //sparqlUtils.removeDuplicates(constantService.getCoauthorsSameAsGraph());
//            log.info("Upload Logs");
//            updateLogs(providersResult);
        } catch (Exception ex) {
            log.error("Unknown error while disambiguating");
            ex.printStackTrace();
        }
        taskManagerService.endTask(task);
    }

    public void subjectsMerger(List<Provider> AuthorsProviderslist) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
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
        List<Map<String, Value>> query = sparqlService.query(QueryLanguage.SPARQL, q);
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

    public void mergeAuthors() throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        String qryDisambiguatedCoauthors = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "select distinct ?a  { \n"
                + "  graph <" + constantService.getAuthorsSameAsGraph() + "> { \n"
                + "    ?a <http://www.w3.org/2002/07/owl#sameAs> [] . \n"
                + "  }\n"
                + "}";

        List<Map<String, Value>> queryResponsec = sparqlService.query(QueryLanguage.SPARQL, qryDisambiguatedCoauthors);

        List<Map<String, Value>> r = new ArrayList<>();
        for (Map<String, Value> rq : queryResponsec) {
            String qryDisambiguatedCoauthors2 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                    + "select ?a ?n ?fn ?ln { \n"
                    + "  graph <" + constantService.getAuthorsProviderGraph() + "> { \n"
                    + "  bind (<" + rq.get("a").stringValue() + "> as ?a) .\n"
                    + "  	optional { ?a <http://xmlns.com/foaf/0.1/name> ?n}\n"
                    + "    optional { ?a <http://xmlns.com/foaf/0.1/givenName> ?fn}\n"
                    + "    optional { ?a <http://xmlns.com/foaf/0.1/familyName> ?ln}\n"
                    + "  }\n"
                    + "}";
            List<Map<String, Value>> rx = sparqlService.query(QueryLanguage.SPARQL, qryDisambiguatedCoauthors2);
            r.addAll(rx);
        }
        Map<String, Person> persons = getPersons(r);
        String qryGroups = "select ?a ?b { \n"
                + "  graph <" + constantService.getAuthorsSameAsGraph() + "> { \n"
                + "    ?a <http://www.w3.org/2002/07/owl#sameAs> ?c . \n"
                + "    ?b <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "  }\n"
                + "}";
        List<Map<String, Value>> query = sparqlService.query(QueryLanguage.SPARQL, qryGroups);
        Set<Set<String>> groupsAuthors = getGroupsAuthors(query);

        Map<String, Set<String>> ngroups = new HashMap<>();
        Map<String, Set<String>> sgroups = new HashMap<>();
        for (Set<String> ag : groupsAuthors) {
            List<String> ls = new ArrayList<>(ag);
            String u = null;
            int score = -1;
            for (int i = 0; i < ls.size(); i++) {
                Person get1 = persons.get(ls.get(i));
                int bestNameLen = get1.bestNameLen();
                if (bestNameLen > score) {
                    u = get1.URI;
                    score = bestNameLen;
                }
            }
            Person getLong = persons.get(u);
            for (int i = 0; i < ls.size(); i++) {
                Person get1 = persons.get(ls.get(i));
                if (!ngroups.containsKey(u)) {
                    ngroups.put(u, new HashSet<String>());
                }
                ngroups.get(u).add(get1.URI);
                Boolean checkName = getLong.checkName(get1);
                if (checkName == null || !checkName) {
                    if (!sgroups.containsKey(u)) {
                        sgroups.put(u, new HashSet<String>());
                    }
                    sgroups.get(u).add(get1.URI);
                }
            }
        }
        for (Entry<String, Set<String>> next : ngroups.entrySet()) {
            for (String next1 : next.getValue()) {
                registerSameAs(constantService.getAuthorsSameAsGraph() + "2", next.getKey(), next1);
            }
        }
        for (Entry<String, Set<String>> next : sgroups.entrySet()) {
            for (String next1 : next.getValue()) {
                registerSameAs(constantService.getAuthorsSameAsGraph() + "Fix", next.getKey(), next1);
            }
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

    public void InitAuthorsProvider() throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {
        boolean ask = sparqlService.ask(QueryLanguage.SPARQL, "ask from <" + constantService.getAuthorsProviderGraph() + "> { ?a ?b ?c }");
        if (ask) {
            return;
        }
        //copy
        new LongUpdateQueryExecutor(sparqlService,
                "	graph <" + constantService.getAuthorsGraph() + "> {\n"
                + "		?a ?b ?c .\n"
                + "	}\n",
                "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                + "		?a ?b ?c .\n"
                + "	}\n", null, "prefix foaf: <http://xmlns.com/foaf/0.1/>\n", "?a ?b ?c").execute();

        //delete provider triple
        String deleteProviderType = "delete where {\n"
                + "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                + "		?a a <http://ucuenca.edu.ec/ontology#Provider> .\n"
                + "	}\n"
                + "}";
        sparqlService.update(QueryLanguage.SPARQL, deleteProviderType);

        //givName
        new LongUpdateQueryExecutor(sparqlService,
                "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                + "		?a foaf:firstName ?c .\n"
                + "	}\n",
                "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                + "		?a foaf:givenName ?c .\n"
                + "	}\n", null, "prefix foaf: <http://xmlns.com/foaf/0.1/>\n", "?a ?c").execute();
        //famName
        new LongUpdateQueryExecutor(sparqlService,
                "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                + "		?a foaf:lastName ?c .\n"
                + "	}\n",
                "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                + "		?a foaf:familyName ?c .\n"
                + "	}\n", null, "prefix foaf: <http://xmlns.com/foaf/0.1/>\n", "?a ?c").execute();

        //org
        new LongUpdateQueryExecutor(sparqlService,
                "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                + "		?a <http://purl.org/dc/terms/provenance> ?p .\n"
                + "		?a a foaf:Person .\n"
                + "	}\n"
                + "	graph <" + constantService.getEndpointsGraph() + "> {\n"
                + "		?p <http://ucuenca.edu.ec/ontology#belongTo> ?o .\n"
                + "	}\n"
                + "	graph <" + constantService.getOrganizationsGraph() + "> {\n"
                + "		?o <http://ucuenca.edu.ec/ontology#fullName> ?n .\n"
                + "		?o <http://ucuenca.edu.ec/ontology#name> ?nn .\n"
                + "	}\n",
                "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                + "		?a schema:memberOf ?o .\n"
                + "		?o a foaf:Organization .\n"
                + "		?o foaf:name ?n .\n"
                + "		?o foaf:name ?nn .\n"
                + "	}\n", null, "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "prefix  schema: <http://schema.org/>\n", "?a ?o ?n ?nn").execute();

        //alias
        String orgAlias = "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "prefix  schema: <http://schema.org/>\n"
                + "select ?o ?n  {\n"
                + "	graph <" + constantService.getOrganizationsGraph() + "> {\n"
                + "		?o <http://ucuenca.edu.ec/ontology#alias> ?n .\n"
                + "	}\n"
                + "}";
        List<Map<String, Value>> query = sparqlService.query(QueryLanguage.SPARQL, orgAlias);
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
                sparqlService.update(QueryLanguage.SPARQL, buildInsertQuery);
            }
        }

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
        for (int i = 0; i < allAuthors.size(); i++) {
            final int ix = i;
            final int allx = allAuthors.size();
            final Person aSeedAuthor = allAuthors.get(i);
            final List<Map.Entry<Provider, List<Person>>> Candidates = new ArrayList<>();
            Candidates.add(new AbstractMap.SimpleEntry<Provider, List<Person>>(MainAuthorsProvider, Lists.newArrayList(aSeedAuthor)));
            List<Provider> providersHarvested = new ArrayList<>();
            //Check Harvested Data
            String harvestedProvidersList = "";
            for (int j = 1; j < AuthorsProviderslist.size(); j++) {
                Provider aSecondaryProvider = AuthorsProviderslist.get(j);
                boolean harvested = aSecondaryProvider.isHarvested(aSeedAuthor.URI);
                if (harvested) {
                    providersHarvested.add(aSecondaryProvider);
                }
                harvestedProvidersList += (harvested ? "1" : "0");
            }
            final String harvestedProvidersListURI = constantService.getDisambiguationStatusResource() + harvestedProvidersList;
            boolean alreadyProcessed = sparqlService.ask(QueryLanguage.SPARQL, "ask from <" + constantService.getAuthorsSameAsGraph() + "> { <" + aSeedAuthor.URI + "> <http://dbpedia.org/ontology/status> <" + harvestedProvidersListURI + "> }");
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
                bexecutorService.submitTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            task.updateDetailMessage("Status", String.format("Start disambiguating %s  out of %s  authors", ix, allx));
                            log.info("Start disambiguating {} out of {} authors", ix, allx);
                            for (Map.Entry<Provider, List<Person>> aCandidateList : Candidates) {
                                aCandidateList.getKey().FillData(aCandidateList.getValue());
                            }
                            List<Entry<Provider, List<Person>>> subList = Candidates.subList(1, Candidates.size());
                            Candidates.addAll(Lists.reverse(subList));
                            ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
                            Model Disambiguate = Disambiguate(Candidates, 0, new Person());
                            boolean alreadyHasPublications = sparqlService.ask(QueryLanguage.SPARQL, "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                                    + "ask from <" + constantService.getAuthorsProviderGraph() + "> {\n"
                                    + "	<" + aSeedAuthor.URI + "> foaf:publications [] .\n"
                                    + "}");
                            if (alreadyHasPublications || Disambiguate.size() > 0) {
                                Disambiguate.add(instance.createURI(aSeedAuthor.URI), instance.createURI("http://www.w3.org/2002/07/owl#sameAs"), instance.createURI(aSeedAuthor.URI));
                            }
                            Disambiguate.add(instance.createURI(aSeedAuthor.URI), instance.createURI("http://dbpedia.org/ontology/status"), instance.createURI(harvestedProvidersListURI));
                            RepositoryConnection connection = sesameService.getConnection();
                            connection.add(Disambiguate, instance.createURI(constantService.getAuthorsSameAsGraph()));
                            connection.commit();
                            connection.close();
                            task.updateDetailMessage("Status", String.format("Finish disambiguating %s out of %s authors", ix, allx));
                            log.info("Finish disambiguating {} out of {} authors", ix, allx);

                        } catch (Exception ex) {
                            log.error("Unknown error while disambiguating");
                            ex.printStackTrace();
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
        List<Person> CandidateListLevel = Candidates.get(level).getValue();
        boolean up = true;
        for (Person aCandidate : CandidateListLevel) {
            if (superAuthor.check(aCandidate)) {
                up = false;
                Person enrich = superAuthor.enrich(aCandidate, true);
                registerSameAsModel(r, superAuthor.URI, aCandidate.URI);
                Model Disambiguate = Disambiguate(Candidates, level + 1, enrich);
                r.addAll(Disambiguate);
            }
        }
        if (up) {
            Model Disambiguate = Disambiguate(Candidates, level + 1, superAuthor);
            r.addAll(Disambiguate);
        }
        return r;
    }

    public void ProcessCoauthors(final List<Provider> ProvidersList, final boolean onlySameAs) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException, InterruptedException {
        BoundedExecutor bexecutorService = BoundedExecutor.getThreadPool(MAXTHREADS);
        String qryDisambiguatedCoauthors = " select distinct ?p { graph <" + constantService.getAuthorsSameAsGraph() + "> { ?p <http://www.w3.org/2002/07/owl#sameAs> ?o } }";
        final List<Map<String, Value>> queryResponse = sparqlService.query(QueryLanguage.SPARQL, qryDisambiguatedCoauthors);
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
                        ex.printStackTrace();
                    }
                }
            });
            task.updateDetailMessage("Threads", bexecutorService.workingThreads() + "");
            i++;
        }
        bexecutorService.end();
    }

    public Map<String, Person> getPersons(String query) throws MarmottaException {
        List<Map<String, Value>> queryResponsex = sparqlService.query(QueryLanguage.SPARQL, query);
        return getPersons(queryResponsex);
    }

    public Map<String, Person> getPersons(List<Map<String, Value>> list) {
        Map<String, Person> mp = new HashMap<>();
        for (Map<String, Value> a : list) {
            Person auxPerson = getAuxPerson(a);
            if (mp.containsKey(auxPerson.URI)) {
                Person get = mp.get(auxPerson.URI);
                Person enrich = get.enrich(auxPerson, false);
                mp.put(auxPerson.URI, enrich);
            } else {
                mp.put(auxPerson.URI, auxPerson);
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

    public void groupCoauthors(List<Provider> ProvidersList, String authorURI, boolean onlySameAs) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        String providersGraphs = "  ";
        for (Provider aProvider : ProvidersList) {
            providersGraphs += " <" + aProvider.Graph + "> ";
        }
        String qryAllAuthors = "select distinct ?a ?n ?fn ?ln {\n"
                + "		graph <" + constantService.getAuthorsSameAsGraph() + "> {\n"
                + "			values ?pu { <" + authorURI + "> } .\n"
                + "			?pu <http://www.w3.org/2002/07/owl#sameAs> ?ax .\n"
                + "		}\n"
                + "		values ?g { " + providersGraphs + " } graph ?g {\n"
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
                Boolean checkName = auxPersoni.checkName(auxPersonj);
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

        for (Set<String> eachGroup : coauthorsGroups) {
            String eachGroupUUID = Cache.getMD5(UUID.randomUUID().toString());
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
                    Boolean checkName = p.checkName(get);
                    if (checkName != null && checkName) {
                        pros = true;
                        if (onlySameAs) {
                            registerSameAs(constantService.getAuthorsSameAsGraph() + "1", p.URI, groupIndex);
                        }
                    }
                }
                if (!pros) {
                    if (!onlySameAs) {
                        registerSameAsCheck(constantService.getCoauthorsSameAsGraph(), PossibleNewURI, groupIndex);
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

    private Set<String> get() throws MarmottaException {
        String q = "select ?c {\n"
                + "	graph <https://redi.cedia.edu.ec/context/authorsSameAs>{\n"
                + "    	<https://redi.cedia.edu.ec/resource/authors/UCUENCA/oai-pmh/GARCIA_ALVEAR__JORGE_LUIS> <http://www.w3.org/2002/07/owl#sameAs> ?c\n"
                + "    }\n"
                + "} ";
        List<Map<String, Value>> query = sparqlService.query(QueryLanguage.SPARQL, q);
        Set<String> ls = new LinkedHashSet<>();
        for (Map<String, Value> v : query) {
            ls.add(v.get("c").stringValue());
        }
        return ls;
    }

    public void ProcessPublications(final List<Provider> ProvidersList) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException, InterruptedException {
        BoundedExecutor bexecutorService = BoundedExecutor.getThreadPool(MAXTHREADS);
        String qryDisambiguatedAuthors = " select distinct ?p { graph <" + constantService.getAuthorsSameAsGraph() + "> { ?p <http://www.w3.org/2002/07/owl#sameAs> ?o } }";
        final List<Map<String, Value>> queryResponse = sparqlService.query(QueryLanguage.SPARQL, qryDisambiguatedAuthors);
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
                    }
                }
            });
            task.updateDetailMessage("Threads", bexecutorService.workingThreads() + "");
            i++;
        }
        bexecutorService.end();
    }

    public void groupPublications(List<Provider> ProvidersList, String personURI) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
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
        List<Map<String, Value>> queryResponse = sparqlService.query(QueryLanguage.SPARQL, qryAllPublications);
        Set<Set<String>> publicationsGroups = new HashSet<>();
        for (int i = 0; i < queryResponse.size(); i++) {
            for (int j = i + 1; j < queryResponse.size(); j++) {
                double titleSimilarity = PublicationUtils.compareTitle(queryResponse.get(i).get("t").stringValue(), queryResponse.get(j).get("t").stringValue());
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
        for (Set<String> eachGroup : publicationsGroups) {
            String eachGroupUUID = Cache.getMD5(UUID.randomUUID().toString());
            String PossibleNewURI = constantService.getPublicationResource() + eachGroupUUID;
            for (String groupIndex : eachGroup) {
                registerSameAsCheck(constantService.getPublicationsSameAsGraph(), PossibleNewURI, groupIndex);
            }
        }
    }

    public void registerSameAsCheck(String graph, String URIO, String URIP) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {
        if (URIO != null && URIP != null && URIO.compareTo(URIP) != 0) {
            String q = "select distinct ?o { graph <" + graph + "> { values ?p { <" + URIP + "> } . ?o <http://www.w3.org/2002/07/owl#sameAs> ?p. } }";
            List<Map<String, Value>> query = sparqlService.query(QueryLanguage.SPARQL, q);
            if (query.isEmpty()) {
                String buildInsertQuery = buildInsertQuery(graph, URIO, "http://www.w3.org/2002/07/owl#sameAs", URIP);
                sparqlService.update(QueryLanguage.SPARQL, buildInsertQuery);
            } else {
                for (Map<String, Value> mp : query) {
                    String stringValue = mp.get("o").stringValue();
                    String buildInsertQuery = buildInsertQuery(graph, stringValue, "http://www.w3.org/2002/07/owl#sameAs", URIP);
                    sparqlService.update(QueryLanguage.SPARQL, buildInsertQuery);
                }
            }
        }
    }

    public void registerSameAs(String graph, String URIO, String URIP) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {
        if (URIO != null && URIP != null && URIO.compareTo(URIP) != 0) {
            String buildInsertQuery = buildInsertQuery(graph, URIO, "http://www.w3.org/2002/07/owl#sameAs", URIP);
            sparqlService.update(QueryLanguage.SPARQL, buildInsertQuery);
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
            SPARQLUtils sparqlUtils = new SPARQLUtils(sparqlService);
            List<Provider> Providers = getProviders();
            log.info("Merging raw data ...");
            sparqlUtils.mergeRawDataSameAs(Providers, constantService.getCentralGraph() + "TempRaw", constantService.getAuthorsSameAsGraph() + "3");
            log.info("Merging Same As ...");
            sparqlUtils.addAll(constantService.getCentralGraph() + "TempAllSameAs", constantService.getAuthorsSameAsGraph() + "3");
            sparqlUtils.addAll(constantService.getCentralGraph() + "TempAllSameAs", constantService.getPublicationsSameAsGraph());
            sparqlUtils.addAll(constantService.getCentralGraph() + "TempAllSameAs", constantService.getCoauthorsSameAsGraph());
            log.info("Replacing subjects ...");
            sparqlUtils.replaceSameAs(constantService.getCentralGraph() + "TempRaw", constantService.getCentralGraph() + "TempAllSameAs",
                    constantService.getCentralGraph() + "TempAllSameAsD1", constantService.getCentralGraph() + "TempAllSameAsI1", true);
            log.info("Deleting old subjects ...");
            sparqlUtils.minus(constantService.getCentralGraph() + "TempRaw2", constantService.getCentralGraph() + "TempRaw", constantService.getCentralGraph() + "TempAllSameAsD1");
            log.info("Adding new subjects ...");
            sparqlUtils.addAll(constantService.getCentralGraph() + "TempRaw2", constantService.getCentralGraph() + "TempAllSameAsI1");
            log.info("Replacing objects ...");
            sparqlUtils.replaceSameAs(constantService.getCentralGraph() + "TempRaw2", constantService.getCentralGraph() + "TempAllSameAs",
                    constantService.getCentralGraph() + "TempAllSameAsD2", constantService.getCentralGraph() + "TempAllSameAsI2", false);
            log.info("Deleting old objects ...");
            sparqlUtils.minus(constantService.getCentralGraph() + "2", constantService.getCentralGraph() + "TempRaw2", constantService.getCentralGraph() + "TempAllSameAsD2");
            log.info("Adding new objects ...");
            sparqlUtils.addAll(constantService.getCentralGraph() + "2", constantService.getCentralGraph() + "TempAllSameAsI2");
            log.info("Adding sameAs triples ...");
            sparqlUtils.addAll(constantService.getCentralGraph() + "2", constantService.getCentralGraph() + "TempAllSameAs");
            log.info("Deleting temporals ...");
            sparqlUtils.delete(constantService.getCentralGraph() + "TempRaw");
            sparqlUtils.delete(constantService.getCentralGraph() + "TempRaw2");
            sparqlUtils.delete(constantService.getCentralGraph() + "TempAllSameAs");
            sparqlUtils.delete(constantService.getCentralGraph() + "TempAllSameAsD1");
            sparqlUtils.delete(constantService.getCentralGraph() + "TempAllSameAsI1");
            sparqlUtils.delete(constantService.getCentralGraph() + "TempAllSameAsD2");
            sparqlUtils.delete(constantService.getCentralGraph() + "TempAllSameAsI2");
            log.info("Finished merging ...");

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
                        Proccess();
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
                        Proccess(orgss);
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
    public void Proccess() {
        Proccess(null);
    }
}
