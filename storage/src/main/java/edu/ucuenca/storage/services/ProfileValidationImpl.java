/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.storage.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.hp.hpl.jena.query.QueryExecutionFactory.sparqlService;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import edu.ucuenca.storage.api.MongoService;
import javax.enterprise.context.ApplicationScoped;
import edu.ucuenca.storage.api.ProfileValidation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;

/**
 *
 * @author joe
 */
@ApplicationScoped
public class ProfileValidationImpl implements ProfileValidation {
     @Inject
    private ConfigurationService conf;

    @Inject
    private SparqlService sparqlService;
    @Inject
    private ConstantService con;
    
      @Inject
    private MongoService mongos;

    @Override
    public JSONObject getProfileCandidates(String uri ,  HashMap<String, Boolean> table) {
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
                if (p.containsKey("lbls")){
                obj.put("subject", p.get("lbls").stringValue());
                }
                if (!table.isEmpty() && table.containsKey(obj.get("uri"))){
                obj.put("status", table.get(obj.get("uri")));
                } else {
                 obj.put("status", false);
                }
                array.add(obj);
            }
            main.put("data", array);
            return main;

        } catch (MarmottaException ex) {
            Logger.getLogger(ProfileValidationImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public JSONObject getProfileNames(String uri ,  HashMap<String, Boolean> table) {
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
            return unifiednames(names ,table);

        } catch (MarmottaException ex) {
            Logger.getLogger(ProfileValidationImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public JSONObject unifiednames(List<String> nombres , HashMap<String, Boolean> table) {
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
            if (!table.isEmpty() &&table.containsKey(obj.get("name"))){
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
    public JSONObject getProfileEmail(String uri ,  HashMap<String, Boolean> table) {
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
                if (p.containsKey("mail")){
                obj.put("mail", p.get("mail").stringValue());
                  if (!table.isEmpty() && table.containsKey(obj.get("mail"))){
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
            Logger.getLogger(ProfileValidationImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;

    }

    @Override
    public JSONObject getPublicationsCandidates(String uri ,  HashMap<String, Boolean> table) {
        String queryprofile = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX dct: <http://purl.org/dc/terms/>\n"
                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"
                + "SELECT distinct ?pub (group_concat(DISTINCT ?t ; separator=';') as ?ts)  (group_concat(DISTINCT ?namex ; separator=';') as ?names)    WHERE {\n"
                + "  GRAPH <"+con.getCentralGraph()+"> {\n"
                + " <"+uri+">  foaf:publications  ?pub . \n"
                + "   ?pub dct:title ?t .\n"
                + "    ?co  foaf:publications ?pub .\n"
                + "    {\n"
                + "    select  ?co (SAMPLE (?name) as ?namex) {\n"
                + "      <"+uri+">  foaf:publications  ?pub .\n"
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
                if (p.containsKey("ts")){
                obj.put("title", p.get("ts").stringValue());
                }
                if (p.containsKey("names")){
                obj.put("authors", p.get("names").stringValue());
                }
                    if (!table.isEmpty() && table.containsKey(obj.get("uri"))){
                obj.put("status", table.get(obj.get("uri")));
                } else {
                 obj.put("status", false);
                }
                
                array.add(obj);
            }
            main.put("data", array);
            return main;

        } catch (MarmottaException ex) {
            Logger.getLogger(ProfileValidationImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @Override
    public String totalProfileVal (String uri , String orcid ) {
        
         Document d = mongos.getProfileValAuthor(orcid);
         
          JSONObject main = new JSONObject();
        main.put("profiles", this.getProfileCandidates(uri ,recoverData ( d, "profiles" ) ));
        main.put("names", this.getProfileNames(uri , recoverData ( d, "names" )));
        main.put("emails", this.getProfileEmail(uri , recoverData ( d, "emails" )));
        main.put("publications", this.getPublicationsCandidates(uri , recoverData ( d, "publications" )));
        
    return main.toJSONString() ;
    }
    
     public HashMap<String, Boolean> recoverData (Document d, String name) {
          HashMap <String, Boolean> hmap = new HashMap ();
       if (d!= null && d.containsKey(name)){
         List<Document> ld = (List<Document>) d.get(name);
         Iterator lt = ld.iterator();
          
           while (lt.hasNext()) {
           List<Document> ldoc =  (List<Document> ) lt.next();
               Iterator ild = ldoc.iterator();
             while (ild.hasNext()) {
                 
               Document doc =  (Document) ild.next();
               hmap.put(doc.get("id").toString(), (Boolean) doc.get("status"));
             }
           }
         }
       return hmap;
     }
    
     @Override
     public String saveProfileData (String jsondata , String id , String uri) {   
    try (MongoClient client = new MongoClient(conf.getStringConfiguration("mongo.host"), conf.getIntConfiguration("mongo.port"));) {
             
            MongoDatabase db = client.getDatabase(MongoService.Database.NAME.getDBName());
           
            // Delete and create collection
            MongoCollection<Document> collection = db.getCollection(MongoService.Collection.PROFILE_AUTHOR.getValue());
           // collection.drop();
         
           // ObjectMapper objectMapper = new ObjectMapper();
            mongos.removeProfileValAuthor(id);
            Document parse = Document.parse(jsondata);
            parse.append("_id", id);
            parse.append("uri", uri);
            collection.insertOne(parse);
     
      return "";
     }
    
     }
     
     
   
    
     }


