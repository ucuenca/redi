/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.ucuenca.storage.webservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import edu.ucuenca.storage.api.MongoService;
import edu.ucuenca.storage.exceptions.FailMongoConnectionException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.bson.Document;
import org.slf4j.Logger;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
@Path("/mongo")
@ApplicationScoped
public class MongoDBWebService {

  @Inject
  private Logger log;

  @Inject
  private ConfigurationService confService;

  @Inject
  private MongoService mongoService;

  @GET
  @Path("/author")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAuthor(@QueryParam("uri") String uri) throws FailMongoConnectionException {
    String response;
    try {
      response = mongoService.getAuthor(uri);
    } catch (Exception e) {
      throw new FailMongoConnectionException(String.format("Cannot retrieve author %s", uri), e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/author-stats")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStatsbyAuthor(@QueryParam("uri") String uri) throws FailMongoConnectionException {
    String response;
    try {
      response = mongoService.getStatisticsByAuthor(uri);
    } catch (Exception e) {
      throw new FailMongoConnectionException(String.format("Cannot retrieve author %s", uri), e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/cluster")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCluster(@QueryParam("uri") String uri) throws FailMongoConnectionException {
    Object response;
    try {
      String[] split = uri.split(";");
      response = mongoService.getCluster(split);
    } catch (Exception e) {
      throw new FailMongoConnectionException(String.format("Cannot retrieve cluster %s", uri), e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/clusters")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getClusters() throws FailMongoConnectionException {
    List<Document> response;
    try {
      response = mongoService.getClusters();
    } catch (Exception e) {
      throw new FailMongoConnectionException("Cannot retrieve clusters", e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/clustersTotals")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getClustersTotals() throws FailMongoConnectionException {
    List<Document> response;
    try {
      response = mongoService.getClustersTotals();
    } catch (Exception e) {
      throw new FailMongoConnectionException("Cannot retrieve clusters", e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/subclustersTotals")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getSubClustersTotals(@QueryParam("uri") String uri) throws FailMongoConnectionException {
    List<Document> response;
    try {
      response = mongoService.getSubClustersTotals(uri);
    } catch (Exception e) {
      throw new FailMongoConnectionException("Cannot retrieve subclusters", e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/countries")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCountries() throws FailMongoConnectionException {
    List<Document> response;
    try {
      response = mongoService.getCountries();
    } catch (Exception e) {
      throw new FailMongoConnectionException("Cannot retrieve countries", e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/project")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getProjects(@QueryParam("uri") String uri) throws FailMongoConnectionException {
    Document response;
    try {
      response = mongoService.getProfileProject(uri);
    } catch (Exception e) {
      throw new FailMongoConnectionException("Cannot retrieve projects", e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/patent")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPatent(@QueryParam("uri") String uri) throws FailMongoConnectionException {
    Document response;
    try {
      response = mongoService.getProfilePatent(uri);
    } catch (Exception e) {
      throw new FailMongoConnectionException("Cannot retrieve patents", e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/instbyproject")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getcolbyproject() throws FailMongoConnectionException {
    Document response;
    try {
      response = mongoService.getinstbyProject("All");
    } catch (Exception e) {
      throw new FailMongoConnectionException("Cannot retrieve projects", e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/statistics")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStatistics(@QueryParam("id") String id) throws FailMongoConnectionException {
    String response;
    try {
      response = mongoService.getStatistics(id);
    } catch (Exception e) {
      throw new FailMongoConnectionException(String.format("Cannot retrieve information for id %s", id), e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/statisticsbyInst")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStatisticsbyInst(@QueryParam("id") String id) throws FailMongoConnectionException {
    String response;
    try {
      response = mongoService.getStatisticsByInst(id);
    } catch (Exception e) {
      throw new FailMongoConnectionException(String.format("Cannot retrieve information for id %s", id), e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/Institution")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getInstProfile(@QueryParam("id") String id) throws FailMongoConnectionException {
    Document response;
    try {
      response = mongoService.getProfileInst(id);
    } catch (Exception e) {
      throw new FailMongoConnectionException(String.format("Cannot retrieve information of Institution for id %s", id), e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/statisticsbyArea")
  @Produces(MediaType.APPLICATION_JSON)
  public Response statisticsbyArea(@QueryParam("id") String id) throws FailMongoConnectionException {
    String response;
    try {
      response = mongoService.getStatisticsByArea(id);
    } catch (Exception e) {
      throw new FailMongoConnectionException(String.format("Cannot retrieve information for id %s", id), e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/authorByArea")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStatistics(@QueryParam("cluster") String cluster, @QueryParam("subcluster") String subcluster) throws FailMongoConnectionException {
    String response;
    try {
      response = mongoService.getAuthorsByArea(cluster, subcluster);
    } catch (Exception e) {
      throw new FailMongoConnectionException(String.format("Cannot retrieve information for cluster %s and subcluster", cluster, subcluster), e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/pubBySubArea")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPublicationSubAreas(@QueryParam("cluster") String cluster, @QueryParam("subcluster") String subcluster) throws FailMongoConnectionException {
    List<Document> response;
    try {
      response = mongoService.getPubBySubAreaDate(cluster, subcluster);
    } catch (Exception e) {
      throw new FailMongoConnectionException(String.format("Cannot retrieve publication's information for cluster %s and subcluster", cluster, subcluster), e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/pubByArea")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPublicationAreas(@QueryParam("cluster") String cluster) throws FailMongoConnectionException {
    List<Document> response;
    try {
      response = mongoService.getPubByAreaDate(cluster);
    } catch (Exception e) {
      throw new FailMongoConnectionException(String.format("Cannot retrieve publication's information for cluster %s ", cluster), e);
    }
    return Response.ok().entity(response).build();
  }

  @POST
  @Path("/reinit")
  public Response pingMongoDB() throws FailMongoConnectionException {
    mongoService.connect();
    return Response.ok().build();
  }

  @GET
  @Path("/relatedauthors")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRelatedAuthors(@QueryParam("uri") String uri) throws FailMongoConnectionException {
    String response;
    try {
      response = mongoService.getRelatedAuthors(uri);
    } catch (Exception e) {
      throw new FailMongoConnectionException(String.format("Cannot retrieve author %s", uri), e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/clusterDiscipline")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getclusterDiscipline(@QueryParam("uri") String uri) throws FailMongoConnectionException {
    String response;
    try {
      response = mongoService.getAuthorsByDiscipline(uri);
    } catch (Exception e) {
      throw new FailMongoConnectionException(String.format("Cannot retrieve Cluster %s", uri), e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/sparql")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getSPARQLGET(@QueryParam("query") String qry, @HeaderParam("Accept") String accept) throws FailMongoConnectionException {
    return getSPARQL(qry, accept);
  }

  @POST
  @Path("/sparql")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getSPARQL(@FormParam("query") String qry, @HeaderParam("Accept") String accept) throws FailMongoConnectionException {
    if (accept == null || accept.trim().isEmpty()) {
      accept = "application/ld+json";
    }
    String response;
    try {
      response = mongoService.getSPARQL(qry, accept);
    } catch (Exception e) {
      e.printStackTrace();
      throw new FailMongoConnectionException(String.format("Cannot retrieve cached-query %s", qry), e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/getKeyCloakToken")
  @Produces(MediaType.APPLICATION_JSON)
  public Response obtainToken(@QueryParam("code") String code, @QueryParam("uri") String uri) throws FailMongoConnectionException {
    String body = null;
    try {
      HttpResponse<JsonNode> asJson = Unirest.post("https://service.login.cedia.edu.ec/auth/realms/redi/protocol/openid-connect/token")
              .field("client_id", "rediclon")
              .field("grant_type", "refresh_token")
              .field("refresh_token", code).asJson();
      if (asJson.getStatus() == 200) {
        JsonNode body1 = asJson.getBody();
        Base64.Decoder decoder = Base64.getDecoder();
        String header = new String(decoder.decode(body1.getObject().getString("access_token").split("\\.")[1]));
        JsonObject parse = JSON.parse(header);
        String redi_token = UUID.randomUUID().toString();
        mongoService.registerSession(parse.getAsObject().get("email").getAsString().value(), redi_token);
        body = "{\"token\":\"" + redi_token + "\"}";
      } else {
        body = "{\"err\":\"" + asJson.getStatusText() + "\"}";
      }
    } catch (Exception e) {
      throw new FailMongoConnectionException(String.format("Cannot retrieve KeyCloak-token %s", uri), e);
    }
    return Response.ok()
            .entity(body).build();
  }

  public String runXpath(String data, String q) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(false);
    DocumentBuilder builder = factory.newDocumentBuilder();
    org.w3c.dom.Document parse = builder.parse(IOUtils.toInputStream(data));
    XPathFactory xpathFactory = XPathFactory.newInstance();
    XPath xpath = xpathFactory.newXPath();
    XPathExpression expr = xpath.compile(q);
    String name = (String) expr.evaluate(parse, XPathConstants.STRING);
    return name;
  }

  public String prettyPrintJsonString(JsonNode jsonNode) throws Exception {
    try {
      ObjectMapper mapper = new ObjectMapper();
      Object json = mapper.readValue(jsonNode.toString(), Object.class);
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    } catch (Exception e) {
      throw new Exception("Sorry, pretty print didn't work", e);
    }
  }

  @GET
  @Path("/getMetrics")
  @Produces(MediaType.APPLICATION_JSON)
  public Response obtainMetric(@QueryParam("metric") String metric, @QueryParam("uri") String uri) throws FailMongoConnectionException {
    String metricr = mongoService.getGlobalAuthorMetrics(metric, uri);
    return Response.ok().entity(metricr).build();
  }
}
