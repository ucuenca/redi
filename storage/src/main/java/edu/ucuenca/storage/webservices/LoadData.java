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
            loadService.authors();
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
        } catch (Exception e) {
            log.error("Cannot load statistics into Mongo DB", e);
            throw new FailMongoConnectionException(String.format("Cannot load statistics into Mongo DB"), e);
        }
        return Response.ok().entity("Statistics load successfully").build();
    }

}
