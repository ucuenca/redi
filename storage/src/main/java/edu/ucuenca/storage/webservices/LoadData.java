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

import edu.ucuenca.storage.api.PopulateMongo;
import edu.ucuenca.storage.exceptions.FailMongoConnectionException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
@Path("/populate")
@ApplicationScoped
public class LoadData {

  @Inject
  private PopulateMongo loadService;
  @Inject
  private Logger log;

  @POST
  @Path("/authors")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAuthor() throws FailMongoConnectionException {
    try {
      loadService.authors(null);
    } catch (Exception e) {
      log.error("Cannot load authors into Mongo DB", e);
      throw new FailMongoConnectionException(String.format("Cannot load authors into Mongo DB"), e);
    }
    return Response.ok().entity("Authors load successfully").build();
  }

  @POST
  @Path("/statistics")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStatistics() throws FailMongoConnectionException {
    try {
      loadService.statistics();
      loadService.cleanSPARQLS();
    } catch (Exception e) {
      log.error("Cannot load statistics into Mongo DB", e);
      throw new FailMongoConnectionException(String.format("Cannot load statistics into Mongo DB"), e);
    }
    return Response.ok().entity("Statistics load successfully").build();
  }

  @POST
  @Path("/statisticsbyInst")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStatisticsbyInst() throws FailMongoConnectionException {
    try {
      loadService.LoadStatisticsbyInst();

    } catch (Exception e) {
      log.error("Cannot load statistics by Inst into Mongo DB", e);
      throw new FailMongoConnectionException(String.format("Cannot load statistics into Mongo DB"), e);
    }
    return Response.ok().entity("Statistics load successfully").build();
  }

  @POST
  @Path("/clusters")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getClusters() throws FailMongoConnectionException {
    try {
      loadService.clusters();
      loadService.Countries();
    } catch (Exception e) {
      log.error("Cannot load clusters into Mongo DB", e);
      throw new FailMongoConnectionException(String.format("Cannot load clusters into Mongo DB"), e);
    }
    return Response.ok().entity("Clusters loaded successfully").build();
  }

  @POST
  @Path("/countries")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCountries() throws FailMongoConnectionException {
    try {
      loadService.Countries();
    } catch (Exception e) {
      log.error("Cannot load countries and countries into Mongo DB", e);
      throw new FailMongoConnectionException(String.format("Cannot load countries into Mongo DB"), e);
    }
    return Response.ok().entity("countries and countries loaded successfully").build();
  }

  @POST
  @Path("/networks")
  @Produces(MediaType.APPLICATION_JSON)
  public Response loadNetworks() throws FailMongoConnectionException {
    try {
      loadService.networks();
    } catch (Exception e) {
      log.error("Cannot load pre-calculated networks into Mongo DB", e);
      throw new FailMongoConnectionException(String.format("Cannot load pre-calculated networks into Mongo DB"), e);
    }
    return Response.ok().entity("Statistics load successfully").build();
  }

  @POST
  @Path("/author-by-area")
  @Produces(MediaType.APPLICATION_JSON)
  public Response loadAuthorByArea() throws FailMongoConnectionException {
    try {
      loadService.authorsByDiscipline();
      loadService.authorsByArea();
    } catch (Exception e) {
      log.error("Cannot load authors by area into Mongo DB", e);
      throw new FailMongoConnectionException(String.format("Cannot load authors by area into Mongo DB"), e);
    }
    return Response.ok().entity("Authors by area were successfully loaded into MongoDB").build();
  }

  @POST
  @Path("/statisticsbyAuthor")
  @Produces(MediaType.APPLICATION_JSON)
  public Response loadAuthorStats() throws FailMongoConnectionException {
    try {

      loadService.LoadStatisticsbyAuthor();
    } catch (Exception e) {
      log.error("Cannot load authors stats into Mongo DB", e);
      throw new FailMongoConnectionException(String.format("Cannot load authors by area into Mongo DB"), e);
    }
    return Response.ok().entity("Authors by area were successfully loaded into MongoDB").build();
  }

  @POST
  @Path("/translatebyPublication")
  @Produces(MediaType.APPLICATION_JSON)
  public Response loadTranslatebyPublication() throws FailMongoConnectionException {
    try {

      loadService.populatePublicationTranslations();
    } catch (Exception e) {
      log.error("Cannot load publications tranlation into Mongo DB", e);
      throw new FailMongoConnectionException(String.format("Cannot load publications tranlation Mongo DB"), e);
    }
    return Response.ok().entity("Publications tranlation were successfully loaded into MongoDB").build();
  }

}
