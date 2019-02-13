/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.marmotta.ucuenca.wk.authors.services;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.authors.api.OrganizationService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.FOAF;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.semarglproject.vocab.RDF;

/**
 *
 * @author joe
 */
public class OrganizationServiceImpl implements OrganizationService {
    
     @Inject
    private ExternalSPARQLService sparqlService;
     
     @Inject
    private QueriesService queriesService;
     
    @Inject
    private ConstantService con;
      
     private final static String STR = "^^xsd:string";

  //   private final static String BOOL = "^^xsd:boolean";
       
      private final static String LANGEN = "@en";
       
      private final  static String LANGES = "@es";

    @Override
    public String addOrganization(String acro, String namEn, String namEs, String alias, String country, String prov, String city, String lat, String lon, String type ) {
        String uriOrg = con.getOrganizationBaseUri()+acro ;
         if (!askOrganization (uriOrg)) {
             insertOrganization(uriOrg, REDI.NAME.toString(), acro , STR);
             insertOrganization(uriOrg, REDI.FULLNAME.toString(), namEn , LANGEN);
             insertOrganization(uriOrg, REDI.FULLNAME.toString() , namEs , LANGES);
             insertOrganization(uriOrg, REDI.ALIAS.toString() , alias , STR);
             insertOrganization(uriOrg, REDI.COUNTRY.toString() , country , STR);
             insertOrganization(uriOrg, REDI.PROVINCE.toString(), prov , STR);
             insertOrganization(uriOrg, REDI.CITY.toString(), city , STR);
             insertOrganization(uriOrg, REDI.LATITUDE.toString(), lat , STR);
             insertOrganization(uriOrg, REDI.LONGITUDE.toString(), lon , STR);
             insertOrganization(uriOrg, REDI.TYPE.toString(), type , STR);
             insertOrganization(uriOrg, RDF.TYPE.toString() , FOAF.ORGANIZATION.toString() , STR);
             return "Successfully Registration" ;
         } else {
             return "Fail: ACRONYM ALREADY IN USE";
         }
    }
    
    
        private String  insertOrganization(String... parameters) {
        try {
           // String queryInsertEndpoint = queriesService.getInsertEndpointQuery(parameters[0], parameters[1], parameters[2], parameters[3]);
          String queryInsertOrg = queriesService.getInsertGeneric (con.getOrganizationsGraph() ,  parameters[0], parameters[1], parameters[2], parameters[3] );
            sparqlService.getSparqlService().update(QueryLanguage.SPARQL, queryInsertOrg);
            return "Successfully Registration";
        } catch (InvalidArgumentException | MarmottaException | MalformedQueryException | UpdateExecutionException ex) {
            Logger.getLogger(EndpointServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return "Fail Insert";
        }
    }
         @Override
         public boolean  askOrganization (String uri )  {
            String askOrg =  queriesService.getAskResourceQuery (con.getOrganizationsGraph(), uri);
         try {
             return  sparqlService.getSparqlService().ask( QueryLanguage.SPARQL , askOrg );
         } catch (MarmottaException ex) {
             Logger.getLogger(OrganizationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
             //return ;
             return false;
         }
         
              
          }
         
         
         @Override
         public String listOrganization ()  {
         try {
             //  ObjectMapper mapper = new ObjectMapper();
             String queryOrg =  queriesService.getListOrganizationQuery ();
             List<Map<String, Value>> response = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, queryOrg);
             return listmapTojson (response);
         } catch ( MarmottaException | JSONException ex) {
             Logger.getLogger(OrganizationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
         
         }
         
         
     public String listmapTojson(List<Map<String, Value>> list) throws JSONException
     {       
         JSONObject jsonh1 =new JSONObject();
         
     
    JSONArray jsonArr=new JSONArray();
    for (Map<String, Value> map : list) {
        JSONObject jsonObj=new JSONObject();
        for (Map.Entry<String, Value> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().stringValue();
            
            try {
                jsonObj.put(key,value);
            
            } catch (org.json.JSONException ex) {
                Logger.getLogger(OrganizationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }                           
        }
        jsonArr.put(jsonObj);
    }
    
    //return jsonArr.toString();
    return jsonh1.put("data", jsonArr).toString();
}
    @Override
    public String loadOrgbyURI( String uri) {
         String queryOrgURI  =  queriesService.getOrgByUri(uri);
         try {
               List<Map<String, Value>> result = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, queryOrgURI);
             return listmapTojson (result);
         } catch ( MarmottaException | JSONException ex) {
             Logger.getLogger(OrganizationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
             return "FAIL"+ex; 
         }
      
     //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    @Override
   public  String editOrg (String acro, String namEn, String namEs, String alias, String country, String prov, String city, String lat, String lon, String type ) {
         try {
             String uriOrg = con.getOrganizationBaseUri()+acro ;
             
             editOrganization(uriOrg, REDI.FULLNAME.toString(), namEn , LANGEN);
             editOrganization(uriOrg, REDI.FULLNAME.toString() , namEs , LANGES);
             editOrganization(uriOrg, REDI.ALIAS.toString() , alias , STR);
             editOrganization(uriOrg, REDI.COUNTRY.toString() , country , STR);
             editOrganization(uriOrg, REDI.PROVINCE.toString(), prov , STR);
             editOrganization(uriOrg, REDI.CITY.toString(), city , STR);
             editOrganization(uriOrg, REDI.LATITUDE.toString(), lat , STR);
             editOrganization(uriOrg, REDI.LONGITUDE.toString(), lon , STR);
             editOrganization(uriOrg, REDI.TYPE.toString(), type , STR);
             
             return "Success";
         } catch ( InvalidArgumentException | MarmottaException | MalformedQueryException | UpdateExecutionException ex) {
             Logger.getLogger(OrganizationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
             return "Fail";
         }
   
   }
   
   
         private String  editOrganization(String... parameters) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {
        
           // String queryInsertEndpoint = queriesService.getInsertEndpointQuery(parameters[0], parameters[1], parameters[2], parameters[3]);
          String queryInsertOrg = queriesService.updateGeneric(con.getOrganizationsGraph() ,  parameters[0], parameters[1], parameters[2], parameters[3] );
          sparqlService.getSparqlService().update(QueryLanguage.SPARQL, queryInsertOrg);
            return "Successfully Registration";
      
    }
         
    @Override
    public String removeOrgbyURI(String resourceid) {
       
            
            String queryEnd =   queriesService.getAskObjectQuery(con.getEndpointsGraph(), resourceid);
          
            try {
                 if ( !sparqlService.getSparqlService().ask(QueryLanguage.SPARQL, queryEnd)){
                String queryRemove = queriesService.removeGenericType( con.getOrganizationsGraph() ,  FOAF.ORGANIZATION.toString() , resourceid );
                sparqlService.getSparqlService().update(QueryLanguage.SPARQL, queryRemove);
                return "Success";
                 } else {
                  return "Fail: Endpoints Attachment";
                 }
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            } catch ( InvalidArgumentException | MarmottaException | MalformedQueryException | UpdateExecutionException ex) {
                Logger.getLogger(OrganizationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                return "Fail: Update";
            }
   
        
} 

}
