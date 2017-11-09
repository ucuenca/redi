/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.google.common.base.Preconditions;
import edu.emory.mathcs.backport.java.util.Collections;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class ScopusProviderService extends AbstractProviderService {

    @Inject
    private ConstantService constantService;
    @Inject
    private ConfigurationService configurationService;

    private final String requestTemplate = "http://api.elsevier.com/content/search/author?query=%s&count=100&apiKey=%s";
    private final String expressionTemplate = "authfirst(%s) OR authfirst(%s) AND authlast(%s)";

    @Override
    protected List<String> buildURLs(String firstname, String lastname) {
        Preconditions.checkArgument(firstname != null && !"".equals(firstname.trim()));
        Preconditions.checkArgument(lastname != null && !"".equals(lastname.trim()));

        String apikey = configurationService.getStringConfiguration("publications.scopus.apikey");

        if (apikey == null) {
            throw new RuntimeException("Invalid apikey");
        }

        firstname = StringUtils.stripAccents(firstname).trim().toLowerCase();
        lastname = StringUtils.stripAccents(lastname).trim().toLowerCase();

        String[] names = firstname.split(" ").length == 2 ? firstname.split(" ") : new String[]{firstname};
        lastname = lastname.split(" ").length > 1 ? lastname.split(" ")[0] : lastname;

        String expression = String.format(expressionTemplate, names[0], names[1], lastname);
        try {
            expression = URLEncoder.encode(expression, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
        }
        return Collections.singletonList(String.format(requestTemplate, expression, apikey));
    }

    private String cleanNameAuthor(String value) {
        value = value.toLowerCase().trim();
        value = value.replaceAll("  ", " ");
        value = value.replaceAll(" de ", " ");
        value = value.replaceAll(" del ", " ");
        value = value.replaceAll(" los ", " ");
        value = value.replaceAll(" y ", " ");
        value = value.replaceAll(" las ", " ");
        value = value.replaceAll(" la ", " ");

        value = value.replaceAll("^de ", "");
        value = value.replaceAll("^del ", "");
        value = value.replaceAll("^los ", "");
        value = value.replaceAll("^y ", "");
        value = value.replaceAll("^las ", "");
        value = value.replaceAll("^la ", "");

        value = value.replaceAll(" de$", "");
        value = value.replaceAll(" del$", "");
        value = value.replaceAll(" los$", "");
        value = value.replaceAll(" y$", "");
        value = value.replaceAll(" las$", "");
        value = value.replaceAll(" la$", "");

        return value.trim();
    }

    @Override
    protected String getProviderGraph() {
        return constantService.getScopusGraph();
    }

    @Override
    protected String getProviderName() {
        return "SCOPUS";
    }
}
