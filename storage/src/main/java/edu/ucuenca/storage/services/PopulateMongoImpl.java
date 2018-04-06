/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.storage.services;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import edu.ucuenca.storage.api.MongoService;
import edu.ucuenca.storage.api.PopulateMongo;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.CommonService;
import org.bson.Document;
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
    private SesameService sesameService;
    @Inject
    private QueriesService queriesService;
    @Inject
    private SparqlService sparqlService;
    @Inject
    private Logger log;
    @Inject
    private CommonService commonService;
    @Inject
    private TaskManagerService taskManagerService;

    private static final Map context = new HashMap();

    static {
        context.put("dct", "http://purl.org/dc/terms/");
        context.put("owl", "http://www.w3.org/2002/07/owl#");
        context.put("foaf", "http://xmlns.com/foaf/0.1/");
        context.put("uc", "http://ucuenca.edu.ec/ontology#");
        context.put("bibo", "http://purl.org/ontology/bibo/");
        context.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    }

    /**
     *
     * @param queryResources query to load resources to describe.
     * @param queryDescribe  query to describe each candidate; it has to be a
     *                       describe/construct.
     * @param collection     collection name in Mongo db.
     */
    private void loadResources(String queryResources, String queryDescribe, String c) {
        try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));
                StringWriter writter = new StringWriter();) {
            RepositoryConnection conn = sesameService.getConnection();

            int num_candidates = 0;
            try {
                MongoDatabase db = client.getDatabase(MongoService.DATABASE);
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
            RepositoryConnection conn = sesameService.getConnection();

            try {
                MongoDatabase db = client.getDatabase(MongoService.DATABASE);
                // Delete and create collection
                MongoCollection<Document> collection = db.getCollection(c);
                collection.drop();

                RDFWriter jsonldWritter = Rio.createWriter(RDFFormat.JSONLD, writter);
                for (String key : queries.keySet()) {
                    conn.prepareGraphQuery(QueryLanguage.SPARQL, queries.get(key))
                            .evaluate(jsonldWritter);
                    Object compact = JsonLdProcessor.compact(JsonUtils.fromString(writter.toString()), context, new JsonLdOptions());
                    Map<String, Object> json = (Map<String, Object>) compact;
                    json.put("_id", key);
                    collection.insertOne(new Document(json));
                    writter.getBuffer().setLength(0);
                    log.info("Load aggregation into {} collection for id {}", c, key);
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
        Task task = taskManagerService.createSubTask("Caching authors profiles", "Mongo Service");
        try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
            MongoDatabase db = client.getDatabase(MongoService.DATABASE);
            // Delete and create collection
            MongoCollection<Document> collection = db.getCollection(MongoService.Collection.AUTHORS.getValue());
            collection.drop();
            List<Map<String, Value>> authorsRedi = sparqlService.query(QueryLanguage.SPARQL, queriesService.getAuthorsCentralGraph());
            task.updateTotalSteps(authorsRedi.size());
            for (int i = 0; i < authorsRedi.size(); i++) {
                String author = authorsRedi.get(i).get("a").stringValue();
                // Print progress
                log.info("Relating {} ", author);
                log.info("Relating {}/{}. Author: '{}' ", i, authorsRedi.size(), author);
                task.updateDetailMessage("URI", author);
                task.updateProgress(i);
                // Get and store author data (json) from SPARQL repository.
                String profiledata = commonService.getAuthorDataProfile(author);
                Document parse = Document.parse(profiledata);
                parse.append("_id", author);
                collection.insertOne(parse);
            }
        } catch (MarmottaException ex) {
            log.error(ex.getMessage(), ex);
        }
        taskManagerService.endTask(task);
    }

    @Override
    public void statistics() {
        HashMap<String, String> queries = new HashMap<>();
        queries.put("barchar", queriesService.getBarcharDataQuery());
        queries.put("count_authors", queriesService.getAggreggationAuthors());
        queries.put("count_publications", queriesService.getAggregationPublications());
        queries.put("count_research_areas", queriesService.getAggregationAreas());
        queries.put("keywords_frequencypub_gt4", queriesService.getKeywordsFrequencyPub());
        loadStadistics(MongoService.Collection.STATISTICS.getValue(), queries);
    }

    @Override
    public void networks() {
        Task task = taskManagerService.createSubTask("Caching related authors", "Mongo Service");
        try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {

            MongoDatabase db = client.getDatabase(MongoService.DATABASE);
            // Delete and create collection
            MongoCollection<Document> collection = db.getCollection(MongoService.Collection.RELATEDAUTHORS.getValue());
            collection.drop();
            task.updateMessage("Calculating related authors");
            List<Map<String, Value>> query = sparqlService.query(QueryLanguage.SPARQL, queriesService.getAuthorsCentralGraph());
            int i = 0;
            for (Map<String, Value> mp : query) {
                i++;
                String stringValue = mp.get("a").stringValue();
                log.info("Relating {} ", stringValue);
                log.info("Relating {}/{} ", i, query.size());
                task.updateDetailMessage("URI", stringValue);
                task.updateDetailMessage("Status", i + "/" + query.size());
                String collaboratorsData = commonService.getCollaboratorsData(stringValue);
                Document parse = Document.parse(collaboratorsData);
                parse.append("_id", stringValue);
                collection.insertOne(parse);
            }
        } catch (Exception w) {
            log.debug(w.getMessage(), w);
        }
        taskManagerService.endTask(task);
    }

    @Override
    public void publications() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
