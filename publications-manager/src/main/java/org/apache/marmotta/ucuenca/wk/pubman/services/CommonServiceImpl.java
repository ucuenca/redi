/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import org.apache.marmotta.ucuenca.wk.pubman.services.providers.ScopusProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.MicrosoftAcadProviderServiceImpl;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.GoogleScholarProviderServiceImpl;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.AcademicsKnowledgeProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.DBLPProviderServiceImpl;
import com.google.gson.JsonArray;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.pubman.api.CommonService;
import org.apache.marmotta.ucuenca.wk.pubman.api.DBLPProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.api.ProviderServiceGoogleScholar;
import org.apache.marmotta.ucuenca.wk.pubman.api.ReportsService;
import org.json.JSONException;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;
import org.slf4j.Logger;

/**
 *
 * @author Satellite
 *
 */
public class CommonServiceImpl implements CommonService {
    
    @Inject
    private Logger log;

    @Inject
    MicrosoftAcadProviderServiceImpl microsoftAcadProviderService;

    @Inject
    DBLPProviderServiceImpl dblpProviderService;

    @Inject
    GoogleScholarProviderServiceImpl googleProviderService;

    @Inject
    ProviderServiceGoogleScholar googleService;

    @Inject
    AcademicsKnowledgeProviderService academicsKnowledgeService;

    @Inject
    Data2GlobalGraphImpl data2GlobalGraphService;

    @Inject
    CountPublicationsServiceImpl countPublicationsService;

    @Inject
    DBLPProviderService dblpProviderServiceInt;

    @Inject
    DspaceProviderServiceImpl dspaceProviderService;

    @Inject
    ReportsService reportService;

    @Inject
    ReportsImpl reportsImpl;

    @Inject
    AuthorAttributesImpl authorAttr;

    @Inject
    LantindexDetectionServiceImpl LatindexImpl;

    @Inject
    IndexCentralGraphImpl indexingCentralGraphService;

    @Inject
    private QueriesService queriesService;
    
   @Inject
    private SparqlService sparqlService;
     
    
    @Inject
    private CommonsServices com;

    @Inject
    private ScopusProviderService providerServiceScopus1;
    private Thread scopusThread;
    private Thread academicsThread;

    @Override
    public String getDataFromProvidersService(final String[] organizations) {
        // Find a way to execute thread and get response information.
        if (scopusThread != null && scopusThread.isAlive()) {
            return "Process is executing.";
        }

        scopusThread = new Thread(new Runnable() {
            @Override
            public void run() {
                providerServiceScopus1.extractAuthors(organizations);
            }
        });
        scopusThread.start();
        return "Data Provider SCOPUS are extracted in background.   Please review main.log file for details";

    }

    @Override
    public String GetDataFromProvidersServiceAcademicsKnowledge(final String[] organizations) {
        if (academicsThread != null && academicsThread.isAlive()) {
            return "Process is executing.";
        }
        academicsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                academicsKnowledgeService.extractAuthors(organizations);
            }
        });
        academicsThread.start();
        return "Data Provider AK are extracted in background.   Please review main.log file for details";
    }

    @Override
    public String GetDataFromProvidersServiceDBLP() {
        Thread DblpProvider = new Thread(dblpProviderService);
        DblpProvider.start();
        return "Data Provider DBLP are extracted in background.   Please review main.log file for details";
    }


    @Override
    public String GetDataFromProvidersServiceMicrosoftAcademics() {
        Thread MicrosofProvider = new Thread(microsoftAcadProviderService);
        MicrosofProvider.start();
        return "Data Provider MICROSOFT ACEDEMICS are extracted in background.   Please review main.log file for details";
    }

    @Override
    public String GetDataFromProvidersServiceGoogleScholar(boolean update) {
        googleProviderService.executeUpdateTask(update);
        Thread GoogleProvider = new Thread(googleProviderService);
        GoogleProvider.start();
        return "Data Provider Google Scholar are extracted in background.   Please review main.log file for details";
    }

    @Override
    public String GetDataFromProvidersServiceDspace() {
        Thread DspaceProvider = new Thread(dspaceProviderService);
        DspaceProvider.start();
        return "Data Provider Dspace are extracted in background.   Please review main.log file for details";
    }

    @Override
    public String Data2GlobalGraph() {
        Thread data2globalTask = new Thread(data2GlobalGraphService);
        data2globalTask.start();
        return "Load Publications Data from Providers Graph to Global Graph. Task run in background.   Please review main.log file for details";
    }

    @Override
    public String CountPublications() {
        Thread countPublications = new Thread(countPublicationsService);
        countPublications.start();
        return "Count Publications from Providers and  Global Graph. Task run in background.   Please review main.log file for details";
    }

    @Override
    public JsonArray searchAuthor(String uri) {

        return dblpProviderServiceInt.SearchAuthorTaskImpl(uri);
    }

    @Override
    public String createReport(String hostname, String realPath, String name, String type, List<String> params) {
        return reportService.createReport(hostname, realPath, name, type, params);
    }

    @Override
    public String authorAttrFromProviders() {
        new Thread(authorAttr).start();
        return "Extracting attributes from authors. Task run in background.   Please review main.log file for details";
    }

    @Override
    public String IndexCentralGraph() {
        Thread indexingTask = new Thread(indexingCentralGraphService);
        indexingTask.start();
        return "Index Publications in Global Graph. Task run in background.   Please review main.log file for details";
    }

    @Override
    public String getSearchQuery(String textSearch) {
        return queriesService.getSearchQuery(textSearch);
    }

    @Override
    public String DetectLatindexPublications() {
        String startProcess = LatindexImpl.startProcess();

        return startProcess;
    }
    
    @Override
    public String organizationListExtracted () {
        String queryOrg =  queriesService.getExtractedOrgList();
        List<Map<String, Value>> response;
        try {
            response = sparqlService.query(QueryLanguage.SPARQL, queryOrg);
            return com.listmapTojson (response);
        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(CommonServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         return null;
    }
    
         
         
         
    

}
