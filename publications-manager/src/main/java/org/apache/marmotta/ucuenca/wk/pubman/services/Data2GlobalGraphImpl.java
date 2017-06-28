/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.vocabulary.FOAF;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.DistanceService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.Data2GlobalGraph;
import org.apache.marmotta.ucuenca.wk.pubman.api.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.BIBO;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.semarglproject.vocab.OWL;
import org.semarglproject.vocab.RDF;
import org.simmetrics.StringMetric;
import static org.simmetrics.StringMetricBuilder.with;
import org.simmetrics.metrics.CosineSimilarity;
import org.simmetrics.metrics.Levenshtein;
import org.simmetrics.simplifiers.Simplifiers;
import org.simmetrics.tokenizers.Tokenizers;
import org.slf4j.Logger;
import static org.simmetrics.StringMetricBuilder.with;

/**
 *
 * @author Satellite
 */
@ApplicationScoped
public class Data2GlobalGraphImpl implements Data2GlobalGraph, Runnable {
    
    @Inject
    private Logger log;
    
    @Inject
    private QueriesService queriesService;
    
    @Inject
    private ConstantService constant;
    
    @Inject
    private CommonsServices commonsServices;
    
    @Inject
    private DistanceService distanceService;
    
    @Inject
    private SparqlFunctionsService sparqlFunctionsService;
    
    private String namespaceGraph = "http://ucuenca.edu.ec/";
    private String wkhuskaGraph = namespaceGraph + "wkhuska";
    private String uriPublication = "http://ucuenca.edu.ec/wkhuska/publication/";
    private String bibloTitle = "http://purl.org/dc/terms/title";
    private String publicationOntology = "http://purl.org/ontology/bibo/Article";
    private String uriNewAuthor = "http://ucuenca.edu.ec/resource/author/";
    private double total = 0;
    private double totalPublicationRecognized = 0;
    private double totalPublicationNotRecognized = 0;
    private double totalPublications = 0;
    private long totalPublicationsProcess = 0;
    private double problemWithTitle = 0;
    private boolean newInsert = false;
    private String bufferTitle = null;
    private int countPublicationAskIngnored = 0;
    private List<String> results;
    private String authorsGraph = "http://ucuenca.edu.ec/wkhuska/authors";
    
    private int processpercent = 0;
    private boolean comparacionSemantica = false;

    /* graphByProvider
     Graph to save publications data by provider
     Example: http://ucuenca.edu.ec/wkhuska/dblp
     */
    private String graphByProviderNS = wkhuskaGraph + "/provider/";
    
    @Inject
    private SparqlService sparqlService;
    
    @Override
    public String LoadData2GlobalGraph() {
        try {
            results = new ArrayList<String>();
            
            String providerGraph = "";
            String getGraphsListQuery = queriesService.getGraphsQuery();
            List<Map<String, Value>> resultGraph = sparqlService.query(QueryLanguage.SPARQL, getGraphsListQuery);
            /* FOR EACH GRAPH*/
            
            for (Map<String, Value> map : resultGraph) {
                providerGraph = map.get("grafo").toString();
                KiWiUriResource providerGraphResource = new KiWiUriResource(providerGraph);
                String propertyProviderTitle = "";
                String propertyTargetTitle = "";
                if (providerGraph.contains("provider")) {
                    Map<String, String> mapper = loadProperties(providerGraphResource.getLocalName());
                    for (Map.Entry<String, String> value : mapper.entrySet()) {
                        if (value.getKey().contains("title")) {
                            propertyProviderTitle = value.getKey().replace("..", ":");
                            propertyTargetTitle = value.getValue().replace("..", ":");
                        }
                    }
                    
                    List<Map<String, Value>> auxPublications = sparqlService.query(QueryLanguage.SPARQL, queriesService.getPublicationsTitleScopusQuery(providerGraph, propertyProviderTitle));
                    List<Map<String, Value>> auxPublications2 = sparqlService.query(QueryLanguage.SPARQL, queriesService.getPublicationsTitleByType(providerGraph, "foaf:publications"));
                    List<Map<String, Value>> result = sparqlService.query(QueryLanguage.SPARQL, queriesService.getPublicationsTitleScopusQuery(providerGraph, propertyTargetTitle));
                    List<Map<String, Value>> resultPublications = auxPublications.isEmpty() ? result.isEmpty() ? auxPublications2 : result : auxPublications;
                    
                    results.add(providerGraph + " :size :" + resultPublications.size());
                    totalPublicationsProcess = 0;
                    for (Map<String, Value> pubresource : resultPublications) {
                        totalPublicationsProcess += 1;
                        String authorResource = pubresource.get("authorResource").stringValue();
                        String publicationResource = pubresource.get("publicationResource").stringValue();
                        String publicationTitleCleaned = cleanStringUri(pubresource.get("title").stringValue());
                        String publicationTitle = StringEscapeUtils.unescapeJava(pubresource.get("title").stringValue());
                        String publicationProperty = constant.getPubProperty();
                        totalPublications += 1;

                        // asint SameAs between newUri publication and Uri of provider graph
                        String insertPublicationPropertySameAs = buildInsertQuery(constant.getCentralGraph(), constant.getPublicationResource() + publicationTitleCleaned, OWL.SAME_AS, publicationResource);
                        try {
                            sparqlService.update(QueryLanguage.SPARQL, insertPublicationPropertySameAs);
                            
                        } catch (MalformedQueryException ex) {
                            log.error("Malformed Query:  " + insertPublicationPropertySameAs);
                        } catch (UpdateExecutionException ex) {
                            log.error("Update Query:  " + insertPublicationPropertySameAs);
                        } catch (MarmottaException ex) {
                            log.error("Marmotta Exception:  " + insertPublicationPropertySameAs);
                        }

                        //verificar existencia de la publicacion y su author sobre el grafo general, y que la nueva uri este asignada la pub. al autor
                        String newUriAuthorCentral = buildNewUri(authorResource);//adds the new author to the central graph if it is not already there
                        String askTripletQuery = queriesService.getAskQuery(constant.getCentralGraph(), newUriAuthorCentral, FOAF.publications.toString(), constant.getPublicationResource() + publicationTitleCleaned);
                        boolean ask = false;
                        try {
                            //asks if the new author has the publication in the central graph
                            ask = sparqlService.ask(QueryLanguage.SPARQL, askTripletQuery);
                        } catch (Exception ex) {
                            log.error("Marmotta Exception:  " + askTripletQuery);
                            
                            problemWithTitle += 1;
                            continue;
                            
                        }
                        
                        if (!ask) {//Si no se encuentra el autor con esa publicacion, busca la publicacion actual del autor en el grafo central
                            List<Map<String, Value>> resultPublicationsAuthor = sparqlService.query(QueryLanguage.SPARQL, queriesService.getAuthorPublicationsQuery(constant.getCentralGraph(), newUriAuthorCentral, DCTERMS.TITLE.toString(), getQuerySearchTextAuthor(publicationTitle)));
                            List<Map<String, Value>> auxResultPublicationsAuthorOfProvider = sparqlService.query(QueryLanguage.SPARQL, queriesService.getAuthorPublicationsQueryFromProvider(providerGraph, authorResource, propertyProviderTitle, getQuerySearchTextAuthor(publicationTitle)));
                            List<Map<String, Value>> resultPublicationsAuthorOfGenericProvider = sparqlService.query(QueryLanguage.SPARQL, queriesService.getAuthorPublicationsQueryFromGenericProvider(providerGraph, authorResource, propertyTargetTitle, getQuerySearchTextAuthor(publicationTitle)));
                            List<Map<String, Value>> resultPublicationsAuthorOfTargetProvider = sparqlService.query(QueryLanguage.SPARQL, queriesService.getAuthorPublicationsQueryFromProvider(providerGraph, authorResource, propertyTargetTitle, getQuerySearchTextAuthor(publicationTitle)));
                            List<Map<String, Value>> resultPublicationsAuthorOfProvider = auxResultPublicationsAuthorOfProvider.isEmpty() ? resultPublicationsAuthorOfTargetProvider.isEmpty() ? resultPublicationsAuthorOfGenericProvider : resultPublicationsAuthorOfTargetProvider : auxResultPublicationsAuthorOfProvider;
                            
                            boolean flagPublicationAlreadyExist = false;
                            String authorResourceBuilding = searchAuthorOfpublication(resultPublicationsAuthorOfProvider, authorResource, newUriAuthorCentral);
                            String authorResourceCentral = authorResourceBuilding == null ? newUriAuthorCentral : authorResourceBuilding;
                            String sameAsInsertQuery = buildInsertQuery(constant.getCentralGraph(), newUriAuthorCentral, OWL.SAME_AS, authorResource);
                            try {
                                sparqlService.update(QueryLanguage.SPARQL, sameAsInsertQuery);
                            } catch (MalformedQueryException ex) {
                                java.util.logging.Logger.getLogger(Data2GlobalGraphImpl.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (UpdateExecutionException ex) {
                                java.util.logging.Logger.getLogger(Data2GlobalGraphImpl.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            //Si la publicacion no esta en el grafo central
                            for (Map<String, Value> publicacion : resultPublicationsAuthor) {
                                if (compareTitlePublicationWithSimmetrics(publicationTitleCleaned, cleanStringUri(publicacion.get("title").stringValue()))) {
                                    flagPublicationAlreadyExist = true;
                                    bufferTitle = publicacion.get("publicationResource").stringValue();
                                    String insertPublicationPropertyQuery = buildInsertQuery(wkhuskaGraph, bufferTitle, "http://purl.org/dc/terms/contributor", authorResourceCentral);
                                    try {
                                        sparqlService.update(QueryLanguage.SPARQL, insertPublicationPropertyQuery);
                                        
                                    } catch (MalformedQueryException ex) {
                                        log.error("Malformed Query:  " + insertPublicationPropertyQuery);
                                    } catch (UpdateExecutionException ex) {
                                        log.error("Update Query:  " + insertPublicationPropertyQuery);
                                    } catch (MarmottaException ex) {
                                        log.error("Marmotta Exception:  " + insertPublicationPropertyQuery);
                                    }
                                    
                                }
                                
                            }
                            if (!flagPublicationAlreadyExist || resultPublicationsAuthor.isEmpty()) {
                                //semanticComparison 
                                insertPublicationToCentralGraph(authorResourceCentral, publicationProperty, constant.getPublicationResource() + publicationTitleCleaned);
                                String queryKeysAut = "PREFIX dct: <http://purl.org/dc/terms/> "
                                        + "SELECT DISTINCT ?value WHERE {"
                                        + "  Graph <" + constant.getAuthorsGraph() + ">"
                                        + "  { <" + authorResource + ">  dct:subject ?value }"
                                        + "} Limit 10 ";
                                List<Map<String, Value>> keywordsAut = sparqlService.query(QueryLanguage.SPARQL, queryKeysAut);
                                List<String> keyAut = new ArrayList<>();
                                for (Map<String, Value> key : keywordsAut) {
                                    keyAut.add(key.get("value").stringValue());
                                }
                                String queryKeyPub = " SELECT DISTINCT ?publicationPropertyValue "
                                        + "WHERE {  "
                                        + "  GRAPH <http://ucuenca.edu.ec/wkhuska/provider/ScopusProvider>  { "
                                        + "    <" + publicationResource + "> <http://prismstandard.org/namespaces/basic/2.0/keyword> ?publicationPropertyValue.  } "
                                        + "} "
                                        + "Limit 10 ";
                                List<Map<String, Value>> keywordsPub = sparqlService.query(QueryLanguage.SPARQL, queryKeyPub);
                                List<String> keyPub = new ArrayList<>();
                                for (Map<String, Value> key : keywordsPub) {
                                    keyPub.add(key.get("publicationPropertyValue").stringValue());
                                }
                                boolean semanticComp = true;
                                if (keyAut.size() > 4 && keyPub.size() > 4 && comparacionSemantica) {
                                    semanticComp = distanceService.semanticComparison(keyAut, keyPub);
                                }
                                if (semanticComp) {
                                    String insertSourceOfPublication = buildInsertQuery(constant.getCentralGraph(), constant.getPublicationResource() + publicationTitleCleaned, DCTERMS.PROVENANCE.toString(), getNameOfProvider(providerGraph));
                                    try {
                                        sparqlService.update(QueryLanguage.SPARQL, insertSourceOfPublication);
                                    } catch (MalformedQueryException ex) {
                                        java.util.logging.Logger.getLogger(Data2GlobalGraphImpl.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (UpdateExecutionException ex) {
                                        java.util.logging.Logger.getLogger(Data2GlobalGraphImpl.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                } else {
                                    countPublicationAskIngnored += 1;
                                }
                                newInsert = true;
                            }
                        } else {
                            countPublicationAskIngnored += 1;
                            
                        }
                        //Pregunta si la publicacion fue extraida del provider que se esta usando actualmente (Scopus, DBLP)
                        String askTripletProcessPublicationQuery = queriesService.getAskQuery(constant.getCentralGraph(), constant.getPublicationResource() + publicationTitleCleaned, DCTERMS.PROVENANCE.toString(), getNameOfProvider(providerGraph));
                        boolean askPublication = false;
                        try {
                            
                            askPublication = sparqlService.ask(QueryLanguage.SPARQL, askTripletProcessPublicationQuery);
                            if (!askPublication || newInsert) {
                                List<Map<String, Value>> resultPubProperties = sparqlService.query(QueryLanguage.SPARQL, queriesService.getPublicationsPropertiesQuery(providerGraph, publicationResource));
                                resultPubProperties = resultPubProperties.size() > 150 ? resultPubProperties.subList(0, 150) : resultPubProperties;
                                for (Map<String, Value> pubproperty : resultPubProperties) {
                                    String nativeProperty = pubproperty.get("publicationProperties").toString();
                                    if (mapper.get(nativeProperty) != null) {
                                        String newPublicationProperty = mapper.get(nativeProperty);
                                        String publicacionPropertyValue = StringEscapeUtils.unescapeJava(pubproperty.get("publicationPropertyValue").stringValue());
                                        
                                        String key = "", resource = "", newuri;
                                        boolean uri = false;
                                        if (nativeProperty.equals(mapper.get("journal"))) {
                                            key = "journal";
                                            resource = constant.getJournalResource();
                                            uri = true;
                                        } else if (nativeProperty.equals(mapper.get("conference"))) {
                                            key = "conference";
                                            resource = constant.getConferenceResource();
                                            uri = true;
                                        } else if (nativeProperty.equals(mapper.get("publisher"))) {
                                            key = "publisher";
                                            resource = constant.getPublisherResource();
                                            uri = true;
                                        } else if (nativeProperty.equals(mapper.get("creator"))) {
                                            key = "creator";
                                            resource = constant.getAuthorResource();
                                            uri = true;
                                        } else if (nativeProperty.equals(mapper.get("contributor"))) {
                                            key = "contributor";
                                            resource = constant.getAuthorResource();
                                            uri = true;
                                        } else if (nativeProperty.equals(mapper.get("book"))) {
                                            key = "book";
                                            resource = constant.getBookResource();
                                            uri = true;
                                        } else if (nativeProperty.equals(mapper.get("subject"))) {
                                            key = "subject";
                                            resource = constant.getSubjectResource();
                                            uri = true;
                                        } else if (nativeProperty.equals(mapper.get("topic"))) {
                                            key = "topic";
                                            resource = constant.getTopicResource();
                                            uri = true;
                                        }
                                        
                                        if (uri) {
                                            if (commonsServices.isURI(publicacionPropertyValue)) {
                                                String name = sparqlService
                                                        .query(QueryLanguage.SPARQL, queriesService.getObjectByPropertyQuery(providerGraph, publicacionPropertyValue, mapper.get(key + ".name.provider")))
                                                        .get(0).get("object").stringValue();
                                                newuri = resource + cleanStringUri(name.replace(".", ""));
                                                
                                                List<Map<String, Value>> resultAuthorName = sparqlService.query(QueryLanguage.SPARQL, queriesService.detailsOfProvenance(providerGraph, publicacionPropertyValue));
                                                
                                                for (Map<String, Value> values : resultAuthorName) {
                                                    if (values.containsKey("property") && values.containsKey("hasValue")) {
                                                        String property = mapper.get(key + "." + values.get("property").stringValue());
                                                        String hasvalue = values.get("hasValue").stringValue();
                                                        boolean existType = RDF.TYPE.equals(property);
                                                        
                                                        if (property != null && !existType) {
                                                            sparqlService.update(QueryLanguage.SPARQL, buildInsertQuery(constant.getCentralGraph(), newuri, property, hasvalue));
                                                        }
                                                    }
                                                }
                                                if (mapper.get(key + ".type") != null) {
                                                    sparqlService.update(QueryLanguage.SPARQL, buildInsertQuery(constant.getCentralGraph(), newuri, RDF.TYPE, mapper.get(key + ".type")));
                                                }
                                            } else {
                                                newuri = resource + cleanStringUriAuthor(publicacionPropertyValue.replace(".", ""));
                                                if (mapper.get(key + ".type") != null) {
                                                    sparqlService.update(QueryLanguage.SPARQL, buildInsertQuery(constant.getCentralGraph(), newuri, RDF.TYPE, mapper.get(key + ".type")));
                                                }
                                                sparqlService.update(QueryLanguage.SPARQL, buildInsertQuery(constant.getCentralGraph(), newuri, mapper.get(key + ".name"), publicacionPropertyValue));
                                            }
                                            publicacionPropertyValue = newuri;
                                        }
                                        
                                        if (BIBO.DOCUMENT.stringValue().equals(publicacionPropertyValue)) {
                                            publicacionPropertyValue = BIBO.ACADEMIC_ARTICLE.stringValue();
                                        }
                                        
                                        String insertPublicationPropertyQuery = buildInsertQuery(constant.getCentralGraph(), newInsert ? (constant.getPublicationResource() + publicationTitleCleaned) : bufferTitle == null ? (constant.getPublicationResource() + publicationTitleCleaned) : bufferTitle, newPublicationProperty, publicacionPropertyValue);
                                        
                                        try {
                                            sparqlService.update(QueryLanguage.SPARQL, insertPublicationPropertyQuery);
                                        } catch (MalformedQueryException ex) {
                                            log.error("Malformed Query:  " + insertPublicationPropertyQuery);
                                        } catch (UpdateExecutionException ex) {
                                            log.error("Update Query:  " + insertPublicationPropertyQuery);
                                        } catch (MarmottaException ex) {
                                            log.error("Marmotta Exception:  " + insertPublicationPropertyQuery);
                                        } catch (Exception ex) {
                                            log.error("Error: {}", ex);
                                        }
                                    } else {
                                        log.info("CATCH: {}", nativeProperty);
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            log.error("Marmotta Exception:  " + ex);
                        }

                        //compare properties with the mapping and insert new properties
                        //mapping.get(map)
                        newInsert = false;
                        bufferTitle = null;
                        log.info("Integration process is: " + totalPublicationsProcess + " of : " + resultPublications.size() + " - from provider:" + providerGraphResource.getLocalName());
                        
                    }
                }
                //in this part, for each graph
            }
            for (String aux : results) {
                log.info(aux);
            }
            log.info("Publication ignored total: " + problemWithTitle);
            log.info("Publication repository total: " + totalPublications);
            log.info("Publication process recognition total: " + total);
            log.info("Publication total Recognized: " + totalPublicationRecognized);
            log.info("Publication total Not Recognized: " + totalPublicationNotRecognized);
            log.info("Publication total ASK ignored: " + countPublicationAskIngnored);
            
            return "Los datos de las publicaciones se han cargado exitosamente.";
        } catch (InvalidArgumentException ex) {
            return "error:  " + ex;
        } catch (MarmottaException ex) {
            return "error:  " + ex;
        }
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

    //construyendo sparql query insert 
    private String buildInsertQuery(String grapfhProv, String sujeto, String predicado, String objeto) {
        if (commonsServices.isURI(objeto)) {
            return queriesService.getInsertDataUriQuery(grapfhProv, sujeto, predicado, objeto);
        } else {
            return queriesService.getInsertDataLiteralQuery(grapfhProv, sujeto, predicado, objeto);
        }
    }
    
    @Override
    public void run() {
        LoadData2GlobalGraph();
    }
    
    private void insertPublicationToCentralGraph(String authorResource, String publicationProperty, String publicationResource) {
        String insertPubQuery = buildInsertQuery(constant.getCentralGraph(), authorResource, publicationProperty, publicationResource);
        try {
            sparqlService.update(QueryLanguage.SPARQL, insertPubQuery);
        } catch (MalformedQueryException ex) {
            log.error("Malformed Query:  " + insertPubQuery);
        } catch (UpdateExecutionException ex) {
            log.error("Update Query :  " + insertPubQuery);
        } catch (MarmottaException ex) {
            log.error("Marmotta Exception:  " + insertPubQuery);
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
        
        total += 1;
        
        float similarity = (float) ((compare + compare2) / 2.0);
        //log.info("Titulos " + publicationResourceOne + "," + publicationResourceTwo + ": similaridad " + similarity * 100 + "%");

        if (similarity > 0.9) {
            totalPublicationRecognized += 1;
        } else {
            totalPublicationNotRecognized += 1;
        }
        return similarity > 0.9; // 0.8131
    }
    
    private String cleanStringUri(String uri) {
        uri = StringEscapeUtils.unescapeJava(uri);
        uri = StringUtils.stripAccents(uri);
        String titleUri = "";
        final String dash = "-";
        Pattern pat = Pattern.compile("[a-zA-Z0-9-]{2,100}");
        for (String token : uri.split(" ")) {
            token = token.replaceAll("[\\W]", "");
            Matcher mat = pat.matcher(token);
            if (mat.matches()) {
                if (titleUri.length() > 1) {
                    titleUri += dash;
                }
                titleUri += token.toLowerCase();
            }
        }
        return titleUri;
    }
    
    private String cleanStringUriAuthor(String uri) {
        String aux = StringUtils.stripAccents(uri);
        uri = aux;
        String authorUri = "";
        String dash = "-";
        for (String token : uri.replaceAll(dash, dash).split(" ")) {
            Pattern pat = Pattern.compile("[a-zA-Z0-9-ÑñáéíóúÁÉÍÓÚ]{1,100}");
            Matcher mat = pat.matcher(token);
            if (mat.matches()) {
                if (authorUri.length() > 1) {
                    authorUri += dash;
                }
                authorUri += token.toLowerCase();
            }
        }
        return authorUri;
    }
    
    private String searchAuthorOfpublication(List<Map<String, Value>> publications, String authorNativeResource, String newUriAuthorCentral) {
        try {
            List<String> authorName = getFirstAndLastNameAuthor(newUriAuthorCentral);
            List<Map<String, Value>> resultPublicationsTitle = sparqlService.query(QueryLanguage.SPARQL, queriesService.getAuthorPublicationFilter(constant.getCentralGraph(), authorName.isEmpty() ? "noThing" : authorName.get(0), authorName.isEmpty() ? "noThing" : authorName.get(1)));
            for (Map<String, Value> publicacion : resultPublicationsTitle) {
                String authorResource = publicacion.get("authorResource").stringValue();
                String publicationResource = publicacion.get("publicationResource").stringValue();
                String title = publicacion.get("title").stringValue();
                
                for (Map<String, Value> publicacionParam : publications) {
                    if (compareTitlePublicationWithSimmetrics(publicacionParam.get("title").stringValue(), title)) {
                        //log.info("publication that coinside between authors: 1:" + publicationResource + "2: " + publicacionParam + ", author: " + authorResource);

                        return authorResource;
                    }
                }
            }
            
        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(Data2GlobalGraphImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidArgumentException ex) {
            java.util.logging.Logger.getLogger(Data2GlobalGraphImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
        
    }
    
    private String buildNewUri(String authorResource) {
        List<Map<String, Value>> resultProvenance;
        List<Map<String, Value>> auxResultProvenance;
        String provenance = "";
        try {
            auxResultProvenance = sparqlService.query(QueryLanguage.SPARQL, queriesService.authorGetProvenance(constant.getCentralGraph(), authorResource));
            resultProvenance = auxResultProvenance.isEmpty() ? sparqlService.query(QueryLanguage.SPARQL, queriesService.authorGetProvenance(constant.getAuthorsGraph(), authorResource)) : auxResultProvenance;
            for (Map<String, Value> prov : resultProvenance) {
                provenance = prov.get("name").stringValue();
            }
        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(Data2GlobalGraphImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            
            List<Map<String, Value>> resultAuthorName = sparqlService.query(QueryLanguage.SPARQL, queriesService.getFirstNameLastNameAuhor(constant.getAuthorsGraph(), authorResource));
            
            for (Map<String, Value> publicacion : resultAuthorName) {
                String fisrtName = publicacion.get("fname").stringValue();
                String lastName = publicacion.get("lname").stringValue();
                
                String newuri = constant.getAuthorResource() + cleanStringUriAuthor((fisrtName + " " + lastName).replace(".", ""));
                String askTripletQuery = queriesService.getAskQuery(constant.getCentralGraph(), newuri, RDF.TYPE, FOAF.NAMESPACE + "Person");
                boolean askNewAuthor = sparqlService.ask(QueryLanguage.SPARQL, askTripletQuery);
                if (!askNewAuthor) {
                    //If the author is not already added, get the properties of the author from the provider graph and add them to the new author in the central graph
                    List<Map<String, Value>> resultAuthorProperties = sparqlService.query(QueryLanguage.SPARQL, queriesService.detailsOfProvenance(constant.getAuthorsGraph(), authorResource));
                    Map<String, String> authorMapper = loadProperties("AuthorMapper");
                    for (Map<String, Value> property : resultAuthorProperties) {
                        String p = authorMapper.get(property.get("property").stringValue());
                        String v = property.get("hasValue").stringValue();
                        if (p == null || v == null) {
                            continue;
                        }
                        if (FOAF.topic_interest.toString().equals(p)) {
                            String topic = v;
                            v = constant.getTopicResource() + cleanStringUri(topic);
                            try {
                                sparqlService.update(QueryLanguage.SPARQL, buildInsertQuery(constant.getCentralGraph(), v, RDFS.LABEL.toString(), topic));
                            } catch (InvalidArgumentException | MalformedQueryException | UpdateExecutionException ex) {
                                log.error("ERROR: add subject statement. {}", ex);
                            }
                        }
                        String insertPubQuery = buildInsertQuery(constant.getCentralGraph(), newuri, p, v);
                        try {
                            sparqlService.update(QueryLanguage.SPARQL, insertPubQuery);
                        } catch (MalformedQueryException ex) {
                            log.error("Malformed Query:  " + insertPubQuery);
                        } catch (UpdateExecutionException ex) {
                            log.error("Update Query :  " + insertPubQuery);
                        } catch (MarmottaException ex) {
                            log.error("Marmotta Exception:  " + insertPubQuery);
                        }
                    }
                }
                return newuri;
            }
        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(Data2GlobalGraphImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return authorResource;
    }
    
    private List<String> getFirstAndLastNameAuthor(String authorResource) {
        List<String> names = new ArrayList<>();
        try {
            List<Map<String, Value>> resultAuthorName = sparqlService.query(QueryLanguage.SPARQL, queriesService.getFirstNameLastNameAuhor(wkhuskaGraph, authorResource));
            for (Map<String, Value> publicacion : resultAuthorName) {
                String fisrtName = publicacion.get("fname").stringValue();
                String lastName = publicacion.get("lname").stringValue();
                String pipe = " | ";
                String firstNameProcessed = "";
                String lastNameProcessed = "";
                for (String fname : fisrtName.split(" ")) {
                    if (firstNameProcessed.length() > 1 && fname.trim().length() > 1) {
                        firstNameProcessed = firstNameProcessed.concat(pipe);
                    }
                    firstNameProcessed = firstNameProcessed.concat(fname.trim().length() > 1 ? fname.trim() : "");
                }
                names.add(firstNameProcessed);
                // proccess to lastname
                for (String lname : lastName.split(" ")) {
                    if (lastNameProcessed.length() > 1 && lname.trim().length() > 1) {
                        lastNameProcessed = lastNameProcessed.concat(pipe);
                    }
                    lastNameProcessed = lastNameProcessed.concat(lname.trim().length() > 1 ? lname.trim() : "");
                }
                names.add(lastNameProcessed);
            }
        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(Data2GlobalGraphImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return names;
        
    }
    
    private String getQuerySearchTextAuthor(String title) {
        String query = "";
        String pipe = " & ";
        for (String token : title.split(" ")) {
            Pattern pat = Pattern.compile("[a-zA-Z]{4,20}");
            Matcher mat = pat.matcher(token);
            if (mat.matches()) {
                if (query.length() > 1) {
                    query += pipe;
                }
                query += token;
            }
        }
        return query;
    }
    
    private String getNameOfProvider(String providerGraph) {
        
        if (providerGraph.toUpperCase().contains("DBLP")) {
            return "http://dblp.uni-trier.de/";
        } else if (providerGraph.toUpperCase().contains("ACADEMICS")) {
            return "https://academic.microsoft.com/";
        } else if (providerGraph.toUpperCase().contains("SCOPUS")) {
            return "http://scopus/";
        } else if (providerGraph.toUpperCase().contains("GOOGLESCHOLAR")) {
            return "https://scholar.google.com/";
        } else {
            return "No definida";
        }
        
    }
}
