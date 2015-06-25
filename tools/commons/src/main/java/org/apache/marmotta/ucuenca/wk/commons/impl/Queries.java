/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.impl;

import java.net.URL;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;

/**
 *
 * @author Satellite
 */
public class Queries implements QueriesService {

    @Override
    public String getAuthorsQuery() {
        return "SELECT DISTINCT ?o WHERE {  ?s <http://id.loc.gov/vocabulary/relators/aut> ?o } ORDER BY ?o";
    }

    @Override
    public String getRetrieveResourceQuery() {
        return "SELECT ?x ?y ?z WHERE { ?x ?y ?z }";
    }

    /**
     * Return a INSERT QUERY when object is a LITERAL
     *
     * @param subject
     * @param predicate
     * @param object
     * @return
     */
    @Override
    public String getInsertDataLiteralQuery(String subject, String predicate, String object) {
        return "INSERT DATA { <" + subject + "> <" + predicate + "> " + object + " }";
    }

    /**
     * Return a INSERT QUERY when object is a URI
     *
     * @param subject
     * @param predicate
     * @param object
     * @return
     */
    @Override
    public String getInsertDataUriQuery(String subject, String predicate, String object) {
        return "INSERT DATA { <" + subject + "> <" + predicate + "> <" + object + "> }";

    }

    /**
     * Return true or false if object is a URI
     *
     * @param object
     * @return
     */
    @Override
    public Boolean isURI(String object) {
        URL url = null;
        try {
            url = new URL(object);
        } catch (Exception e1) {
            return false;
        }
        return "http".equals(url.getProtocol());
    }

    @Override
    public String getAskQuery(String resource) {
        return "ASK { <" + resource + "> ?p ?o }";
    }

    @Override
    public String getEndpointNameQuery(String endpointsGraph, String name, String resourceHash) {
        return "INSERT DATA { GRAPH <" + endpointsGraph + "> { <http://localhost:8080/endpoint/" + resourceHash + ">  <http://localhost:8080/endpoint/name>  \"" + name + "\" }}";
    }

    @Override
    public String getEndpointUrlQuery(String endpointsGraph, String url, String resourceHash) {
        return "INSERT DATA { GRAPH <" + endpointsGraph + "> { <http://localhost:8080/endpoint/" + resourceHash + ">  <http://localhost:8080/endpoint/url>  <" + url + "> }}";
    }

    @Override
    public String getEndpointGraphQuery(String endpointsGraph, String graphUri, String resourceHash) {
        return "INSERT DATA { GRAPH <" + endpointsGraph + "> { <http://localhost:8080/endpoint/" + resourceHash + ">  <http://localhost:8080/endpoint/graph>  <" + graphUri + "> }}";
    }

    @Override
    public String getlisEndpointsQuery(String endpointsGraph) {
        return "SELECT DISTINCT ?id ?name ?url ?graph  WHERE {  "
                + " GRAPH <" + endpointsGraph + ">"
                + " {"
                + " ?id <http://localhost:8080/endpoint/name> ?name ."
                + " ?id <http://localhost:8080/endpoint/url> ?url."
                + " ?id <http://localhost:8080/endpoint/graph> ?graph."
                + " }"
                + " }";
    }

    @Override
    public String getEndpointByIdQuery(String endpointsGraph, String id) {
        return "SELECT DISTINCT ?id ?name ?url ?graph  WHERE {  "
                + " GRAPH <" + endpointsGraph + ">"
                + " {"
                + " ?id <http://localhost:8080/endpoint/name> ?name ."
                + " ?id <http://localhost:8080/endpoint/url> ?url."
                + " ?id <http://localhost:8080/endpoint/graph> ?graph."
                + " FILTER(?id = <" + id + ">)"
                + " }"
                + " }";
    }

    @Override
    public String getEndpointDeleteQuery(String endpointsGraph, String id) {

        return "DELETE { ?id ?p ?o } "
                + "WHERE"
                + " { "
                + " GRAPH <"+endpointsGraph+">"
                + " { "
                + " ?id ?p ?o . "
                + " FILTER(?id = <"+id+">) "
                + " } "
                + " }";
    }
}
