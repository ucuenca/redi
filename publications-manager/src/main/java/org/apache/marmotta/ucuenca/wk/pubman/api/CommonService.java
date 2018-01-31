/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.api;

import java.util.List;

/**
 *
 * @author Satellite
 */
public interface CommonService {

    String getDataFromProvidersService(final String[] organizations);

    String GetDataFromProvidersServiceAcademicsKnowledge(final String[] organizations);

    String Data2GlobalGraph();

    String authorAttrFromProviders();

    String CountPublications();

    String GetDataFromProvidersServiceDBLP(final String[] organizations);
    String GetDataFromProvidersServiceScielo(final String[] organizations);

    String GetDataFromProvidersServiceMicrosoftAcademics();

    String GetDataFromProvidersServiceGoogleScholar(boolean update);

    String GetDataFromProvidersServiceDspace();

    String createReport(String hostname, String realPath, String name, String type, List<String> params);

    String IndexCentralGraph();

    String getSearchQuery(String textSearch);

    String DetectLatindexPublications();
    String DisambiguationProcess();
    String CentralGraphProcess();

    String organizationListExtracted();
}
