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
package org.apache.marmotta.ucuenca.wk.pubman.webservices;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.marmotta.ucuenca.wk.commons.service.TranslationService;
import org.apache.marmotta.ucuenca.wk.pubman.api.CommonService;
import org.apache.marmotta.ucuenca.wk.pubman.api.SyncGraphDBMarmotta;
import org.slf4j.Logger;

@Path("/pubman")
@ApplicationScoped
public class PubWebService {

  @Inject
  private Logger log;

  @Inject
  private SyncGraphDBMarmotta synprocess;

  @Inject
  private CommonService commonService;

  @Inject
  private TranslationService traslateService;

  private static final int MAX_TURNS = 100;
  private static final int MIN_TURNS = 0;
  public static final String GET_PUBLICATIONS = "/publications";
  public static final String GET_PUBLICATIONS_GOOGLE = "/publications_google";
  public static final String GET_PUBLICATIONS_DBLP = "/publications_dblp";
  public static final String GET_PUBLICATIONS_MA = "/publications_ma";
  public static final String GET_PUBLICATIONS_AK = "/publications_ak";
  public static final String GET_PUBLICATIONS_DSPACE = "/publications_dspace";
  public static final String DETECT_LATINDEX_PUBLICATIONS = "/publications_latindex";
  public static final String DISAMBIGUATION_PUBLICATIONS = "/publications_disambiguation";
  public static final String SYNC_PUBLICATIONS = "/publications_sync";
  public static final String CENTRAL_GRAPH_PUBLICATIONS = "/publications_centralgraph";
  public static final String LOAD_PUBLICATIONS = "/publications_provider_graph";
  public static final String LOAD_AUTHOR_ATTR = "/author_attr";
  public static final String GET_AUTHOR_DATA = "/pubsearch";
  public static final String GET_REPORT = "/report";
  public static final String GET_REPORT_DOWNLOAD = "/reportDownload";
  public static final String TRANSLATE = "/translate";
  public static final String INDEX_CENTRAL_GRAPH = "/indexing";
  public static final String GET_SEARCH_QUERY = "/searchQuery";
  public static final String APPLICATIONJSON = "application/json";
  public static final String COLABORATORDATA = "reports/collaboratorsData";

  public static final String SUBCL = "/reports/subclusterData";

  public static final String CL = "/reports/clusterData";

  /*
     * Get Publications Data from Source and Load into Provider Graph
   */
  @POST
  @Path(GET_PUBLICATIONS)
  public Response readPublicationsPost(@QueryParam("update") Boolean update) {
    String[] organizations = {"http://redi.cedia.edu.ec/resource/organization/UCUENCA"};
    String result = commonService.getDataFromScopusProvidersService(organizations, false);
    return Response.ok().entity(result).build();
  }

  @POST
  @Path("/publicationsScopusByOrg")
  public Response readPublicationsPostScopus(@Context HttpServletRequest request) {

    String[] org = request.getParameterMap().get("data[]");
    boolean force = request.getParameterMap().get("force")[0].compareTo("true") == 0;
    String result = commonService.getDataFromScopusProvidersService(org, force);
    //  String output = authorService.extractAuthorsGeneric(get);

    return Response.ok().entity(result).build();
    //return  Response.status(Status.BAD_REQUEST).entity("Incorrect file format.").build();
  }
  
  @POST
  @Path("/publicationsScopusUpdateByOrg")
  public Response readPublicationsPostScopusUpdate(@Context HttpServletRequest request) {

    String[] org = request.getParameterMap().get("data[]");
    boolean force = request.getParameterMap().get("force")[0].compareTo("true") == 0;
    String result = commonService.getDataFromScopusUpdateProvidersService(org, force);
    //  String output = authorService.extractAuthorsGeneric(get);

    return Response.ok().entity(result).build();
    //return  Response.status(Status.BAD_REQUEST).entity("Incorrect file format.").build();
  }
  

  @POST
  @Path("/central/store")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response storeCentralNodes(String data) {
    String[] pairs = data.split("&");
    String name = pairs[0].substring(pairs[0].indexOf("=") + 1);
    String url_ = pairs[1].substring(pairs[1].indexOf("=") + 1);
    URL url;
    try {
      name = URLDecoder.decode(name, "utf8");

      url_ = URLDecoder.decode(url_, "utf8");
      url_ = url_.endsWith("/") ? url_ : url_ + "/";
      url = new URL(url_);
    } catch (MalformedURLException | UnsupportedEncodingException ex) {
      log.error(ex.getMessage(), ex);
      return Response.status(Response.Status.BAD_REQUEST).entity("Insert a correct URL.").build();
    }
    try {
      commonService.registerREDIEndpoint(name, url);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    }
    return Response.ok().entity("REDI Endpoint was successfully registered.").build();
  }

  @POST
  @Path("/central/delete")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response deleteCentralNode(String id) throws UnsupportedEncodingException {
    String uri = URLDecoder.decode(id.substring(id.indexOf("=") + 1), "utf8");
    log.debug("Deleting endpoint REDI {}", uri);
    if (commonService.deleteREDIEndpoint(uri)) {
      return Response.ok()
              .entity("REDI Endpoint was successfully deleted.")
              .build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity("REDI Endpoint couldn't be deleted.")
            .build();
  }

  @POST
  @Path("/central/centralize")
  @Produces(MediaType.APPLICATION_JSON)
  public Response centralize(@Context HttpServletRequest request, @QueryParam("update") boolean update) {
    String[] endpoints = request.getParameterMap().get("data[]");
    commonService.centralize(endpoints, update);
    return Response.ok().entity("OK").build();
  }

  @GET
  @Path("central/list")
  @Produces(MediaType.APPLICATION_JSON)
  public Response listREDIEndpoints() {
    return Response.ok().entity(commonService.listREDIEndpoints()).build();
  }

  @POST
  @Path("/publicationsDBLPByOrg")
  public Response readPublicationsPostDBLP(@Context HttpServletRequest request) {

    String[] org = request.getParameterMap().get("data[]");
    boolean force = request.getParameterMap().get("force")[0].compareTo("true") == 0;
    String result = commonService.getDataFromDBLPProvidersService(org, force);

    return Response.ok().entity(result).build();
  }

  @POST
  @Path("/publicationsScieloByOrg")
  public Response readPublicationsPostScielo(@Context HttpServletRequest request) {

    String[] org = request.getParameterMap().get("data[]");
    boolean force = request.getParameterMap().get("force")[0].compareTo("true") == 0;
    String result = commonService.getDataFromScieloProvidersService(org, force);

    return Response.ok().entity(result).build();
  }
  
  @POST
  @Path("/publicationsCrossrefByOrg")
  public Response readPublicationsPostCrossref(@Context HttpServletRequest request) {

    String[] org = request.getParameterMap().get("data[]");
    boolean force = request.getParameterMap().get("force")[0].compareTo("true") == 0;
    String result = commonService.getDataFromCrossrefProvidersService(org, force);

    return Response.ok().entity(result).build();
  }
  

  @POST
  @Path("/publicationsDOAJByOrg")
  public Response readPublicationsPostDOAJ(@Context HttpServletRequest request) {

    String[] org = request.getParameterMap().get("data[]");
    boolean force = request.getParameterMap().get("force")[0].compareTo("true") == 0;
    String result = commonService.getDataFromDOAJProvidersService(org, force);

    return Response.ok().entity(result).build();
  }

  @POST
  @Path("/publicationsORCIDByOrg")
  public Response readPublicationsPostORCID(@Context HttpServletRequest request) {

    String[] org = request.getParameterMap().get("data[]");
    boolean force = request.getParameterMap().get("force")[0].compareTo("true") == 0;
    String result = commonService.getDataFromORCIDProvidersService(org, force);

    return Response.ok().entity(result).build();
  }

  @POST
  @Path("/publicationsGSchoolarByOrg")
  public Response readPublicationsPostGSchoolar(@Context HttpServletRequest request) {
    String[] org = request.getParameterMap().get("data[]");
    boolean force = request.getParameterMap().get("force")[0].compareTo("true") == 0;
    String result = commonService.getDataFromGoogleScholarProvidersService(org, force);

    return Response.ok().entity(result).build();
  }

  @POST
  @Path("/publicationsSpringerByOrg")
  public Response readPublicationsPostSpringer(@Context HttpServletRequest request) {
    String[] org = request.getParameterMap().get("data[]");
    boolean force = request.getParameterMap().get("force")[0].compareTo("true") == 0;
    String result = commonService.getDataFromSpringerProvidersService(org, force);

    return Response.ok().entity(result).build();
  }

  @POST
  @Path("/publicationsAkByOrg")
  //  @Produces(APPLICATIONJSON)
  public Response readPublicationsPostAK(@Context HttpServletRequest request) {

    String[] org = request.getParameterMap().get("data[]");
    boolean force = request.getParameterMap().get("force")[0].compareTo("true") == 0;
    String result = commonService.getDataFromAcademicsKnowledgeProvidersService(org, force);
    //  String output = authorService.extractAuthorsGeneric(get);

    return Response.ok().entity(result).build();
    //return  Response.status(Status.BAD_REQUEST).entity("Incorrect file format.").build();
  }

//    /*
//     * Get Publications Data from Source and Load into Provider Graph
//     */
//    @POST
//    @Path(GET_PUBLICATIONS_GOOGLE)
//    public Response readPublicationsPostGoogle(@QueryParam("update") Boolean update) {
//        log.debug("Publications Task, update {}", update);
//        String result = commonService.getDataFromGoogleScholarProvidersService(update);
//        return Response.ok().entity(result).build();
//    }

  /*
     * Get Publications Data from Source and Load into Provider Graph
   */
  @POST
  @Path(GET_PUBLICATIONS_DBLP)
  public Response readPublicationsPostDBLP(@QueryParam("Endpoint") String resultType) {
    String[] organizations = {"http://redi.cedia.edu.ec/resource/organization/UCUENCA"};
    String params = resultType;
    log.debug("Publications Task", params);
    String result = commonService.getDataFromDBLPProvidersService(organizations, false);
    return Response.ok().entity(result).build();
  }

  /*
     * Get Publications Data from Source and Load into Provider Graph
   */
  @POST
  @Path(GET_PUBLICATIONS_AK)
  public Response readPublicationsPostAK(@QueryParam("Endpoint") String resultType) {
    String[] organizations = {"http://redi.cedia.edu.ec/resource/organization/UCUENCA"};
    String params = resultType;
    log.debug("Publications Task", params);
    String result = commonService.getDataFromAcademicsKnowledgeProvidersService(organizations, false);
    return Response.ok().entity(result).build();
  }

  @GET
  @Path("publication/organization/list")
  @Produces(APPLICATIONJSON)
  public Response listExtractedOrganization() {

    String result = commonService.organizationListExtracted();
    //  result = organizationService.listOrganization();
    return Response.ok().entity(result).build();

  }

  @GET
  @Path(COLABORATORDATA)
  @Produces(APPLICATIONJSON)
  public Response getCollaboratorsData(@QueryParam("URI") String uri) {

    String result = commonService.getCollaboratorsData(uri);
    //  result = organizationService.listOrganization();
    return Response.ok().entity(result).build();

  }

  @GET
  @Path(CL)
  @Produces(APPLICATIONJSON)
  public Response getClusterGraph(@QueryParam("cluster") String cl) {

    String result = commonService.getClusterGraph(cl);
    //  result = organizationService.listOrganization(); 
    return Response.ok().entity(result).build();

  }

  @GET
  @Path(SUBCL)
  @Produces(APPLICATIONJSON)
  public Response getSubClusterGraph(@QueryParam("cluster") String cl, @QueryParam("subcluster") String subcl) {

    String result = commonService.getsubClusterGraph(cl, subcl);
    //  result = organizationService.listOrganization(); 
    return Response.ok().entity(result).build();

  }

  @GET
  @Path("publication/organization/disambiguationList")
  @Produces(APPLICATIONJSON)
  public Response listEnrichmentOrganization() {

    String result = commonService.organizationListEnrichment();
    //  result = organizationService.listOrganization();
    return Response.ok().entity(result).build();

  }

  /*
     * Detect Latindex Journals
   */
  @POST
  @Path(DETECT_LATINDEX_PUBLICATIONS)
  public Response detectPostLatindex(@QueryParam("Endpoint") String resultType) {
    String params = resultType;
    log.debug("Publications Task", params);
    String result = commonService.DetectLatindexPublications();
    return Response.ok().entity(result).build();
  }

//    /**
//     * Service to get data related with especific author.
//     *
//     * @param uri //url to find
//     */
//    @POST
//    @Path(GET_AUTHOR_DATA)
//    @Produces("application/ld+json")
//    public Response searchAuthor(@FormParam("resource") String uri, @Context HttpServletRequest request) {
//        JsonArray resultjson = commonService.searchAuthor(uri);
//        String result = resultjson.toString();
//        return Response.ok().entity(result).build();
//    }
  /**
   * @Author Jose Luis Cullcay. Service used to create reports
   * @param report Name of the report
   * @param param Type of the report
   * @param param1 Parameter
   * @param request
   * @return Address to the new report created
   */
  @POST
  @Path(GET_REPORT)
  public Response createReport(@FormParam("hostname") String host, @FormParam("report") String report, @FormParam("type") String type, @FormParam("param1") List<String> param1, @Context HttpServletRequest request) {
    if (!type.equals("pdf") && !type.equals("xls")) {
      return Response.ok("Invalid format").build();
    }

    ServletContext context = request.getServletContext();
    String realContextPath = context.getRealPath("");
    log.info("Reports Path");
    log.info(request.getContextPath());
    log.info(realContextPath);
    log.debug("Report Task");
    String result = commonService.createReport(host, realContextPath, report, type, param1);
    return Response.ok().entity(result).build();
  }

  @GET
  @Path(GET_REPORT)
  public Response createReportGet(@QueryParam("report") String report, @QueryParam("type") String type, @QueryParam("param1") List<String> param1, @Context HttpServletRequest request) {
    if (!type.equals("pdf") && !type.equals("xls")) {
      return Response.ok("Invalid format").build();
    }
    String scheme = request.getScheme();
    String serverName = request.getServerName();
    int serverPort = request.getServerPort();
    String localName = scheme + "://" + serverName + ":" + serverPort + "/";
    ServletContext context = request.getServletContext();
    String realContextPath = context.getRealPath("");
    log.info("Reports Path");
    log.info(request.getContextPath());
    log.info(realContextPath);
    log.debug("Report Task");
    String result = commonService.createReport(localName, realContextPath, report, type, param1);
    return Response.ok().entity(result).build();
  }

  /**
   * @Author José Ortiz. Service used to create download reports
   * @param file Name of the report
   * @return Data Stream
   */
  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path(GET_REPORT_DOWNLOAD)
  public Response DownloadReport(@QueryParam("file") String report) {

    if (!report.matches("[a-fA-F0-9]{32}\\.(pdf|xls)")) {
      return Response.ok("Invalid File").build();
    }

    File file = new File("/tmp/redi_reports/redi_reports_" + report);
    ResponseBuilder response = Response.ok((Object) file);
    response.header("Content-Disposition", "attachment; filename=Report_" + report);
    return response.build();
  }

  /**
   *
   */
  @POST
  @Path(TRANSLATE)
  @Produces("application/ld+json")
  public Response translate(@QueryParam("totranslate") String totranslate) {
    String result = traslateService.translate(totranslate).toString();
    return Response.ok().entity(result).build();
  }

  @POST
  @Path(GET_SEARCH_QUERY)
  public Response createSearchQuery(@FormParam("textSearch") String textSearch, @Context HttpServletRequest request) {
    ServletContext context = request.getServletContext();
    String realContextPath = context.getRealPath(request.getContextPath());
    log.debug("Report Task");
    String result = commonService.getSearchQuery(textSearch);
    return Response.ok().entity(result).build();
  }

  @POST
  @Path(DISAMBIGUATION_PUBLICATIONS)
  public Response disambiguation(@QueryParam("Endpoint") String resultType) {
    String params = resultType;
    log.debug("Publications Task", params);
    String result = commonService.runDisambiguationProcess();
    return Response.ok().entity(result).build();
  }

  @POST
  @Path(SYNC_PUBLICATIONS)
  public Response sync(@QueryParam("Endpoint") String resultType) {
    //String params = resultType;
    log.debug("SYNC Task");
    synprocess.init();
    return Response.ok().entity("Synchronizing").build();
  }

  @POST
  @Path("/runDisambiguation")
  public Response runDisambiguation(@Context HttpServletRequest request) {

    String[] org = request.getParameterMap().get("data[]");
    String result = commonService.runDisambiguationProcess(org);

    return Response.ok().entity(result).build();
  }

  @POST
  @Path(CENTRAL_GRAPH_PUBLICATIONS)
  public Response centralGraph(@QueryParam("Endpoint") String resultType) {
    String params = resultType;
    log.debug("Publications Task", params);
    String result = commonService.CentralGraphProcess();
    return Response.ok().entity(result).build();
  }

}
