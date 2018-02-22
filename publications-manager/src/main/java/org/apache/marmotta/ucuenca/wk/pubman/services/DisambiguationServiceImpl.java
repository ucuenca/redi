/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.google.common.collect.Lists;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.function.Cache;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.pubman.api.CommonService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.DisambiguationService;
import org.apache.marmotta.ucuenca.wk.pubman.api.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Person;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Provider;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.utils.PublicationUtils;
import org.openrdf.model.Value;
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

    final int MAXTHREADS = 10;

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
    private CommonService comPub;

    @Inject
    private TaskManagerService taskManagerService;

    @Inject
    private SparqlFunctionsService sparqlFunctionsService;

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
        List<Provider> Providers = comPub.getProviders();
         if (!Providers.get(0).isMain) {
        Providers.add(0, new Provider("AuthorsProvider", constantService.getAuthorsProviderGraph(), sparqlService, true));  } 

         // Providers.add(0, new Provider("AuthorsProvider", constantService.getAuthorsProviderGraph(), sparqlService));
        return Providers;
    }

    @Override
    public void Proccess(String[] orgs) {
        try {
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

            int iasa = count(constantService.getAuthorsSameAsGraph());
            do {
                ProcessCoauthors(Providers, true, true);
                int asa = count(constantService.getAuthorsSameAsGraph());
                if (asa != iasa) {
                    iasa = asa;
                } else {
                    break;
                }
            } while (true);
            task.updateDetailMessage("Status", String.format("%s Disambiguation", "Coauthors"));
            delete(constantService.getCoauthorsSameAsGraph());
            ProcessCoauthors(Providers, false, true);
            task.updateDetailMessage("Status", String.format("%s Disambiguation", "Publications"));
            delete(constantService.getPublicationsSameAsGraph());
            ProcessPublications(Providers);
            task.updateDetailMessage("Status", String.format("%s Remove", "Duplicates"));
            log.info("Remove Duplicates");
            removeDuplicates(constantService.getAuthorsSameAsGraph());
            removeDuplicates(constantService.getPublicationsSameAsGraph());
            removeDuplicates(constantService.getCoauthorsSameAsGraph());
            log.info("Upload Logs");
            updateLogs(providersResult);
        } catch (Exception ex) {
            log.error("Unknown error while disambiguating");
            ex.printStackTrace();
        }
        taskManagerService.endTask(task);
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

    public void InitAuthorsProvider() throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {
        boolean ask = sparqlService.ask(QueryLanguage.SPARQL, "ask from <" + constantService.getAuthorsProviderGraph() + "> { ?a ?b ?c }");
        if (ask) {
            return;
        }

        String delete = "delete {\n"
                + "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                + "		?a ?b ?c .\n"
                + "	}\n"
                + "} where {\n"
                + "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                + "		?a ?b ?c .\n"
                + "	}\n"
                + "}";
        String copy = "insert {\n"
                + "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                + "		?a ?b ?c .\n"
                + "	}\n"
                + "} where {\n"
                + "	graph <" + constantService.getAuthorsGraph() + "> {\n"
                + "		?a ?b ?c .\n"
                + "	}\n"
                + "}";
        String givName = "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "insert {\n"
                + "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                + "		?a foaf:givenName ?c .\n"
                + "	}\n"
                + "} where {\n"
                + "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                + "		?a foaf:firstName ?c .\n"
                + "	}\n"
                + "}";
        String famName = "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "insert {\n"
                + "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                + "		?a foaf:familyName ?c .\n"
                + "	}\n"
                + "} where {\n"
                + "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                + "		?a foaf:lastName ?c .\n"
                + "	}\n"
                + "}";
        String org = "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "prefix  schema: <http://schema.org/>\n"
                + "insert {\n"
                + "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                + "		?a schema:memberOf ?o .\n"
                + "		?o a foaf:Organization .\n"
                + "		?o foaf:name ?n .\n"
                + "		?o foaf:name ?nn .\n"
                + "	}\n"
                + "} where {\n"
                + "	graph <" + constantService.getAuthorsProviderGraph() + "> {\n"
                + "		?a <http://purl.org/dc/terms/provenance> ?p .\n"
                + "	}\n"
                + "	graph <" + constantService.getEndpointsGraph() + "> {\n"
                + "		?p <http://ucuenca.edu.ec/ontology#belongTo> ?o .\n"
                + "	}\n"
                + "	graph <" + constantService.getOrganizationsGraph() + "> {\n"
                + "		?o <http://ucuenca.edu.ec/ontology#fullName> ?n .\n"
                + "		?o <http://ucuenca.edu.ec/ontology#name> ?nn .\n"
                + "	}\n"
                + "}";
        sparqlService.update(QueryLanguage.SPARQL, delete);
        sparqlService.update(QueryLanguage.SPARQL, copy);
        sparqlService.update(QueryLanguage.SPARQL, givName);
        sparqlService.update(QueryLanguage.SPARQL, famName);
        sparqlService.update(QueryLanguage.SPARQL, org);
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
        ExecutorService executorServicex = Executors.newFixedThreadPool(MAXTHREADS);
        BoundedExecutor bexecutorService = new BoundedExecutor(executorServicex, MAXTHREADS);
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
            Person aSeedAuthor = allAuthors.get(i);
            final List<Map.Entry<Provider, List<Person>>> Candidates = new ArrayList<>();
            List<Map.Entry<Provider, List<Person>>> CandidatesI = new ArrayList<>();
            Candidates.add(new AbstractMap.SimpleEntry<Provider, List<Person>>(MainAuthorsProvider, Lists.newArrayList(aSeedAuthor)));
            for (int j = 1; j < AuthorsProviderslist.size(); j++) {
                Provider aSecondaryProvider = AuthorsProviderslist.get(j);
                List<Person> aProviderCandidates = aSecondaryProvider.getCandidates(aSeedAuthor.URI);
                List<Person> aProviderCandidatesI = aSecondaryProvider.getCandidates(aSeedAuthor.URI);
                if (!aProviderCandidates.isEmpty()) {
                    Candidates.add(new AbstractMap.SimpleEntry<>(aSecondaryProvider, aProviderCandidates));
                    CandidatesI.add(new AbstractMap.SimpleEntry<>(aSecondaryProvider, aProviderCandidatesI));

                }
                ProvidersElements.put(AuthorsProviderslist.get(j), aProviderCandidates.size());
            }
            Candidates.addAll(Lists.reverse(CandidatesI));
            bexecutorService.submitTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        task.updateDetailMessage("Status", String.format("Start disambiguating %s  out of %s  authors", ix, allx));
                        log.info("Start disambiguating {} out of {} authors", ix, allx);
                        for (Map.Entry<Provider, List<Person>> aCandidateList : Candidates) {
                            aCandidateList.getKey().FillData(aCandidateList.getValue());
                        }
                        Disambiguate(Candidates, 0, new Person());
                        task.updateDetailMessage("Status", String.format("Finish disambiguating %s out of %s authors", ix, allx));
                        log.info("Finish disambiguating {} out of {} authors", ix, allx);

                    } catch (Exception ex) {
                        log.error("Unknown error while disambiguating");
                        ex.printStackTrace();
                    }
                }
            });
        }
        bexecutorService.end();
        return ProvidersElements;
    }

    public void Disambiguate(List<Map.Entry<Provider, List<Person>>> Candidates, int level, Person superAuthor) throws MarmottaException, RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException, InvalidArgumentException, UpdateExecutionException {
        if (level >= Candidates.size()) {
            return;
        }
        List<Person> CandidateListLevel = Candidates.get(level).getValue();
        boolean up = true;
        for (Person aCandidate : CandidateListLevel) {
            if (superAuthor.check(aCandidate)) {
                up = false;
                Person enrich = superAuthor.enrich(aCandidate);
                registerSameAs(constantService.getAuthorsSameAsGraph(), superAuthor.URI, aCandidate.URI);
                Disambiguate(Candidates, level + 1, enrich);
            }
        }
        if (up) {
            Disambiguate(Candidates, level + 1, superAuthor);
        }
    }

    public void ProcessCoauthors(final List<Provider> ProvidersList, final boolean onlySameAs, boolean asyn) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException, InterruptedException {
        ExecutorService executorServicex = Executors.newFixedThreadPool(asyn ? MAXTHREADS : 1);
        BoundedExecutor bexecutorService = new BoundedExecutor(executorServicex, asyn ? MAXTHREADS : 1);

        String qryDisambiguatedCoauthors = " select distinct ?p { graph <" + constantService.getAuthorsSameAsGraph() + "> { ?p <http://www.w3.org/2002/07/owl#sameAs> ?o } }";
        final List<Map<String, Value>> queryResponse = sparqlService.query(QueryLanguage.SPARQL, qryDisambiguatedCoauthors);
        int i = 0;
        for (Map<String, Value> anAuthor : queryResponse) {
            final int ix = i;
            final String authorURI = anAuthor.get("p").stringValue();
            log.info("Start disambiguating coauthors {} out of {}", i, queryResponse.size());
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
            i++;
        }
        bexecutorService.end();
    }

    public List<Person> getPersons(List<Map<String, Value>> list) {
        Map<String, Person> mp = new HashMap<>();
        for (Map<String, Value> a : list) {
            Person auxPerson = getAuxPerson(a);
            if (mp.containsKey(auxPerson.URI)) {
                Person get = mp.get(auxPerson.URI);
                Person enrich = get.enrich(auxPerson);
                mp.put(auxPerson.URI, enrich);
            } else {
                mp.put(auxPerson.URI, auxPerson);
            }
        }
        return new ArrayList(mp.values());
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
        List<Map<String, Value>> queryResponsex = sparqlService.query(QueryLanguage.SPARQL, qryAllAuthors);
        List<Person> queryResponse = getPersons(queryResponsex);
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
                    Set<String> auxGroup = null;
                    for (Set<String> potentialGroup : coauthorsGroups) {
                        if (potentialGroup.contains(auxPersoni.URI) || potentialGroup.contains(auxPersonj.URI)) {
                            auxGroup = potentialGroup;
                            break;
                        }
                    }
                    if (auxGroup == null) {
                        coauthorsGroups.add(aGroup);
                    } else {
                        auxGroup.addAll(aGroup);
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
            String firstIndex = null;

            String coauthoruris = "  ";
            for (String aProvider : eachGroup) {
                coauthoruris += " <" + aProvider + "> ";
            }

            String qry = "select distinct ?a {\n"
                    + "		graph <" + constantService.getAuthorsSameAsGraph() + "> {\n"
                    + "			values ?pa { " + coauthoruris + "  } .\n"
                    + "			?a <http://www.w3.org/2002/07/owl#sameAs> ?pa .\n"
                    + "		}\n"
                    + "}";

            List<Map<String, Value>> query = sparqlService.query(QueryLanguage.SPARQL, qry);
            for (String groupIndex : eachGroup) {
                if (firstIndex == null) {
                    firstIndex = groupIndex;
                }
                String URICoauthorA = firstIndex;
                String URICoauthorB = groupIndex;
                String newURI = constantService.getAuthorResource() + Cache.getMD5(URICoauthorA);
                if (query.isEmpty() && !onlySameAs) {
                    registerSameAs(constantService.getCoauthorsSameAsGraph(), newURI, URICoauthorB);
                } else {
                    for (Map<String, Value> re : query) {
                        registerSameAs(constantService.getAuthorsSameAsGraph(), re.get("a").stringValue(), URICoauthorB);
                    }
                }
            }
        }
    }

    public void ProcessPublications(final List<Provider> ProvidersList) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException, InterruptedException {
        ExecutorService executorServicex = Executors.newFixedThreadPool(MAXTHREADS);
        BoundedExecutor bexecutorService = new BoundedExecutor(executorServicex, MAXTHREADS);
        String qryDisambiguatedAuthors = " select distinct ?p { graph <" + constantService.getAuthorsSameAsGraph() + "> { ?p <http://www.w3.org/2002/07/owl#sameAs> ?o } }";
        final List<Map<String, Value>> queryResponse = sparqlService.query(QueryLanguage.SPARQL, qryDisambiguatedAuthors);
        int i = 0;
        for (Map<String, Value> anAuthor : queryResponse) {
            final int ix = i;
            final String authorURI = anAuthor.get("p").stringValue();
            log.info("Start disambiguating publications {} out of {} authors", i, queryResponse.size());
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
            String firstIndex = null;
            for (String groupIndex : eachGroup) {
                if (firstIndex == null) {
                    firstIndex = groupIndex;
                }
                String URIPublicationA = firstIndex;
                String URIPublicationB = groupIndex;
                String newURI = constantService.getPublicationResource() + Cache.getMD5(URIPublicationA);
                registerSameAs(constantService.getPublicationsSameAsGraph(), newURI, URIPublicationB);
            }
        }
    }

    public void registerSameAs(String graph, String URIO, String URIP) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {
        if (URIO != null && URIP != null && URIO.compareTo(URIP) != 0) {
            String buildInsertQuery = buildInsertQuery(graph, URIO, "http://www.w3.org/2002/07/owl#sameAs", URIP);
            sparqlService.update(QueryLanguage.SPARQL, buildInsertQuery);
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
            List<Provider> Providers = getProviders();
            log.info("Merging raw data ...");
            mergeRawData(Providers, constantService.getCentralGraph() + "TempRaw");
            log.info("Merging Same As ...");
            addAll(constantService.getCentralGraph() + "TempAllSameAs", constantService.getAuthorsSameAsGraph());
            addAll(constantService.getCentralGraph() + "TempAllSameAs", constantService.getPublicationsSameAsGraph());
            addAll(constantService.getCentralGraph() + "TempAllSameAs", constantService.getCoauthorsSameAsGraph());
            log.info("Replacing subjects ...");
            replaceSameAs(constantService.getCentralGraph() + "TempRaw", constantService.getCentralGraph() + "TempAllSameAs",
                    constantService.getCentralGraph() + "TempAllSameAsD1", constantService.getCentralGraph() + "TempAllSameAsI1", true);
            log.info("Deleting old subjects ...");
            minus(constantService.getCentralGraph() + "TempRaw2", constantService.getCentralGraph() + "TempRaw", constantService.getCentralGraph() + "TempAllSameAsD1");
            log.info("Adding new subjects ...");
            addAll(constantService.getCentralGraph() + "TempRaw2", constantService.getCentralGraph() + "TempAllSameAsI1");
            log.info("Replacing objects ...");
            replaceSameAs(constantService.getCentralGraph() + "TempRaw2", constantService.getCentralGraph() + "TempAllSameAs",
                    constantService.getCentralGraph() + "TempAllSameAsD2", constantService.getCentralGraph() + "TempAllSameAsI2", false);
            log.info("Deleting old objects ...");
            minus(constantService.getCentralGraph(), constantService.getCentralGraph() + "TempRaw2", constantService.getCentralGraph() + "TempAllSameAsD2");
            log.info("Adding new objects ...");
            addAll(constantService.getCentralGraph(), constantService.getCentralGraph() + "TempAllSameAsI2");
            log.info("Adding sameAs triples ...");
            addAll(constantService.getCentralGraph(), constantService.getCentralGraph() + "TempAllSameAs");
            log.info("Deleting temporals ...");
            delete(constantService.getCentralGraph() + "TempRaw");
            delete(constantService.getCentralGraph() + "TempRaw2");
            delete(constantService.getCentralGraph() + "TempAllSameAs");
            delete(constantService.getCentralGraph() + "TempAllSameAsD1");
            delete(constantService.getCentralGraph() + "TempAllSameAsI1");
            delete(constantService.getCentralGraph() + "TempAllSameAsD2");
            delete(constantService.getCentralGraph() + "TempAllSameAsI2");
            log.info("Finished merging ...");

        } catch (Exception ex) {
            log.error("Unknown error while merging Central Graph");
            ex.printStackTrace();
        }
    }

    public void replaceSameAs(String D, String SAG, String DG, String IG, boolean s) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {
        String qry1 = "insert {\n"
                + "	graph <" + DG + "> {\n"
                + "		?c ?p ?v .\n"
                + "	}\n"
                + "	graph <" + IG + "> {\n"
                + "		?a ?p ?v .\n"
                + "	}\n"
                + "}\n"
                + "where {\n"
                + "	graph <" + SAG + "> {\n"
                + "		?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "	}\n"
                + "	graph <" + D + "> {\n"
                + "		?c ?p ?v .\n"
                + "	}\n"
                + "}";

        String qry2 = "insert {\n"
                + "	graph <" + DG + "> {\n"
                + "		?v ?p ?c .\n"
                + "	}\n"
                + "	graph <" + IG + "> {\n"
                + "		?v ?p ?a .\n"
                + "	}\n"
                + "}\n"
                + "where {\n"
                + "	graph <" + SAG + "> {\n"
                + "		?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "	}\n"
                + "	graph <" + D + "> {\n"
                + "		?v ?p ?c .\n"
                + "	}\n"
                + "}";
        if (s) {
            sparqlService.update(QueryLanguage.SPARQL, qry1);
        } else {
            sparqlService.update(QueryLanguage.SPARQL, qry2);
        }
    }

    public void mergeRawData(List<Provider> ProvidersList, String graph) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {
        String providersGraphs = "  ";
        for (Provider aProvider : ProvidersList) {
            providersGraphs += " <" + aProvider.Graph + "> ";
        }
        String qry1 = "insert {\n"
                + "    graph <" + graph + "> {\n"
                + "        ?c ?p ?v .\n"
                + "    }\n"
                + "} where {\n"
                + "    graph <" + constantService.getAuthorsSameAsGraph() + "> {\n"
                + "        ?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "    }\n"
                + "    values ?g { " + providersGraphs + " } graph ?g {\n"
                + "        ?c ?p ?v .\n"
                + "    }\n"
                + "}";

        String qry2 = "insert {\n"
                + "    graph <" + graph + "> {\n"
                + "        ?v ?w ?q .\n"
                + "    }\n"
                + "} where {\n"
                + "    graph <" + constantService.getAuthorsSameAsGraph() + "> {\n"
                + "        ?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "    }\n"
                + "    values ?g { " + providersGraphs + " } graph ?g {\n"
                + "         ?c ?p ?v .\n"
                + "         ?v ?w ?q .\n"
                + "    }\n"
                + "}";
        String qry3 = "insert {\n"
                + "    graph <" + graph + "> {\n"
                + "        ?q ?z ?m .\n"
                + "    }\n"
                + "} where {\n"
                + "    graph <" + constantService.getAuthorsSameAsGraph() + "> {\n"
                + "        ?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "    }\n"
                + "    values ?g { " + providersGraphs + " } graph ?g {\n"
                + "         ?c ?p ?v .\n"
                + "         ?v ?w ?q .\n"
                + "        ?q ?z ?m .\n"
                + "    }\n"
                + "}";

        sparqlService.update(QueryLanguage.SPARQL, qry1);
        sparqlService.update(QueryLanguage.SPARQL, qry2);
        sparqlService.update(QueryLanguage.SPARQL, qry3);
    }

    public int count(String graph) throws MarmottaException {
        String c = "select (count (*) as ?co) { graph <" + graph + "> { ?a ?b ?c }}";
        List<Map<String, Value>> query = sparqlService.query(QueryLanguage.SPARQL, c);
        int parseInt = Integer.parseInt(query.get(0).get("co").stringValue());
        return parseInt;
    }

    public void delete(String graph) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        String d = "delete { graph <" + graph + "> { ?a ?b ?c }} where { graph <" + graph + "> { ?a ?b ?c }} ";
        sparqlService.update(QueryLanguage.SPARQL, d);
    }

    public void addAll(String graph, String graph1) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        String d = "insert { graph <" + graph + "> { ?a ?b ?c }} where { graph <" + graph1 + "> { ?a ?b ?c }} ";
        sparqlService.update(QueryLanguage.SPARQL, d);
    }

    public void minus(String graph, String graph1, String graph2) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        String d = "insert {\n"
                + "	graph <" + graph + "> {\n"
                + "		?a ?b ?c .\n"
                + "	}\n"
                + "} where {	\n"
                + "	graph <" + graph1 + "> {\n"
                + "		?a ?b ?c \n"
                + "		filter not exists {\n"
                + "			graph <" + graph2 + "> {\n"
                + "				?a ?b ?c .\n"
                + "			}\n"
                + "		}		\n"
                + "	}\n"
                + "}";
        sparqlService.update(QueryLanguage.SPARQL, d);
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

    class BoundedExecutor {

        private final Executor exec;
        private final Semaphore semaphore;

        public BoundedExecutor(Executor exec, int bound) {
            this.exec = exec;
            this.semaphore = new Semaphore(bound);
        }

        public void submitTask(final Runnable command)
                throws InterruptedException {
            semaphore.acquire();
            try {
                exec.execute(new Runnable() {
                    public void run() {
                        try {
                            command.run();
                        } finally {
                            semaphore.release();
                        }
                    }
                });
            } catch (RejectedExecutionException e) {
                semaphore.release();
            }
        }

        public void end() throws InterruptedException {
            ExecutorService sexec = (ExecutorService) exec;
            sexec.shutdown();
            sexec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        }
    }

}
