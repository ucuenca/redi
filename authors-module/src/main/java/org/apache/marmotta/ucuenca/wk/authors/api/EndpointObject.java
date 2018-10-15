/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.authors.api;

import java.util.List;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.util.CDIContext;

/**
 *
 * @author joe
 */
public abstract class EndpointObject implements Comparable<EndpointObject> {

    private ConfigurationService configurationService;
    /**
     * constant indicating high priority of this endpoint definition
     */
    public final static int PRIORITY_HIGH = 3;

    /**
     * constant indicating medium priority of this endpoint definition
     */
    public final static int PRIORITY_MEDIUM = 2;

    /**
     * constant indicating low priority of this endpoint definition
     */
    public final static int PRIORITY_LOW = 1;

    /**
     * The priority of this endpoint configuration; endpoints with higher
     * priority take precedence over endpoints with lower priority in case both
     * would be applicable.
     */
    private int priority = PRIORITY_MEDIUM;

    /**
     * A state enable to work ( true || false ) for this endpoint.
     */
    private String status;

    /**
     * A human-readable name for this endpoint.
     */
    /**
     * The HTTP URL to access the endpoint. Examples: http://dbpedia.org/sparql
     */
    private String access;

    private String resourceId;

    private String type;

    private String graph;

    private String org;

    public EndpointObject(String status, String org, String access, String type, String graph, String resourceId) {
        this.status = status;
        this.org = org;
        this.access = access;
        this.resourceId = resourceId;
        this.type = type;
        this.graph = graph;
        
         configurationService = CDIContext.getInstance(ConfigurationService.class);
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getGraph() {
        return graph;
    }

    public void setGraph(String graph) {
        this.graph = graph;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOrgName() {
        String name = this.getOrg();
        return name.substring(name.lastIndexOf('/') + 1);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        String name = this.getOrg();
        return name.substring(name.lastIndexOf('/') + 1);
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    /**
     * A URI of a endpoint resource combine with HASH CODE of (name+url+graph)
     * this is using for find an author ( example: Delete Case )
     */
    public String getPluginsPath() {

        return configurationService.getStringConfiguration("extraction.pentahoPlugins.path");

    }

    public String getCsvTransfPath() {

        return configurationService.getStringConfiguration("extraction.transformation.file");
    }

    public String getOAITransfPath() {

        return configurationService.getStringConfiguration("extraction.transformation.oai");
    }
    
     public String getOJSTransfPath() {

        return configurationService.getStringConfiguration("extraction.transformation.ojs");
    }

    public String getOutputFilesPath() {

        return configurationService.getStringConfiguration("extraction.transformation.outputFolder");
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    //public abstract Boolean  extractData ();
    // public abstract Repository  CreateRepo (String Path);
    public abstract Boolean prepareQuery();

    public abstract List querySource(String query);

    public abstract Boolean closeconnection();

    @Override
    public int compareTo(EndpointObject o) {
        if (getPriority() > o.getPriority()) {
            return -1;
        } else if (getPriority() < o.getPriority()) {
            return 1;
        } else {
            return 0;
        }
    }

}
