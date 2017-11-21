/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.marmotta.ucuenca.wk.pubman.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.vocabulary.DCTERMS;
import org.apache.marmotta.commons.vocabulary.FOAF;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.authors.services.EndpointServiceImpl;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.GetAuthorsGraphData;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.BIBO;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.simmetrics.StringMetric;
import static org.simmetrics.StringMetricBuilder.with;
import org.simmetrics.metrics.CosineSimilarity;
import org.simmetrics.metrics.JaccardSimilarity;
import org.simmetrics.metrics.Levenshtein;
import org.simmetrics.simplifiers.Simplifiers;
import org.simmetrics.tokenizers.Tokenizers;

/**
 *
 * @author joe
 */
public class FindRootAuthor {
    
    @Inject
    private GetAuthorsGraphData getauthorsData;
    @Inject
    private QueriesService queriesService;
    @Inject
    private SparqlService sparqlService;
    
    @Inject
    private ConstantService constantService;
    
    @Inject
    private org.slf4j.Logger log;
    
   @Inject
    private InterfaceProviders interProv;
    
    
    private final static String STR = "^^xsd:string";
    
   public List<Map<String, String>> testData () {
       List <Map<String, String>> listaTest = new ArrayList ();
       Map <String, String> maptest = new HashMap ();
       maptest.put("firstName", "Victor" );
       maptest.put("lastName", "Saquicela" );
       maptest.put("Afiliation", "Universidad de Cuenca" );
       maptest.put("Keywords", "Web semantica, Mineria de datos, RDF " );
       listaTest.add(maptest);
       
       Map <String, String> maptest2 = new HashMap ();
       maptest2.put("firstName", "Victor Hugo" );
       maptest2.put("lastName", "Saquicela Galarza" );
       maptest2.put("Afiliation", "Universidad de Cataluña" );
       maptest2.put("Keywords", "Computer Science, Data mining, RDF " );
        listaTest.add(maptest2);
        
       Map <String, String> maptest3 = new HashMap ();
       maptest3.put("firstName", "Victor H." );
       maptest3.put("lastName", "Saquicela" );
       maptest3.put("Afiliation", "CEDIA" );
       maptest3.put("keywords", "Computer Science, Mineria de datos, RDF , Big data , Data Integration , Integracion de datos, Clustering" );
        listaTest.add(maptest3);
       
       return listaTest;
   }
   
    public List<Map<String, String>> testDataPub () {
       List <Map<String, String>> listaTest = new ArrayList ();
       Map <String, String> maptest = new HashMap ();
        maptest.put("uri", "123456" );
       maptest.put("firstName", "Victor" );
       maptest.put("lastName", "Saquicela" );
       maptest.put("keywords", "Web semantica, Mineria de datos, RDF " );
       listaTest.add(maptest);
       
       Map <String, String> maptest2 = new HashMap ();
       maptest2.put("uri", "789456123" );
       maptest2.put("firstName", "Victor Hugo" );
       maptest2.put("lastName", "Saquicela Galarza" );
       maptest2.put("keywords", "Computer Science, Data mining, RDF " );
        listaTest.add(maptest2);
        
       Map <String, String> maptest3 = new HashMap ();
       maptest3.put("uri", "00000001" );
       maptest3.put("firstName", "Victor H." );
       maptest3.put("lastName", "Saquicela" );
       maptest3.put("keywords", "Semantic Web, Data Integration, Linked data, Web Services,semantic annotations" );
        listaTest.add(maptest3);
       
       return listaTest;
   }
    
    public String testAuthor (String [] organizations) {
    
         Map<String, AuthorsInfo> response = interProv.interfaceAK("http://redi.cedia.edu.ec/resource/authors/UCUENCA/file/UCUENCA%3A_SAQUICELA_GALARZA_____VICTOR_HUGO_");
         
         for (String uri : response.keySet()) {
            log.info(uri+" "+response.get(uri).getIdentifier()+response.get(uri).getAfiliation()[0]);
            log.info(uri+" "+response.get(uri).getName()[0]);
            log.info(uri+" "+response.get(uri).getCoautors()[0]);
            log.info(uri+" "+response.get(uri).getTopics()[0]);
         }
         
        return "Success";
    }
    
    public String   findAuthor (String [] organizations) { 
   // List<Map<String, Value>> resultAllAuthors = getauthorsData.getListOfAuthors( organizations);
        String [] organizationmodif= organizations.clone(); 
        String getAllAuthorsDataQuery = queriesService.getAuthorsDataQuery(organizationmodif);
       List<Map<String, Value>>   resultAllAuthors = new ArrayList();
        try {
            resultAllAuthors = sparqlService.query(QueryLanguage.SPARQL, getAllAuthorsDataQuery);
        } catch (MarmottaException ex) {
            Logger.getLogger(FindRootAuthor.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    Map <String ,Map<String, Value>> orgmap = getOrgsData ( organizations);
    
      for (Map<String, Value> authorMap : resultAllAuthors) {
            
                String firstName = getMap(authorMap , "fname");
                String lastName =  getMap(authorMap , "lname");
                String organization = getMap(authorMap , "organization");
                      //  authorMap.get("fname").stringValue();
                //String lastName = authorMap.get("lname").stringValue();
                //String organization = authorMap.get("organization").stringValue();
               
                Map<String, Value> organizationmap =  orgmap.get(organization);
                
                String OrgNameEs = getMap (organizationmap , "fullNameEs" );
                String OrgNameEn = getMap (organizationmap , "fullNameEn" );
                String OrgPrefix = getMap (organizationmap , "name" );
                /*String OrgNameEs = organizationmap.get("fullNameEs").stringValue();
                String OrgNameEn = organizationmap.get("fullNameEn").stringValue();
                String OrgPrefix = organizationmap.get("name").stringValue();*/
                    
                List<Map<String, String>> Ltest = testData ();
                List <Map<String, String>> candidates = new ArrayList();
                String OriginalUri = (firstName+"_"+lastName).replace(" ","_").toUpperCase();
                
                for (Map<String, String> maptest : Ltest ) {
                    String organizationCandidate = getMap (maptest , "Afiliation" );
                if (compareAffiliation (OrgNameEs , organizationCandidate , 0.9 ) || compareAffiliation (OrgNameEn ,organizationCandidate , 0.9 ) || compareAffiliation (OrgPrefix , organizationCandidate , 0.9 ) )
                {  String [] original = {firstName, lastName};
                   
                   String cfname = getMap (maptest , "firstName" ); 
                   String clastname = getMap (maptest , "lastName" );  
                   
                   String [] candidate = { cfname , clastname };
                   boolean authorRoot = compareAuthorsNames (original , candidate , 0.6);
                   
                   if (authorRoot) {
                   candidates.add(maptest);
                    String candidateUri =   (cfname+"_"+clastname).replace(" ", "_") ;
                            
                     if (candidateUri.length() > OriginalUri.length() ) {
                                OriginalUri = candidateUri;
                                
                     }
                  // createAuthor ("","" );
                   }
                }
                
                }
                
                if (!candidates.isEmpty()) {
                    try {
                        // Inserto datos de original
                        String authorURI = buildURI (OriginalUri , OrgPrefix);
                        if (!askAuthor (authorURI)){
                        insertStatement(authorURI, RDF.TYPE.toString(), FOAF.Person.toString() , STR);
  
                        insertStatement(authorURI, RDFS.LABEL.toString(), OriginalUri.replace("_", " ") , STR);
                        }
                        
                        insertStatement(authorURI, FOAF.givenName.toString() , firstName , STR);
                        insertStatement(authorURI, FOAF.familyName.toString(), lastName , STR);
                      
                        //Insertar datos de candidatos
                        List <String> keywordsGlobal = new ArrayList ();
                        for (Map<String, String> mapcandidates : candidates ) {
                            
                             insertStatement(authorURI, FOAF.givenName.toString() , getMap (mapcandidates , "firstName"), STR) ;
                             insertStatement(authorURI, FOAF.familyName.toString(),  getMap (mapcandidates , "lastName")  , STR);
                           
                            List <String>  keywordsList = Arrays.asList( getMap (mapcandidates , "keywords").toUpperCase().split(","));
                            keywordsGlobal.addAll(keywordsList);
                            //List<String> newList = new ArrayList<String>(new HashSet<String>(keywords));
                            
                        }
                        
                         generateSubjects (keywordsGlobal ,authorURI );
                     
                        publicationsEnrichment (authorMap , keywordsGlobal ,authorURI );
                        return "Success";
                        
                    }  catch (UpdateExecutionException ex) {
                        Logger.getLogger(FindRootAuthor.class.getName()).log(Level.SEVERE, null, ex);
                        return "Fail"+ex;
                    }
                        
                }
                
                
              
    } 
        return "Fail";
    }
    
    public  List<Map<String, Value>> getOrgData (String organization) {
        try {
            String queryOrg =  queriesService.getOrgByUri (organization);
            return sparqlService.query(QueryLanguage.SPARQL, queryOrg);
        } catch (MarmottaException ex) {
            Logger.getLogger(FindRootAuthor.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    
    }
    
    public Map <String ,Map<String, Value>> getOrgsData (String [] organizations) {
        Map <String ,Map<String, Value>> orgMap = new HashMap();
        for (String org: organizations) {
          Map<String, Value> orgResponse = getOrgData (org).get(0);
          orgMap.put(org, orgResponse);
        }
    return orgMap;
    }
    
    public boolean compareAffiliation (String original , String candidate , double umbral) {
        if (original != null && candidate != null){
        String a = original;
        String b = candidate;

        StringMetric metric
                = with(new CosineSimilarity<String>())
                .simplify(Simplifiers.toLowerCase())
                .simplify(Simplifiers.removeNonWord()).simplifierCache()
               .tokenize(Tokenizers.qGram(3)).tokenizerCache().build();
        float compare = metric.compare(a, b);  
        
     return  compare > umbral ;
        }else { return false;}
    }

    private boolean compareAuthorsNames(String[] original, String[] candidate, double d) {
     
        StringMetric metric
                = with(new JaccardSimilarity<String>())
                .simplify(Simplifiers.toLowerCase()).tokenize(Tokenizers.whitespace())
		.build();
              float compare1 = metric.compare(original[0], candidate[0]);  
              float compare2 = metric.compare(original[1], candidate[1]);
              float result = (compare1+compare2) / 2;
              return  result > d ;
        
// throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String buildURI(String OriginalUri, String OrgPrefix) {
       return constantService.getAuthorResource()+OrgPrefix+"/"+OriginalUri;    
    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
       private String  insertStatement(String... parameters) throws UpdateExecutionException {
        try {
           // String queryInsertEndpoint = queriesService.getInsertEndpointQuery(parameters[0], parameters[1], parameters[2], parameters[3]);
          String queryInsertOrg = queriesService.getInsertGeneric (constantService.getCentralGraph() ,  parameters[0], parameters[1], parameters[2], parameters[3] );
            sparqlService.update(QueryLanguage.SPARQL, queryInsertOrg);
            return "Successfully Registration";
        } catch (InvalidArgumentException | MarmottaException | MalformedQueryException | UpdateExecutionException ex) {
            Logger.getLogger(FindRootAuthor.class.getName()).log(Level.SEVERE, null, ex);
            return "Fail Insert";
        }
    }

    private void generateSubjects(List<String> keywordsGlobal, String authorURI) throws UpdateExecutionException {
         for (String key : keywordsGlobal) {
             String subjectURI = buildURISubject (key) ;
             insertStatement(subjectURI , RDFS.LABEL.toString() , key  , STR);
             insertStatement(authorURI , FOAF.topic_interest.toString() , subjectURI  , STR);
         }
         
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private String buildURISubject (String subject) {
       return constantService.getSubjectResource()+StringUtils.stripAccents(subject.trim().toUpperCase().replace(" ", "_"));
    }
    
        public boolean  askAuthor (String uri )  {
            String askOrg =  queriesService.getAskResourceQuery (constantService.getCentralGraph(), uri);
         try {
             return  sparqlService.ask( QueryLanguage.SPARQL , askOrg );
         } catch (MarmottaException ex) {
             Logger.getLogger(FindRootAuthor.class.getName()).log(Level.SEVERE, null, ex);
             //return ;
             return false;
         }
         
              
          }


    private void publicationsEnrichment(Map<String, Value> authorMap, List<String> keywordsGlobal, String authorURI) throws UpdateExecutionException {
                String firstName = getMap (authorMap, "fname" );
                String lastName = getMap (authorMap, "lname" ); 
                String organization = getMap (authorMap, "organization" ); 
                
               List<Map<String, String>> mapOthersProviderList = testDataPub();
               List<Map<String, String>> publicationListGlobal = new ArrayList(); 
               for ( Map<String, String> mapEnrichment : mapOthersProviderList ){
                   String [] original = {firstName, lastName};
                   String [] candidate = {getMap (mapEnrichment, "firstName" )  , getMap (mapEnrichment, "lastName" ) };
                   boolean authorCandidateName = compareAuthorsNames (original , candidate , 0.5);
                   if (authorCandidateName) {
                       
                    boolean subjectsim = compareSubjectSimilarity ( keywordsGlobal , Arrays.asList(getMap (mapEnrichment, "keywords").toUpperCase().split(",")), 0.2);
                    
                    if (subjectsim) {
                       
                            insertStatement(authorURI, FOAF.givenName.toString() , getMap(mapEnrichment,"firstName") , STR);
                            insertStatement(authorURI, FOAF.familyName.toString(), getMap(mapEnrichment, "lastName") , STR);
                       
                          List <Map<String, String>> publicationList = publicationsQuery (getMap(mapEnrichment,"uri"));
                          publicationListGlobal.addAll(publicationList);  
                    } 
                   }
               }
               
               generatePublications (publicationListGlobal , authorURI );
    }

    private boolean compareSubjectSimilarity(List<String> keywordsGlobal, List<String> asList, double d) {
         StringMetric metric
                = with(new JaccardSimilarity<String>())
                .simplify(Simplifiers.toLowerCase()).tokenize(Tokenizers.whitespace())
		.build();
                float result = 0;
               for (String key1 :keywordsGlobal) {
                    for (String key2 : asList ) {
                      float compare1 = metric.compare(key1, key2); 
                         result += compare1;
                               }
               }  
               int umbral = 0  ;
               umbral = keywordsGlobal.size() > asList.size() ?  keywordsGlobal.size()  :  asList.size() ;
                    
               
            //  float compare1 = metric.compare(keywordsGlobal, asList);  
              return  result/umbral > d ;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private List <Map<String, String>> publicationsQuery(String uriProfile) {
         List <Map<String, String>> listaTest = new ArrayList ();
       Map <String, String> maptest = new HashMap ();
        maptest.put("uri", "123456" );
       maptest.put("title", "Plataforma de anotacion ...." );
       maptest.put("sbn", "50221" );
       maptest.put("abstract", "mi tesis" );
       maptest.put("keywords", "Web semantica, Mineria de datos, RDF " );
       maptest.put("relType", "creator" );
       listaTest.add(maptest);
       
       Map <String, String> maptest2 = new HashMap ();
       maptest2.put("uri", "789456123" );
       maptest2.put("title", "Patito Lee" );
       maptest2.put("sbn", "147852" );
       maptest2.put("keywords", "Computer Science, Data mining, RDF " );
       maptest2.put("abstract", "Libro de patitos" );
       maptest2.put("relType", "contributor" );
        listaTest.add(maptest2);
        
       Map <String, String> maptest3 = new HashMap ();
       maptest3.put("uri", "00000001" );
       maptest3.put("title", "Che guevata" );
       maptest3.put("sbn", "8998878" );
       maptest3.put("abstract", "robolucionario" );
       maptest3.put("keywords", "Semantic Web, Data Integration, Linked data, Web Services,semantic annotations" );
       maptest3.put("relType", "contributor" );
        listaTest.add(maptest3);
       
       return listaTest;
    }

    private void generatePublications(List<Map<String, String>> publicationListGlobal, String authorURI) {
         for ( int i = 0 ; i < publicationListGlobal.size() ; i++) {
             try {
                 Map<String, String> actual = publicationListGlobal.get(i);
                 String title1 = getMap(actual, "title");
                 String pubURI = getPublicationURI (title1);
                 createPublication (pubURI , actual , authorURI);
                 for ( int j = 1; j < publicationListGlobal.size() ;j++) {
                     Map<String, String> next = publicationListGlobal.get(j);
                     String title2 = getMap(next,"title");
                     
                     if (compareTitlePublicationWithSimmetrics (title1 , title2)) {
                         createPublication (pubURI , next , authorURI);
                         publicationListGlobal.remove(j);
                     }
                 }
             } catch (UpdateExecutionException ex) {
                 Logger.getLogger(FindRootAuthor.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
    }
    
     private boolean compareTitlePublicationWithSimmetrics(String publicationResourceOne, String publicationResourceTwo) {

        String a = publicationResourceOne;
        String b = publicationResourceTwo;

        StringMetric metric
                = with(new CosineSimilarity<String>())
                .simplify(Simplifiers.toLowerCase())
                .simplify(Simplifiers.removeNonWord()).simplifierCache()
                .tokenize(Tokenizers.qGram(3)).tokenizerCache().build();
        float compare = metric.compare(a, b);

        StringMetric metric2
                = with(new Levenshtein())
                .simplify(Simplifiers.removeDiacritics())
                .simplify(Simplifiers.toLowerCase()).build();

        float compare2 = metric2.compare(a, b);

    

        float similarity = (float) ((compare + compare2) / 2.0);
        //log.info("Titulos " + publicationResourceOne + "," + publicationResourceTwo + ": similaridad " + similarity * 100 + "%");

     
        return similarity > 0.9; // 0.8131
    }

    private String  getPublicationURI(String title1) {
        return constantService.getPublicationResource()+title1.trim().toUpperCase().replace(" ", "_");
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void createPublication(String pubURI, Map<String, String> actual , String authorURI) throws UpdateExecutionException {
         insertStatement(pubURI, RDF.TYPE.toString() , BIBO.ACADEMIC_ARTICLE.toString() , STR);
       //  insertStatement(pubURI,  DCTERMS.title.toString(), actual.get("title") , STR);
         
          Map<String, String> mapper = loadProperties("publicationConfig");
         
         for (Entry prop : actual.entrySet()) {
           //  prop.
            if (mapper.containsKey(prop.getKey())){
                insertStatement(pubURI,mapper.get(prop.getKey()) , getMap(actual, prop.getKey().toString() ) , STR);
            //Asocia las publicaciones con los autores
            }else if ("relType".equals(prop.getKey())) {
                if ("creator".equals(prop.getValue())) {
            insertStatement(authorURI, DCTERMS.creator.toString() , pubURI , STR);
            } else if ("contributor".equals(prop.getValue())) {
            insertStatement(authorURI, DCTERMS.contributor.toString() , pubURI , STR);
            }
            } else if ("keywords".equals(prop.getKey())) {
                List <String> keyList = new ArrayList();
                String keywords =  getMap(actual, prop.getKey().toString() );
                if (keywords.contains(",")){ 
                keyList = Arrays.asList(keywords.split(",")) ;
                }else {
                keyList.add(keywords);
                }
               
                generateSubjects( keyList ,  pubURI);
            }
              
         }
         
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private Map<String, String> loadProperties(String filename) {
        Properties propiedades = new Properties();
        Map<String, String> mapping = new HashMap<>();

        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream entrada = classLoader.getResourceAsStream(filename + ".properties");) {
            // cargamos el archivo de propiedades
            propiedades.load(entrada);
            for (String provider : propiedades.stringPropertyNames()) {
                String target = propiedades.getProperty(provider);
                mapping.put(provider.replace("..", ":"), target.replace("..", ":"));
            }
        } catch (IOException ex) {
            log.error("Error: check the properties file. {}", ex);
        }
        return mapping;
    }
    
    
    private String  getMap (Map m , String atr) {
        
         Object result =   m.get(atr);
         
         if (result != null){
             if (result instanceof Value) {
                return ((Value)result).stringValue();
             }else
             {
             return result.toString();
             }
         }else  {
         return "";
         }
     
    }

}