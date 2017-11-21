/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;

/**
 *
 * @author joe
 */
public class InterfaceProviders {

   

    @Inject
    private SparqlService sparqlService;

    public String getAfiliationQuery(String authorUri) {
        return "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "PREFIX ucmodel: <http://ucuenca.edu.ec/ontology#>\n"
                + "SELECT ?resource ?name   (group_concat(?afiliation;separator=\" | \") as  ?afiliations)  FROM <http://redi.cedia.edu.ec/context/provider/AcademicsKnowledgeProvider> "
                + "WHERE {\n"
                + "  ?query <http://www.w3.org/2002/07/owl#oneOf> <" + authorUri + "> .\n"
                + "    ?resource  <http://www.w3.org/2002/07/owl#oneOf>  ?query .\n"
                + "    ?resource     foaf:name  ?name .\n"
                + "    ?resource     ucmodel:affiliationName ?afiliation .\n"
                + "}\n"
                + "group by ?resource ?name ";

    }

    public String getPubTitleQuery(String authorUri) {
        return "PREFIX dct: <http://purl.org/dc/terms/>\n"
                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "PREFIX ucmodel: <http://ucuenca.edu.ec/ontology#>\n"
                + "SELECT ?resource ?name (group_concat(?title;separator=\" | \") as ?titles) FROM <http://redi.cedia.edu.ec/context/provider/AcademicsKnowledgeProvider>"
                + " WHERE {\n"
                + "  ?query <http://www.w3.org/2002/07/owl#oneOf> <" + authorUri + "> .\n"
                + "    ?resource  <http://www.w3.org/2002/07/owl#oneOf>  ?query .\n"
                + "    ?resource     foaf:name  ?name .\n"
                + "    ?resource      foaf:publications  ?publication .\n"
                + "    ?publication   dct:title  ?title \n"
                + "}\n"
                + "group by ?resource ?name ";

    }

    public String getCoautorsQuery(String authorUri) {
        return "PREFIX dct: <http://purl.org/dc/terms/>\n"
                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "PREFIX ucmodel: <http://ucuenca.edu.ec/ontology#>\n"
                + "SELECT ?resource ?name  (group_concat(?nameCont ;separator=\" | \") as  ?namesContr) FROM <http://redi.cedia.edu.ec/context/provider/AcademicsKnowledgeProvider>"
                + "  WHERE {\n"
                + "  ?query <http://www.w3.org/2002/07/owl#oneOf> <" + authorUri + "> .\n"
                + "    ?resource  <http://www.w3.org/2002/07/owl#oneOf>  ?query .\n"
                + "    ?resource     foaf:name  ?name .\n"
                + "    ?resource      foaf:publications  ?publication .\n"
                + "    ?publication    dct:contributor   ?contributor .\n"
                + "    ?contributor    foaf:name  ?nameCont \n"
                + "} Group by  ?resource ?name ";

    }

    public String getTopicsQuery(String authorUri) {
        return "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "PREFIX ucmodel: <http://ucuenca.edu.ec/ontology#>\n"
                + "SELECT ?resource ?name   (group_concat(?topic;separator=\" | \") as  ?topics) FROM <http://redi.cedia.edu.ec/context/provider/AcademicsKnowledgeProvider> "
                + "WHERE {\n"
                + "  ?query <http://www.w3.org/2002/07/owl#oneOf> <" + authorUri + "> .\n"
                + "    ?resource  <http://www.w3.org/2002/07/owl#oneOf>  ?query .\n"
                + "    ?resource      foaf:publications  ?publication .\n"
                + "    ?resource     foaf:name  ?name .\n"
                + "    ?publication   foaf:topic  ?topic \n"
                + "}\n"
                + "group by ?resource ?name      ";
    }

    public  Map <String, AuthorsInfo> interfaceAK(String authorUri) {
              List author = new ArrayList ();
              Map <String, AuthorsInfo>  mapAuthors = new HashMap ();
             // AuthorsInfo a = new AuthorsInfo ();
        try {
            List<Map<String, Value>> afiliationResp = sparqlService.query(QueryLanguage.SPARQL, getAfiliationQuery(authorUri));
            List<Map<String, Value>> publicationResp = sparqlService.query(QueryLanguage.SPARQL, getPubTitleQuery(authorUri));
            List<Map<String, Value>> coautorsResp = sparqlService.query(QueryLanguage.SPARQL, getCoautorsQuery(authorUri));
            List<Map<String, Value>> topicsResp = sparqlService.query(QueryLanguage.SPARQL, getTopicsQuery(authorUri));
            
            for  ( Map<String, Value> af : afiliationResp) {
                 String resourceId =  af.get("resource").stringValue();
                 String [] nameOri =  af.get("name").stringValue().split(" | ");
                 String [] afiliations   =  af.get("afiliation").stringValue().split(" | ");
                 
                if ( !mapAuthors.containsKey(resourceId)) {
                     mapAuthors.put(resourceId, new AuthorsInfo (resourceId) );
                }
                   mapAuthors.get(resourceId).setName(nameOri);
                   mapAuthors.get(resourceId).setAfiliation(afiliations);
                   
            }
            
            
            for (Map<String, Value> p : publicationResp) {
                String resourceId =  p.get("resource").stringValue();
                String [] publication   =  p.get("titles").stringValue().split(" | ");
                 if ( mapAuthors.containsKey(resourceId)) {
                 mapAuthors.get(resourceId).setArticles(publication);
     
                 }
            }
            
            
              for (Map<String, Value> co : coautorsResp) {
                String resourceId =  co.get("resource").stringValue();
                String [] names   =  co.get("namesContr").stringValue().split(" | ");
                 if ( mapAuthors.containsKey(resourceId)) {
                 mapAuthors.get(resourceId).setCoautors(names);
     
                 }
            }
              
              for (Map<String, Value> tp : topicsResp) {
                String resourceId =  tp.get("resource").stringValue();
                String [] topics   =  tp.get("topics").stringValue().split(" | ");
                 if ( mapAuthors.containsKey(resourceId)) {
                 mapAuthors.get(resourceId).setTopics(topics);
     
                 }
            }
              
           return mapAuthors;
            
        } catch (MarmottaException ex) {
            Logger.getLogger(InterfaceProviders.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
