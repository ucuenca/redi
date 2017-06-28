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

import com.google.gson.JsonArray;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.apache.marmotta.ucuenca.wk.commons.service.TranslationService;
import org.apache.marmotta.ucuenca.wk.pubman.api.CommonService;
import org.slf4j.Logger;

@Path("/pubman")
@ApplicationScoped
public class PubWebService {

    @Inject
    private Logger log;

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
    public static final String LOAD_PUBLICATIONS = "/publications_provider_graph";
    public static final String LOAD_AUTHOR_ATTR = "/author_attr";
    public static final String GET_AUTHOR_DATA = "/pubsearch";
    public static final String GET_REPORT = "/report";
    public static final String TRANSLATE = "/translate";

    /*
     * Get Publications Data from Source and Load into Provider Graph
     */
    @POST
    @Path(GET_PUBLICATIONS)
    public Response readPublicationsPost(@QueryParam("Endpoint") String resultType) {
        String params = resultType;
        log.debug("Publications Task", params);
        return runPublicationsProviderTask(params);
    }

    /*
     * Get Publications Data from Source and Load into Provider Graph
     */
    @POST
    @Path(GET_PUBLICATIONS_GOOGLE)
    public Response readPublicationsPostGoogle(@QueryParam("update") Boolean update) {
        log.debug("Publications Task, update {}", update);
        String result = commonService.GetDataFromProvidersServiceGoogleScholar(update);
        return Response.ok().entity(result).build();
    }

    /*
     * Get Publications Data from Source and Load into Provider Graph
     */
    @POST
    @Path(GET_PUBLICATIONS_DBLP)
    public Response readPublicationsPostDBLP(@QueryParam("Endpoint") String resultType) {
        String params = resultType;
        log.debug("Publications Task", params);
        String result = commonService.GetDataFromProvidersServiceDBLP();
        return Response.ok().entity(result).build();
    }

    /*
     * Get Publications Data from Source and Load into Provider Graph
     */
    @POST
    @Path(GET_PUBLICATIONS_AK)
    public Response readPublicationsPostAK(@QueryParam("Endpoint") String resultType) {
        String params = resultType;
        log.debug("Publications Task", params);
        String result = commonService.GetDataFromProvidersServiceAcademicsKnowledge();
        return Response.ok().entity(result).build();
    }

    /*
     * Get Publications Data from Source and Load into Provider Graph
     */
    @POST
    @Path(GET_PUBLICATIONS_MA)
    public Response readPublicationsPostMA(@QueryParam("Endpoint") String resultType) {
        String params = resultType;
        log.debug("Publications Task", params);
        String result = commonService.GetDataFromProvidersServiceMicrosoftAcademics();
        return Response.ok().entity(result).build();
    }

    /*
     * Get Publications Data from Source and Load into Provider Graph
     */
    @POST
    @Path(GET_PUBLICATIONS_DSPACE)
    public Response readPublicationsPostDspace(@QueryParam("Endpoint") String resultType) {
        String params = resultType;
        log.debug("Publications Task", params);
        String result = commonService.GetDataFromProvidersServiceDspace();
        return Response.ok().entity(result).build();
    }

    private Response runPublicationsProviderTask(String urisString) {
        //String result = publicationsService.runPublicationsMAProviderTaskImpl(urisString);
        String result = runGetDataFromProvidersService();
        return Response.ok().entity(result).build();
    }

    private String runGetDataFromProvidersService() {
        return commonService.GetDataFromProvidersService();
    }

    /*
     * Get Publications Data from  Provider Graph and load into General Graph
     */
    @POST
    @Path(LOAD_PUBLICATIONS)
    public Response loadPublicationsPost(@QueryParam("Endpoint") String resultType, @Context HttpServletRequest request) {
        String params = resultType;
        log.debug("Publications Task", params);
        String result = commonService.Data2GlobalGraph();
        return Response.ok().entity(result).build();
    }

    /*
     * Get Publications Data from  Provider Graph and load into General Graph
     */
    @POST
    @Path(LOAD_AUTHOR_ATTR)
    public Response loadPublicationsPost() {
        return Response.ok(commonService.authorAttrFromProviders()).build();
    }

    /**
     * Service to get data related with especific author.
     *
     * @param uri //url to find
     */
    @POST
    @Path(GET_AUTHOR_DATA)
    @Produces("application/ld+json")
    public Response searchAuthor(@FormParam("resource") String uri, @Context HttpServletRequest request) {
        JsonArray resultjson = commonService.searchAuthor(uri);
        String result = resultjson.toString();
        return Response.ok().entity(result).build();
    }
    public static final String COUNT_PUBLICATIONS = "/count_publications_graph";

    /**
     * @Author Freddy Sumba. Service that count the publications in the provider
     * an central graph.
     * @param resultType
     * @param request
     * @return
     */
    @POST
    @Path(COUNT_PUBLICATIONS)
    public Response CountPublicationsPost(@QueryParam("Endpoint") String resultType, @Context HttpServletRequest request) {
        String params = resultType;
        log.debug("Publications Task Count", params);
        return runPublicationsCountTask(params);
    }

    private Response runPublicationsCountTask(String urisString) {
        String result = commonService.CountPublications();
        return Response.ok().entity(result).build();
    }

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
        ServletContext context = request.getServletContext();
        String realContextPath = context.getRealPath(request.getContextPath());
        log.debug("Report Task");
        String result = commonService.createReport(host, realContextPath, report, type, param1);
        return Response.ok().entity(result).build();
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

}
