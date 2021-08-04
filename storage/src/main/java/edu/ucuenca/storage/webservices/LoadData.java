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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
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
  private ConfigurationService configurationService;
  @Inject
  private Logger log;

  @POST
  @Path("/uploadBanner")
  @SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.AvoidDuplicateLiterals", "PMD.NPathComplexity"})
  public Response uploadBanner(
          @HeaderParam(HttpHeaders.CONTENT_TYPE) String type,
          @Context HttpServletRequest request,
          @QueryParam("id") String id
  ) throws IOException {
    if (type == null || !("image/png".equals(type.toLowerCase()))) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Incorrect file format.").build();
    }
    if (id == null || id.trim().length() <= 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity("There is not orcid.").build();
    }

    File f = new File(configurationService.getHome() + File.separator + "banner_photo", id.hashCode() + ".png");
    if (f.exists()) {
      f.delete();
    }
    if (!f.exists()) {
      f.getParentFile().mkdirs();
      f.createNewFile();
    }
    try (FileOutputStream fos = new FileOutputStream(f)) {
      IOUtils.copy(request.getInputStream(), fos);
    }

    return Response.ok().entity(id.hashCode() + ".png").build();
  }

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
  @Path("/projects")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getProjects() throws FailMongoConnectionException {
    try {
      loadService.ProjectProfile();
      loadService.instbyProj();
    } catch (Exception e) {
      log.error("Cannot load projects into Mongo DB", e);
      throw new FailMongoConnectionException(String.format("Cannot load projects into Mongo DB"), e);
    }
    return Response.ok().entity("Projects loaded successfully").build();
  }
  
  
   @POST
  @Path("/patents")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPatent () throws FailMongoConnectionException {
    try {
      loadService.PatentProfile();

    } catch (Exception e) {
      log.error("Cannot load patents into Mongo DB", e);
      throw new FailMongoConnectionException(String.format("Cannot load patents into Mongo DB"), e);
    }
    return Response.ok().entity("Patents loaded successfully").build();
  }
  
  
  @POST
  @Path("/instbyProj")
  @Produces(MediaType.APPLICATION_JSON)
  public Response instbyproj() throws FailMongoConnectionException {
    try {
      loadService.instbyProj();
    } catch (Exception e) {
      log.error("Cannot load statistics project by Institutions into Mongo DB", e);
      throw new FailMongoConnectionException(String.format("Cannot load project by Institutions into Mongo DB"), e);
    }
    return Response.ok().entity("Projects by Institutions loaded successfully").build();
  }
  
  
  @POST
  @Path("/areaStats")
  @Produces(MediaType.APPLICATION_JSON)
  public Response areastats() throws FailMongoConnectionException {
    try {
      loadService.areasbydocument();
    } catch (Exception e) {
      log.error("Cannot load statistics areas  into Mongo DB", e);
      throw new FailMongoConnectionException(String.format("Cannot load area stats into Mongo DB"), e);
    }
    return Response.ok().entity("areas stats loaded successfully").build();
  }
  
  @POST
  @Path("/pub-by-area")
  @Produces(MediaType.APPLICATION_JSON)
  public Response docAreas() throws FailMongoConnectionException {
    try {
      loadService.getPublicationDatesbyAreas ();
    } catch (Exception e) {
      log.error("Cannot load statistics areas  into Mongo DB", e);
      throw new FailMongoConnectionException(String.format("Cannot load area stats into Mongo DB"), e);
    }
    return Response.ok().entity("areas publication stats loaded successfully").build();
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
  @Path("/indicadorPub")
  @Produces(MediaType.APPLICATION_JSON)
  public Response indicadorPub() throws FailMongoConnectionException {
    try {

      String s = loadService.indicadorGeneralPub();
    } catch (Exception e) {
      log.error("Cannot load publication indicators stats into Mongo DB", e);
      throw new FailMongoConnectionException(String.format("Cannot load apublication indicators into Mongo DB"), e);
    }
    return Response.ok().entity("Indicators by publications were successfully loaded into MongoDB").build();
  }
  
  @POST
  @Path("/translatebyPublication")
  @Produces(MediaType.APPLICATION_JSON)
  public Response loadTranslatebyPublication() throws FailMongoConnectionException {
    try {
//      loadService.populatePublicationKeywords();
      loadService.populatePublicationTranslations();
    } catch (Exception e) {
      log.error("Cannot load publications tranlation into Mongo DB", e);
      throw new FailMongoConnectionException(String.format("Cannot load publications tranlation Mongo DB"), e);
    }
    return Response.ok().entity("Publications tranlation were successfully loaded into MongoDB").build();
  }
  
  

  @POST
  @Path("/translatePublications")
  @Produces(MediaType.APPLICATION_JSON)
  public Response googleTranslatePub () throws FailMongoConnectionException {
    String response = "-";
    try {
//      loadService.populatePublicationKeywords();
       response = loadService.googlePublicationTranslation();
     
    } catch (Exception e) {
      log.error("Cannot translate publications and insert into Mongo DB", e);
      throw new FailMongoConnectionException(String.format("Cannot translate publications Mongo DB"), e);
    }
    return Response.ok().entity("Result:"+response).build();
  }

  @POST
  @Path("/applyChanges")
  @Produces(MediaType.APPLICATION_JSON)
  public Response loadApplyChanges() throws FailMongoConnectionException {
    try {
      loadService.populateProfileChanges();
    } catch (Exception e) {
      log.error("Cannot load authors changes into Mongo DB", e);
      throw new FailMongoConnectionException(String.format("Cannot load authors changes into Mongo DB"), e);
    }
    return Response.ok().entity("Authors profile changes were successfully loaded into MongoDB").build();
  }

}
