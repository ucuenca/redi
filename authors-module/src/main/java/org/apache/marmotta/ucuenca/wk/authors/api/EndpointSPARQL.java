/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.marmotta.ucuenca.wk.authors.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;


/**
 *
 * @author joe
 */
public class EndpointSPARQL extends EndpointObject {
   private String url;
   private Repository repo;




    public Repository getRepo() {
        return repo;
    }

    public void setRepo(Repository repo) {
        this.repo = repo;
    }

    /**
     * graph where the information will be extracted
     */
  //  private String graph;

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }
    


    public EndpointSPARQL(String status, String name, String access, String type , String graph, String resourceId) {
        super(status, name, access,  type , graph ,resourceId);
        this.url = access;
       // this.graph = graph;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }



    @Override
    public List querySource( String query) {
         RepositoryConnection conn = null;
         Repository repo = this.getRepo();
       try {
           List <HashMap> listResults ;
           if (!repo.isInitialized()) {
               repo.initialize();
           }
           conn = repo.getConnection();
           conn.begin();
           TupleQueryResult result = conn.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate();
           
           List<String> resultsName = result.getBindingNames();
            listResults = new ArrayList ();
                while (result.hasNext()) {
                 BindingSet bd = result.next();  
                 HashMap hm = new HashMap ();
                 for (String name : resultsName )
                 {   
                      String value =  bd.getBinding(name).getValue().stringValue();
                      hm.put(name, value);
                 }
                   listResults.add(hm);
                } 
             
            
            
            return listResults;
           //conn.close();
       } catch (RepositoryException | QueryEvaluationException | MalformedQueryException ex) {
             Logger.getLogger(EndpointSPARQL.class.getName()).log(Level.SEVERE, null, ex);
             return new ArrayList ();
             
       } finally {
             try {
                 conn.close();
             } catch (RepositoryException ex) {
                 Logger.getLogger(EndpointSPARQL.class.getName()).log(Level.SEVERE, null, ex);
             }
       }
      // return new ArrayList ();
        
    }

    @Override
    public Boolean prepareQuery() {
         if (this.getRepo() == null){
         Repository endpointrep = new SPARQLRepository(this.getURL());
         this.setRepo(endpointrep);
         }
         return true;    
    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean closeconnection() {
       try {
           this.getRepo().shutDown();
           return true;
           // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
       } catch (RepositoryException ex) {
           Logger.getLogger(EndpointSPARQL.class.getName()).log(Level.SEVERE, null, ex);
           return false;
       }
    }

    
}
