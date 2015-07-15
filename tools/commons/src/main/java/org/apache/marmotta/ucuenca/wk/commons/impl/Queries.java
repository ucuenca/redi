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
    public String getAuthorsQuery(String datagraph) {
        return "SELECT DISTINCT ?s WHERE { GRAPH <" + datagraph + "> { ?s rdf:type foaf:Person }}";
    }

    @Override
    public String getRetrieveResourceQuery() {
        return "SELECT ?x ?y ?z WHERE { ?x ?y ?z }";
    }

    /**
     * Return a INSERT QUERY when object is a LITERAL
     *
     * @param varargs
     * @return
     */
    @Override
    public String getInsertDataLiteralQuery(String... varargs) {
        String wkhuskaGraph = varargs[0];
        String subject = varargs[1];
        String predicate = varargs[2];
        String object = varargs[3];
        String graphSentence = "GRAPH <" + wkhuskaGraph + ">";
        return "INSERT DATA { " + graphSentence + " {<" + subject + "> <" + predicate + "> " + object + " }}";
    }

    /**
     * Return a INSERT QUERY when object is a URI
     *
     * @param varargs
     * @return
     */
    @Override
    public String getInsertDataUriQuery(String... varargs) {
        String wkhuskaGraph = varargs[0];
        String subject = varargs[1];
        String predicate = varargs[2];
        String object = varargs[3];
        String graphSentence = "GRAPH <" + wkhuskaGraph + ">";
        return "INSERT DATA { " + graphSentence + " {<" + subject + "> <" + predicate + "> <" + object + "> }}";

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
        return "INSERT DATA { GRAPH <" + endpointsGraph + "> { <http://ucuenca.edu.ec/wkhuska/endpoint/" + resourceHash + ">  <http://ucuenca.edu.ec/wkhuska/resource/name>  \"" + name + "\" }}";
    }

    @Override
    public String getEndpointUrlQuery(String endpointsGraph, String url, String resourceHash) {
        return "INSERT DATA { GRAPH <" + endpointsGraph + "> { <http://ucuenca.edu.ec/wkhuska/endpoint/" + resourceHash + ">  <http://ucuenca.edu.ec/wkhuska/resource/url>  <" + url + "> }}";
    }

    @Override
    public String getEndpointGraphQuery(String endpointsGraph, String graphUri, String resourceHash) {
        return "INSERT DATA { GRAPH <" + endpointsGraph + "> { <http://ucuenca.edu.ec/wkhuska/endpoint/" + resourceHash + ">  <http://ucuenca.edu.ec/wkhuska/resource/graph>  <" + graphUri + "> }}";
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
                + " GRAPH <" + endpointsGraph + ">"
                + " { "
                + " ?id ?p ?o . "
                + " FILTER(?id = <" + id + ">) "
                + " } "
                + " }";
    }

    @Override
    public String getWkhuskaGraph() {
        return "http://ucuenca.edu.ec/wkhuska";
    }

    @Override
    public String getCountPersonQuery(String datagraph) {
        return "  SELECT (COUNT(?s) as ?count) WHERE { GRAPH <" + datagraph + "> { ?s rdf:type foaf:Person. }}";
    }

    @Override
    public String getLimit(String limit) {
        return " Limit " + limit;
    }

    @Override
    public String getOffset(String offset) {
        return " offset " + offset;
    }

    @Override
    public String getProvenanceProperty() {
        return "http://purl.org/dc/terms/provenance";
    }

    @Override
    public String getPublicationsQuery() {
        return " PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                + " SELECT * "
                + " WHERE { "
                + " ?subject a foaf:Person. "
                + " ?subject foaf:name ?name."
                + " ?subject foaf:firstName ?fname."
                + " ?subject foaf:lastName ?lname."
                + " {"
                + " FILTER (regex(?name,\"Espinoza Mejia\"))"
                + " }"
                + " UNION"
                + " {"
                + " FILTER (regex(?name,\"Saquicela Galarza\"))"
                + " }"
                + " }";
    }

}
