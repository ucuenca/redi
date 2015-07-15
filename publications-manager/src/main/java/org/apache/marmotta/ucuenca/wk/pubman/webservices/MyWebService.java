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

import com.google.common.io.CharStreams;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ucuenca.wk.endpoint.dblp.DBLPEndpoint;
import org.apache.marmotta.ucuenca.wk.pubman.api.MyService;
import org.apache.marmotta.ucuenca.wk.pubman.exceptions.DoThisException;
import org.apache.marmotta.ucuenca.wk.pubman.job.AuthorVersioningJob;

@Path("/pubman")
@ApplicationScoped
public class MyWebService {

    @Inject
    private Logger log;

    @Inject
    private MyService publicationsService;
    
    private static final int MAX_TURNS = 100;
    private static final int MIN_TURNS = 0;
    public static final String LOAD_PUBLICATIONS = "/publications";
    
    @POST
    @Path(LOAD_PUBLICATIONS)
    public Response addEndpointPost(@QueryParam("Endpoint") String resultType) {
            String params = resultType;
            log.debug("Publications Task", params);
            return runPublicationsTask(params);    
    }
    
     private Response runPublicationsTask(String urisString) {
              String result = publicationsService.runPublicationsTaskImpl(urisString);
                return Response.ok().entity(result).build();        

        
     }
    
    @GET
    @Produces("text/plain; charset=utf8")
    public Response hello(@QueryParam("name") String name) {
        if (StringUtils.isEmpty(name)) {
            log.warn("No name given");
            // No name given? Invalid request.
            return Response.status(Status.BAD_REQUEST).entity("Missing Parameter 'name'").build();
        }

        log.debug("Sending regards to {}", name);
     
        // Return the greeting.
        return Response.ok(publicationsService.helloWorld(name)).build();
    }
    
//    @GET
//    @Produces("text/turtle; charset=utf8")
//    public Response hola(@QueryParam("name") String name) {
//        if (StringUtils.isEmpty(name)) {
//            log.warn("No name given");
//            // No name given? Invalid request.
//            return Response.status(Status.BAD_REQUEST).entity("Missing Parameter 'name'").build();
//        }
//
//        log.debug("Sending regards to {}", name);
//        //new AuthorVersioningJob(log).proveSomething();
//        ClientConfiguration conf = new ClientConfiguration();
//        //conf.addEndpoint(new DBLPEndpoint());
//        LDClient ldClient = new LDClient(conf);
//        Model model = null;
//        try {
//        	//ClientResponse response = ldClient.retrieveResource("http://rdf.dblp.com/ns/m.0wqhskn");
//        	String NS_DBLP = "http://rdf.dblp.com/ns/search/";
//        	ClientResponse response = ldClient.retrieveResource(NS_DBLP + name);
//        	/*ClientResponse response = ldClient.retrieveResource(
//        			"http://dblp.uni-trier.de/search/author?xauthor=Saquicela+Victor");*/
//        	model = response.getData();
//        	log.info(model.toString());
//        	FileOutputStream out = new FileOutputStream("/root/test.ttl");
//        	RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
//        	try {
//        	  writer.startRDF();
//        	  for (Statement st: model) {
//        		 writer.handleStatement(st);
//        	  }
//        	  writer.endRDF();
//        	}
//        	catch (RDFHandlerException e) {
//        	 // oh no, do something!
//        	}
//        }catch(Exception e) {
//        	log.info(e.getMessage());
//        	
//        }
//        // Return the greeting.
//        return Response.ok(model, MediaType.TEXT_PLAIN_TYPE).build();
//    }

    @POST
    public Response doThis(@FormParam("turns") @DefaultValue("2") int turns) throws DoThisException {
        log.debug("Request to doThis {} times", turns);
        if (turns > MAX_TURNS) { throw new DoThisException("At max, 100 turns are allowed"); }
        if (turns < MIN_TURNS) { throw new DoThisException("Can't undo 'This'"); }

        publicationsService.doThis(turns);
        return Response.noContent().build();
    }

}
