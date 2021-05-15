/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
import org.apache.marmotta.ucuenca.wk.commons.service.DisambiguationUtilsService;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.apache.marmotta.ucuenca.wk.commons.service.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.commons.util.BoundedExecutor;
import org.apache.marmotta.ucuenca.wk.commons.util.LongUpdateQueryExecutor;
import org.apache.marmotta.ucuenca.wk.commons.util.SPARQLUtils;
import org.apache.marmotta.ucuenca.wk.pubman.api.IdentificationManager;
import org.apache.marmotta.ucuenca.wk.pubman.utils.BucketType;
import org.apache.marmotta.ucuenca.wk.pubman.utils.MapSet;
import org.apache.marmotta.ucuenca.wk.pubman.utils.MapSetWID;
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

    final int MAXTHREADS = 4;

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

    @Inject
    private DisambiguationUtilsService test;
    private Task task;

    @Inject
    private IdentificationManager idm;

    private Thread DisambiguationWorker;
    private Thread CentralGraphWorker;

    private static final String ASA_C = "Complete";

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
                ProcessAuthors(Providers, null);
                sparqlUtils.copyGraph(constantService.getAuthorsSameAsGraph(), constantService.getAuthorsSameAsGraph() + ASA_C);
            }

            for (int w0 = 0; w0 < 5; w0++) {
                completeLinkage(Providers);
            }
//            saveLinks();
//            applyManualFix();
//            checkWarnings();
//            CentralGraphGenerator centralGraphGenerator = new CentralGraphGenerator(constantService.getCentralGraph() + "x", constantService.getBaseContext() + "buckets", Providers);
//            centralGraphGenerator.run(idm, sparqlService, log);
//merge collections
            //mergeCollections();
            //mergeSubjects();
        } catch (Exception ex) {
            try {
                sparqlService.getGraphDBInstance().dumpBuffer();
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
            log.error("Unknown error while disambiguating");
            ex.printStackTrace();
        } finally {
            try {
                sparqlService.getGraphDBInstance().dumpBuffer();
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
            taskManagerService.endTask(task);
        }

    }

    public void checkWarnings() throws MarmottaException, InterruptedException, RepositoryException, RDFHandlerException, MalformedQueryException, UpdateExecutionException, Exception {
        String q = "        PREFIX redi: <https://redi.cedia.edu.ec/ont#>\n"
                + "        PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                + "\n"
                + "        select ?b (group_concat(distinct ?e ; separator='||||||') as ?uris) {\n"
                + "\n"
                + "            graph <" + constantService.getAuthorsSameAsGraph() + ASA_C + "> {\n"
                + "                ?e owl:sameAs [] .\n"
                + "            }\n"
                + "            graph <" + idm.getGraph() + "> {\n"
                + "                ?b redi:type 'author' .\n"
                + "                ?b redi:element ?e .\n"
                + "            }\n"
                + "        } group by ?b having (count(distinct ?e) > 1)";
        final ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
        BoundedExecutor threadPool = BoundedExecutor.getThreadPool(MAXTHREADS);

        List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, q);
        int ix = 0;
        for (final Map<String, Value> x : query) {
            log.info("warnings {}/{}", ix++, query.size());
            threadPool.submitTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        Model m = new LinkedHashModel();
                        String b = x.get("b").stringValue();
                        Set<String> ur = Sets.newHashSet(x.get("uris").stringValue().split("\\|\\|\\|\\|\\|\\|"));

                        String groq = "";
                        for (String rqq : ur) {
                            groq += "<" + rqq + "> ";
                        }
                        String q2 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                                + "select ?a ?n ?fn ?ln { \n"
                                + "  graph <" + constantService.getAuthorsProviderGraph() + "> { \n"
                                + "  values ?a { " + groq + " } . \n"
                                + "    optional { ?a <http://xmlns.com/foaf/0.1/name> ?n . }\n"
                                + "    optional { ?a <http://xmlns.com/foaf/0.1/givenName> ?fn . }\n"
                                + "    optional { ?a <http://xmlns.com/foaf/0.1/familyName> ?ln . }\n"
                                + "  }\n"
                                + "}";
                        List<Map<String, Value>> rx;

                        rx = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, q2);

                        Map<String, Person> persons = getPersons(rx);
                        List<String> ls = new ArrayList<>(persons.keySet());

                        for (int i = 0; i < ls.size(); i++) {
                            for (int j = i + 1; j < ls.size(); j++) {
                                Person get1 = persons.get(ls.get(i));
                                Person get2 = persons.get(ls.get(j));
                                Boolean checkName1 = get1.checkName(get2, true);
                                Boolean checkName2 = get2.checkName(get1, true);
                                Boolean xc = checkName1 != null && checkName2 != null && !checkName1 && !checkName2;
                                if (xc) {
                                    m.add(instance.createURI(b), instance.createURI("https://redi.cedia.edu.ec/ont#element"), instance.createURI(get1.URI));
                                    m.add(instance.createURI(b), instance.createURI("https://redi.cedia.edu.ec/ont#element"), instance.createURI(get2.URI));
                                }
                            }
                        }
                        sparqlService.getGraphDBInstance().addBuffer(instance.createURI(idm.getGraph() + "Warnings"), m);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
        threadPool.end();
        sparqlService.getGraphDBInstance().dumpBuffer();
    }

    public void applyManualFix() throws MarmottaException, InterruptedException, RepositoryException, RDFHandlerException, MalformedQueryException, UpdateExecutionException, Exception {
        idm.applyFix();
    }

    public void saveLinks() throws MarmottaException, InterruptedException, RepositoryException, RDFHandlerException, MalformedQueryException, UpdateExecutionException, Exception {

        final Map<BucketType, Long> counters = idm.getCounters();
        Map<String, BucketType> m = new HashMap<>();
        m.put(constantService.getAuthorsSameAsGraph() + ASA_C, BucketType.author);
        m.put(constantService.getCoauthorsSameAsGraph(), BucketType.ext_author);
        m.put(constantService.getPublicationsSameAsGraph(), BucketType.publication);
        BoundedExecutor threadPool = BoundedExecutor.getThreadPool(MAXTHREADS);
        for (final Entry<String, BucketType> it : m.entrySet()) {
            threadPool.submitTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        saveLinks(it.getKey(), it.getValue(), counters);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
        threadPool.end();
        idm.saveCounters(counters);
    }

    private void saveLinks(final String graph, final BucketType t, final Map<BucketType, Long> tc) throws MarmottaException, Exception {
        final MapSetWID bucketsContent = idm.getBucketsContent(t);
        String q = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "PREFIX dct: <http://purl.org/dc/terms/>\n"
                + "\n"
                + "select * {   \n"
                + "    graph <" + graph + "> {\n"
                + "        ?b owl:sameAs ?e .\n"
                + "    }\n"
                + "}";
        final List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, q);

        Map<String, Set<String>> b = new HashMap<>();
        for (Map<String, Value> m : query) {
            if (!b.containsKey(m.get("b").stringValue())) {
                b.put(m.get("b").stringValue(), new HashSet<String>());
            }
            b.get(m.get("b").stringValue()).add(m.get("e").stringValue());
        }
        int i = 0;
        for (Entry<String, Set<String>> next : b.entrySet()) {
            idm.addBucket(t, next.getValue(), tc, bucketsContent);
            log.info("{} {} / {}", t, i, b.size());
            i++;
        }
        idm.saveBucketsContent(bucketsContent, t);
    }

    public void mergeSubjects() throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException, RepositoryException, RDFHandlerException, Exception {
        List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, "PREFIX dct: <http://purl.org/dc/terms/>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                + "select distinct ?s ?sx {\n"
                + "    graph <" + constantService.getCentralGraph() + "> {\n"
                + "        ?p dct:subject ?s .\n"
                + "        ?s rdfs:label ?l .\n"
                + "        bind (replace (replace (lcase(str(?l)), 'ü|ñ|á|é|í|ó|ú|a|e|i|o|u|,|;|:|-|\\\\(|\\\\)|\\\\||\\\\.' ,' '), ' ' ,'') as ?t1) .\n"
                + "        optional {\n"
                + "            ?p dct:subject ?sx .\n"
                + "        	?sx rdfs:label ?lx .\n"
                + "        	bind (replace (replace (lcase(str(?lx)), 'ü|ñ|á|é|í|ó|ú|a|e|i|o|u|,|;|:|-|\\\\(|\\\\)|\\\\||\\\\.' ,' '), ' ' ,'') as ?t2) .\n"
                + "            filter (str(?s) > str(?sx)) .\n"
                + "            filter (str(?t1) = str(?t2)) .\n"
                + "        }\n"
                + "    }\n"
                + "} ");
        MapSet ms = new MapSet(new HashMap<String, Set<String>>());
        for (Map<String, Value> mp : query) {
            Value get1 = mp.get("sx");
            if (get1 == null) {
                ms.put(mp.get("s").stringValue());
            } else {
                ms.put(mp.get("s").stringValue(), get1.stringValue());
            }
        }
        Set<Set<String>> journalsGroups = new HashSet(ms.values());
        Map<BucketType, Long> counters = idm.getCounters();
        MapSetWID bucketsContent = idm.getBucketsContent(BucketType.subject);
        for (Set<String> eachGroup : journalsGroups) {
            idm.addBucket(BucketType.subject, eachGroup, counters, bucketsContent);
        }
        idm.saveCounters(counters);
        idm.saveBucketsContent(bucketsContent, BucketType.subject);
    }

    public void mergeCollections() throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException, RepositoryException, RDFHandlerException, Exception {
        List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, "PREFIX dct: <http://purl.org/dc/terms/>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                + "select distinct ?c ?cx {\n"
                + "            graph <" + constantService.getCentralGraph() + "> {\n"
                + "                ?p dct:isPartOf ?c .\n"
                + "                ?c a ?t .\n"
                + "        		?c rdfs:label ?cr .\n"
                + "        		bind (replace (replace (lcase(str(?cr)), 'ü|ñ|á|é|í|ó|ú|a|e|i|o|u|,|;|:|-|\\\\(|\\\\)|\\\\||\\\\.' ,' '), ' ' ,'') as ?t1) .\n"
                + "        		optional {\n"
                + "                    ?p dct:isPartOf ?cx .\n"
                + "                    ?cx a ?t .\n"
                + "            		?cx rdfs:label ?cxr .\n"
                + "            		bind (replace (replace (lcase(str(?cxr)), 'ü|ñ|á|é|í|ó|ú|a|e|i|o|u|,|;|:|-|\\\\(|\\\\)|\\\\||\\\\.' ,' '), ' ' ,'') as ?t2) .\n"
                + "            		filter (?t1 = ?t2) .\n"
                + "            		filter (str(?c) > str(?cx)) .\n"
                + "        		}\n"
                + "            }\n"
                + "}");
        MapSet ms = new MapSet(new HashMap<String, Set<String>>());
        for (Map<String, Value> mp : query) {
            Value get1 = mp.get("cx");
            if (get1 == null) {
                ms.put(mp.get("c").stringValue());
            } else {
                ms.put(mp.get("c").stringValue(), get1.stringValue());
            }
        }
        Set<Set<String>> journalsGroups = new HashSet(ms.values());
        Map<BucketType, Long> counters = idm.getCounters();
        MapSetWID bucketsContent = idm.getBucketsContent(BucketType.collection);
        for (Set<String> eachGroup : journalsGroups) {
            idm.addBucket(BucketType.collection, eachGroup, counters, bucketsContent);
        }
        idm.saveCounters(counters);
        idm.saveBucketsContent(bucketsContent, BucketType.collection);
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
                + "		?o <http://www.eurocris.org/ontologies/cerif/1.3#acronym> ?nn .\n"
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
            //Check Harvested Data
            List<Map<String, Value>> queryResponse = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, "select ?g ?p {\n"
                    + "    values ?g { " + harvestedProvidersList + " } .\n"
                    + "    graph ?g {\n"
                    + "        optional {\n"
                    + "   			?q <http://www.w3.org/2002/07/owl#oneOf> <" + aSeedAuthor.URI + "> .\n"
                    + "         ?p <http://www.w3.org/2002/07/owl#oneOf> ?q.\n"
                    + "        }\n"
                    + "    }\n"
                    + "}");
            Map<String, Set<String>> qRHM = new HashMap<>();
            for (Map<String, Value> aresh : queryResponse) {
                String g = aresh.get("g").stringValue();
                if (!qRHM.containsKey(g)) {
                    qRHM.put(g, new HashSet<String>());
                }
                if (aresh.get("p") != null) {
                    qRHM.get(g).add(aresh.get("p").stringValue());
                }
            }
            String harvestedProvidersListRes = "";
            for (int j = 1; j < AuthorsProviderslist.size(); j++) {
                Set<String> get = qRHM.get(AuthorsProviderslist.get(j).Graph);
                harvestedProvidersListRes += !get.isEmpty() ? "1" : "0";
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
                    if (!qRHM.get(aSecondaryProvider.Graph).isEmpty()) {
                        List<Person> aProviderCandidates = aSecondaryProvider.getCandidates(qRHM.get(aSecondaryProvider.Graph));
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
                            boolean alreadyHasPublicationsOrProjects = sparqlService.getSparqlService().ask(QueryLanguage.SPARQL, "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "prefix cerif: <https://www.openaire.eu/cerif-profile/1.1/> \n"
                                    + "ask from <" + constantService.getAuthorsProviderGraph() + "> {\n"
                                    + "	<" + aSeedAuthor.URI + "> foaf:publications|cerif:MemberOf [] .\n"
                                    + "}");
                            if (alreadyHasPublicationsOrProjects || Disambiguate.size() > 0) {
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
        sparqlService.getGraphDBInstance().dumpBuffer();
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

    public void completeLinkage(final List<Provider> ProvidersList) throws MarmottaException, InterruptedException, RepositoryException, RDFHandlerException, MalformedQueryException, UpdateExecutionException {
        final Map<String, Set<String>> pg = new ConcurrentHashMap<>();
        final MapSet ms = new MapSet(pg);
        SPARQLUtils sparqlUtils = new SPARQLUtils(sparqlService.getSparqlService());
        BoundedExecutor bexecutorService = BoundedExecutor.getThreadPool(MAXTHREADS);
        String qryDisambiguatedCoauthors = " select distinct ?p { "
                + " graph <" + constantService.getAuthorsSameAsGraph() + ASA_C + "> { "
                + "     ?p <http://www.w3.org/2002/07/owl#sameAs> ?o "
                + " } "
                + "}";
        final List<Map<String, Value>> queryResponse = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, qryDisambiguatedCoauthors);
        int i = 0;
        for (Map<String, Value> anAuthor : queryResponse) {
            if (i % 5000 == 0) {
                System.gc();
            }
            final int ix = i;
            final String authorURI = anAuthor.get("p").stringValue();
            task.updateDetailMessage("Threads", bexecutorService.workingThreads() + "");
            task.updateDetailMessage("Status", "" + i);
            bexecutorService.submitTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        log.info("Processing second-level linkage (publications) {}/{}, {}", ix, queryResponse.size(), authorURI);
                        completePublications(ProvidersList, authorURI, ms);
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
        sparqlUtils.deleteGraph(constantService.getPublicationsSameAsGraph());
        Set<Set<String>> publicationsGroups = new HashSet(ms.values());
        for (Set<String> eachGroup : publicationsGroups) {
            registerSameAsBucket(true, constantService.getPublicationsSameAsGraph(), eachGroup, "publications");
        }
        sparqlService.getGraphDBInstance().dumpBuffer();
        ms.clear();
        bexecutorService = BoundedExecutor.getThreadPool(MAXTHREADS);//MAXTHREADS * 2
        qryDisambiguatedCoauthors = " select distinct ?p { "
                + " graph <" + constantService.getPublicationsSameAsGraph() + "> { "
                + "     ?p <http://www.w3.org/2002/07/owl#sameAs> ?o "
                + " } "
                + "}";
        final List<Map<String, Value>> queryResponse2 = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, qryDisambiguatedCoauthors);
        i = 0;
        for (Map<String, Value> anPublication : queryResponse2) {
            if (i % 5000 == 0) {
                System.gc();
            }
            final int ix = i;
            final String pubURI = anPublication.get("p").stringValue();
            task.updateDetailMessage("Threads", bexecutorService.workingThreads() + "");
            task.updateDetailMessage("Status", "" + i);
            bexecutorService.submitTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        log.info("Processing second-level linkage (authors) {}/{}, {}", ix, queryResponse2.size(), pubURI);
                        completeCoauthors(ProvidersList, pubURI, ms);
                    } catch (Exception ex) {
                        log.error("Unknown exception while disambiguating coauthors");
                        log.info("Retrying ... {}", pubURI);
                        ex.printStackTrace();
                        run();
                    }
                }
            });
            task.updateDetailMessage("Threads", bexecutorService.workingThreads() + "");
            i++;
        }
        bexecutorService.end();
        sparqlUtils.deleteGraph(constantService.getCoauthorsSameAsGraph());
        Set<Set<String>> coauthorsGroups = new HashSet(ms.values());
        for (Set<String> eachGroup : coauthorsGroups) {
            registerSameAsBucket(true, constantService.getCoauthorsSameAsGraph(), eachGroup, "coauthors");
        }
        sparqlService.getGraphDBInstance().dumpBuffer();
        sparqlUtils.clearDifferent(constantService.getCoauthorsSameAsGraph(), constantService.getAuthorsGraph() + ASA_C);
        sparqlUtils.mergeSameAs(constantService.getCoauthorsSameAsGraph());

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

    public void completeCoauthors(List<Provider> ProvidersList, String pubURI, MapSet ms) throws MarmottaException, RepositoryException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException, RDFHandlerException {
        Map<String, Boolean> ch = Maps.newConcurrentMap();
        String providersGraphs = "  ";
        for (Provider aProvider : ProvidersList) {
            providersGraphs += " <" + aProvider.Graph + "> ";
        }
        String qryAllAuthors1 = "select distinct ?a {\n"
                + "    values ?g { " + providersGraphs + " } .\n"
                + "    graph <" + constantService.getPublicationsSameAsGraph() + "> {\n"
                + "     <" + pubURI + "> <http://www.w3.org/2002/07/owl#sameAs> ?p .\n"
                + "    } .\n"
                + "    {\n"
                + "        graph <" + constantService.getAuthorsSameAsGraph() + ASA_C + "> {\n"
                + "            ?a <http://www.w3.org/2002/07/owl#sameAs> ?ax .\n"
                + "        }\n"
                + "        graph ?g {\n"
                + "            ?ax <http://xmlns.com/foaf/0.1/publications> ?p .\n"
                + "        }\n"
                + "    } union {\n"
                + "        graph ?g {\n"
                + "            ?a <http://xmlns.com/foaf/0.1/publications> ?p .\n"
                + "        }\n"
                + "    }\n"
                + "}";

        List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, qryAllAuthors1);
        Map<String, Set<String>> mapgroups = new HashMap<>();
        Map<String, Person> queryResponse_mp = null;
        //too many coauthors to process
        if (query.size() == 1 || query.size() > 400) {
            for (Map<String, Value> q : query) {
                mapgroups.put(q.get("a").stringValue(), Sets.newHashSet(q.get("a").stringValue()));
            }
        } else {
            String urs = "";
            for (Map<String, Value> q : query) {
                urs += " <" + q.get("a").stringValue() + "> ";
            }

            String qryAllAuthors2 = "select distinct * {\n"
                    + "    {\n"
                    + "        select distinct ?a ?n ?aup {\n"
                    + "            {\n"
                    + "                select ?a (max(?l) as ?lx) (sample(?aupx) as ?aup) {\n"
                    + "                    values ?g { " + providersGraphs + " } .\n"
                    + "                    values ?a { " + urs + " } .\n"
                    + "                    graph ?g {\n"
                    + "                        ?a <http://xmlns.com/foaf/0.1/name> ?n .\n"
                    + "                        bind(strlen(?n) as ?l) .\n"
                    + "                    }\n"
                    + "                    graph <" + constantService.getAuthorsSameAsGraph() + ASA_C + "> {\n"
                    + "                        optional {\n"
                    + "                            ?a <http://www.w3.org/2002/07/owl#sameAs> [] .\n"
                    + "                            bind('1' as ?aupx) .\n"
                    + "                        }\n"
                    + "                    }\n"
                    + "                } group by ?a\n"
                    + "            } .\n"
                    + "            values ?g { " + providersGraphs + " } graph ?g {\n"
                    + "                    ?a <http://xmlns.com/foaf/0.1/name> ?n .\n"
                    + "                    bind(strlen(?n) as ?l) .\n"
                    + "            } .\n"
                    + "            filter (?lx = ?l) .\n"
                    + "        }  \n"
                    + "    } union{\n"
                    + "        select distinct ?a ?fn ?ln ?aup {\n"
                    + "            {\n"
                    + "                select ?a (max(?l) as ?lx) (sample(?aupx) as ?aup) {\n"
                    + "                    values ?g { " + providersGraphs + " } .\n"
                    + "                    values ?a { " + urs + " } .\n"
                    + "                    graph ?g {\n"
                    + "                        ?a <http://xmlns.com/foaf/0.1/givenName> ?fn .\n"
                    + "                        ?a <http://xmlns.com/foaf/0.1/familyName> ?ln .\n"
                    + "                        bind(strlen(concat(?fn,' ', ?ln)) as ?l) .\n"
                    + "                    }\n"
                    + "                    graph <" + constantService.getAuthorsSameAsGraph() + ASA_C + "> {\n"
                    + "                        optional {\n"
                    + "                            ?a <http://www.w3.org/2002/07/owl#sameAs> [] .\n"
                    + "                            bind('1' as ?aupx) .\n"
                    + "                        }\n"
                    + "                    }\n"
                    + "                } group by ?a\n"
                    + "            } .\n"
                    + "            values ?g { " + providersGraphs + " } graph ?g {\n"
                    + "                ?a <http://xmlns.com/foaf/0.1/givenName> ?fn .\n"
                    + "                ?a <http://xmlns.com/foaf/0.1/familyName> ?ln .\n"
                    + "                bind(strlen(concat(?fn,' ', ?ln)) as ?l) .\n"
                    + "            } .\n"
                    + "            filter (?lx = ?l) .\n"
                    + "        }\n"
                    + "    }\n"
                    + "}";

            queryResponse_mp = getPersons(qryAllAuthors2);
            List<Person> queryResponse = new ArrayList<>(queryResponse_mp.values());

            for (int i = 0; i < queryResponse.size(); i++) {
                for (int j = i + 1; j < queryResponse.size(); j++) {
                    Person auxPersoni = queryResponse.get(i);
                    Person auxPersonj = queryResponse.get(j);
                    String k1 = auxPersoni.URI;
                    String k2 = auxPersonj.URI;
                    String ks = Cache.getMD5(Cache.getMD5(k1) + Cache.getMD5(k2));
                    Boolean r = ch.get(ks);
                    if (r == null) {
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
                        r = checkName != null && checkName;
                        ch.put(ks, r);
                    }
                    if (r) {
                        Set<String> s1 = mapgroups.get(k1);
                        Set<String> s2 = mapgroups.get(k2);
                        if (s1 == null && s2 == null) {
                            Set<String> as = new HashSet<>();
                            as.add(k1);
                            as.add(k2);
                            mapgroups.put(k1, as);
                            mapgroups.put(k2, as);
                        } else if (s1 != null && s2 == null) {
                            s1.add(k2);
                            mapgroups.put(k2, s1);
                        } else if (s1 == null && s2 != null) {
                            s2.add(k1);
                            mapgroups.put(k1, s2);
                        } else if (s1 != null && s2 != null) {
                            Set<String> as = new HashSet<>();
                            as.addAll(s1);
                            as.addAll(s2);
                            for (String sx : as) {
                                mapgroups.put(sx, as);
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < queryResponse.size(); i++) {
                String k1 = queryResponse.get(i).URI;
                if (!mapgroups.containsKey(k1)) {
                    Set<String> hsalone = new HashSet<>();
                    hsalone.add(k1);
                    mapgroups.put(k1, hsalone);
                }
            }
        }

        Set<Set<String>> coauthorsGroups = new HashSet(mapgroups.values());
        for (Set<String> eachGroup : coauthorsGroups) {

            Map<String, Person> principal = Maps.newConcurrentMap();
            for (String groupIndex : eachGroup) {
                if (queryResponse_mp != null) {
                    Person get = queryResponse_mp.get(groupIndex);
                    if (get.Origin != null) {
                        principal.put(groupIndex, get);
                    }
                }
            }
            Set<String> minus = Sets.newHashSet(eachGroup);
            minus.removeAll(principal.keySet());

            Set<String> unkAuth = new HashSet<>();
            for (String groupIndex : minus) {
                boolean pros = false;
                if (queryResponse_mp != null) {
                    Person get = queryResponse_mp.get(groupIndex);
                    for (Person p : principal.values()) {
                        if (get != null) {
                            String ks = Cache.getMD5("_" + Cache.getMD5(p.URI) + Cache.getMD5(get.URI));
                            Boolean r = ch.get(ks);
                            if (r == null) {
                                Boolean checkName = p.checkName(get, true);
                                r = checkName != null && checkName;
                                ch.put(ks, r);
                            }

                            if (r) {
                                pros = true;
                                if (get.Origin == null) {
                                    registerSameAs(constantService.getAuthorsSameAsGraph() + ASA_C, p.URI, groupIndex);
                                }
                            }
                        }
                    }
                }
                if (!pros) {
                    unkAuth.add(groupIndex);
                }
            }
            ms.put(unkAuth);
        }
    }

    private void completePublications(List<Provider> ProvidersList, String personURI, MapSet ch) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException, RepositoryException, RDFHandlerException {
        String providersGraphs = "  ";
        for (Provider aProvider : ProvidersList) {
            providersGraphs += " <" + aProvider.Graph + "> ";
        }
        String qryAllPublications = "select distinct ?p ?t {\n"
                + "  	graph <" + constantService.getAuthorsSameAsGraph() + ASA_C + ">{\n"
                + "		<" + personURI + "> <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "    }\n"
                + "    values ?g { " + providersGraphs + " } graph ?g {\n"
                + "  	 ?c <http://xmlns.com/foaf/0.1/publications> ?p.\n"
                + "  	 ?p <http://purl.org/dc/terms/title> ?t_.\n"
                + "    bind (replace (replace (lcase(str(?t_)), 'ü|ñ|á|é|í|ó|ú|a|e|i|o|u|,|;|:|-|\\\\(|\\\\)|\\\\||\\\\.' ,' '), ' ' ,'') as ?t) ."
                + "    }\n"
                + "} ";
        List<Map<String, Value>> queryResponse = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, qryAllPublications);

        for (int i = 0; i < queryResponse.size(); i++) {
            for (int j = i + 1; j < queryResponse.size(); j++) {
                final String t_i = queryResponse.get(i).get("t").stringValue();
                final String t_j = queryResponse.get(j).get("t").stringValue();
                String k1 = queryResponse.get(i).get("p").stringValue();
                String k2 = queryResponse.get(j).get("p").stringValue();
                if (!k1.equals(k2)) {
                    if (PublicationUtils.compareTitleFast(t_i, t_j) >= Person.thresholdTitleFast) {
                        ch.put(k1, k2);
                    }
                }
            }
        }

        for (int i = 0; i < queryResponse.size(); i++) {
            String k1 = queryResponse.get(i).get("p").stringValue();
            ch.put(k1, null);
        }
    }

    public void registerSameAs(String graph, String URIO, String URIP) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException, RepositoryException, RDFHandlerException {
        if (URIO != null && URIP != null && URIO.compareTo(URIP) != 0) {
            //String buildInsertQuery = buildInsertQuery(graph, URIO, "http://www.w3.org/2002/07/owl#sameAs", URIP);
            //sparqlService.getSparqlService().update(QueryLanguage.SPARQL, buildInsertQuery);
            sparqlService.getGraphDBInstance().addBuffer(graph, URIO, "http://www.w3.org/2002/07/owl#sameAs", URIP);
        }
    }

    public void registerSameAsBucket(boolean same, String graph, Set<String> urs, String prefix) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException, RepositoryException, RDFHandlerException {
        List<String> list = new ArrayList(urs);
        Collections.sort(list);
        String eachGroupUUID = Cache.getMD5(list.size() > 0 ? list.get(0) : UUID.randomUUID().toString());
        String PossibleNewURI = constantService.getBaseResource() + prefix + "/" + eachGroupUUID;
        if (same) {
            sparqlService.getGraphDBInstance().addBufferBucket(graph, PossibleNewURI, "http://www.w3.org/2002/07/owl#sameAs", urs);
        } else {
            sparqlService.getGraphDBInstance().addBufferBucket(graph, PossibleNewURI, "http://www.w3.org/2002/07/owl#differentFrom", urs);
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
