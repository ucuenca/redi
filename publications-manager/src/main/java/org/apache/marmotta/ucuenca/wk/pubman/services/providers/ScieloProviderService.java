/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services.providers;

import com.google.common.base.Preconditions;
import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.pubman.api.AbstractProviderService;

/**
 *
 * @author José Ortiz
 */
public class ScieloProviderService extends AbstractProviderService {

    @Inject
    private ConstantService constantService;

    @Inject
    private CommonsServices commonsServices;

    @Override
    protected List<String> buildURLs(String firstName, String lastName) {
        Preconditions.checkArgument(firstName != null && !"".equals(firstName.trim()));
        Preconditions.checkArgument(lastName != null && !"".equals(lastName.trim()));
        firstName = or(firstName);
        lastName = or(lastName);
        String NS_DBLP = "https://search.scielo.org/search/";
        String URI = NS_DBLP + firstName+"_"+lastName;
        return Collections.singletonList(URI);
    }

    @Override
    protected String getProviderGraph() {
        return constantService.getDBLPGraph();
    }

    @Override
    protected String getProviderName() {
        return "SCIELO";
    }

    public String or(String name) {
        name = StringUtils.stripAccents(name).trim().toLowerCase().replaceAll("\\.|,|;|:|-|\n|\\\\|\\||\"|\'|_|/", " ").trim();
        String s = "";
        String[] tokens = name.split(" ");
        List<String> list = new ArrayList<String>(Arrays.asList(tokens));
        list.removeAll(Arrays.asList("", null));
        for (int i = 0; i < list.size(); i++) {
            s += list.get(i) + (i == list.size() - 1 ? "" : "-");
        }
        return s;
    }
}
