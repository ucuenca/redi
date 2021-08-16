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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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
import org.json.simple.parser.ParseException;
import org.openrdf.model.Value;
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
  private MongoCollection<Document> documentbyarea;
  private MongoCollection<Document> statisticsByAuthor;
  private MongoCollection<Document> relatedauthors;
  private MongoCollection<Document> clusters;
  private MongoCollection<Document> clustersTotals;
  private MongoCollection<Document> authorsByArea;
  private MongoCollection<Document> pubsByArea;
  private MongoCollection<Document> pubsBySubArea;
  private MongoCollection<Document> authorsByDisc;
  private MongoCollection<Document> countries;
  private MongoCollection<Document> sparqls;
  private MongoCollection<Document> authors_val;
  private MongoCollection<Document> projects;
  private MongoCollection<Document> instbyProject;
  private MongoCollection<Document> patent;
  private MongoCollection<Document> pubtrasnlate;
  private MongoCollection<Document> subjecttranslation;
  private MongoCollection<Document> institution;
  private MongoCollection<Document> indicators;

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
    pubsByArea = db.getCollection(Collection.DOCUMENTDATEBYAREA.getValue());
    pubsBySubArea = db.getCollection(Collection.DOCUMENTDATEBYSUBAREA.getValue());
    authorsByDisc = db.getCollection(Collection.AUTHORS_DISCPLINE.getValue());
    countries = db.getCollection(Collection.COUNTRIES.getValue());
    sparqls = db.getCollection(Collection.SPARQLS.getValue());
    statisticsByAuthor = db.getCollection(Collection.STATISTICS_AUTHOR.getValue());
    authors_val = db.getCollection(Collection.PROFILE_AUTHOR.getValue());
    projects = db.getCollection(Collection.PROJECTPROFILE.getValue());
    institution = db.getCollection(Collection.PROFILE_INST.getValue());
    sessions = db.getCollection(Collection.SESSIONS.getValue());
    instbyProject = db.getCollection(Collection.INSTBYPROJECT.getValue());
    documentbyarea = db.getCollection(Collection.DOCUMENTBYAREA.getValue());
    patent = db.getCollection(Collection.PATENTPROFILE.getValue());
    pubtrasnlate = db.getCollection(Collection.PUBTRANSLATIONS.getValue());
    subjecttranslation = db.getCollection(Collection.SUBJECTRANSLATION.getValue());
    indicators = db.getCollection(Collection.INDICATORS.getValue());
  }

  @Override
  public String getAuthor(String uri) {
    return authors.find(eq("_id", uri))
            .first()
            .toJson();
  }

  @Override
  public Document getProfileValAuthor(String id) {
    return authors_val.find(eq("_id", id)).first();

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
  public Document getProfileProject(String id) {
    return projects.find(eq("_id", id)).first();

  }

  @Override
  public Document getProfileInst(String id) {
    return institution.find(eq("_id", id)).first();

  }

  @Override
  public Document getProfilePatent(String id) {
    return patent.find(eq("_id", id)).first();

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
  public List<Document> getPubBySubAreaDate(String cluster, String subcluster) {

    BasicDBObject key = new BasicDBObject();
    if (!cluster.isEmpty() && subcluster.isEmpty()) {

      key.put("area", cluster);

    } else if (!cluster.isEmpty() && !subcluster.isEmpty()) {
      key.put("_id", cluster + "|" + subcluster);
    }
    List<Document> c = new ArrayList<>();
    MongoCursor<Document> it = pubsBySubArea.find(key).iterator();
    while (it.hasNext()) {
      c.add(it.next());
    }
    return c;
  }

  @Override
  public List<Document> getPubByAreaDate(String cluster) {
    List<Document> c = new ArrayList<>();
    MongoCursor<Document> it;
    BasicDBObject key = new BasicDBObject();
    if (!cluster.isEmpty()) {

      key.put("area", cluster);
      it = pubsByArea.find(key).iterator();

    } else {

      it = pubsByArea.find().iterator();
    }

    while (it.hasNext()) {
      c.add(it.next());
    }
    return c;
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
        if (f.contains("application/rdf+json")) {
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

  @Override
  public Document getinstbyProject(String id) {
    return instbyProject.find(eq("_id", id)).first();
  }

  @Override
  public String getStatisticsByArea(String id) {
    return documentbyarea.find(eq("_id", id)).first().toJson();

  }

  @Override
  public boolean checkPublicationTrasnlate(String id) {
    return pubtrasnlate.count(eq("_id", id)) > 0;

    //return pubtrasnlate.find (eq("_id", id)).
  }

  @Override
  public Document getSubjectTr(String id) {

    return subjecttranslation.find(eq("_id", id)).first();
  }

  @Override
  public String getGlobalAuthorMetrics(String string, String group) {
    String query = "";
    switch (string) {
      case "publicationsPerArea":
        query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "select ?c ?authorURI (?authorURI as ?g) (sample(?l) as ?k) (count(distinct ?publicationURI ) as ?v) {\n"
                + "    graph <https://redi.cedia.edu.ec/context/redi> {\n"
                + (group != null ? "        bind (<" + group + "> as ?authorURI ).\n" : "")
                + "        ?authorURI foaf:publications ?publicationURI .\n"
                + "        \n"
                + "    }\n"
                + "    graph <https://redi.cedia.edu.ec/context/clustersPub> {\n"
                + "       ?publicationURI  <http://purl.org/dc/terms/isPartOf> ?c .\n"
                + "       ?c rdfs:label ?l .\n"
                + "       ?c a <http://ucuenca.edu.ec/ontology#SubCluster> .\n"
                + "       filter (lang(?l) = 'es') .\n"
                + "    }\n"
                + "} group by ?c ?authorURI order by desc(?v)";
        break;
      case "publicationsPerSource":
        query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX dct: <http://purl.org/dc/terms/>\n"
                + "select ?pro ?authorURI (?authorURI as ?g) (?pro as ?k) (count(distinct ?publicationURI ) as ?v) {\n"
                + "    graph <https://redi.cedia.edu.ec/context/redi> {\n"
                + (group != null ? "        bind (<" + group + "> as ?authorURI ).\n" : "")
                + "        ?authorURI foaf:publications ?publicationURI .\n"
                + "        \n"
                + "    }\n"
                + "    graph <https://redi.cedia.edu.ec/context/redi> {\n"
                + "       ?publicationURI dct:provenance ?pro .\n"
                + "    }\n"
                + "} group by ?pro ?authorURI order by desc(?v)";
        break;
      case "publicationsPerCoauthor":
        query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX dct: <http://purl.org/dc/terms/>\n"
                + "select ?coauthor ?authorURI (sample(?name) as ?g) (sample(?coathorName) as ?k) (count(distinct ?publicationURI ) as ?v) {\n"
                + "    graph <https://redi.cedia.edu.ec/context/redi> {\n"
                + (group != null ? "        bind (<" + group + "> as ?authorURI ).\n" : "")
                + "        ?authorURI foaf:publications ?publicationURI .\n"
                + "        ?authorURI foaf:name ?name .\n"
                + "        \n"
                + "    }\n"
                + "    graph <https://redi.cedia.edu.ec/context/redi> {\n"
                + "       ?coauthor foaf:publications ?publicationURI .\n"
                + "       ?coauthor foaf:name ?coathorName .\n"
                + "       filter ( ?coauthor != ?authorURI ) .\n"
                + "    }\n"
                + "} group by ?coauthor ?authorURI order by desc(?v)";
        break;
      case "publicationsPerYear":
        query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX dct: <http://purl.org/dc/terms/>\n"
                + "PREFIX bibo: <http://purl.org/ontology/bibo/>\n"
                + "select ?y ?authorURI (?authorURI as ?g) (?y as ?k) (count(distinct ?publicationURI ) as ?v) {\n"
                + "    graph <https://redi.cedia.edu.ec/context/redi> {\n"
                + (group != null ? "        bind (<" + group + "> as ?authorURI ).\n" : "")
                + "        ?authorURI foaf:publications ?publicationURI .\n"
                + "        \n"
                + "    }\n"
                + "    graph <https://redi.cedia.edu.ec/context/redi> {\n"
                + "        ?publicationURI bibo:created ?yx .\n"
                + "        BIND (str(?yx) as ?y2) .\n"
                + "        bind( strbefore( ?y2, '-' ) as ?y3 ).\n"
                + "        bind( strafter( ?y2, ' ' ) as ?y4 ).\n"
                + "        bind( if (str(?y3)='' && str(?y4)='',?y2, if(str(?y3)='',strafter( ?y2, ' ' ),strbefore( ?y2, '-' ))) as ?y ) .\n"
                + "    }\n"
                + "} group by ?y ?authorURI order by asc(?k)";
        break;
      case "globalCounters":
        query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX dct: <http://purl.org/dc/terms/>\n"
                + "PREFIX bibo: <http://purl.org/ontology/bibo/>\n"
                + "select (?authorURI as ?g) (count(distinct ?pub) as ?cpub) (count(distinct ?pro) as ?cpro) (count(distinct ?coauthorURI) as ?ccau) (max(?v) as ?hindex) {\n"
                + "    graph <https://redi.cedia.edu.ec/context/redi> {\n"
                + (group != null ? "        bind (<" + group + "> as ?authorURI ).\n" : "")
                + "        optional {\n"
                + "            ?authorURI foaf:publications ?pub .\n"
                + "            optional {\n"
                + "        	 	?coauthorURI foaf:publications ?pub .\n"
                + "                filter (?authorURI != ?coauthorURI) .\n"
                + "            }\n"
                + "        }\n"
                + "        optional {\n"
                + "        	?authorURI <http://eurocris.org/ontology/cerif#MemberOf> ?pro .    \n"
                + "        }\n"
                + "        optional {\n"
                + "            ?authorURI <http://ucuenca.edu.ec/ontology#h-index> ?v .\n"
                + "        }\n"
                + "    }\n"
                + "} group by ?authorURI";
        break;
      case "journalImpact":
        query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX dct: <http://purl.org/dc/terms/>\n"
                + "PREFIX bibo: <http://purl.org/ontology/bibo/>\n"
                + "select (?authorURI as ?g) (?bq as ?k) (count(distinct ?pub) as ?v) {\n"
                + "    {\n"
                + "        select ?authorURI ?pub (max(?qu) as ?bq) {\n"
                + "            graph <https://redi.cedia.edu.ec/context/redi> {\n"
                + (group != null ? "        bind (<" + group + "> as ?authorURI ).\n" : "")
                + "                ?authorURI foaf:publications ?pub .\n"
                + "                ?pub dct:isPartOf ?jou .\n"
                + "                ?jou <http://ucuenca.edu.ec/ontology#bestQuartile> ?qu .\n"
                + "            }\n"
                + "        } group by ?authorURI ?pub  \n"
                + "    } .\n"
                + "} group by ?authorURI ?bq";
        break;
      case "journalCountry":
        query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX dct: <http://purl.org/dc/terms/>\n"
                + "PREFIX bibo: <http://purl.org/ontology/bibo/>\n"
                + "select (?authorURI as ?g) (?bq as ?k) (count(distinct ?pub) as ?v) {\n"
                + "    {\n"
                + "        select ?authorURI ?pub (max(?qu) as ?bq) {\n"
                + "            graph <https://redi.cedia.edu.ec/context/redi> {\n"
                + (group != null ? "        bind (<" + group + "> as ?authorURI ).\n" : "")
                + "                ?authorURI foaf:publications ?pub .\n"
                + "                ?pub dct:isPartOf ?jou .\n"
                + "                ?jou <http://ucuenca.edu.ec/ontology#country> ?qu .\n"
                + "            }\n"
                + "        } group by ?authorURI ?pub  \n"
                + "    } .\n"
                + "} group by ?authorURI ?bq";
          break;
      case "AuthorsTotal" : 
        query = "PREFIX dct: <http://purl.org/dc/terms/>\n" +
                "PREFIX eurocris: <http://eurocris.org/ontology/cerif#>\n" +
                "select ( COUNT (distinct ?e) as ?total) ?name where { \n" +
                "    graph <https://redi.cedia.edu.ec/context/redi>{ \n" +
                "	  ?p a ?name . \n" +
                "      ?p dct:creator|dct:contributor|eurocris:linksToPerson|eurocris:linkToPerson  ?e  .\n" +
                "      ?e <http://schema.org/memberOf> ?pro .   \n" +
                "      ?pro <http://ucuenca.edu.ec/ontology#memberOf> ?b . \n" +
                "    }\n" +
                "} group by ?name" ;
        
        break;
    }

    JSONObject data = new JSONObject();
    JSONArray array = new JSONArray();
    data.put("data", array);
    try {
      List<Map<String, Value>> query1 = sesameService2.getSparqlService().query(QueryLanguage.SPARQL, query);
      for (Map<String, Value> mp : query1) {
        JSONObject datakv = new JSONObject();
        for (Iterator<Map.Entry<String, Value>> it = mp.entrySet().iterator(); it.hasNext();) {
          Map.Entry<String, Value> next = it.next();
          datakv.put(next.getKey(), next.getValue().stringValue());
        }
        array.add(datakv);
      }
    } catch (Exception ex) {
      log.info("Error {}", ex);
    }
    return data.toJSONString();
  }
  
  @Override
  public String getGlobalIndicators(String indname) { 
      return indicators.find(eq("_id", indname))
            .first().toJson();
  }
  
  /*@Override
  public String getGlobalPublicationMetrics(String string, String group) {
    String query = "";
    switch (string) {
      case "pubByYear":
        query = "PREFIX schema: <http://schema.org/>\n" +
          "PREFIX bibo: <http://purl.org/ontology/bibo/>\n" +
          "PREFIX nature: <http://ns.nature.com/terms/>\n" +
          "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
          "select (count ( distinct ?pub ) as ?total) ?y where { \n" +
          "  graph <https://redi.cedia.edu.ec/context/redi>{ \n" +
          "    ?pub schema:copyrightYear|nature:coverDate  ?yx .\n" +
          "    ?pub a <http://purl.org/ontology/bibo/AcademicArticle> .\n" +
          "    ?authors foaf:publications ?pub .\n" +
          "    BIND (str(?yx) as ?y2) .\n" +
          "    bind( strbefore( ?y2, '-' ) as ?y3 ).  \n" +
          "    bind( strafter( ?y2, ' ' ) as ?y4 ).   \n" +
          "    bind( if (str(?y3)='' && str(?y4)='',?y2, if(str(?y3)='',strafter( ?y2, ' ' ),strbefore( ?y2, '-' ))) as ?y ) . FILTER regex(?y, '^[0-9]*$')    \n" +
          "                    \n" +
          "    }\n" +
          "} group by ?y order by ASC ( ?y )";
        break;
      case "quartilPub":
        query = "PREFIX dct: <http://purl.org/dc/terms/>\n" +
              "select   (count ( distinct ?pub ) as ?total)  ?qu where { \n" +
              "            ?pub dct:isPartOf ?jou .\n" +
              "           ?jou <http://ucuenca.edu.ec/ontology#bestQuartile> ?qu \n" +
              "} group by ?qu ";
        break;
      case "volPub" :
                    query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
           "select distinct ?authors    (count ( distinct ?pub ) as ?total) \n" +
           "FROM <https://redi.cedia.edu.ec/context/redi> where { \n" +
           "	\n" +
           "    ?pub a <http://purl.org/ontology/bibo/AcademicArticle> .\n" +
           "    ?authors foaf:publications ?pub .\n" +
           "    ?authors a <http://xmlns.com/foaf/0.1/Person> .\n" +
           "    ?authors  <http://schema.org/memberOf> ?org .\n" +
           "    ?org <http://ucuenca.edu.ec/ontology#memberOf> ?c\n" +
           "} group by ?authors ";
                 break; 
      case "topJournals" : query = "PREFIX dct: <http://purl.org/dc/terms/>\n" +
          "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
          "PREFIX bibo: <http://purl.org/ontology/bibo/>\n" +
          "\n" +
          "select   (count ( distinct ?pub ) as ?total)  ?jou  ( SAMPLE (?lb) as ?name ) " +
          "FROM <https://redi.cedia.edu.ec/context/redi> where { \n" +
          "            ?pub dct:isPartOf ?jou .\n" +
          "    		?jou a bibo:Journal .\n" +
          "    		?jou rdfs:label ?lb\n" +
          "          \n" +
          "} group by ?jou   order by DESC( ?total ) limit 50";
                break;
      case  "topAreas" : query = "PREFIX dct: <http://purl.org/dc/terms/>\n" +
          "PREFIX bibo: <http://purl.org/ontology/bibo/>\n" +
          "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
          "PREFIX uc: <http://ucuenca.edu.ec/wkhuska/resource/>\n" +
          "PREFIX uco: <http://ucuenca.edu.ec/ontology#>\n" +
          "select   (count ( distinct ?pub ) as ?total)  ?jou  ( SAMPLE (?lb) as ?name ) \n" +
          "FROM <https://redi.cedia.edu.ec/context/redi>\n" +
          "FROM <https://redi.cedia.edu.ec/context/clustersPub>  where { \n" +
          "            ?pub dct:isPartOf ?jou .\n" +
          "    		?jou a uco:Cluster .\n" +
          "    		?jou rdfs:label ?lb\n" +
          "          \n" +
          "} group by ?jou   order by DESC( ?total ) limit 100";
          break;
          
      case  "overlapProviders" : 
        query = "PREFIX dct: <http://purl.org/dc/terms/>\n" +
      "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
      "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
      "PREFIX uco: <http://ucuenca.edu.ec/ontology#>\n" +
      "select  (COUNT  (distinct ?pub) as ?total) ?prov ?name\n" +
      "FROM <https://redi.cedia.edu.ec/context/redi>\n" +
      "FROM <https://redi.cedia.edu.ec/context/redi>\n" +
      "where {\n" +
      "    {\n" +
      "    ?pub a <http://purl.org/ontology/bibo/AcademicArticle> .\n" +
      "    ?pub dct:provenance ?prov .\n" +
      "    ?authors foaf:publications ?pub .\n" +
      "    ?authors a <http://xmlns.com/foaf/0.1/Person> .\n" +
      "    ?authors  <http://schema.org/memberOf> ?org .\n" +
      "    ?org <http://ucuenca.edu.ec/ontology#memberOf> ?c .\n" +
      "    } UNION {\n" +
      "    ?pub dct:provenance   uco:ScopusProvider .\n" +
      "    ?pub dct:provenance   uco:GoogleScholarProvider .\n" +
      "    BIND(CONCAT(STR( uco:ScopusProvider ),\",\" , STR( uco:GoogleScholarProvider )) AS ?name )   .\n" +
      "    ?authors foaf:publications ?pub .\n" +
      "    } UNION {\n" +
      "    ?pub dct:provenance   uco:ScopusProvider .\n" +
      "    ?pub dct:provenance   uco:AcademicsKnowledgeProvider .\n" +
      "    BIND(CONCAT(STR( uco:ScopusProvider ),\",\" , STR( uco:AcademicsKnowledgeProvider )) AS ?name ) .\n" +
      "    ?authors foaf:publications ?pub .\n" +
      "    }UNION {\n" +
      "    ?pub dct:provenance   uco:AcademicsKnowledgeProvider .\n" +
      "    ?pub dct:provenance   uco:GoogleScholarProvider .\n" +
      "    BIND(CONCAT(STR( uco:AcademicsKnowledgeProvider ),\",\" , STR( uco:GoogleScholarProvider )) AS ?name ) .\n" +
      "    ?authors foaf:publications ?pub .\n" +
      "    }UNION {\n" +
      "    ?pub dct:provenance   uco:AcademicsKnowledgeProvider .\n" +
      "    ?pub dct:provenance   uco:GoogleScholarProvider .\n" +
      "    ?pub dct:provenance   uco:ScopusProvider .\n" +
      "     BIND(CONCAT(STR( uco:AcademicsKnowledgeProvider ),\",\" , STR( uco:GoogleScholarProvider ) , STR( uco:ScopusProvider ) ) AS ?name ) .\n" +
      "    ?authors foaf:publications ?pub .\n" +
      "    }\n" +
      "    \n" +
      "} group by ?prov ?name order by DESC ( ?total )";
        break;
        
      case "typePub" : query =  "PREFIX bibo: <http://purl.org/ontology/bibo/>\n" +
        "PREFIX dct: <http://purl.org/dc/terms/>\n" +
        "select (COUNT (Distinct ?pub) as ?total )  ?name\n" +
        "FROM <https://redi.cedia.edu.ec/context/redi>\n" +
        "where { \n" +
        "     ?pub a <http://purl.org/ontology/bibo/AcademicArticle> .\n" +
        "     ?pub dct:isPartOf ?jou .\n" +
        "     ?jou a ?name\n" +
        "} group by ?name" ;
       break;
    }
    
    JSONObject data = new JSONObject();
    JSONArray array = new JSONArray();
    data.put("data", array);
    try {
      List<Map<String, Value>> query1 = sesameService2.getSparqlService().query(QueryLanguage.SPARQL, query);
      for (Map<String, Value> mp : query1) {
        JSONObject datakv = new JSONObject();
        for (Iterator<Map.Entry<String, Value>> it = mp.entrySet().iterator(); it.hasNext();) {
          Map.Entry<String, Value> next = it.next();
          datakv.put(next.getKey(), next.getValue().stringValue());
        }
        array.add(datakv);
      }
    } catch (Exception ex) {
      log.info("Error {}", ex);
    }
    return data.toJSONString();
  }*/
}
