/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services.providers;

import edu.emory.mathcs.backport.java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.pubman.api.AbstractProviderService;

/**
 *
 * @author José Ortiz
 */
public class DBLPProviderService extends AbstractProviderService {

    @Inject
    private ConstantService constantService;

    @Inject
    private CommonsServices commonsServices;

    @Override
    protected List<String> buildURLs(String firstname, String lastname) {
        String NS_DBLP = "http://rdf.dblp.com/ns/search/";
        String nameToFind = commonsServices.removeAccents(priorityFindQueryBuilding(firstname, lastname));
        String URI = NS_DBLP + nameToFind;
        return Collections.singletonList(URI);
    }

    @Override
    protected String getProviderGraph() {
        return constantService.getDBLPGraph();
    }

    @Override
    protected String getProviderName() {
        return "DBLP";
    }

    public String priorityFindQueryBuilding(String firstName, String lastName) {
        String fn = firstName.trim();
        String ln = lastName.trim();
        fn = fn.split(" ")[0];
        ln = ln.split(" ")[0];
        return fn + "_" + ln;
    }
}
