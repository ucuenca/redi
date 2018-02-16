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
package org.apache.marmotta.ucuenca.wk.pubman.api;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.exceptions.QuotaLimitException;
import org.apache.marmotta.ucuenca.wk.pubman.utils.OntologyMapper;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.semarglproject.vocab.OWL;
import org.semarglproject.vocab.RDF;
import org.semarglproject.vocab.RDFS;
import org.slf4j.Logger;
import scala.actors.threadpool.Arrays;

/**
 * Default Implementation of {@link ProviderService}. Each provider
 * implementation extends this class.
 *
 * @author Xavier Sumba
 */
public abstract class AbstractProviderService implements ProviderService {

    @Inject
    private Logger log;

    @Inject
    private QueriesService queriesService;

    @Inject
    private SparqlFunctionsService sparqlFunctionsService;

    @Inject
    private TaskManagerService taskManagerService;

    @Inject
    private SparqlService sparqlService;

    @Inject
    private SesameService sesameService;

    @Inject
    protected ConstantService constantService;

    private final  String STR = "string";

    /**
     * Build a list of URLs to request authors with {@link LDClient}.
     *
     * @param firstname
     * @param lastname
     * @param organization
     * @return
     */
    protected abstract List<String> buildURLs(String firstname, String lastname, List<String> organization);

    /**
     * Provider's graph to store triples using {@link LDClient}.
     * <p>
     * For example: http://redi.cedia.edu.ec/context/provider/DummyProvider
     *
     * @return
     */
    protected abstract String getProviderGraph();

    /**
     * Name of the provider being used.
     *
     * @return
     */
    protected abstract String getProviderName();

    /**
     * Returns a filter expression to be executed in the ask query. If the
     * filter is empty, the ask evaluates without filter.
     *
     * @return
     */
    protected String filterExpressionSearch() {
        return "";
    }

    /**
     * Do Something if the request wasn't successful.
     */
    protected void retryPlan() {
    }

    /**
     * Extract authors from a particular provider.
     *
     * @param organizations URLs of organizations to extract authors.
     */
    @Override
    public void extractAuthors(String[] organizations) {
        Map<String, String> msgOrg = new HashMap();
        String providerUri = createProvider(getProviderName());
        Task task = taskManagerService.createSubTask(String.format("%s Extraction", getProviderName()), "Publication Extractor");
        task.updateMessage(String.format("Extracting publications from %s Provider", getProviderName()));
        task.updateDetailMessage("Graph", getProviderGraph());
        LDClient ldClient = new LDClient(new ClientConfiguration());
        try {
            for (String organization : organizations) {
                List<Map<String, Value>> resultAllAuthors = sparqlService.query(
                        QueryLanguage.SPARQL, queriesService.getAuthorsDataQuery(organization));
                List<String> organizationNames = getOrganizationNames(sparqlService.query(
                        QueryLanguage.SPARQL, queriesService.getOrganizationNameQuery(organization)));
                int totalAuthors = resultAllAuthors.size();
                if (totalAuthors == 0) {
                    log.info("There are not authors for organization(s) {}", Arrays.toString(organizations));
                }
                int processedAuthors = 0;
                task.updateTotalSteps(totalAuthors);
                task.updateDetailMessage("Organization", organization.substring(organization.lastIndexOf('/') + 1));

                for (Map<String, Value> map : resultAllAuthors) {

                    // Information of local author.
                    String authorResource = map.get("subject").stringValue();
                    String firstName = map.get("fname").stringValue().trim().toLowerCase().replaceAll("\\p{C}", "");
                    String lastName = map.get("lname").stringValue().trim().toLowerCase().replaceAll("\\p{C}", "");

                    task.updateDetailMessage("Author URI", authorResource);

                    for (String reqResource : buildURLs(firstName, lastName, organizationNames)) {
                        // validate if the request has been done
                        String querySearchAuthor;
                        if ("".equals(filterExpressionSearch())) {
                            querySearchAuthor = queriesService.getAskResourceQuery(getProviderGraph(), reqResource.replace(" ", ""));
                        } else {
                            querySearchAuthor = queriesService.getAskObjectQuery(getProviderGraph(), authorResource, filterExpressionSearch());
                        }
                        boolean isResquestDone = sparqlService.ask(QueryLanguage.SPARQL, querySearchAuthor);
                        if (isResquestDone) {
                            // Register only the search query, given that the resource might be from other author.
                            sparqlFunctionsService.executeInsert(getProviderGraph(), reqResource.replace(" ", ""), OWL.ONE_OF, authorResource);
                            continue;
                        }

                        try {
                            ClientResponse response = ldClient.retrieveResource(reqResource);

                            switch (response.getHttpStatus()) {
                                // Manage only HTTP 200 responses, otherwise error. Which error? Stop or continue with next resource.
                                case 200:
                                    break;
                                default:
                                    log.warn("Invalid request/unexpected error for '{}', skipping resource; response with HTTP {} code status.",
                                            reqResource, response.getHttpStatus(), new QuotaLimitException());
                                    retryPlan();
                                    continue;
                            }
                            RepositoryConnection connection = sesameService.getConnection();
                            try {
                                // store triples with new vocabulary
                                Model data = response.getData();
                                // TODO: distribute conversion sesame/jena to improve convertion time.
                                data = OntologyMapper.map(data, getMappingPathFile(), getVocabularyMapper());
                                log.info("After ontology mapper: writing {} triples in context {} for request '{}'.", data.size(), getProviderGraph(), reqResource);
                                Resource providerContext = connection.getValueFactory().createURI(getProviderGraph());
                                connection.add(data, providerContext);
                            } catch (IOException ex) {
                                log.error("cannot store data", ex);
                            } finally {
                                connection.close();
                            }
                            // Register search query.
                            sparqlFunctionsService.executeInsert(getProviderGraph(), reqResource.replace(" ", ""), OWL.ONE_OF, authorResource);
                        } catch (DataRetrievalException dre) {
                            msgOrg.put(organization, "Fail: " + processedAuthors + "/" + totalAuthors);
                            log.error("Cannot retieve RDF for the given resource: '{}'", reqResource, dre);
                            taskManagerService.endTask(task);
                            throw new RuntimeException(dre);
                        }
                    }

                    // Update statistics.
                    processedAuthors++;
                    printprogress(processedAuthors, totalAuthors, getProviderName(), organization);
                    task.updateProgress(processedAuthors);

                    msgOrg.put(organization, processedAuthors + "/" + totalAuthors);
                    if (processedAuthors == totalAuthors) {
                        registerDate(organization, providerUri, "Success: " + processedAuthors + "/" + totalAuthors);

                        msgOrg.put(organization, "Success: " + processedAuthors + "/" + totalAuthors);
                    }
                }
            }

        } catch (MarmottaException me) {
            log.error("Cannot query.", me);
        } catch (RepositoryException re) {
            log.error("Cannot store data retrieved.", re);
        } finally {
            for (String key : msgOrg.keySet()) {
                if (!msgOrg.get(key).contains("Success:")) {
                    registerDate(key, providerUri, msgOrg.get(key));
                }
            }
            taskManagerService.endTask(task);
        }
    }

    /**
     * Returns an {@link InputStream} of the mapping file if exists. The mapping
     * is expressed using
     * <a href="http://wifo5-03.informatik.uni-mannheim.de/bizer/r2r/spec/">R2R
     * Mapping Language</a>. See some
     * <a href="http://wifo5-03.informatik.uni-mannheim.de/bizer/r2r/#quickstart">examples</a>
     * of R2R Mapping Framework.
     *
     * @return
     */
    protected InputStream getMappingPathFile() {
        String name = getProviderName().toLowerCase()
                .replace(' ', '_').trim();
        String resource = String.format("/mapping/%s.ttl", name);
        return this.getClass().getResourceAsStream(resource);
    }

    /**
     * Returns the target vocabulary to map using the {@link InputStream}
     * returned by {@link #getMappingPathFile}. The vocabulary returned is
     * specified in the target properties of the mapping file,
     * <a href="http://wifo5-03.informatik.uni-mannheim.de/bizer/r2r/spec/#targetvocabulary">see
     * the specification</a>.
     *
     * @throws java.io.IOException
     * @see #getMappingPathFile
     * @return
     */
    protected String getVocabularyMapper() throws IOException {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/mapping/redi.r2r");
        String toString = IOUtils.toString(resourceAsStream);
        return toString;
    }

    private List<String> getOrganizationNames(List<Map<String, Value>> names) {
        List<String> n = new ArrayList<>();
        for (Map<String, Value> name : names) {
            n.add(name.get("name").stringValue());
        }
        return n;
    }

    /**
     * Log information about the progress of extraction process.
     *
     * @param actual
     * @param total
     * @param name   of the provider.
     */
    private void printprogress(int actual, int total, String name, String org) {
        int processpercent = actual * 100 / total;
        log.info("{}: processed authors ({}%) {} of {} for organization {}.", name, processpercent, actual, total, org);
    }

    private String createProvider(String providerName) {
        String providerUri = constantService.getProviderBaseUri() + "/" + providerName.toUpperCase().replace(" ", "_");
        String queryProvider = queriesService.getAskResourceQuery(getProviderGraph(), providerUri);
        try {
            boolean result = sparqlService.ask(QueryLanguage.SPARQL, queryProvider);

            if (!result) {
                sparqlFunctionsService.executeInsert(getProviderGraph(), providerUri, RDF.TYPE, REDI.PROVIDER.toString());
                sparqlFunctionsService.executeInsert(getProviderGraph(), providerUri, RDFS.LABEL, providerName, "string");
                // insertStatement(providerUri, RDF.TYPE.toString(),  , STR);
                if ("SCOPUS".equals(providerName)) {
                    sparqlFunctionsService.executeInsert(getProviderGraph(), providerUri, REDI.MAIN.toString(), "True", "boolean");
                } else {
                    sparqlFunctionsService.executeInsert(getProviderGraph(), providerUri, REDI.MAIN.toString(), "False", "boolean");
                }
            }

            return providerUri;
        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(AbstractProviderService.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    private String createExtractEventUri(String providerName, String org) {
        String orgName = org.substring(org.lastIndexOf("/") + 1);

        return constantService.getEndpointBaseEvent() + providerName.replace(' ', '_') + "_" + orgName.replace(' ', '_');

    }

    private void registerDate(String org, String providerUri, String detail) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        String uriEvent = createExtractEventUri(getProviderName(), org);
        sparqlFunctionsService.executeInsert(getProviderGraph(), uriEvent, RDF.TYPE, REDI.EXTRACTION_EVENT.toString());
        sparqlFunctionsService.executeInsert(getProviderGraph(), providerUri, REDI.BELONGTO.toString(), uriEvent);
        sparqlFunctionsService.executeInsert(constantService.getOrganizationsGraph(), org, REDI.BELONGTO.toString(), uriEvent);
        sparqlFunctionsService.executeInsert(getProviderGraph(), uriEvent, REDI.EXTRACTIONDATE.toString(), dateFormat.format(date), STR);
        sparqlFunctionsService.executeInsert(getProviderGraph(), uriEvent, RDFS.LABEL, dateFormat.format(date) + " | " + detail, STR);

    }

}
