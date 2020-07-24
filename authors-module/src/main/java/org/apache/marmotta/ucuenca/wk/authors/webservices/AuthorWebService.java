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
package org.apache.marmotta.ucuenca.wk.authors.webservices;

//import com.google.common.io.CharStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import java.util.logging.Level;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.ucuenca.wk.authors.api.AuthorService;

import org.apache.marmotta.ucuenca.wk.authors.api.EndpointsService;
import org.apache.marmotta.ucuenca.wk.authors.api.OrganizationService;

import org.apache.marmotta.ucuenca.wk.authors.api.UTPLAuthorService;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.DaoException;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.UpdateException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;

@ApplicationScoped
@Path("/authors-module")
public class AuthorWebService {

  @Inject
  private Logger log;

  @Inject
  private AuthorService authorService;

  @Inject
  private UTPLAuthorService utplAuthorService;

  @Inject
  private ConfigurationService configurationService;

  @Inject
  private OrganizationService organizationService;

  @Inject
  private EndpointsService endpointsService;

  public static final String ADD_ENDPOINT = "/addendpoint";
  public static final String AUTHOR_SPLIT = "/split";
  private static final int MAX_NUMBER_CSV_FIELDS = 5;
  private static final int MIN_NUMBER_CSV_FIELDS = 2;
  public static final String APPLICATIONJSON = "application/json";

  public boolean isOrcid(String t) {
    return t.length() > 0 ? t.matches("^\\s*(https?:\\/\\/)(orcid.org)\\/([0-9X]{4}-[0-9X]{4}-[0-9X]{4}-[0-9X]{4})\\s*$") : true;

  }

  public boolean isMail(String t) {
   // return t.length() > 0 ? t.matches("^[^@]+@[^@]+\\.[a-zA-Z]{2,}\\s*$") : true;
   return t.length() > 0 ? t.matches("^[^@]+@[^@]+$") : true;
   
  }

  public boolean isOther(String t) {
    return t.matches("^((?![0-9\\@]).)*$");
  }

  @POST
  @Path("/upload")
  @SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.AvoidDuplicateLiterals", "PMD.NPathComplexity"})
  public Response uploadAuthors(@HeaderParam(HttpHeaders.CONTENT_TYPE) String type, @Context HttpServletRequest request,
          @QueryParam("org") String organization, @QueryParam("type") String endpointType) throws IOException {
    if (type == null || !("text/csv".equals(type.toLowerCase()) || "application/vnd.ms-excel".equals(type.toLowerCase()))) {
      return Response.status(Status.BAD_REQUEST).entity("Incorrect file format.").build();
    }
    if (organization == null || organization.trim().length() <= 0) {
      return Response.status(Status.BAD_REQUEST).entity("There is not institution name.").build();
    }

    List<String> lines;
    try (InputStream in = request.getInputStream()) {
      lines = IOUtils.readLines(in, StandardCharsets.UTF_8);
      // Validate file structure.
      int count = 0;
      for (String line : lines) {
        String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (fields.length > MAX_NUMBER_CSV_FIELDS || fields.length < MIN_NUMBER_CSV_FIELDS) {
          return Response.status(Status.BAD_REQUEST).entity("File should have two to three fields.").build();
        }
        if (count > 0) {
          boolean validation = false;
          for (int i = 0; i < fields.length; i++) {
            switch (i) {
              case 2:
                validation = true;
                break;
              case 3:
                validation = isOrcid(fields[i]);
                break;
              case 4:
                validation = isMail(fields[i]);
                break;

              default:
                validation = isOther(fields[i]);
                break;
            }
            if (!validation) {
              return Response.status(Status.BAD_REQUEST).entity("Validation problems in field:" + (i + 1) + " Value:" + fields[i] + " \n Remember file schema for csv [first Name, last Name, keywords , orcid , mail ] ").build();
            }
          }
        }
        count++;

      }
    }
    // Store authors
    File f = new File(configurationService.getHome() + File.separator + "authors", organization + ".csv");
    if (!f.exists()) {
      f.getParentFile().mkdirs();
      f.createNewFile();
    }
    try (FileOutputStream fos = new FileOutputStream(f)) {
      IOUtils.writeLines(lines, null, fos, StandardCharsets.UTF_8);
      log.info("File imported to {}.", f.getPath());
      String resultado = endpointsService.registerFile(endpointType, organization, f.getPath());

      //String resultado = endpointService.addEndpoint("true", organization, f.getPath(), "-", "-", "-", "-", "-", "-", "-");
      //authorService.extractFile (organization, f.getPath());
      log.info(resultado);
    }
    return Response.ok().entity("Import Successfully.").build();
  }

  @POST
  @Path("/organization/loadOrg")
  @SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.AvoidDuplicateLiterals"})
  public Response loadOrg(@QueryParam("uri") String resourceid) {

    String result = organizationService.loadOrgbyURI(resourceid);
    return Response.ok().entity(result).build();
  }

  @POST
  @Path("/organization/removeOrg")
  public Response removeOrg(@QueryParam("uri") String resourceid) {

    String result = organizationService.removeOrgbyURI(resourceid);
    return Response.ok().entity(result).build();
  }

  @POST
  @Path("endpoints/removeEnd")
  @SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.AvoidDuplicateLiterals"})
  public Response removeEnd(@QueryParam("uri") String resourceid) {
    String result = endpointsService.deleteEndpoint(resourceid);
    endpointsService.updateStatus(result, result);
    return Response.ok().entity(result).build();
  }

  @POST
  @Path("endpoints/updateStatusEnd")
  @SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.AvoidDuplicateLiterals"})
  public Response removeEnd(@QueryParam("uri") String resourceid, @QueryParam("status") String status) {
    String result = endpointsService.updateStatus(resourceid, status);
    return Response.ok().entity(result).build();
  }

  @GET
  @Path("/organization/list")
  @Produces(APPLICATIONJSON)
  public Response listOrganization() {

    String result;
    result = organizationService.listOrganization();
    return Response.ok().entity(result).build();

  }

  @GET
  @Path("/endpoints/list")
  @Produces(APPLICATIONJSON)
  public Response listEndpointsAvalaible() {

    String result;
    result = endpointsService.listEndpoints();
    return Response.ok().entity(result).build();

  }

  /*       
    @POST
    @Path("/update")
    public Response updateAuthorPost() throws UpdateException, DaoException {
      //  String result = authorService.extractAuthors();
        return Response.ok().entity(result).build();
    }*/
  @POST
  @Path("/orgRegister")
  @SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.AvoidDuplicateLiterals"})
  public Response orgRegister(@QueryParam("acro") String acro, @QueryParam("namEn") String namEn, @QueryParam("namEs") String namEs, @QueryParam("alias") String alias, @QueryParam("scopusId") String scopusId , @QueryParam("coun") String coun, @QueryParam("prov") String prov, @QueryParam("city") String city, @QueryParam("lan") String lan, @QueryParam("long") String lon, @QueryParam("type") String type) throws UpdateException, DaoException {
    // String resultado = endpointService.addEndpoint("true", name, endpoint, "-", "-", "-", "-", "-", "-", "-");
    // log.info(resultado);
    // String result = authorService.extractOAI(name, endpoint);
    String result = organizationService.addOrganization(acro, namEn, namEs, alias, scopusId , coun, prov, city, lan, lon, type);
    return Response.ok().entity(result).build();

  }

  @POST
  @Path("/orgEdit")
  public Response orgEdit(@QueryParam("acro") String acro, @QueryParam("namEn") String namEn, @QueryParam("namEs") String namEs, @QueryParam("alias") String alias, @QueryParam("scopusId") String scopusId, @QueryParam("coun") String coun, @QueryParam("prov") String prov, @QueryParam("city") String city, @QueryParam("lan") String lan, @QueryParam("long") String lon, @QueryParam("type") String type) throws UpdateException, DaoException {
    // String resultado = endpointService.addEndpoint("true", name, endpoint, "-", "-", "-", "-", "-", "-", "-");
    // log.info(resultado);
    // String result = authorService.extractOAI(name, endpoint);
    String result = organizationService.editOrg(acro, namEn, namEs, alias,  scopusId ,coun, prov, city, lan, lon, type);
    if ("Success".equals(result)) {
      return Response.ok().entity(result).build();
    } else {
      return Response.serverError().build();
    }
  }

  @POST
  @Path("/endpointSparqlRegister")
  public Response endpointSparqlRegister(@QueryParam("type") String type, @QueryParam("org") String org, @QueryParam("url") String url, @QueryParam("graph") String graph) {
    //String result = organizationService.editOrg( acro, namEn, namEs, coun, prov, city, lan, lon, type);
    String result = endpointsService.registerSPARQL(type, org, url, graph);
    return Response.ok().entity(result).build();
    //return  Response.status(Status.BAD_REQUEST).entity("Incorrect file format.").build();
  }

  @POST
  @Path("/endpointORCIDRegister")
  public Response endpointORCIDRegister(@QueryParam("type") String type, @QueryParam("org") String org) {
    String result = endpointsService.registerORCID(type, org);
    return Response.ok().entity(result).build();
  }
  
  @POST
  @Path("/endpointVIVORegister")
  public Response endpointVIVORegister(@QueryParam("type") String type, @QueryParam("org") String org, @QueryParam("url") String u) {
    String result = endpointsService.registerVIVO(type, org, u);
    return Response.ok().entity(result).build();
  }

  @POST
  @Path("/endpointOAIRegister")
  public Response endpointOAIRegister(@QueryParam("type") String type, @QueryParam("org") String org, @QueryParam("url") String url, @QueryParam("severe") Boolean severemode) {
    //String result = organizationService.editOrg( acro, namEn, namEs, coun, prov, city, lan, lon, type);
    String result = endpointsService.registerOAI(type, org, url, severemode , false );
    return Response.ok().entity(result).build();
    //return  Response.status(Status.BAD_REQUEST).entity("Incorrect file format.").build();
  }

  @POST
  @Path("/endpoints/extractAuthors")
  @Produces(APPLICATIONJSON)
  public Response extractAuthors(@Context HttpServletRequest request) {
    String scheme = request.getScheme();
    String serverName = request.getServerName();
    int serverPort = request.getServerPort();
    String localName = scheme + "://" + serverName + ":" + serverPort + "/";
    String[] get = request.getParameterMap().get("data[]");
    String output = authorService.extractAuthorsGeneric(localName, get);
    authorService.postProcessAffiliations(get);
    return Response.ok().entity(output).build();
    //return  Response.status(Status.BAD_REQUEST).entity("Incorrect file format.").build();
  }

  @POST
  @Path(AUTHOR_SPLIT)
  public Response split(@QueryParam("endpointuri") String endpointuri, @QueryParam("graphuri") String graphuri) {

    try {
      String endpoint = endpointuri;
      String graph = graphuri;
      return authorSplit(endpoint, graph);
    } catch (UpdateException ex) {
      java.util.logging.Logger.getLogger(AuthorWebService.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;

  }

  /**
   * AUTHOR UPDATE IMPLEMENTATION
   *
   * @param urisString // JSON contains Endpoint and GraphURI
   *
   */
  private Response authorSplit(String endpoint, String graph) throws UpdateException {
    if (StringUtils.isBlank(endpoint)) {
      return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Required Endpoint and GraphURI").build();
    } else {

      try {
        String result = utplAuthorService.runAuthorsSplit(endpoint, graph);
        return Response.ok().entity(result).build();
      } catch (DaoException | RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
        java.util.logging.Logger.getLogger(AuthorWebService.class.getName()).log(Level.SEVERE, null, ex);
        log.error("Error: Getting Sources List");
      }

    }
    return null;
  }

}
