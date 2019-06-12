/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.authors.services;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.apache.marmotta.commons.vocabulary.FOAF;
import static org.apache.marmotta.commons.vocabulary.SCHEMA.url;
import static org.apache.marmotta.commons.vocabulary.SPARQL_SD.graph;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointFile;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointOAI;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointORCID;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointObject;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointSPARQL;
import org.apache.marmotta.ucuenca.wk.authors.api.EndpointsService;
import org.apache.marmotta.ucuenca.wk.authors.api.OrganizationService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.semarglproject.vocab.RDF;

/**
 *
 * @author joe
 */
public class EndpointsServiceImpl implements EndpointsService {

  @Inject
  private ExternalSPARQLService sparqlService;

  @Inject
  private QueriesService queriesService;

  @Inject
  private OrganizationService orgserv;

  @Inject
  private ConstantService con;

  private final static String INITIALSTATUS = "Active";

  private final static String STR = "^^xsd:string";

  private final static String FAIL = "Fail";

  @Override
  public String registerSPARQL(String type, String org, String url, String graph) {
    String resourceId = con.getEndpointBaseUri() + type + "/" + org;
    EndpointObject endpoint = new EndpointSPARQL(INITIALSTATUS, org, url, type, graph, resourceId);
    //  con.getEndpointBaseUri();
    try {
      return insertEndpoint(endpoint);

    } catch (MarmottaException ex) {
      Logger.getLogger(EndpointsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return FAIL + ex;
    }

  }

  @Override
  public String registerOAI(String type, String org, String url, Boolean severemode) {
    // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    String resourceId = con.getEndpointBaseUri() + type + "/" + org;
    EndpointObject endpoint = new EndpointOAI(INITIALSTATUS, org, url, type, resourceId, severemode);
    try {

      return insertEndpoint(endpoint);
    } catch (MarmottaException ex) {
      Logger.getLogger(EndpointsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return FAIL + ex;
    }
  }

  @Override
  public String registerFile(String type, String org, String file) {
    // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    String resourceId = con.getEndpointBaseUri() + type + "/" + org;
    EndpointObject endpoint = new EndpointFile(INITIALSTATUS, org, file, type, resourceId);
    try {
      return insertEndpoint(endpoint);
    } catch (MarmottaException ex) {
      Logger.getLogger(EndpointsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return FAIL + ex;
    }
  }

  @Override
  public String updateExtractionDate(String uri, String date) {
    try {
      String queryUpdate = queriesService.updateGeneric(con.getEndpointsGraph(), uri, REDI.EXTRACTIONDATE.toString(), date, STR);
      sparqlService.getSparqlService().update(QueryLanguage.SPARQL, queryUpdate);

      return "Success";
    } catch (InvalidArgumentException | MarmottaException | MalformedQueryException | UpdateExecutionException ex) {
      Logger.getLogger(EndpointsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return "Fail";
    }
  }

  private String insertEndpoint(EndpointObject endpoint) throws MarmottaException {
    if (!askEndpoint(endpoint.getResourceId())) {
      try {
        insertEndpoint(endpoint.getResourceId(), RDF.TYPE.toString(), REDI.ENDPOINT.toString(), STR);
        insertEndpoint(endpoint.getResourceId(), REDI.STATUS.toString(), endpoint.getStatus(), STR);
        insertEndpoint(endpoint.getResourceId(), REDI.URL.toString(), endpoint.getAccess(), STR);
        insertEndpoint(endpoint.getResourceId(), REDI.TYPE.toString(), endpoint.getType(), STR);
        insertEndpoint(endpoint.getResourceId(), REDI.GRAPH.toString(), endpoint.getGraph(), STR);
        insertEndpoint(endpoint.getResourceId(), REDI.EXTRACTIONDATE.toString(), "", STR);
        if (endpoint instanceof EndpointOAI) {
          insertEndpoint(endpoint.getResourceId(), REDI.EXTRACTION_MODE.toString(), ((EndpointOAI) endpoint).isSeveremode().toString(), STR);
        }
        String org = con.getOrganizationBaseUri() + endpoint.getName();
        if (orgserv.askOrganization(org)) {
          insertEndpoint(endpoint.getResourceId(), REDI.BELONGTO.toString(), org, STR);
        }
        return "Successfull Registration";
// insertEndpoint(endpoint.getResourceId(), REDI.GRAPH.toString() , endpoint.get, STR);
        //insertEndpoint(endpoint.getResourceId(), REDI.TYPE.toString() , endpoint. , STR);
      } catch (InvalidArgumentException | MalformedQueryException | UpdateExecutionException ex) {
        Logger.getLogger(EndpointsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        return "Fail:" + ex;
      }
    }
    return FAIL + " Endpoint Already Exist";

  }

  private String insertEndpoint(String... parameters) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {

    // String queryInsertEndpoint = queriesService.getInsertEndpointQuery(parameters[0], parameters[1], parameters[2], parameters[3]);
    String queryInsertOrg = queriesService.getInsertGeneric(con.getEndpointsGraph(), parameters[0], parameters[1], parameters[2], parameters[3]);
    sparqlService.getSparqlService().update(QueryLanguage.SPARQL, queryInsertOrg);
    return "Successfully Registration";

  }

  private boolean askEndpoint(String uri) throws MarmottaException {
    String askOrg = queriesService.getAskResourceQuery(con.getEndpointsGraph(), uri);
    return sparqlService.getSparqlService().ask(QueryLanguage.SPARQL, askOrg);

  }

  @Override
  public String listEndpoints() {
    try {
      //  ObjectMapper mapper = new ObjectMapper();
      String queryEnd = queriesService.getListEndpoints();
      List<Map<String, Value>> response = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, queryEnd);
      return listmapTojson(response);
    } catch (MarmottaException | JSONException ex) {
      Logger.getLogger(EndpointsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public String listmapTojson(List<Map<String, Value>> list) throws JSONException {
    JSONObject jsonh1 = new JSONObject();

    JSONArray jsonArr = new JSONArray();
    for (Map<String, Value> map : list) {
      JSONObject jsonObj = new JSONObject();
      for (Map.Entry<String, Value> entry : map.entrySet()) {
        String key = entry.getKey();
        String value = entry.getValue().stringValue();

        try {
          jsonObj.put(key, value);

        } catch (org.json.JSONException ex) {
          Logger.getLogger(OrganizationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      jsonArr.put(jsonObj);
    }

    //return jsonArr.toString();
    return jsonh1.put("data", jsonArr).toString();
  }

  @Override
  public String deleteEndpoint(String resourceid) {
    try {
      //   String queryRemove = queriesService.removeGeneric ( con.getOrganizationsGraph() , resourceid , RDF.TYPE.toString() , FOAF.ORGANIZATION.toString() , STR );
      //String queryRemove = queriesService.removeGeneric ( con.getEndpointsGraph() , resourceid , RDF.TYPE.toString() , REDI.ENDPOINT.toString() , STR );
      String queryDependecies = queriesService.removeGenericRelationwithDependecies(con.getAuthorsGraph(), DCTERMS.PROVENANCE.toString(), resourceid, FOAF.publications.toString());
      sparqlService.getSparqlService().update(QueryLanguage.SPARQL, queryDependecies);
      String queryAsociation = queriesService.removeGenericRelation(con.getAuthorsGraph(), DCTERMS.PROVENANCE.toString(), resourceid);
      sparqlService.getSparqlService().update(QueryLanguage.SPARQL, queryAsociation);
      String queryRemove = queriesService.removeGenericType(con.getEndpointsGraph(), REDI.ENDPOINT.toString(), resourceid);
      sparqlService.getSparqlService().update(QueryLanguage.SPARQL, queryRemove);
      return "Success delete";
      //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    } catch (InvalidArgumentException | MarmottaException | MalformedQueryException | UpdateExecutionException ex) {
      Logger.getLogger(EndpointsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return "Fail delete";
    }
  }

  // editOrganization(uriOrg, REDI.FULLNAME.toString(), namEn , LANGEN);
  @Override
  public String updateStatus(String uri, String oldstatus) {
    try {
      String newStatus = "";

      if ("Active".equals(oldstatus)) {
        newStatus = "Inactive";
      } else {
        newStatus = "Active";
      }

      String queryUpdate = queriesService.updateGeneric(con.getEndpointsGraph(), uri, REDI.STATUS.toString(), newStatus, STR);
      sparqlService.getSparqlService().update(QueryLanguage.SPARQL, queryUpdate);

      return "Success";
    } catch (InvalidArgumentException | MarmottaException | MalformedQueryException | UpdateExecutionException ex) {
      Logger.getLogger(EndpointsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return FAIL + ex;
    }
  }

  @Override
  public String registerORCID(String type, String org) {
    String resourceId = con.getEndpointBaseUri() + type + "/" + org;
    EndpointObject endpoint = new EndpointORCID(INITIALSTATUS, org, "None", type, "None", resourceId);
    try {
      return insertEndpoint(endpoint);
    } catch (MarmottaException ex) {
      Logger.getLogger(EndpointsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return FAIL + ex;
    }
  }

}
