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
package edu.ucuenca.storage.services;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import edu.ucuenca.storage.api.MongoService;
import edu.ucuenca.storage.api.PopulateMongo;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.apache.marmotta.ucuenca.wk.commons.util.BoundedExecutor;
import org.apache.marmotta.ucuenca.wk.pubman.api.CommonService;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
@ApplicationScoped
public class PopulateMongoImpl implements PopulateMongo {

    @Inject
    private ConfigurationService conf;
    @Inject
    private QueriesService queriesService;
    @Inject
    private ExternalSPARQLService sparqlService;
    @Inject
    private Logger log;
    @Inject
    private CommonService commonService;
    @Inject
    private CommonsServices commonServices;
    @Inject
    private TaskManagerService taskManagerService;

    private static final Map context = new HashMap();

    private final JsonNodeFactory factory = JsonNodeFactory.instance;

    static {
        context.put("dct", "http://purl.org/dc/terms/");
        context.put("owl", "http://www.w3.org/2002/07/owl#");
        context.put("foaf", "http://xmlns.com/foaf/0.1/");
        context.put("uc", "http://ucuenca.edu.ec/ontology#");
        context.put("bibo", "http://purl.org/ontology/bibo/");
        context.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        context.put("schema", "http://schema.org/");
    }

    /**
     *
     * @param queryResources query to load resources to describe.
     * @param queryDescribe query to describe each candidate; it has to be a
     * describe/construct.
     * @param collection collection name in Mongo db.
     */
    private void loadResources(String queryResources, String queryDescribe, String c) {
        try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));
                StringWriter writter = new StringWriter();) {
            RepositoryConnection conn = sparqlService.getRepositoryConnetion();

            int num_candidates = 0;
            try {
                MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());
                // Delete and create collection
                MongoCollection<Document> collection = db.getCollection(c);
                collection.drop();

                RDFWriter jsonldWritter = Rio.createWriter(RDFFormat.JSONLD, writter);
                TupleQueryResult resources = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryResources).evaluate();
                while (resources.hasNext()) {
                    String resource = resources.next().getValue("subject").stringValue();
                    conn.prepareGraphQuery(QueryLanguage.SPARQL, queryDescribe.replace("{}", resource))
                            .evaluate(jsonldWritter);
                    Object compact = JsonLdProcessor.compact(JsonUtils.fromString(writter.toString()), context, new JsonLdOptions());
                    Map<String, Object> json = (Map<String, Object>) compact;
                    json.put("_id", resource);
                    collection.insertOne(new Document(json));
                    writter.getBuffer().setLength(0);
                    log.info("{} inserting describe for resource {}", ++num_candidates, resource);
                }
                log.info("Load {} resources into {} collection", num_candidates, collection);
            } finally {
                conn.close();
            }
        } catch (RepositoryException ex) {
            log.error("Cannot retrieve Sesame connection", ex);
        } catch (MalformedQueryException ex) {
            log.error("Query cannot be processed", ex);
        } catch (QueryEvaluationException ex) {
            log.error("Execution of query failed", ex);
        } catch (RDFHandlerException ex) {
            log.error("Cannot handle RDFWriter", ex);
        } catch (JsonLdError ex) {
            log.error("Cannot convert string to JSON-LD", ex);
        } catch (IOException ex) {
            log.error("IO error", ex);
        }
    }

    private void loadStadistics(String c, HashMap<String, String> queries) {
        try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));
                StringWriter writter = new StringWriter();) {
            RepositoryConnection conn = sparqlService.getRepositoryConnetion();

            try {
                MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());
                // Delete and create collection
                MongoCollection<Document> collection = db.getCollection(c);
                collection.drop();

                RDFWriter jsonldWritter = Rio.createWriter(RDFFormat.JSONLD, writter);
                for (String key : queries.keySet()) {
                    log.info("Getting {} query", key);

                    conn.prepareGraphQuery(QueryLanguage.SPARQL, queries.get(key))
                            .evaluate(jsonldWritter);
                    Object compact = JsonLdProcessor.compact(JsonUtils.fromString(writter.toString()), context, new JsonLdOptions());
                    Map<String, Object> json = (Map<String, Object>) compact;
                    json.put("_id", key);
                    collection.insertOne(new Document(json));
                    writter.getBuffer().setLength(0);
                    log.info("Load aggregation into {} collection for id '{}'", c, key);
                }
            } finally {
                conn.close();
            }
        } catch (RepositoryException ex) {
            log.error("Cannot retrieve Sesame connection", ex);
        } catch (MalformedQueryException ex) {
            log.error("Query cannot be processed", ex);
        } catch (QueryEvaluationException ex) {
            log.error("Execution of query failed", ex);
        } catch (RDFHandlerException ex) {
            log.error("Cannot handle RDFWriter", ex);
        } catch (JsonLdError ex) {
            log.error("Cannot convert string to JSON-LD", ex);
        } catch (IOException ex) {
            log.error("IO error", ex);
        }
    }

    @Override
    public void authors() {
        final Task task = taskManagerService.createSubTask("Caching authors profiles", "Mongo Service");
        try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
            MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());
            // Delete and create collection
            final MongoCollection<Document> collection = db.getCollection(MongoService.Collection.AUTHORS.getValue());
            collection.drop();
            final List<Map<String, Value>> authorsRedi = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getAuthorsCentralGraph());
            task.updateTotalSteps(authorsRedi.size());
            BoundedExecutor threadPool = BoundedExecutor.getThreadPool(5);
            for (int i = 0; i < authorsRedi.size(); i++) {
                final String author = authorsRedi.get(i).get("a").stringValue();
                final int j = i;
                threadPool.submitTask(new Runnable() {
                    @Override
                    public void run() {
                        // Print progress
                        log.info("Relating {} ", author);
                        log.info("Relating {}/{}. Author: '{}' ", j + 1, authorsRedi.size(), author);
                        task.updateDetailMessage("URI", author);
                        task.updateProgress(j + 1);
                        // Get and store author data (json) from SPARQL repository.
                        String profiledata = commonService.getAuthorDataProfile(author);
                        Document parse = Document.parse(profiledata);
                        parse.append("_id", author);
                        collection.insertOne(parse);
                    }
                });
            }
            threadPool.end();
        } catch (MarmottaException | InterruptedException ex) {
            log.error(ex.getMessage(), ex);
        }
        taskManagerService.endTask(task);
    }

    @Override
    public void statistics() {
        HashMap<String, String> queries = new HashMap<>();

        queries.put("barchar", queriesService.getBarcharDataQuery());

        if (countCountries() > 1) {
            queries.put("count_authors", queriesService.getAggregationPublicationsbyCountry());
            queries.put("count_publications", queriesService.getAggreggationAuthorsbyCountry());
        } else {
            queries.put("count_authors", queriesService.getAggreggationAuthors());
            queries.put("count_publications", queriesService.getAggregationPublications());
        }
        queries.put("count_research_areas", queriesService.getAggregationAreas());
        queries.put("keywords_frequencypub_gt4", queriesService.getKeywordsFrequencyPub());
        loadStadistics(MongoService.Collection.STATISTICS.getValue(), queries);
    }

    private int countCountries() {
        try {
            List<Map<String, Value>> countc = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getCountCountry());
            return Integer.parseInt(countc.get(0).get("ncountry").stringValue());

        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(PopulateMongoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    public void networks() {
        final Task task = taskManagerService.createSubTask("Caching related authors", "Mongo Service");
        try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
            MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());
            // Delete and create collection
            final MongoCollection<Document> collection = db.getCollection(MongoService.Collection.RELATEDAUTHORS.getValue());
            collection.drop();
            BoundedExecutor threadPool = BoundedExecutor.getThreadPool(5);
            task.updateMessage("Calculating related authors");
            final List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getAuthorsCentralGraph());
            int i = 0;
            for (final Map<String, Value> mp : query) {
                final int j = i++;
                threadPool.submitTask(new Runnable() {
                    @Override
                    public void run() {
                        String stringValue = mp.get("a").stringValue();
                        log.info("Relating {} ", stringValue);
                        log.info("Relating {}/{} ", j, query.size());
                        task.updateDetailMessage("URI", stringValue);
                        task.updateDetailMessage("Status", j + "/" + query.size());
                        String collaboratorsData = commonService.getCollaboratorsData(stringValue);
                        Document parse = Document.parse(collaboratorsData);
                        parse.append("_id", stringValue);
                        collection.insertOne(parse);
                    }
                });

            }
            threadPool.end();
        } catch (Exception w) {
            log.debug(w.getMessage(), w);
        }
        taskManagerService.endTask(task);
    }

    @Override
    public void clusters() {
        Task task = taskManagerService.createSubTask("Caching clusters", "Mongo Service");
        clustersTotals();
        try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
            MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());

            // Delete and create collection
            MongoCollection<Document> collection = db.getCollection(MongoService.Collection.CLUSTERS.getValue());
            collection.drop();

            List<Map<String, Value>> clusters = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getClusterURIs());

            task.updateTotalSteps(clusters.size());

            for (int i = 0; i < clusters.size(); i++) {
                String cluster = clusters.get(i).get("c").stringValue();
                // Print progress
                log.info("Relating {}/{}. Cluster: '{}' ", i + 1, clusters.size(), cluster);
                task.updateDetailMessage("URI", cluster);
                task.updateProgress(i + 1);
                // Get and store author data (json) from SPARQL repository.
                String clusterData = commonService.getCluster(cluster);
                Document parse = Document.parse(clusterData);
                parse.append("_id", cluster);
                collection.insertOne(parse);
            }
        } catch (MarmottaException ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            taskManagerService.endTask(task);
        }
    }

    public void clustersTotals() {
        try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
            MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());

            // Delete and create collection
            MongoCollection<Document> collection = db.getCollection(MongoService.Collection.CLUSTERSTOTALS.getValue());
            collection.drop();
            log.info("Counting clusters");
            List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getClusterTotals());
            log.info("Writing totals");
            for (Map<String, Value> a : query) {
                String label = a.get("k").stringValue();
                log.info("Cluster {}", label);
                String uri = a.get("area").stringValue();
                String tot = a.get("totalAuthors").stringValue();
                Document parse = new Document();
                parse.append("_id", uri);
                parse.append("area", uri);
                parse.append("k", label);
                parse.append("totalAuthors", tot);
                List<BasicDBObject> lsdoc = new ArrayList<>();
                List<Map<String, Value>> query1 = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getSubClusterTotals(uri));
                for (Map<String, Value> b : query1) {
                    if (b.get("sc") == null) {
                        continue;
                    }
                    String sc = b.get("sc").stringValue();
                    String k = b.get("k").stringValue();
                    String totalAuthors = b.get("totalAuthors").stringValue();
                    BasicDBObject parseSub = new BasicDBObject();
                    parseSub.put("sc", sc);
                    parseSub.put("k", k);
                    parseSub.put("totalAuthors", totalAuthors);
                    lsdoc.add(parseSub);
                }
                parse.append("subclusters", lsdoc);
                collection.insertOne(parse);
            }

        } catch (MarmottaException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void authorsByArea() {
        final Task task = taskManagerService.createSubTask("Caching Authors by Area", "Mongo Service");
        try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
            MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());

            // Delete and create collection
            final MongoCollection<Document> collection = db.getCollection(MongoService.Collection.AUTHORS_AREA.getValue());
            collection.drop();

            final List<Map<String, Value>> areas = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getClusterAndSubclusterURIs());

            task.updateTotalSteps(areas.size());
            BoundedExecutor threadPool = BoundedExecutor.getThreadPool(5);
            for (int i = 0; i < areas.size(); i++) {
                final int j = i;
                final String cluster = areas.get(i).get("cluster").stringValue();
                final String subcluster = areas.get(i).get("subcluster").stringValue();

                threadPool.submitTask(new Runnable() {
                    @Override
                    public void run() {
                        // Print progress
                        log.info("Relating {}/{}. Cluster: '{}' - Subcluster: '{}'", j + 1, areas.size(), cluster, subcluster);
                        task.updateDetailMessage("Cluster", cluster);
                        task.updateDetailMessage("Subluster", subcluster);
                        task.updateProgress(j + 1);
                        // Get authors of an area from the SPARQL endpoint and transform them to JSON .
                        String authorsByArea = commonService.getsubClusterGraph(cluster, subcluster);
                        Document parse = Document.parse(authorsByArea);
                        BasicDBObject key = new BasicDBObject();
                        key.put("cluster", cluster);
                        key.put("subcluster", subcluster);
                        parse.append("_id", key);
                        collection.insertOne(parse);
                    }
                });

            }
            threadPool.end();
        } catch (MarmottaException | InterruptedException ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            taskManagerService.endTask(task);
        }
    }

    @Override
    public void authorsByDiscipline() {
        final Task task = taskManagerService.createSubTask("Caching Authors by Discipline", "Mongo Service");
        try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
            MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());

            // Delete and create collection
            final MongoCollection<Document> collection = db.getCollection(MongoService.Collection.AUTHORS_DISCPLINE.getValue());
            collection.drop();

            final List<Map<String, Value>> clusters = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getClusterURIs());

            task.updateTotalSteps(clusters.size());
            BoundedExecutor threadPool = BoundedExecutor.getThreadPool(5);
            for (int i = 0; i < clusters.size(); i++) {
                final int j = i;
                final String cluster = clusters.get(i).get("c").stringValue();
                threadPool.submitTask(new Runnable() {
                    @Override
                    public void run() {
                        // String subcluster = areas.get(i).get("subcluster").stringValue();
                        // Print progress
                        log.info("Relating {}/{}. Cluster: '{}'", j + 1, clusters.size(), cluster);
                        task.updateDetailMessage("Cluster", cluster);
                        // task.updateDetailMessage("Subluster", subcluster);
                        task.updateProgress(j + 1);
                        // Get authors of an area from the SPARQL endpoint and transform them to JSON .
                        String authorsByDisc = commonService.getClusterGraph(cluster);
                        Document parse = Document.parse(authorsByDisc);
                        BasicDBObject key = new BasicDBObject();
                        key.put("cluster", cluster);
                        //key.put("subcluster", subcluster);
                        parse.append("_id", key);
                        collection.insertOne(parse);
                    }
                });

            }
            threadPool.end();
        } catch (MarmottaException | InterruptedException ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            taskManagerService.endTask(task);
        }
    }

    @Override
    public void Countries() {
        Task task = taskManagerService.createSubTask("Caching countries", "Mongo Service");
        try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
            MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());

            // Delete and create collection
            MongoCollection<Document> collection = db.getCollection(MongoService.Collection.COUNTRIES.getValue());
            collection.drop();
            try {
                List<Map<String, Value>> countries = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getCountries());
                task.updateTotalSteps(countries.size());
                for (int i = 0; i < countries.size(); i++) {
                    String co = countries.get(i).get("co").stringValue();
                    String code = getCountryCode(co);
                    String countriesNodes = countrynodes(co, code).toString();
                    Document parse = Document.parse(countriesNodes);
                    parse.append("_id", co);
                    collection.insertOne(parse);

                    task.updateDetailMessage("Country", co);
                    task.updateProgress(i + 1);
                }
            } catch (MarmottaException ex) {
                java.util.logging.Logger.getLogger(PopulateMongoImpl.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                taskManagerService.endTask(task);
            }
        }
    }

    private ObjectNode countrynodes(String name, String code) {
        ObjectNode node = factory.objectNode(); // initializing
        node.put("name", name);
        node.put("code", code);
        return node;
    }

    private String getCountryCode(String coName) {

        try {
            String Country = URLEncoder.encode(coName, "UTF-8").replace("+", "%20");
            Object js = commonServices.getHttpJSON("https://restcountries.eu/rest/v2/name/" + Country);
            JSONArray json = null;
            if (js instanceof JSONArray) {
                json = (JSONArray) js;
            }
            System.out.print(js);
            for (Object j : json) {
                // System.out.println ("ite");
                // System.out.println ((JSONObject) j);
                JSONObject jo = (JSONObject) j;
                Object r = jo.get("alpha2Code");
                // System.out.println ("---"+r);
                return r.toString();

            }
        } catch (UnsupportedEncodingException ex) {
            java.util.logging.Logger.getLogger(PopulateMongoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void publications() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cleanSPARQLS() {
        try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
            MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());
            MongoCollection<Document> collection = db.getCollection(MongoService.Collection.SPARQLS.getValue());
            collection.drop();
        }
    }

}
