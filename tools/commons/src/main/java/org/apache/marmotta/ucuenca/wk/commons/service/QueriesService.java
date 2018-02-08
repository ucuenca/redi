/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.service;


import java.util.Map;


/**
 *
 * @author Satellite
 */
public interface QueriesService {

    String getAuthorsDataQuery(String... organizations);

    String getAuthorsDataQuery(String organization);

    String getAuthors();

    String getAuthorsPropertiesQuery(String authorURI);

    String getAuthorsTuplesQuery(String subject);

    String getAuthorDeleteQuery(String id);

    String getAuthorDataQuery(String authorUri);

    String getAuthorProvenanceQuery(String authorUri);

    String getAuthorsByName(String graph, String firstName, String lastName);

    String getSameAsAuthors(String authorResource);

    String getSameAsAuthors(String graph, String authorResource);

    String getCountPersonQuery(String graph, String num);

    String getCountAuthors();

    String getCountSubjects(String authorResource);

    String getArticlesFromDspaceQuery(String graph, String person);

    String getLimit(String limit);

    String getOffset(String offset);

    String getIESInfobyAuthor(String authorName);

    String getAskPublicationsURLGS(String graphName, String authorResource);

    String getPublicationsURLGS(String graphName, String authorResource);

    String getInsertGeneric(String graph, String resource, String property, String object, String literal);

    String removeGenericType(String graph, String type, String resource);

    String removeGenericRelation(String graph, String relation, String resource);

    String getListOrganizationQuery();

    String getListEndpoints();

    String getListEndpointsByUri(String uri);

    
    String getExtractedOrgList(Map<String, String> providers);
    
    String getOrgEnrichmentProvider( Map <String, String> mp);
    
    String getOrgDisambiguationResult(Map<String, String> providers);

    /**
     * return query to obtain all subject ( keywords ) of an author , using
     * dct:subject property
     *
     * @return
     */
    String getRetrieveKeysQuery();

    /**
     * return a query to obtain all resource related with Authors
     *
     * @param wkhuskagraph
     * @param num
     * @return
     */
    String getAuthorsQuery(String wkhuskagraph, String num);

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
    String getInsertDataLiteralQuery(String graph, String subject, String predicate, String object, String datatype);

    String getInsertDataLiteralQuery(String graph, String subject, String predicate, String object);

    /**
     * Return a INSERT QUERY when object is a URI
     *
     * @param args
     * @return
     */
    String getInsertDataUriQuery(String... args);

    /**
     * Returns Insert query. Where args[0] is graph, args[1] is subject, args[2]
     * is predicate, and args[3] is object.
     *
     * @param args
     * @return
     */
    String buildInsertQuery(String... args);

    /**
     *
     * @param graph
     * @param resource
     * @return
     */
    String getAskResourceQuery(String graph, String resource);

    String getAskObjectQuery(String graph, String resource, String filterexpr);

    String getAskObjectQuery(String graph, String object);

    String getAskAcademicsQuery(String graph, String object);

    /**
     * ASK is exist a triplet
     *
     * @param args //graph, subject, predicate, object arguments
     * @return
     */
    String getAskQuery(String... args);

    String getEndpointDataQuery(String... arg);

    String getInsertEndpointQuery(String resourceHash, String property, String object, String literal);

    String getInsertDomainQuery(String enpointId, String domain);

    String getLisEndpointsQuery();

    String getlisEndpointsQuery(String endpointsGraph);

    String getlistEndpointNamesQuery();

    String getEndpointByIdQuery(String endpointsGraph, String id);

    String getEndpointDeleteQuery(String endpointsGraph, String id);

    String getEndpointUpdateStatusQuery(String... args);

    String getGraphsQuery();

    String getPublicationsQuery(String providerGraph);

    String getPublicationsPropertiesQuery(String providerGraph, String publicationResource);

    String getPublicationsPropertiesQuery(String publicationResource);

    //String getMembersQuery();
    String getPublicationFromProviderQuery();

    String getPublicationForExternalAuthorFromProviderQuery(String property);

    String getPublicationPropertiesQuery(String property);

    String getInsertOrganizationQuery(String resourceHash, String property, String object, String literal);

    //Microsoft Academics
//    String getPublicationsMAQuery(String providerGraph);
    //  String getPublicationFromMAProviderQuery();
    //  String getPublicationMAPropertiesQuery();
    String getAllTitlesDataQuery(String graph);

    // String getAllTitlesDataQuery();
    //String getMembersByTitleQuery();
    String getObjectByPropertyQuery(String subject, String property);

    String getObjectByPropertyQuery(String graphname, String subject, String property);

    String getObjectByPropertyQuery(String property);

    String getAbstractAndTitleQuery(String resource);

    String getSubjectAndObjectByPropertyQuery(String property);
    //Google Scholar

    // String getAllCreatorsDataQuery();
    //String getPublicationFromGSProviderQuery();
    String getAuthorPublicationsQuery(String... varargs);

    String getAuthorPublicationsQueryFromProvider(String... varargs);

    String getPublicationDetails(String publicationResource);

    String getPublicationsTitleQuery(String providerGraph, String prefix);

    String getPublicationsTitleScopusQuery(String providerGraph, String prefix);

    String getPublicationsCount(String graph);

    String getTotalAuthorWithPublications(String graph);

    String getPublicationsCountCentralGraph();

    String deleteDataGraph(String graph);

    String getPublicationPropertiesAsResourcesQuery();

    String getTitlePublications(String graph);

    String getFirstNameLastNameAuhor(String graph, String authorResource);

    String detailsOfProvenance(String graph, String resource);

    String authorGetProvenance(String graph, String authorResource);

    String authorGetProvenance(String authorResource);

    String getAuthorPublicationFilter(String graph, String fname, String lname);

    String getAskResourcePropertieQuery(String graph, String resource, String propertie);

    String getAskProcessAlreadyAuthorProvider(String providerGraph, String authorResource);

    String getAuthorsKeywordsQuery(String resource);

    String getAuthorSubjectQuery(String resource);

    /**
     * Get All Data sources from UTPL ENDPOINT
     *
     * @param graph
     * @return
     */
    String getSourcesfromUniqueEndpoit(String graph);

    /**
     * Get All documents from UTPL ENDPOINT
     *
     * @param repository
     * @param graph
     * @return
     */
    String getDocumentsAuthors(String repository, String graph);

    /**
     * @See Get all resources of an specific type
     *
     * @param type
     * @return
     */
    String getResourceUriByType(String type);

    /**
     * @See Get all properties of a resource by type
     * @param type
     * @return
     */
    String getPropertiesOfResourceByType(String type);

    /**
     * Get all titles of publications give a type.
     *
     * @param graph
     * @param type
     * @return
     */
    String getPublicationsTitleByType(String graph, String type);

    /**
     * @See Get all properties of an author by uri.
     * @param graph
     * @param endpointsgraph
     * @param resource
     * @return
     */
    String getAuthorsDataQueryByUri(String graph, String endpointsgraph, String resource);

    /**
     * @See Get triples by filters, parameters of search in args[]
     * @param args
     * @return
     */
    String getTriplesByFilter(String... args);

    /**
     * @See Get an uri endpoint by name of endpoint.
     *
     * @param nameEndpint
     * @return
     */
    String getEndPointUriByName(String nameEndpint);

    String getAuthorPublicationsQueryFromGenericProvider(String... args);

    String getPublicationsScholar(String resource);

    String getProfileScholarAuthor();

    /**
     * * Some queries to get info about same authors. This is a test.
     *
     * @return
     */
    String getAuthorsCentralGraphSize();

    String getAuthorsCentralGraph(int limit, int offset);

    String getSameAuthorsLvl2(String authorResource);

    String getOptionalProperties(String sameAs, String property);

    String getPublicationsTitlesQuery();

    String getSearchQuery(String textSearch);

    /**
     * Returns a list of every Journal within the Central Graph .
     */
    String getJournalsCentralGraphQuery();

    /**
     * Returns a list of every Journal within the Latindex Graph .
     */
    String getJournalsLantindexGraphQuery();

    /**
     * Returns a list of every Publication of a Journal.
     */
    String getPublicationsOfJournalCentralGraphQuery(String journalURI);

    /**
     * Returns a list of every Publication.
     */
    String getPublicationsCentralGraphQuery();

    String getOrgByUri(String uri);

    String updateGeneric(String graph, String resource, String property, String object, String literal);

    String removeGeneric(String graph, String resource, String property, String object, String literal);

    // Queries for used with MongoDB
    String getBarcharDataQuery();

    String getAggreggationAuthors();

    String getAggregationPublications();

    String getAggregationAreas();

    String getKeywordsFrequencyPub();
}
