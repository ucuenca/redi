/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.CommonService;
import org.apache.marmotta.ucuenca.wk.pubman.api.ProviderServiceGoogleScholar;
import org.apache.marmotta.ucuenca.wk.pubman.api.ReportsService;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.AcademicsKnowledgeProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.DBLPProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.DspaceProviderServiceImpl;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.GoogleScholarProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.MicrosoftAcadProviderServiceImpl;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.ScopusProviderService;
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
    GoogleScholarProviderService googleProviderService;

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
    DisambiguationServiceImpl DisambiguationImpl;

    @Inject
    IndexCentralGraphImpl indexingCentralGraphService;

    @Inject
    private QueriesService queriesService;

    @Inject
    private SparqlService sparqlService;

    @Inject
    private CommonsServices com;

    @Inject
    private org.apache.marmotta.ucuenca.wk.pubman.services.providers.DBLPProviderService providerServiceDblp1;

    @Inject
    private org.apache.marmotta.ucuenca.wk.pubman.services.providers.ScieloProviderService providerServiceScielo;

    @Inject
    private ScopusProviderService providerServiceScopus1;
    private Thread scopusThread;
    private Thread academicsThread;
    private Thread dblpThread;
    private Thread scieloThread;
    private Thread scholarThread;

    @Override
    public String getDataFromScopusProvidersService(final String[] organizations) {
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
    public String getDataFromAcademicsKnowledgeProvidersService(final String[] organizations) {
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
    public String getDataFromDBLPProvidersService(final String[] organizations) {
        if (dblpThread != null && dblpThread.isAlive()) {
            return "Process is executing.";
        }
        dblpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                providerServiceDblp1.extractAuthors(organizations);
            }
        });
        dblpThread.start();
        return "Data Provider DBLP are extracted in background.   Please review main.log file for details";
    }

    @Override
    public String getDataFromProvidersServiceMicrosoftAcademics() {
        Thread MicrosofProvider = new Thread(microsoftAcadProviderService);
        MicrosofProvider.start();
        return "Data Provider MICROSOFT ACEDEMICS are extracted in background.   Please review main.log file for details";
    }

    @Override
    public String getDataFromGoogleScholarProvidersService(final String[] organizations) {

        if (scholarThread != null && scholarThread.isAlive()) {
            return "Process is executing.";
        }
        scholarThread = new Thread(new Runnable() {
            @Override
            public void run() {
                googleProviderService.extractAuthors(organizations);
            }
        });
        scholarThread.start();
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

//    @Override
//    public JsonArray searchAuthor(String uri) {
//
//        return dblpProviderServiceInt.SearchAuthorTaskImpl(uri);
//    }
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
    public String organizationListExtracted() {
        String queryOrg = queriesService.getExtractedOrgList();
        List<Map<String, Value>> response;
        try {
            response = sparqlService.query(QueryLanguage.SPARQL, queryOrg);
            return com.listmapTojson(response);
        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(CommonServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public String DisambiguationProcess() {
        String startProcess = DisambiguationImpl.startDisambiguation();
        return startProcess;
    }

    @Override
    public String CentralGraphProcess() {
        String startProcess = DisambiguationImpl.startMerge();
        return startProcess;
    }

    @Override
    public String getDataFromScieloProvidersService(final String[] organizations) {
        if (scieloThread != null && scieloThread.isAlive()) {
            return "Process is executing.";
        }
        scieloThread = new Thread(new Runnable() {
            @Override
            public void run() {
                providerServiceScielo.extractAuthors(organizations);
            }
        });
        scieloThread.start();
        return "Data Provider DBLP are extracted in background.   Please review main.log file for details";
    }

}
