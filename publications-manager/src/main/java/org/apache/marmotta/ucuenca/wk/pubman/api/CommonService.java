/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.api;

import java.net.URL;
import java.util.List;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Provider;

/**
 *
 * @author Satellite
 */
public interface CommonService {

    String getDataFromDBLPProvidersService(final String[] organizations);

    String getDataFromScopusProvidersService(final String[] organizations);

    String getDataFromAcademicsKnowledgeProvidersService(final String[] organizations);

    String getDataFromScieloProvidersService(final String[] organizations);
    
    String getDataFromDOAJProvidersService(final String[] organizations);
    
    String getDataFromORCIDProvidersService(final String[] organizations);

    String getDataFromGoogleScholarProvidersService(final String[] organizations);

    String getDataFromSpringerProvidersService(final String[] organizations);

    String GetDataFromProvidersServiceDspace();

    String createReport(String hostname, String realPath, String name, String type, List<String> params);

    String getSearchQuery(String textSearch);

    String DetectLatindexPublications();

    String runDisambiguationProcess(String[] orgs);

    String runDisambiguationProcess();

    String CentralGraphProcess();

    String organizationListExtracted();

    String listREDIEndpoints();

    boolean deleteREDIEndpoint(String id);

    String organizationListEnrichment();

    List<Provider> getProviders() throws MarmottaException;

    String getCollaboratorsData(String uri);

    String getAuthorDataProfile(String author);

    String getsubClusterGraph(String cl, String subcl);
    
    String getClusterGraph (String cl);

    String getCluster(String cl);
    
    String getUniqueName(String names, String separator);

    void registerREDIEndpoint(String name, URL url) throws Exception;

    void centralize(String[] endpoints, boolean isUpdate);

}
