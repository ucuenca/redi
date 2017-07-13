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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
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
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.endpoint.gs.GoogleScholarPublicationEndpoint;
import org.apache.marmotta.ucuenca.wk.endpoint.gs.GoogleScholarSearchEndpoint;
import org.apache.marmotta.ucuenca.wk.pubman.api.ProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.api.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.pubman.exceptions.PubException;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;

/**
 * Default Implementation of {@link ProviderService} to get data from Google
 * Scholar. Using {@link Goo
 *
 * Fernando Baculima CEDIA - Universidad de Cuenca
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
@ApplicationScoped
public class GoogleScholarProviderServiceImpl implements ProviderService, Runnable {

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
    private DistanceService distanceService;

    private String authorGraph;
    private String endpointsGraph;
    private String googleGraph;
    private String resourceGoogle;

    private boolean update = false;
    private int processpercent = 0;

    @PostConstruct
    public void init() {
        authorGraph = constantService.getAuthorsGraph();
        endpointsGraph = constantService.getEndpointsGraph();
        googleGraph = constantService.getGoogleScholarGraph();
        resourceGoogle = constantService.getGoogleScholarResource();
    }

    @Override
    public String extractPublications(boolean update) {

        try {
            ClientConfiguration conf = new ClientConfiguration();

            //<editor-fold defaultstate="collapsed" desc="Used to skip certificate problem. Just for test purposes">
            HttpClient httpclient = HttpClientBuilder.create().build();
            conf.setHttpClient(httpclient);
            //</editor-fold>
            LDClient ldClient = new LDClient(conf);

            String nameToFind = "";
            String authorResource = "";
            int priorityToFind = 0;
            List<Map<String, Value>> resultAllAuthors = sparqlService.query(QueryLanguage.SPARQL, queriesService.getAuthorsDataQuery(authorGraph, endpointsGraph));

            /*To Obtain Processed Percent*/
            int allAuthors = resultAllAuthors.size();
            int processedPersons = 0;

            RepositoryConnection conUri = null;
            for (Map<String, Value> map : resultAllAuthors) {
                processedPersons++;
                log.info("Autores procesados con GoogleScholar: " + processedPersons + " de " + allAuthors);
                authorResource = map.get("subject").stringValue();
                String firstName = map.get("fname").stringValue();
                String lastName = map.get("lname").stringValue();
                priorityToFind = 1;
                if (!sparqlService.ask(QueryLanguage.SPARQL, queriesService.getAskResourceQuery(googleGraph, authorResource)) && !isProcessed(authorResource)) {
                    boolean dataretrieve = false;//( Data Retrieve Exception )
                    do {
                        try {
                            boolean existNativeAuthor = false;
                            ClientResponse response = null;
                            nameToFind = commonsServices.removeAccents(priorityFindQueryBuilding(priorityToFind, firstName, lastName).replace("_", "+"));
                            String url_to_find = "https://scholar.google.com/citations?mauthors=" + nameToFind + "&hl=en&view_op=search_authors";

                            existNativeAuthor = sparqlService.ask(QueryLanguage.SPARQL, queriesService.getAskResourceQuery(googleGraph, url_to_find));
                            if (nameToFind.compareTo("") != 0 && !existNativeAuthor) {

                                List<Map<String, Value>> infoQuery = sparqlService.query(QueryLanguage.SPARQL, queriesService.getIESInfobyAuthor(authorResource));
                                Set[] infoIES = new HashSet[]{new HashSet(), new HashSet(), new HashSet(), new HashSet()};
                                for (Map<String, Value> info : infoQuery) {
                                    infoIES[0].add(info.get("city").stringValue());
                                    infoIES[1].add(info.get("province").stringValue());
                                    infoIES[2].addAll(Arrays.asList(info.get("ies").stringValue().split(",")));
                                    infoIES[3].addAll(Arrays.asList(info.get("domains").stringValue().split(",")));
                                }
                                if (!infoIES[0].isEmpty() && !infoIES[1].isEmpty() && !infoIES[2].isEmpty()) {
                                    GoogleScholarSearchEndpoint endpoint = (GoogleScholarSearchEndpoint) ldClient.getEndpoint(url_to_find);

                                    endpoint.setCity((String[]) infoIES[0].toArray(new String[0]));
                                    endpoint.setProvince((String[]) infoIES[1].toArray(new String[0]));
                                    endpoint.setIes((String[]) infoIES[2].toArray(new String[0]));

                                    if (!infoIES[3].isEmpty()) {
                                        endpoint.setDomains((String[]) infoIES[3].toArray(new String[0]));
                                    } else {
                                        endpoint.setDomains(new String[0]);
                                    }

                                    endpoint.setResource(resourceGoogle);
                                    endpoint.setFirstName(firstName);
                                    endpoint.setLastName(lastName);
                                    endpoint.setDistance(distanceService);
                                }
                                //do {
                                try {//waint 1  second between queries
                                    Thread.sleep(1000);
                                } catch (InterruptedException ex) {
                                    Thread.currentThread().interrupt();
                                }
                                try {
                                    boolean retry = false;
                                    do {
                                        response = ldClient.retrieveResource(url_to_find);
                                        if (response.getHttpStatus() >= 500 || response.getHttpStatus() >= 503 || response.getHttpStatus() == 504) {
                                            retry = true;
                                            log.warn("Cannot acces to Scholar. Waiting...");
                                            long retryAfter = 21600000 + response.getExpires().getTime() - new Date(System.currentTimeMillis()).getTime();
                                            if (retryAfter > 0) {
                                                Thread.sleep(retryAfter);
                                            }
                                        } else {
                                            retry = false;
                                        }
                                    } while (retry);

                                    if (response.getData().size() > 0) {
                                        dataretrieve = true;
                                    }
                                } catch (DataRetrievalException e) {
                                    log.error("Error when retrieve: " + url_to_find + " -  Exception: " + e);
                                    dataretrieve = false;
                                }

                            }

                            if (dataretrieve) {
                                Preconditions.checkNotNull(response);
                                conUri = ModelCommons.asRepository(response.getData()).getConnection();
                                conUri.begin();

                                if (!existNativeAuthor) {
                                    String getTriples = queriesService.getRetrieveResourceQuery();
                                    TupleQuery triplesGS = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getTriples);
                                    TupleQueryResult gsResult = triplesGS.evaluate();
                                    while (gsResult.hasNext()) {
                                        BindingSet bind = gsResult.next();
                                        String s = bind.getValue("x").stringValue().trim();
                                        String p = bind.getValue("y").stringValue().trim();
                                        String o = bind.getValue("z").stringValue().trim();

                                        String publicationInsertQuery = buildInsertQuery(googleGraph, s, p, o);
                                        updatePub(publicationInsertQuery);

                                    }

                                    // SameAs URI DSPACE/GS
                                    String authorGoogle = resourceGoogle + "author/" + nameToFind.replace("+", "_");
                                    String sameAs = buildInsertQuery(googleGraph, authorResource, "http://www.w3.org/2002/07/owl#sameAs", authorGoogle);
                                    updatePub(sameAs);
                                    sameAs = buildInsertQuery(googleGraph, authorGoogle, "http://www.w3.org/2002/07/owl#sameAs", authorResource);
                                    updatePub(sameAs);
                                }//end if existNativeAuthor
                                conUri.commit();
                                conUri.close();
                            }//fin   if (dataretrieve)
                        } catch (Exception e) {
                            log.error("ioexception " + e.toString());
                        }
                        priorityToFind++;
                    } while (priorityToFind <= 3 && !dataretrieve);//end do while
                    writeResource(authorResource);
                    printPercentageProgress(processedPersons, allAuthors, "Google Scholar");
                } else if (sparqlService.ask(QueryLanguage.SPARQL, queriesService.getAskPublicationsURLGS(googleGraph, authorResource))) {
                    // ask if there are publications left to extract for authorresource
                    List<Map<String, Value>> publications = sparqlService.query(QueryLanguage.SPARQL, queriesService.getPublicationsURLGS(googleGraph, authorResource));
                    for (Map<String, Value> publication : publications) {
                        String url = publication.get("url").stringValue();
                        String author = publication.get("author").stringValue();
                        try {
                            GoogleScholarPublicationEndpoint pubEndpoint = (GoogleScholarPublicationEndpoint) ldClient.getEndpoint(url);
                            pubEndpoint.setAuthorURI(author);
                            log.info(url);
                            ClientResponse response = ldClient.retrieveResource(url);
                            if (response.getData().size() > 0 && response.getHttpStatus() == 200) {

                                conUri = ModelCommons.asRepository(response.getData()).getConnection();
                                conUri.begin();

                                String getTriples = queriesService.getRetrieveResourceQuery();
                                TupleQueryResult result = conUri.prepareTupleQuery(QueryLanguage.SPARQL, getTriples).evaluate();
                                while (result.hasNext()) {
                                    BindingSet bind = result.next();
                                    String s = bind.getValue("x").stringValue().trim();
                                    String p = bind.getValue("y").stringValue().trim();
                                    String o = bind.getValue("z").stringValue().trim();

                                    String publicationInsertQuery = buildInsertQuery(googleGraph, s, p, o);
                                    updatePub(publicationInsertQuery);

                                }
                                conUri.commit();
                                conUri.close();
                            }

                        } catch (DataRetrievalException | RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
                            log.error("Something went wrong when extracting publication {}. Error: {}", url, ex.getMessage());
                        }
                    }
                }
            }
            if (update) {
                String result = updateAuthors(ldClient);
                log.info(result);
            }
            ldClient.shutdown();
            return "Extracction successful";
        } catch (MarmottaException ex) {
            log.error("Something went wrong. Error: {}", ex.getMessage());
        }

        return "Something is going wrong";
    }

    private String updateAuthors(LDClient ldClient) {
        String queryAuthorScholar = queriesService.getProfileScholarAuthor();
        List<Map<String, Value>> authors;
        int authorsUpdated = 0;
        try {
            authors = sparqlService.query(QueryLanguage.SPARQL, queryAuthorScholar);
        } catch (MarmottaException ex) {
            log.error("Can't query authors from Google Scholar. Error: {}", ex);
            return "Can't query authors from Google Scholar. Check log for more detail.";
        }

        for (Map<String, Value> author : authors) {
            String resource = author.get("resource").stringValue();
            String profile = author.get("profile").stringValue() + "&cstart=0&pagesize=100";
            int oldPublicationsSize = Integer.parseInt(author.get("count").stringValue());
            Model model;
            try {
                model = ldClient.retrieveResource(profile).getData();
            } catch (DataRetrievalException ex) {
                log.error("Can't retrieve profile publications. Error: {}", ex);
                continue;
            }
            int newPublicationsSize = model.size();
            if (newPublicationsSize != oldPublicationsSize) {
                String queryPublications = queriesService.getPublicationsScholar(resource);
                List<String> newPublications = new ArrayList<>();
                try {
                    for (Map<String, Value> map : sparqlService.query(QueryLanguage.SPARQL, queryPublications)) {
                        newPublications.add(map.get("url").toString());
                    }
                    for (Statement statement : model) {
                        if (newPublications.contains(statement.getObject().stringValue())) {
                            newPublications.remove(statement.getObject().stringValue());
                        } else {
                            newPublications.add(statement.getObject().stringValue());
                        }
                    }
                } catch (MarmottaException ex) {
                    log.error("Can't query publications to resource '{}'. Error: {}", resource, ex);
                    continue;
                }
                for (String url : newPublications) {
                    Model publication;
                    try {
                        GoogleScholarPublicationEndpoint pubEndpoint = (GoogleScholarPublicationEndpoint) ldClient.getEndpoint(url);
                        pubEndpoint.setAuthorURI(resource);
                        publication = ldClient.retrieveResource(url).getData();
                    } catch (DataRetrievalException ex) {
                        log.error("Can't retrieve publication information. Error: {}", ex);
                        continue;
                    }
                    try {
                        for (Statement s : publication) {
                            log.debug("{}", s);
                            sparqlService.update(QueryLanguage.SPARQL,
                                    buildInsertQuery(googleGraph, s.getSubject().stringValue(),
                                            s.getPredicate().stringValue(), s.getObject().stringValue()));
                        }
                        sparqlService.update(QueryLanguage.SPARQL, buildInsertQuery(googleGraph, resource, REDI.GSCHOLAR_URl.toString(), url));
                    } catch (InvalidArgumentException | MarmottaException | MalformedQueryException | UpdateExecutionException ex) {
                        log.error("Can't insert triple statement. Error: {}", ex);
                    }
                }
                authorsUpdated++;
            }
        }
        return String.format("%s authors have been updated.", authorsUpdated);
    }

    /**
     * Build a name for an author based on three priorities. 1. Returns first
     * name, middle name and last name. 2. Returns first name and last name. 3.
     * Returns middle name and last name. If there isn't middle name, it returns
     * only the last name.
     *
     * @param priority
     * @param firstName
     * @param lastName
     * @return
     */
    private String priorityFindQueryBuilding(int priority, String firstName, String lastName) {
        String[] fnamelname = {"", "", "", "", "", ""};
        /**
         * fnamelname[0] is a firstName A, fnamelname[1] is a firstName B
         * fnamelname[2] is a lastName A, fnamelname[3] is a lastName B
         */
        System.arraycopy(firstName.split(" "), 0, fnamelname, 0, firstName.split(" ").length);
        System.arraycopy(lastName.split(" "), 0, fnamelname, 2, lastName.split(" ").length);

        switch (priority) {
            case 3:
                return fnamelname[1] + "_" + fnamelname[2];
            case 2:
                return fnamelname[0] + "_" + fnamelname[2];
            case 1:
                return fnamelname[0] + "_" + fnamelname[1] + "_" + fnamelname[2];
        }
        return "";
    }

    /**
     * Execute query.
     *
     * @param query
     * @return
     */
    private boolean updatePub(String query) {
        try {
            sparqlFunctionsService.updatePub(query);
        } catch (PubException ex) {
            log.error("Query cannot be executed. Query\n {}. \nEROR: {}", query, ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Print progress of extraction task.
     *
     * @param processedPersons
     * @param allPersons
     * @param provider
     */
    private void printPercentageProgress(int processedPersons, int allPersons, String provider) {
        if ((processedPersons * 100 / allPersons) != processpercent) {
            processpercent = processedPersons * 100 / allPersons;
            log.info("Procesado el: " + processpercent + " % de " + provider);
        }
    }

    /**
     * Build SPARQL query.
     *
     * @param grapfhProv
     * @param sujeto
     * @param predicado
     * @param objeto
     * @return
     */
    public String buildInsertQuery(String grapfhProv, String sujeto, String predicado, String objeto) {
        if (commonsServices.isURI(objeto)) {
            return queriesService.getInsertDataUriQuery(grapfhProv, sujeto, predicado, objeto);
        } else {
            return queriesService.getInsertDataLiteralQuery(grapfhProv, sujeto, predicado, objeto);
        }
    }

    /**
     * True to execute a task to check if there's new publications.
     *
     * @param update
     */
    public void executeUpdateTask(boolean update) {
        this.update = update;
    }

    @Override
    public void run() {
        extractPublications(update);
    }

    /**
     * Store author URI once its extracted.
     *
     * @param authorResource
     */
    private void writeResource(String authorResource) {
        try {
            File f = new File(constantService.getHome(), "ProcessedGoogleScholarURIs");
            if (!f.exists()) {
                f.createNewFile();
            }
            Files.write(f.toPath(), String.format("%s\n", authorResource).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ex) {
            log.error("Can't save resource {}, Error: {} \n{}", authorResource, ex.getMessage(), ex);
        }
    }

    /**
     * Check if a specific author is already extracted. URIs from extracted
     * authors are stored in a file in marmotta.home.
     *
     * @param authorResource
     * @return
     */
    private boolean isProcessed(String authorResource) {
        try (Scanner scanner = new Scanner(new File(constantService.getHome(), "ProcessedGoogleScholarURIs"));) {
            while (scanner.hasNext()) {
                if (authorResource.equals(scanner.nextLine().trim())) {
                    return true;
                }
            }
        } catch (FileNotFoundException ex) {
            log.error("Can't read URIs. URI trying to read {}. Error msj {}.\n{}", authorResource, ex.getMessage(), ex);
        }
        return false;
    }
}
