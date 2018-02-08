/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;

import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;

import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;

import org.apache.marmotta.ucuenca.wk.pubman.api.CommonService;
import org.apache.marmotta.ucuenca.wk.pubman.api.ReportsService;

import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Provider;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.DBLPProviderService;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;

import org.apache.marmotta.ucuenca.wk.pubman.services.providers.AcademicsKnowledgeProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.DBLPProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.DspaceProviderServiceImpl;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.GoogleScholarProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.services.providers.ScopusProviderService;

import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;
import org.rometools.feed.module.mediarss.types.Hash;
import org.slf4j.Logger;

/**
 *
 * @author Satellite
 *
 */
public class CommonServiceImpl implements CommonService {
    @Inject
    private ConstantService con;

    @Inject
    private Logger log;

    @Inject
    GoogleScholarProviderService googleProviderService;

    @Inject
    AcademicsKnowledgeProviderService academicsKnowledgeService;


    @Inject
    DBLPProviderService dblpProviderServiceInt;

    @Inject
    DspaceProviderServiceImpl dspaceProviderService;

    @Inject
    ReportsService reportService;

    @Inject
    ReportsImpl reportsImpl;

    @Inject
    LantindexDetectionServiceImpl LatindexImpl;

    @Inject
    DisambiguationServiceImpl DisambiguationImpl;


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
    public String createReport(String hostname, String realPath, String name, String type, List<String> params) {
        return reportService.createReport(hostname, realPath, name, type, params);
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
        List<Provider> prov = getProviders();
        Map <String, String> mprov = new HashMap ();
        for (Provider p  :prov){
       
        mprov.put(p.Graph, p.Name);
       
        }    
        String queryOrg = queriesService.getExtractedOrgList(mprov);
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
    public String organizationListEnrichment() {
        List<Provider> prov = getProviders();
        Map <String, String> mprov = new HashMap ();
        for (Provider p  :prov){
       
        mprov.put(p.Graph, p.Name);
       
        }     
        String queryOrg = queriesService.getOrgEnrichmentProvider( mprov);
        List<Map<String, Value>> response1;
        
        String queryd = queriesService.getOrgDisambiguationResult (mprov);
        log.info(queryd);
         List<Map<String, Value>> response2;
         
        
        try {
            response1 = sparqlService.query(QueryLanguage.SPARQL, queryOrg);
            response2 = sparqlService.query(QueryLanguage.SPARQL, queryd);
            log.info ("RESP");
            log.info (response2.toString());
             for ( Map<String, Value> m1:response1) {
              for (Map<String, Value> m2: response2) {
                  if (m1.containsKey("org") && m2.containsKey("org")){
                     if (m1.get("org").equals( m2.get("org"))){
                      //m1.putAll(m2);
                        // String log;
                         for (Map.Entry<String, Value> e2  :m2.entrySet()) {
                          m1.put(e2.getKey()+"l", e2.getValue());
                         }    
                     }                     
                  }
              }
             }
            
            
            return com.listmapTojson(response1);
        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(CommonServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
    
     @Override
     public List<Provider> getProviders()  {
            List<Provider> Providers = new ArrayList();
        try {
         
          //  Providers.add(new Provider("Authors", con.getAuthorsProviderGraph(), sparqlService, ""));
            
            String queryProviders = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                    + "SELECT ?uri ?name ?graph WHERE { "
                    + "  GRAPH ?graph { "
                    + "  ?uri a <"+REDI.PROVIDER.toString()+"> . "
                    + "  ?uri rdfs:label ?name "
                    + "  }}";
            
            List<Map<String, Value>> response = sparqlService.query(QueryLanguage.SPARQL, queryProviders);
            
            for (Map<String, Value> prov : response) {
                
                Provider p = new Provider(prov.get("name").stringValue().replace(" ", ""), prov.get("graph").stringValue(), sparqlService, prov.get("uri").stringValue());
                Providers.add(p);
            }
            
            return Providers;
        } catch (MarmottaException ex) {
            java.util.logging.Logger.getLogger(CommonServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return Providers; 
        }
    }
    

    
     @Override
    public String runDisambiguationProcess(String [] orgs) {
        String startProcess = DisambiguationImpl.startDisambiguation( orgs);
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
