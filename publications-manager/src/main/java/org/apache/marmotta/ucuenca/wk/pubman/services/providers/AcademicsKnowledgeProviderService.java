/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services.providers;

import com.google.common.base.Preconditions;
import edu.emory.mathcs.backport.java.util.Collections;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.ucuenca.wk.pubman.api.AbstractProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.exceptions.APIException;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class AcademicsKnowledgeProviderService extends AbstractProviderService {

    @Inject
    private ConfigurationService configurationService;
    private String expression;

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

        firstName = StringUtils.stripAccents(firstName).trim().toLowerCase().replace("'", "");
        lastName = StringUtils.stripAccents(lastName).trim().toLowerCase().replace("'", "");

        StringBuilder expr = new StringBuilder("AND(Ty='1',OR(");

        String[] fName = splitName(firstName);
        String[] lName = splitName(lastName);
        for (int i = 0; i <= fName.length; i++) {
            if (i == fName.length) {
                expr.append("AuN='")
                        .append(firstName).append(' ')
                        .append(lName[0]).append('\'');
            } else {
                expr.append("AuN='")
                        .append(fName[i]).append(' ')
                        .append(lName[0]).append('\'');
            }
            if (i < fName.length) {
                expr.append(',');
            }
        }
        expr.append("))");
        expression = expr.toString();
        URIBuilder builder = new URIBuilder("https://api.labs.cognitive.microsoft.com/academic/v1.0/evaluate");
        builder.setParameter("expr", expression);
        builder.setParameter("attributes", "Id,AuN,DAuN,CC,ECC,LKA.AfId,LKA.AfN");
        builder.setParameter("model", "latest");
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
    protected List<String> buildURLs(String firstname, String lastname, List<String> organizations) {
        if (!(firstname != null && !"".equals(firstname.trim()) && lastname != null && !"".equals(lastname.trim()))) {
            return Collections.emptyList();
        }
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

    @Override
    protected String filterExpressionSearch() {
        try {
            return URLEncoder.encode(expression, "UTF-8").replace('+', '.');
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Cannot build expression", ex);
        }
    }

}
