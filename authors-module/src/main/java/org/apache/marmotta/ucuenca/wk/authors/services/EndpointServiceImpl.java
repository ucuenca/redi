/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.authors.services;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointService;
import org.apache.marmotta.ucuenca.wk.authors.api.SparqlEndpoint;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;

/**
 *
 * @author Satellite
 */
public class EndpointServiceImpl implements EndpointService {

    @Inject
    private SparqlService sparqlService;

    @Inject
    private QueriesService queriesService;

    private String endpointsGraph = "http://ucuenca.edu.ec/wkhuska/endpoints";

    @Override
    public String addEndpoint(String name, String endpointUrl, String graphUri) {

        try {

            //          String queryEndpointToAdd = queriesService.getEndpointToAddQuery(endpointGraph, name, endpointUrl, graphUri);
            //sparqlService.update(QueryLanguage.SPARQL, queryEndpointToAdd);
            String resourceHash = getHashCode(name, endpointUrl, graphUri);
            addEndpointName(endpointsGraph, name, resourceHash);
            addEndpointUrl(endpointsGraph, endpointUrl, resourceHash);
            addEndpointGraph(endpointsGraph, graphUri, resourceHash);
            return "Endpoint Insertado Correctamente";
            //  return "Error al intentar agregar el Endpoint";
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(EndpointServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "AddEndpoint Error";
    }
    /**
     * Funcion que agrega un nuevo endpoint
     * @param args //String name, String endpointUrl, String graphUri, String fullName, String city, String province, String latitude, String longitude
     * @return 
     */
    public String addEndpoint(String... args) {
        try {
            String resourceHash = getHashCode(args[0], args[1], args[2]);
            addEndpointName(endpointsGraph, args[0], resourceHash);
            addEndpointUrl(endpointsGraph, args[1], resourceHash);
            addEndpointGraph(endpointsGraph, args[2], resourceHash);
            addEndpointFullName(endpointsGraph, args[3], resourceHash);
            addEndpointCity(endpointsGraph, args[4], resourceHash);
            addEndpointProvince(endpointsGraph, args[5], resourceHash);
            addEndpointLatitude(endpointsGraph, args[6], resourceHash);
            addEndpointLongitude(endpointsGraph, args[7], resourceHash);
            
            return "Endpoint Insertado Correctamente";
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(EndpointServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "AddEndpoint Error";
    }

    public void addEndpointName(String endpointsGraph, String name, String resourceHash) {
        try {
            String queryEndpointName = queriesService.getEndpointNameQuery(endpointsGraph, name, resourceHash);
            sparqlService.update(QueryLanguage.SPARQL, queryEndpointName);
        } catch (InvalidArgumentException | MarmottaException | MalformedQueryException | UpdateExecutionException ex) {
            Logger.getLogger(EndpointServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addEndpointUrl(String endpointsGraph, String endpointUrl, String resourceHash) {
        try {
            String queryEndpointUrl = queriesService.getEndpointUrlQuery(endpointsGraph, endpointUrl, resourceHash);

            sparqlService.update(QueryLanguage.SPARQL, queryEndpointUrl);
        } catch (InvalidArgumentException | MarmottaException | MalformedQueryException | UpdateExecutionException ex) {
            Logger.getLogger(EndpointServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addEndpointGraph(String endpointsGraph, String graphUri, String resourceHash) {
        try {
            String queryEndpointGraph = queriesService.getEndpointGraphQuery(endpointsGraph, graphUri, resourceHash);
            sparqlService.update(QueryLanguage.SPARQL, queryEndpointGraph);
        } catch (InvalidArgumentException | MarmottaException | MalformedQueryException | UpdateExecutionException ex) {
            Logger.getLogger(EndpointServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addEndpointFullName(String endpointsGraph, String fullName, String resourceHash) {
        try {
            String queryFullName = queriesService.getEndpointFullNameQuery(endpointsGraph, fullName, resourceHash);
            sparqlService.update(QueryLanguage.SPARQL, queryFullName);
        } catch (InvalidArgumentException | MarmottaException | MalformedQueryException | UpdateExecutionException ex) {
            Logger.getLogger(EndpointServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addEndpointCity(String endpointsGraph, String city, String resourceHash) {
        try {
            String queryCity = queriesService.getEndpointCityQuery(endpointsGraph, city, resourceHash);
            sparqlService.update(QueryLanguage.SPARQL, queryCity);
        } catch (InvalidArgumentException | MarmottaException | MalformedQueryException | UpdateExecutionException ex) {
            Logger.getLogger(EndpointServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addEndpointProvince(String endpointsGraph, String province, String resourceHash) {
        try {
            String queryProvince = queriesService.getEndpointProvinceQuery(endpointsGraph, province, resourceHash);
            sparqlService.update(QueryLanguage.SPARQL, queryProvince);
        } catch (InvalidArgumentException | MarmottaException | MalformedQueryException | UpdateExecutionException ex) {
            Logger.getLogger(EndpointServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addEndpointLatitude(String endpointsGraph, String latitude, String resourceHash) {
        try {
            String queryLatitude = queriesService.getEndpointLatitudeQuery(endpointsGraph, latitude, resourceHash);
            sparqlService.update(QueryLanguage.SPARQL, queryLatitude);
        } catch (InvalidArgumentException | MarmottaException | MalformedQueryException | UpdateExecutionException ex) {
            Logger.getLogger(EndpointServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addEndpointLongitude(String endpointsGraph, String longitude, String resourceHash) {
        try {
            String queryLongitude = queriesService.getEndpointLongitudeQuery(endpointsGraph, longitude, resourceHash);
            sparqlService.update(QueryLanguage.SPARQL, queryLongitude);
        } catch (InvalidArgumentException | MarmottaException | MalformedQueryException | UpdateExecutionException ex) {
            Logger.getLogger(EndpointServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Function that return a Hash Code to get a unique ID for resource URI
     * according name, url and graph. example:
     * http://localhost:8080/endpoint/21552MED5454AFDCSS55422354F54D5SX9VRSSDB
     *
     * @param name
     * @param url
     * @param graph
     * @return
     * @throws NoSuchAlgorithmException
     */
    public String getHashCode(String name, String url, String graph) throws NoSuchAlgorithmException {
        String plaintext = name + url + graph;
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.reset();
        m.update(plaintext.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        return bigInt.toString(16);
    }

    @Override
    public List<SparqlEndpoint> listEndpoints() {
       try {
            List<SparqlEndpoint> result = new ArrayList<SparqlEndpoint>();
            List<Map<String, Value>> endpointsresult = sparqlService.query(QueryLanguage.SPARQL, queriesService.getlisEndpointsQuery(endpointsGraph));
            for (Map<String, Value> singleendpoint : endpointsresult) {
                SparqlEndpoint endpoint = new SparqlEndpoint();
                endpoint.setResourceId(singleendpoint.get("id").stringValue());
                endpoint.setName(singleendpoint.get("name").stringValue());
                endpoint.setEndpointUrl(singleendpoint.get("url").stringValue());
                endpoint.setGraph(singleendpoint.get("graph").stringValue());
                endpoint.setFullName(singleendpoint.get("fullName").stringValue());
                endpoint.setCity(singleendpoint.get("city").stringValue());
                endpoint.setProvince(singleendpoint.get("province").stringValue());
                endpoint.setLatitude(singleendpoint.get("latitude").stringValue());
                endpoint.setLongitude(singleendpoint.get("longitude").stringValue());
                result.add(endpoint);
            }
            return result;
        } catch (MarmottaException ex) {
            Logger.getLogger(EndpointServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public SparqlEndpoint getEndpoint(String resourceId) {
        try {
            List<Map<String, Value>> endpointresult = sparqlService.query(QueryLanguage.SPARQL, queriesService.getEndpointByIdQuery(endpointsGraph, resourceId));
            SparqlEndpoint endpoint = new SparqlEndpoint();
            endpoint.setResourceId(endpointresult.get(0).get("id").stringValue());
            endpoint.setName(endpointresult.get(0).get("name").stringValue());
            endpoint.setEndpointUrl(endpointresult.get(0).get("url").stringValue());
            endpoint.setGraph(endpointresult.get(0).get("graph").stringValue());
            endpoint.setFullName(endpointresult.get(0).get("fullName").stringValue());
            endpoint.setCity(endpointresult.get(0).get("city").stringValue());
            endpoint.setProvince(endpointresult.get(0).get("province").stringValue());
            endpoint.setLatitude(endpointresult.get(0).get("latitude").stringValue());
            endpoint.setLongitude(endpointresult.get(0).get("longitude").stringValue());
            return endpoint;
        } catch (MarmottaException ex) {
            Logger.getLogger(EndpointServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;

    }

    @Override
    public String removeEndpoint(String resourceid) {
        try {
            sparqlService.update(QueryLanguage.SPARQL, queriesService.getEndpointDeleteQuery(endpointsGraph, resourceid));
            return "Endpoint was DELETE";
        } catch (MarmottaException | InvalidArgumentException | MalformedQueryException | UpdateExecutionException ex) {
            Logger.getLogger(EndpointServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
