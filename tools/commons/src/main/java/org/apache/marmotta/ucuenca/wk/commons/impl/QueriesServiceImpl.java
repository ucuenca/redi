package org.apache.marmotta.ucuenca.wk.commons.impl;

import com.hp.hpl.jena.sparql.vocabulary.FOAF;
//import com.hp.hpl.jena.vocabulary.OWL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
//import java.util.Map;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Provider;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.openrdf.model.vocabulary.RDF;

/**
 * @author Fernando Baculima
 * @author Xavier Sumba
 * @author Jose Cullcay
 */
@SuppressWarnings("PMD")
public class QueriesServiceImpl implements QueriesService {

  @Inject
  private ConstantService con;//= new ConstantServiceImpl();
  @Inject
  private CommonsServices commonsServices;//= new CommonsServicesImpl();

  private final static String PREFIXES = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
          + " PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
          + " PREFIX owl: <http://www.w3.org/2002/07/owl#> "
          + " PREFIX dct: <http://purl.org/dc/terms/> "
          + " PREFIX mm: <http://marmotta.apache.org/vocabulary/sparql-functions#> "
          + " PREFIX dcat: <http://www.w3.org/ns/dcat#> "
          + " PREFIX bibo: <http://purl.org/ontology/bibo/> "
          + " PREFIX dc: <http://purl.org/dc/elements/1.1/> "
          + " PREFIX uc: <http://ucuenca.edu.ec/ontology#> "
          + " PREFIX schema: <http://schema.org/> "
          + " PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
          + " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
          + " PREFIX cerif: <https://www.openaire.eu/cerif-profile/1.1/>"
          + " PREFIX cerif3: <http://www.eurocris.org/ontologies/cerif/1.3#>";

  private final static String OWLSAMEAS = "<http://www.w3.org/2002/07/owl#sameAs>";

  private final static String INSERTDATA = "INSERT DATA { ";

  private final static String ENDPOINTPREFIX = "http://ucuenca.edu.ec/wkhuska/endpoint/";

  private String id = " ?id ";

  private String fullName = "fullName";

  private String status = "status";

  private String url = "url";

  private String graph = "graph";

  @Override
  public String getAuthorsQuery(String datagraph, String num, Boolean mode) {
    if (mode) {
      return PREFIXES
              + " SELECT DISTINCT ?s WHERE { "
              + "                 ?s a foaf:Person . "
              + "                  ?docu ?property ?s  . "
              + "                  ?docu a bibo:Article. "
              + "}  GROUP BY ?s HAVING (count(?docu)> 0  )";

    } else {
      return PREFIXES
              + " SELECT DISTINCT ?s WHERE {  "
              + "                 ?s a foaf:Person . "
              + "                  { "
              + "                  ?article ?property  ?s   . "
              + "                   ?article a bibo:Article "
              + "                  }UNION { "
              + "                    ?docu ?property  ?s  . "
              + "                  ?docu a bibo:Document "
              + "                   } "
              + "} GROUP BY ?s HAVING (count(?docu)> " + num + " || count (?article) > 0  ) ";
    }

    /* return PREFIXES
         + " SELECT ?s WHERE { " + getGraphString(datagraph) + "{"
         + " ?doc rdf:type bibo:Document ;"
         + " ?c ?s ."
         + "?s a foaf:Person."
         + "} }"
         + " GROUP BY ?s"
         + " HAVING (count(?doc)>" + num + ")";*/
  }

  @Override
  public String getRetrieveResourceQuery() {
    return "SELECT ?x ?y ?z WHERE { ?x ?y ?z }";
  }

  @Override
  public String getInsertDataLiteralQuery(String graph, String subject, String predicate, String object) {
    return getInsertDataLiteralQuery(graph, subject, predicate, object, null);
  }

  /**
   * Return a INSERT QUERY when object is a LITERAL
   */
  @Override
  public String getInsertDataLiteralQuery(String graph, String subject, String predicate, String object, String datatype) {
    String subjectSentence = "<" + subject + ">";
    String graphSentence = "GRAPH <" + graph + ">";
    if (datatype != null) {
      object = "\"" + object + "\"^^xsd:" + datatype;
    } else {
      object = "\"" + object + "\"^^xsd:string";
    }

    if (isURI(predicate)) {
      return INSERTDATA + graphSentence + "  { " + subjectSentence + " <" + predicate + "> " + object + " }}";
    } else {
      return PREFIXES + INSERTDATA + graphSentence + "  { " + subjectSentence + " " + predicate + " " + object + " }}";
    }
  }

  private boolean isURI(String value) {
    return commonsServices.isURI(value);
  }

  /**
   * Return a INSERT QUERY when object is a URI
   *
   * @param varargs
   */
  @Override
  @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
  public String getInsertDataUriQuery(String... varargs) {
    String graphSentence = getGraphString(varargs[0]);
    String subjectSentence = "<" + varargs[1] + ">";
    if (isURI(varargs[2])) {
      return INSERTDATA + graphSentence + " " + "{ " + subjectSentence + " <" + varargs[2] + "> <" + varargs[3] + "> }}  ";
    } else {
      return PREFIXES + INSERTDATA + graphSentence + " " + "{ " + subjectSentence + " " + varargs[2] + " <" + varargs[3] + "> }}";
    }
  }

  @Override
  public String buildInsertQuery(String... args) {
    String graph = args[0];
    String sujeto = args[1];
    String predicado = args[2];
    String objeto = args[3];
    if (isURI(objeto)) {
      return getInsertDataUriQuery(graph, sujeto, predicado, objeto);
    }
    return getInsertDataLiteralQuery(graph, sujeto, predicado, objeto);
  }

  /**
   * Return ASK query for a resource
   */
  @Override
  public String getAskResourceQuery(String graph, String resource) {
    return "ASK FROM <" + graph + "> { <" + resource + "> ?p ?o }";
  }

  @Override
  public String getAskObjectQuery(String graph, String resource, String filterexpr) {
    return "ASK FROM <" + graph + "> { "
            + "?s ?p <" + resource + "> "
            + "FILTER REGEX(STR(?s), \".*" + filterexpr + ".*\")}";
  }

  @Override
  @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
  public String getAskObjectQuery(String graph, String object) {
    return "ASK FROM  <" + graph + "> { ?s ?p <" + object + "> }";
  }

  @Override
  public String getAskAcademicsQuery(String graph, String object) {
    object = object.substring(0, object.indexOf("subscription-key"));
    return "ASK FROM  <" + graph + "> { "
            + "?author <" + REDI.ACADEMICS_KNOWLEDGE_URl + "> ?s."
            + "FILTER(STRSTARTS(?s, <" + object + ">))  "
            + "}";
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
  @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
  public String getInsertEndpointQuery(String resourceHash, String property, String object, String literal) {
    String graph = con.getEndpointsGraph();
    String resource = con.getEndpointResource() + resourceHash;
    if (isURI(object)) {
      return INSERTDATA + getGraphString(graph) + "{<" + resource + ">  <" + property + ">  <" + object + "> }}";
    } else {
      return INSERTDATA + getGraphString(graph) + "{<" + resource + ">  <" + property + "> '" + object + "'" + literal + " }}  ";
    }
  }

  @Override
  public String getInsertOrganizationQuery(String resourceHash, String property, String object, String literal) {
    String graph = con.getOrganizationsGraph();
    String resource = con.getOrganizationBaseUri() + resourceHash;
    if (isURI(object)) {
      return INSERTDATA + getGraphString(graph) + "{<" + resource + ">  <" + property + ">  <" + object + "> }}";
    } else {
      return INSERTDATA + getGraphString(graph) + "{<" + resource + ">  <" + property + "> '" + object + "'" + literal + " }}  ";
    }
  }

  @Override
  public String getInsertGeneric(String graph, String resource, String property, String object, String literal) {
    // String graph = con.getOrganizationsGraph();
    // String resource = con.getOrganizationBaseUri() + resourceHash;
    if (isURI(object)) {
      return INSERTDATA + getGraphString(graph) + "{<" + resource + ">  <" + property + ">  <" + object + "> }}";
    } else {
      return INSERTDATA + getGraphString(graph) + "{<" + resource + ">  <" + property + "> '" + object + "'" + literal + " }}  ";
    }
  }

  @Override
  @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
  public String getLisEndpointsQuery() {
    return "SELECT DISTINCT ?id ?status ?name ?url ?graph (concat(?fName, \" - \", ?engName) as ?fullName) ?city ?province ?latitude ?longitude  WHERE {  "
            + "   GRAPH  <" + con.getEndpointsGraph() + ">"
            + " {"
            + id + con.uc(status) + " ?status;"
            + con.uc("name") + " ?name;"
            + con.uc(url) + " ?url;"
            + con.uc(graph) + " ?graph;"
            + con.uc(fullName) + " ?fName;"
            + con.uc(fullName) + "?engName;"
            + con.uc("city") + " ?city;"
            + con.uc("province") + " ?province;"
            + con.uc("latitude") + " ?latitude;"
            + con.uc("longitude") + " ?longitude."
            + " FILTER (langMatches(lang(?fName), 'es') && langMatches(lang(?engName),'en')) .  "
            + "}}";
  }

  @Override
  @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
  public String getListOrganizationQuery() {
    return "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
            + "PREFIX REDI: <http://ucuenca.edu.ec/ontology#>"
            + "select DISTINCT ?URI ?name ?fullNameEn  ?fullNameEs ?alias ?country ?province ?city ?type ?lang ?long "
            + "FROM <" + con.getOrganizationsGraph() + ">"
            + " where "
            + " {   ?URI <" + RDF.TYPE.toString() + "> <" + FOAF.Organization.toString() + ">  . "
            + "?URI  <" + REDI.NAME.toString() + "> ?name  ."
            + "OPTIONAL { ?URI  <" + REDI.FULLNAME.toString() + "> ?fullNameEs . "
            + "FILTER (langMatches(lang(?fullNameEs), 'es')) } ."
            + "OPTIONAL { ?URI  <" + REDI.FULLNAME.toString() + "> ?fullNameEn  ."
            + "FILTER (langMatches(lang(?fullNameEn), 'en')) } ."
            + "OPTIONAL { ?URI <" + REDI.ALIAS.toString() + "> ?alias } . "
            + "OPTIONAL { ?URI <" + REDI.COUNTRY.toString() + "> ?country } . "
            + "OPTIONAL { ?URI <" + REDI.PROVINCE.toString() + "> ?province } ."
            + "OPTIONAL { ?URI <" + REDI.CITY.toString() + "> ?city } . "
            + "OPTIONAL { ?URI <" + REDI.TYPE.toString() + "> ?type } ."
            + "OPTIONAL { ?URI <" + REDI.LATITUDE.toString() + "> ?lang } . "
            + "OPTIONAL { ?URI <" + REDI.LONGITUDE.toString() + "> ?long } . "
            + "}";
    //  + "FILTER (langMatches(lang(?fullNameEn), 'en') && langMatches(lang(?fullNameEs), 'es'))    } ";

  }

  @Override
  @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
  public String getOrgByUri(String uri) {
    return "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
            + "PREFIX REDI: <http://ucuenca.edu.ec/ontology#>"
            + "select DISTINCT ?URI ?name ?fullNameEn  ?fullNameEs ?alias ?scopusId ?country ?province ?city ?type ?lang ?long "
            + "FROM <" + con.getOrganizationsGraph() + ">"
            + " where "
            + "{   <" + uri + "> <" + RDF.TYPE.toString() + "> <" + FOAF.Organization.toString() + "> . "
            + "<" + uri + ">  <" + REDI.NAME.toString() + "> ?name  ."
            + "OPTIONAL { <" + uri + ">  <" + REDI.FULLNAME.toString() + "> ?fullNameEs . "
            + "FILTER (langMatches(lang(?fullNameEs), 'es')) } ."
            + "OPTIONAL { <" + uri + ">  <" + REDI.FULLNAME.toString() + "> ?fullNameEn . "
            + "FILTER (langMatches(lang(?fullNameEn), 'en')) } ."
            + "OPTIONAL { <" + uri + "> <" + REDI.ALIAS.toString() + "> ?alias } . "
            + "OPTIONAL { <" + uri + "> <http://vivoweb.org/ontology/core#scopusId> ?scopusId } . "
            + "OPTIONAL { <" + uri + "> <" + REDI.COUNTRY.toString() + "> ?country } . "
            + "OPTIONAL { <" + uri + "> <" + REDI.PROVINCE.toString() + "> ?province } ."
            + "OPTIONAL { <" + uri + "> <" + REDI.CITY.toString() + "> ?city } . "
            + "OPTIONAL { <" + uri + "> <" + REDI.TYPE.toString() + "> ?type } ."
            + "OPTIONAL { <" + uri + "> <" + REDI.LATITUDE.toString() + "> ?lang } . "
            + "OPTIONAL { <" + uri + "> <" + REDI.LONGITUDE.toString() + "> ?long } . "
            + "} ";

  }

  @Override
  @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
  public String getListEndpoints() {
    return "PREFIX REDI: <http://ucuenca.edu.ec/ontology#>"
            + "SELECT DISTINCT ?URI ?status ?url ?graph ?type ?org ?date"
            + " FROM <" + con.getEndpointsGraph() + "> "
            + "WHERE {"
            + "?URI <" + RDF.TYPE.toString() + ">  <" + REDI.ENDPOINT.toString() + "> ."
            + "OPTIONAL { ?URI    <" + REDI.STATUS.toString() + "> ?status } ."
            + "OPTIONAL { ?URI   <" + REDI.URL.toString() + ">    ?url } ."
            + "OPTIONAL { ?URI   <" + REDI.GRAPH.toString() + ">   ?graph } ."
            + "OPTIONAL { ?URI   <" + REDI.TYPE.toString() + ">   ?type } ."
            + "OPTIONAL { ?URI   <" + REDI.BELONGTO.toString() + ">  ?org } . "
            + "OPTIONAL { ?URI   <" + REDI.EXTRACTIONDATE.toString() + ">  ?date }"
            + "}";
  }

  @Override
  @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
  public String getListEndpointsByUri(String uri) {
    return "PREFIX REDI: <http://ucuenca.edu.ec/ontology#>"
            + "SELECT DISTINCT  ?status ?url ?graph ?type ?org ?date ?mode"
            + " FROM <" + con.getEndpointsGraph() + "> "
            + "WHERE {"
            + "<" + uri + "> <" + RDF.TYPE.toString() + ">  <" + REDI.ENDPOINT.toString() + "> ."
            + "OPTIONAL { <" + uri + ">   <" + REDI.STATUS.toString() + "> ?status } ."
            + "OPTIONAL { <" + uri + ">  <" + REDI.URL.toString() + ">    ?url } ."
            + "OPTIONAL { <" + uri + ">   <" + REDI.GRAPH.toString() + ">   ?graph } ."
            + "OPTIONAL { <" + uri + ">   <" + REDI.TYPE.toString() + ">   ?type } ."
            + "OPTIONAL { <" + uri + ">   <" + REDI.BELONGTO.toString() + ">  ?org } . "
            + "OPTIONAL { <" + uri + ">   <" + REDI.EXTRACTIONDATE.toString() + ">  ?date }"
            + "OPTIONAL { <" + uri + ">   <" + REDI.EXTRACTION_MODE.toString() + ">  ?mode }"
            + "} limit 1";
  }

  @Override
  public String getlisEndpointsQuery(String endpointsGraph) {
    return "SELECT DISTINCT ?id ?status ?name ?url ?graph (concat(?fName, \" - \", ?engName) as ?fullName) ?city ?province ?latitude ?longitude  WHERE {  "
            + "  GRAPH <" + con.getEndpointsGraph() + ">"
            + " GRAPH <" + endpointsGraph + ">"
            + " {"
            + id + con.uc(status) + " ?status."
            + id + con.uc("name") + " ?name ."
            + id + con.uc(url) + " ?url."
            + id + con.uc(graph) + " ?graph."
            + id + con.uc(fullName) + " ?fName."
            + id + con.uc(fullName) + "?engName."
            + id + con.uc("city") + " ?city."
            + id + con.uc("province") + " ?province."
            + id + con.uc("latitude") + " ?latitude."
            + id + con.uc("longitude") + " ?longitude."
            + " FILTER (lang(?fName) = 'es') . "
            + " FILTER (lang(?engName) = 'en') . "
            + "}}";
  }

  @Override
  public String getExtractedOrgListD(List<Provider> providers) {
    String varprov = "";
    String prov = "";
    String lspro = "";
    String varprovl = "";

    for (Provider p : providers) {
      varprov = " (group_concat(  ?label" + p.Name + "  ; separator=\";\")  as  ?Adv" + p.Name + " ) " + varprov;
      lspro += " <" + p.Graph + "> ";
      if (p.Name.equals("Ojs")) {
        prov += "bind (if(regex(str(?event), 'Ojs','i'),?labelp,'') as ?label" + p.Name + ") .\n";
      } else if (p.Name.equals("Dspace")) {
        prov += "bind (if(regex(str(?event), 'Dspace','i'),?labelp,'') as ?label" + p.Name + ") .\n";
      } else if (p.Name.equals("AcademicsKnowledge")) {
        prov += "bind (if(regex(str(?event), 'Academics','i'),?labelp,'') as ?label" + p.Name + ") .\n";
      } else if (p.Name.equals("GoogleScholar")) {
        prov += "bind (if(regex(str(?event), 'Google','i'),?labelp,'') as ?label" + p.Name + ") .\n";
      } else {
        prov += "bind (if(regex(str(?event), '" + p.Name + "','i'),?labelp,'') as ?label" + p.Name + ") .\n";
      }
    }

    String head = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
            + "PREFIX ucmodel: <http://ucuenca.edu.ec/ontology#> "
            + "SELECT  ?uri ?name " + varprov + " {"
            + "GRAPH <" + con.getOrganizationsGraph() + "> { \n"
            + "		?uri <http://ucuenca.edu.ec/ontology#name> ?name .\n"
            + "		?uri <http://ucuenca.edu.ec/ontology#belongTo> ?event \n"
            + "    }\n"
            + "    GRAPH <" + con.getEndpointsGraph() + "> { \n"
            + "    	?subject <http://ucuenca.edu.ec/ontology#belongTo> ?uri . \n"
            + "		?subject <http://ucuenca.edu.ec/ontology#extractionDate> ?date .\n"
            + "        FILTER ( STR(?date) != '') . \n"
            + "    }\n"
            + "    values ?g { " + lspro + "\n"
            + "    } .\n"
            + "    graph ?g {\n"
            + "        optional{\n"
            + "            ?event a <http://ucuenca.edu.ec/ontology#ExtractionEvent> .\n"
            + "        	?event rdfs:label ?labelp .\n"
            + "        }        \n"
            + "    }"
            + "    \n"
            + prov
            + "} Group by ?uri ?name";

    return head;

  }

  @Deprecated
  @Override
  public String getExtractedOrgList(List<Provider> providers) {
    String varprov = "";
    String prov = "";

    for (Provider p : providers) {
      varprov = " (group_concat(  ?label" + p.Name + "  ; separator=\";\")  as  ?Adv" + p.Name + " ) " + varprov;
      prov = "  GRAPH  <" + p.Graph + "> {"
              + "  OPTIONAL { "
              + "  ?event rdfs:label  ?label" + p.Name + " } "
              + "  OPTIONAL { "
              + "  ?event  <" + RDF.TYPE.toString() + "> <" + REDI.EXTRACTION_EVENT.toString() + "> } "
              + "  } " + prov;

    }

    String head = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
            + "PREFIX ucmodel: <http://ucuenca.edu.ec/ontology#> "
            + "SELECT DISTINCT ?uri ?name " + varprov
            + " WHERE { "
            + "?subject  <" + REDI.BELONGTO.toString() + "> ?uri . "
            + "?subject  <" + REDI.EXTRACTIONDATE.toString() + ">  ?date .  "
            + "FILTER ( STR(?date)  != '') . "
            + "?uri  <" + REDI.NAME.toString() + ">  ?name ."
            + "OPTIONAL  {  GRAPH   <" + con.getOrganizationsGraph() + "> { "
            + "   ?uri  <" + REDI.BELONGTO.toString() + "> ?event  }"
            + prov
            + "} }Group by ?uri ?name";

    return head;

  }

  /*
     @Deprecated
     @Override
     public String getOrgEnrichmentProvider(Map<String, String> providers) {
     String varprov = "";
     String prov = "";

     for (Map.Entry<String, String> provset : providers.entrySet()) {

     varprov = " ?" + p.Name + " " + varprov;
     prov = " OPTIONAL {       "
     + "   GRAPH <" + provset.getKey() + "> { "
     + "    SELECT  ?endp  (COUNT (distinct ?author) as ?" + p.Name + " )  WHERE { "
     + "       GRAPH <" + con.getAuthorsGraph() + "> { "
     + "      ?author dct:provenance ?endp . "
     + "     }   "
     + "      ?object <" + OWL.oneOf.toString() + "> ?author . "
     + "     } GROUP BY  ?endp "
     + "    } } " + prov;

     }

     String head = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
     + "PREFIX dct: <http://purl.org/dc/terms/> "
     + "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
     + "SELECT     ?org ?label (COUNT (?authort) as ?total)" + varprov
     + " WHERE {       "
     + "      GRAPH <" + con.getOrganizationsGraph() + "> { "
     + "    ?org  	<" + REDI.NAME.toString() + "> ?label  "
     + "          }           "
     + "    GRAPH <" + con.getEndpointsGraph() + "> { "
     + "    ?endp  <" + REDI.BELONGTO.toString() + ">  ?org  } "
     + " GRAPH <" + con.getAuthorsGraph() + "> { "
     + "      ?authort dct:provenance ?endp . "
     + "      ?authort a foaf:Person . "
     + "     }  "
     + "     " + prov + " }GROUP BY ?org  ?label" + varprov;

     return head;
     }
   */
  @Override
  public String getEnrichmentQueryResult(List<Provider> providers) {

    String varprov = "";
    String prov = "";
    String gps = "";

    for (Provider p : providers) {
      varprov = "(COUNT  (distinct ?r" + p.Name + ") as ?" + p.Name + ")  " + varprov;
      gps += " <" + p.Graph + "> ";
      prov += "bind (if (?g = <" + p.Graph + ">,?ar,'') as ?r" + p.Name + ") .\n";

    }
    String prefix = "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX owl: <http://www.w3.org/2002/07/owl#>";

    String head = prefix + "SELECT ?org  ?label (COUNT  (DISTINCT ?authort) as ?total) " + varprov + " "
            + "WHERE { "
            + "GRAPH <" + con.getAuthorsGraph() + "> { \n"
            + "        ?authort a foaf:Person . \n"
            + "		?authort dct:provenance ?endp . \n"
            + "	} \n"
            + "    GRAPH <" + con.getEndpointsGraph() + "> { \n"
            + "		?endp <http://ucuenca.edu.ec/ontology#belongTo> ?org \n"
            + "	} \n"
            + "	GRAPH <" + con.getOrganizationsGraph() + "> { \n"
            + "		?org <http://ucuenca.edu.ec/ontology#name> ?label . \n"
            + "	} \n"
            + "    values ?g { \n"
            + gps
            + "    } .\n"
            + "    graph ?g {\n"
            + "        optional{\n"
            + "			?as owl:oneOf ?authort . \n"
            + "			?ar owl:oneOf ?as .\n"
            + "        }\n"
            + "    }\n"
            + prov
            + "} Group by ?org ?label  ";

    return head;
  }

  @Override
  public String getOrgDisambiguationResult(List<Provider> providers) {

    String varprov = "";
    String prov = "";

    for (Provider p : providers) {

      varprov = " (GROUP_CONCAT(?" + p.Name + "s ;separator=\";\") as ?" + p.Name + ") " + varprov;
      prov = " OPTIONAL {       "
              + "   GRAPH <" + p.Graph + "> { "
              + "Select ?event ?" + p.Name + "s  {"
              + "?event   rdfs:label ?" + p.Name + "s"
              + " } } } " + prov;

    }

    String queryDes = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
            + "Select ?orgname ?org  " + varprov
            + " where {   "
            + "       Graph <" + con.getOrganizationsGraph() + "> { "
            + "      ?org  <" + REDI.BELONGTO.toString() + "> ?event . "
            + "       ?org  <" + REDI.NAME.toString() + "> ?orgname     "
            + "       }       "
            + "     ?event a  <" + REDI.DISAMBIGUATION_EVENT.toString() + "> .   "
            + prov
            + " } GROUP BY ?orgname ?org ";

    return queryDes;
  }

  /*   @Override
     public String getExtractedOrgList() {
     return "SELECT DISTINCT ?uri ?name "
     + "FROM  <" + con.getEndpointsGraph() + "> "
     + "FROM  <" + con.getOrganizationsGraph() + "> "
     + "WHERE  {"
     + "  ?subject  <" + REDI.BELONGTO.toString() + "> ?uri ."
     + "  ?uri  <" + REDI.NAME.toString() + ">  ?name ."
     + "  ?subject   <" + REDI.EXTRACTIONDATE.toString() + ">  ?date ."
     + "  FILTER ( STR(?date)  != '')"
     + "}";
     }*/
  @Override
  public String getlistEndpointNamesQuery() {
    return "SELECT DISTINCT ?fullName WHERE {   GRAPH <http://ucuenca.edu.ec/wkhuska/endpoints>"
            + "	{"
            + "      " + id + con.uc(fullName) + " ?fName."
            + "      	BIND (STR(?fName)  AS ?fullName)"
            + "	}"
            + "}";
  }

  @Override
  public String getEndpointByIdQuery(String endpointsGraph, String id) {
    String idc = " ?id ";
    return "SELECT DISTINCT ?id ?status ?name ?url ?graph ?fullName ?city ?province ?latitude ?longitude  WHERE {  "
            + getGraphString(endpointsGraph)
            + " {"
            + idc + con.uc(status) + " ?status."
            + idc + con.uc("name") + " ?name ."
            + idc + con.uc(url) + " ?url."
            + idc + con.uc(graph) + " ?graph."
            + idc + con.uc(fullName) + " ?fullName."
            + idc + con.uc("city") + " ?city."
            + idc + con.uc("province") + " ?province."
            + idc + con.uc("latitude") + " ?latitude."
            + idc + con.uc("longitude") + " ?longitude."
            + " FILTER(?id = <" + id + ">)"
            + " }"
            + " }";
  }

  @Override
  @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
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
    String stat = con.uc("status");
    return " DELETE { " + getGraphString(args[0])
            + "   {   <" + args[1] + "> " + stat + " ?status "
            + " }} "
            + " INSERT  { "
            + getGraphString(args[0]) + "  {"
            + "             <" + args[1] + "> " + stat + " '" + args[3] + "'^^xsd:boolean"
            + " }       } "
            + "WHERE { "
            + getGraphString(args[0]) + "  { "
            + "             <" + args[1] + "> " + stat + " ?status"
            + "             FILTER (regex(?status,'" + args[2] + "')) "
            + " }   } ";
  }

  @Override
  @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
  public String updateGeneric(String graph, String resource, String property, String object, String literal) {
   //cambio para funcionar en graph db se mueve el corchete del optional
    if (isURI(object)) {
      return "WITH  <" + graph + ">"
              + " DELETE  {<" + resource + ">  <" + property + ">  ?o  }"
              + " INSERT {<" + resource + ">  <" + property + ">  <" + object + "> }"
              + " WHERE { OPTIONAL{ <" + resource + ">  <" + property + ">  ?o } }";
    } else {
      return "WITH  <" + graph + ">"
              + " DELETE  {<" + resource + ">  <" + property + ">  ?o  }"
              + " INSERT {<" + resource + ">  <" + property + ">  '" + object + "'" + literal + " }"
              + " WHERE {OPTIONAL{<" + resource + ">  <" + property + ">  ?o ."
              + " BIND( lang(?o) as  ?la  ) . FILTER (  !Bound(?la) || langMatches(?la , '" + literal.replace("@", "") + "'  ) )"
              + " }}";
    }

  }

  @Override
  @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
  public String removeGeneric(String graph, String resource, String property, String object, String literal) {

    return " WITH <" + graph + "> "
            + "DELETE { ?subject ?property ?object .     ?subject2  ?property2 ?subject }"
            + "WHERE {"
            + "  ?subject  <" + property + ">   <" + object + "> . "
            + " FILTER ( ?subject = <" + resource + ">) ."
            + "    ?subject ?property  ?object ."
            + "OPTIONAL { ?subject2  ?property2 ?subject }   "
            + " }";

  }

  @Override
  @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
  public String removeGenericType(String graph, String type, String resource) {

    return "WITH <" + graph + ">"
            + " DELETE { ?a ?b ?c  }\n"
            + "WHERE { "
            + "?a a  <" + type + "> ."
            + "?a ?b ?c .\n"
            + "FILTER ( ?a = <" + resource + "> )}";

  }

  @Override
  @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
  public String removeGenericRelation(String graph, String relation, String resource) {

    return "WITH <" + graph + ">"
            + "DELETE { ?a ?b ?c }\n"
            + "WHERE { ?a  <" + relation + "> ?e  . "
            + " ?a ?b ?c  . "
            + "FILTER ( ?e = <" + resource + "> )} ";

  }

  @Override
  @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
  public String removeGenericRelationwithDependecies(String graph, String relation, String resource, String relationdel) {

    return "WITH <" + graph + "> DELETE {"
            + " ?a1  ?b1 ?c1 } WHERE { VALUES ?e { <" + resource + "> } . "
            + "?a  <" + relation + "> ?e . "
            + "?a <" + relationdel + "> ?a1 . ?a1 ?b1 ?c1 } ";

  }

  @Override
  public String getAuthors() {
    return PREFIXES
            + "SELECT ?s WHERE {"
            + "  GRAPH  <" + con.getAuthorsGraph() + "> { "
            + "    ?s a foaf:Person. ?s foaf:name ?name. "
            + "   }"
            + "} order by desc(strlen(str(?name)))";
  }

  @Override
  public String getSameAsAuthors(String authorResource) {
    return PREFIXES
            + "SELECT ?o WHERE {"
            + "GRAPH <" + con.getAuthorsGraph() + "> { "
            + "     <" + authorResource + "> owl:sameAs  ?o . "
            + "     <" + authorResource + "> a foaf:Person . "
            + "   }"
            + "}";
  }

  @Override
  public String getSameAsAuthors(String graph, String authorResource) {
    return PREFIXES
            + "SELECT ?o WHERE {  GRAPH <" + graph + ">  {     "
            + "     <" + authorResource + "> owl:sameAs  ?o . "
            + "   }"
            + "}";
  }

  @Override
  public String getCountPersonQuery(String graph, String num, Boolean modo) {
    /* return PREFIXES
         + " SELECT (COUNT(DISTINCT ?s) as ?count) WHERE {"
         + " SELECT DISTINCT ?s WHERE {" + getGraphString(graph) + "{ "
         + " ?docu rdf:type bibo:Document ; "
         + "      ?c ?s ."
         + " ?s a foaf:Person."
         + " } }"
         + " GROUP BY ?s"
         + " HAVING (count(?docu)>" + num + ")}";*/
    if (modo) {
      return PREFIXES
              + " SELECT (count (?s) as ?count) { "
              + " SELECT DISTINCT ?s WHERE {   "
              + "                 ?s a foaf:Person . "
              + "                  ?docu ?property ?s . "
              + "                  ?docu a bibo:Article. "
              + " } GROUP BY ?s HAVING (count(?docu)> 0  )"
              + "}";

    } else {
      return PREFIXES
              + " SELECT (count (?s) as ?count) {"
              + " SELECT DISTINCT ?s WHERE { "
              + "                 ?s a foaf:Person . "
              + "                  { "
              + "                  ?article   ?property  ?s . "
              + "                   ?article a bibo:Article "
              + "                  }UNION { "
              + "                  ?docu  ?property ?s  . "
              + "                  ?docu a bibo:Document "
              + "                  }  "
              + "} GROUP BY ?s HAVING (count(?docu)> " + num + " || count (?article) > 0  ) "
              + "}";
    }
  }

  @Override
  public String getArticlesFromDspaceQuery(String graph, String person) {
    return PREFIXES
            + " select distinct ?docu where { " + getGraphString(graph)
            + "{   ?docu a bibo:Article. ?docu ?c <" + person + "> .    }   } ";
  }

  @Override
  public String getCountAuthors() {
    return PREFIXES
            + "SELECT (COUNT(?author) as ?count) WHERE {GRAPH <" + con.getAuthorsGraph() + "> { ?author a foaf:Person . }}";
  }

  @Override
  public String getCountSubjects(String authorResource) {
    return PREFIXES
            + "SELECT (COUNT(?subject) as ?count) WHERE {  GRAPH <" + con.getAuthorsGraph() + "> "
            + "{ <" + authorResource + "> dct:subject ?subject .  } }";
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
  public String getRetrieveKeysQuery() {
    return " PREFIX dct: <http://purl.org/dc/terms/>  "
            + " SELECT ?subject WHERE { [] dct:subject ?subject}";
  }

  @Override
  public String getAuthorsDataQuery(String... organizations) {
    String[] orgs = Arrays.copyOf(organizations, organizations.length);
    for (int i = 0; i < orgs.length; i++) {
      orgs[i] = "<" + orgs[i] + ">";
    }
    return PREFIXES
            + "SELECT DISTINCT ?subject (SAMPLE(?name_) as ?name) (SAMPLE(?fname_) as ?fname) (SAMPLE(?lname_) as ?lname)"
            + "WHERE {"
            + "  VALUES ?organization {" + StringUtils.join(orgs, " ") + "}"
            + "  GRAPH <" + con.getEndpointsGraph() + ">  {"
            + "      ?provenance uc:belongTo ?organization."
            + "  }"
            + "  GRAPH <" + con.getAuthorsGraph() + ">  {"
            + "    ?subject a foaf:Person;"
            + "               foaf:name ?name_;"
            + "               foaf:firstName ?fname_;"
            + "               foaf:lastName ?lname_;"
            + "               dct:provenance ?provenance."
            //                + "filter (mm:fulltext-search(?name_,\"Saquicela\")) "
            //                + "filter (mm:fulltext-search(?name_,\"Mauricio espinoza\")) "
            //                + "filter (mm:fulltext-search(?name_,\"Saquicela\") || mm:fulltext-search(?name,\"Mauricio espinoza\")) "
            + "  }"
            + "} GROUP BY ?subject order by ?subject";
  }

  @Override
  public String getAuthorsDataQuery(String organization) {
    String[] orgs = new String[]{organization};
    return getAuthorsDataQuery(orgs);
  }

  @Override
  public String getOrganizationNameQuery(String organization) {
    return PREFIXES
            + "SELECT (str(?name_) as ?name)"
            + "WHERE {"
            + "  GRAPH <" + con.getOrganizationsGraph() + "> {"
            + " 	 <" + organization + "> uc:fullName  ?name_ ."
            + "  }"
            + "}";
  }

  @Override
  public String getAuthorsTuplesQuery(String subject) {
    return PREFIXES
            + " SELECT distinct ?p ?o WHERE { "
            + con.getGraphString(con.getAuthorsGraph())
            + "  {<" + subject + "> ?p ?o.    }  }   ";
  }

  @Override
  public String getAuthorDeleteQuery(String id) {
    return "DELETE { " + con.getGraphString(con.getAuthorsGraph()) + "  { <" + id + "> ?p ?o }} WHERE { "
            + con.getGraphString(con.getAuthorsGraph())
            + " {   <" + id + "> ?p ?o .      }  } ";
  }

  @Override
  public String getAuthorDataQuery(String authorUri) {
    authorUri = " <" + authorUri + "> ";
    return PREFIXES
            + " SELECT distinct * WHERE { " + con.getGraphString(con.getAuthorsGraph()) + "   {     "
            + authorUri + " a foaf:Person. "
            + authorUri + " foaf:name ?name."
            + authorUri + " foaf:firstName ?fname. "
            + authorUri + " foaf:lastName ?lname. "
            + authorUri + " dct:provenance ?provenance. "
            + " }  } ";

  }

  @Override
  public String getAuthorProvenanceQuery(String authorUri) {
    authorUri = " <" + authorUri + "> ";
    return PREFIXES
            + " SELECT distinct * WHERE { " + con.getGraphString(con.getAuthorsGraph()) + "   {     "
            + authorUri + " dct:provenance ?provenance. "
            + " }  } ";

  }

  @Override
  public String getAuthorsByName(String graph, String firstName, String lastName) {
    int one = 1;
    String option = ", 'i'";
    if (firstName.length() == one) {
      option = "";
    }

    return PREFIXES
            + "SELECT distinct ?subject ?name (STR(?fName)  AS ?firstName) (STR(?lName)  AS ?lastName)  WHERE { "
            + getGraphString(graph) + "{ "
            + "    ?subject a foaf:Person; "
            + " foaf:name ?name; "
            + " foaf:firstName ?fName; "
            + " foaf:lastName ?lName. "
            + "    filter(regex(?fName, '" + firstName + "'" + option + ")). " //"^M$"
            + "    filter(regex(?lName, '" + lastName + "', 'i')). " //"^Espinoza"
            + "  } "
            + "}";
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

//    @Override
//    public String getPublicationsMAQuery(String providerGraph) {
//        return " SELECT DISTINCT ?authorResource ?pubproperty ?publicationResource WHERE { "
//                + getGraphString(providerGraph)
//                + " {  "
//                + " ?authorResource owl:sameAs   ?authorNative. "
//                + " ?authorNative ?pubproperty ?publicationResource. "
//                + " filter (regex(?pubproperty,\"pub\")) "
//                + " }  "
//                + " }  ";
//    }
  @Override
  public String getPublicationsPropertiesQuery(String providerGraph, String publicationResource) {
    return " SELECT DISTINCT ?publicationProperties ?publicationPropertyValue WHERE { "
            + getGraphString(providerGraph)
            + " {"
            + " <" + publicationResource + ">  ?publicationProperties ?publicationPropertyValue. "
            + " } }"
            + "ORDER BY DESC(?publicationProperties) ";
  }

  @Override
  public String isREDIEndpointStored(String id) {
    return "ASK FROM <" + con.getCentralEndpointsGraph() + ">\n"
            + "{<" + id + "> ?p []} ";
  }

  @Override
  public String insertREDIEndpoints(String id, String name, String url, String context) {
    return PREFIXES
            + "INSERT DATA\n"
            + "{ \n"
            + "  GRAPH <" + con.getCentralEndpointsGraph() + "> {\n"
            + "    <" + id + "> a uc:REDIEndpoint;\n"
            + "                         foaf:name \"" + name + "\" ;\n"
            + "                         uc:baseContext <" + context + ">;\n"
            + "                         uc:sparql <" + url + "sparql/" + ">;\n"
            + "                         foaf:homepage <" + url + ">.\n"
            + "                                              \n"
            + "  }\n"
            + "}";
  }

  @Override
  public String getInsertOffsetQuery(String endpoint, int id, String name) {
    String offsetURI = con.getGraphResource() + id;
    return PREFIXES
            + "INSERT DATA {\n"
            + "  GRAPH <" + con.getCentralEndpointsGraph() + "> {\n"
            + "  	<" + endpoint + "> uc:offset <" + offsetURI + ">.\n"
            + "     <" + offsetURI + "> uc:value \"-1\"^^xsd:integer.\n"
            + "     <" + offsetURI + "> uc:status \"Not started.\".\n"
            + "     <" + offsetURI + ">   uc:graphName \"" + name + "\".\n"
            + "  }\n"
            + "}";
  }

  @Override
  public String getListREDIEndpoints() {
    return PREFIXES
            + "SELECT ?id ?name ?url ?sparql ?context ?offset ?status\n"
            + "WHERE {\n"
            + "  GRAPH <" + con.getCentralEndpointsGraph() + "> {\n"
            + "    ?id a uc:REDIEndpoint;\n"
            + "        foaf:name ?name;\n"
            + "        foaf:homepage ?url;\n"
            + "        uc:sparql ?sparql;\n"
            + "        uc:baseContext ?context.\n"
            + "  }\n"
            + "}";
  }

  @Override
  public String getREDIEndpointStatistics(String id) {
    return PREFIXES
            + "SELECT ?value ?graphName ?status WHERE {\n"
            + "  GRAPH <" + con.getCentralEndpointsGraph() + "> {\n"
            + "    <" + id + "> uc:offset ?uri.\n"
            + "    ?uri uc:value ?value;\n"
            + "         uc:graphName ?graphName;\n"
            + "         uc:status ?status.\n"
            + "  }\n"
            + "}";
  }

  @Override
  public String getREDIEndpoint(String id) {
    return PREFIXES
            + "SELECT ?name ?url ?sparql ?context ?offset\n"
            + "WHERE {\n"
            + "  GRAPH <" + con.getCentralEndpointsGraph() + "> {\n"
            + "    <" + id + "> a uc:REDIEndpoint;\n"
            + "        foaf:name ?name;\n"
            + "        foaf:homepage ?url;\n"
            + "        uc:sparql ?sparql;\n"
            + "        uc:baseContext ?context.\n"
            + "  }\n"
            + "}";
  }

  @Override
  public String getGraphOffset(int id) {
    return PREFIXES
            + "SELECT ?val WHERE {"
            + "  GRAPH <" + con.getCentralEndpointsGraph() + "> { "
            + "   <" + con.getGraphResource() + id + "> uc:value ?val."
            + "  }"
            + "}";
  }

  @Override
  public String getUpdateOffsetQuery(int id, int newOffset) {
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    Date date = new Date();
    return PREFIXES
            + "WITH <" + con.getCentralEndpointsGraph() + ">\n"
            + "DELETE {\n"
            + "  	<" + con.getGraphResource() + id + "> uc:value ?w.\n"
            + "  	<" + con.getGraphResource() + id + "> uc:status ?s.\n"
            + "} INSERT {\n"
            + "  	<" + con.getGraphResource() + id + "> uc:value \"" + newOffset + "\"^^xsd:integer.\n"
            + "  	<" + con.getGraphResource() + id + "> uc:status\"" + dateFormat.format(date) + "\".\n"
            + "} WHERE {\n"
            + "  	<" + con.getGraphResource() + id + "> uc:value ?w.\n"
            + "  	<" + con.getGraphResource() + id + "> uc:status ?s.\n"
            + "}";
  }

  @Override
  public String getNumberofTriplesREDIEndpoint(String selectService, int offset, String targetGraph) {
    return "SELECT (COUNT(*) as ?total) WHERE { \n"
            + "   SERVICE <" + selectService + "> { \n"
            + "     SELECT * {\n"
            + "       GRAPH <" + targetGraph + "> {\n"
            + "     	?s ?p ?o .\n"
            + "       }\n"
            + "     } OFFSET " + offset + " LIMIT " + LIMIT_TRIPLES_REDI_END + "\n"
            + "   }  \n"
            + "} ";
  }

  @Override
  public String getCopyDataQuery(String selectService, int offset, String targetGraph, String localGraphName) {
    return "INSERT {\n"
            + "    GRAPH <" + con.getBaseContext() + localGraphName + "> {\n"
            + "    	?s ?p ?o.\n"
            + "    }\n"
            + "} WHERE { \n"
            + "   SERVICE <" + selectService + "> { \n"
            + "     SELECT * {\n"
            + "       GRAPH <" + targetGraph + "> {\n"
            + "     	?s ?p ?o .\n"
            + "       }\n"
            + "     } OFFSET " + offset + " LIMIT " + LIMIT_TRIPLES_REDI_END + "\n"
            + "   }  \n"
            + "} ";
  }

  @Override
  public String delteREDIEndpointQuery(String id) {
    return PREFIXES
            + "DELETE WHERE { \n"
            + "  GRAPH <" + con.getCentralEndpointsGraph() + "> {\n"
            + "      <" + id + "> ?t ?b.\n"
            + "      ?b ?w ?o.\n"
            + "  }\n"
            + "}\n"
            + "\n"
            + ";\n"
            + "\n"
            + "DELETE WHERE { \n"
            + "  GRAPH <" + con.getCentralEndpointsGraph() + "> {\n"
            + "      <" + id + "> ?t ?b.\n"
            + "  }\n"
            + "}";
  }

  @Override
  public String getPublicationsPropertiesQuery(String publicationResource) {
    return " SELECT DISTINCT ?property ?value WHERE { "
            + " <" + publicationResource + ">  ?property ?value. "
            + " }";
  }

  @Override
  public String getAuthorsPropertiesQuery(String uriAuthor) {
    /*  return " SELECT  ?property ?object WHERE { "
         + " <" + uriAuthor + ">  ?property ?object  "
         + " }";*/
    return "select ?property ?object ?type where { "
            + "VALUES ?uri { <" + uriAuthor + "> } .  "
            + "?uri  ?property ?object . "
            + "OPTIONAL { "
            + "?object a  ?type }"
            + "}";
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
  public String getPublicationPropertiesQuery(String property) {
    return PREFIXES + "SELECT DISTINCT ?publicationResource ?publicationProperties ?publicationPropertiesValue "
            + " WHERE { ?authorResource " + (isURI(property) ? "<" + property + ">" : property) + " ?publicationResource. ?publicationResource ?publicationProperties ?publicationPropertiesValue }";
  }

  @Override
  public String getAllTitlesDataQuery(String graph) {

    return PREFIXES + "SELECT DISTINCT  ?publications ?title "
            + " FROM <" + graph + "> "
            + " WHERE { ?publications dct:title ?title  } ";
  }

  @Override
  public String getSubjectAndObjectByPropertyQuery(String property) {
    return PREFIXES + "SELECT DISTINCT  ?subject ?object "
            //                + " WHERE { ?subject "+(commonsServices.isURI(property)? "<" + property + ">" : property) + " ?object  } ";
            + " WHERE { ?subject " + property + " ?object  } ";
  }

  @Override
  public String getObjectByPropertyQuery(String subject, String property) {
    return PREFIXES + " SELECT DISTINCT ?object "
            //          + "  WHERE { <" + subject + "> "+(commonsServices.isURI(property)? "<" + property + ">" : property) + "  ?object } ";
            + "  WHERE { <" + subject + "> " + property + "  ?object } ";

  }

  @Override
  public String getObjectByPropertyQuery(String graphname, String subject, String property) {
    return PREFIXES
            + " SELECT DISTINCT ?object FROM <" + graphname + "> WHERE { "
            + "                <" + subject + ">   <" + property + "> ?object"
            + "}";
  }

  @Override
  public String getObjectByPropertyQuery(String property) {
    return PREFIXES + " SELECT DISTINCT ?object "
            //    + "  WHERE {  ?s "+(commonsServices.isURI(property)? "<" + property + ">" : property) + "  ?object } ";
            + "  WHERE {  ?s " + property + "  ?object } ";

  }

  @Override
  public String getAbstractAndTitleQuery(String resource) {
    return PREFIXES + " SELECT DISTINCT  ?title ?abstract ?description "
            + "  WHERE {   <" + resource + "> dct:title ?title. "
            + "OPTIONAL {  <" + resource + "> bibo:abstract  ?abstract  }"
            + "OPTIONAL {  <" + resource + "> dct:description  ?description  } } ";
  }

  @Override
  public String getAuthorsKeywordsQuery(String resource) {
    return PREFIXES + " SELECT DISTINCT ?keyword FROM <" + con.getAuthorsGraph() + "> "
            + " WHERE { <" + resource + "> dct:subject ?keyword. filter(strlen(?keyword) < 41) } limit 50";
  }

  @Override
  public String getAuthorSubjectQuery(String resource) {
    return PREFIXES + " SELECT DISTINCT ?keyword FROM <" + con.getAuthorsGraph() + "> "
            + " WHERE { <" + resource + "> foaf:topic ?keyword. } limit 50";
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
            + "?publicationResource <" + prefix + ">  ?title "
            + " { FILTER (regex(?pubproperty,\"authorOf\")) }  "
            + "UNION { FILTER (regex(?pubproperty,\"pub\")) }  }} ";
  }

  @Override
  public String getPublicationsTitleScopusQuery(String providerGraph, String prefix) {
    return PREFIXES
            + " SELECT DISTINCT ?authorResource ?publicationResource ?title "
            + " WHERE {" + getGraphString(providerGraph)
            + "{   ?authorResource " + OWLSAMEAS + "   ?authorNative. "
            + " ?authorNative foaf:publications ?publicationResource. "
            + " ?publicationResource <" + prefix + ">  ?title "
            + " }} ";
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
  public String detailsOfProvenance(String graph, String resource) {
    return PREFIXES
            + " SELECT DISTINCT ?property ?hasValue  WHERE {  graph <" + graph + ">{ "
            + "  {  <" + resource + "> ?property ?hasValue } "
            + " UNION "
            + "  { ?isValueOf ?property <" + resource + "> } "
            + " }}  "
            + "ORDER BY ?property ?hasValue ?isValueOf";
  }

  @Override
  public String authorGetProvenance(String graph, String authorResource) {
    return PREFIXES
            //+ " PREFIX uc: <http://ucuenca.edu.ec/ontology#> "
            + " SELECT ?name WHERE "
            + " {          GRAPH <" + con.getEndpointsGraph() + "> "
            + "   { "
            + "  	?object  <http://ucuenca.edu.ec/ontology#name> ?name."
            + "     GRAPH <" + graph + ">	" //http://ucuenca.edu.ec/wkhuska/authors
            + "     {  		"
            + "    	     <" + authorResource + ">  dct:provenance ?object. " //http://190.15.141.85:8080/resource/authors_epn/HIDALGO_TRUJILLO_SILVANA_IVONNE
            + "     } "
            + "   }    "
            + "} ";
  }

  @Override
  public String authorGetProvenance(String authorResource) {
    return PREFIXES
            + " SELECT ?name WHERE "
            + " {        "
            + "   GRAPH <" + con.getEndpointsGraph() + "> "
            + "   { "
            + "  	?object  <http://ucuenca.edu.ec/ontology#name> ?name."
            + "     GRAPH <" + con.getAuthorsGraph() + ">	"
            + "     {  		"
            + "    	     <" + authorResource + ">  dct:provenance ?object. "
            + "     } "
            + "   }    "
            + "} ";
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
    return "GRAPH <" + graph + "> ";
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

  @Override
  public String getResourceUriByType(String type) {
    return PREFIXES + "SELECT DISTINCT * WHERE { ?publicationResource  " + type + " ?authorResource } ";
  }

  @Override
  public String getPropertiesOfResourceByType(String type) {
    return PREFIXES + "SELECT DISTINCT * WHERE { ?publicationResource a " + type + ". "
            + "?publicationResource ?publicationProperties ?publicationPropertiesValue } ";
  }

  @Override
  public String getPublicationsTitleByType(String graph, String type) {
    return PREFIXES + "SELECT DISTINCT ?authorResource ?publicationResource ?title\n"
            + "WHERE {   GRAPH <" + graph + ">  {"
            + "	   ?authorResource " + type + " ?publicationResource."
            + "        ?publicationResource dct:title ?title "
            + "  }"
            + "} ";
  }

  @Override
  public String getAuthorsDataQueryByUri(String graph, String endpointsgraph, String resource) {
    return PREFIXES
            + " SELECT distinct *"
            + " WHERE   { " + getGraphString(graph) + " { "
            + " ?subject a foaf:Person. "
            + " ?subject foaf:name ?name."
            + " ?subject foaf:firstName ?fname. "
            + " ?subject foaf:lastName ?lname. "
            + " ?subject foaf:nick ?nick. "
            + " ?subject dct:provenance ?provenance. "
            + " { select ?status "
            + "     where { " + getGraphString(endpointsgraph) + " {"
            + "     ?provenance <http://ucuenca.edu.ec/ontology#status> ?status "
            + " }}} filter (regex(?status,\"true\"))"
            + "filter (regex(?subject,\"" + resource + "\"))"
            + "                }}";

  }

  @Override
  public String getTriplesByFilter(String... args) {

    return PREFIXES + "SELECT * WHERE {"
            + "  ?subject ?property ?object\n"
            + "  FILTER (regex(?object, \"" + args[0] + "\", \"i\")  "
            + "         || regex(?object, \"" + args[1] + "\", \"i\") "
            + "         || regex(str(?object), \"" + args[2] + "\", \"i\") "
            + "         || regex(str(?object), \"" + args[3] + "\", \"i\") )  "
            + "}";

  }

  @Override
  public String getEndPointUriByName(String nameEndpoint) {
    return "SELECT ?object  WHERE {"
            + "  GRAPH  <http://ucuenca.edu.ec/wkhuska/endpoints> {\n"
            + "      	?object  <http://ucuenca.edu.ec/ontology#name> ?name "
            + "             filter (regex(?name,\"" + nameEndpoint + "\")) "
            + "  }    "
            + "} ";

  }

  @Override
  public String getAuthorPublicationsQueryFromGenericProvider(String... varargs) {
    return PREFIXES
            + "SELECT DISTINCT  ?pubproperty ?publicationResource ?title "
            + "WHERE {graph <" + varargs[0] + ">"
            + " {     <" + varargs[1] + "> foaf:publications ?publicationResource."
            + " ?publicationResource <" + varargs[2] + ">  ?title"
            + "}} ";
  }

  public String getIESInfobyAuthor(String authorURI) {
    return PREFIXES
            + "SELECT DISTINCT ?city ?province\n"
            + "(GROUP_CONCAT(DISTINCT STR(?fullname); separator=\",\") as ?ies) \n"
            + "(GROUP_CONCAT(DISTINCT ?domain; separator=\",\") as ?domains) WHERE {    \n"
            + "{<" + authorURI + "> dct:provenance ?p }\n"
            + "UNION\n"
            + "{<" + authorURI + "> owl:sameAs [dct:provenance ?p].}\n"
            + "?p <http://ucuenca.edu.ec/ontology#fullName> ?fullname;\n"
            + "   <http://ucuenca.edu.ec/ontology#city> ?city;  \n"
            + "   <http://ucuenca.edu.ec/ontology#province> ?province; \n"
            + "  OPTIONAL { ?p  <http://ucuenca.edu.ec/ontology#domain> ?domain.}  \n"
            + "} GROUP BY ?p ?city ?province";
  }

  public String getAskPublicationsURLGS(String graphName, String authorResource) {
    return pubUrlsGS(graphName, authorResource, true);
  }

  public String getPublicationsURLGS(String graphName, String authorResource) {
    return pubUrlsGS(graphName, authorResource, false);
  }

  private String pubUrlsGS(String graphName, String authorResource, boolean isAsk) {
    String query = PREFIXES;
    query += isAsk ? "ASK " : "SELECT ?author ?url ";
    query += "WHERE {"
            + getGraphString(graphName) + "{"
            + "    <" + authorResource + ">  owl:sameAs  ?author."
            + "    ?author a foaf:Person ;"
            + "     {?author <http://ucuenca.edu.ec/ontology#googlescholarURL> ?url . }"
            + "    MINUS "
            + "     {?author foaf:publications [<http://ucuenca.edu.ec/ontology#googlescholarURL> ?url].}"
            + "  }"
            + "}";

    return query;
  }

  @Override
  public String getInsertDomainQuery(String enpointId, String domain) {
    return "INSERT DATA {   GRAPH <" + con.getEndpointsGraph() + "> {"
            + " <" + enpointId + ">   <" + REDI.DOMAIN + "> \"" + domain + "\""
            + "}}";
  }

  @Override
  public String getPublicationsScholar(String resource) {
    return PREFIXES
            + "SELECT ?url WHERE{ GRAPH <" + con.getGoogleScholarGraph() + "> {\n"
            + "	<" + resource + "> a foaf:Person ;\n"
            + "    	<" + REDI.GSCHOLAR_URl + "> ?url\n"
            + "}} ";
  }

  @Override
  public String getProfileScholarAuthor() {
    return PREFIXES
            + "SELECT ?resource ?profile (COUNT(?url) as ?count) "
            + "WHERE{ GRAPH <" + con.getGoogleScholarGraph() + "> {\n"
            + "	?resource a foaf:Person ;\n"
            + " 		bibo:uri  ?profile;\n"
            + "    	<" + REDI.GSCHOLAR_URl + "> ?url\n"
            + "}} GROUP BY ?resource ?profile";
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
      return INSERTDATA + getGraphString(endpointsGraph) + "{<" + ENDPOINTPREFIX + resourceHash + ">  " + con.uc(parameter) + "   '" + newValue + "'" + type + " }}  ";
    }
  }

  @Override
  public String getAuthorsCentralGraphSize() {
    return PREFIXES
            + "SELECT (COUNT(DISTINCT ?author) as ?tot) WHERE {"
            + "   GRAPH <" + con.getCentralGraph() + "> {"
            + "    ?author a foaf:Person;"
            + " foaf:publications []."
            + "    }"
            + "}";
  }

  @Override
  public String getAuthorsCentralGraph(int limit, int offset) {
    return PREFIXES
            + "SELECT DISTINCT ?author WHERE {"
            + "   GRAPH <" + con.getCentralGraph() + ">  { "
            + "    ?author a foaf:Person;"
            + " foaf:publications []."
            + "    }}"
            + " LIMIT " + limit
            + " OFFSET " + offset;
  }

  @Override
  public String getSameAuthorsLvl2(String authorResource) {
    return PREFIXES
            + "SELECT * WHERE {{"
            + "    SELECT * WHERE {"
            + "      GRAPH <" + con.getCentralGraph() + ">  { "
            + "        <" + authorResource + "> a foaf:Person; "
            + "           owl:sameAs  ?other."
            + "      }}}"
            + "  ?other owl:sameAs ?general."
            + "}";
  }

  @Override
  public String getOptionalProperties(String sameAs, String property) {
    return PREFIXES
            + "SELECT ?attr WHERE {"
            + "  OPTIONAL {<" + sameAs + "> " + property + " ?attr.}"
            + "}";
  }

  @Override
  public String getPublicationsTitlesQuery() {
    return PREFIXES + " PREFIX dcterms: <http://purl.org/dc/terms/> "
            + " SELECT ?pub ?title ?abstract WHERE { "
            + "  graph <http://ucuenca.edu.ec/wkhuska> "
            + "     {   ?authorResource foaf:publications ?pub. "
            + "       	 ?pub dcterms:title ?title."
            + "              OPTIONAL{ ?pub bibo:abstract ?abstract.}     }  }";
  }

  @Override
  public String getSearchQuery(String textSearch) {
    return PREFIXES
            + " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX uc: <http://ucuenca.edu.ec/ontology#> PREFIX dcterms: <http://purl.org/dc/terms/> "
            + "CONSTRUCT { "
            + " ?keyword uc:publication ?publicationUri. "
            + " ?publicationUri dct:contributors ?subject . "
            + " ?subject foaf:name ?name . "
            + " ?subject a foaf:Person . "
            + " ?publicationUri a bibo:Document. "
            + " ?publicationUri dct:title ?title. "
            + " ?publicationUri bibo:abstract ?abstract. "
            + " ?publicationUri bibo:uri ?uri. "
            + "} "
            + "WHERE { "
            + " GRAPH <http://ucuenca.edu.ec/wkhuska> { "
            + " ?subject foaf:publications ?publicationUri . "
            + " ?subject foaf:name ?name . "
            + " ?publicationUri dct:title ?title . "
            + " OPTIONAL{ ?publicationUri bibo:abstract ?abstract. } "
            + " OPTIONAL{ ?publicationUri bibo:uri ?uri. } "
            //+ " #?publicationUri dcterms:subject ?keySub. "
            //+ " #?keySub rdfs:label ?quote. "
            //+ " #FILTER (mm:fulltext-search(?quote, \"population\" )) . "
            + " FILTER(" + commonsServices.getIndexedPublicationsFilter(textSearch) + ")"
            + " BIND(REPLACE( \"population\", \" \", \"_\", \"i\") AS ?key) . "
            + " BIND(IRI(?key) as ?keyword) "
            + " }"
            + "}";
  }

  @Override
  public String getJournalsCentralGraphQuery() {
    return PREFIXES
            + "SELECT DISTINCT ?JOURNAL ?NAME { "
            + "GRAPH  <" + con.getCentralGraph() + "> {    "
            + "?JOURNAL a bibo:Journal . "
            + "?JOURNAL rdfs:label ?NAME ."
            + "} "
            + "}";

  }

  //FIXME use prefixes
  @Override
  public String getJournalsLantindexGraphQuery() {
    return PREFIXES
            + "SELECT DISTINCT ?JOURNAL ?NAME ?TOPIC ?YEAR ?ISSN { "
            + " GRAPH <" + con.getLatindexJournalsGraph() + "> {   "
            + "?JOURNAL a <http://redi.cedia.edu.ec/ontology/journal> . "
            + "?JOURNAL <http://redi.cedia.edu.ec/ontology/tit_clave> ?NAME ."
            + "?JOURNAL <http://redi.cedia.edu.ec/ontology/subtema> ?TOPIC ."
            + "?JOURNAL <http://redi.cedia.edu.ec/ontology/ano_ini> ?YEAR ."
            + "?JOURNAL <http://redi.cedia.edu.ec/ontology/issn> ?ISSN ."
            + "} "
            + "}";
  }

  @Override
  public String getPublicationsOfJournalCentralGraphQuery(String journalURI) {
    return PREFIXES
            + "SELECT DISTINCT ?PUBLICATION ?TITLE ?ABSTRACT { "
            + "GRAPH   <" + con.getCentralGraph() + "> {  "
            + "?PUBLICATION <http://purl.org/dc/terms/isPartOf> <" + journalURI + "> . "
            + "?PUBLICATION <http://purl.org/dc/terms/title> ?TITLE ."
            + "OPTIONAL{"
            + "?PUBLICATION <http://purl.org/ontology/bibo/abstract> ?ABSTRACT ."
            + "}"
            + "} "
            + "}";
  }

  @Override
  public String getPublicationsCentralGraphQuery() {
    return PREFIXES
            + "SELECT DISTINCT ?PUBLICATION ?TITLE ?ABSTRACT { "
            + "GRAPH   <" + con.getCentralGraph() + "> {  "
            + "?MOCKAUTHOR foaf:publications ?PUBLICATION . "
            + "?PUBLICATION <http://purl.org/dc/terms/title> ?TITLE ."
            + "OPTIONAL{"
            + "?PUBLICATION <http://purl.org/ontology/bibo/abstract> ?ABSTRACT ."
            + "}"
            + "} "
            + "}";
  }

  @Override
  public String getBarcharDataQuery() {
    return PREFIXES
            + "CONSTRUCT {  "
            + "  ?org uc:totalPublications ?totalPub;              "
            + "              uc:totalAuthors ?totalAuthors;         "
            + "              uc:totalProjects ?totalProj; "
            + "              uc:name ?name."
            + "} WHERE {    "
            + "	SELECT DISTINCT ?org (count(DISTINCT ?pub) as ?totalPub) (count(DISTINCT ?author) as ?totalAuthors) (count(DISTINCT ?proj) as ?totalProj) (SAMPLE(?label) as ?name)"
            + "WHERE { "
            + "  GRAPH <" + con.getCentralGraph() + "> {"
            + "  	?author  <http://schema.org/memberOf> ?org;"
            + "         foaf:publications ?pub. "
            + "optional { ?proj   cerif:linksToOrganisationUnit ?org  ." 
            + "        ?proj  a foaf:Project }"
            + "	}"
            + "	GRAPH <" + con.getOrganizationsGraph() + "> {"
            + "  		?org  a foaf:Organization ;"
            + "               uc:name ?label "
            + "	}"
            + "} GROUP BY ?org "
            + "}";
//<editor-fold defaultstate="collapsed" desc="old query">
//                + "CONSTRUCT {"
//                + "  ?provenance uc:totalPublications ?totalPub;"
//                + "              uc:totalAuthors ?totalAuthors;"
//                + "              uc:name ?name."
//                + "}   WHERE {"
//                + "  {"
//                + "    SELECT DISTINCT ?provenance (SAMPLE(?ies) as ?name) (count(DISTINCT ?author) as ?totalAuthors) (COUNT(DISTINCT ?publications) as ?totalPub)"
//                + "    WHERE {"
//                + "      GRAPH <" + con.getCentralGraph() + "> {"
//                + "        ?author a foaf:Person."
//                + "        ?author dct:provenance ?provenance."
//                + "        ?author foaf:publications ?publications."
//                + "        GRAPH <" + con.getEndpointsGraph() + "> {"
//                + "          ?provenance uc:name ?ies."
//                + "        }"
//                + "      }"
//                + "    } GROUP BY ?provenance ORDER BY DESC(?totalAuthors)"
//                + "  }"
//                + "}";
//</editor-fold>
  }

  @Override
  public String getBarcharbyCountryDataQuery() {
    return PREFIXES
            + "CONSTRUCT {  "
            + "                   ?co uc:name ?country; "
            + "                   uc:totalPublications ?totalPub ;            "
            + "                   uc:totalAuthors ?totalAuthors.              "
            + "                 } WHERE {    "
            + "                 	SELECT DISTINCT ?co ?country (count(DISTINCT ?pub) as ?totalPub) (count(DISTINCT ?author) as ?totalAuthors) \n"
            + "                 WHERE { "
            + "                   GRAPH <" + con.getCentralGraph() + "> { "
            + "                   		?author  <http://schema.org/memberOf> ?org; "
            + "                                 foaf:publications ?pub. \n"
            + "                 	} "
            + "                 	GRAPH <" + con.getOrganizationsGraph() + "> {"
            + "                   		?org  a foaf:Organization ; "
            + "                                uc:name ?label ;"
            + "                                 uc:country  ?country . "
            + "                       BIND( IRI(CONCAT(uc:country,?country)) as ?co)"
            + "                 	} "
            + "                 } GROUP BY ?co ?country ";
//<editor-fold defaultstate="collapsed" desc="old query">
//                + "CONSTRUCT {"
//                + "  ?provenance uc:totalPublications ?totalPub;"
//                + "              uc:totalAuthors ?totalAuthors;"
//                + "              uc:name ?name."
//                + "}   WHERE {"
//                + "  {"
//                + "    SELECT DISTINCT ?provenance (SAMPLE(?ies) as ?name) (count(DISTINCT ?author) as ?totalAuthors) (COUNT(DISTINCT ?publications) as ?totalPub)"
//                + "    WHERE {"
//                + "      GRAPH <" + con.getCentralGraph() + "> {"
//                + "        ?author a foaf:Person."
//                + "        ?author dct:provenance ?provenance."
//                + "        ?author foaf:publications ?publications."
//                + "        GRAPH <" + con.getEndpointsGraph() + "> {"
//                + "          ?provenance uc:name ?ies."
//                + "        }"
//                + "      }"
//                + "    } GROUP BY ?provenance ORDER BY DESC(?totalAuthors)"
//                + "  }"
//                + "}";
//</editor-fold>
  }

  @Override
  public String getAggreggationAuthors() {
    return PREFIXES
            + "CONSTRUCT { "
            + "  ?org a uc:Endpoint;             "
            + "                uc:name ?label;              "
            + "                uc:total ?total."
            + "} WHERE {    "
            + "	SELECT DISTINCT ?org (COUNT(DISTINCT(?author)) as ?total) (SAMPLE(?name) as ?label)"
            + "     WHERE { "
            + "  GRAPH <" + con.getCentralGraph() + "> {"
            + "  		?author  <http://schema.org/memberOf> ?org."
            + "	}"
            + "	GRAPH <" + con.getOrganizationsGraph() + "> {"
            + "  		?org  a foaf:Organization ;"
            + "               uc:name ?name "
            + "	}"
            + "} GROUP BY ?org "
            + "}";
//<editor-fold defaultstate="collapsed" desc="old query">
//                + "CONSTRUCT { "
//                + "?provenance a uc:Endpoint;"
//                + "              uc:name ?name;"
//                + "              uc:total ?total."
//                + "} WHERE { "
//                + "  GRAPH <" + con.getCentralGraph() + "> { "
//                + "    SELECT ?provenance ?name (COUNT(DISTINCT(?author)) AS ?total)"
//                + "    WHERE { "
//                + "      ?author a foaf:Person;"
//                + "                foaf:publications ?pub;"
//                + "                dct:provenance ?provenance . "
//                + "      GRAPH <" + con.getEndpointsGraph() + "> {"
//                + "        ?provenance uc:name ?name ."
//                + "      }"
//                + "    } GROUP BY ?provenance ?name "
//                + "  } "
//                + "} ";
//</editor-fold>
  }

  @Override
  public String getAggreggationAuthorsbyCountry() {
    return PREFIXES
            + " CONSTRUCT { "
            + "                     ?co uc:name ?country;        "
            + "                                 uc:total ?total. "
            + "                 } where { "
            + "                      SELECT DISTINCT ?co ?country (COUNT(DISTINCT(?author)) as ?total) "
            + "                      WHERE { "
            + "                   GRAPH <" + con.getCentralGraph() + "> { "
            + "                              ?author  <http://schema.org/memberOf> ?org. "
            + "                      } "
            + "                      GRAPH <" + con.getOrganizationsGraph() + "> { "
            + "                              ?org  a foaf:Organization ; "
            + "                                uc:country  ?country . "
            + "                       BIND( IRI(CONCAT(uc:country,?country)) as ?co) "
            + "                      } "
            + "                 } GROUP BY ?co  ?country "
            + "                 }";

  }

  @Override
  public String getAggregationPublications() {
    return PREFIXES
            + "CONSTRUCT {      "
            + "  ?org uc:total ?totalp;"
            + "              uc:name ?label."
            + "} WHERE {    "
            + "	SELECT DISTINCT ?org (count(DISTINCT ?pub) as ?totalp) (SAMPLE(?name) as ?label)"
            + "WHERE { "
            + "  GRAPH <" + con.getCentralGraph() + "> {"
            + "  		?author  <http://schema.org/memberOf> ?org;"
            + " foaf:publications ?pub."
            + "	}"
            + "	GRAPH <" + con.getOrganizationsGraph() + "> {"
            + "  		?org  a foaf:Organization ;"
            + "               uc:name ?name "
            + "	}"
            + "} GROUP BY ?org "
            + "}";
//<editor-fold defaultstate="collapsed" desc="old query">
//                + "CONSTRUCT { "
//                + "     ?provenance uc:total ?totalp."
//                + "     ?provenance uc:name ?sname."
//                + "} WHERE {"
//                + "  {"
//                + "    SELECT DISTINCT ?provenance (SAMPLE(?sourcename)  as ?sname)  (count(DISTINCT ?pub) as ?totalp)"
//                + "    WHERE {"
//                + "      GRAPH <" + con.getCentralGraph() + "> {"
//                + "        ?author a foaf:Person;"
//                + "             foaf:publications ?pub;"
//                + "             dct:provenance ?provenance."
//                + "        GRAPH <" + con.getEndpointsGraph() + "> {"
//                + "          ?provenance uc:name ?sourcename."
//                + "        }"
//                + "      }"
//                + "    } group by ?provenance "
//                + "  }"
//                + "}";
//</editor-fold>
  }

  @Override
  public String getAggregationPublicationsbyCountry() {
    return PREFIXES
            + " CONSTRUCT {   "
            + "                    ?co uc:name ?country; "
            + "                         uc:total ?totalp. "
            + "                 }WHERE { "
            + "  "
            + "         SELECT DISTINCT ?co ?country (count(DISTINCT ?pub) as ?totalp) "
            + "                 WHERE { "
            + "                   GRAPH <" + con.getCentralGraph() + "> { "
            + "                              ?author  <http://schema.org/memberOf> ?org; "
            + "                  foaf:publications ?pub. "
            + "                      } "
            + "                      GRAPH <" + con.getOrganizationsGraph() + "> { "
            + "                              ?org  a foaf:Organization ; "
            + "                                uc:country  ?country . "
            + "                      BIND( IRI(CONCAT(uc:country,?country)) as ?co) "
            + "                      } "
            + "                 } GROUP BY ?co ?country "
            + "                 }";

  }

  @Override
  public String getCountCountry() {
    return PREFIXES
            + " SELECT  (count(DISTINCT  lcase(?country)) as ?ncountry)  "
            + "                 WHERE { "
            + "                 	GRAPH <" + con.getOrganizationsGraph() + "> {"
            + "                   		?org   uc:country  ?country "
            + "                 	} "
            + "                 }";
  }

  @Override
  public String getAggregationAreas() {
    return PREFIXES
            + "CONSTRUCT {     "
            + "  ?area a uc:ResearchArea;                "
            + "             uc:name ?label;                "
            + "             uc:total ?total."
            + "} WHERE {   "
            + "  {  "
            + "SELECT ?area (COUNT(DISTINCT ?author) as ?total) (SAMPLE(?name) as ?label) "
            + "WHERE {  "
            + "  GRAPH <" + con.getClusterGraph() + "> {"
            + "    ?area rdf:type uc:Cluster;"
            + "          rdfs:label ?name."
            + "    ?author dct:isPartOf ?area."
            + "  }"
            + "} GROUP BY ?area "
            + "  }"
            + "}";
//<editor-fold defaultstate="collapsed" desc="old query">
//                + "CONSTRUCT {"
//                + "     ?keyword a uc:ResearchArea;"
//                + "                uc:name ?label;"
//                + "                uc:total ?total."
//                + "} WHERE { "
//                + "  {"
//                + "    SELECT DISTINCT ?keyword (SAMPLE(?k) as ?label) (COUNT(?keyword) AS ?total) "
//                + "    WHERE {"
//                + "      GRAPH <" + con.getCentralGraph() + "> {"
//                + "        ?author foaf:publications ?publications."
//                + "        ?publications dct:subject ?keyword."
//                + "        ?keyword rdfs:label ?k."
//                + "      }"
//                + "    }"
//                + "    GROUP BY ?keyword"
//                + "    ORDER BY DESC(?total)"
//                + "    LIMIT 15 "
//                + "  }"
//                + "}";
//</editor-fold>
  }

  @Override
  public String getKeywordsFrequencyPub() {
    return PREFIXES
            + "CONSTRUCT { "
            + "  ?area rdfs:label ?label "
            + "}WHERE {  "
            + "  SELECT  ?area  ?label"
            + "  WHERE {    "
            + "    GRAPH <" + con.getClusterGraph() + "> {            "
            + "      ?area rdf:type uc:Cluster; "
            + "            rdfs:label ?label .    "
            + "    }  "
            + "  } "
            + "}";
 /*    + "CONSTRUCT { "
            + "  ?area rdfs:label ?label "
            + "}WHERE {  "
            + "  SELECT  ?area (sample(?name) as ?label)"
            + "  WHERE {    "
            + "    GRAPH <" + con.getClusterGraph() + "> {            "
            + "      ?area rdf:type uc:Cluster; "
            + "            rdfs:label ?name.    "
            + "    }  "
            + "  }group by ?area"
            + "}";*/
  }
  
  
  @Override
  public String getGeneralCluster() {
    return PREFIXES
            + " SELECT  ?area  ?label_es ?label_en " +
"                WHERE {    \n" +
"                  GRAPH <"+ con.getClusterGraph() +"> {            \n" +
"                     ?area rdf:type uc:Cluster . \n" +
"                     ?area rdfs:label ?label_es. \n" +
"                     filter (lang (?label_es) = 'es') \n" +
"        			 ?area rdfs:label ?label_en. \n" +
"        			 filter (lang (?label_en) = 'en')\n" +
"                }  \n" +
"              }";

  }
  
  


  @Override
  public String getAuthorsCentralGraph() {
    return PREFIXES
            + "select distinct ?a {\n"
            + "  graph <" + con.getCentralGraph() + "> {\n"
            + "    ?a foaf:publications [] .\n"
            + "    ?a <http://schema.org/memberOf> ?o.\n"
            + "  }\n"
            + "  graph <" + con.getOrganizationsGraph() + "> {\n"
            + "  	?o a foaf:Organization .\n"
            + "  }\n"
            + "} order by rand()";
  }

  @Override
  public String getClusterURIs() {
    return PREFIXES
            + "SELECT distinct *\n"
            + "WHERE {\n"
            + "  GRAPH <" + con.getClusterGraph() + "> {\n"
            + "    ?c a uc:Cluster\n"
            + "  }\n"
            + "}";
  }

  @Override
  public String getCountries() {
    return PREFIXES
            + "SELECT distinct ?co WHERE {\n"
            + "  graph <" + con.getOrganizationsGraph() + "> {\n"
            + "  ?subject  uc:country  ?co \n"
            + "           }\n"
            + "}";
  }

  @Override
  public String getClusterAndSubclusterURIs() {
    return PREFIXES
            + "SELECT DISTINCT * WHERE {\n"
            + "  GRAPH <" + con.getClusterGraph() + "> {\n"
            + "    ?subcluster a uc:SubCluster;\n"
            + "             dct:isPartOf ?cluster.\n"
            + "    ?cluster a uc:Cluster.\n"
            + "  }\n"
            + "}";
  }

  @Override
  public String getClusterTotals() {
    return "PREFIX uc: <http://ucuenca.edu.ec/ontology#>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "      SELECT ?area ?labeles ?labelen  (COUNT(DISTINCT ?authors) AS ?totalAuthors)\n"
            + "              WHERE {\n"
            + "                GRAPH <" + con.getClusterGraph() + "> { \n"
            + "                  ?area a uc:Cluster .\n"
            + "  ?area   rdfs:label ?labeles .\n" 
            + "       filter ( lang(?labeles) = 'es' ) .\n" 
            + "	  ?area   rdfs:label ?labelen .\n" 
            + "    filter ( lang(?labelen) = 'en' ) .\n" 
            + "                  ?authors dct:isPartOf ?area .\n"
            + "                } \n"
            + "                GRAPH <" + con.getCentralGraph() + "> { \n"
            + "                         ?authors  a foaf:Person . \n"
            + "                } \n"
            + "      } GROUP BY ?area ?labeles ?labelen ";
  }

  @Override
  public String getSubClusterTotals(String uri) {
    return PREFIXES + "SELECT ?sc ?labeles ?labelen (COUNT(DISTINCT ?authors) AS ?totalAuthors) \n"
            + "                          WHERE {\n"
            + "                            GRAPH <" + con.getClusterGraph() + "> { \n"
            + "                              <" + uri + "> a uc:Cluster .\n"
            + "                              ?sc dct:isPartOf <" + uri + "> .\n"
            + "                              ?sc    a uc:SubCluster .\n"
            + "                              ?sc   rdfs:label ?labeles .\n" 
            + "                              filter ( lang(?labeles) = 'es' ) .\n" 
            + "                              ?sc   rdfs:label ?labelen .\n" 
            + "                              filter ( lang(?labelen) = 'en' ) ."
            + "                              ?authors dct:isPartOf ?sc .\n"
            + "                            }\n"
            + "                            GRAPH <" + con.getCentralGraph() + "> { \n"
            + "                               ?authors a foaf:Person .\n"
            + "                            }\n"
            + "                          } GROUP BY ?sc ?labeles ?labelen  ";
  }

  @Override
  public String getClustersbyInst(String uri) {
    return PREFIXES + "SELECT ?area (SAMPLE(?namengs) as ?nameng) (COUNT(DISTINCT ?author) as ?total)      WHERE {  \n"
            + "  GRAPH <" + con.getCentralGraph() + "> {\n"
            + "  ?author schema:memberOf  <" + uri + "> \n"
            + "          }\n"
            + "                GRAPH <" + con.getClusterGraph() + "> {\n"
            + "                  ?area a uc:Cluster;\n"
            + "                   rdfs:label ?namengs .\n"
            + "                  FILTER( lang(?namengs) = 'en') .\n"
            + "                   ?author dct:isPartOf ?area.\n"
            + "                  }\n"
            + "                } GROUP BY ?area   Order by DESC(?total)";

  }

  @Override
  public String getDatesPubbyInst(String uri) {
    return PREFIXES + "SELECT  ?y  (COUNT( ?publication ) as ?total)   \n"
            + "WHERE {\n"
            + "  graph <" + con.getCentralGraph() + "> {\n"
            + "  ?author schema:memberOf  <" + uri + "> .\n"
            + "  ?author foaf:publications ?publication.  \n"
            + "  ?publication bibo:created ?yx . BIND (str(?yx) as ?y2) .\n"
            + "   bind( strbefore( ?y2, '-' ) as ?y3 ).  \n"
            + "   bind( strafter( ?y2, ' ' ) as ?y4 ). \n"
            + "   bind( if (str(?y3)='' && str(?y4)='',?y2, if(str(?y3)='',strafter( ?y2, ' ' ),strbefore( ?y2, '-' ))) as ?y ) \n"
            + "     \n"
            + "  }\n"
            + "}  GROUP BY ?y Order by ASC(?y)";

  }

  @Override
  public String getAuthorsbyInst(String uri) {
    return PREFIXES + "SELECT  ?author (group_concat( distinct ?names  ; separator=';') as ?name)  (COUNT( distinct ?publication ) as ?total)   \n"
            + "WHERE {\n"
            + "  graph <" + con.getCentralGraph() + "> {\n"
            + "  ?author schema:memberOf  <" + uri + "> .\n"
            + "  ?author foaf:publications ?publication.  \n"
            + "   ?author foaf:name ?names\n"
            + "     \n"
            + "  }\n"
            + "} GROUP BY ?author Order by DESC(?total) limit 10";

  }

  @Override
  public String getInstAsobyInst(String uri) {
    return PREFIXES + "SELECT ?org    ?norg  ( COUNT (distinct ?author) as ?total )\n"
            + "WHERE {\n"
            + "  graph <" + con.getCentralGraph() + "> {\n"
            + "  ?author schema:memberOf  <" + uri + "> .\n"
            + "  ?author foaf:publications ?pub .\n"
            + "  ?coauthor   foaf:publications   ?pub .\n"
            + "   filter ( ?author != ?coauthor) .\n"
            + "    ?coauthor schema:memberOf ?org .\n"
            + "    filter (<" + uri + "> != ?org)\n"
            + "  }\n"
            + "     graph <" + con.getOrganizationsGraph() + "> {\n"
            + "     ?org   <http://ucuenca.edu.ec/ontology#fullName>  ?norg .\n"
            + "               FILTER( lang(?norg) = 'es') .\n"
            + "        }\n"
            + "} group by ?org ?norg order  by DESC (?total)";

  }
  
    @Override
  public String getAreasSubAreasPub (  ){
  /*return PREFIXES + "select ?area ?label ?subarea ?labels where {"
          + "graph <"+con.getClusterPublicationsGraph()+">"
          + "{ ?area a <http://ucuenca.edu.ec/ontology#Cluster> ."
          + "  ?area rdfs:label ?label ."
          + " ?subarea dct:isPartOf ?area ."
          + " ?subarea a <http://ucuenca.edu.ec/ontology#SubCluster> ."
          + " ?subarea rdfs:label ?labels"
          + "}"
          + "} ORDER BY DESC (?area)";*/
  
  return PREFIXES + "select distinct ?area ?labelaen ?labelaes ?subarea ?labelsen ?labelses where {\n" +
          "          graph <"+con.getClusterPublicationsGraph()+">\n" +
          "          { \n" +

          "           ?area a <http://ucuenca.edu.ec/ontology#Cluster> .\n" +
          "           ?area rdfs:label ?labelaen .\n" +
          "           filter ( lang(?labelaen) = \"en\") .\n" +
          "           ?area rdfs:label ?labelaes .\n" +
          "           filter ( lang(?labelaes) = \"es\") .        	 \n" +
          "           ?subarea dct:isPartOf ?area .\n" +
          "           ?subarea a <http://ucuenca.edu.ec/ontology#SubCluster> .\n" +
          "           ?subarea rdfs:label ?labelsen .\n" +
          "           filter ( lang(?labelsen) = \"en\") .\n" +
          "           ?subarea rdfs:label ?labelses .\n" +
          "           filter ( lang(?labelses) = \"es\") .}\n" +

          "          } ORDER BY DESC (?area)";
}


 @Override
  public String getOrgAreasPub ( String org , String area ){
      String onlyarea = "";
      if (!area.isEmpty()){
       onlyarea  = " values ( ?area) {  (<"+area+">) } ";
      } 
      String query = PREFIXES + 
         "select  ?area  (COUNT( distinct ?publication ) as ?total)  where { \n" +
              onlyarea +
          "    graph <"+con.getClusterPublicationsGraph()+"> {\n" +
          "    ?publication a <http://purl.org/ontology/bibo/AcademicArticle> .\n" +
          "        ?publication dct:isPartOf ?area .\n" +
          "        ?area a    <http://ucuenca.edu.ec/ontology#Cluster>\n" +
          "        graph <" + con.getCentralGraph() + "> {\n" +
          "              values ( ?org) {  (<ORGVALUE>) } \n" +
          "	      ?author foaf:publications	?publication .\n" +
          "           ?author schema:memberOf ?org .\n" +
          "        }\n" +
          "    } \n" +
          "} GROUP BY  ?area  ";
      
  return query.replace( "ORGVALUE" , org);
}
  
  @Override
  public String getAuthorAreasPub ( String author , String area ){
      String onlyarea = "";
      if (!area.isEmpty()){
       onlyarea  = " values ( ?area) {  (<http://dbpedia.org/resource/Computer_Science>) } ";
      }  
  return PREFIXES + 
         "select  ?area  (COUNT( distinct ?publication ) as ?total)  where { \n" +
              onlyarea +
          "    graph <"+con.getClusterPublicationsGraph()+"> {\n" +
          "    ?publication a <http://purl.org/ontology/bibo/AcademicArticle> .\n" +
          "        ?publication dct:isPartOf ?area .\n" +
          "            ?area a    <http://ucuenca.edu.ec/ontology#Cluster>\n" +
          "        graph <" + con.getCentralGraph() + "> {\n" +
          "              values ( ?author ) {  (<AUTVALUE>) } \n" +
          "	      ?author foaf:publications	?publication .\n" +
          "        }\n" +
          "    } \n" +
          "} GROUP BY  ?area  ".replace( "AUTVALUE" , author);
}  
  
  
  @Override
  public String getResearchPubDate ( String area ){
  return PREFIXES + "select ?area ?y  (COUNT( distinct ?publication ) as ?total)  where { \n" +
  "   values ( ?area) {  (<"+area+">) }\n" +
  "    graph <"+con.getClusterPublicationsGraph()+"> {\n" +
  "    ?publication a <http://purl.org/ontology/bibo/AcademicArticle> .\n" +
  "        ?publication dct:isPartOf ?area .\n" +
  "        graph <"+ con.getCentralGraph() +"> {\n" +
  "        	?publication <http://schema.org/copyrightYear>|<http://ns.nature.com/terms/coverDate> ?yx .\n" +  
  "            BIND (str(?yx) as ?y2) .\n" +
  "		    bind( strbefore( ?y2, '-' ) as ?y3 ). \n" +
  "            bind( strafter( ?y2, ' ' ) as ?y4 ). \n" +
  "            bind( if (str(?y3)='' && str(?y4)='',?y2, if(str(?y3)='',strafter( ?y2, ' ' ),strbefore( ?y2, '-' ))) as ?y ) .\n" +
  "            FILTER regex(?y, '^[0-9]*$')\n" +
  "        }\n" +
  "    } \n" +
  "} GROUP BY ?area ?y Order by ASC(?y)";
 
}
  

  @Override
  public String getProvbyInst(String uri) {
    return PREFIXES + "SELECT      ?prov (COUNT (distinct ?pub) as ?total)\n"
            + "WHERE {\n"
            + "  graph <" + con.getCentralGraph() + "> {\n"
            + "  ?author schema:memberOf  <" + uri + "> .\n"
            + "  ?author foaf:publications ?pub .\n"
            + "  ?pub  dct:provenance ?prov\n"
            + "} \n"
            + "} GROUP BY ?prov order by DESC (?total)\n";

  }

  @Override
  public String getAuthorPubbyDate(String uri) {
     return PREFIXES + " SELECT  ?y  (COUNT( distinct ?publication ) as ?total)   \n"
            + "                WHERE {\n"
            + "                  graph <" + con.getCentralGraph() + "> {\n"
            + "      \n"
            + "              <" + uri + ">  foaf:publications ?publication.  \n"
            + "                  ?publication <http://schema.org/copyrightYear>|<http://ns.nature.com/terms/coverDate>  ?yx   .               "
            + "         BIND (str(?yx) as ?y2) .\n" +
"                        bind( strbefore( ?y2, '-' ) as ?y3 ).  \n" +
"                        bind( strafter( ?y2, ' ' ) as ?y4 ). \n" +
"                         bind( if (str(?y3)='' && str(?y4)='',?y2, if(str(?y3)='',strafter( ?y2, ' ' ),strbefore( ?y2, '-' ))) as ?y ) .\n" +
"                          FILTER regex(?y, '^[0-9]*$')"
            + "                  }\n"
            + "                }  GROUP BY ?y Order by ASC(?y)";
     
   /* return PREFIXES + " SELECT  ?y  (COUNT( distinct ?publication ) as ?total)   \n"
            + "                WHERE {\n"
            + "                  graph <" + con.getCentralGraph() + "> {\n"
            + "      \n"
            + "              <" + uri + ">  foaf:publications ?publication.  \n"
            + "                  ?publication bibo:created ?yx . BIND (str(?yx) as ?y2) .\n"
            + "                   bind( strbefore( ?y2, '-' ) as ?y3 ).  \n"
            + "                   bind( strafter( ?y2, ' ' ) as ?y4 ). \n"
            + "                   bind( if (str(?y3)='' && str(?y4)='',?y2, if(str(?y3)='',strafter( ?y2, ' ' ),strbefore( ?y2, '-' ))) as ?y ) \n"
            + "                     \n"
            + "                  }\n"
            + "                }  GROUP BY ?y Order by ASC(?y)"; */

  }
  
  
  @Override
  public String getDocumentbyArea (String uriarea) {
   return PREFIXES + "select ?doc (group_concat( distinct lcase(?l) ; separator = ' ') as ?documentText) ?tl ?y {\n" +
          "              graph <" + con.getCentralGraph() + "> {\n" +
          "               graph <" + con.getClusterGraph() + "> {\n" +
          "                ?person   dct:isPartOf <"+uriarea+"> .\n" +
          "                 #?cl a uc:Cluster ;        \n" +
          "                 }\n" +
          "		        ?o uc:memberOf <https://redi.cedia.edu.ec/> .\n" +
          "		        ?person schema:memberOf ?o .\n" +
          "		        ?person foaf:publications ?doc .        \n" +
          "		        ?doc  dct:subject ?s .\n" +
          "                OPTIONAL { ?doc uc:translation ?tl } .\n" +
          "		        ?s rdfs:label ?l .\n" +
          "                ?doc <http://schema.org/copyrightYear>|<http://ns.nature.com/terms/coverDate>  ?yx .               \n" +
          "                     BIND (str(?yx) as ?y2) . \n" +
          "                     bind( strbefore( ?y2, '-' ) as ?y3 ).   \n" +
          "                     bind( strafter( ?y2, ' ' ) as ?y4 ).  \n" +
          "                     bind( if (str(?y3)='' && str(?y4)='',?y2, if(str(?y3)='',strafter( ?y2, ' ' ),strbefore( ?y2, '-' ))) as ?y ) . FILTER regex(?y, '^[0-9]*$')}\n" +
          "		    \n" +
          "		} group by ?doc ?tl ?y ";
  }

  @Override
  public String getConferencebyAuthor(String uri) {
    return PREFIXES + "SELECT ?b  (GROUP_CONCAT(DISTINCT STR(?y); separator=';') as ?name)  (COUNT( distinct ?publication ) as ?total)   \n"
            + "                WHERE {\n"
            + "                  graph <" + con.getCentralGraph() + "> {\n"
            + "                  <" + uri + ">  foaf:publications ?publication.  \n"
            + "                  ?publication  dct:isPartOf  ?b.\n"
            + "                   ?b rdfs:label ?y .\n"
            + "                   ?b a  <http://purl.org/ontology/bibo/Conference> \n"
            + "                  }\n"
            + "                }  GROUP BY ?b Order by Desc(?total)";
  }

  @Override
  public String getJournalbyAuthor(String uri) {
    return PREFIXES + "SELECT ?b  (GROUP_CONCAT(DISTINCT STR(?y); separator=';') as ?name)  (COUNT( distinct ?publication ) as ?total)   \n"
            + "                WHERE {\n"
            + "                  graph <" + con.getCentralGraph() + "> {\n"
            + "                  <" + uri + ">  foaf:publications ?publication.  \n"
            + "                  ?publication  dct:isPartOf  ?b.\n"
            + "                   ?b rdfs:label ?y .\n"
            + "                   ?b a  <http://purl.org/ontology/bibo/Journal> \n"
            + "                     \n"
            + "                  }\n"
            + "                }  GROUP BY ?b Order by Desc(?total)";
  }

  @Override
  public String getOrgbyAuyhor(String uri) {
    return PREFIXES + "SELECT  distinct ?org ?orgname \n"
            + "#?b  (GROUP_CONCAT(DISTINCT STR(?y); separator=';') as ?name)  (COUNT( ?publication ) as ?total)   \n"
            + "                WHERE {\n"
            + "                  graph <" + con.getCentralGraph() + "> { "
            + "              <" + uri + "> <http://schema.org/memberOf> ?org .\n"
            + "                    ?org foaf:name ?orgname .                     \n"
            + "                  }"
            + "                } ";

  }

  @Override
  public String getRelevantKbyAuthor(String uri, int limit) {
    return PREFIXES + "  SELECT   (SAMPLE ( ?l ) as ?lsubject)  (COUNT (?pub) as ?npub)    { \n"
            + "                 VALUES ?author { <" + uri + "> } .  \n"
            + "                   ?author     foaf:publications ?pub . \n"
            + "                ?pub dct:subject ?subject . \n"
            + "                ?subject rdfs:label ?slabel .\n"
            + "                 BIND ( LCASE(?slabel) as ?l  )\n"
            + "  } GROUP BY ?l ORDER BY DESC (?npub) limit " + limit;
  }

  @Override
  public String getRelevantProvbyAuthor(String uri) {
    return PREFIXES + "SELECT   ?prov (COUNT (distinct ?pub) as ?total)\n"
            + "                WHERE {\n"
            + "                  graph <" + con.getCentralGraph() + "> {\n"
            + "                <" + uri + "> foaf:publications ?pub .\n"
            + "                    ?pub  dct:provenance  ?prov .\n"
            + "                  }\n"
            + "                } GROUP BY ?prov order by desc (?total)";
  }
  
  @Override
  public String getProjects () {
   return PREFIXES + "select distinct ?uri \n" +
                      "where { " +
                      "    graph <" + con.getCentralGraph() + "> {\n" +
                      "        ?uri a <http://xmlns.com/foaf/0.1/Project>  \n" +
                      "   }" +
                      "}";
  }
  
  
  @Override
  public String getProjectInfo (String uri) {
   return PREFIXES + "select distinct ?title (CONCAT(STR(DAY(?sdate)), '-', STR(MONTH(?sdate)), '-', STR(YEAR(?sdate))) as ?starDate)  (CONCAT(STR(DAY(?edate)), '-', STR(MONTH(?edate)), '-', STR(YEAR(?edate))) as ?endDate) (GROUP_CONCAT(DISTINCT STR(?funded); separator='|') as ?funders) (GROUP_CONCAT(DISTINCT STR(?org); separator='|') as ?orgs)\n" +
                      "where {\n" +
                      "    graph <" + con.getCentralGraph() + "> {\n" +
                      "<"+uri+"> dct:title ?title .\n" +
                      "OPTIONAL {  <"+uri+"> cerif:StartDate ?sdate   }\n" +
                      "OPTIONAL {  <"+uri+"> cerif:EndDate ?edate .   }\n" +
                      "OPTIONAL {  <"+uri+"> foaf:fundedBy ?funded  }       \n" +
                      "OPTIONAL {  <"+uri+"> <https://www.openaire.eu/cerif-profile/1.1/linksToOrganisationUnit> ?org \n" +
                      "        } " +
                      "   } " +
                      "} group by ?title  ?sdate ?edate ";
  }
  
  @Override
  public String getPatents () {
   return PREFIXES + "select distinct ?uri \n" +
                      "where { " +
                      "    graph <" + con.getCentralGraph() + "> {\n" +
                      "        ?uri a <http://www.eurocris.org/ontologies/cerif/1.3/Patent>  \n" +
                      "   }" +
                      "}";
  }
  
  @Override
  public String getPatentInfo (String uri) {
   return PREFIXES + "select distinct ?title ?pnumber ?abstract ?rdate ?adate ?edate ?link ?name (GROUP_CONCAT(DISTINCT STR(?lorg); separator='|') as ?lorgs)  (GROUP_CONCAT(DISTINCT STR(?subject); separator='|') as ?subjects) " +
                      "where {\n" +
                      "    graph <" + con.getCentralGraph() + "> {\n" +
                      "<"+uri+"> dct:title ?title .\n" +
                      "<"+uri+"> cerif3:patentNumber ?pnumber . " +
                      "OPTIONAL {  <"+uri+"> dct:subject ?subject   }\n" +
                      "OPTIONAL {  <"+uri+"> dct:abstract ?abstract   }\n" +
                      "OPTIONAL {  <"+uri+"> cerif3:registrationDate ?rdate   }\n" +
                      "OPTIONAL {  <"+uri+"> cerif3:approvalDate ?adate .   }\n" +
                      "OPTIONAL {  <"+uri+"> cerif3:endDate ?edate .   }\n" +
                      "OPTIONAL {  <"+uri+"> cerif3:link ?link .   }\n" +
                      "OPTIONAL {  <"+uri+"> cerif3:name ?name .   }\n" +
                      "OPTIONAL {  <"+uri+"> <http://schema.org/affiliation> ?lorg .   }\n" +
                      "   } " +
                      "} group by ?title  ?pnumber ?abstract ?rdate ?adate ?edate ?link ?name";
  }  

  @Override
  public String getAuthorsbyArea(String uri) {
   return PREFIXES + "select ?uri (group_concat( distinct ?name ; separator = ';') as ?names) (COUNT (?p) as ?number)   where {  \n" +
          "graph <" + con.getCentralGraph() + "> {\n" +
          "               graph <" + con.getClusterGraph() + "> {\n" +
          "                ?uri   dct:isPartOf <"+uri+"> .      \n" +
          "                 }\n" +
          "		        ?org uc:memberOf <https://redi.cedia.edu.ec/> .\n" +
          "		        ?uri schema:memberOf ?org .\n" +
          "        		?uri foaf:name ?name .\n" +
          "                ?uri foaf:publications ?p .\n" +
          "              \n" +
          "   }\n" +
          "} group by ?uri order by DESC (?number)";
  }

  @Override
  public String getOrgsbyArea(String uri) {
  return PREFIXES + "select ?uri (group_concat( distinct ?name ; separator = ';') as ?names)   (COUNT (?person) as ?number)   where {  \n" +
          "graph <" + con.getCentralGraph() + "> {\n" +
          "               graph <" + con.getClusterGraph() + "> {\n" +
          "                ?person   dct:isPartOf <"+uri+">  .      \n" +
          "                 }\n" +
          "		        ?uri uc:memberOf <https://redi.cedia.edu.ec/> .\n" +
          "                     ?uri foaf:name ?name .\n" +
          "		        ?person schema:memberOf ?uri .\n" +
          "\n" +
          "   }\n" +
          "} group by ?uri order by DESC (?na)";
  
  }
    

  @Override
  public String getProvbyArea(String uri) {
    return PREFIXES + "select ?uri (group_concat( distinct ?name ; separator = ';') as ?names) (COUNT (?p) as ?number)   where {  \n" +
                      "graph <" + con.getCentralGraph() + "> {\n" +
                      "               graph <" + con.getClusterGraph() + "> {\n" +
                      "                ?author   dct:isPartOf <"+uri+"> .      \n" +
                      "                 }\n" +
                      "                ?author  foaf:publications ?p. " +
                      "                ?p  dct:provenance  ?uri . " +
                      "   }\n" +
                      "} group by ?uri  order by DESC (?number)";
  }
  

}
