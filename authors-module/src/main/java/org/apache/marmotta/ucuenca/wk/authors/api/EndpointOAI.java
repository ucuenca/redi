/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.marmotta.ucuenca.wk.authors.api;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.openrdf.repository.Repository;
import org.slf4j.Logger;
import java.util.logging.Level;
//import javax.inject.Inject;
import org.apache.marmotta.ucuenca.wk.authors.services.AuthorServiceImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joe
 */
public class EndpointOAI extends EndpointObject {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private String endpoint ;
    
    private Boolean severemode ;

    private Repository repo;

    public Repository getRepo() {
        return repo;
    }

    public void setRepo(Repository repo) {
        this.repo = repo;
    }

 
   // private static final String PLUGINS_FOLDER = System.getProperty("user.home")+"/REDI/Pentaho/data-integration/plugins/";
  
   // private static final String PENTAHO_TRANSFORMATION_PATH = System.getProperty("user.home")+"/REDITRANSF/ExtOAI.ktr";
    private static final String TRANSFORMATION_PARAMETER_NAME_1 = "repoUrl";
    private static final String TRANSFORMATION_PARAMETER_NAME_2 = "repoName";
    private static final String TRANSFORMATION_PARAMETER_NAME_3 = "outputPath";
    private static final String FOLDER = "OAI";
     private static final String FILENAME = "R2RMLtoRDF.ttl";
    
   // private static final String OUTPUT_RDF_PATH = System.getProperty("user.home")+"/REDITRANSF/OAI/R2RMLtoRDF.ttl";
    private static final String TEMPORAL_URI = "http://redi/oai";
    private static final String TEMPORAL_GRAPH = "http://localhost:8080/";
    
    public String getEndpoint() {
        return endpoint;
    }

    public Boolean isSeveremode() {
        return severemode;
    }

    public void setSeveremode(Boolean severemode) {
        this.severemode = severemode;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public String  getFileOutputLocalPath () {
        return this.getOutputFilesPath()+"/"+FOLDER+"/"+FILENAME;
    }

    public EndpointOAI(String status, String name, String access, String type ,  String resourceId , Boolean severemode) {
        //this.graph = TEMPORAL_GRAPH;
        super(status, name, access,  type  , TEMPORAL_GRAPH , resourceId);
        this.endpoint = access;
        this.severemode = severemode;
        
    }
  /*  public String getTemporalGraph () {
        return TEMPORAL_GRAPH+this.getName();
    }*/


    public Boolean extractData() {
       
        try {
             log.info("Comienza Extraccion");
            log.info(this.getName()+"- "+ this.getAccess());
            System.setProperty("KETTLE_PLUGIN_BASE_FOLDERS", this.getPluginsPath() );
            KettleEnvironment.init();
            TransMeta transMeta = new TransMeta(this.getOAITransfPath());
            Trans trans = new Trans(transMeta);
            trans.initializeVariablesFrom(null);
          //  trans.setParameterValue("repo", "http://www.dspace.uce.edu.ec/oai/request");
          //  trans.setParameterValue("repo", "http://dspace.uazuay.edu.ec/oai/request");
            trans.setParameterValue(TRANSFORMATION_PARAMETER_NAME_1 , this.getEndpoint());
            trans.setParameterValue(TRANSFORMATION_PARAMETER_NAME_2 , this.getName());
            trans.setParameterValue(TRANSFORMATION_PARAMETER_NAME_3 , this.getOutputFilesPath()+"/"+FOLDER);
            trans.getTransMeta().setInternalKettleVariables(trans);
            trans.prepareExecution(null);
            trans.startThreads();
            log.info("Ejecutando");
            trans.waitUntilFinished();
           
            if (trans.getErrors()!=0) {
                //System.out.println("Error encountered!");
                log.info("Error:"+trans.getStatus());
                return false;
                
            } else {
            log.info("Extraccion Exitosa");
            return true;
            }
          // Repository repo = createRepo (name);
           // log.info("Repositorio Creado");
           //queryRepoOAI (repo, name);
           //log.info("Consulta Evaluada");
           
        } catch (KettleException ex) {
            java.util.logging.Logger.getLogger(EndpointOAI.class.getName()).log(Level.SEVERE, null, ex);
            log.info("!Error"+ex);
            return false;
            
        }
    }
    

    public Repository createRepo() {
        try {
            log.info("Creando Base Temporal");
            Repository repo = new SailRepository(new MemoryStore());
            repo.initialize();
            
            File file = new File(this.getFileOutputLocalPath());
            String baseURI = TEMPORAL_URI;
            
            RepositoryConnection con = repo.getConnection();
           
            con.add(file, baseURI, RDFFormat.TURTLE, con.getValueFactory().createURI(this.getGraph()));
               
            log.info("Base Temporal Creada");
            
            return repo;
         
            //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        } catch (RepositoryException | IOException | RDFParseException ex) {
            java.util.logging.Logger.getLogger(EndpointOAI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

 
    
     public  List executelocalquery (String query ) {
        RepositoryConnection con = null;
        List <HashMap> listResults ;
        try {
            con = this.getRepo().getConnection();
            log.info("Execute query");
            log.info (query);
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult result = tupleQuery.evaluate();
            List<String> resultsName = result.getBindingNames();
            listResults = new ArrayList ();
                while (result.hasNext()) {
                 BindingSet bd = result.next();  
                 HashMap hm = new HashMap ();
                 for (String name : resultsName )
                 {    if (bd.hasBinding(name)){
                      String value =  bd.getBinding(name).getValue().stringValue();
                      hm.put(name, value);
                      }
                 }
                   listResults.add(hm);
                } 
             
            
            
            return listResults;
        
        } catch (RepositoryException  | MalformedQueryException | QueryEvaluationException ex ) {
            java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            
            try {
                con.close();
            } catch (RepositoryException ex) {
                java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
     
        
          return null;
    }

    @Override
    public List querySource(String query) {
        if (this.getRepo() == null) {
            this.prepareQuery();
         }
        
       return this.executelocalquery(query);
    }

    @Override
    public Boolean prepareQuery() {
         if (this.extractData()) {
          Repository newrepository =  this.createRepo() ;
          this.setRepo(newrepository);
          return true;
          } else {
          return false;
          }
        
        
    }

    @Override
    public Boolean closeconnection() {
        try {
            this.getRepo().shutDown();
            return true;
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        } catch (RepositoryException ex) {
            java.util.logging.Logger.getLogger(EndpointOAI.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    
    
}
