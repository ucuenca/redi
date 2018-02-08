/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.api;

import java.util.List;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Provider;
//import org.apache.marmotta.ucuenca.wk.pubman.disambiguation.Provider;

/**
 *
 * @author Satellite
 */
public interface CommonService {

    String getDataFromDBLPProvidersService(final String[] organizations);

    String getDataFromScopusProvidersService(final String[] organizations);

    String getDataFromAcademicsKnowledgeProvidersService(final String[] organizations);

    String getDataFromScieloProvidersService(final String[] organizations);

    String getDataFromGoogleScholarProvidersService(final String[] organizations);

    String GetDataFromProvidersServiceDspace();

    String createReport(String hostname, String realPath, String name, String type, List<String> params);


    String getSearchQuery(String textSearch);

    String DetectLatindexPublications();

  
    String runDisambiguationProcess(String [] orgs);

    String CentralGraphProcess();

    String organizationListExtracted();
    
    String organizationListEnrichment();
    
    List<Provider> getProviders();
}
