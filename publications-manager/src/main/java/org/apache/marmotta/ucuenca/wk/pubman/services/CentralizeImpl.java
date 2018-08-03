/*
 * Copyright 2018 Xavier Sumba <xavier.sumba93@ucuenca.ec>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.Centralize;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
@RequestScoped
public class CentralizeImpl implements Centralize {

    @Inject
    private Logger log;

    @Inject
    private TaskManagerService taskService;

    @Inject
    private SesameService sesameService;

    @Inject
    private QueriesService queriesService;

    @Inject
    private ConfigurationService configurationService;

    @Override
    public void copy(String endpoint) throws Exception {
        Task t = null;
        List<String> graphNames = configurationService.getListConfiguration("publications.graph.copy");
        String name, url, sparql, context;
        int offset;
        RepositoryConnection con = null;
        try {
            con = sesameService.getConnection();
            // Get information of REDI Endpoint
            TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SPARQL,
                    queriesService.getREDIEndpoint(endpoint))
                    .evaluate();
            if (result.hasNext()) {
                BindingSet bs = result.next();
                name = bs.getBinding("name").getValue().stringValue();
                url = bs.getBinding("url").getValue().stringValue();
                sparql = bs.getBinding("sparql").getValue().stringValue();
                context = bs.getBinding("context").getValue().stringValue();
            } else {
                throw new Exception("Cannot get information of REDI Endpoint");
            }

            // Show progress
            t = taskService.createTask(endpoint, "Centralization");
            t.updateMessage(String.format("Centralizing %s node", name));
            t.updateDetailMessage("ID", endpoint);
            t.updateDetailMessage("Name", name);
            t.updateDetailMessage("URL", url);

            // Copy data
            for (String gname : graphNames) {
                int idOffset = (name + gname).hashCode();
                offset = getOffset(con, endpoint, gname, idOffset);

                if (offset == -2) {
                    log.info("{} graph was already copied.", gname);
                    continue;
                }

                String targetGraph = context + gname;
                String selectService = sparql + "select";

                t.updateDetailMessage("Processing graph", targetGraph);
                t.updateDetailMessage("Select service", selectService);
                t.updateDetailMessage("Offset", String.valueOf(offset));

                while (numberOfTriples(con, selectService, targetGraph, offset) > 0) {
                    String copyQuery = queriesService.getCopyDataQuery(selectService, offset, targetGraph, gname);
                    con.prepareUpdate(QueryLanguage.SPARQL, copyQuery).execute();
                    // update status
                    offset += QueriesService.LIMIT_TRIPLES_REDI_END;
                    updateOffset(con, idOffset, offset);
                    t.updateDetailMessage("Offset", String.valueOf(offset));

                    log.debug(copyQuery);
                }
                // When finishes copying graph
                updateOffset(con, idOffset, -2);
            }
        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            if (t != null) {
                taskService.endTask(t);
            }
            if (con != null) {
                try {
                    con.close();
                } catch (RepositoryException ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
        }
    }

    @Override
    public void resetCopy(String endpoint) throws Exception {
        List<String> graphNames = configurationService.getListConfiguration("publications.graph.copy");

        RepositoryConnection con = null;
        String name;
        try {
            con = sesameService.getConnection();

            // Get information of REDI Endpoint
            TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SPARQL,
                    queriesService.getREDIEndpoint(endpoint))
                    .evaluate();
            if (result.hasNext()) {
                BindingSet bs = result.next();
                name = bs.getBinding("name").getValue().stringValue();
            } else {
                throw new Exception("Cannot get information of REDI Endpoint");
            }

            for (String gname : graphNames) {
                int idOffset = (name + gname).hashCode();
                updateOffset(con, idOffset, -1);
            }
        } catch (RepositoryException | QueryEvaluationException | MalformedQueryException ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (RepositoryException ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
        }
    }

    private int numberOfTriples(RepositoryConnection con,
            String selectService, String targetGraph, int offset)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        TupleQueryResult r = con.prepareTupleQuery(QueryLanguage.SPARQL,
                queriesService.getNumberofTriplesREDIEndpoint(
                        selectService, offset, targetGraph
                )).evaluate();
        return Integer.parseInt(r.next().getBinding("total").getValue().stringValue());
    }

    private void updateOffset(RepositoryConnection con, int offsetId, int newOffset)
            throws UpdateExecutionException, RepositoryException, MalformedQueryException {
        String query = queriesService.getUpdateOffsetQuery(offsetId, newOffset);
        con.prepareUpdate(QueryLanguage.SPARQL, query).execute();
    }

    private int getOffset(RepositoryConnection con, String endpoint, String gname, int offsetId)
            throws QueryEvaluationException, RepositoryException,
            MalformedQueryException, UpdateExecutionException {
        String offseQuery = queriesService.getGraphOffset(offsetId);
        TupleQueryResult r = con.prepareTupleQuery(QueryLanguage.SPARQL, offseQuery)
                .evaluate();
        if (r.hasNext()) {
            int offset = Integer.parseInt(r.next().getBinding("val").getValue().stringValue());
            return offset == -1 ? 0 : offset;
        } else {
            String insert = queriesService.getInsertOffsetQuery(endpoint, offsetId, gname);
            con.prepareUpdate(QueryLanguage.SPARQL, insert).execute();
            return 0;
        }
    }

}
