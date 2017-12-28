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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    final int MAXTHREADS = 20;

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
            ProcessPublications(Providers);
        } catch (Exception ex) {
            log.error("Unknown error while disambiguating");
            ex.printStackTrace();
        }
    }

    public void InitAuthorsProvider() throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {
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
        ExecutorService executorService = Executors.newFixedThreadPool(MAXTHREADS);
        Provider MainAuthorsProvider = AuthorsProviderslist.get(0);
        List<Person> allAuthors = MainAuthorsProvider.getAuthors();
        for (int i = 0; i < allAuthors.size(); i++) {
            final int ix=i;
            final int allx=allAuthors.size();
            Person aSeedAuthor = allAuthors.get(i);
            final List<Map.Entry<Provider, List<Person>>> Candidates = new ArrayList<>();
            Candidates.add(new AbstractMap.SimpleEntry<Provider, List<Person>>(MainAuthorsProvider, Lists.newArrayList(aSeedAuthor)));
            for (int j = 1; j < AuthorsProviderslist.size(); j++) {
                Provider aSecondaryProvider = AuthorsProviderslist.get(j);
                List<Person> aProviderCandidates = aSecondaryProvider.getCandidates(aSeedAuthor.URI);
                if (!aProviderCandidates.isEmpty()) {
                    Candidates.add(new AbstractMap.SimpleEntry<>(aSecondaryProvider, aProviderCandidates));
                }
            }
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (Map.Entry<Provider, List<Person>> aCandidateList : Candidates) {
                            aCandidateList.getKey().FillData(aCandidateList.getValue());
                        }
                        Disambiguate(Candidates, 0, new Person());
                        log.info("Disambiguating {} out of {} authors", ix, allx);
                    } catch (Exception ex) {
                        log.error("Unknown error while disambiguating");
                        ex.printStackTrace();
                    }
                }
            });
        }
        executorService.shutdown();
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

    public void ProcessPublications(List<Provider> ProvidersList) throws MarmottaException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        String qryDisambiguatedAuthors = " select distinct ?p { graph <" + constantService.getAuthorsSameAsGraph() + "> { ?p <http://www.w3.org/2002/07/owl#sameAs> ?o } }";
        List<Map<String, Value>> queryResponse = sparqlService.query(QueryLanguage.SPARQL, qryDisambiguatedAuthors);
        int i=0;
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
        Set<Set<Integer>> publicationsGroups = new HashSet<>();
        for (int i = 0; i < queryResponse.size(); i++) {
            for (int j = i + 1; j < queryResponse.size(); j++) {
                double titleSimilarity = PublicationUtils.compareTitle(queryResponse.get(i).get("t").stringValue(), queryResponse.get(j).get("t").stringValue());
                if (titleSimilarity >= Person.thresholdTitle) {
                    Set<Integer> aGroup = new HashSet<>();
                    aGroup.add(i);
                    aGroup.add(j);
                    Set<Integer> auxGroup = null;
                    for (Set<Integer> potentialGroup : publicationsGroups) {
                        if (potentialGroup.contains(i) || potentialGroup.contains(j)) {
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
        for (Set<Integer> eachGroup : publicationsGroups) {
            int firstIndex = -1;
            for (Integer groupIndex : eachGroup) {
                if (firstIndex == -1) {
                    firstIndex = groupIndex;
                }
                String URIPublicationA = queryResponse.get(firstIndex).get("p").stringValue();
                String URIPublicationB = queryResponse.get(groupIndex).get("p").stringValue();
                String newURI = constantService.getPublicationResource()+Cache.getMD5(URIPublicationA);
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
        } catch (Exception ex) {
            log.error("Unknown error while merging Central Graph");
            ex.printStackTrace();
        }
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
                + "		?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
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
                + "		?a <http://www.w3.org/2002/07/owl#sameAs> ?c .\n"
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

}
