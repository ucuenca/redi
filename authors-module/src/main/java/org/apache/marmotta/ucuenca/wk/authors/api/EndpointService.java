/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.authors.api;

import java.util.List;
import org.apache.marmotta.ucuenca.wk.authors.api.SparqlEndpoint;

/**
 *
 * @author Satellite
 */
public interface EndpointService {

    /**
     * Add Enpoint
     *
     * @param args http://localhost:8890/sparql
     * @return
     */
    String addEndpoint(String... args);

    SparqlEndpoint getEndpoint(String resourceId);

    /**
     *
     * @return
     */
    List<SparqlEndpoint> listEndpoints();

    String removeEndpoint(String resourceid);

    String updateEndpoint(String resourceid, String oldstatus, String newstatus);

}
