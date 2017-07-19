/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.google.common.base.Preconditions;
import info.aduna.iteration.Iterations;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.commons.vocabulary.FOAF;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.DistanceService;
import org.apache.marmotta.ucuenca.wk.commons.service.GetAuthorsGraphData;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.AcademicsKnowledgeProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.api.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.pubman.exceptions.PubException;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.BIBO;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.semarglproject.vocab.OWL;
import org.slf4j.Logger;

/**
 * Default Implementation of {@link PubVocabService} Get Data From ACADEMICS
 * KNOWLEDGE ACADEMICS PROVIDER
 *
 * Freddy Sumba CEDIA - Universidad de Cuenca
 *
 */
@ApplicationScoped
public class AcademicsKnowledgeProviderServiceImpl implements AcademicsKnowledgeProviderService, Runnable {

    @Inject
    private Logger log;
    @Inject
    private QueriesService queriesService;
    @Inject
    private CommonsServices commonsServices;
    @Inject
    private ConstantService constantService;
    @Inject
    private SparqlFunctionsService sparqlFunctionsService;
    @Inject
    private SparqlService sparqlService;
    @Inject
    private GetAuthorsGraphData getauthorsData;
    @Inject
    private DistanceService distanceService;

    private int processpercent = 0;
    private static final String ACADEMICSK_DETAIL = "https://academic.microsoft.com/#/detail/";

    @Override
    public String runPublicationsProviderTaskImpl() {
        try {
            ClientConfiguration conf = new ClientConfiguration();
            LDClient ldClient = new LDClient(conf);

            String authorResource = "";
            List<Map<String, Value>> resultAllAuthors = getauthorsData.getListOfAuthors();

            // To Obtain Processed Percent
            int allPersons = resultAllAuthors.size();
            int processedPersons = 0;

            RepositoryConnection conUri = null;
            ClientResponse response = null;

            for (Map<String, Value> map : resultAllAuthors) {
                processedPersons++;
                log.info("Autores procesados con Academics Knowledge: " + processedPersons + " de " + allPersons);
                authorResource = map.get("subject").stringValue();
                String firstName = map.get("fname").stringValue();
                String lastName = map.get("lname").stringValue();
                String akresource = constantService.getAcademicsKnowledgeResource() + StringUtils.stripAccents(lastName + " " + firstName).replace(" ", "_");
//                String keysubscriptions = readPropertyFromFile("seachProperties.properties", "apiKey");
                String authorSeachQuery = null;
                Set<String> authorWords = getAuthorAreas(authorResource);
                try {
//                    nameToFind = stripAccents(buildRequestURL(firstName, lastName));
//                    String URL_TO_FIND_AK1 = "https://api.projectoxford.ai/academic/v1.0/evaluate?expr=And(Composite(AA.AuN==%27" + nameToFind + "%27),Composite(AA.AfN==%27" + nameOfSource.replace(" ", "%20") + "%27))&attributes=Id,Ti,Y,D,CC,ECC,AA.AuN,AA.AuId,AA.AfN,AA.AfId,F.FN,F.FId,J.JN,J.JId,C.CN,C.CId,RId,W,E,D&E=DN,D,S,S.Ty,S.U,VFN,VSN,V,I,FP,LP,DOI&subscription-key=" + keysubscriptions + "&count=100&sort=2";
                    Set<String> ies = new HashSet<>();
                    for (Map<String, Value> m : sparqlService.query(QueryLanguage.SPARQL, queriesService.getIESInfobyAuthor(authorResource))) {
                        if (m.containsKey("ies")) {
                            ies.addAll(Arrays.asList(m.get("ies").stringValue().split(",")));
                        }
                    }
                    String urlToFIND = buildRequestURL(firstName, lastName, new ArrayList<>(ies));//"https://api.projectoxford.ai/academic/v1.0/evaluate?expr=Composite(AA.AuN==%27" + nameToFind + "%27)&attributes=Id,Ti,Y,D,CC,ECC,AA.AuN,AA.AuId,AA.AfN,AA.AfId,F.FN,F.FId,J.JN,J.JId,C.CN,C.CId,RId,W,E,D&E=DN,D,S,S.Ty,S.U,VFN,VSN,V,I,FP,LP,DOI&subscription-key=" + keysubscriptions + "&count=100&sort=2";
                    boolean dataretrieve = false;

                    String academicsGraph = constantService.getAcademicsKnowledgeGraph();

                    // Ask if already search query is in triple Store .
                    if (!sparqlService.ask(QueryLanguage.SPARQL, queriesService.getAskResourceQuery(academicsGraph, urlToFIND))) {

                        try {
                            response = ldClient.retrieveResource(urlToFIND);
                            if (response.getHttpStatus() == 200 && !response.getData().isEmpty()) {
                                //load retrieve triples in Sesame repository to make some searchs.
                                conUri = ModelCommons.asRepository(response.getData()).getConnection();
                                conUri.begin();
                                dataretrieve = true;
                            } else if (response.getHttpStatus() == 200 && response.getData().isEmpty()) {
                                log.info("No data retrieve for {} with query {}", authorResource, urlToFIND);
                            } else if (response.getHttpStatus() == 403 || response.getHttpStatus() == 401) {
                                log.error("Check your apikey, key subscription "
                                        + "is not valid or you exceed the number of transactions. "
                                        + "HTTP status {}", response.getHttpStatus());
                                throw new DataRetrievalException("Try later.");
                            } else {
                                throw new DataRetrievalException("Cannot retrieve data. HTTP status" + response.getHttpStatus());
                            }
                        } catch (DataRetrievalException e) {
                            log.error("Cannot extract information to query {} from author {}.", urlToFIND, authorResource, e);
                            dataretrieve = false;
                        } finally {
                            //Save the search query with success result in triple store
                            authorSeachQuery = urlToFIND;
                            //Wait four seconds to do send other query, api restrictions.
                            try {
                                Thread.sleep(4000);
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }

                        }
////<editor-fold defaultstate="collapsed" desc="delete">
// Search author by other fields, like afiliation name, country or repository url
//                        if (!dataretrievee) {
//                            try {
//                                response = ldClient.retrieveResource(URL_TO_FIND_AK2);
//                                if (!response.getData().isEmpty()) {
//                                    //load retrieve triples in Sesame repository to make some searchs.
//                                    conUri = ModelCommons.asRepository(response.getData()).getConnection();
//                                    conUri.begin();
//
//                                    //Search some string in data retrieved to find correct authors.
//                                    String paramSearch = readPropertyFromFile("seachProperties.properties", "paramSearch");
//                                    String getPublicationsFromProviderQuery = queriesService.getTriplesByFilter(paramSearch.split(",")[0], paramSearch.split(",")[1], paramSearch.split(",")[2], paramSearch.split(",")[3]);
//                                    TupleQuery pubquery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getPublicationsFromProviderQuery); //
//                                    TupleQueryResult tripletasResult = pubquery.evaluate();
//                                    dataretrievee = tripletasResult.hasNext();
//
//                                    /// If an author has three values in his name is more problably that the author is correct.
//                                    if (!dataretrievee && nameToFind.split("%20").length > 1) {
//                                        dataretrievee = true;
//                                    }
//                                }
//                            } catch (DataRetrievalException e) {
//
//                                log.error("Data Retrieval emply to find: " + URL_TO_FIND_AK2 + " " + e.getMessage());
//
//                                dataretrievee = false;
//
//                            } finally {
//                                authorSeachQuery = URL_TO_FIND_AK2;
//                                try {
//                                    Thread.sleep(4000);
//                                } catch (InterruptedException ex) {
//                                    Thread.currentThread().interrupt();
//                                }
//                            }
//
//                        }
//</editor-fold>
                        // Save triples if there's data.
                        if (dataretrieve) {
                            boolean existNativeAuthor = sparqlService.ask(QueryLanguage.SPARQL, queriesService.getAskResourceQuery(academicsGraph, authorResource));
                            String sameAsInsertQuery = buildInsertQuery(academicsGraph, authorResource, OWL.SAME_AS, akresource);
                            updatePub(sameAsInsertQuery);
                            sameAsInsertQuery = buildInsertQuery(academicsGraph, akresource, REDI.ACADEMICS_KNOWLEDGE_URl.toString(), authorSeachQuery);
                            updatePub(sameAsInsertQuery);

                            if (!existNativeAuthor) {
                                //SPARQL obtain all publications of author
                                String getPublicationsFromProviderQuery = queriesService.getResourceUriByType("dc:creator");
                                TupleQuery pubquery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getPublicationsFromProviderQuery); //
                                TupleQueryResult tripletasResult = pubquery.evaluate();
                                List<String> ownerOf = new ArrayList<>();
                                while (tripletasResult.hasNext()) {
                                    Set<String> publicationWords = new HashSet<>();
                                    BindingSet tripletsResource = tripletasResult.next();
                                    String authorSourceResource = tripletsResource.getValue("authorResource").toString();
                                    String publicationResource = tripletsResource.getValue("publicationResource").toString();
                                    for (Statement statement : Iterations.asList(conUri.getStatements(ValueFactoryImpl.getInstance().createURI(publicationResource), BIBO.QUOTE, null, false))) {
                                        publicationWords.add(statement.getObject().stringValue());
                                    }
                                    // Ignore semantic distance bc it takes too much to execute.
//                                    if (distanceService.semanticComparisonValue(new ArrayList<>(authorWords), new ArrayList<>(publicationWords)) < 1.1) {
                                    String publicationInsertQuery = buildInsertQuery(academicsGraph, akresource, "http://xmlns.com/foaf/0.1/publications", publicationResource);
                                    ownerOf.add(publicationResource);
                                    updatePub(publicationInsertQuery);
//                                    } else {
//                                        int end = publicationResource.lastIndexOf('/') + 1;
//                                        log.info("Publication with id {} ignored.", publicationResource.substring(end));
//                                        log.warn("Author doesn't have common words. \nAuthor words {}. \nPublication words {}.", authorWords, publicationWords);
//                                    }
                                    //CODE TO SAVE A RELATION BETWEEN AUTOR URI AND CREATOR OF PUBLICATION    
                                    // String sameAsInsertQuery = buildInsertQuery(providerGraph, authorSourceResource, "http://www.w3.org/2002/07/owl#sameAs", authorResource);
                                    //updatePub(sameAsInsertQuery);
                                }
                                // save the publications properties in triple store.
                                String getPublicationPropertiesQuery = queriesService.getPropertiesOfResourceByType("bibo:Document");
                                TupleQuery resourcequery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getPublicationPropertiesQuery); //
                                tripletasResult = resourcequery.evaluate();
                                while (tripletasResult.hasNext()) {
                                    BindingSet tripletsResource = tripletasResult.next();
                                    String publicationResource = tripletsResource.getValue("publicationResource").stringValue();
                                    String publicationProperties = tripletsResource.getValue("publicationProperties").stringValue();
                                    String publicationValue = tripletsResource.getValue("publicationPropertiesValue").stringValue();
                                    if (BIBO.URI.stringValue().equals(publicationProperties)) {
                                        int index = publicationValue.lastIndexOf('/') + 1;
                                        publicationValue = ACADEMICSK_DETAIL + publicationValue.substring(index);
                                    }
                                    ///insert sparql query, 
                                    if (ownerOf.contains(publicationResource)) {
                                        String publicationPropertiesInsertQuery = buildInsertQuery(academicsGraph, publicationResource, publicationProperties, publicationValue);
                                        updatePub(publicationPropertiesInsertQuery);
                                    }
                                }
                                // save authors' name.
                                String getAuthorPropertiesQuery = queriesService.getResourceUriByType("foaf:name");
                                tripletasResult = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getAuthorPropertiesQuery).evaluate();
                                while (tripletasResult.hasNext()) {
                                    BindingSet tripletsResource = tripletasResult.next();
                                    String publicationResource = tripletsResource.getValue("publicationResource").stringValue();
                                    String publicationValue = tripletsResource.getValue("authorResource").stringValue();
                                    String publicationPropertiesInsertQuery = buildInsertQuery(academicsGraph, publicationResource, FOAF.name.stringValue(), publicationValue);
                                    updatePub(publicationPropertiesInsertQuery);
                                }
                            }
                            conUri.commit();
                            conUri.close();
                        }
                    }
                } catch (Exception e) {

                    log.error("ioexception " + e.toString());
                }

                //** end View Data
                printPercentProcess(processedPersons, allPersons, "Academics Knowledge");
            }
            return "True for publications";

        } catch (InvalidArgumentException ex) {
            log.error("Marmotta Exception: " + ex);

        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(AcademicsKnowledgeProviderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "fail";
    }

    private Set<String> getAuthorAreas(String authorResource) throws MarmottaException {
        Set<String> words = new HashSet<>();
        for (Map<String, Value> m : sparqlService.query(QueryLanguage.SPARQL, queriesService.getAuthorsKeywordsQuery(authorResource))) {
            if (m.containsKey("keyword")) {
                words.add(m.get("keyword").stringValue().toLowerCase());
            }
        }
        if (!words.isEmpty()) {
            return words;
        }
        for (Map<String, Value> m : sparqlService.query(QueryLanguage.SPARQL, queriesService.getAuthorSubjectQuery(authorResource))) {
            if (m.containsKey("keyword")) {
                words.add(m.get("keyword").stringValue().toLowerCase());
            }
        }
        return words;
    }

    /**
     * Build url to request information from Microsoft Academics API.
     *
     * @see
     * https://westus.dev.cognitive.microsoft.com/docs/services/56332331778daf02acc0a50b/operations/565d753be597ed16ac3ffc03
     * @see
     * https://docs.microsoft.com/es-es/azure/cognitive-services/academic-knowledge/queryexpressionsyntax
     * @param firstName
     * @param lastName
     * @return
     */
    private String buildRequestURL(String firstName, String lastName, List<String> ies) throws URISyntaxException {
        Preconditions.checkArgument(firstName != null && !"".equals(firstName.trim()));
        Preconditions.checkArgument(lastName != null && !"".equals(lastName.trim()));
        String apiKey = readPropertyFromFile("seachProperties.properties", "apiKey");

        firstName = StringUtils.stripAccents(firstName).trim().toLowerCase();
        lastName = StringUtils.stripAccents(lastName).trim().toLowerCase();

        StringBuilder expression = new StringBuilder("AND(OR(");

        String[] fName = splitName(firstName);
        String[] lName = splitName(lastName);
        for (int i = 0; i <= fName.length; i++) {
            if (i == fName.length) {
                expression.append("Composite(AA.AuN='")
                        .append(firstName).append(' ').append(lName[0])
                        .append("'...)");
            } else {
                expression.append("Composite(AA.AuN='")
                        .append(fName[i]).append(' ').append(lName[0]);
                expression.append("'...)");
            }
            if (i < fName.length) {
                expression.append(',');
            }
        }
        if (!ies.isEmpty()) {
            expression.append("),OR(");
            Iterator<String> it = ies.iterator();
            while (it.hasNext()) {
                expression.append("Composite(AA.AfN='")
                        .append(it.next().toLowerCase())
                        .append("')");
                if (it.hasNext()) {
                    expression.append(',');
                }
            }
            expression.append("))");
        } else {
            expression.append("))");
        }
        URIBuilder builder = new URIBuilder("https://westus.api.cognitive.microsoft.com/academic/v1.0/evaluate");
        builder.setParameter("expr", expression.toString());
        builder.setParameter("attributes", "Id,Ti,Y,D,CC,ECC,AA.AuN,AA.AuId,AA.AfN,AA.AfId,F.FN,F.FId,J.JN,J.JId,C.CN,C.CId,RId,W,E,D");
        builder.setParameter("subscription-key", apiKey);
        builder.setParameter("count", "500");

        return builder.build().toString();
    }

    private String[] splitName(String name) {
        int start = name.indexOf(' ');

        if (start > 0) {
            String first = name.substring(0, start);
            String second = name.substring(start + 1);
            return new String[]{first, second};
        }
        return new String[]{name};

    }

    /*
     *   UPDATE - with SPARQL MODULE, to load triplet in marmotta plataform
     *   
     */
    public String updatePub(String querytoUpdate) {

        try {
            sparqlFunctionsService.updatePub(querytoUpdate);
        } catch (PubException ex) {
            log.error("No se pudo insertar: " + querytoUpdate);
            //         java.util.logging.Logger.getLogger(PubVocabServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Correcto";

    }

    /*
     * 
     * @param contAutoresNuevosEncontrados
     * @param allPersons
     * @param endpointName 
     */
    public void printPercentProcess(int processedPersons, int allPersons, String provider) {

        if ((processedPersons * 100 / allPersons) != processpercent) {
            processpercent = processedPersons * 100 / allPersons;
            log.info("Procesado el: " + processpercent + " % de " + provider);
        }
    }

    //construyendo sparql query insert 
    public String buildInsertQuery(String grapfhProv, String sujeto, String predicado, String objeto) {
        if (commonsServices.isURI(objeto)) {
            return queriesService.getInsertDataUriQuery(grapfhProv, sujeto, predicado, objeto);
        } else {
            return queriesService.getInsertDataLiteralQuery(grapfhProv, sujeto, predicado, objeto);
        }
    }

    @Override
    public void run() {
        runPublicationsProviderTaskImpl();
        // runPublicationsProviderTaskImpl("uri");
    }

    /**
     * @See Method to remove accents of a string
     * @param str
     * @return
     */
    public static String stripAccents(String str) {
        String ORIGINAL
                = "ÁáÉéÍíÓóÚúÑñÜü";
        String REPLACEMENT
                = "AaEeIiOoUuNnUu";
        if (str == null) {
            return null;
        }
        char[] array = str.toCharArray();
        for (int index = 0; index < array.length; index++) {
            int pos = ORIGINAL.indexOf(array[index]);
            if (pos > -1) {
                array[index] = REPLACEMENT.charAt(pos);
            }
        }
        return new String(array);
    }

    /**
     * @See Method to read some configuration properties from file.
     * @param file
     * @param property
     * @return
     */
    public String readPropertyFromFile(String file, String property) {
        Properties propiedades = new Properties();
        InputStream entrada = null;
        ConcurrentHashMap<String, String> mapping = new ConcurrentHashMap<String, String>();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            entrada = classLoader.getResourceAsStream(file);
            propiedades.load(entrada);
            for (String source : propiedades.stringPropertyNames()) {
                String target = propiedades.getProperty(source);
                mapping.put(source, target);
            }
        } catch (IOException ex) {
            log.error("IOException in getReadPropertyFromFile CommonsServiceImpl " + ex);
        } catch (Exception ex) {
            log.error("Exception in getReadPropertyFromFile CommonsServiceImpl " + ex);
        } finally {
            if (entrada != null) {
                try {
                    entrada.close();
                } catch (IOException e) {
                    log.error("IOException un getReadPropertyFromFile line 106" + e);
                }
            }
        }
        return mapping.get(property);
    }

}
