package org.apache.marmotta.ucuenca.wk.commons.impl;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;

/**
 * @author Fernando Baculima
 */
public class Queries implements QueriesService {

    private final ConstantService con = new Constant();

    private final static String PREFIXES = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
            + " PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
            + " PREFIX owl: <http://www.w3.org/2002/07/owl#> "
            + " PREFIX dct: <http://purl.org/dc/terms/> "
            + " PREFIX mm: <http://marmotta.apache.org/vocabulary/sparql-functions#> "
            + " PREFIX dcat: <http://www.w3.org/ns/dcat#>"
            + " PREFIX bibo: <http://purl.org/ontology/bibo/>";

    private final static String OWLSAMEAS = "<http://www.w3.org/2002/07/owl#sameAs>";

    private final static String INSERTDATA = "INSERT DATA { ";

    private final static String ENDPOINTPREFIX = "http://ucuenca.edu.ec/wkhuska/endpoint/";

    @Override
    public String getAuthorsQuery(String datagraph) {
        return PREFIXES
                + " SELECT DISTINCT ?s WHERE {" + getGraphString(datagraph) + "{ ?s rdf:type foaf:Person }}";
    }

    @Override
    public String getRetrieveResourceQuery() {
        return "SELECT ?x ?y ?z WHERE { ?x ?y ?z }";
    }

    /**
     * Return a INSERT QUERY when object is a LITERAL
     */
    @Override
    public String getInsertDataLiteralQuery(String... varargs) {
        String graphSentence = getGraphString(varargs[0]);
        String subjectSentence = "<" + varargs[1] + ">";
        String object = null;
        if (varargs[3].contains("^^")) {
            object = "\"" + StringEscapeUtils.escapeJava(varargs[3].substring(1, varargs[3].indexOf("^^") - 1)) + "\"" + varargs[3].substring(varargs[3].indexOf("^^"));
        } else {
            object = "\"" + StringEscapeUtils.escapeJava(varargs[3].substring(1, varargs[3].length() - 1)) + "\"" + (varargs.length > 4 ? varargs[4] != null ? "^^xsd:" + varargs[4] : "^^xsd:string" : "^^xsd:string");
        }

        if (isURI(varargs[2])) {
            return INSERTDATA + graphSentence + "  { " + subjectSentence + " <" + varargs[2] + "> " + object + " }}";
        } else {
            return PREFIXES + INSERTDATA + graphSentence + "  { " + subjectSentence + " " + varargs[2] + " " + object + " }}";
        }
    }

    /**
     * Return a INSERT QUERY when object is a URI
     */
    @Override
    public String getInsertDataUriQuery(String... varargs) {
        String graphSentence = getGraphString(varargs[0]);
        String subjectSentence = "<" + varargs[1] + ">";
        if (isURI(varargs[2])) {
            return INSERTDATA + graphSentence + " " + "{ " + subjectSentence + " <" + varargs[2] + "> <" + varargs[3] + "> }}";
        } else {
            return PREFIXES + INSERTDATA + graphSentence + " " + "{ " + subjectSentence + " " + varargs[2] + " <" + varargs[3] + "> }}";
        }
    }

    /**
     * Return true or false if object is a URI
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
        return mat.matches(); // return "http".equals(url.getProtocol()) || "https".equals(url.getProtocol()) ;
    }

    /**
     * Return ASK query for a resource
     */
    @Override
    public String getAskResourceQuery(String graph, String resource) {
        return "ASK FROM <" + graph + "> { <" + resource + "> ?p ?o }";
    }

    /**
     * Return ASK property query for a resource
     */
    @Override
    public String getAskResourcePropertieQuery(String graph, String resource, String property) {
        if (isURI(property)) {
            return "ASK FROM <" + graph + "> {  <" + resource + ">    <" + property + "> ?o }";
        } else {
            return PREFIXES + "ASK FROM <" + graph + "> {  <" + resource + "> " + property + " ?o }";
        }
    }

    @Override
    public String getEndpointDataQuery(String... arg) {
        String endpointsGraph = arg[0];
        String parameter = arg[1];
        String newValue = arg[2];
        String resourceHash = arg[3];
        String type = arg[4];
        boolean condition = "url".equals(parameter) || "graph".equals(parameter);
        if (condition) {
            return INSERTDATA + getGraphString(endpointsGraph) + "{<" + ENDPOINTPREFIX + resourceHash + ">  " + con.uc(parameter) + "  <" + newValue + "> }}";
        } else {
            return INSERTDATA + getGraphString(endpointsGraph) + "{<" + ENDPOINTPREFIX + resourceHash + ">  " + con.uc(parameter) + "   '" + newValue + "'^^xsd:" + type + " }} ";
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
                + getGraphString(endpointsGraph)
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
                + "WHERE { "
                + con.getGraphString(endpointsGraph)
                + " {   ?id ?p ?o . "
                + "     FILTER(?id = <" + id + ">) "
                + " } "
                + " }";
    }

    @Override
    public String getEndpointUpdateStatusQuery(String... args) {
        String status = con.uc("status");
        return " DELETE { " + getGraphString(args[0]) + " { "
                + "     <" + args[1] + "> " + status + " ?status "
                + " }} "
                + " INSERT  { "
                + getGraphString(args[0]) + "  {"
                + "             <" + args[1] + "> " + status + " '" + args[3] + "'^^xsd:boolean"
                + " }       } "
                + "WHERE { "
                + getGraphString(args[0]) + "  { "
                + "             <" + args[1] + "> " + status + " ?status"
                + "             FILTER (regex(?status,'" + args[2] + "')) "
                + " }   } ";
    }

    @Override
    public String getCountPersonQuery(String graph) {
        return PREFIXES
                + " SELECT (COUNT(?s) as ?count) WHERE { " + getGraphString(graph) + " { ?s rdf:type foaf:Person. }}";
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
    public String getRetrieveKeysQuery() {
        return " PREFIX dct: <http://purl.org/dc/terms/>  "
                + " SELECT ?x ?y ?z WHERE { ?x dct:subject ?z. ?x ?y ?z. }";
    }

    @Override
    public String getAuthorsDataQuery(String graph, String endpointsgraph) {
        return PREFIXES
                + " SELECT *"
                + " WHERE { " + getGraphString(graph) + " { "
                //+ " WHERE { graph <http://ucuenca.edu.ec/wkhuska/authorsaux> {"
                + " ?subject a foaf:Person. "
                + " ?subject foaf:name ?name."
                + " ?subject foaf:firstName ?fname. "
                + " ?subject foaf:lastName ?lname. "
                + " ?subject dct:provenance ?provenance. "
                //                + ""
                //                + "{"
                //                + " filter (regex(UCASE(?subject), \"SAQUICELA\"))"
                //                + "filter (regex(UCASE(?subject), \"GALARZA\"))  "
                //                + "    }"
                //                + "UNION "
                //                + "{"
                //                + "filter (regex(UCASE(?subject), \"ESPINOZA\")) "
                //                + "filter (regex(UCASE(?subject), \"MAURICIO\")) "
                //                + "}"
                //                + "UNION {"
                //                + "filter (regex(UCASE(?subject), \"CARVALLO\"))  "
                //                + "filter (regex(UCASE(?subject), \"JUAN\"))     "
                //                + "}"
                //                + " UNION {"
                //                + " filter (regex(UCASE(?subject), \"FELIPE\"))  "
                //                + "filter (regex(UCASE(?subject), \"CISNEROS\"))   "
                //                + "  } UNION"
                //                + " {"
                //                + "  filter (regex(UCASE(?subject), \"NELSON\"))  "
                //                + "  filter (regex(UCASE(?subject), \"PIEDRA\"))   "
                //                + " } UNION"
                //                + " {"
                //                + " filter (regex(UCASE(?subject), \"LIZANDRO\"))  "
                //                + "  filter (regex(UCASE(?subject), \"SOLANO\"))     "
                //                + "} "
                + " { select ?status "
                + "     where { " + getGraphString(endpointsgraph) + " {"
                + "     ?provenance <http://ucuenca.edu.ec/ontology#status> ?status "
                + " }}} filter (regex(?status,\"true\")) "
                + "                }} ";

    }

    /**
     * get list of graphs query
     */
    @Override
    public String getGraphsQuery() {
        return " SELECT DISTINCT ?grafo WHERE { "
                + " graph ?grafo {?x ?y ?z } "
                + " } ";
    }

    /**
     * Return ASK query for triplet
     */
    @Override
    public String getAskQuery(String... varargs) {
        String graphSentence = getGraphString(varargs[0]);
        return "ASK { " + graphSentence + "{ <" + varargs[1] + "> <" + varargs[2] + "> <" + varargs[3] + "> } }";
    }

    @Override
    public String getPublicationsQuery(String providerGraph) {
        return " SELECT DISTINCT ?authorResource ?pubproperty ?publicationResource WHERE { "
                + getGraphString(providerGraph)
                + " {  "
                + " ?authorResource " + OWLSAMEAS + "   ?authorNative. "
                + " ?authorNative ?pubproperty ?publicationResource. "
                + " { FILTER (regex(?pubproperty,\"authorOf\")) } "
                + " UNION"
                + " { FILTER (regex(?pubproperty,\"pub\")) } "
                + " }}";
    }

    @Override
    public String getPublicationsMAQuery(String providerGraph) {
        return " SELECT DISTINCT ?authorResource ?pubproperty ?publicationResource WHERE { "
                + getGraphString(providerGraph)
                + " {  "
                + " ?authorResource owl:sameAs   ?authorNative. "
                + " ?authorNative ?pubproperty ?publicationResource. "
                + " filter (regex(?pubproperty,\"pub\")) "
                + " }  "
                + " }  ";
    }

    @Override
    public String getPublicationsPropertiesQuery(String providerGraph, String publicationResource) {
        return PREFIXES
                + " SELECT DISTINCT ?publicationProperties ?publicationPropertyValue WHERE { "
                + getGraphString(providerGraph)
                + " {"
                + " <" + publicationResource + ">  ?publicationProperties ?publicationPropertyValue. "
                + " } }"
                + "ORDER BY DESC(?publicationProperties) ";
    }

    /**
     * To get Members from DBLP
     *
     * @return
     */
    @Override
    public String getMembersQuery() {
        return "SELECT DISTINCT ?members"
                + " WHERE { ?x " + con.foaf("member") + " ?members. } ";
    }

    /**
     * To get Publications Members from MA
     *
     * @return
     */
    @Override
    public String getMembersByTitleQuery() {
        return "SELECT DISTINCT ?members"
                + " WHERE { ?x " + con.foaf("publications") + " ?members. } ";
    }

    @Override
    public String getPublicationFromProviderQuery() {
        return "SELECT DISTINCT ?authorResource ?publicationProperty  ?publicationResource "
                + " WHERE {  ?authorResource " + OWLSAMEAS + " ?authorOtherResource. "
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
                + " WHERE {  ?authorResource <http://xmlns.com/foaf/0.1/publications> ?publicationResource}";
    }

    @Override
    public String getPublicationPropertiesQuery() {
        return "SELECT DISTINCT ?publicationResource ?publicationProperties ?publicationPropertiesValue "
                + " WHERE { ?authorResource " + con.dblp("authorOf") + " ?publicationResource. ?publicationResource ?publicationProperties ?publicationPropertiesValue }";
    }

    @Override
    public String getPublicationMAPropertiesQuery() {
        return "SELECT DISTINCT ?publicationResource ?publicationProperties ?publicationPropertiesValue "
                + " WHERE { ?authorResource <http://xmlns.com/foaf/0.1/publications> ?publicationResource. ?publicationResource ?publicationProperties ?publicationPropertiesValue }";
    }

    @Override
    public String getAllTitlesDataQuery(String graph) {

        return PREFIXES + "SELECT DISTINCT  ?publications ?title "
                + "FROM <" + graph + "> "
                + " WHERE { ?publications dct:title ?title  } ";
    }

    @Override
    public String getAbstractQuery(String resource) {
        return PREFIXES + " SELECT DISTINCT ?abstract "
                + " WHERE {  <" + resource + "> bibo:abstract  ?abstract } ";
    }

    @Override
    public String getKeywordsQuery(String resource) {
        return PREFIXES + " SELECT DISTINCT ?keyword "
                + " WHERE {  <" + resource + "> bibo:Quote ?keyword. } ";
    }

    @Override
    public String getPublicationPropertiesAsResourcesQuery() {
        return "SELECT DISTINCT ?publicationResource ?publicationProperties ?publicationPropertiesValue "
                + " WHERE { ?authorResource <http://xmlns.com/foaf/0.1/publications> ?publicationResourceItem. ?publicationResourceItem ?publicationPropertiesItem ?publicationResource. ?publicationResource ?publicationProperties ?publicationPropertiesValue }";
    }

    @Override
    public String getAuthorPublicationsQuery(String... varargs) {
        return PREFIXES
                + " SELECT DISTINCT  ?authorResource  ?pubproperty  ?publicationResource ?title "
                + " WHERE "
                + "{ "
                + getGraphString(varargs[0])
                + "     { "
                + "         <" + varargs[1] + "> " + con.foaf("publications") + " ?publicationResource. "
                + "         ?publicationResource <" + varargs[2] + "> ?title "
                + "         FILTER( mm:fulltext-query(str(?title),\"" + varargs[3] + "\"))"
                + "     } "
                + "}";
    }

    @Override
    public String getAuthorPublicationsQueryFromProvider(String... varargs) {
        return "PREFIX query: <http://marmotta.apache.org/vocabulary/sparql-functions#>	"
                + " SELECT DISTINCT  ?pubproperty ?publicationResource ?title  "
                + "WHERE {  graph <" + varargs[0] + ">  "
                + "{    <" + varargs[1] + "> "
                + "owl:sameAs   ?authorNative.  ?authorNative ?pubproperty ?publicationResource.  "
                + "?publicationResource <" + varargs[2] + ">  ?title\n"
                + "\n"
                + "}} ";
    }

    @Override
    public String getPublicationDetails(String publicationResource) {
        return " SELECT DISTINCT ?property ?hasValue  WHERE { "
                + "  { <" + publicationResource + "> ?property ?hasValue } "
                + " UNION "
                + "  { ?isValueOf ?property <" + publicationResource + "> } "
                + " } "
                + " ORDER BY ?property ?hasValue ?isValueOf";
    }

    @Override
    public String getPublicationsTitleQuery(String providerGraph, String prefix) {
        return ""
                + " SELECT DISTINCT ?authorResource ?pubproperty ?publicationResource ?title "
                + "WHERE {" + getGraphString(providerGraph)
                + "{   ?authorResource " + OWLSAMEAS + "   ?authorNative.  "
                + "?authorNative ?pubproperty ?publicationResource.  "
                + "?publicationResource <" + prefix + ">  ?title\n"
                + "\n" + "{ FILTER (regex(?pubproperty,\"authorOf\")) }  "
                + "UNION { FILTER (regex(?pubproperty,\"pub\")) }                                                                                        }} ";
    }

    @Override
    public String getPublicationsCount(String graph) {
        return "SELECT  (COUNT(distinct ?publicationResource) AS ?total)WHERE {"
                + getGraphString(graph)
                + "                 {  "
                + "                 ?authorResource " + OWLSAMEAS + "   ?authorNative. "
                + "                 ?authorNative ?pubproperty ?publicationResource. "
                + "                 }}";
    }

    @Override
    public String getPublicationsCountCentralGraph() {
        return "PREFIX query: <http://marmotta.apache.org/vocabulary/sparql-functions#>	"
                + "               SELECT   (COUNT(distinct ?publications) as ?total) WHERE { "
                + getGraphString(con.getWkhuskaGraph())
                + "                                {  "
                + "                                 ?authorResource <http://xmlns.com/foaf/0.1/publications>  ?publications"
                + "                                 }}";
    }

    @Override
    public String getTotalAuthorWithPublications(String graph) {
        return "PREFIX query: <http://marmotta.apache.org/vocabulary/sparql-functions#>	"
                + "SELECT   (COUNT(distinct ?authorResource) as ?total) WHERE { "
                + getGraphString(con.getWkhuskaGraph())
                + "                 { "
                + "                 ?authorResource <http://xmlns.com/foaf/0.1/publications>  ?publications "
                + "                 }}";
    }

    @Override
    public String deleteDataGraph(String graph) {
        return " DELETE  { ?s ?p ?o } where { " + getGraphString(graph) + " { ?s ?p ?o } }";
    }

    @Override
    public String getTitlePublications(String graph) {
        return PREFIXES
                + " SELECT *  WHERE { graph <" + graph + "> "
                + "  { ?authorResource <http://xmlns.com/foaf/0.1/publications>  ?publicationResource. "
                + "   ?publicationResource dct:title ?title "
                + "  } "
                + "}";
    }

    @Override
    public String getFirstNameLastNameAuhor(String graph, String authorResource) {
        return PREFIXES
                + " SELECT distinct (str(?firstname) as ?fname) (str(?lastname) as ?lname) from <" + graph + "> WHERE { "
                + "                <" + authorResource + "> a foaf:Person; "
                + "                 foaf:firstName  ?firstname;"
                + "                 foaf:lastName   ?lastname;  "
                + "}";
    }

    @Override
    public String authorDetailsOfProvenance(String graph, String authorResource) {
        return " SELECT DISTINCT ?property ?hasValue  WHERE { "
                + "  graph <" + graph + ">{ "
                + "  { <" + authorResource + "> ?property ?hasValue } "
                + " UNION "
                + "  { ?isValueOf ?property <" + authorResource + "> } "
                + " }} "
                + "ORDER BY ?property ?hasValue ?isValueOf";
    }

    @Override
    public String getAuthorPublicationFilter(String graph, String fname, String lname) {
        return PREFIXES
                + " SELECT distinct ?authorResource  ?publicationResource ?title  WHERE { "
                + " graph <" + graph + "> "
                + "                  {  "
                + "                   ?authorResource  foaf:firstName ?fname. "
                + "                    ?authorResource foaf:lastName  ?lname. "
                + "                   ?authorResource foaf:publications   ?publicationResource. "
                + "                   ?publicationResource dct:title ?title "
                + "					    {FILTER( mm:fulltext-query(str(?fname), \"" + fname + "\")  "
                + "                                               && mm:fulltext-query(str(?lname), \"" + lname + "\")) "
                + "                   }}}";
    }

    @Override
    public String getAskProcessAlreadyAuthorProvider(String providerGraph, String authorResource) {
        return PREFIXES
                + " ASK FROM <" + providerGraph + "> {  <" + authorResource + "> " + OWLSAMEAS + "  ?o }";
    }

    public String getGraphString(String graph) {
        return " GRAPH <" + graph + "> ";
    }

    @Override
    public String getSourcesfromUniqueEndpoit(String graph) {

        return PREFIXES
                + "SELECT DISTINCT ?dataset ?nameu "
                + " FROM <" + graph + ">"
                + " WHERE { "
                + "{ "
                + " ?Universidad dcat:dataset ?dataset. "
                + " ?Universidad rdfs:label ?nameu. "
                + " ?Universidad dct:location <http://dbpedia.org/resource/Ecuador>. "
                + " FILTER (REGEX(?nameu , '@es')) "
                + "} "
                + "UNION "
                + "{ "
                + "  ?Universidad dcat:dataset ?dataset. "
                + "  ?Universidad rdfs:label ?nameu.  "
                + "  ?Universidad dct:location <http://dbpedia.org/resource/Ecuador>. "
                + "  FILTER (!regex(?nameu , '@'))  "
                + "} "
                + "} ";
    }

    @Override
    public String getDocumentsAuthors(String repository, String graph) {
        return PREFIXES
                + " SELECT  DISTINCT ?document ?author "
                + " FROM <" + graph + ">"
                + " WHERE { "
                + "   ?document dct:isPartOf <" + repository + ">. "
                + "   ?document dct:creator ?author. "
                + " }";
    }

    /**
     * Función que elimina acentos y caracteres especiales
     *
     * @param value
     * @return cadena de texto limpia de acentos y caracteres especiales.
     */
    @Override
    public String removeAccents(String value) {
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
}
