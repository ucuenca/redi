/*
 * Copyright 2018 Xavier Sumba <xavier.sumba93@ucuenca.ec>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.List;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.ucuenca.wk.pubman.api.AbstractProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.exceptions.APIException;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class SpringerProviderService extends AbstractProviderService {

    @Inject
    private ConfigurationService configurationService;
    private final String TEMPLATE = "http://api.springer.com/meta/v1/json?q=%s&api_key=%s&p=50&s=0";

    @Override
    protected List<String> buildURLs(String firstname, String lastname, List<String> organization) {
        Preconditions.checkArgument(firstname != null && !"".equals(firstname.trim()));
        Preconditions.checkArgument(lastname != null && !"".equals(lastname.trim()));

        String apiKey = configurationService.getStringConfiguration("publications.springer.apikey");

        if (apiKey == null) {
            throw new RuntimeException(new APIException("Invalid Api Key."));
        }

        firstname = StringUtils.stripAccents(firstname).trim().toLowerCase().replace("'", "");
        lastname = StringUtils.stripAccents(lastname).trim().toLowerCase().replace("'", "");

        StringBuilder expr = new StringBuilder("(");

        String[] fName = firstname.split(" ");
        String[] lName = lastname.split(" ");
        if (lName.length > 0) {
            lastname = lName[0];
        }
        for (int i = 0; i < fName.length; i++) {
            if (i == 0) {
                expr.append("(");
            }
            expr.append("name:").append(fName[i]);
            if (i == fName.length - 1) {
                expr.append(")");
            } else {
                expr.append(" OR ");
            }
        }
        if (!"".equals(lastname)) {
            expr.append(" AND name:").append(lastname);
        }
        expr.append(")");

        String query = String.format(TEMPLATE, expr.toString(), apiKey);
        return Collections.singletonList(query);
    }

    @Override
    protected String getProviderGraph() {
        return constantService.getSpringerGraph();
    }

    @Override
    protected String getProviderName() {
        return "Springer";
    }

}
