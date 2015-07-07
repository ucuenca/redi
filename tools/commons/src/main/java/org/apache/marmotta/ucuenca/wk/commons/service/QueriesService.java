/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.service;

/**
 *
 * @author Satellite
 */
public interface QueriesService {

    /**
     * return a query to obtain all resource related with Authors
     *
     * @return
     */
    String getAuthorsQuery();

    /**
     * return query to obtain all properties of a resource using LDC ( Linked
     * Data Client )
     *
     * @return
     */
    String getRetrieveResourceQuery();

    /**
     * Return a INSERT QUERY when object is a LITERAL
     *
     * @param s
     * @param p
     * @param o
     * @return
     */
    String getInsertDataLiteralQuery(String s, String p, String o);

    /**
     * Return a INSERT QUERY when object is a URI
     *
     * @param s
     * @param p
     * @param o
     * @return
     */
    String getInsertDataUriQuery(String s, String p, String o);

    /**
     * Return true or false if object is a URI
     *
     * @param object
     * @return
     */
    Boolean isURI(String object);

    /**
     *
     * @param resource
     * @return
     */
    String getAskQuery(String resource);

    String getEndpointNameQuery(String endpointsGraph, String name, String resourceHash);

    String getEndpointUrlQuery(String endpointsGraph, String url, String resourceHash);

    String getEndpointGraphQuery(String endpointsGraph, String graphUri, String resourceHash);
    
    String getlisEndpointsQuery(String endpointsGraph);

    String getEndpointByIdQuery(String endpointsGraph, String id);
    
    String getEndpointDeleteQuery(String endpointsGraph, String id);
}
