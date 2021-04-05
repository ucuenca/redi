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

import at.newmedialab.lmf.search.api.indexing.SolrIndexingService;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.google.common.collect.Lists;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import edu.ucuenca.storage.api.MongoService;
import edu.ucuenca.storage.api.PopulateMongo;
import edu.ucuenca.storage.utils.TranslatorManager;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.DistanceService;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.apache.marmotta.ucuenca.wk.commons.util.BoundedExecutor;
import org.apache.marmotta.ucuenca.wk.pubman.api.CommonService;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.BIBO;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import scala.actors.threadpool.Arrays;

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
  private ExternalSPARQLService fastSparqlService;
  @Inject
  private Logger log;
  @Inject
  private CommonService commonService;
  @Inject
  private CommonsServices commonServices;
  @Inject
  private DistanceService distservice;
  @Inject
  private TaskManagerService taskManagerService;

  @Inject
  private TranslatorManager trService;

  @Inject
  private ConstantService conService;

  @Inject
  private SolrIndexingService solrIndexingService;

  @Inject
  private PopulateMongo loadService;

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
      RepositoryConnection conn = fastSparqlService.getRepositoryConnetion();

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
      RepositoryConnection conn = fastSparqlService.getRepositoryConnetion();

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
  public void authors(String uri) {
    final Task task = taskManagerService.createSubTask("Caching authors profiles", "Mongo Service");
    try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
      MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());
      // Delete and create collection
      final MongoCollection<Document> collection = db.getCollection(MongoService.Collection.AUTHORS.getValue());
      List<Map<String, Value>> authorsRedi2;
      if (uri != null) {
        Document pk = new Document();
        pk.put("_id", uri);
        collection.deleteOne(pk);
        authorsRedi2 = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, "select ?a { values ?a { <" + uri + "> } . }");
      } else {
        collection.drop();
        authorsRedi2 = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getAuthorsCentralGraph());
      }

      final List<Map<String, Value>> authorsRedi = authorsRedi2;
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

  @Override
  public void LoadStatisticsbyInst() {
    Task task = taskManagerService.createSubTask("Caching statistics by Institution", "Mongo Service");
    try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
      MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());
      MongoCollection<Document> collection = db.getCollection(MongoService.Collection.STATISTICS_INST.getValue());
      collection.drop();

      List<String> queries = new ArrayList();
      queries.add("inst_by_area");
      queries.add("pub_by_date");
      queries.add("author_by_inst");
      queries.add("inst_by_inst");
      queries.add("prov_by_inst");

      String uri = "";
      String name = "";
      String fullname = "";
      List<Map<String, Value>> org = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getListOrganizationQuery());
      Document parse = new Document();
      task.updateTotalSteps((org.size() + 1) * (queries.size() + 1));
      int ints = 0;
      for (Map<String, Value> o : org) {

        uri = o.get("URI").stringValue();
        name = o.get("name").stringValue();
        fullname = o.get("fullNameEs").stringValue();
        task.updateDetailMessage("Institution ", uri);
        for (String q : queries) {
          ints++;
          String response = statisticsbyInstQuery(uri, q);

          parse.append(q, Document.parse(response));

          log.info("Stats Inst {} ", uri);
          log.info("Query {}", q);

          task.updateProgress(ints);

        }

        parse.append("_id", uri);
        parse.append("name", name);
        parse.append("fullname", fullname);
        collection.insertOne(parse);
      }
      taskManagerService.endTask(task);
      // loadStadistics(MongoService.Collection.STATISTICS.getValue(), queries);
    } catch (MarmottaException ex) {
      log.error("erro" + ex);
      java.util.logging.Logger.getLogger(PopulateMongoImpl.class.getName()).log(Level.INFO, null, ex);
    }
  }

  private String statisticsbyInstQuery(String uri, String query) throws MarmottaException {
    // List <Map<String,String>>   
    switch (query) {
      case "inst_by_area":
        return this.getStatsInstbyArea(uri);
      case "pub_by_date":
        return this.getStatsInstbyPubDate(uri);
      case "author_by_inst":
        return this.getTopAuthorbyInst(uri);
      case "inst_by_inst":
        return this.getTopInstbyInst(uri);
      case "prov_by_inst":
        return this.getTopProvbyInst(uri);
      default:
        return null;

    }

  }

  private int countCountries() {
    try {
      List<Map<String, Value>> countc = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getCountCountry());
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
      BoundedExecutor threadPool = BoundedExecutor.getThreadPool(3);
      task.updateMessage("Calculating related authors");
      final List<Map<String, Value>> query = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getAuthorsCentralGraph());
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

      List<Map<String, Value>> clusters = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getClusterURIs());

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
      List<Map<String, Value>> query = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getClusterTotals());
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
        List<Map<String, Value>> query1 = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getSubClusterTotals(uri));
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

      final List<Map<String, Value>> areas = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getClusterAndSubclusterURIs());

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

      final List<Map<String, Value>> clusters = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getClusterURIs());

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
        List<Map<String, Value>> countries = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getCountries());
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
  public void ProjectProfile() {
    Task task = taskManagerService.createSubTask("Caching projects", "Mongo Service");
    try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
      MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());

      // Delete and create collection
      MongoCollection<Document> collection = db.getCollection(MongoService.Collection.PROJECTPROFILE.getValue());
      collection.drop();
      try {
        List<Map<String, Value>> projects = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getProjects());
        task.updateTotalSteps(projects.size());
        for (int i = 0; i < projects.size(); i++) {
          String uri = projects.get(i).get("uri").stringValue();
          String proj = getProfileInfo( uri);
          Document parse = Document.parse(proj);
          parse.append("_id", uri);
          collection.insertOne(parse);

          task.updateDetailMessage("Project", uri);
          task.updateProgress(i + 1);
        }
      } catch (MarmottaException ex) {
        java.util.logging.Logger.getLogger(PopulateMongoImpl.class.getName()).log(Level.SEVERE, null, ex);
      } finally {
        taskManagerService.endTask(task);
      }
    }
  }
  
  
  @Override
  public void PatentProfile() {
    Task task = taskManagerService.createSubTask("Caching patents", "Mongo Service");
    try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
      MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());

      // Delete and create collection
      MongoCollection<Document> collection = db.getCollection(MongoService.Collection.PATENTPROFILE.getValue());
      collection.drop();
      try {
        List<Map<String, Value>> patents = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getPatents());
        task.updateTotalSteps(patents.size());
        for (int i = 0; i < patents.size(); i++) {
          String uri = patents.get(i).get("uri").stringValue();
          String pat = getPatentInfo( uri);
          Document parse = Document.parse(pat);
          parse.append("_id", uri);
          collection.insertOne(parse);

          task.updateDetailMessage("Patent", uri);
          task.updateProgress(i + 1);
        }
      } catch (MarmottaException ex) {
        java.util.logging.Logger.getLogger(PopulateMongoImpl.class.getName()).log(Level.SEVERE, null, ex);
      } finally {
        taskManagerService.endTask(task);
      }
    }
  }
  
  public String getProfileInfo(String uri) throws MarmottaException {
    List<Map<String, Value>> proy = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getProjectInfo(uri));
    JSONObject main = new JSONObject();
    //JSONArray array = new JSONArray();
    JSONObject obj = new JSONObject();
    for (Map<String, Value> a : proy) {
      obj.put("uri",  uri );
      obj.put("title", a.get("title").stringValue());
      obj.put("starDate", validateexist ( a.get("starDate")) );
      obj.put("endDate", validateexist (a.get("endDate")));
      obj.put("funders", validateexist (a.get("funders")));
      obj.put("members", validateexist(a.get("orgs")));
      //obj.put("members", validateexist(a.get("orgs")));
      
      
    }
    main.put("data", obj);
    return main.toJSONString();
  }
  
  
  public String getPatentInfo(String uri) throws MarmottaException {
    List<Map<String, Value>> patent = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getPatentInfo(uri));
    JSONObject main = new JSONObject();
    //JSONArray array = new JSONArray();
    JSONObject obj = new JSONObject();
    for (Map<String, Value> a : patent) {
      obj.put("uri",  uri );
      obj.put("title", a.get("title").stringValue());
      obj.put("identifier",  a.get("pnumber") );
      obj.put("subject", validateexist (a.get("subjects")));
      obj.put("abstract", validateexist (a.get("abstract")));
      obj.put("rdate", validateexist (a.get("rdate")));
      obj.put("adate", validateexist (a.get("adate")));
      obj.put("edate", validateexist (a.get("edate")));
      obj.put("link", validateexist(a.get("link")));
      obj.put("name", validateexist(a.get("name")));
      obj.put("lorgs", validateexist(a.get("lorgs")));
      
      
    }
    main.put("data", obj);
    return main.toJSONString();
  }
  
  
    public String validateexist ( Value val){
    
     return  val == null ? "" : val.stringValue();
    }
  
  @Override
  public void areasbydocument() {
     Task task = taskManagerService.createSubTask("Caching areas", "Mongo Service");
    try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
      MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());

      // Delete and create collection
      MongoCollection<Document> collection = db.getCollection(MongoService.Collection.DOCUMENTBYAREA.getValue());
      collection.drop();
      try {
        List<Map<String, Value>> clusters = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getClusterURIs());
        task.updateTotalSteps(clusters.size());
        for (int i = 0; i < clusters.size(); i++) {
          String uri = clusters.get(i).get("c").stringValue();
          String doc = getDocumentsbyArea(uri);
          String author = getStatsQuerysArea (uri , 1);
          String org = getStatsQuerysArea (uri , 2);
          String prov = getStatsQuerysArea (uri , 3);
          if (doc != null ) {
            Document area = new Document ();
            Document parse = Document.parse(doc);
            area.append("_id", uri);
            area.append("date", parse);
            area.append("authors", Document.parse(author));
            area.append("orgs", Document.parse(org));
            area.append("provs", Document.parse(prov) );
            collection.insertOne(area);
          }  
          task.updateDetailMessage("Area", uri);
          task.updateProgress(i + 1);
        }
      } catch (MarmottaException ex) {
        java.util.logging.Logger.getLogger(PopulateMongoImpl.class.getName()).log(Level.SEVERE, null, ex);
      } finally {
        taskManagerService.endTask(task);
      }
    }
  }
  
  public  String getDocumentsbyArea(String uri) throws MarmottaException{
        String response;
    
        List<Map<String, Value>> docs = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getDocumentbyArea(uri));
       
        TreeMap <String, Integer> tree_map  = new TreeMap (); 
        for (int j = 0 ; j < docs.size() ; j++ ) {
         Map<String, Value> doc = docs.get(j);
         String keys = doc.get("tl") == null ?  doc.get("documentText").stringValue().toLowerCase() :  doc.get("tl").stringValue().toLowerCase()  ;
         if (keys.isEmpty()){ continue;}
         keys = keys.replaceAll("[^a-zA-Z0-9ñáéíóú\\s]", "");
         keys = keys.replaceAll("[\\n\\s+]", " ");
         String date = doc.get("y").stringValue();
         String uridoc = doc.get("doc").stringValue();
         String query = "PREFIX :<http://www.ontotext.com/graphdb/similarity/>\n" +
                        "PREFIX inst:<http://www.ontotext.com/graphdb/similarity/instance/>\n" +
                        "PREFIX pubo: <http://ontology.ontotext.com/publishing#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "\n" +
                        "SELECT  ?documentID (SAMPLE(?ll) as ?label) ?score {\n" +
                        "    ?search a inst:uareas ;\n" +
                        "        :searchTerm '"+keys+"' ;" +
                        "        :searchParameters '';\n" +
                        "        :documentResult ?result .\n" +
                        "    ?result :value ?documentID ;\n" +
                        "            :score ?score.\n" +
                        "    ?documentID rdfs:label ?ll\n" +
                        "	} group by ?documentID ?score";
         System.out.println (j+"-"+uridoc+":"+keys);
         List<Map<String, Value>> areasc = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, query);
          if (!areasc.isEmpty() && areasc.size() > 1){
             if (uri.equals(areasc.get(0).get("documentID").stringValue()) || uri.equals(areasc.get(1).get("documentID").stringValue()) ){
                 insertAreabyDoc (conService.getClusterGraph() , uridoc , "http://purl.org/dc/terms/isPartOf" , uri );
                 insertAreabyDoc (conService.getClusterGraph() , uridoc , RDF.TYPE.toString() , BIBO.ACADEMIC_ARTICLE.toString() );
                 if (tree_map.containsKey(date)){ 
                   tree_map.put(date, tree_map.get(date)+1);
                 }else {
                   tree_map.put(date, 1);
                 }
             }
          }
              
        }
        if (!tree_map.isEmpty()){
          response = JSONValue.toJSONString(tree_map);
        }else {
          response = null;
        } 
      return response;
  }
  
  public void insertAreabyDoc (String graph , String subject , String  property , String  object  ) {
    try {
      String insertq = queriesService.buildInsertQuery(graph, subject, property, object);
      fastSparqlService.getSparqlService().update(QueryLanguage.SPARQL, insertq);
    } catch (MarmottaException | InvalidArgumentException | MalformedQueryException | UpdateExecutionException ex) {
      java.util.logging.Logger.getLogger(PopulateMongoImpl.class.getName()).log(Level.SEVERE, null, ex);
    }
  
  }
  
  
  public String getStatsQuerysArea(String uri , int number) throws MarmottaException {
    String query = "";
    switch (number) {
      case 1 : query = queriesService.getAuthorsbyArea(uri) ; break;
      case 2 : query = queriesService.getOrgsbyArea(uri) ; break;
      case 3 : query = queriesService.getProvbyArea(uri);  break;
        
    }
    
    
    List<Map<String, Value>> data = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, query);
    JSONObject main = new JSONObject();
    JSONArray array = new JSONArray();
    for (Map<String, Value> a : data) {
      if (a.get("uri") == null) {
        continue;
      }
      JSONObject obj = new JSONObject();
      obj.put("uri", a.get("uri").stringValue());
      obj.put("name", commonService.getUniqueName(a.get("names").stringValue(), ";"));
      obj.put("total", a.get("number").stringValue());
      array.add(obj);
    }
    main.put("data", array);
    return main.toJSONString();
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

  public String getStatsInstbyPubDate(String uri) throws MarmottaException {
    List<Map<String, Value>> years = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getDatesPubbyInst(uri));
    JSONObject main = new JSONObject();
    JSONArray array = new JSONArray();
    for (Map<String, Value> y : years) {
      if (y.get("y") == null) {
        continue;
      }
      JSONObject obj = new JSONObject();
      obj.put("y", y.get("y").stringValue());
      obj.put("total", y.get("total").stringValue());
      array.add(obj);
    }
    main.put("data", array);
    return main.toJSONString();

  }

  public String getStatsInstbyArea(String uri) throws MarmottaException {
    List<Map<String, Value>> area = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getClustersbyInst(uri));
    JSONObject main = new JSONObject();
    JSONArray array = new JSONArray();
    for (Map<String, Value> a : area) {
      if (a.get("area") == null) {
        continue;
      }
      JSONObject obj = new JSONObject();
      obj.put("uri", a.get("area").stringValue());
      obj.put("name", a.get("nameng").stringValue());
      obj.put("total", a.get("total").stringValue());
      array.add(obj);
    }
    main.put("data", array);
    return main.toJSONString();
  }

  public String getTopAuthorbyInst(String uri) throws MarmottaException {
    List<Map<String, Value>> author = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getAuthorsbyInst(uri));
    JSONObject main = new JSONObject();
    JSONArray array = new JSONArray();
    for (Map<String, Value> a : author) {
      if (a.get("author") == null) {
        continue;
      }
      JSONObject obj = new JSONObject();
      obj.put("uri", a.get("author").stringValue());
      obj.put("name", commonService.getUniqueName(a.get("name").stringValue(), ";"));
      obj.put("total", a.get("total").stringValue());
      array.add(obj);
    }
    main.put("data", array);
    return main.toJSONString();
  }

  public String getTopInstbyInst(String uri) throws MarmottaException {
    List<Map<String, Value>> inst = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getInstAsobyInst(uri));
    JSONObject main = new JSONObject();
    JSONArray array = new JSONArray();
    for (Map<String, Value> ins : inst) {
      if (ins.get("org") == null) {
        continue;
      }
      JSONObject obj = new JSONObject();
      obj.put("uri", ins.get("org").stringValue());
      obj.put("name", ins.get("norg").stringValue());
      obj.put("total", ins.get("total").stringValue());
      array.add(obj);
    }
    main.put("data", array);
    return main.toJSONString();
  }

  public String getTopProvbyInst(String uri) throws MarmottaException {

    List<Map<String, Value>> prov = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getProvbyInst(uri));
    JSONObject main = new JSONObject();
    JSONArray array = new JSONArray();
    for (Map<String, Value> p : prov) {
      if (p.get("prov") == null) {
        continue;
      }
      JSONObject obj = new JSONObject();
      String uriprov = p.get("prov").stringValue();
      obj.put("uri", uriprov);
      obj.put("name", providerName(uriprov.substring(uriprov.lastIndexOf("#") + 1)));
      obj.put("total", p.get("total").stringValue());
      array.add(obj);
    }
    main.put("data", array);

    return main.toJSONString();
  }

  public String providerName(String text) {
    return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(text), ' ').replace("Provider", "");
  }

  public String getStatsAuthorbyPubDate(String uri) throws MarmottaException {
    List<Map<String, Value>> years = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getAuthorPubbyDate(uri));
    JSONObject main = new JSONObject();
    JSONArray array = new JSONArray();
    for (Map<String, Value> y : years) {
      JSONObject obj = new JSONObject();
      if (y.containsKey("y")) {
        obj.put("y", y.get("y").stringValue());
        obj.put("total", y.get("total").stringValue());
        array.add(obj);
      }
    }
    main.put("data", array);
    return main.toJSONString();
  }
  
    public String getAreasDate (String area_uri) throws MarmottaException {
    JSONObject main = new JSONObject();
    JSONArray array = new JSONArray();
    List<Map<String, Value>> areasDate = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getResearchPubDate ( area_uri ));
    for (Map<String, Value> subareas : areasDate){
        String subarea_uri = subareas.get ("area").stringValue();
        String year = subareas.get("y").stringValue();
        String total = subareas.get ("total").stringValue();
        
        JSONObject obj = new JSONObject();
        obj.put("y", year);
        obj.put("total", total);
        array.add(obj);
    }
    
     main.put("data", array);
    return main.toJSONString();
    
    }
  
    
    @Override
    public String getPublicationDatesbyAreas ()  {
       final Task task = taskManagerService.createSubTask("Caching statistics by Publications Areas", "Mongo Service");
    try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
      MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());
      MongoCollection<Document> collection = db.getCollection(MongoService.Collection.DOCUMENTDATEBYAREA.getValue());
      collection.drop();
      MongoCollection<Document> collectionsub = db.getCollection(MongoService.Collection.DOCUMENTDATEBYSUBAREA.getValue());
      collectionsub.drop();
      String area_act = "";
    
    List<Map<String, Value>> areas = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getAreasSubAreasPub());
    for ( Map<String, Value> area : areas ){
        String area_uri = area.get ("area").stringValue();
        String area_label = area.get("label").stringValue();
        String subarea_uri = area.get ("subarea").stringValue();
        String subarea_label = area.get ("labels").stringValue();
        
        if (!area_act.equals(area_uri)) {
             String responseareas = getAreasDate (area_uri);
             area_act = area_uri;
              Document parse = Document.parse(responseareas);
              parse.append("_id", area_uri);
              parse.append("area", area_uri);
              parse.append("label", area_label);
              collection.insertOne(parse);
        }
        
        String responsesub = getAreasDate (subarea_uri);
   
    

        Document parsesub = Document.parse(responsesub);
        parsesub.append("_id", area_uri+"|"+subarea_uri);
        parsesub.append("area", area_uri);
        parsesub.append("subarea", subarea_uri);
        parsesub.append("label", subarea_label);
        collectionsub.insertOne(parsesub);
        
        
    
    } 
    
    } catch (Exception ex) {
    return "Error";
    }
    

  
    return "Success";
  }

  public String getStatsRelevantKbyAuthor(String uri) throws MarmottaException {
    List<Map<String, Value>> key = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getRelevantKbyAuthor(uri, 15));
    JSONObject main = new JSONObject();
    JSONArray array = new JSONArray();
    for (Map<String, Value> k : key) {
      if (k.containsKey("lsubject")) {
        JSONObject obj = new JSONObject();
        obj.put("subject", k.get("lsubject").stringValue());
        obj.put("total", k.get("npub").stringValue());

        array.add(obj);
      }
    }
    main.put("data", array);
    return main.toJSONString();
  }

  /*  public String getStatsConferencebyAuthor (String uri) throws MarmottaException { 
     List<Map<String, Value>> prov = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getConferencebyAuthor(uri));
     JSONObject main = new JSONObject();
     JSONArray array = new JSONArray();
     for (Map<String, Value> p :prov){
     JSONObject obj = new JSONObject();
     obj.put("name", p.get("name").stringValue());
     obj.put("total",p.get("total").stringValue());
     array.add(obj);
     }
     main.put("data", array);
    
     return main.toJSONString();
     }*/
  public String getStatsConferencebyAuthor(String uri) throws MarmottaException {
    //  List<Map<String, Value>> prov1 = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getConferencebyAuthor(uri));
    // System.out.print (prov1);

    List<Map<String, Value>> prov = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getConferencebyAuthor(uri));
    System.out.print("Dentro ");

    Map<String, Integer> auxmap = new HashMap();
    for (Map<String, Value> p : prov) {
      if (!p.containsKey("name")) {
        continue;
      }
      String[] cand = p.get("name").stringValue().split(";");
      for (String c : cand) {
        String[] w = c.split(" ");
        for (String word : w) {
          String cw = word.replaceAll("\\)|\\(|:|,", " ").trim();
          if (cw.length() > 2 && !StringUtils.isNumericSpace(cw) && cw.equals(cw.toUpperCase())) {
            if (!auxmap.containsKey(cw)) {
              auxmap.put(cw, 0);
            } else {
              auxmap.put(cw, auxmap.get(cw) + 1);
            }
          }
        }
      }

      Iterator it = auxmap.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry pair = (Map.Entry) it.next();
        if (p.get("name").stringValue().contains(pair.getKey().toString())) {
          auxmap.put(pair.getKey().toString(), (Integer) pair.getValue() + Integer.parseInt(p.get("total").stringValue()));
        }
      }

    }
    JSONObject main = new JSONObject();
    JSONArray array = new JSONArray();
    List keys = new ArrayList(auxmap.keySet());
    Collections.sort(keys);
    System.out.print(keys);
    for (String key : auxmap.keySet()) {
      JSONObject obj = new JSONObject();
      obj.put("name", key);
      obj.put("total", auxmap.get(key));
      array.add(obj);
    }

    main.put("data", array);

    return main.toJSONString();
  }

  //DATATEST
/*
    public List<Map<String, String>> getdata() {
        String[] name = {"Maskana;MASKANA", "eswc;ESWC", "IEEE Transactions on Industrial Electronics",
            "eswc;ESWC;European Semantic Web Conference", "RISTI: Revista Ibérica de Sistemas e Tecnologias de Informação ", "2017 XLIII Latin American Computer Conference (CLEI) ", "Computer Conference (CLEI), 2017 XLIII Latin American ",
            "Ecuador Technical Chapters Meeting (ETCM), 2017 IEEE",
            "2017 IEEE 2nd Ecuador Technical Chapters Meeting, ETCM 2017 ", "iecon;IECON"};
        String[] total = {"6", "4", "4", "4", "4", "4", "4", "4", "3", "3"};
        List<Map<String, String>> prov = new ArrayList();
        for (int i = 0; i < name.length; i++) {
            Map<String, String> m = new HashMap();
            m.put("name", name[i]);
            m.put("total", total[i]);
            prov.add(m);
        }

        return prov;

    }*/
  // DATATEST 
  /*  public List<Map<String, String>> getdataAff() {
        String[] name = {"Universidad Politécnica de Madrid", "Universidad de Cuenca", "Departamento de Ciencias de la Computación, Universidad de Cuenca, Av. de Abril s/n y Agustín Cueva, Cuenca, Ecuador,",
            "UCUENCA", "University of Zaragoza ", "Universidad Politecnica de Madrid", "Universidad de Cuenca",
            "Universidad Politecnica de Madrid",
            "Universidad de Cuenca, Universidad de Zaragoza, UPM"};
        //String [] total = {"6","4","4","4","4","4","4","4","3","3"};
        List<Map<String, String>> prov = new ArrayList();
        for (int i = 0; i < name.length; i++) {
            Map<String, String> m = new HashMap();
            m.put("orgname", name[i]);
            //m.put("total", total[i]);
            prov.add(m);
        }

        return prov;

    }*/
 /*providerName(uriprov.substring(uriprov.lastIndexOf("#") + 1)) */
  public String getStatsProvbyAuthor(String uri) throws MarmottaException {
    List<Map<String, Value>> key = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getRelevantProvbyAuthor(uri));
    JSONObject main = new JSONObject();
    JSONArray array = new JSONArray();
    for (Map<String, Value> k : key) {
      if (k.containsKey("prov")) {
        String uriprov = k.get("prov").stringValue();
        JSONObject obj = new JSONObject();
        obj.put("uri", uriprov);
        obj.put("prov", providerName(uriprov.substring(uriprov.lastIndexOf("#") + 1)));
        obj.put("total", k.get("total").stringValue());

        array.add(obj);
      }
    }
    main.put("data", array);
    return main.toJSONString();
  }

  private String getLang(Value value) {
    if (value instanceof Literal) {
      Literal lit = (Literal) value;
      return lit.getLanguage();
    }
    return "";
  }

  public String getStatsAffbyAuthor(String uri) throws MarmottaException {
    //   List<Map<String, String>> provn = getdataAff();
    List<Map<String, Value>> provn = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getOrgbyAuyhor(uri));
    List<Map<String, Integer>> affl = new ArrayList();
    Map<String, Integer> auxmap = new HashMap();
    List<String> blackl = new ArrayList();
    // affl.add(auxmap);

    for (Map<String, Value> p : provn) {
      if (!p.containsKey("orgname")) {
        continue;
      }
      String l = getLang(p.get("orgname"));

      if ("en".equals(l)) {
        blackl.add(p.get("orgname").stringValue().trim().toLowerCase());
        continue;
      }

      String[] cand = p.get("orgname").stringValue().split("(;)|(,)");
      String uricand = p.get("org").stringValue();
      Map<String, Integer> newmap = new HashMap();
      for (String c : cand) {
        String word = c.replaceAll("\\)|\\(|:|,", " ").trim().toLowerCase();
        if (validwords(word)) {
          newmap.put(word, 0);
        }

      }
      if (affl.isEmpty()) {
        affl.add(newmap);
      } else {
        boolean match = false;
        int size = affl.size();
        for (int i = 0; i < size; i++) {
          auxmap = affl.get(i);
          Map<String, Integer> mp = compareAff(newmap, auxmap);
          if (mp != null) {
            affl.add(i, commonServices.sortByComparator(mp, false));
            affl.remove(i + 1);
            match = true;
          }
        }
        if (!match) {
          affl.add(commonServices.sortByComparator(newmap, false));
        }

      }
    }

    JSONObject main = new JSONObject();
    JSONArray array = new JSONArray();
    List keys = new ArrayList(auxmap.keySet());
    Collections.sort(keys);
    System.out.print(keys);
    for (Map<String, Integer> af : affl) {
      JSONObject obj = new JSONObject();
      int n = 0;
      for (String a : af.keySet()) {
        if (n < 1 && !blackl.contains(a)) {
          obj.put("name", a);
          array.add(obj);
        }
        n++;
      }
    }

    main.put("data", array);

    return main.toJSONString();
    // distservice.jaccardDistance("", "");
    // Map<String, Integer> unsortMap = new HashMap<String, Integer>();
    //  Map<String, Integer> map =commonServices.sortByComparator(unsortMap, false);

    //  return "";
  }

  public boolean validwords(String key) {
    List<String> vw = new ArrayList();
    vw.add("university");
    vw.add("universidad");
    vw.add("institute");
    vw.add("instituto");
    vw.add("department");
    vw.add("departamento");
    for (String w : vw) {
      if (key.contains(w)) {
        return true;
      }
    }

    return false;

  }

  private Map<String, Integer> compareAff(Map<String, Integer> newmap, Map<String, Integer> oldmap) {
    boolean equival = false;
    Map<String, Integer> temp = new HashMap();

    for (String key2 : oldmap.keySet()) {
      for (String key : newmap.keySet()) {
        if (distservice.jaccardDistance(key, key2) > 0.7) {
          equival = true;
          temp.put(key2, oldmap.get(key2) + 1);
        } else {
          temp.put(key, 0);
        }
      }
    }
    if (equival) {
      oldmap.putAll(temp);
    }
    return equival ? oldmap : null;
  }

  @Override
  public String getStatsbyAuthor() {
    String uri = "https://redi.cedia.edu.ec/resource/authors/UCUENCA/oai-pmh/SAQUICELA_GALARZA__VICTOR_HUGO";
    System.out.print("Entrando Metodo");
    try {
      getStatsConferencebyAuthor(uri);
      return getStatsAffbyAuthor(uri);
    } catch (MarmottaException ex) {
      java.util.logging.Logger.getLogger(PopulateMongoImpl.class.getName()).log(Level.SEVERE, null, ex);
      return "error";
    }
  }

  @Override
  public void LoadStatisticsbyAuthor() {
    final Task task = taskManagerService.createSubTask("Caching statistics by Author", "Mongo Service");
    try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
      MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());
      MongoCollection<Document> collection = db.getCollection(MongoService.Collection.STATISTICS_AUTHOR.getValue());
      collection.drop();

      List<String> queries = new ArrayList();
      queries.add("date");
      queries.add("keywords");
      queries.add("providers");
      queries.add("provenance");
      queries.add("conference");

      final String uri = "";
      String name = "";
      String fullname = "";
      final List<Map<String, Value>> authors = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, queriesService.getAuthorsCentralGraph());
      Document parse = new Document();
      task.updateTotalSteps(authors.size());
      int ints = 0;
      // final int j = 0;
      for (Map<String, Value> o : authors) {
        //  j++;
        ints++;

        final String a = o.get("a").stringValue();
        task.updateDetailMessage("Author ", a);
        final SynchronizedParse sp = new SynchronizedParse();
        BoundedExecutor threadPool = BoundedExecutor.getThreadPool(5);

        log.info("Stats {} ", a);
        log.info("Stats {}/{}. Author: '{}' ", ints, authors.size(), a);
        //task.updateDetailMessage("URI", a);
        task.updateProgress(ints);
        for (final String q : queries) {

          threadPool.submitTask(new Runnable() {
            @Override
            public void run() {

              String response;
              try {
                response = statisticsbyAuthorsQuery(a, q);
                sp.appendParse(Document.parse(response), q);

              } catch (MarmottaException ex) {
                java.util.logging.Logger.getLogger(PopulateMongoImpl.class.getName()).log(Level.SEVERE, null, ex);
              }

            }
          });

          /*  ints++;
                    uri = a;
                    parse.append(q, Document.parse(response));

                    log.info("Stats Author {} ", uri);
                    log.info("Query {}", q);

                    task.updateProgress(ints);*/
        }
        threadPool.end();
        Document authorp = sp.getDoc();
        authorp.append("_id", a);
        // parse.append("name", name);
        // parse.append("fullname", fullname);
        collection.insertOne(authorp);
      }
      taskManagerService.endTask(task);
      // loadStadistics(MongoService.Collection.STATISTICS.getValue(), queries);
    } catch (MarmottaException ex) {
      log.error("erro" + ex);
      java.util.logging.Logger.getLogger(PopulateMongoImpl.class.getName()).log(Level.INFO, null, ex);
    } catch (InterruptedException ex) {
      java.util.logging.Logger.getLogger(PopulateMongoImpl.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private String statisticsbyAuthorsQuery(String uri, String query) throws MarmottaException {
    // List <Map<String,String>>   
    switch (query) {
      case "date":
        return this.getStatsAuthorbyPubDate(uri);
      case "keywords":
        return this.getStatsRelevantKbyAuthor(uri);
      case "providers":
        return this.getStatsProvbyAuthor(uri);
      case "provenance":
        return this.getStatsAffbyAuthor(uri);
      case "conference":
        return this.getStatsConferencebyAuthor(uri);
      default:
        return null;

    }

  }

  @Override
  public void populatePublicationTranslations() {
    final Task task = taskManagerService.createSubTask("Translating publications", "Mongo Service");
    try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
      MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());
      MongoCollection<Document> collection = db.getCollection(MongoService.Collection.TRANSLATIONS.getValue());
      try {

        //get publications ids
        List<Map<String, Value>> pubs = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, "PREFIX dct: <http://purl.org/dc/terms/>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "PREFIX bibo: <http://purl.org/ontology/bibo/>\n"
                + "select distinct ?p {\n"
                + "    graph <" + conService.getCentralGraph() + "> {\n"
                + "    	?p a bibo:AcademicArticle .\n"
                + "    	?p dct:subject [] .\n"
                + "        [] foaf:publications ?p .\n"
                + "    }\n"
                + "}");
        ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
        int i = 0;
        task.updateTotalSteps(pubs.size());
        for (Map<String, Value> pub : pubs) {
          i++;
          task.updateProgress(i);
          String pURI = pub.get("p").stringValue();
          URI createURI = instance.createURI(pURI);
          URI translation = instance.createURI("http://ucuenca.edu.ec/ontology#translation");
          boolean ask = fastSparqlService.getSparqlService().ask(QueryLanguage.SPARQL, "PREFIX dct: <http://purl.org/dc/terms/>\n"
                  + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                  + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                  + "PREFIX bibo: <http://purl.org/ontology/bibo/>\n"
                  + "ask {\n"
                  + "    graph <" + conService.getCentralGraph() + "> {\n"
                  + "    	<" + pURI + "> <" + translation.stringValue() + "> [] .\n"
                  + "    }\n"
                  + "}");
          if (ask) {
            log.info("Translating (skip)... {}", pURI);
            continue;
          } else {
            log.info("Translating ... {}", pURI);
          }
          //get kws by id
          List<Map<String, Value>> kws = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, "PREFIX dct: <http://purl.org/dc/terms/>\n"
                  + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                  + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                  + "PREFIX bibo: <http://purl.org/ontology/bibo/>\n"
                  + "select distinct ?k {\n"
                  + "    graph <" + conService.getCentralGraph() + "> {\n"
                  + "    	<" + pURI + "> dct:subject ?s .\n"
                  + "        ?s rdfs:label ?k .\n"
                  + "    }\n"
                  + "}");
          String txtVal = "";
          for (Map<String, Value> kw : kws) {
            txtVal += kw.get("k").stringValue() + " ;;; ";
          }
          String traductor = "";
          boolean hasNext = collection.find(eq("_id", txtVal.hashCode())).iterator().hasNext();
          if (!hasNext) {
            traductor = trService.traductorIBM(txtVal);
            traductor = traductor .replaceAll("context;;;", "") .replaceAll("context ;;;", "").trim();
            traductor = traductor.replaceAll(";;;", " ; ");
            Document dc = new Document();
            dc.put("_id", txtVal.hashCode());
            dc.put("txt", traductor);
            collection.insertOne(dc);
          }
          Document first = collection.find(eq("_id", txtVal.hashCode())).first();
          traductor = first.getString("txt");
          Model md = new LinkedHashModel();
          md.add(createURI, translation, instance.createLiteral(traductor));
          fastSparqlService.getGraphDBInstance().addBuffer(instance.createURI(conService.getCentralGraph()), md);
          fastSparqlService.getGraphDBInstance().dumpBuffer();
        }
      } catch (Exception ex) {
        log.error(ex.getMessage());
      }
    }
    taskManagerService.endTask(task);
  }

  public String getRandomElement(List<String> list) {
    Random rand = new Random();
    return list.get(rand.nextInt(list.size()));
  }

  @Override
  public void populatePublicationKeywords() {
    String keys = conf.getStringConfiguration("refinitiv.tagging");
    List<String> asList = Arrays.asList(keys.split(";"));
    final Task task = taskManagerService.createSubTask("Keywords extraction from publications", "Mongo Service");
    try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
      MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());
      MongoCollection<Document> collection = db.getCollection(MongoService.Collection.TRANSLATIONS.getValue());
      try {
        //get publications ids
        List<Map<String, Value>> pubs = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX bibo: <http://purl.org/ontology/bibo/>\n"
                + "PREFIX dct: <http://purl.org/dc/terms/>\n"
                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "select distinct ?p {\n"
                + "    graph <" + conService.getCentralGraph() + "> {\n"
                + "        ?o <http://ucuenca.edu.ec/ontology#memberOf> <https://redi.cedia.edu.ec/> . \n"
                + "        ?a <http://schema.org/memberOf> ?o .\n"
                + "        ?a foaf:publications ?p .\n"
                + "        filter not exists {\n"
                + "            ?p dct:subject ?s .\n"
                + "            ?s rdfs:label [] .\n"
                + "        }\n"
                + "    }\n"
                + "}  ");
        ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
        int i = 0;
        task.updateTotalSteps(pubs.size());
        for (Map<String, Value> pub : pubs) {
          i++;
          task.updateProgress(i);
          String pURI = pub.get("p").stringValue();
          URI createURI = instance.createURI(pURI);
          URI extracted = instance.createURI("http://ucuenca.edu.ec/ontology#extracted");
          URI subProp = instance.createURI("http://purl.org/dc/terms/subject");
          boolean ask = fastSparqlService.getSparqlService().ask(QueryLanguage.SPARQL, "PREFIX dct: <http://purl.org/dc/terms/>\n"
                  + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                  + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                  + "PREFIX bibo: <http://purl.org/ontology/bibo/>\n"
                  + "ask {\n"
                  + "    graph <" + conService.getCentralGraph() + "> {\n"
                  + "    	<" + pURI + "> dct:subject ?s .\n"
                  + "    	?s a <" + extracted.toString() + "> .\n"
                  + "    }\n"
                  + "}");
          if (ask) {
            log.info("Extracting keywords (skip)... {}", pURI);
            continue;
          } else {
            log.info("Extracting keywords ... {}", pURI);
          }
          List<Map<String, Value>> kws = fastSparqlService.getSparqlService().query(QueryLanguage.SPARQL, "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                  + "PREFIX bibo: <http://purl.org/ontology/bibo/>\n"
                  + "PREFIX dct: <http://purl.org/dc/terms/>\n"
                  + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                  + "select (group_concat (?t ; separator =' ') as ?k) {\n"
                  + "    graph <" + conService.getCentralGraph() + "> {\n"
                  + "        {\n"
                  + "        	<" + pURI + "> dct:title ?t .\n"
                  + "        } union {\n"
                  + "            <" + pURI + "> bibo:abstract ?t .\n"
                  + "        }\n"
                  + "    }\n"
                  + "} ");
          String txtVal = "";
          for (Map<String, Value> kw : kws) {
            txtVal += kw.get("k").stringValue() + " ";
          }
          String ktxtVal = "keywords_prefix:" + txtVal;
          List<String> extractedKws = Lists.newArrayList();
          boolean hasNext = collection.find(eq("_id", ktxtVal.hashCode())).iterator().hasNext();
          if (!hasNext) {

            HttpResponse<JsonNode> asJson = null;
            try {
              asJson = Unirest.post("https://api.thomsonreuters.com/permid/calais")
                      .header("Content-Type", "text/raw")
                      .header("Accept", "application/json")
                      .header("x-ag-access-token", getRandomElement(asList))
                      .header("outputFormat", "application/json")
                      .body(txtVal)
                      .asJson();
            } catch (Exception e) {
            }
            if (asJson != null && asJson.getStatus() == 200) {
              JsonNode body1 = asJson.getBody();
              org.json.JSONObject object = body1.getObject();
              Set<String> keySet = object.keySet();
              for (String aK : keySet) {
                org.json.JSONObject obj = (org.json.JSONObject) object.get(aK);
                if (obj.has("_typeGroup") && obj.getString("_typeGroup").compareTo("socialTag") == 0) {
                  extractedKws.add(obj.getString("name"));
                }
              }
              Document dc = new Document();
              dc.put("_id", ktxtVal.hashCode());
              dc.put("kws", extractedKws);
              collection.insertOne(dc);
            } else {
              if (asJson != null) {
                log.error("Invalid request {} for {}, code: {}", txtVal, pURI, asJson.getStatus());
              } else {
                log.error("Invalid request {} for {}", txtVal, pURI);
              }
              //throw new Exception("Invalid request");
              continue;
            }
          }
          Document first = collection.find(eq("_id", ktxtVal.hashCode())).first();
          extractedKws = (List<String>) first.get("kws");
          Model md = new LinkedHashModel();
          for (String kw : extractedKws) {
            URI kwK = instance.createURI(conService.getBaseURI() + "extractedSubject/" + URLEncoder.encode(kw));
            md.add(createURI, subProp, kwK);
            md.add(kwK, RDFS.LABEL, instance.createLiteral(kw));
            md.add(kwK, RDF.TYPE, extracted);
          }
          fastSparqlService.getGraphDBInstance().addBuffer(instance.createURI(conService.getCentralGraph()), md);
          fastSparqlService.getGraphDBInstance().dumpBuffer();
        }
      } catch (Exception ex) {
        log.error(ex.getMessage());
      }
    }
    taskManagerService.endTask(task);
  }

  @Override
  public void populateProfileChanges() {

    Model addMRedi = new LinkedHashModel();
    Model delMRedi = new LinkedHashModel();
    Model addMClusters = new LinkedHashModel();
    Model delMClusters = new LinkedHashModel();

    List<URI> updateAuthors = new ArrayList<>();
    List<String> updateIndexResources = new ArrayList<>();

    try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
      MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());
      final MongoCollection<Document> collection = db.getCollection(MongoService.Collection.PROFILE_AUTHOR.getValue());
      FindIterable<Document> find = collection.find();
      for (Document doc : find) {
        if (doc.getString("uri").compareTo("new_") != 0) {
          URI authURI = ValueFactoryImpl.getInstance().createURI(doc.getString("uri"));
          Document profile = (Document) doc.get("profile");
          if (profile.getString("org").startsWith("https://redi.cedia.edu.ec/resource/organization/")) {
            addMRedi.add(authURI, ValueFactoryImpl.getInstance().createURI("http://schema.org/memberOf"), ValueFactoryImpl.getInstance().createURI(profile.getString("org")));
          } else {
            continue;
          }
          updateAuthors.add(authURI);
          updateIndexResources.add(authURI.stringValue());
          addMRedi.add(authURI, ValueFactoryImpl.getInstance().createURI("http://purl.org/spar/scoro/hasORCID"), ValueFactoryImpl.getInstance().createLiteral(doc.getString("_id")));
          addMRedi.add(authURI, FOAF.NAME, ValueFactoryImpl.getInstance().createLiteral(profile.getString("name")));
          addMRedi.add(authURI, FOAF.GIVEN_NAME, ValueFactoryImpl.getInstance().createLiteral(profile.getString("fname")));
          addMRedi.add(authURI, FOAF.FAMILY_NAME, ValueFactoryImpl.getInstance().createLiteral(profile.getString("lname")));
          addMRedi.add(authURI, ValueFactoryImpl.getInstance().createURI("http://www.w3.org/2006/vcard/ns#hasEmail"), ValueFactoryImpl.getInstance().createLiteral(profile.getString("email")));
          addMRedi.add(authURI, ValueFactoryImpl.getInstance().createURI("http://purl.org/vocab/bio/0.1/olb"), ValueFactoryImpl.getInstance().createLiteral(profile.getString("bio")));
          addMRedi.add(authURI, FOAF.IMG, ValueFactoryImpl.getInstance().createLiteral(profile.getString("img")));

          List<List<Document>> namesPages = (List<List<Document>>) doc.get("names");
          for (List<Document> namePage : namesPages) {
            for (Document name : namePage) {
              if (name.getString("id").compareTo("on") != 0) {
                if (name.getBoolean("status")) {
                  addMRedi.add(authURI, FOAF.NAME, ValueFactoryImpl.getInstance().createLiteral(name.getString("id")));
                } else {
                  delMRedi.add(authURI, FOAF.NAME, ValueFactoryImpl.getInstance().createLiteral(name.getString("id")));
                }
              }
            }
          }
          namesPages = (List<List<Document>>) doc.get("emails");
          for (List<Document> namePage : namesPages) {
            for (Document name : namePage) {
              if (name.getString("id").compareTo("on") != 0) {
                if (name.getBoolean("status")) {
                  addMRedi.add(authURI, ValueFactoryImpl.getInstance().createURI("http://www.w3.org/2006/vcard/ns#hasEmail"), ValueFactoryImpl.getInstance().createLiteral(name.getString("id")));
                } else {
                  delMRedi.add(authURI, ValueFactoryImpl.getInstance().createURI("http://www.w3.org/2006/vcard/ns#hasEmail"), ValueFactoryImpl.getInstance().createLiteral(name.getString("id")));
                }
              }
            }
          }
          namesPages = (List<List<Document>>) doc.get("publications");
          for (List<Document> namePage : namesPages) {
            for (Document name : namePage) {
              if (name.getString("id").compareTo("on") != 0) {
                updateIndexResources.add(name.getString("id"));
                if (name.getBoolean("status")) {
                  addMRedi.add(authURI, FOAF.PUBLICATIONS, ValueFactoryImpl.getInstance().createURI(name.getString("id")));
                } else {
                  delMRedi.add(authURI, FOAF.PUBLICATIONS, ValueFactoryImpl.getInstance().createURI(name.getString("id")));
                }
              }
            }
          }

          namesPages = (List<List<Document>>) doc.get("clusters");
          for (List<Document> namePage : namesPages) {
            for (Document name : namePage) {
              if (name.getString("id").compareTo("on") != 0) {
                if (name.getBoolean("status")) {
                  addMClusters.add(authURI, ValueFactoryImpl.getInstance().createURI("http://purl.org/dc/terms/isPartOf"), ValueFactoryImpl.getInstance().createURI(name.getString("id")));
                } else {
                  delMClusters.add(authURI, ValueFactoryImpl.getInstance().createURI("http://purl.org/dc/terms/isPartOf"), ValueFactoryImpl.getInstance().createURI(name.getString("id")));
                }
              }
            }
          }

          namesPages = (List<List<Document>>) doc.get("subclusters");
          for (List<Document> namePage : namesPages) {
            for (Document name : namePage) {
              if (name.getString("id").compareTo("on") != 0) {
                if (name.getBoolean("status")) {
                  addMClusters.add(authURI, ValueFactoryImpl.getInstance().createURI("http://purl.org/dc/terms/isPartOf"), ValueFactoryImpl.getInstance().createURI(name.getString("id")));
                } else {
                  delMClusters.add(authURI, ValueFactoryImpl.getInstance().createURI("http://purl.org/dc/terms/isPartOf"), ValueFactoryImpl.getInstance().createURI(name.getString("id")));
                }
              }
            }
          }

        }
      }

      URI redGrp = ValueFactoryImpl.getInstance().createURI(conService.getCentralGraph());
      URI clsGrp = ValueFactoryImpl.getInstance().createURI(conService.getClusterGraph());
      URI addMRedi_ = ValueFactoryImpl.getInstance().createURI(conService.getCentralGraph() + "_AddLog");
      URI delMRedi_ = ValueFactoryImpl.getInstance().createURI(conService.getCentralGraph() + "_DelLog");
      URI addMClusters_ = ValueFactoryImpl.getInstance().createURI(conService.getClusterGraph() + "_AddLog");
      URI delMClusters_ = ValueFactoryImpl.getInstance().createURI(conService.getClusterGraph() + "_DelLog");

      RepositoryConnection connection = fastSparqlService.getGraphDBInstance().getConnection();
      RepositoryConnection connection2 = fastSparqlService.getRepositoryConnetion();

      connection.begin();
      connection2.begin();

      //Save log only in GraphDB
      fastSparqlService.getGraphDBInstance().runSplitAddOp(connection, addMRedi, addMRedi_);
      fastSparqlService.getGraphDBInstance().runSplitAddOp(connection, delMRedi, delMRedi_);
      fastSparqlService.getGraphDBInstance().runSplitAddOp(connection, addMClusters, addMClusters_);
      fastSparqlService.getGraphDBInstance().runSplitAddOp(connection, delMClusters, delMClusters_);

      //Apply Marmotta
//      fastSparqlService.getGraphDBInstance().runSplitDelOp(connection2, delMRedi, redGrp);
//      fastSparqlService.getGraphDBInstance().runSplitAddOp(connection2, addMRedi, redGrp);
//      fastSparqlService.getGraphDBInstance().runSplitDelOp(connection2, delMClusters, clsGrp);
//      fastSparqlService.getGraphDBInstance().runSplitAddOp(connection2, addMClusters, clsGrp);

      //Apply GraphDB
      fastSparqlService.getGraphDBInstance().runSplitDelOp(connection, delMRedi, redGrp);
      fastSparqlService.getGraphDBInstance().runSplitAddOp(connection, addMRedi, redGrp);
      fastSparqlService.getGraphDBInstance().runSplitDelOp(connection, delMClusters, clsGrp);
      fastSparqlService.getGraphDBInstance().runSplitAddOp(connection, addMClusters, clsGrp);

      connection.commit();
      connection2.commit();

      connection.close();
      connection2.close();

      //Update indexes
      for (URI authURI : updateAuthors) {
        loadService.authors(authURI.stringValue());
      }
      for (String pubURI : updateIndexResources) {
        solrIndexingService.indexResource(ValueFactoryImpl.getInstance().createURI(pubURI));
      }
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }

  }

  @Override
  public void instbyProj() {
      final Task task = taskManagerService.createSubTask("Caching related inst by projects", "Mongo Service");
    try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
      MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());
      // Delete and create collection
      final MongoCollection<Document> collection = db.getCollection(MongoService.Collection.INSTBYPROJECT.getValue());
      collection.drop();
      String data  = commonService.getProjectbyInstInfo();
      Document parse = Document.parse(data);
      parse.append("_id", "All");
      collection.insertOne(parse);
      
      
      
    }
    
  }

  

  class SynchronizedParse {

    private Document queryparse = new Document();

    // Synchronized Method 
    public synchronized void appendParse(Document d, String q) {
      queryparse.append(q, d);
    }

    public Document getDoc() {
      return queryparse;
    }
  }
}
