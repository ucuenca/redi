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

    String getPublicationsQuery();
    
    String getProvenanceProperty();

    String getLimit(String limit);

    String getOffset(String offset);

    String getCountPersonQuery(String graph);

    /**
     * return a query to obtain all resource related with Authors
     *
     * @param wkhuskagraph
     * @return
     */
    String getAuthorsQuery(String wkhuskagraph);

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
     * @param args
     * @return
     */
    String getInsertDataLiteralQuery(String... args);

    /**
     * Return a INSERT QUERY when object is a URI
     *
     * @param args
     * @return
     */
    String getInsertDataUriQuery(String... args);

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

    String getWkhuskaGraph();

}
