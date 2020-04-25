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

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;
import edu.ucuenca.storage.api.MongoService;
import edu.ucuenca.storage.exceptions.FailMongoConnectionException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;

;

/**
 * Default Implementation of {@link MongoService}
 */
@ApplicationScoped
public class MongoServiceImpl implements MongoService {

    @Inject
    private Logger log;
    @Inject
    private ConfigurationService configurationService;
    @Inject
    private ExternalSPARQLService sesameService2;
    private MongoClient mongoClient;
    private MongoDatabase db;
    private MongoCollection<Document> authors;
    private MongoCollection<Document> statistics;
    private MongoCollection<Document> statisticsByInst;
    private MongoCollection<Document> statisticsByAuthor;
    private MongoCollection<Document> relatedauthors;
    private MongoCollection<Document> clusters;
    private MongoCollection<Document> clustersTotals;
    private MongoCollection<Document> authorsByArea;
    private MongoCollection<Document> authorsByDisc;
    private MongoCollection<Document> countries;
    private MongoCollection<Document> sparqls;
    private MongoCollection<Document> authors_val;

    private MongoCollection<Document> sessions;

    @PostConstruct
    public void initialize() throws FailMongoConnectionException {
        connect();
    }

    @Override
    public void connect() throws FailMongoConnectionException {
        String host = configurationService.getStringConfiguration("mongo.host");
        int port = configurationService.getIntConfiguration("mongo.port");

        mongoClient = new MongoClient(host, port);
        db = mongoClient.getDatabase(Database.NAME.getDBName());
        authors = db.getCollection(Collection.AUTHORS.getValue());
        relatedauthors = db.getCollection(Collection.RELATEDAUTHORS.getValue());
        statistics = db.getCollection(Collection.STATISTICS.getValue());
        statisticsByInst = db.getCollection(Collection.STATISTICS_INST.getValue());
        clusters = db.getCollection(Collection.CLUSTERS.getValue());
        clustersTotals = db.getCollection(Collection.CLUSTERSTOTALS.getValue());
        authorsByArea = db.getCollection(Collection.AUTHORS_AREA.getValue());
        authorsByDisc = db.getCollection(Collection.AUTHORS_DISCPLINE.getValue());
        countries = db.getCollection(Collection.COUNTRIES.getValue());
        sparqls = db.getCollection(Collection.SPARQLS.getValue());
        statisticsByAuthor = db.getCollection(Collection.STATISTICS_AUTHOR.getValue());
        authors_val = db.getCollection(Collection.PROFILE_AUTHOR.getValue());
        sessions = db.getCollection(Collection.SESSIONS.getValue());

    }

    @Override
    public String getAuthor(String uri) {
        return authors.find(eq("_id", uri))
                .first()
                .toJson();
    }

    @Override
    public String getStatistics(String id) {
        return statistics.find(eq("_id", id))
                .first().toJson();
    }

    @Override
    public String getStatisticsByInst(String id) {
        return statisticsByInst.find(eq("_id", id)).first().toJson();

    }

    @Override
    public String getStatisticsByAuthor(String id) {
        return statisticsByAuthor.find(eq("_id", id)).first().toJson();

    }

    @Override
    public String getRelatedAuthors(String uri) {
        return relatedauthors.find(eq("_id", uri))
                .first()
                .toJson();
    }

    @Override
    public Document getCluster(String... uri) {
        List<Bson> ls = new ArrayList<>();
        List<Document> c = new ArrayList<>();
        for (String p : uri) {
            Bson eq = eq("_id", p);
            ls.add(eq);
        }
        FindIterable<Document> sort = clusters.find(or(ls))
                .projection(include("subclusters"))
                .sort(ascending("label-en"));
        MongoCursor<Document> it = sort.iterator();
        while (it.hasNext()) {
            c.add(it.next());
        }
        Document parse = new Document();

        if (c.size() == 1) {
            parse = c.get(0);
        } else {
            parse.put("data", c);
        }
        return parse;

    }

    @Override
    public Document getProfileValAuthor(String id) {
        return authors_val.find(eq("_id", id)).first();

    }

    @Override
    public Document removeProfileValAuthor(String id) {
        return authors_val.findOneAndDelete(eq("_id", id));

    }

    @Override
    public List<Document> getClusters() {
        List<Document> c = new ArrayList<>();
        FindIterable<Document> cls = clusters.find()
                .projection(exclude("subclusters"))
                .sort(ascending("label-en", "label-es"));
        MongoCursor<Document> it = cls.iterator();
        while (it.hasNext()) {
            c.add(it.next());
        }
        return c;
    }

    @Override
    public List<Document> getCountries() {
        List<Document> c = new ArrayList<>();
        FindIterable<Document> cls = countries.find();
        MongoCursor<Document> it = cls.iterator();
        while (it.hasNext()) {
            c.add(it.next());
        }
        return c;
    }

    @Override
    public String getAuthorsByArea(String cluster, String subcluster) {
        BasicDBObject key = new BasicDBObject();
        key.put("cluster", cluster);
        key.put("subcluster", subcluster);
        return authorsByArea.find(eq("_id", key))
                .first().toJson();
    }

    @Override
    public String getAuthorsByDiscipline(String cluster) {
        BasicDBObject key = new BasicDBObject();
        key.put("cluster", cluster);
        return authorsByDisc.find(eq("_id", key))
                .first().toJson();
    }

    @PreDestroy
    public void shutdown() {
        log.info("Killing connection to MongoDB.");
        mongoClient.close();
    }

    @Override
    public List<Document> getClustersTotals() {
        List<Document> c = new ArrayList<>();
        MongoCursor<Document> it = clustersTotals.find().iterator();
        while (it.hasNext()) {
            c.add(it.next());
        }
        return c;
    }

    @Override
    public List<Document> getSubClustersTotals(String uri) {
        Document first = clustersTotals.find(eq("_id", uri)).first();
        return (List<Document>) first.get("subclusters");
    }

    @Override
    public String getSPARQL(String qry, String f) {
        String k = getMd5(f + qry);
        String R = "";
        MongoCursor<Document> find = sparqls.find(eq("_id", k)).iterator();
        if (!find.hasNext()) {
            try {
                RepositoryConnection conn = sesameService2.getRepositoryConnetion();
                StringWriter writter = new StringWriter();
                RDFFormat extfrmt = RDFFormat.JSONLD;
                if (f.contains("application/rdf+json")){
                    extfrmt = RDFFormat.RDFJSON;
                }
                RDFWriter jsonldWritter = Rio.createWriter(extfrmt, writter);
                conn.prepareGraphQuery(QueryLanguage.SPARQL, qry).evaluate(jsonldWritter);
//                Model mm = new LinkedHashModel();
//                while (evaluate.hasNext()){
//                    mm.add(evaluate.next());
//                }
//                Rio.write(mm, jsonldWritter);
                //Object compact = JsonLdProcessor.compact(JsonUtils.fromString(writter.toString()), new HashMap(), new JsonLdOptions());
                Map<String, Object> json = new HashMap<String, Object>();
                json.put("_id", k);
                json.put("data", writter.toString());
                sparqls.insertOne(new Document(json));
                conn.close();
                writter.getBuffer().setLength(0);
                find = sparqls.find(eq("_id", k)).iterator();
            } catch (Exception ex) {
                ex.printStackTrace();
                log.debug("Unexpected error cached-query {}", ex);
            }
        }
        Document next = find.next();
        R = next.getString("data");
        return R;
    }

    private String getMd5(String input) {
        try {
            // Static getInstance method is called with hashing MD5 
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest 
            //  of an input digest() return array of byte 
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation 
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value 
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } // For specifying wrong message digest algorithms 
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerSession(String orcid, String token) {
        BasicDBObject key = new BasicDBObject();
        key.put("orcid", orcid);
        key.put("token", token);
        BasicDBObject main = new BasicDBObject();
        main.append("_id", key);
        Document parse = Document.parse(main.toJson());
        sessions.insertOne(parse);
    }

    @Override
    public boolean checkSession(String orcid, String token) {
        BasicDBObject key = new BasicDBObject();
        key.put("orcid", orcid);
        key.put("token", token);
        return sessions.find(eq("_id", key)).iterator().hasNext();
    }

    @Override
    public JSONObject obtainNewProfiles(String org) throws Exception {
        BasicDBObject key = new BasicDBObject();
        key.put("profile.org", org);
        BasicDBObject pro = new BasicDBObject();
        pro.put("profile", 1);
        JSONObject main = new JSONObject();
        JSONArray array = new JSONArray();
        main.put("data", array);
        JSONParser parser = new JSONParser();
        for (Document document : authors_val.find(key).projection(pro)) {
            JSONObject parse = (JSONObject) parser.parse(document.toJson());
            array.add(parse);
        }
        return main;
    }
}
