/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.google.common.collect.Lists;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.function.Cache;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.DisambiguationService;
import org.apache.marmotta.ucuenca.wk.pubman.disambiguation.Person;
import org.apache.marmotta.ucuenca.wk.pubman.disambiguation.Provider;
import org.apache.marmotta.ucuenca.wk.pubman.disambiguation.utils.PublicationUtils;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;

/**
 *
 * @author cedia
 */
@ApplicationScoped
public class DisambiguationServiceImpl implements DisambiguationService {
    
    final int MAXTHREADS = 5;
    
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
    
    private Thread DisambiguationWorker;
    private Thread CentralGraphWorker;
    
    private List<Provider> getProviders() {
        Provider a0 = new Provider("Authors", constantService.getAuthorsProviderGraph(), sparqlService);
        Provider a1 = new Provider("Scopus", constantService.getScopusGraph(), sparqlService);
        Provider a2 = new Provider("MSAK", constantService.getAcademicsKnowledgeGraph(), sparqlService);
        Provider a3 = new Provider("DBLP", constantService.getDBLPGraph(), sparqlService);
        List<Provider> Providers = new ArrayList();
        Providers.add(a0);
        Providers.add(a1);
        Providers.add(a2);
        Providers.add(a3);
        return Providers;
    }
    
    @Override
    public void Proccess() {
        try {
            InitAuthorsProvider();
            List<Provider> Providers = getProviders();
            ProcessAuthors(Providers);
            int iasa = count(constantService.getAuthorsSameAsGraph());
            do {
                delete(constantService.getPublicationsSameAsGraph());
                ProcessPublications(Providers);
                ProcessCoauthors(Providers, true);
                int asa = count(constantService.getAuthorsSameAsGraph());
                if (asa != iasa) {
                    iasa = asa;
                } else {
                    break;
                }
            } while (true);
            ProcessCoauthors(Providers, false);
            
            removeDuplicates(constantService.getAuthorsSameAsGraph());
            removeDuplicates(constantService.getPublicationsSameAsGraph());
            removeDuplicates(constantService.getCoauthorsSameAsGraph());
        } catch (Exception ex) {
            log.error("Unknown error while disambiguating");
            ex.printStackTrace();
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
        sparqlService.update(QueryLanguage.SPARQL, q);
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
                + "	}\n"
                + "}";
        sparqlService.update(QueryLanguage.SPARQL, delete);
        sparqlService.update(QueryLanguage.SPARQL, copy);
        sparqlService.update(QueryLanguage.SPARQL, givName);
        sparqlService.update(QueryLanguage.SPARQL, famName);
        sparqlService.update(QueryLanguage.SPARQL, org);
    }
    
    public void ProcessAuthors(List<Provider> AuthorsProviderslist) throws MarmottaException, RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException, InvalidArgumentException, UpdateExecutionException, InterruptedException {
        ExecutorService executorServicex = Executors.newFixedThreadPool(MAXTHREADS);
        BoundedExecutor bexecutorService = new BoundedExecutor(executorServicex, MAXTHREADS);
        Provider MainAuthorsProvider = AuthorsProviderslist.get(0);
        List<Person> allAuthors = MainAuthorsProvider.getAuthors();
        for (int i = 0; i < allAuthors.size(); i++) {
            final int ix = i;
            final int allx = allAuthors.size();
            Person aSeedAuthor = allAuthors.get(i);
            final List<Map.Entry<Provider, List<Person>>> Candidates = new ArrayList<>();
            //List<Map.Entry<Provider, List<Person>>> CandidatesI = new ArrayList<>();
            Candidates.add(new AbstractMap.SimpleEntry<Provider, List<Person>>(MainAuthorsProvider, Lists.newArrayList(aSeedAuthor)));
            for (int j = 1; j < AuthorsProviderslist.size(); j++) {
                Provider aSecondaryProvider = AuthorsProviderslist.get(j);
                List<Person> aProviderCandidates = aSecondaryProvider.getCandidates(aSeedAuthor.URI);
                //List<Person> aProviderCandidatesI = aSecondaryProvider.getCandidates(aSeedAuthor.URI);
                if (!aProviderCandidates.isEmpty()) {
                    Candidates.add(new AbstractMap.SimpleEntry<>(aSecondaryProvider, aProviderCandidates));
                    //CandidatesI.add(new AbstractMap.SimpleEntry<>(aSecondaryProvider, aProviderCandidatesI));
                }
            }
            //Candidates.addAll(Lists.reverse(CandidatesI));
            bexecutorService.submitTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        log.info("Start disambiguating {} out of {} authors", ix, allx);
                        for (Map.Entry<Provider, List<Person>> aCandidateList : Candidates) {
                            aCandidateList.getKey().FillData(aCandidateList.getValue());
                        }
                        Disambiguate(Candidates, 0, new Person());
                        log.info("Finish disambiguating {} out of {} authors", ix, allx);
                    } catch (Exception ex) {
                        log.error("Unknown error while disambiguating");
                        ex.printStackTrace();
                    }
                }
            });
        }
        executorServicex.shutdown();
        executorServicex.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }
    
    public void Disambiguate(List<Map.Entry<Provider, List<Person>>> Candidates, int level, Person superAuthor) throws MarmottaException, RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException, InvalidArgumentException, UpdateExecutionException {
        if (level >= Candidates.size()) {
            return;
        }
        List<Person> CandidateListLevel = Candidates.get(level).getValue();
        for (Person aCandidate : CandidateListLevel) {
            if (superAuthor.check(aCandidate)) {
                Person enrich = superAuthor.enrich(aCandidate);
                registerSameAs(constantService.getAuthorsSameAsGraph(), superAuthor.URI, aCandidate.URI);
                Disambiguate(Candidates, level + 1, enrich);
            } else {
                Disambiguate(Candidates, level + 1, superAuthor);
            }
        }
    }
    
    public void ProcessCoauthors(List<Provider> ProvidersList, boolean onlySameAs) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        String qryDisambiguatedCoauthors = " select distinct ?p { graph <" + constantService.getPublicationsSameAsGraph() + "> { ?p <http://www.w3.org/2002/07/owl#sameAs> ?o } }";
        List<Map<String, Value>> queryResponse = sparqlService.query(QueryLanguage.SPARQL, qryDisambiguatedCoauthors);
        int i = 0;
        for (Map<String, Value> anAuthor : queryResponse) {
            String publicationURI = anAuthor.get("p").stringValue();
            groupCoauthors(ProvidersList, publicationURI, onlySameAs);
            log.info("Disambiguating coauthors {} out of {} publications", i, queryResponse.size());
            i++;
        }
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
        return n;
    }
    
    public void groupCoauthors(List<Provider> ProvidersList, String publicationURI, boolean onlySameAs) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        String providersGraphs = "  ";
        for (Provider aProvider : ProvidersList) {
            providersGraphs += " <" + aProvider.Graph + "> ";
        }
        String qryAllAuthors = "select distinct ?a ?n ?fn ?ln {\n"
                + "		graph <" + constantService.getPublicationsSameAsGraph() + "> {\n"
                + "			values ?pu { <" + publicationURI + "> } .\n"
                + "			?pu <http://www.w3.org/2002/07/owl#sameAs> ?p .\n"
                + "		}\n"
                + "		values ?g { " + providersGraphs + " } graph ?g {\n"
                + "			?a <http://xmlns.com/foaf/0.1/publications> ?p .\n"
                + "			optional { ?a <http://xmlns.com/foaf/0.1/name> ?n}\n"
                + "			optional { ?a <http://xmlns.com/foaf/0.1/givenName> ?fn}\n"
                + "			optional { ?a <http://xmlns.com/foaf/0.1/familyName> ?ln}\n"
                + "		}\n"
                + "}";
        List<Map<String, Value>> queryResponse = sparqlService.query(QueryLanguage.SPARQL, qryAllAuthors);
        Set<Set<String>> coauthorsGroups = new HashSet<>();
        for (int i = 0; i < queryResponse.size(); i++) {
            for (int j = i + 1; j < queryResponse.size(); j++) {
                
                Person auxPersoni = getAuxPerson(queryResponse.get(i));
                Person auxPersonj = getAuxPerson(queryResponse.get(j));
                
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
            Person auxPersoni = getAuxPerson(queryResponse.get(i));
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
    
    public void ProcessPublications(List<Provider> ProvidersList) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        String qryDisambiguatedAuthors = " select distinct ?p { graph <" + constantService.getAuthorsSameAsGraph() + "> { ?p <http://www.w3.org/2002/07/owl#sameAs> ?o } }";
        List<Map<String, Value>> queryResponse = sparqlService.query(QueryLanguage.SPARQL, qryDisambiguatedAuthors);
        int i = 0;
        for (Map<String, Value> anAuthor : queryResponse) {
            String authorURI = anAuthor.get("p").stringValue();
            groupPublications(ProvidersList, authorURI);
            log.info("Disambiguating publications {} out of {} authors", i, queryResponse.size());
            i++;
        }
        
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
            mergeRawData(Providers);
            replaceSameAs(constantService.getAuthorsSameAsGraph());
            replaceSameAs(constantService.getPublicationsSameAsGraph());
            replaceSameAs(constantService.getCoauthorsSameAsGraph());
            mergeSameAs(constantService.getAuthorsSameAsGraph());
            mergeSameAs(constantService.getPublicationsSameAsGraph());
            mergeSameAs(constantService.getCoauthorsSameAsGraph());
        } catch (Exception ex) {
            log.error("Unknown error while merging Central Graph");
            ex.printStackTrace();
        }
    }
    
    public void mergeSameAs(String CGP) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {
        String q = "insert { graph <" + constantService.getCentralGraph() + "> { ?a ?b ?c } } where { graph <" + CGP + "> { ?a ?b ?c }}";
        sparqlService.update(QueryLanguage.SPARQL, q);
    }
    
    public void replaceSameAs(String CGP) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {
        String qry1 = "delete {\n"
                + "	graph <" + constantService.getCentralGraph() + "> {\n"
                + "		?c ?p ?v .\n"
                + "	}\n"
                + "}\n"
                + "insert {\n"
                + "	graph <" + constantService.getCentralGraph() + "> {\n"
                + "		?a ?p ?v .\n"
                + "	}\n"
                + "}\n"
                + "where {\n"
                + "	graph <" + CGP + "> {\n"
                + "		?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "	}\n"
                + "	graph <" + constantService.getCentralGraph() + "> {\n"
                + "		?c ?p ?v .\n"
                + "	}\n"
                + "}";
        String qry2 = "delete {\n"
                + "	graph <" + constantService.getCentralGraph() + "> {\n"
                + "		?v ?p ?c .\n"
                + "	}\n"
                + "}\n"
                + "insert {\n"
                + "	graph <" + constantService.getCentralGraph() + "> {\n"
                + "		?v ?p ?a .\n"
                + "	}\n"
                + "}\n"
                + "where {\n"
                + "	graph <" + CGP + "> {\n"
                + "		?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
                + "	}\n"
                + "	graph <" + constantService.getCentralGraph() + "> {\n"
                + "		?v ?p ?c .\n"
                + "	}\n"
                + "}";
        sparqlService.update(QueryLanguage.SPARQL, qry1);
        sparqlService.update(QueryLanguage.SPARQL, qry2);
    }
    
    public void mergeRawData(List<Provider> ProvidersList) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {
        String providersGraphs = "  ";
        for (Provider aProvider : ProvidersList) {
            providersGraphs += " <" + aProvider.Graph + "> ";
        }
        String qry1 = "insert {\n"
                + "    graph <" + constantService.getCentralGraph() + "> {\n"
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
                + "    graph <" + constantService.getCentralGraph() + "> {\n"
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
                + "    graph <" + constantService.getCentralGraph() + "> {\n"
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
    }
    
}
