/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.storage.services;

import at.newmedialab.lmf.search.api.indexing.SolrIndexingService;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import edu.ucuenca.storage.api.MongoService;
import edu.ucuenca.storage.api.PopulateMongo;
import javax.enterprise.context.ApplicationScoped;
import edu.ucuenca.storage.api.ProfileValidation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.vocabulary.FOAF;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.email.EmailPopulatingBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;
import org.slf4j.Logger;

/**
 *
 * @author joe
 */
@ApplicationScoped
public class ProfileValidationImpl implements ProfileValidation {

  @Inject
  private Logger log;

  @Inject
  private ConfigurationService conf;

  @Inject
  private SparqlService sparqlService;

  @Inject
  private ExternalSPARQLService fastSparqlService;

  @Inject
  private ConstantService con;

  @Inject
  private MongoService mongos;

  @Inject
  private ConstantService constantService;

  @Inject
  private SolrIndexingService solrIndexingService;

  @Inject
  private SesameService sesameService;

  @Inject
  private PopulateMongo loadService;

  @Override
  public JSONObject getProfileInst(String uri) {
    String querymail = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "SELECT distinct ?org ?orgname ?img WHERE {\n"
            + "  graph <" + con.getCentralGraph() + ">{\n"
            + "   <" + uri + ">   <http://schema.org/memberOf>   ?org .\n"
            + "    graph <" + con.getOrganizationsGraph() + "> {\n"
            + "      ?org a [] .\n"
            + "      ?org <http://ucuenca.edu.ec/ontology#name> ?orgname\n"
            + "    }\n"
            + "   OPTIONAL {\n"
            + "    <" + uri + ">   foaf:img  ?img\n"
            + "   }\n"
            + "  }\n"
            + "}";
    try {
      List<Map<String, Value>> qmail = sparqlService.query(QueryLanguage.SPARQL, querymail);
      JSONObject main = new JSONObject();
      JSONArray array = new JSONArray();
      for (Map<String, Value> p : qmail) {

        JSONObject obj = new JSONObject();
        if (p.containsKey("org")) {
          obj.put("org", p.get("org").stringValue());
          obj.put("orgName", p.get("orgname").stringValue());
        }
        if (p.containsKey("img")) {
          obj.put("img", p.get("img").stringValue());
        }
        array.add(obj);
      }
      main.put("data", array);
      return main;

    } catch (MarmottaException ex) {
      log.debug(ex.getMessage());
    }

    return null;

  }

  @Override
  public JSONObject getProfileCandidates(String uri, HashMap<String, Boolean> table) {
    String queryprofile = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"
            + "SELECT ?object (group_concat(DISTINCT ?name ; separator=';') as ?names) (group_concat(DISTINCT ?l ; separator=';') as ?lbls) WHERE {\n"
            + "  GRAPH <" + con.getCentralGraph() + "> {\n"
            + " <" + uri + ">  owl:sameAs  ?object }\n"
            + "  GRAPH <" + con.getAuthorsGraph() + "> {\n"
            + "    ?object foaf:name ?name .\n"
            + "    OPTIONAL {\n"
            + "    ?object foaf:publications ?pub .\n"
            + "    OPTIONAL {\n"
            + "    ?pub dct:subject ?s .\n"
            + "     OPTIONAL {\n"
            + "        ?s rdfs:label ?l\n"
            + "         }\n"
            + "       }\n"
            + "    }\n"
            + "    }\n"
            + "} group by ?object";

    try {
      List<Map<String, Value>> prof = sparqlService.query(QueryLanguage.SPARQL, queryprofile);
      JSONObject main = new JSONObject();
      JSONArray array = new JSONArray();
      for (Map<String, Value> p : prof) {

        JSONObject obj = new JSONObject();
        obj.put("name", p.get("names").stringValue());
        obj.put("uri", p.get("object").stringValue());
        if (p.containsKey("lbls")) {
          obj.put("subject", p.get("lbls").stringValue());
        }
        if (!table.isEmpty() && table.containsKey(obj.get("uri"))) {
          obj.put("status", table.get(obj.get("uri")));
        } else {
          obj.put("status", false);
        }
        array.add(obj);
      }
      main.put("data", array);
      return main;

    } catch (MarmottaException ex) {
      log.debug(ex.getMessage());
    }
    return null;
  }

  @Override
  public JSONObject getProfileNames(String uri, HashMap<String, Boolean> table) {
    String queryprofile = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"
            + "SELECT ?name  WHERE {\n"
            + "  GRAPH <" + con.getCentralGraph() + "> {\n"
            + " <" + uri + ">  foaf:name ?name  }\n"
            + "} ";

    try {
      List<Map<String, Value>> prof = sparqlService.query(QueryLanguage.SPARQL, queryprofile);
      JSONObject main = new JSONObject();
      JSONArray array = new JSONArray();
      List<String> names = new ArrayList();
      for (Map<String, Value> p : prof) {
        names.add(p.get("name").stringValue());

      }
      main.put("data", array);
      return unifiednames(names, table);

    } catch (MarmottaException ex) {
      log.debug(ex.getMessage());
    }
    return null;
  }

  public JSONObject unifiednames(List<String> nombres, HashMap<String, Boolean> table) {
    HashMap hmap = new HashMap();
    //String [] nombres =  new String [] {"M Espinoza","M., Espinoza","Espinoza , Mauricio","Espinoza, Mauricio","mauricio espinoza","Mauricio Espinoza","Espinoza, M.","Espinoza Mejía, Jorge Mauricio","Espinoza Mejía, M","Espinoza Mejia, M","ESPINOZA MEJIA ,   JORGE MAURICIO","mauricio espinoza mejia","Espinoza,Mauricio","Mauricio Espinoza Mejía","Mauricio Espinosa Mejía","Jorge Mauricio Espinoza Mejía","M Espinoza Mejía","M Espinoza-Mejía","M Espinoza-Mejia","Mauricio Espinoza-Mejía","J Mejía","Mauricio Espinoza-Mejıa","J Mejia","Mauricio, Espinoza-Mejla","Mauricio, Espinoza-Mejía","M., Espinoza-Mejla","M., Espinoza-Mejía","Mauricio, Espinoza","M., Mejía","Mauricio Espinoza, Espinoza-Mejla","Mauricio Espinoza, Mejía","Mauricio, Mejía","Mauricio Espinoza, Espinoza-Mejía","Mauricio Espinoza, Espinoza","Mauricio, Espinoza-Mejia","Jorge Mauricio Espinoza","Espinoza-Mejía, M.","Espinoza-Mejía, Mauricio","Espinoza, Mauricio J.","Mauricio J Espinoza","Jorge Mauricio Espinoza Mejia","M., Espinoza-Mejia","Mauricio J., Espinoza","Mauricio J., Espinoza-Mejía","Mauricio J., Espinoza-Mejia","Mauricio J., Espinoza-Mejla"};
    //String cadena = "Maurício, Espinoza.";

    for (String no : nombres) {
      String n = no.toUpperCase().replaceAll("[,\\.-]", " ").replaceAll("\\s+", " ");
      String t = StringUtils.stripAccents(n).trim();
      if (hmap.containsKey(t)) {
        hmap.put(t, hmap.get(t) + ";" + no);
      } else {
        hmap.put(t, no);
      }

    }
    JSONObject main = new JSONObject();
    JSONArray array = new JSONArray();
    Iterator it = hmap.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry pair = (Map.Entry) it.next();
      System.out.println(pair.getKey() + "-" + pair.getValue());
      JSONObject obj = new JSONObject();
      obj.put("name", pair.getKey());
      obj.put("other", pair.getValue());
      if (!table.isEmpty() && table.containsKey(obj.get("name"))) {
        obj.put("status", table.get(obj.get("name")));
      } else {
        obj.put("status", false);
      }
      array.add(obj);
    }
    main.put("data", array);
    return main;
  }

  @Override
  public JSONObject getProfileEmail(String uri, HashMap<String, Boolean> table) {
    String querymail = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"
            + "SELECT ?mail  WHERE {\n"
            + "  GRAPH <" + con.getCentralGraph() + "> {\n"
            + " <" + uri + ">  <http://www.w3.org/2006/vcard/ns#hasEmail> ?mail  }\n"
            + "} ";
    try {
      List<Map<String, Value>> qmail = sparqlService.query(QueryLanguage.SPARQL, querymail);
      JSONObject main = new JSONObject();
      JSONArray array = new JSONArray();
      for (Map<String, Value> p : qmail) {

        JSONObject obj = new JSONObject();
        if (p.containsKey("mail")) {
          obj.put("mail", p.get("mail").stringValue());
          if (!table.isEmpty() && table.containsKey(obj.get("mail"))) {
            obj.put("status", table.get(obj.get("mail")));
          } else {
            obj.put("status", false);
          }
        }
        array.add(obj);
      }
      main.put("data", array);
      return main;

    } catch (MarmottaException ex) {
      log.debug(ex.getMessage());
    }

    return null;

  }

  @Override
  public JSONObject getPublicationsCandidates(String uri, HashMap<String, Boolean> table) {
    String queryprofile = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"
            + "SELECT distinct ?pub (group_concat(DISTINCT ?t ; separator=';') as ?ts)  (group_concat(DISTINCT ?namex ; separator=';') as ?names)    WHERE {\n"
            + "  GRAPH <" + con.getCentralGraph() + "> {\n"
            + " <" + uri + ">  foaf:publications  ?pub . \n"
            + "   ?pub dct:title ?t .\n"
            + "    ?co  foaf:publications ?pub .\n"
            + "    {\n"
            + "    select  ?co (SAMPLE (?name) as ?namex) {\n"
            + "      <" + uri + ">  foaf:publications  ?pub .\n"
            + "     ?co  foaf:publications ?pub .\n"
            + "     ?co foaf:name ?name\n"
            + "     } group by ?co\n"
            + "  }}\n"
            + "\n"
            + "} group by ?pub";

    try {
      List<Map<String, Value>> prof = sparqlService.query(QueryLanguage.SPARQL, queryprofile);
      JSONObject main = new JSONObject();
      JSONArray array = new JSONArray();
      for (Map<String, Value> p : prof) {

        JSONObject obj = new JSONObject();
        obj.put("uri", p.get("pub").stringValue());
        if (p.containsKey("ts")) {
          obj.put("title", p.get("ts").stringValue());
        }
        if (p.containsKey("names")) {
          obj.put("authors", p.get("names").stringValue());
        }
        if (!table.isEmpty() && table.containsKey(obj.get("uri"))) {
          obj.put("status", table.get(obj.get("uri")));
        } else {
          obj.put("status", false);
        }

        array.add(obj);
      }
      main.put("data", array);
      return main;

    } catch (MarmottaException ex) {
      log.debug(ex.getMessage());
    }
    return null;
  }

  @Override
  public String totalProfileVal(String uri, String orcid) {

    Document d = mongos.getProfileValAuthor(orcid);

    JSONObject main = new JSONObject();
    main.put("profiles", this.getProfileCandidates(uri, recoverData(d, "profiles")));
    main.put("names", this.getProfileNames(uri, recoverData(d, "names")));
    main.put("emails", this.getProfileEmail(uri, recoverData(d, "emails")));
    main.put("publications", this.getPublicationsCandidates(uri, recoverData(d, "publications")));
    main.put("basic", this.getProfileInst(uri));
    JSONObject profileAreas = this.getProfileAreas(uri, recoverData(d, "clusters"), recoverData(d, "subclusters"));
    main.put("clusters", profileAreas.get("clusters"));
    main.put("subclusters", profileAreas.get("subclusters"));

    return main.toJSONString();
  }

  public HashMap<String, Boolean> recoverData(Document d, String name) {
    HashMap<String, Boolean> hmap = new HashMap();
    if (d != null && d.containsKey(name)) {
      List<Document> ld = (List<Document>) d.get(name);
      Iterator lt = ld.iterator();

      while (lt.hasNext()) {
        List<Document> ldoc = (List<Document>) lt.next();
        Iterator ild = ldoc.iterator();
        while (ild.hasNext()) {

          Document doc = (Document) ild.next();
          hmap.put(doc.get("id").toString(), (Boolean) doc.get("status"));
        }
      }
    }
    return hmap;
  }

  @Override
  public String saveProfileData(String jsondata, String id, String uri, String prof) {
    try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {

      MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());

      // Delete and create collection
      MongoCollection<Document> collection = db.getCollection(MongoService.Collection.PROFILE_AUTHOR.getValue());
      // collection.drop();

      // ObjectMapper objectMapper = new ObjectMapper();
      mongos.removeProfileValAuthor(id);
      Document parse = Document.parse(jsondata);
      Document parseprof = Document.parse(prof);
      parse.append("_id", id);
      parse.append("uri", uri);
      parse.append("uri", uri);
      parse.append("profile", parseprof);
      collection.insertOne(parse);

      if (uri.compareTo("new_") == 0) {
        updateAuthor(id, parseprof);
      }

    } catch (Exception ex) {
      log.info("Error updating author profile {}, {}", id, ex.getMessage());
    }
    return "";
  }

  public void updateAuthor(String orcid, Document profile) throws RepositoryException, RDFHandlerException, InterruptedException {
    String org = profile.getString("org");
    String fullname = profile.getString("name");
    String fname = profile.getString("fname");
    String lname = profile.getString("lname");
    String mail = profile.getString("email");
    String bio = profile.getString("bio");
    String img = profile.getString("img");
    boolean orgOk = false;
    try {
      orgOk = fastSparqlService.getSparqlService()
              .ask(QueryLanguage.SPARQL, "ask { graph <" + constantService.getOrganizationsGraph() + "> { <" + org + "> ?p ?v .  }} ");
    } catch (MarmottaException ex) {
    }

    if (orgOk) {
      Model m = new LinkedHashModel();
      ValueFactoryImpl vfi = ValueFactoryImpl.getInstance();
      String orgx = org.replaceAll(constantService.getOrganizationBaseUri(), "");
      URI authURI = vfi.createURI(constantService.getAuthorResource() + "orcid/" + orgx + "/" + orcid);
      m.add(authURI, RDF.TYPE, FOAF.Person);
      m.add(authURI, FOAF.name, vfi.createLiteral(fullname));
      m.add(authURI, FOAF.firstName, vfi.createLiteral(fname));
      m.add(authURI, FOAF.lastName, vfi.createLiteral(lname));
      m.add(authURI, vfi.createURI("http://www.w3.org/2006/vcard/ns#hasEmail"), vfi.createLiteral(mail));
      m.add(authURI, vfi.createURI("http://purl.org/spar/scoro/hasORCID"), vfi.createURI("https://orcid.org/" + orcid));
      m.add(authURI, vfi.createURI("http://purl.org/vocab/bio/0.1/olb"), vfi.createLiteral(bio));
      m.add(authURI, vfi.createURI("http://xmlns.com/foaf/0.1/img"), vfi.createURI(img));
      m.add(authURI, vfi.createURI("http://schema.org/memberOf"), vfi.createURI(org));
      m.add(authURI, RDF.TYPE, vfi.createURI("http://semanticweb.cs.vu.nl/2009/11/sem/Temporary"));

      URI contxRedi = vfi.createURI(constantService.getCentralGraph());
      RepositoryConnection connection = sesameService.getConnection();
      connection.begin();
      connection.add(m, contxRedi);
      connection.commit();
      connection.close();
      fastSparqlService.getGraphDBInstance().addBuffer(contxRedi, m);
      fastSparqlService.getGraphDBInstance().dumpBuffer();
      solrIndexingService.indexResource(authURI);
      loadService.authors(authURI.stringValue());

    } else {
      log.info("Ignoring new author {} from {}", orcid, org);
    }

  }

  @Override
  public String getProfile(String uri, String orcid) {
    Document d = mongos.getProfileValAuthor(orcid);
    Document obj = (Document) d.get("profile");
    obj.append("uri", d.get("uri").toString());
    // Document uri =      d.get("uri").toString();

    return obj.toJson();
  }

  @Override
  public JSONObject obtainNewProfiles(String org) throws Exception {
    JSONObject obtainNewProfiles = mongos.obtainNewProfiles(org);
    return obtainNewProfiles;
  }

  @Override
  public JSONObject getProfileAreas(String uri, HashMap<String, Boolean> tablec, HashMap<String, Boolean> tablesc) {
    String queryClusters = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "\n"
            + "select ?c ?len {\n"
            + "    graph <" + con.getClusterGraph() + "> {\n"
            + "    	<" + uri + "> dct:isPartOf ?c .\n"
            + "      ?c a <http://ucuenca.edu.ec/ontology#Cluster> .\n"
            + "      ?c rdfs:label ?len .\n"
            + "    }\n"
            + "} ";
    String querySubClusters = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "\n"
            + "select ?c (group_concat(?len ; separator = ';' ) as ?le ) {\n"
            + "    graph <" + con.getClusterGraph() + "> {\n"
            + "    	<" + uri + "> dct:isPartOf ?c .\n"
            + "      ?c a <http://ucuenca.edu.ec/ontology#SubCluster> .\n"
            + "      ?c rdfs:label ?len .\n"
            + "    }\n"
            + "} group by ?c";
    try {
      List<Map<String, Value>> aCluster = sparqlService.query(QueryLanguage.SPARQL, queryClusters);
      List<Map<String, Value>> aSubCluster = sparqlService.query(QueryLanguage.SPARQL, querySubClusters);
      JSONObject main = new JSONObject();
      JSONArray array = new JSONArray();
      for (Map<String, Value> p : aCluster) {
        JSONObject obj = new JSONObject();
        if (p.containsKey("c")) {
          obj.put("cluster", p.get("c").stringValue());
          obj.put("label", p.get("len").stringValue());
        }
        if (!tablec.isEmpty() && tablec.containsKey(obj.get("cluster"))) {
          obj.put("status", tablec.get(obj.get("cluster")));
        } else {
          obj.put("status", false);
        }
        array.add(obj);
      }
      JSONObject clus = new JSONObject();
      clus.put("data", array);
      main.put("clusters", clus);
      JSONArray array2 = new JSONArray();
      for (Map<String, Value> p : aSubCluster) {
        JSONObject obj = new JSONObject();
        if (p.containsKey("c")) {
          obj.put("subcluster", p.get("c").stringValue());
          obj.put("label", p.get("le").stringValue());
        }
        if (!tablesc.isEmpty() && tablesc.containsKey(obj.get("subcluster"))) {
          obj.put("status", tablesc.get(obj.get("subcluster")));
        } else {
          obj.put("status", false);
        }
        array2.add(obj);
      }
      clus = new JSONObject();
      clus.put("data", array2);
      main.put("subclusters", clus);
      return main;
    } catch (MarmottaException ex) {
      log.debug(ex.getMessage());
    }
    return null;
  }

  @Override
  public String sendFeedback(String name, String email, String topic, String content, String url) throws Exception {

    if (name == null || email == null || topic == null || content == null) {
      throw new Exception("Null Fields");
    }
    if (name.trim().isEmpty() || email.trim().isEmpty() || topic.trim().isEmpty() || content.trim().isEmpty()) {
      throw new Exception("Empty Fields");
    }
    url = url != null ? url : "";

    String[] mails = conf.getStringConfiguration("redi.feedback.mail").split(";");
    Mailer mailer = MailerBuilder
            .withSMTPServer("190.15.141.2", 25, "no-reply@cedia.org.ec")
            .clearEmailAddressCriteria() // turns off email validation
            .withDebugLogging(true)
            .buildMailer();

    EmailPopulatingBuilder bui = EmailBuilder.startingBlank()
            .from("Notificationes REDI", "no-reply@cedia.org.ec");
    for (String mmail : mails) {
      bui = bui.to("Administrador de REDI", mmail);
    }
    Email emailx = bui.
            withSubject("Retroalimentación de REDI: " + StringEscapeUtils.escapeHtml4(topic))
            .withHTMLText("<i>Nombre</i> : " + StringEscapeUtils.escapeHtml4(name) + "<br/>\n"
                    + "<i>Correo</i> : " + StringEscapeUtils.escapeHtml4(email) + "<br/>\n"
                    + "<i>Tema</i> : " + StringEscapeUtils.escapeHtml4(topic) + "<br/>\n"
                    + "<i>URL</i> : " + StringEscapeUtils.escapeHtml4(url) + "<br/>\n"
                    + "<i>Contenido</i> : " + StringEscapeUtils.escapeHtml4(content) + "<br/>\n")
            .withPlainText("Nombre : " + name + "\n"
                    + "Correo : " + email + "\n"
                    + "Tema : " + topic + "\n"
                    + "URL : " + url + "\n"
                    + "Contenido : " + content + "\n"
            )
            .buildEmail();
    mailer.sendMail(emailx, true);

    return " { status : 0 } ";

  }
}
