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
import java.net.URLEncoder;
import java.util.List;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.pubman.api.AbstractProviderService;

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
    private final String expressionTemplateNames = "authfirst(%s) OR authfirst(%s) AND authlast(%s)";
    private final String expressionTemplateName = "authfirst(%s) AND authlast(%s)";
    private String expression;

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
        if (names.length == 2) {
            expression = String.format(expressionTemplateNames, names[0], names[1], lastname);
        } else {
            expression = String.format(expressionTemplateName, names[0], lastname);
        }
        try {
            expression = URLEncoder.encode(expression, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
        }
        return Collections.singletonList(String.format(requestTemplate, expression, apikey));
    }

    @Override
    protected String getProviderGraph() {
        return constantService.getScopusGraph();
    }

    @Override
    protected String getProviderName() {
        return "SCOPUS";
    }

    @Override
    protected String filterExpressionSearch() {
        return expression.replace('+', '.');
    }

}
