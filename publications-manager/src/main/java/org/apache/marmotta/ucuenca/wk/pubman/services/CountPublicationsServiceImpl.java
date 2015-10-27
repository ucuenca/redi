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
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.PropertyPubService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.CountPublicationsService;
import org.apache.marmotta.ucuenca.wk.pubman.api.SparqlFunctionsService;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.base.RepositoryConnectionBase;
import org.slf4j.Logger;

/**
 *
 * @author Freddy Sumba
 */
@ApplicationScoped
public class CountPublicationsServiceImpl implements CountPublicationsService, Runnable {

    @Inject
    private Logger log;

    @Inject
    private QueriesService queriesService;

    @Inject
    private PropertyPubService pubVocabService;

    @Inject
    private SparqlFunctionsService sparqlFunctionsService;

    private String graphCountName = "http://ucuenca.edu.ec/counters";

    /**
     * graphByProvider Graph to count publications data by provider and central
     * graph.
     */
    @Inject
    private SparqlService sparqlService;

    @Override
    public String CountPublicationsService() {
        try {

            String providerGraph = "";
            //String getAuthorsQuery = queriesService.getAuthorsQuery();
            String getGraphsListQuery = queriesService.getGraphsQuery();
            List<Map<String, Value>> resultGraph = sparqlService.query(QueryLanguage.SPARQL, getGraphsListQuery);
            /* FOR EACH GRAPH*/

            for (Map<String, Value> map : resultGraph) {
                providerGraph = map.get("grafo").toString();
                KiWiUriResource providerGraphResource = new KiWiUriResource(providerGraph);

                if (providerGraph.contains("provider") || providerGraph.equals("http://ucuenca.edu.ec/wkhuska")) {
                    //load the properties of each graph provider
//                    loadPropertiesProvider(providerGraphResource);

                    List<Map<String, Value>> count = sparqlService.query(QueryLanguage.SPARQL, queriesService.getPublicationsCount(providerGraph));
                    for (Map<String, Value> map2 : count) {
                        String contPublications = map2.get("total").stringValue();
                        insertPublicationToCentralGraph(providerGraph + "/publications", "http://purl.org/ontology/bibo/number", "\"" + contPublications + "\"", "integer");
                    }
                    if (providerGraph.equals("http://ucuenca.edu.ec/wkhuska")) {
                        List<Map<String, Value>> countAuthors = sparqlService.query(QueryLanguage.SPARQL, queriesService.getTotalAuthorWithPublications(providerGraph));
                        for (Map<String, Value> map3 : countAuthors) {
                            String contAuthors = map3.get("total").stringValue();
                            insertPublicationToCentralGraph(providerGraph + "/authors", "http://purl.org/ontology/bibo/number", "\"" + contAuthors + "\"", "integer");
                        }
                    }
                }
            }

        } catch (InvalidArgumentException ex) {
            return "error:  " + ex;
        } catch (MarmottaException ex) {
            return "error:  " + ex;
        }
        return "Sucessfull publications count.";
    }

    @Override
    public void run() {
        CountPublicationsService();
    }

    private void loadPropertiesProvider(KiWiUriResource providerGraphResource) {
        Properties propiedades = new Properties();
        InputStream entrada = null;
        Map<String, String> mapping = new HashMap<String, String>();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            //File file = new File(classLoader.getResource("DBLPProvider.properties").getFile());
            entrada = classLoader.getResourceAsStream(providerGraphResource.getLocalName() + ".properties");
            // cargamos el archivo de propiedades
            propiedades.load(entrada);
            for (String source : propiedades.stringPropertyNames()) {
                String target = propiedades.getProperty(source);

                mapping.put(source.replace("..", ":"), target.replace("..", ":"));

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (entrada != null) {
                try {
                    entrada.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    //construyendo sparql query insert 
    public String buildInsertQuery(String grapfhProv, String sujeto, String predicado, String objeto, String type) {
        if (queriesService.isURI(objeto)) {
            return queriesService.getInsertDataUriQuery(grapfhProv, sujeto, predicado, objeto);
        } else {
            return queriesService.getInsertDataLiteralQuery(grapfhProv, sujeto, predicado, objeto, type);
        }
    }

    public void insertPublicationToCentralGraph(String sujeto, String propiedad, String value, String type) {
        String insertPubQuery = buildInsertQuery(graphCountName, sujeto, propiedad, value, type);
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
