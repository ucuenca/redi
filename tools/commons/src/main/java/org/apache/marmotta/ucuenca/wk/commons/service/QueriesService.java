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

    String getAuthorsDataQuery(String graph);

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
     * @param graph
     * @param resource
     * @return
     */
    String getAskResourceQuery(String graph, String resource);

    /**
     * ASK is exist a triplet
     *
     * @param args //graph, subject, predicate, object arguments
     * @return
     */
    String getAskQuery(String... args);

    String getEndpointStatusQuery(String endpointsGraph, String state, String resourceHash);

    String getEndpointNameQuery(String endpointsGraph, String name, String resourceHash);

    String getEndpointUrlQuery(String endpointsGraph, String url, String resourceHash);

    String getEndpointGraphQuery(String endpointsGraph, String graphUri, String resourceHash);

    String getEndpointFullNameQuery(String endpointsGraph, String fullName, String resourceHash);

    String getEndpointCityQuery(String endpointsGraph, String city, String resourceHash);

    String getEndpointProvinceQuery(String endpointsGraph, String province, String resourceHash);

    String getEndpointLatitudeQuery(String endpointsGraph, String latitude, String resourceHash);

    String getEndpointLongitudeQuery(String endpointsGraph, String longitude, String resourceHash);

    String getlisEndpointsQuery(String endpointsGraph);

    String getEndpointByIdQuery(String endpointsGraph, String id);

    String getEndpointDeleteQuery(String endpointsGraph, String id);

    String getEndpointUpdateStatusQuery(String... args);

    String getWkhuskaGraph();

    String getGraphsQuery();

    String getPublicationsQuery(String providerGraph);

    String getPublicationsPropertiesQuery(String providerGraph, String publicationResource);

    String getMembersQuery();

    String getPublicationFromProviderQuery();

    String getPublicationForExternalAuthorFromProviderQuery(String property);

    String getPublicationPropertiesQuery();

    //Microsoft Academics
    String getPublicationsMAQuery(String providerGraph);

    String getPublicationFromMAProviderQuery();

    String getPublicationMAPropertiesQuery();

    //Google Scholar
    //String getPublicationFromGSProviderQuery();
    String getAuthorPublicationsQuery(String providerGraph, String author, String prefix);

    String getAuthorPublicationsQueryFromProvider(String providerGraph, String authorResource, String prefix);

    String getPublicationDetails(String publicationResource);

    String getPublicationsTitleQuery(String providerGraph, String prefix);

    String getPublicationsCount(String graph);

    String getTotalAuthorWithPublications(String graph);

    String getPublicationsCountCentralGraph();

    String deleteDataGraph(String graph);

    String getPublicationPropertiesAsResourcesQuery();

    String getTitlePublications(String graph);

    String getFirstNameLastNameAuhor(String graph, String authorResource);

    String authorDetailsOfProvenance(String graph, String authorResource);

    String getAuthorPublicationFilter(String graph, String fname, String lname);

    String getAskResourcePropertieQuery(String graph, String resource, String propertie);
}
