/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.LatindexDetectionService;
import org.apache.marmotta.ucuenca.wk.pubman.model.Journal;
import org.apache.marmotta.ucuenca.wk.pubman.model.JournalLatindex;
import org.apache.marmotta.ucuenca.wk.pubman.model.Publication;
import org.apache.marmotta.ucuenca.wk.commons.util.BingService;
import org.apache.marmotta.ucuenca.wk.commons.util.ModifiedJaccard;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.BIBO;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.semarglproject.vocab.OWL;
import org.semarglproject.vocab.RDF;
import org.semarglproject.vocab.RDFS;
import org.slf4j.Logger;

/**
 *
 * @author José Ortiz
 */
@ApplicationScoped
public class LantindexDetectionServiceImpl implements LatindexDetectionService {

    @Inject
    private Logger log;

    @Inject
    private QueriesService queriesService;

    @Inject
    private ConstantService constantService;

    @Inject
    private SparqlService sparqlService;
    @Inject
    private CommonsServices commonsServices;

    private Thread DetectionWorker;

    @Override
    public String startProcess() {
        String State = "";
        if (DetectionWorker != null && DetectionWorker.isAlive()) {
            State = "Process running.. check the marmotta main log for further details";
        } else {
            State = "Process starting.. check the marmotta main log for further details";
            DetectionWorker = new Thread() {
                @Override
                public void run() {
                    try {
                        log.info("Starting Latindex detection process ...");
                        KnownJournalProcess();
                        //unKnownJournalProcess();
                    } catch (Exception ex) {
                        log.warn("Unknown error while detecting Latindex Journals, please check the catalina log for further details.");
                        ex.printStackTrace();
                    }
                }

            };
            DetectionWorker.start();
        }
        return State;
    }


    public Map<String, JournalLatindex> getLatindexJournals() throws MarmottaException {
        //Extracting Latindex journals
        List<Map<String, Value>> allLatindexJournals = sparqlService.query(QueryLanguage.SPARQL, queriesService.getJournalsLantindexGraphQuery());
        Map<String, JournalLatindex> allLatindexJournalsObjects = new HashMap<>();
        for (Map<String, Value> aLatindexJournal : allLatindexJournals) {
            String JournalURI = aLatindexJournal.get("JOURNAL").stringValue();
            String JournalName = aLatindexJournal.get("NAME").stringValue().replaceAll("\\(.*?\\)", " ").trim();
            String JournalTopic = aLatindexJournal.get("TOPIC").stringValue();
            String JournalYear = aLatindexJournal.get("YEAR").stringValue();
            String JournalISSN = aLatindexJournal.get("ISSN").stringValue();

            int JournalYearInt = 0;
            try {
                JournalYearInt = Integer.parseInt(JournalYear);
            } catch (Exception ex) {
                log.warn("Invalid year {} in the central graph publication {}.", JournalYear, JournalURI);
            }

            if (allLatindexJournalsObjects.containsKey(JournalURI)) {
                allLatindexJournalsObjects.get(JournalURI).getTopics().add(JournalTopic);
            } else {
                JournalLatindex newLatindexJournal = new JournalLatindex(JournalURI, JournalName, JournalISSN, new ArrayList<String>(), JournalYearInt);
                newLatindexJournal.getTopics().add(JournalTopic);
                allLatindexJournalsObjects.put(JournalURI, newLatindexJournal);
            }
        }
        return allLatindexJournalsObjects;
    }
    
    //Alpha - brute force implementation
    //Optimization needed !!! ... probably a blocking method.
    //
    public void unKnownJournalProcess() throws MarmottaException, IOException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        log.info("Processing publications without journal information ...");
        Map<String, JournalLatindex> allLatindexJournalsObjects = getLatindexJournals();

        Map<String, Publication> allPublicationsObjects = new HashMap<>();

        String publicationsOfJournalCentralGraphQuery = queriesService.getPublicationsCentralGraphQuery();
        List<Map<String, Value>> Publications = sparqlService.query(QueryLanguage.SPARQL, publicationsOfJournalCentralGraphQuery);
        Map<String, Publication> ListPublications = new HashMap<>();
        for (Map<String, Value> aPublicationData : Publications) {
            String URI = aPublicationData.get("PUBLICATION").stringValue();
            String Title = aPublicationData.get("TITLE").stringValue();
            String Abstract = aPublicationData.get("ABSTRACT") != null ? aPublicationData.get("ABSTRACT").stringValue() : "";
            Publication aPublication = null;
            if (ListPublications.containsKey(URI)) {
                aPublication = ListPublications.get(URI);
            } else {
                aPublication = new Publication(URI, new ArrayList<String>(), new ArrayList<String>());
                ListPublications.put(URI, aPublication);
            }
            if (!StringUtils.isBlank(Title)) {
                aPublication.getTitles().add(Title);
            }
            if (!StringUtils.isBlank(Abstract)) {
                aPublication.getAbstracts().add(Abstract);
            }
        }
        allPublicationsObjects = ListPublications;

        int i = 0;
        for (Publication a : allPublicationsObjects.values()) {
            i++;
            ArrayList<Publication> fakePublications = new ArrayList<Publication>();
            fakePublications.add(a);
            Journal fakeJournal = new Journal("Fake", "Fake", fakePublications, null);
            ArrayList<JournalLatindex> allcandidates = new ArrayList<>(allLatindexJournalsObjects.values());
            Validate(fakeJournal, allcandidates, false);
            if (i % 100 == 0) {
                log.info("{} out of {} Publications checked", i, allPublicationsObjects.size());
            }
        }

    }

    public void KnownJournalProcess() throws MarmottaException, IOException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {
        log.info("Processing publications with journal information ...");
        Map<String, JournalLatindex> allLatindexJournalsObjects = getLatindexJournals();
        //Extracting CG journals
        List<Map<String, Value>> allJournalsCentralGraph = sparqlService.query(QueryLanguage.SPARQL, queriesService.getJournalsCentralGraphQuery());

        log.info("Found {} journals within the central graph and {} within the latindex's.", allJournalsCentralGraph.size(), allLatindexJournalsObjects.size());
        int i = 0;
        for (Map<String, Value> aJournalCentralGraph : allJournalsCentralGraph) {
            i++;
            String aJournalURICentralGraph = aJournalCentralGraph.get("JOURNAL").stringValue();
            String aJournalNameCentralGraph = aJournalCentralGraph.get("NAME").stringValue();
            Journal newCGJournal = new Journal(aJournalNameCentralGraph, aJournalURICentralGraph, new ArrayList<Publication>(), null);
            //Blocking
            List<JournalLatindex> blockingJournalNameResult = blockingJournalName(newCGJournal, allLatindexJournalsObjects);
            if (blockingJournalNameResult != null && !blockingJournalNameResult.isEmpty()) {
                Validate(newCGJournal, blockingJournalNameResult, true);
                log.info("{} out of {} Journals checked", i, allJournalsCentralGraph.size());
            }
        }

    }

    public void Validate(Journal aJournal, List<JournalLatindex> Candidates, boolean sameAs) throws MarmottaException, IOException, InvalidArgumentException, MalformedQueryException, UpdateExecutionException {

        /**
         * Validation levels info:
         *
         * Information found together on the web
         *
         * 1 : Latindex ISSN + Publication title + Publication abstract 2 :
         * Latindex ISSN + Publication title 3 : Latindex Journal Name$ +
         * Publication title + Publication abstract 4 : Latindex Journal Name$ +
         * Publication title 5 : Latindex Journal Name + Publication title +
         * Publication abstract 6 : Latindex Journal Name + Publication title
         * 100 : None (default)
         *
         * $ : If the Journal name is large enough. When it has at least 4 words
         * (ignoring words with a length inferior to 4 characters)
         *
         * Note: Only those links with validations levels between 1 and 4 are
         * considered valid and registered with owl:sameAs
         */
        if (sameAs) {
            PopulatePublications(aJournal);
        }
        JournalLatindex mostProbable = null;
        int level = 100;

        for (JournalLatindex aLatindexJournal : Candidates) {
            int validateCandidate = validateCandidate(aJournal, aLatindexJournal);
            if (validateCandidate < level) {
                mostProbable = aLatindexJournal;
                level = validateCandidate;
            }
            if (level == 1) {
                break;
            }
        }
        if (level < 5) {
            if (sameAs) {
                log.info("Link validated Journal {} with {} Journal (Latindex)", aJournal.getURI(), mostProbable.getURI());
                String ins = buildInsertQuery(constantService.getCentralGraph(), aJournal.getURI(), OWL.SAME_AS, mostProbable.getURI());
                sparqlService.update(QueryLanguage.SPARQL, ins);
            } else {
                log.info("Link validated Publication {} with {} Journal (Latindex)", aJournal.getPublications().get(0).getURI(), mostProbable.getURI());
                String ins1 = buildInsertQuery(constantService.getCentralGraph(), aJournal.getPublications().get(0).getURI(), "http://purl.org/dc/terms/isPartOf", mostProbable.getURI());
                String ins2 = buildInsertQuery(constantService.getCentralGraph(), mostProbable.getURI(), OWL.SAME_AS, mostProbable.getURI());
                String ins3 = buildInsertQuery(constantService.getCentralGraph(), mostProbable.getURI(), RDF.TYPE, BIBO.JOURNAL.stringValue());
                String ins4 = buildInsertQuery(constantService.getCentralGraph(), mostProbable.getURI(), RDFS.LABEL, mostProbable.getName());
                sparqlService.update(QueryLanguage.SPARQL, ins1);
                sparqlService.update(QueryLanguage.SPARQL, ins2);
                sparqlService.update(QueryLanguage.SPARQL, ins3);
                sparqlService.update(QueryLanguage.SPARQL, ins4);
            }
        }
    }

    private String buildInsertQuery(String grapfhProv, String sujeto, String predicado, String objeto) {
        if (commonsServices.isURI(objeto)) {
            return queriesService.getInsertDataUriQuery(grapfhProv, sujeto, predicado, objeto);
        } else {
            return queriesService.getInsertDataLiteralQuery(grapfhProv, sujeto, predicado, objeto);
        }
    }

    //Add publications to the journals
    public void PopulatePublications(Journal journal) throws MarmottaException {
        String publicationsOfJournalCentralGraphQuery = queriesService.getPublicationsOfJournalCentralGraphQuery(journal.getURI());
        List<Map<String, Value>> Publications = sparqlService.query(QueryLanguage.SPARQL, publicationsOfJournalCentralGraphQuery);
        Map<String, Publication> ListPublications = new HashMap<>();
        for (Map<String, Value> aPublicationData : Publications) {
            String URI = aPublicationData.get("PUBLICATION").stringValue();
            String Title = aPublicationData.get("TITLE").stringValue();
            String Abstract = aPublicationData.get("ABSTRACT") != null ? aPublicationData.get("ABSTRACT").stringValue() : "";
            Publication aPublication = null;
            if (ListPublications.containsKey(URI)) {
                aPublication = ListPublications.get(URI);
            } else {
                aPublication = new Publication(URI, new ArrayList<String>(), new ArrayList<String>());
                ListPublications.put(URI, aPublication);
            }
            if (!StringUtils.isBlank(Title)) {
                aPublication.getTitles().add(Title);
            }
            if (!StringUtils.isBlank(Abstract)) {
                aPublication.getAbstracts().add(Abstract);
            }
        }
        journal.setPublications(new ArrayList(ListPublications.values()));
    }

    public int validateCandidate(Journal journal, JournalLatindex latindexJournal) throws IOException {
        //Create possible queries
        Map<Integer, String> Queries = new HashMap<>();
        boolean RelevantTitleLength = len(latindexJournal.getName()) >= 4;
        for (Publication aPublication : journal.getPublications()) {
            for (int i = 0; i < 2; i++) {
                String query = "\"" + (i == 0 ? latindexJournal.getISSN() : latindexJournal.getName()) + "\" ";
                for (String titles : aPublication.getTitles()) {
                    String query2 = query + "\"" + getFirstNStrings(titles, 10) + "\" ";
                    int level = i == 0 ? 2 : RelevantTitleLength ? 4 : 6;
                    Queries.put(level, query2);
                    for (String abstracts : aPublication.getAbstracts()) {
                        String query3 = query2 + "\"" + getFirstNStrings(abstracts, 10) + "\"";
                        level = i == 0 ? 1 : RelevantTitleLength ? 3 : 5;
                        Queries.put(level, query3);
                    }
                }
            }
        }

        //Ording queries
        Queries = new TreeMap<Integer, String>(Queries);

        //Run validation
        int currentState = 100; //No validated

        for (Map.Entry<Integer, String> entry : Queries.entrySet()) {
            if (BingTest(entry.getValue())) {
                currentState = entry.getKey();
                break;
            }
        }

        return currentState;
    }

    public boolean BingTest(String query) throws IOException {

        BingService test = new BingService();

        return test.queryBing(query);
    }

    public int len(String text) {

        ModifiedJaccard mod = new ModifiedJaccard();
        List<String> Tokenizer = mod.tokenizer(text.toLowerCase());
        List<String> TokenizerR = new ArrayList<>();
        for (String word : Tokenizer) {
            if (word.length() > 3) {
                TokenizerR.add(word);
            }
        }

        return TokenizerR.size();
    }

    public String getFirstNStrings(String str, int n) {

        ModifiedJaccard mod = new ModifiedJaccard();

        str = mod.specialCharactersClean(str);

        String[] sArr = str.split(" ");
        String firstStrs = "";
        for (int i = 0; i < n && i < sArr.length; i++) {
            firstStrs += sArr[i] + " ";
        }
        return firstStrs.trim();
    }

    //returns a list of candidate latindex journals based on the syntactical similarity of the journal's name
    public List<JournalLatindex> blockingJournalName(Journal aJournal, Map<String, JournalLatindex> allLatindexJournalsObjects) {
        List<JournalLatindex> CandidatesList = new ArrayList<>();
        for (JournalLatindex onePossibleJournal : allLatindexJournalsObjects.values()) {
            if (JournalNameComparison(aJournal.getName(), onePossibleJournal.getName())) {
                CandidatesList.add(onePossibleJournal);
                //log.info("Candidato:" + aJournal.getName() + "_" + onePossibleJournal.getName());
            }
        }
        return CandidatesList;
    }

    public boolean JournalNameComparison(String NameJournal1, String NameJournal2) {
        ModifiedJaccard SynDistance = new ModifiedJaccard();
        double DistanceJournalName = SynDistance.distanceJournalName(NameJournal1, NameJournal2);
        return DistanceJournalName > 0.8;
    }
}
