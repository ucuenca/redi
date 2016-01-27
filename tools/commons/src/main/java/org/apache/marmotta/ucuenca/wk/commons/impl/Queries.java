/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.impl;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.openrdf.model.vocabulary.FOAF;

/**
 *
 *
 *
 * @author Satellite
 */
public class Queries implements QueriesService {

    private String insertData = "INSERT DATA { GRAPH <";
    private String endpointString = "> { <http://ucuenca.edu.ec/wkhuska/endpoint/";

    @Override
    public String getAuthorsQuery(String datagraph) {
        return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                + "SELECT DISTINCT ?s WHERE { GRAPH <" + datagraph + "> { ?s rdf:type foaf:Person }}";
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
        String object = null;
        if (varargs[3].contains("^^")) {
            object = "\"" + StringEscapeUtils.escapeJava(varargs[3].substring(1, varargs[3].indexOf("^^") - 1)) + "\"" + varargs[3].substring(varargs[3].indexOf("^^"));
        } else {
            object = "\"" + StringEscapeUtils.escapeJava(varargs[3].substring(1, varargs[3].length() - 1)) + "\"" + (varargs.length > 4 ? varargs[4] != null ? "^^xsd:" + varargs[4] : "^^xsd:string" : "");
        }

        return "INSERT DATA { " + graphSentence + "  { " + subjectSentence + " <" + varargs[2] + "> " + object + " }}";

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
        Pattern pat = Pattern.compile("^[hH]ttp(s?)");
        Matcher mat = pat.matcher(url.getProtocol());
        return mat.matches();

        // return "http".equals(url.getProtocol()) || "https".equals(url.getProtocol()) ;
    }

    /**
     * Return ASK query for a resource
     *
     * @param resource
     * @return
     */
    @Override
    public String getAskResourceQuery(String graph, String resource) {
        return "ASK FROM <" + graph + "> {  <" + resource + "> ?p ?o }";
    }

    @Override
    public String getEndpointNameQuery(String endpointsGraph, String name, String resourceHash) {
        return insertData + endpointsGraph + endpointString + resourceHash + ">  <http://ucuenca.edu.ec/wkhuska/resource/name>  \"" + name + "\" }}";
    }

    @Override
    public String getEndpointUrlQuery(String endpointsGraph, String url, String resourceHash) {
        return insertData + endpointsGraph + endpointString + resourceHash + ">  <http://ucuenca.edu.ec/wkhuska/resource/url>  <" + url + "> }}";
    }

    @Override
    public String getEndpointGraphQuery(String endpointsGraph, String graphUri, String resourceHash) {
        return insertData + endpointsGraph + endpointString + resourceHash + ">  <http://ucuenca.edu.ec/wkhuska/resource/graph>  <" + graphUri + "> }}";
    }

    @Override
    public String getEndpointFullNameQuery(String endpointsGraph, String fullName, String resourceHash) {
        return insertData + endpointsGraph + endpointString + resourceHash + ">  <http://ucuenca.edu.ec/wkhuska/resource/fullName>  \"" + fullName + "\" }}";
    }

    @Override
    public String getEndpointCityQuery(String endpointsGraph, String city, String resourceHash) {
        return insertData + endpointsGraph + endpointString + resourceHash + ">  <http://ucuenca.edu.ec/wkhuska/resource/city>  \"" + city + "\" }}";
    }

    @Override
    public String getEndpointProvinceQuery(String endpointsGraph, String province, String resourceHash) {
        return insertData + endpointsGraph + endpointString + resourceHash + ">  <http://ucuenca.edu.ec/wkhuska/resource/province>  \"" + province + "\"}}";
    }

    @Override
    public String getEndpointLatitudeQuery(String endpointsGraph, String latitude, String resourceHash) {
        return insertData + endpointsGraph + endpointString + resourceHash + ">  <http://ucuenca.edu.ec/wkhuska/resource/latitude>  \"" + latitude + "\"}}";
    }

    @Override
    public String getEndpointLongitudeQuery(String endpointsGraph, String longitude, String resourceHash) {
        return insertData + endpointsGraph + endpointString + resourceHash + ">  <http://ucuenca.edu.ec/wkhuska/resource/longitude>  \"" + longitude + "\"}}";
    }

    @Override
    public String getlisEndpointsQuery(String endpointsGraph) {
        return "SELECT DISTINCT ?id ?name ?url ?graph ?fullName ?city ?province ?latitude ?longitude  WHERE {  "
                + " GRAPH <" + endpointsGraph + ">"
                + " {"
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/name> ?name ."
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/url> ?url."
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/graph> ?graph."
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/fullName> ?fullName."
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/city> ?city."
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/province> ?province."
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/latitude> ?latitude."
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/longitude> ?longitude."
                + " }"
                + " }";
    }

    @Override
    public String getEndpointByIdQuery(String endpointsGraph, String id) {
        return "SELECT DISTINCT ?id ?name ?url ?graph ?fullName ?city ?province ?latitude ?longitude  WHERE {  "
                + " GRAPH <" + endpointsGraph + ">"
                + " {"
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/name> ?name ."
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/url> ?url."
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/graph> ?graph."
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/fullName> ?fullName."
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/city> ?city."
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/province> ?province."
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/latitude> ?latitude."
                + " ?id <http://ucuenca.edu.ec/wkhuska/resource/longitude> ?longitude."
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
    public String getCountPersonQuery(String graph) {
        return " PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + " SELECT (COUNT(?s) as ?count) WHERE { GRAPH <" + graph + "> { ?s rdf:type foaf:Person. }}";
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
    public String getAuthorsDataQuery(String graph) {
        return " PREFIX foaf: <" + FOAF.NAMESPACE + "> "
                + " SELECT * "
                + " WHERE { GRAPH <" + graph + "> { "
                + " ?subject a foaf:Person. "
                + " ?subject foaf:name ?name."
                + " ?subject foaf:firstName ?fname."
                + " ?subject foaf:lastName ?lname."
                + "?subject dct:provenance ?pro."
                + "{ select ?resource where"
                + "{ graph <http://ucuenca.edu.ec/wkhuska/endpoints> {"
                + "?pro <http://ucuenca.edu.ec/resource/status> ?resource "
                + "}}} filter (regex(?resource,\"true\"))"
                + "                }} "
                + " }}";
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
                + " graph  <" + providerGraph + "> "
                + " {  "
                + " ?authorResource owl:sameAs   ?authorNative. "
                + " ?authorNative ?pubproperty ?publicationResource. "
                + " { FILTER (regex(?pubproperty,\"authorOf\")) } "
                + " UNION"
                + " { FILTER (regex(?pubproperty,\"pub\")) } "
                + " }}";
    }

    @Override
    public String getPublicationsMAQuery(String providerGraph) {
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
    public String getPublicationsPropertiesQuery(String providerGraph, String publicationResource) {
        return "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
                + " PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                + " SELECT DISTINCT ?publicationProperties ?publicationPropertyValue WHERE { "
                + " graph <" + providerGraph + "> "
                + " {"
                + " <" + publicationResource + ">  ?publicationProperties ?publicationPropertyValue. "
                + " } }"
                + "ORDER BY DESC(?publicationProperties) ";
    }

    @Override
    public String getMembersQuery() {
        return "SELECT DISTINCT ?members"
                + " WHERE { ?x <http://xmlns.com/foaf/0.1/member> ?members. } ";
    }

    @Override
    public String getPublicationFromProviderQuery() {
        return "SELECT DISTINCT ?authorResource ?publicationProperty  ?publicationResource "
                + " WHERE {  ?authorResource <http://www.w3.org/2002/07/owl#sameAs> ?authorOtherResource. "
                + " ?authorOtherResource <http://dblp.uni-trier.de/rdf/schema-2015-01-26#authorOf> ?publicationResource. "
                + " ?authorOtherResource ?publicationProperty ?publicationResource. }";
    }

    @Override
    public String getPublicationForExternalAuthorFromProviderQuery(String property) {
        return "SELECT DISTINCT ?authorResource ?publicationProperty  ?publicationResource "
                + " WHERE { ?authorResource <" + property + "> ?publicationResource. "
                + " ?authorOtherResource ?publicationProperty ?publicationResource. }";
    }

    @Override
    public String getPublicationFromMAProviderQuery() {
        return "SELECT DISTINCT ?authorResource  ?publicationResource "
                + " WHERE {  ?authorResource <http://xmlns.com/foaf/0.1/publications> ?publicationResource}";
    }

    @Override
    public String getPublicationPropertiesQuery() {
        return "SELECT DISTINCT ?publicationResource ?publicationProperties ?publicationPropertiesValue "
                + " WHERE { ?authorResource <http://dblp.uni-trier.de/rdf/schema-2015-01-26#authorOf> ?publicationResource. ?publicationResource ?publicationProperties ?publicationPropertiesValue }";

    }

    @Override
    public String getPublicationMAPropertiesQuery() {
        return "SELECT DISTINCT ?publicationResource ?publicationProperties ?publicationPropertiesValue "
                + " WHERE { ?authorResource <http://xmlns.com/foaf/0.1/publications> ?publicationResource. ?publicationResource ?publicationProperties ?publicationPropertiesValue }";

    }

    @Override
    public String getPublicationPropertiesAsResourcesQuery() {
        return "SELECT DISTINCT ?publicationResource ?publicationProperties ?publicationPropertiesValue "
                + " WHERE { ?authorResource <http://xmlns.com/foaf/0.1/publications> ?publicationResourceItem. ?publicationResourceItem ?publicationPropertiesItem ?publicationResource. ?publicationResource ?publicationProperties ?publicationPropertiesValue }";

    }

    @Override
    public String getAuthorPublicationsQuery(String providerGraph, String author, String prefix) {
        return " SELECT DISTINCT  ?authorResource  ?pubproperty  ?publicationResource "
                + "?title WHERE { "
                + " graph   <" + providerGraph + "> "
                + " { <" + author + "> <http://xmlns.com/foaf/0.1/publications> "
                + "?publicationResource.  ?publicationResource "
                + "<" + prefix + "> "
                + "?title } }";
    }

    @Override
    public String getAuthorPublicationsQueryFromProvider(String providerGraph, String authorResource, String prefix) {

        return " SELECT DISTINCT  ?pubproperty ?publicationResource ?title  "
                + "WHERE {  graph <" + providerGraph + ">  "
                + "{    <" + authorResource + "> "
                + "owl:sameAs   ?authorNative.  ?authorNative ?pubproperty ?publicationResource.  "
                + "?publicationResource <" + prefix + ">  ?title\n"
                + "\n"
                + "{ FILTER (regex(?pubproperty,\"authorOf\")) }  UNION { FILTER (regex(?pubproperty,\"pub\")) }                                                                                        }} ";
    }

    @Override
    public String getPublicationDetails(String publicationResource) {

        return "SELECT DISTINCT ?property ?hasValue  WHERE {\n"
                + "  { <" + publicationResource + "> ?property ?hasValue }\n"
                + "UNION\n"
                + "  { ?isValueOf ?property <" + publicationResource + "> }\n"
                + "}\n"
                + "ORDER BY ?property ?hasValue ?isValueOf";
    }

    @Override
    public String getPublicationsTitleQuery(String providerGraph, String prefix) {
        return ""
                + " SELECT DISTINCT ?authorResource ?pubproperty ?publicationResource ?title "
                + "WHERE {  graph <" + providerGraph + ">  "
                + "{   ?authorResource owl:sameAs   ?authorNative.  "
                + "?authorNative ?pubproperty ?publicationResource.  "
                + "?publicationResource <" + prefix + ">  ?title\n"
                + "\n" + "{ FILTER (regex(?pubproperty,\"authorOf\")) }  "
                + "UNION { FILTER (regex(?pubproperty,\"pub\")) }                                                                                        }} ";
    }

    @Override
    public String getPublicationsCount(String graph) {
        return "SELECT  (COUNT(distinct ?publicationResource) AS ?total)WHERE { \n"
                + "                 graph  <" + graph + "> \n"
                + "                 {  \n"
                + "                 ?authorResource owl:sameAs   ?authorNative. \n"
                + "                 ?authorNative ?pubproperty ?publicationResource. \n"
                + "                 \n"
                + "                 }}";
    }

    @Override
    public String getPublicationsCountCentralGraph() {
        return "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "               SELECT   (COUNT(distinct ?authorNative) as ?total) WHERE { \n"
                + "                                 graph  <http://ucuenca.edu.ec/wkhuska> \n"
                + "                                {  \n"
                + "                                 ?authorResource foaf:publications  ?authorNative\n"
                + "                                \n"
                + "                                 }}";
    }

    @Override
    public String getTotalAuthorWithPublications(String graph) {

        return "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "SELECT   (COUNT(distinct ?authorResource) as ?total) WHERE { \n"
                + "                 graph  <http://ucuenca.edu.ec/wkhuska> \n"
                + "                 {  \n"
                + "                 ?authorResource foaf:publications  ?authorNative\n"
                + "                 \n"
                + "                 }}";
    }

    @Override
    public String deleteDataGraph(String graph) {
        return "DELETE  { \n"
                + "   ?s ?p ?o } \n"
                + "where\n"
                + "{\n"
                + "graph <" + graph + "> {\n"
                + "  ?s ?p ?o }\n"
                + "}";
    }

    @Override
    public String getTitlePublications(String graph) {
        return "PREFIX dct: <http://purl.org/dc/terms/> "
                + "PREFIX foaf: <" + FOAF.NAMESPACE + "> "
                + "SELECT *  WHERE { graph <" + graph + "> "
                + "  {?authorResource foaf:publications  ?publicationResource.\n"
                + "   ?publicationResource dct:title ?title\n"
                + "  }\n"
                + "}";
    }

    @Override
    public String getFirstNameLastNameAuhor(String graph, String authorResource) {
        return "PREFIX foaf: <" + FOAF.NAMESPACE + ">\n"
                + "SELECT distinct (str(?firstname) as ?fname) (str(?lastname) as ?lname) from <" + graph + "> WHERE {\n"
                + "                <" + authorResource + "> a foaf:Person; \n"
                + "                 foaf:firstName ?firstname;\n"
                + "                 foaf:lastName ?lastname;  \n"
                + "}";
    }

    @Override
    public String authorDetailsOfProvenance(String graph, String authorResource) {
        return "SELECT DISTINCT ?property ?hasValue  WHERE {\n"
                + "  \n"
                + "  graph <" + graph + ">{\n"
                + "  { <" + authorResource + "> ?property ?hasValue }\n"
                + "UNION\n"
                + "  { ?isValueOf ?property <" + authorResource + "> }\n"
                + "}}\n"
                + "ORDER BY ?property ?hasValue ?isValueOf";
    }

    @Override
    public String getAuthorPublicationFilter(String graph, String fname, String lname) {
        return "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                + "PREFIX dct: <http://purl.org/dc/terms/> "
                + "PREFIX mm: <http://marmotta.apache.org/vocabulary/sparql-functions#> "
                + "              "
                + "                SELECT distinct ?authorResource  ?publicationResource ?title  WHERE { "
                + "graph <" + graph + ">\n"
                + "                  { \n"
                + "                   ?authorResource foaf:firstName ?fname.\n"
                + "                    ?authorResource foaf:lastName  ?lname.\n"
                + "                   ?authorResource foaf:publications   ?publicationResource.\n"
                + "                   ?publicationResource dct:title ?title\n"
                + "                                        \n"
                + "					    {FILTER( mm:fulltext-query(str(?fname), \"" + fname + "\")  "
                + "                                               && mm:fulltext-query(str(?lname), \"" + lname + "\"))\n"
                + "                   }}}";

    }

}
