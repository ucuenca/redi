/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services.providers;

import com.google.common.base.Preconditions;
import edu.emory.mathcs.backport.java.util.Collections;
import java.net.URLEncoder;
import java.util.List;
import javax.inject.Inject;
import org.apache.marmotta.ucuenca.wk.commons.function.StrNamesUtils;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.pubman.api.AbstractProviderService;

/**
 *
 * @author José Ortiz
 */
public class DOAJProviderService extends AbstractProviderService {

    @Inject
    private CommonsServices commonsServices;

    @Override
    protected List<String> buildURLs(String firstName, String lastName, List<String> organizations) {
        if (!(firstName != null && !"".equals(firstName.trim()) && lastName != null && !"".equals(lastName.trim()))) {
            return Collections.emptyList();
        }
        firstName = StrNamesUtils.or(firstName);
        lastName = StrNamesUtils.or(lastName, 1);
        String NS_DBLP = "https://doaj.org/search/";
        String URI = NS_DBLP + URLEncoder.encode(firstName + "_" + lastName);
        return Collections.singletonList(URI);
    }

    @Override
    protected String getProviderGraph() {
        return constantService.getDOAJGraph();
    }

    @Override
    protected String getProviderName() {
        return "DOAJ";
    }
}
