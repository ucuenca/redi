/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services.providers;

import com.google.common.base.Preconditions;
import edu.emory.mathcs.backport.java.util.Collections;
import java.net.URISyntaxException;
import java.util.List;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.pubman.exceptions.APIException;
import org.apache.marmotta.ucuenca.wk.pubman.api.AbstractProviderService;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class AcademicsKnowledgeProviderService extends AbstractProviderService {

    @Inject
    private ConstantService constantService;
    @Inject
    private ConfigurationService configurationService;

    /**
     * Build URLs to request information from Academics Knowledge API.
     * <p>
     * See the
     * <a href="https://docs.microsoft.com/es-es/azure/cognitive-services/academic-knowledge/queryexpressionsyntax">documentation
     * API</a> for documentation about the API.
     *
     *
     * @param firstName
     * @param lastName
     * @return
     */
    private String buildRequestURL(String firstName, String lastName) throws URISyntaxException, APIException {
        Preconditions.checkArgument(firstName != null && !"".equals(firstName.trim()));
        Preconditions.checkArgument(lastName != null && !"".equals(lastName.trim()));

        String apiKey = configurationService.getStringConfiguration("publications.academics.apikey");

        if (apiKey == null) {
            throw new APIException("There is not api-key.");
        }

        firstName = StringUtils.stripAccents(firstName).trim().toLowerCase();
        lastName = StringUtils.stripAccents(lastName).trim().toLowerCase();

        StringBuilder expression = new StringBuilder("AND(Ty='1',OR(");

        String[] fName = splitName(firstName);
        String[] lName = splitName(lastName);
        for (int i = 0; i <= fName.length; i++) {
            if (i == fName.length) {
                expression.append("AuN='")
                        .append(firstName).append(' ')
                        .append(lName[0]).append('\'');
            } else {
                expression.append("AuN='")
                        .append(fName[i]).append(' ')
                        .append(lName[0]).append('\'');
            }
            if (i < fName.length) {
                expression.append(',');
            }
        }
        expression.append("))");
        URIBuilder builder = new URIBuilder("https://westus.api.cognitive.microsoft.com/academic/v1.0/evaluate");
        builder.setParameter("expr", expression.toString());
        builder.setParameter("attributes", "Id,AuN,DAuN,CC,ECC,E");
        builder.setParameter("subscription-key", apiKey);

        return builder.build().toString();
    }

    private String[] splitName(String name) {
        int start = name.indexOf(' ');

        if (start > 0) {
            String first = name.substring(0, start);
            String second = name.substring(start + 1);
            return new String[]{first, second};
        }
        return new String[]{name};

    }

    @Override
    protected List<String> buildURLs(String firstname, String lastname) {
        List urls = Collections.emptyList();
        try {
            urls = Collections.singletonList(buildRequestURL(firstname, lastname));
        } catch (URISyntaxException | APIException ex) {
            throw new RuntimeException("Invalid API Key");
        }
        return urls;
    }

    @Override
    protected String getProviderGraph() {
        return constantService.getAcademicsKnowledgeGraph();
    }

    @Override
    protected String getProviderName() {
        return "Academics Knowledge";
    }

}
