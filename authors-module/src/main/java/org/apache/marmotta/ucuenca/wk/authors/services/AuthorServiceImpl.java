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
package org.apache.marmotta.ucuenca.wk.authors.services;

//import cc.mallet.pipe.CharSequence2TokenSequence;
//import cc.mallet.pipe.Pipe;
//import cc.mallet.pipe.SerialPipes;
//import cc.mallet.pipe.TokenSequence2FeatureSequenceWithBigrams;
//import cc.mallet.pipe.TokenSequenceLowercase;
//import cc.mallet.pipe.TokenSequenceRemoveStopwords;
//import cc.mallet.pipe.iterator.ArrayIterator;
//import cc.mallet.topics.ParallelTopicModel;
//import cc.mallet.types.InstanceList;
import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.FileWriter;
//import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
//import java.io.PrintWriter;
import java.text.SimpleDateFormat;
//import java.net.URL;
//import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedList;
import java.util.List;
import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.commons.io.LineIterator;
import org.apache.marmotta.commons.vocabulary.FOAF;
//import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
//import org.apache.marmotta.ldclient.exception.DataRetrievalException;
//import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.authors.api.AuthorService;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointFile;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointOAI;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointObject;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointSPARQL;
//import org.apache.marmotta.ucuenca.wk.authors.api.EndpointObject;
//import org.apache.marmotta.ucuenca.wk.authors.api.EndpointService;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointsService;
//import org.apache.marmotta.ucuenca.wk.authors.api.SparqlEndpoint;
import org.apache.marmotta.ucuenca.wk.authors.api.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.AskException;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.UpdateException;
//import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
//import org.apache.marmotta.ucuenca.wk.commons.service.DistanceService;
//import org.apache.marmotta.ucuenca.wk.commons.service.KeywordsService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.BIBO;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.json.JSONException;
import org.json.JSONObject;
//import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
//import org.openrdf.query.BindingSet;
//import org.openrdf.query.MalformedQueryException;
//import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
//import org.openrdf.query.TupleQuery;
//import org.openrdf.query.TupleQueryResult;
//import org.openrdf.repository.RepositoryConnection;
//import org.openrdf.repository.RepositoryException;
//import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
//import org.openrdf.repository.Repository;

/**
 * Default Implementation of {@link AuthorService} Fernando B. CEDIA
 *
 * @author Xavier Sumba
 * @author Jose Cullcay
 * @author Jose Segarra
 */
@ApplicationScoped
public class AuthorServiceImpl implements AuthorService {

    @Inject
    private Logger log;

    @Inject
    private SparqlFunctionsService sparqlFunctionsService;

//    @Inject
//    private ConfigurationService configurationService;
    @Inject
    private QueriesService queriesService;

   
    @Inject 
    private EndpointsService endpointService;

    @Inject
    private ConstantService constantService;
    
    @Inject
    private SparqlService sparqlService;
    
   private static final String STR = "string";
    
   private static final String OAIPROVNAME = "Dspace";
   /* @Inject 
    private EndpointObject endpointObject;*/

    private static final int LIMIT = 5000;
 //   private static final int MAX_SUBJECTS = 15;
 //   private static List<SparqlEndpoint> endpoints;
    private final List<String> stopwords = new ArrayList<>();
    private int processpercent = 0;
    
    private final static String  COUNTWORD = "count" ;

  //  private static int upperLimitKey = 5; //Check 6 keywords
  //  private static int lowerLimitKey = upperLimitKey - 1; //Not less than 4 keywords

  //  private PrintWriter out;

//    private static double tolerance = 0.9;

//    private Set<String> setExplored = new HashSet<String>();
//
//    private static int one = 1;

  //  private static final String FILENAME = "DesambiguacionAutoresLog.csv";

 //   private Set<Entry> pairsCompared = null;

//    private BufferedWriter bw = null;
    //private FileWriter fw = null;

    @PostConstruct
    public void init() {
        BufferedReader input = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("helpers/stoplist.txt")));
        LineIterator it = new LineIterator(input);
        String line;
        while (it.hasNext()) {
            line = it.nextLine();
            String[] words = line.split("\\s+");
            stopwords.addAll(Arrays.asList(words));
        }
        it.close();

//        filterProperties = Arrays.asList("http://www.w3.org/2004/02/skos/core#prefLabel",
//                "http://www.w3.org/2000/01/rdf-schema#comment",
//                "http://www.w3.org/ns/dcat#contactPoint",
//                "http://www.w3.org/ns/dcat#landingPage",
//                "http://vivoweb.org/ontology/core#freetextKeyword",
//                "http://www.w3.org/2002/07/owl#disjointWith", "http://rdaregistry.info",
//                "http://www.w3.org/2000/01/rdf-schema#label", "http://purl.org");
    }

    /**
     * authorDocumentProperty : http://rdaregistry.info/Elements/a/P50161 |
     * http://rdaregistry.info/Elements/a/P50195
     *
 //    * @throws org.apache.marmotta.ucuenca.wk.authors.exceptions.DaoException
 //    * @throws org.apache.marmotta.ucuenca.wk.authors.exceptions.UpdateException
     */
    //private String documentProperty = "http://rdaregistry.info";
   
    

    @Override
    @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
    public String extractAuthorsGeneric (String... endpoints) {
        
      
        ConcurrentHashMap msg = new ConcurrentHashMap() ;
        for (String endpoint : endpoints) {
            try {
                String queryEndpoint =  queriesService.getListEndpointsByUri (endpoint);
                List<Map<String, Value>> result  = sparqlService.query(QueryLanguage.SPARQL, queryEndpoint);
                  if (!result.isEmpty()) {
                    Map<String, Value> map = result.get(0);
                    String type = map.get("type").stringValue();
                    String status = map.get("status").stringValue();
                    String org = map.get("org").stringValue();
                    String graph = map.get("graph").stringValue();
                    String url = map.get("url").stringValue();
                    Boolean mode = false;
                    if (map.containsKey("mode")) {
                    mode = Boolean.valueOf(map.get("mode").stringValue()); 
                    }
                   // String uri = map.get("URI").stringValue();
                    EndpointObject e;
                    
                     String extractResult ="";
                    if ("file".equals(type)){
                     e = new EndpointFile (status , org , url , type , endpoint);
                   //  EndpointsObject.add(e);
                    extractResult  =  extractAuthorGeneric (e , "0" , false );
            
                    }else if ("sparql".equals(type)) {
                    e = new EndpointSPARQL (status , org , url , type, graph , endpoint);
                    extractResult =   extractAuthorGeneric (e , "1" , false );
                   // EndpointsObject.add(e);
                    }else {
                    e = new EndpointOAI (status , org , url , type , endpoint, mode); 
                    extractResult =  extractAuthorGeneric (e , "1" , mode  );
                     if (extractResult.contains("Success")) {
                        String providerUri = createProvider (OAIPROVNAME,constantService.getAuthorsGraph() , true); 
                         registerDate( org,  providerUri, extractResult , OAIPROVNAME , constantService.getAuthorsGraph());
                     }
                   // EndpointsObject.add(e);
                    }
                    
                      if (extractResult.contains("Success")){
                           Date date = new Date();
                           SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                       endpointService.updateExtractionDate(endpoint, dateFormat.format(date) );
                          // "Success: " + processedAuthors + "/" + totalAuthors
                       
                    }
                    
                    msg.put (endpoint, extractResult);
                    
                  } else {
                   msg.put(endpoint, "Not found");
                  }
                
               // msg.put(endpoint, "Success");
            } catch (MarmottaException ex) {
                java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                msg.put(endpoint, ex);
            } catch (UpdateException ex) {
                java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                 msg.put(endpoint, ex);
            }
        
        }
        try {
            return mapTojson(msg);
        } catch (JSONException ex) {
            java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return "fail";
        }
    }
    
    
    private String createProvider(String providerName , String providerGraph , Boolean main) throws UpdateException {
        String providerUri = constantService.getProviderBaseUri() + "/" + providerName.toUpperCase().replace(" ", "_");
        String queryProvider = queriesService.getAskResourceQuery(providerGraph, providerUri);
        try {
            boolean result = sparqlService.ask(QueryLanguage.SPARQL, queryProvider);
               
            if (!result) {
                 executeInsert( providerGraph, providerUri, RDF.TYPE.toString(), REDI.PROVIDER.toString());
                 executeInsert (providerGraph, providerUri, RDFS.LABEL.toString(), providerName, "string");
                 
                if (main) {
                 //   sparqlFunctionsService.executeInsert(providerGraph, providerUri, REDI.MAIN.toString(), "True", "boolean");
                 executeInsert(providerGraph, providerUri, REDI.MAIN.toString(), "true", "boolean");
                
                } else {
                executeInsert(providerGraph, providerUri, REDI.MAIN.toString(), "false", "boolean");
 
                }
            }

            return providerUri;
        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }
    
     private void registerDate(String org, String providerUri, String detail, String getProviderName , String getProviderGraph) throws UpdateException {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        String uriEvent = createExtractEventUri(getProviderName, org);
        executeInsert (getProviderGraph, uriEvent, RDF.TYPE.toString(), REDI.EXTRACTION_EVENT.toString());
        executeInsert(getProviderGraph, providerUri, REDI.BELONGTO.toString(), uriEvent);
        executeInsert (constantService.getOrganizationsGraph(), org, REDI.BELONGTO.toString(), uriEvent);
        executeInsert(getProviderGraph, uriEvent, REDI.EXTRACTIONDATE.toString(), dateFormat.format(date), STR);
        executeInsert( getProviderGraph, uriEvent, RDFS.LABEL.toString(), dateFormat.format(date) + " | " + detail, STR);

    }
     public void executeInsert (String graph , String subject, String property , String object , String type) throws UpdateException {
       String query = queriesService.buildInsertQuery(graph,subject, property ,object , type );
       sparqlFunctionsService.updateAuthor(query);
     }
     
     public void executeInsert (String graph , String subject, String property , String object) throws UpdateException {
       String query = queriesService.buildInsertQuery(graph,subject, property ,object  );
       sparqlFunctionsService.updateAuthor(query);
     }
     
     private String createExtractEventUri(String providerName, String org) {
        char slash = '/';
         String orgName = org.substring(org.lastIndexOf(slash) + 1);

        return constantService.getEndpointBaseEvent() + providerName.replace(' ', '_') + "_" + orgName.replace(' ', '_');

    }
    
    public String mapTojson(Map<String, String> map) throws JSONException
     {       
        JSONObject jsonObj =new JSONObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            try {
                jsonObj.put(key,value);
            
            } catch (org.json.JSONException ex) {
                java.util.logging.Logger.getLogger(OrganizationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }                           
        }
        return jsonObj.toString();
     }
    
    
    @SuppressWarnings({"PMD.ExcessiveMethodLength","PMD.UnusedPrivateMethod","PMD.AvoidDuplicateLiterals"})
    private String extractAuthorGeneric (EndpointObject endpoint , String min , Boolean mode) {
        int tripletasCargadas = 0; //cantidad de tripletas actualizadaas
        int contAutoresNuevosNoCargados = 0; //cantidad de actores nuevos no cargados
        int contAutoresNuevosEncontrados = 0; //hace referencia a la cantidad de actores existentes en el archivo temporal antes de la actualizacion

        if (endpoint.prepareQuery()){
        log.info("Endpoint listo");
        int authorsSize = 0;
        String query = queriesService.getCountPersonQuery(endpoint.getGraph() , min, mode);
        List<HashMap> result = endpoint.querySource(query);
          if (!result.isEmpty() ) {
             authorsSize =  Integer.parseInt((String)result.get(0).get(COUNTWORD));
          }else {
           return "Problema en las consultas";
          }
            String getAuthorsQuery = queriesService.getAuthorsQuery(endpoint.getGraph() , min , mode);
            
           for (int offset = 0; offset < authorsSize; offset += 5000) {
               
               // TupleQueryResult authorsResult = executelocalquery (getAuthorsQuery + getLimitOffset(LIMIT, offset) , repo );//conn.prepareTupleQuery(QueryLanguage.SPARQL, getAuthorsQuery + getLimitOffset(limit, offset)).evaluate();
                   List<HashMap> listAuthors = endpoint.querySource(getAuthorsQuery+ getLimitOffset(LIMIT, offset));
                    String resource = "";
                 for (HashMap resultmap : listAuthors ) {
                       try {
                           // resource = authorsResult.next().getValue("s").stringValue();
                           resource = resultmap.get("s").toString();
                           
                           if (!sparqlFunctionsService.askAuthor(queriesService.getAskObjectQuery(constantService.getAuthorsGraph(), resource))) {
                               contAutoresNuevosEncontrados++;
                               printPercentProcess(contAutoresNuevosEncontrados, authorsSize, endpoint.getName());
                             //  String localResource = buildLocalURI(resource);
                               String localResource = buildLocalURI (resource , endpoint.getType() , endpoint.getName() );
                               //String   queryAuthor = "Select * where {<"+resource+"> ?y ?z}";
                              // TupleQueryResult tripletasResult =  executelocalquery ( queryAuthor , repo);
                               
                                   List<HashMap> describeAuthor = endpoint.querySource(queriesService.getAuthorsPropertiesQuery(resource));
                                   for (HashMap des: describeAuthor ) {
                                   //BindingSet tripletsResource = tripletasResult.next();                   
                                   String predicate = des.get("property").toString();
                                          // tripletsResource.getValue("y").stringValue();
                                   String object = des.get("object").toString();
                                   log.info (des+"");
                                          // tripletsResource.getValue("z").stringValue();
                                   log.info(predicate +"-"+ object+"-"+des.get("type"));
                                   
                                   String insert = "";
                                   switch (predicate) {
                                       case "http://xmlns.com/foaf/0.1/givenName":// store foaf:firstName
                                       case "http://xmlns.com/foaf/0.1/firstName":
                                           insert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, FOAF.firstName.toString(), object);
                                           sparqlFunctionsService.updateAuthor(insert);
                                           break;
                                       case "http://xmlns.com/foaf/0.1/familyName": // store foaf:lastName
                                       case "http://xmlns.com/foaf/0.1/lastName":
                                           insert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, FOAF.lastName.toString(), object);
                                           sparqlFunctionsService.updateAuthor(insert);
                                           break;
                                       case "http://xmlns.com/foaf/0.1/name": // store foaf:name
                                           insert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, FOAF.name.toString(), object);
                                           sparqlFunctionsService.updateAuthor(insert);
                                           break;
                                        case "http://purl.org/dc/terms/isVersionOf":
                                       insert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, DCTERMS.IS_VERSION_OF.toString() , buildLocalURI( object , endpoint.getType() , endpoint.getName() ) );
                                       sparqlFunctionsService.updateAuthor(insert);
                                       break;
                                       case "http://www.w3.org/2002/07/owl#sameAs": // If sameas found include the provenance
                                           //SparqlEndpoint newEndpoint = matchWithProvenance(object);
                                          /* if (newEndpoint != null) {
                                               String provenanceQueryInsert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, DCTERMS.PROVENANCE.toString(), newEndpoint);
                                               sparqlFunctionsService.updateAuthor(provenanceQueryInsert);
                                           }*/
                                           insert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, OWL.SAMEAS.toString(), object);
                                           sparqlFunctionsService.updateAuthor(insert);
                                           break;
                                       case "http://rdaregistry.info/Elements/a/P50195":
                                           
                                       case "http://rdaregistry.info/Elements/a/P50161":
                                        
                                           if (des.containsKey("type") ){
                                           String type = des.get("type").toString();
                                           createDoc (localResource , object , type , endpoint , predicate);
                                           }
                                           break;
                                       default:
                                   }
                               }
                               
                               String sameAs = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, OWL.SAMEAS.toString(), resource);
                               sparqlFunctionsService.updateAuthor(sameAs);
                               
                               String provenanceQueryInsert = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, DCTERMS.PROVENANCE.toString(), endpoint.getResourceId());
                               sparqlFunctionsService.updateAuthor(provenanceQueryInsert);
                               
                               String foafPerson = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localResource, RDF.TYPE.toString(), FOAF.Person.toString());
                               sparqlFunctionsService.updateAuthor(foafPerson);
                               
                              
                               
                               //conn.commit();
                               //conn.close();

                           }  } catch (    AskException | UnsupportedEncodingException | UpdateException ex) {
                           java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                           return "Fail"+ex;
                           }
                
                }
                
              }     log.info( endpoint.getName() + " . Se detectaron " + contAutoresNuevosEncontrados + " autores nuevos ");
                    log.info( endpoint.getName()+ " . Se cargaron " + (contAutoresNuevosEncontrados - contAutoresNuevosNoCargados) + " autores nuevos exitosamente");
                    log.info( endpoint.getName() + "  . Se cargaron " + tripletasCargadas + " tripletas ");
                    log.info( endpoint.getName() + " . No se pudieron cargar " + contAutoresNuevosNoCargados + " autores");
                 //    List<HashMap> describeAuthor0 = endpoint.querySource("Select * where {?a ?b ?c }limit 100");
                 //    List<HashMap> describeAuthor1 = endpoint.querySource("PREFIX bibo: <http://purl.org/ontology/bibo/> Select * where {?a  a bibo:Document}limit 10");
                   // log.info ("Extrayendo Subjects");
          /*  try {
             //   extractSubjects (endpoint);
            } catch (    RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
                java.util.logging.Logger.getLogger(AuthorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                return "Problema Extrayendo Subjects";
            }*/
                      endpoint.closeconnection();
                 return "Success: "+ authorsSize+"/"+authorsSize;
           } else {
           return "Fail: Access";
           }
    }
    
    
     private String buildLocalURI(String resource, String type, String name ) {
         return constantService.getAuthorResource() +name+"/"+type +"/"+resource.substring(resource.lastIndexOf('/') + 1);
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
     
      private void createDoc(String uri , String object , String type , EndpointObject e , String relation) throws UpdateException, UnsupportedEncodingException {
      if ("http://purl.org/ontology/bibo/Article".equals(type)) {
          
          String query = queriesService.getPublicationDetails(object);
          List<HashMap> describePub = e.querySource(query );
          executeInsert(constantService.getAuthorsGraph(), object , RDF.TYPE.toString() , BIBO.ACADEMIC_ARTICLE.toString() );
          for ( HashMap result: describePub ) {
            String property ="";
            String value = "";
            if (result.containsKey("property")&& result.containsKey("hasValue")){
             property =   result.get("property").toString();
             value =   result.get("hasValue").toString();
           
            switch  (property ){
           case "http://purl.org/ontology/bibo/uri": 
                 executeInsert(constantService.getAuthorsGraph(), object , BIBO.URI.toString() , value );
      
                break;
            case "http://purl.org/dc/terms/abstract": 
                 executeInsert(constantService.getAuthorsGraph(), object , BIBO.ABSTRACT.toString() , value.replaceAll("[&@;^\"\\\\]","") );
      
                break;
            case "http://purl.org/dc/terms/title":
                executeInsert(constantService.getAuthorsGraph(), object, DCTERMS.TITLE.toString() , value.replaceAll("[&@;^\"\\\\]","")  );
             
                break;
            case "http://purl.org/dc/terms/subject":
                 String uriSubject = constantService.getSubjectResource()+URLEncoder.encode(value.toUpperCase().replace(" ", "_"), "UTF-8");
                 executeInsert(constantService.getAuthorsGraph(), object, DCTERMS.SUBJECT.toString() , uriSubject  );
                 executeInsert(constantService.getAuthorsGraph(), uriSubject, RDFS.LABEL.toString() , value.toUpperCase().replaceAll("[&@;^\"\\\\]","") , STR );
                
                break;
            case "http://purl.org/ontology/bibo/issn":
                 executeInsert(constantService.getAuthorsGraph(), object, BIBO.ISSN.toString() , value , "integer" );
            
                break;
            case "http://purl.org/dc/terms/date":
                if (object.matches("^[0-9]+-[0-9]+-[0-9]+")){
                 executeInsert(constantService.getAuthorsGraph(), object, BIBO.ISSUE.toString() , value , "date" );
               
                }
                break;
            default:        
            }
            } 

          }
           String rel;
           if ("http://rdaregistry.info/Elements/a/P50195".equals(relation)) {
            rel = DCTERMS.CREATOR.toString();
            
            } else {
               rel = DCTERMS.CONTRIBUTOR.toString();
            }
            
            executeInsert(constantService.getAuthorsGraph(), uri, FOAF.publications.toString() , object );
            executeInsert(constantService.getAuthorsGraph(), object, rel, uri );
          
      }
    }
     
    
        /*
       private void refineSubjectTopic(String localSubject, Set<String> subjects, Set<String> topic) {
         List listTopic = new ArrayList(topic);
         List<String>[] resultTopics = findTopics(new ArrayList(topic), 5, 15);
        Set<String> selectedSubjects = new HashSet<>(getWeightedSubjects(subjects, listTopic));

        // Insert subjects
        for (String keyword : selectedSubjects) {
            if ((!commonsService.isURI(keyword))) {
                try {
                    String insertKeywords = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localSubject, DCTERMS.SUBJECT.toString(), kservice.cleaningText(keyword).toUpperCase());
                    sparqlFunctionsService.updateAuthor(insertKeywords);
                } catch (UpdateException ex) {
                    log.error("Cannot insert new subjects. Error: {}", ex.getMessage());
                }
            }
        }

        // Insert some topics
        for (Object top  : listTopic ) {
            try {
                String insertTopic = queriesService.buildInsertQuery(constantService.getAuthorsGraph(), localSubject, FOAF.topic.toString(), top.toString().trim().toUpperCase());
                sparqlFunctionsService.updateAuthor(insertTopic);
            } catch (UpdateException ex) {
                log.error("Cannot insert topics. Error: {}", ex.getMessage());
            }
        }
        //log.info("Resource {} has {} documents and {} subjects ", localSubject, documents.size(), selectedSubjects.size());
       
       }*/
  
   
    
    
    
    

    

    public boolean compareExactStrings(String string1, String string2) {
        return (string1.matches("^" + string2 + "$") || string2.matches("^" + string1 + "$"));
    }

    public String cleaningTextAuthor(String value) {
        value = value.replace("??", ".*");
        value = value.replace("?", ".*").toLowerCase();
        value = value.replace(" de ", " ");
        value = value.replace("^del ", " ");
        value = value.replace(" del ", " ");
        value = value.replace(" los ", " ");
        value = value.replace(" y ", " ");
        value = value.replace(" las ", " ");
        value = value.replace(" la ", " ");
        // Cadena de caracteres original a sustituir.
        String original = "áàäéèëíìïóòöúùñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
        String output = value;
        for (int i = 0; i < original.length(); i++) {
            // Reemplazamos los caracteres especiales.
            output = output.replace("" + original.charAt(i), ".*");
        }//end for i
        return output.trim();
    }

    public String removeAccents(String value) {
        value = value.replace(".", "");
        value = value.replace("??", ".*").trim();
        value = value.replace("?", ".*");
        // Cadena de caracteres original a sustituir.
        String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
        // Cadena de caracteres ASCII que reemplazarán los originales.
        String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
        String output = value;
        for (int i = 0; i < original.length(); i++) {
            // Reemplazamos los caracteres especiales.
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }//end for i
        return output;
    }//removeAccents
/*
    private TupleQueryResult executeQuery(Repository repository, String query) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        if (!repository.isInitialized()) {
            repository.initialize();
        }
        RepositoryConnection conn = repository.getConnection();
        conn.begin();
        TupleQueryResult result = conn.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate();
        conn.close();
        return result;
    }*/
    /*
      @Deprecated
     @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Set<String>[] extractSubjectsAndDocuments(LDClientService ldClient, String documentURI)
            throws DataRetrievalException, RepositoryException, MalformedQueryException, QueryEvaluationException {
        Set<String> subjects = new HashSet<>();
        Set<String> documents = new HashSet<>();
        Set<String> mentions = new HashSet<>();

        List<Set<String>> result = new ArrayList<>();
        result.add(subjects);
        result.add(documents);
        result.add(mentions);
        ClientResponse respPub = ldClient.retrieveResource(documentURI);
        String document = "";
        for (Statement statement : respPub.getData()) {
            String value = statement.getObject().stringValue().trim();
            switch (statement.getPredicate().getLocalName()) {
                case "subject":
                    subjects.add(value);
                    break;
                case "mentions":
                    mentions.add(value.substring(value.lastIndexOf('/') + 1).replace("_", " ").toUpperCase().trim());
                    break;
                case "title":
                case "abstract":
                    document += value + " ";
                    break;
                default:
            }
        }
        if (!document.trim().equals("")) {
            documents.add(document);
        }
        return result.toArray(new Set[3]);
    }
/*
    private String buildLocalURI(String endpointURI) {
        return constantService.getAuthorResource() + endpointURI.substring(endpointURI.lastIndexOf('/') + 1);
    }*/
/*
    private List<String>[] findTopics(List<String> documents, int numTopics, int numWords) {
        Set<String> topics = new TreeSet<>();

        //File stoplist = new File(getClass().getClassLoader().getResource("/helpers/stoplist.txt"));
        ArrayIterator iterator = new ArrayIterator(documents);

        ArrayList<Pipe> workflow = new ArrayList<>();
        workflow.add(new CharSequence2TokenSequence("\\p{L}+"));
        workflow.add(new TokenSequenceLowercase());
        workflow.add(new TokenSequenceRemoveStopwords(false, false).addStopWords(stopwords.toArray(new String[]{})));
        workflow.add(new TokenSequence2FeatureSequenceWithBigrams());

        InstanceList data = new InstanceList(new SerialPipes(workflow));
        data.addThruPipe(iterator);

        ParallelTopicModel lda = new ParallelTopicModel(numTopics);
        lda.addInstances(data);
        try {
            lda.estimate();
        } catch (IOException ex) {
            log.error("Cannot find topics. Error: {}", ex.getMessage());
        } catch (ArrayIndexOutOfBoundsException ex) {
            log.error("Cannot find {} topics and {} words. Error: {}", numTopics, numWords, ex.getMessage());
        }

        for (Object[] words : lda.getTopWords(numWords)) {
            for (Object word : words) {
                topics.add(String.valueOf(word));
            }
        }
        Set<String> topicsToStore = new HashSet<>();
        // store 2 for each topic because sometimes words are repeated among topics
        for (Object[] words : lda.getTopWords(2)) {
            for (Object word : words) {
                topicsToStore.add(String.valueOf(word));
            }
        }
        return new List[]{
            new ArrayList<>(topics),
            new ArrayList<>(topicsToStore)};
    }
*/
    /*
     * 
     * @param contAutoresNuevosEncontrados
     * @param allPersons
     * @param endpointName 
     */
    private void printPercentProcess(int contAutoresNuevosEncontrados, int allPersons, String endpointName) {

        if ((contAutoresNuevosEncontrados * 100 / allPersons) != processpercent) {
            processpercent = contAutoresNuevosEncontrados * 100 / allPersons;
            log.info("Procesado el: " + processpercent + " % del Endpoint: " + endpointName);
        }
    }

    private String getLimitOffset(int limit, int offset) {
        return " " + queriesService.getLimit(String.valueOf(limit)) + " " + queriesService.getOffset(String.valueOf(offset));
    }

  


   

   

   

   

}
