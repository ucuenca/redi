/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.utils;

import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;

/**
 *
 * @author cedia
 */
public class ScopusMapper {

    private JSONObject raw;

    private Model scopus;
    private Model endpoints;
    private Model organizations;
    private Model authors;

    public ScopusMapper(JSONObject raw) {
        this.raw = raw;
        scopus = new LinkedHashModel();
        endpoints = new LinkedHashModel();
        organizations = new LinkedHashModel();
        authors = new LinkedHashModel();
        
    }

    public void run() {
    }

    public Model getScopus() {
        return scopus;
    }

    public Model getEndpoints() {
        return endpoints;
    }

    public Model getOrganizations() {
        return organizations;
    }

    public Model getAuthors() {
        return authors;
    }

}
