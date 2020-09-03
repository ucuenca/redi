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
import java.util.List;
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
    @Path("/instbyproject")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getcolbyproject() throws FailMongoConnectionException {
        Document response;
        try {
            response = mongoService.getinstbyProject ("All");
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
    @Path("/getORCIDToken")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtainToken(@QueryParam("code") String code, @QueryParam("uri") String uri) throws FailMongoConnectionException {
        String body = null;
        try {
            String app = "";
            String sec = "";
            if (uri.contains("rediclon")) {
                app = "APP-R08L1P7JVVGRW8YN";
                sec = "8149275b-b855-4643-a4ed-02eb2f845866";
            } else {
                app = "APP-7Y3IFBAL9DB2NVJC";
                sec = "8cffcb48-d32a-48b0-b343-1fd48f2ac15f";
            }
            HttpResponse<JsonNode> asJson = Unirest.post("https://orcid.org/oauth/token")
                    .field("client_id", app)
                    .field("client_secret", sec)
                    .field("grant_type", "authorization_code")
                    .field("redirect_uri", uri)
                    .field("code", code).asJson();
            if (asJson.getStatus() == 200) {
                JsonNode body1 = asJson.getBody();
                enrichInfo(body1);
                body = prettyPrintJsonString(body1);
                String orcid = body1.getObject().getString("orcid");
                String access_token = body1.getObject().getString("access_token");
                mongoService.registerSession(orcid, access_token);
            } else {
                body = "{err:" + asJson.getStatusText() + "}";
            }
        } catch (Exception e) {
            throw new FailMongoConnectionException(String.format("Cannot retrieve ORCID-token %s", uri), e);
        }
        return Response.ok()
                .entity(body).build();
    }

    public JsonNode enrichInfo(JsonNode jsonNode) throws Exception {
        String at = jsonNode.getObject().getString("access_token");
        String orcid = jsonNode.getObject().getString("orcid");
        HttpResponse<String> asString = Unirest.get("https://pub.orcid.org/v2.0/" + orcid + "/record").header("Access token", at).asString();
        try {
            String bio = runXpath(asString.getBody(), "/record/person/biography/content");
            String gn = runXpath(asString.getBody(), "/record/person/name/given-names");
            String fn = runXpath(asString.getBody(), "/record/person/name/family-name");
            String em = runXpath(asString.getBody(), "/record/person/emails/email[1]/email");
            jsonNode.getObject().put("biography", bio);
            jsonNode.getObject().put("giveName", gn);
            jsonNode.getObject().put("familyName", fn);
            jsonNode.getObject().put("mail", em);
        } catch (Exception e) {
            throw new Exception(e);
        }
        return jsonNode;
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
}
