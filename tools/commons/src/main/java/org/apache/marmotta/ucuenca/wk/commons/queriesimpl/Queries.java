/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.queriesimpl;

import java.net.URL;
import org.apache.marmotta.ucuenca.wk.commons.queriesservice.QueriesService;

/**
 *
 * @author Satellite
 */
public class Queries implements QueriesService {

    @Override
    public String getAuthorsQuery() {
        return "SELECT DISTINCT ?o WHERE {  ?s <http://id.loc.gov/vocabulary/relators/aut> ?o } ORDER BY ?o";
    }

    @Override
    public String getRetrieveResourceQuery() {
        return "SELECT * WHERE { ?x ?y ?z }";
    }

    /**
     * Return a INSERT QUERY when object is a LITERAL
     *
     * @param subject
     * @param predicate
     * @param object
     * @return
     */
    @Override
    public String getInsertDataLiteralQuery(String subject, String predicate, String object) {
        return "INSERT DATA { <" + subject + "> <" + predicate + "> " + object + " }";
    }

    /**
     * Return a INSERT QUERY when object is a URI
     *
     * @param subject
     * @param predicate
     * @param object
     * @return
     */
    @Override
    public String getInsertDataUriQuery(String subject, String predicate, String object) {
        return "INSERT DATA { <" + subject + "> <" + predicate + "> <" + object + "> }";

    }

    /**
     * Return true or false if object is a URI
     *
     * @param object
     * @return
     */
    @Override
    public Boolean isURI(String object) {
        URL url = null;
        try {
            url = new URL(object);
        } catch (Exception e1) {
            return false;
        }
        return "http".equals(url.getProtocol());
    }

    @Override
    public String getAskQuery(String resource) {
    return "ASK { <" + resource + "> ?p ?o }  ";
    }
}
