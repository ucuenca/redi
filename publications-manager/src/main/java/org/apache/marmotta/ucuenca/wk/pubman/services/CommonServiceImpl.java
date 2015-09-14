/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.inject.Inject;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.PropertyPubService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.CommonService;
import org.apache.marmotta.ucuenca.wk.pubman.api.DBLPProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.api.GoogleScholarProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.api.MicrosoftAcadProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.api.SparqlFunctionsService;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;

/**
 *
 * @author Satellite
 *
 */
public class CommonServiceImpl implements CommonService {

    @Inject
    MicrosoftAcadProviderServiceImpl microsoftAcadProviderService;

    @Inject
    DBLPProviderServiceImpl dblpProviderService;

//    @Inject
//    GoogleScholarProviderServiceImpl googleService;
    @Inject
    GoogleScholarProviderService googleService;

    @Inject
    Data2GlobalGraphImpl data2GlobalGraphService;
//

    @Inject
    DBLPProviderService dblpProviderServiceInt;

    @Override
    public String GetDataFromProvidersService() {

        Thread MicrosofProvider = new Thread(microsoftAcadProviderService);
        MicrosofProvider.start();

        Thread DblpProvider = new Thread(dblpProviderService);
        DblpProvider.start();

        //       return googleService.runPublicationsProviderTaskImpl("d");
//        Thread googleProvider = new Thread(googleScholarProviderService);
//        googleProvider.start();
        return "Data Providers are extracted in background.   Please review main.log file for details";

    }

    @Override
    public String Data2GlobalGraph() {
        Thread data2globalTask = new Thread(data2GlobalGraphService);
        data2globalTask.start();
        return "Load Publications Data from Providers Graph to Global Graph. Task run in background.   Please review main.log file for details";
    }

    @Override
    public JsonArray searchAuthor(String uri) {
        return dblpProviderServiceInt.SearchAuthorTaskImpl(uri);
    }
}
