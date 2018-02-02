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

import java.util.ArrayList;
import java.util.List;
import org.apache.marmotta.ucuenca.wk.pubman.api.AbstractProviderService;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class GoogleScholarProviderService extends AbstractProviderService {

    private final String template = "https://scholar.google.com/citations?mauthors=%s&hl=en&view_op=search_authors";

    @Override
    protected List<String> buildURLs(String firstname, String lastname, List<String> organizations) {
        List<String> queries = new ArrayList<>(organizations.size());
        String name = lastname.split(" ")[0];
        for (String organization : organizations) {
            String query = (name + " " + organization)
                    .trim()
                    .toLowerCase()
                    .replace(' ', '+');
            queries.add(String.format(template, query));
        }
        return queries;
    }

    @Override
    protected String getProviderGraph() {
        return constantService.getAcademicsKnowledgeGraph();
    }

    @Override
    protected String getProviderName() {
        return "Google Scholar";
    }

}