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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import org.apache.marmotta.ucuenca.wk.pubman.api.CommonService;



@Path("/pubman")
@ApplicationScoped
public class PubWebService {

    @Inject
    private Logger log;

    @Inject 
    private CommonService commonService;
    
    private static final int MAX_TURNS = 100;
    private static final int MIN_TURNS = 0;
    public static final String GET_PUBLICATIONS = "/publications";
    public static final String LOAD_PUBLICATIONS = "/publications_provider_graph";
    public static final String GET_AUTHOR_DATA = "/pubsearch";

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

    private Response runPublicationsProviderTask(String urisString) {
        //String result = publicationsService.runPublicationsMAProviderTaskImpl(urisString);
        String result = runGetDataFromProvidersService();
        return Response.ok().entity(result).build();
    }
    
    private String runGetDataFromProvidersService()
    {
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
        return runPublicationsTask(params);
    }

    private Response runPublicationsTask(String urisString) {
        String result = commonService.Data2GlobalGraph();
        return Response.ok().entity(result).build();
    }
    
    /**
     * Service to get data related with especific author.
     * 
     * @param uri   //url to find
     */
    @POST
    @Path(GET_AUTHOR_DATA)
    @Produces("application/ld+json")
    public Response searchAuthor(@FormParam("resource") String uri, @Context HttpServletRequest request){
        JsonArray resultjson = commonService.searchAuthor(uri);       
        String result = resultjson.toString();
        return Response.ok().entity(result).build(); 
    }
}
