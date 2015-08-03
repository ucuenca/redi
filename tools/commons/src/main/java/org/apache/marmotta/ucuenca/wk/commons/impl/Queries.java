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
        String graphSentence = "GRAPH <" + varargs[0] + ">";
        String subjectSentence = "<" + varargs[1] + ">";
        return "INSERT DATA { " + graphSentence + "  { " + subjectSentence + " <" + varargs[2] + "> " + varargs[3] + " }}";

    }

    /**
     * Return a INSERT QUERY when object is a URI
     *
     * @param varargs
     * @return
     */
    @Override
    public String getInsertDataUriQuery(String... varargs) {
        String graphSentence = "GRAPH <" + varargs[0] + ">";
        String subjectSentence = "<" + varargs[1] + ">";
        return "INSERT DATA { " + graphSentence + " { " + subjectSentence + " <" + varargs[2] + "> <" + varargs[3] + "> }}";

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

    /**
     * Return ASK query for a resource
     *
     * @param resource
     * @return
     */
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
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/name> ?name ."
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/url> ?url."
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/graph> ?graph."
                + " }"
                + " }";
    }

    @Override
    public String getEndpointByIdQuery(String endpointsGraph, String id) {
        return "SELECT DISTINCT ?id ?name ?url ?graph  WHERE {  "
                + " GRAPH <" + endpointsGraph + ">"
                + " {"
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/name> ?name ."
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/url> ?url."
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/graph> ?graph."
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
    public String getAuthorsQuery() {
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

    /**
     * get list of graphs query
     *
     * @return
     */
    @Override
    public String getGraphsQuery() {
        return " SELECT DISTINCT ?grafo WHERE { "
                + " graph ?grafo {?x ?y ?z } "
                + " } ";
    }

    /**
     * Return ASK query for triplet
     *
     * @param subject
     * @param predicate
     * @param object
     * @return
     */
    @Override
    public String getAskQuery(String... varargs) {
        String graphSentence = "GRAPH <" + varargs[0] + ">";

        return "ASK { " + graphSentence + "{ <" + varargs[1] + "> <" + varargs[2] + "> <" + varargs[3] + "> } }";
    }

    @Override
    public String getPublicationsQuery(String providerGraph) {
        return " SELECT DISTINCT ?authorResource ?pubproperty ?publicationResource WHERE { "
                + " graph <" + providerGraph + "> "
                + " {  "
                + " ?authorResource owl:sameAs   ?authorNative. "
                + " ?authorNative ?pubproperty ?publicationResource. "
                + " filter (regex(?pubproperty,\"pub\")) "
                + " }  "
                + " }  ";
    }

    @Override
    public String getPublicationsPropertiesQuery(String providerGraph , String publicationResource) {
        return "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
                + " PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                + " SELECT DISTINCT ?publicationProperties ?publicationPropertyValue WHERE { "
                + " graph <"+providerGraph+"> "
                + " {"
                + " <" + publicationResource + ">  ?publicationProperties ?publicationPropertyValue. "
                + " }} ";
    }

    @Override
    public String getNumMembersQuery() {
        return "SELECT DISTINCT (count(?members) as ?numMembers) "
                + " WHERE { ?x <http://xmlns.com/foaf/0.1/member> ?members. } ";
    }
    
    @Override
    public String getPublicationFromProviderQuery() {
        return  "SELECT DISTINCT ?authorResource  ?publicationResource "
                + " WHERE {  ?authorResource <http://xmlns.com/foaf/0.1/publications> ?publicationResource. }";
    }

    @Override
    public String getPublicationPropertiesQuery() {
       return "SELECT DISTINCT ?publicationResource ?publicationProperty ?publicationPropertyValue "
               + " WHERE { ?authorResource <http://xmlns.com/foaf/0.1/publications> ?publicationResource. ?publicationResource ?publicationProperty ?publicationPropertyValue }";
    
    }
    
    
}
