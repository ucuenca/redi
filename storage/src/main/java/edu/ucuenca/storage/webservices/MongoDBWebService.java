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

import edu.ucuenca.storage.api.MongoService;
import edu.ucuenca.storage.exceptions.FailMongoConnectionException;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
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
    private SesameService sesameService;

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
    @Path("/cluster")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCluster(@QueryParam("uri") String uri) throws FailMongoConnectionException {
        String response;
        try {
            response = mongoService.getCluster(uri);
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

}
