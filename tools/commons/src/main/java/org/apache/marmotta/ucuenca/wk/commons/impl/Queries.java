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
import org.apache.marmotta.ucuenca.wk.commons.impl.Constant;

/**
 *
 * @author Satellite
 */
public class Queries implements QueriesService {

    private final Constant con = new Constant();

    @Override
    public String getAuthorsQuery(String datagraph) {
        return con.PREFIX
                + "SELECT DISTINCT ?s WHERE {" + con.getGraphString(datagraph) + "{ ?s rdf:type foaf:Person }}";
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
        String graphSentence = con.getGraphString(varargs[0]);
        String subjectSentence = "<" + varargs[1] + ">";
        String object = null;
        if (varargs[3].contains("^^")) {
            object = "\"" + StringEscapeUtils.escapeJava(varargs[3].substring(1, varargs[3].indexOf("^^") - 1)) + "\"" + varargs[3].substring(varargs[3].indexOf("^^"));
        } else {
            object = "\"" + StringEscapeUtils.escapeJava(varargs[3].substring(1, varargs[3].length() - 1)) + "\"" + (varargs.length > 4 ? varargs[4] != null ? "^^xsd:" + varargs[4] : "^^xsd:string" : "");
        }

        return con.INSERTDATA + graphSentence + "  { " + subjectSentence + " <" + varargs[2] + "> " + object + " }}";

    }

    /**
     * Return a INSERT QUERY when object is a URI
     *
     * @param varargs
     * @return
     */
    @Override
    public String getInsertDataUriQuery(String... varargs) {
        String graphSentence = con.getGraphString(varargs[0]);
        String subjectSentence = "<" + varargs[1] + ">";
        return con.INSERTDATA + graphSentence + " " + "{ " + subjectSentence + " <" + varargs[2] + "> <" + varargs[3] + "> }}";

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
    public String getEndpointDataQuery(String... arg) {
        String endpointsGraph = arg[0];
        String parameter = arg[1];
        String newValue = arg[2]; 
        String resourceHash = arg[3];
        String type = arg[4];
        boolean condition = "url".equals(parameter) || "graph".equals(parameter);
        if (condition){
            return con.INSERTDATA + con.getGraphString(endpointsGraph) + "{<" + con.ENDPOINTPREFIX + resourceHash + ">  <" + con.CENTRALGRAPHPREFIX + parameter + ">  <" + newValue + "> }}";
        } else {
            return con.INSERTDATA + con.getGraphString(endpointsGraph) + "{<" + con.ENDPOINTPREFIX + resourceHash + ">  <" + con.CENTRALGRAPHPREFIX + parameter + ">  '" + newValue + "'^^xsd:" + type + " }} ";
        }
    }
    
    @Override
    public String getlisEndpointsQuery(String endpointsGraph) {
        String id = " ?id ";
        return "SELECT DISTINCT ?id ?status ?name ?url ?graph ?fullName ?city ?province ?latitude ?longitude  WHERE {  "
                + " GRAPH <" + endpointsGraph + ">"
                + " {"
                + id + con.uc("status") + " ?status."
                + id + con.uc("name") + " ?name ."
                + id + con.uc("url") + " ?url."
                + id + con.uc("graph") + " ?graph."
                + id + con.uc("fullName") + " ?fullName."
                + id + con.uc("city") + " ?city."
                + id + con.uc("province") + " ?province."
                + id + con.uc("latitude") + " ?latitude."
                + id + con.uc("longitude") + " ?longitude."
                + "}}";
    }

    @Override
    public String getEndpointByIdQuery(String endpointsGraph, String id) {
        String idc = " ?id ";
        return "SELECT DISTINCT ?id ?status ?name ?url ?graph ?fullName ?city ?province ?latitude ?longitude  WHERE {  "
                + con.getGraphString(endpointsGraph)
                + " {"
                + idc + con.uc("status") + " ?status."
                + idc + con.uc("name") + " ?name ."
                + idc + con.uc("url") + " ?url."
                + idc + con.uc("graph") + " ?graph."
                + idc + con.uc("fullName") + " ?fullName."
                + idc + con.uc("city") + " ?city."
                + idc + con.uc("province") + " ?province."
                + idc + con.uc("latitude") + " ?latitude."
                + idc + con.uc("longitude") + " ?longitude."
                + " FILTER(?id = <" + id + ">)"
                + " }"
                + " }";
    }

    @Override
    public String getEndpointDeleteQuery(String endpointsGraph, String id) {

        return "DELETE { ?id ?p ?o } "
                + "WHERE"
                + " { "
                + con.getGraphString(endpointsGraph)
                + " { "
                + " ?id ?p ?o . "
                + " FILTER(?id = <" + id + ">) "
                + " } "
                + " }";
    }

    @Override
    public String getEndpointUpdateStatusQuery(String... args) {
        String estatus = con.uc("status");
        return " DELETE{ " + con.getGraphString(args[0])
                + " { <" + args[1] + "> " + estatus + " '" + args[2] + "'"
                + "}}"
                + "INSERT"
                + "{    " + con.getGraphString(args[0]) + "  {"
                + "         <" + args[1] + "> " + estatus + " '" + args[3] + "'"
                + "}    }"
                + "WHERE"
                + "{"
                + "   " + con.getGraphString(args[0]) + "  {"
                + "         <" + args[1] + "> " + estatus + " '" + args[2] + "'"
                + "}} ";
    }

    @Override
    public String getCountPersonQuery(String graph) {
        return con.PREFIX
                + " SELECT (COUNT(?s) as ?count) WHERE { " + con.getGraphString(graph) + " { ?s rdf:type foaf:Person. }}";
    }

    @Override
    public String getAuthorsDataQuery(String graph) {
        return con.PREFIX
                + " SELECT * "
                + " WHERE { " + con.getGraphString(graph) + " { "
                + " ?subject a foaf:Person. "
                + " ?subject foaf:name ?name."
                + " ?subject foaf:firstName ?fname."
                + " ?subject foaf:lastName ?lname."
                //                + " {"
                //                + " FILTER (regex(?name,\"Saquicela Galarza\"))"
                //                + " } UNION {"
                //                + " FILTER (regex(?name,\"Espinoza Mejia\"))"
                //                + " }"
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
        String graphSentence = con.getGraphString(varargs[0]);
        return "ASK { " + graphSentence + "{ <" + varargs[1] + "> <" + varargs[2] + "> <" + varargs[3] + "> } }";
    }

    @Override
    public String getPublicationsQuery(String providerGraph) {
        return " SELECT DISTINCT ?authorResource ?pubproperty ?publicationResource WHERE { "
                + con.getGraphString(providerGraph)
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
                + con.getGraphString(providerGraph)
                + " {  "
                + " ?authorResource owl:sameAs   ?authorNative. "
                + " ?authorNative ?pubproperty ?publicationResource. "
                + " filter (regex(?pubproperty,\"pub\")) "
                + " }  "
                + " }  ";
    }

    @Override
    public String getPublicationsPropertiesQuery(String providerGraph, String publicationResource) {
        return con.PREFIX
                + " SELECT DISTINCT ?publicationProperties ?publicationPropertyValue WHERE { "
                + con.getGraphString(providerGraph)
                + " {"
                + " <" + publicationResource + ">  ?publicationProperties ?publicationPropertyValue. "
                + " }} ";
    }

    @Override
    public String getMembersQuery() {
        return "SELECT DISTINCT ?members"
                + " WHERE { ?x " + con.foaf("member") + " ?members. } ";
    }

    @Override
    public String getPublicationFromProviderQuery() {
        return "SELECT DISTINCT ?authorResource ?publicationProperty  ?publicationResource "
                + " WHERE {  ?authorResource " + con.owl("sameAs") + " ?authorOtherResource. "
                + " ?authorOtherResource " + con.dblp("authorOf") + " ?publicationResource. "
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
                + " WHERE {  ?authorResource " + con.foaf("publications") + " ?publicationResource}";
    }

    @Override
    public String getPublicationPropertiesQuery() {
        return "SELECT DISTINCT ?publicationResource ?publicationProperties ?publicationPropertiesValue "
                + " WHERE { ?authorResource " + con.dblp("authorOf") + " ?publicationResource. ?publicationResource ?publicationProperties ?publicationPropertiesValue }";

    }

    @Override
    public String getPublicationMAPropertiesQuery() {
        return "SELECT DISTINCT ?publicationResource ?publicationProperties ?publicationPropertiesValue "
                + " WHERE { ?authorResource " + con.foaf("publications") + " ?publicationResource. ?publicationResource ?publicationProperties ?publicationPropertiesValue }";

    }

    @Override
    public String getAuthorPublicationsQuery(String providerGraph, String author, String prefix) {
        return " SELECT DISTINCT  ?authorResource  ?pubproperty  ?publicationResource "
                + "?title WHERE { "
                + con.getGraphString(providerGraph)
                + " { <" + author + "> " + con.foaf("publications") 
                + "?publicationResource.  ?publicationResource "
                + "<" + prefix + "> "
                + "?title } }";
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
                + "WHERE {" + con.getGraphString(providerGraph)
                + "{   ?authorResource owl:sameAs   ?authorNative.  "
                + "?authorNative ?pubproperty ?publicationResource.  "
                + "?publicationResource <" + prefix + ">  ?title\n"
                + "\n" + "{ FILTER (regex(?pubproperty,\"authorOf\")) }  "
                + "UNION { FILTER (regex(?pubproperty,\"pub\")) }                                                                                        }} ";
    }

    @Override
    public String getPublicationsCount(String graph) {
        return "SELECT  (COUNT(distinct ?publicationResource) AS ?total)WHERE {"
                + con.getGraphString(graph)
                + "                 {  "
                + "                 ?authorResource owl:sameAs   ?authorNative. "
                + "                 ?authorNative ?pubproperty ?publicationResource. "
                + "                 }}";
    }

    @Override
    public String getPublicationsCountCentralGraph() {
        return con.PREFIX
                + "               SELECT   (COUNT(distinct ?authorNative) as ?total) WHERE { "
                + con.getGraphString(con.getWkhuskaGraph())
                + "                                {  "
                + "                                 ?authorResource foaf:publications  ?authorNative"
                + "                                 }}";
    }

    @Override
    public String getTotalAuthorWithPublications(String graph) {
        return con.PREFIX
                + "SELECT   (COUNT(distinct ?authorResource) as ?total) WHERE { "
                + con.getGraphString(con.getWkhuskaGraph())
                + "                 { "
                + "                 ?authorResource foaf:publications  ?authorNative "
                + "                 }}";
    }

    @Override
    public String deleteDataGraph(String graph) {
        return "DELETE  { "
                + "   ?s ?p ?o } "
                + "where "
                + "{"
                + con.getGraphString(graph) 
                + " {"
                + "  ?s ?p ?o }"
                + "}";
    }
}
