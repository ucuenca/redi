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

import com.google.gson.JsonArray;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.impl.DistanceServiceImpl;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.DistanceService;
import org.apache.marmotta.ucuenca.wk.commons.service.GetAuthorsGraphData;
import org.apache.marmotta.ucuenca.wk.commons.service.KeywordsService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.ScopusProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.api.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.pubman.exceptions.PubException;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.semarglproject.vocab.OWL;
import org.slf4j.Logger;

/**
 * Default Implementation of {@link PubVocabService}
 *
 * @author Freddy Sumba
 * @author Jose Luis Cullcay
 */
@ApplicationScoped
public class ScopusProviderServiceImpl implements ScopusProviderService, Runnable {

    @Inject
    private Logger log;

    @Inject
    private QueriesService queriesService;

    @Inject
    private ConstantService constantService;

    @Inject
    private KeywordsService kservice;

    @Inject
    private CommonsServices commonsServices;

    @Inject
    private GetAuthorsGraphData getauthorsData;

    @Inject
    private DistanceService distance;

    @Inject
    private SparqlFunctionsService sparqlFunctionsService;

    @Inject
    private SparqlService sparqlService;

    private int processpercent = 0;

    private String URLSEARCHSCOPUS = "http://api.elsevier.com/content/search/author?query=authfirst%28FIRSTNAME%29authlast%28LASTNAME%29affil%28PAIS%29&apiKey=a3b64e9d82a8f7b14967b9b9ce8d513d&httpAccept=application/xml";
    private String AFFILIATIONPARAM = "affil%28PAIS%29";
    private List<Map<String, Value>> uniNames; //nombre de universidades

    private static int upperLimitKey = 9; //Check 10 keywords
    private static int lowerLimitKey = upperLimitKey - 4; //Not less than 5 keywords
    private static double tolerance = 0.99; //Tolerance of the distance (if the distance is bigger, the author is not included in REDI)

    private boolean update = false;

    @Override
    public String runPublicationsTaskImpl(String param) {
        return null;
    }

    @Override
    public String runPublicationsProviderTaskImpl(boolean update, String[] organizations) {
        try {
            DistanceServiceImpl distancia = new DistanceServiceImpl();
            uniNames = new ArrayList<>();
            ClientConfiguration conf = new ClientConfiguration();
            LDClient ldClient = new LDClient(conf);
            int membersSearchResult = 0;
            String nameToFind = "";
            String authorResource = "";
            int priorityToFind = 0;

            //Get names of universities from endpoints in Spanish and English
            String getEndpointsQuery = queriesService.getlistEndpointNamesQuery();
            uniNames = sparqlService.query(QueryLanguage.SPARQL, getEndpointsQuery);

            List<Map<String, Value>> resultAllAuthors = getauthorsData.getListOfAuthors(organizations);

            /*To Obtain Processed Percent*/
            int allPersons = resultAllAuthors.size();
            int processedPersons = 0;

            RepositoryConnection conUri = null;
            ClientResponse response = null;

            Properties propiedades = new Properties();
            InputStream entrada = null;
            Map<String, String> mapping = new HashMap<String, String>();
            try {
                ClassLoader classLoader = getClass().getClassLoader();
                entrada = classLoader.getResourceAsStream("updatePlatformProcessConfig.properties");
                propiedades.load(entrada);
                for (String source : propiedades.stringPropertyNames()) {
                    String target = propiedades.getProperty(source);
                    mapping.put(source, target);

                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (entrada != null) {
                    try {
                        entrada.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            boolean proccesAllAuthors = Boolean.parseBoolean(mapping.get("proccesAllAuthors").toString());
            boolean semanticAnalizer = Boolean.parseBoolean(mapping.get("semanticAnalizer").toString());

            for (Map<String, Value> map : resultAllAuthors) {
                processedPersons++;
                log.info("Autores procesados con Scopus: " + processedPersons + " de " + allPersons);
                authorResource = map.get("subject").stringValue();
                String firstName = map.get("fname").stringValue().trim().toLowerCase();
                String lastName = map.get("lname").stringValue().trim().toLowerCase();
                String authorID = map.get("subject").stringValue().trim();

                /*if (!firstName.contains("Mauricio") || !lastName.contains("Espinoza")) {
                    continue;
                }*/
 /*if (lastName.split(" ").length <= 2) {
                    //log.error(cleanNameAuthor(lastName));
                //} else {
                    continue;
                }*/
                boolean ask = false;
                if (!proccesAllAuthors && !update) {
                    String askTripletQuery = queriesService.getAskProcessAlreadyAuthorProvider(constantService.getScopusGraph(), authorResource);
                    try {

                        ask = sparqlService.ask(QueryLanguage.SPARQL, askTripletQuery);
                        if (!update) {
                            if (ask || testAuthors(authorResource)) {
                                continue;
                            }
                        }

                    } catch (Exception ex) {
                        log.error("Marmotta Exception:  " + askTripletQuery);
                    }
                }
                priorityToFind = 1;
                try {
                    List<String> uri_search = new ArrayList<>();
                    membersSearchResult = 0;
                    String authorNativeResource = null;
                    String firstNameSearch = cleanNameAuthor(firstName);

                    //firstNameSearch = firstName.split(" ").length > 1 ? firstName.split(" ")[0] : firstName;
                    //String secondNameSearch = firstName.split(" ").length > 1 ? firstName.split(" ")[1] : "";
                    int numApellidos = cleanNameAuthor(lastName).split(" ").length;
                    String lastNameSearch = numApellidos > 1 && numApellidos < 3 ? cleanNameAuthor(lastName).split(" ")[0] : lastName.replace(" ", "+OR+");
                    //String lastNameSearch2 = lastName.split(" ").length > 1 ? lastName.split(" ")[1] : "";
                    //String surnamesSearch = lastNameSearch.split(" ").length > 1 ? firstNameSearch + "+OR+" + secondNameSearch: firstName;

                    uri_search.add(URLSEARCHSCOPUS.replace("FIRSTNAME", firstNameSearch.replace(" ", "+OR+")).replace("LASTNAME", lastName.replace(" ", "+AND+")).replace("PAIS", "Ecuador"));// .replace(AFFILIATIONPARAM, "")
                    String URLSearch = URLSEARCHSCOPUS.replace("FIRSTNAME", firstNameSearch.replace(" ", "+OR+")).replace("LASTNAME", lastNameSearch).replace("PAIS", "Ecuador");
                    if (!uri_search.contains(URLSearch)) {
                        uri_search.add(URLSearch);
                    }
                    //uri_search.add(URLSEARCHSCOPUS.replace("FIRSTNAME", firstNameSearch.replace(" ", "+OR+")).replace("LASTNAME", lastNameSearch).replace(AFFILIATIONPARAM, ""));//.replace("PAIS", "Ecuador"));
                    String scopusfirstName = "";
                    String scopuslastName = "";
                    String scopusAuthorUri = "";
                    String providerGraph = "";
                    List<String> scopusAffiliation = new ArrayList();
                    Boolean testAffiliation = true;

                    Boolean authorFound = false;

                    for (String uri_searchIterator : uri_search) {

                        if (authorFound) {
                            break;
                        }

                        try {
                            boolean existNativeAuthor = false;
                            nameToFind = uri_searchIterator + "&fullName=" + firstName.replace(" ", "%20") + "%20%20" + lastName.replace(" ", "%20");
                            // nameToFind += "&authorURI=" + authorID.replace(" ", "%20");
//                            nameToFind = URLSEARCHSCOPUS.replace("FIRSTNAME", "Mauricio").replace("LASTNAME", "Espinoza").replace("PAIS", "all");
                            membersSearchResult = 0;

                            if (!proccesAllAuthors) {
                                existNativeAuthor = sparqlService.ask(QueryLanguage.SPARQL, queriesService.getAskResourceQuery(constantService.getScopusGraph(), nameToFind.replace(" ", "")));
                            }
                            if ((nameToFind.compareTo("") != 0) && !existNativeAuthor) {
                                response = ldClient.retrieveResource(nameToFind);
                                //response.getData().toString();
                                /**
                                 * Se inserta la tripleta que muestra el intento
                                 * de búsqueda (Esta tripleta NO ofrece sentido
                                 * semantico). Aqui porque el intento debe ser
                                 * plasmado cuando el proveedor no de error al
                                 * buscar el recurso.
                                 */
                                String nameEndpointofPublications = ldClient.getEndpoint(URLSEARCHSCOPUS + nameToFind).getName();
                                providerGraph = constantService.getProviderNsGraph() + "/" + nameEndpointofPublications.replace(" ", "");
                                String InsertQueryOneOf = buildInsertQuery(providerGraph, nameToFind.replace(" ", ""), OWL.ONE_OF, authorResource);
                                updatePub(InsertQueryOneOf);
                            } else {
                                continue;
                            }
                            String getMembersQuery = queriesService.getObjectByPropertyQuery("foaf:member");
                            conUri = ModelCommons.asRepository(response.getData()).getConnection();
                            conUri.begin();
                            TupleQueryResult membersResult = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getMembersQuery).evaluate();

                            while (membersResult.hasNext()) {
                                BindingSet bindingname = membersResult.next();
                                scopusAuthorUri = bindingname.getValue("object").stringValue();
                                membersSearchResult++;
                                //}
                                //if (membersSearchResult == 1) {
                                /**
                                 * Getting contributor name to compare using
                                 * comparisonNames.syntacticComparison function
                                 * - move this query to Queries Service
                                 */
                                String getScopusAuthorName = "SELECT ?firstName ?lastName "
                                        + " WHERE { "
                                        + " <" + scopusAuthorUri + ">  <http://www.elsevier.com/xml/svapi/rdf/dtd/givenName> ?firstName. "
                                        + " <" + scopusAuthorUri + ">  <http://www.elsevier.com/xml/svapi/rdf/dtd/surname> ?lastName. "
                                        + " }";
                                TupleQueryResult nameResult = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getScopusAuthorName).evaluate();
                                boolean equalNames = true;

                                // Save ORCID
                                String getOrcidQuery = queriesService.getObjectByPropertyQuery("<http://www.elsevier.com/xml/svapi/rdf/dtd/orcid>");
                                TupleQueryResult orcid = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getOrcidQuery).evaluate();
                                if (orcid.hasNext()) {
                                    String o = orcid.next().getBinding("object").getValue().stringValue();
                                    String insertOrcid = buildInsertQuery(providerGraph, scopusAuthorUri, "http://www.elsevier.com/xml/svapi/rdf/dtd/orcid", o);
                                    updatePub(insertOrcid);
                                }

                                while (nameResult.hasNext()) {
                                    try {
                                        BindingSet binding = nameResult.next();
                                        scopusfirstName = binding.getValue("firstName").stringValue().length() > scopusfirstName.length() ? binding.getValue("firstName").stringValue() : scopusfirstName;
                                        scopuslastName = binding.getValue("lastName").stringValue().length() > scopuslastName.length() ? binding.getValue("lastName").stringValue() : scopuslastName;
                                    } catch (Exception e) {

                                    }

                                }

                                //if (!distancia.getEqualNamesWithoutInjects(scopusfirstName, scopuslastName, firstName, lastName)) {
                                //    continue;
                                //}
                                //(Jose Luis) Test the affiliation of the researcher
                                String getPublicationsAndTitleFromProviderQuery = queriesService.getSubjectAndObjectByPropertyQuery("dc:title");
                                TupleQuery abstracttitlequery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getPublicationsAndTitleFromProviderQuery); //
                                TupleQueryResult abstractResult = abstracttitlequery.evaluate();

                                while (abstractResult.hasNext()) {
                                    BindingSet abstractResource = abstractResult.next();
                                    // String abstracttext = abstractResource.getValue("abstract").toString();
                                    String publication = abstractResource.getValue("subject").stringValue();

                                    String titletext = abstractResource.getValue("object").stringValue();
                                    //listB = kservice.getKeywords(titletext);
                                    int cero = 0;
                                    if (semanticAnalizer) {//&& listB.size() != cero && listA.size() != cero && distance.semanticComparison(listA, listB)) {

                                        String getPublicationsFromProviderQuery = queriesService.getPublicationsPropertiesQuery(publication);
                                        TupleQuery pubquery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getPublicationsFromProviderQuery); //
                                        TupleQueryResult tripletasResult = pubquery.evaluate();

                                        while (tripletasResult.hasNext()) {
                                            try {
                                                BindingSet tripletsResource = tripletasResult.next();
                                                //authorNativeResource = tripletsResource.getValue("authorResource").toString();
                                                String publicationProperty = tripletsResource.getValue("property").stringValue();
                                                String publicationObject = tripletsResource.getValue("value").stringValue();
                                                ///insert sparql query, 
                                                String publicationInsertQuery = buildInsertQuery(providerGraph, publication, publicationProperty, publicationObject);
                                                updatePub(publicationInsertQuery);

                                                // insert dct:contributor      <> dct:contributor <http://dblp.org/pers/xr/s/Saquicela:Victor> 
                                                String contributorInsertQuery = buildInsertQuery(providerGraph, publication, "http://purl.org/dc/terms/contributor", scopusAuthorUri);
                                                updatePub(contributorInsertQuery);

                                                // sameAs triplet    <http://190.15.141.102:8080/dspace/contribuidor/autor/SaquicelaGalarza_VictorHugo> owl:sameAs <http://dblp.org/pers/xr/s/Saquicela:Victor> 
                                                String sameAsInsertQuery = buildInsertQuery(providerGraph, authorResource, "http://www.w3.org/2002/07/owl#sameAs", scopusAuthorUri);
                                                updatePub(sameAsInsertQuery);

                                                //if value is an uri then search and insert values of this value
                                                if (commonsServices.isURI(publicationObject)) {

                                                    String getResourcesQuery = queriesService.getPublicationsPropertiesQuery(publicationObject);
                                                    TupleQuery resourcequery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getResourcesQuery); //
                                                    TupleQueryResult resourceResult = resourcequery.evaluate();

                                                    while (resourceResult.hasNext()) {
                                                        BindingSet resource = resourceResult.next();
                                                        //authorNativeResource = tripletsResource.getValue("authorResource").toString();
                                                        String resourceProperty = resource.getValue("property").stringValue();
                                                        String resourceObject = resource.getValue("value").stringValue();
                                                        ///insert sparql query, 
                                                        String resourceInsertQuery = buildInsertQuery(providerGraph, publicationObject, resourceProperty, resourceObject);
                                                        updatePub(resourceInsertQuery);
                                                    }
                                                }
                                            } catch (Exception e) {
                                                log.error("ioexception " + e.toString());
                                            }

                                        }

                                    }//end IF semantic distance
                                    else if (!semanticAnalizer) {
                                        String getPublicationsFromProviderQuery = queriesService.getPublicationsPropertiesQuery(publication);
                                        TupleQuery pubquery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getPublicationsFromProviderQuery); //
                                        TupleQueryResult tripletasResult = pubquery.evaluate();

                                        while (tripletasResult.hasNext()) {
                                            try {
                                                BindingSet tripletsResource = tripletasResult.next();
                                                //authorNativeResource = tripletsResource.getValue("authorResource").toString();
                                                String publicationProperty = tripletsResource.getValue("property").stringValue();
                                                String publicationObject = tripletsResource.getValue("value").stringValue();
                                                ///insert sparql query, 
                                                String publicationInsertQuery = buildInsertQuery(providerGraph, publication, publicationProperty, publicationObject);
                                                updatePub(publicationInsertQuery);

                                                // insert dct:contributor      <> dct:contributor <http://dblp.org/pers/xr/s/Saquicela:Victor> 
                                                String contributorInsertQuery = buildInsertQuery(providerGraph, publication, "http://purl.org/dc/terms/contributor", scopusAuthorUri);
                                                updatePub(contributorInsertQuery);

                                                // sameAs triplet    <http://190.15.141.102:8080/dspace/contribuidor/autor/SaquicelaGalarza_VictorHugo> owl:sameAs <http://dblp.org/pers/xr/s/Saquicela:Victor> 
                                                String sameAsInsertQuery = buildInsertQuery(providerGraph, authorResource, "http://www.w3.org/2002/07/owl#sameAs", scopusAuthorUri);
                                                updatePub(sameAsInsertQuery);

                                                //if value is an uri then search and insert values of this value
                                                if (commonsServices.isURI(publicationObject)) {

                                                    String getResourcesQuery = queriesService.getPublicationsPropertiesQuery(publicationObject);
                                                    TupleQuery resourcequery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getResourcesQuery); //
                                                    TupleQueryResult resourceResult = resourcequery.evaluate();

                                                    while (resourceResult.hasNext()) {
                                                        BindingSet resource = resourceResult.next();
                                                        //authorNativeResource = tripletsResource.getValue("authorResource").toString();
                                                        String resourceProperty = resource.getValue("property").stringValue();
                                                        String resourceObject = resource.getValue("value").stringValue();
                                                        ///insert sparql query, 
                                                        String resourceInsertQuery = buildInsertQuery(providerGraph, publicationObject, resourceProperty, resourceObject);
                                                        updatePub(resourceInsertQuery);
                                                    }
                                                }
                                            } catch (Exception e) {
                                                log.error("ioexception " + e.toString());
                                            }

                                        }
                                    }//end else if NO semantic Analizer

                                }
                                authorFound = true; //author with publications found
                            }
                            if (response.getHttpStatus() == 503) {
                                log.error("Error de getStatus o Error de mas de un author como resultado de " + nameToFind);
                                continue;
                            }
                        } catch (DataRetrievalException e) {
                            log.error("Data Retrieval Exception: " + e);
                        }
                    }

                    String scopusfullname = scopuslastName + ":" + scopusfirstName;
                    String localfullname = lastName + ":" + firstName;

//                    if (localfullname.toUpperCase().contains("PIEDRA")) {
//                        localfullname = localfullname.replace(".", "");
//                    }
                    //if (membersSearchResult == 1 //&& testAffiliation && distance.syntacticComparisonNames("local", localfullname, "scopus", scopusfullname)
                    //) {
                    //List<String> listA = kservice.getKeywordsOfAuthor(authorResource);//dspace
                    //List<String> listB = new ArrayList<String>();//desde la fuente de pub
                    logAuthors(authorResource);

                    conUri.commit();
                    conUri.close();
                    //}

                } catch (QueryEvaluationException | MalformedQueryException | RepositoryException ex) {
                    log.error("Evaluation Exception: " + ex);
                } catch (Exception e) {
                    log.error("ioexception " + e.toString());
                }
                //** end View Data
                printPercentProcess(processedPersons, allPersons, "SCOPUS");
            }
            return "True for publications";
        } catch (Exception ex) {
            log.error("Exception: " + ex);
        }
        return "fail";
    }

    public void logAuthors(String authorResource) {

        try {
            FileWriter fw = new FileWriter(new File("AuthorsExploredScopus.csv"), true);
            fw.write(authorResource);
            fw.write(System.lineSeparator());
            fw.close();
        } catch (IOException ex) {
            System.err.println("Couldn't log this: " + authorResource);
        }

    }

    public boolean testAuthors(String authorResource) {

        try {

            BufferedReader b = new BufferedReader(new FileReader(new File("AuthorsExploredScopus.csv")));

            String readLine = "";

            while ((readLine = b.readLine()) != null) {
                if (readLine.equals(authorResource)) {
                    return true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;

    }

    /**
     * Jose Luis Tests if an affiliation matches with the universities in the
     * enpoints.
     *
     * @param affiliation
     * @return
     */
    private Boolean testAffiliation(String affiliation) throws MarmottaException {
        if (affiliation.contains("Ecuador")) {
            return true;
        }
        //try to match the affiliations with a university name
        for (Map<String, Value> name : uniNames) {
            double dist = distance.cosineSimilarityAndLevenshteinDistance(affiliation, name.get("fullName").stringValue());
            if (dist > 0.9) {
                //cercanos.println("Distance1: " + dist + " Scopus: " + affiliation + " Endpoint: " + name.get("fullName").stringValue() );
                return true;
            }
        }
        return false;
    }

    public String cleanNameAuthor(String value) {
        value = value.toLowerCase().trim();
        value = value.replaceAll("  ", " ");
        value = value.replaceAll(" de ", " ");
        value = value.replaceAll(" del ", " ");
        value = value.replaceAll(" los ", " ");
        value = value.replaceAll(" y ", " ");
        value = value.replaceAll(" las ", " ");
        value = value.replaceAll(" la ", " ");

        value = value.replaceAll("^de ", "");
        value = value.replaceAll("^del ", "");
        value = value.replaceAll("^los ", "");
        value = value.replaceAll("^y ", "");
        value = value.replaceAll("^las ", "");
        value = value.replaceAll("^la ", "");

        value = value.replaceAll(" de$", "");
        value = value.replaceAll(" del$", "");
        value = value.replaceAll(" los$", "");
        value = value.replaceAll(" y$", "");
        value = value.replaceAll(" las$", "");
        value = value.replaceAll(" la$", "");

        return value.trim();
    }

    /**
     * Jose Luis Tests if the author's keywords matches with the universities in
     * the enpoints.
     *
     * @param affiliation
     * @return
     */
    public boolean semanticCheck(String authorResource, String similarAuthorResource) {
        boolean result = false;
        try {
            Repository repository = new SPARQLRepository(constantService.getSPARQLEndpointURL());

            double coefficient = 1.1;

            //List<String> keywordsAuthor = getKeywordsAuthor(authorResource, repository);
            //List<String> keywordsPublication = getKeywordsPublications(similarAuthorResource, repository);
            //coefficient = distance.semanticComparisonValue(keywordsAuthor, keywordsPublication);
            result = coefficient < tolerance;

            repository.shutDown();

        } catch (RepositoryException ex) {
            java.util.logging.Logger.getLogger(ScopusProviderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public List<String> getKeywordsAuthor(String authorResource, Repository repository) {
        List<String> keywordsAuthor = new ArrayList<>();
        try {
            String getQueryKeys = queriesService.getAuthorsKeywordsQuery(authorResource);
            TupleQueryResult keywords = executeQuery(repository, getQueryKeys);

            int cont = 0;
            while (keywords.hasNext() && cont <= upperLimitKey) {
                BindingSet bindingKey = keywords.next();
                String keyword = String.valueOf(bindingKey.getValue("keyword")).replace("\"", "").replace("^^", "").split("<")[0].trim();
                if (!keywordsAuthor.contains(keyword)) {
                    keywordsAuthor.add(keyword);
                    cont++;
                }
            }

            if (keywordsAuthor.size() < lowerLimitKey) {

                //get subjects as keywords 
                getQueryKeys = queriesService.getAuthorSubjectQuery(authorResource);
                keywords = executeQuery(repository, getQueryKeys);

                while (keywords.hasNext() && cont <= upperLimitKey) {
                    BindingSet bindingKey = keywords.next();
                    String keyword = String.valueOf(bindingKey.getValue("keyword")).replace("\"", "").replace("^^", "").split("<")[0].trim();
                    if (!keywordsAuthor.contains(keyword)) {
                        keywordsAuthor.add(keyword);
                        cont++;
                    }
                }
            }

        } catch (QueryEvaluationException | RepositoryException | MalformedQueryException ex) {
            log.error("Cannot find similar authors for duplicate authors DSpace. Error: {}", ex);
        }
        return keywordsAuthor;

    }

    private TupleQueryResult executeQuery(Repository repository, String query) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        if (!repository.isInitialized()) {
            repository.initialize();
        }
        RepositoryConnection conn = repository.getConnection();
        conn.begin();
        TupleQueryResult result = conn.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate();
        conn.close();
        return result;
    }

    @Override
    public JsonArray SearchAuthorTaskImpl(String uri) {
        return null;
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

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    @Override
    public void run() {
        String[] organizations = null;
        runPublicationsProviderTaskImpl(update,organizations);
    }

    public void insertSubResources(RepositoryConnection conUri, TupleQueryResult tripletasResult, String providerGraph) {
        try {
            String getPublicationPropertiesQuery = queriesService.getPublicationPropertiesAsResourcesQuery();
            TupleQuery resourcequery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getPublicationPropertiesQuery); //
            tripletasResult = resourcequery.evaluate();
            while (tripletasResult.hasNext()) {
                BindingSet tripletsResource = tripletasResult.next();
                String publicationResource = tripletsResource.getValue("publicationResource").stringValue();
                String publicationProperties = tripletsResource.getValue("publicationProperties").stringValue();
                String publicationPropertiesValue = tripletsResource.getValue("publicationPropertiesValue").stringValue();
                ///insert sparql query,
                String publicationPropertiesInsertQuery = buildInsertQuery(providerGraph, publicationResource, publicationProperties, publicationPropertiesValue);
                //load values publications to publications resource
                updatePub(publicationPropertiesInsertQuery);

            }
        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
            java.util.logging.Logger.getLogger(ScopusProviderServiceImpl.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    void setOrganizations(String[] organizations) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
