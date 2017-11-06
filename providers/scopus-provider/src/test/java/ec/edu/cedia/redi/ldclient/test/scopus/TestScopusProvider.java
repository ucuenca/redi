/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ec.edu.cedia.redi.ldclient.test.scopus;

import org.apache.marmotta.ldclient.test.provider.ProviderTestBase;
import org.junit.Test;

/**
 * Some tests over random data on Scopus
 *
 * @author Jose Luis Cullcay
 * @author Xavier Sumba
 */
public class TestScopusProvider extends ProviderTestBase {

    private final String apiKey = "a3b64e9d82a8f7b14967b9b9ce8d513d";
    private final String templateURL = "http://api.elsevier.com/content/search/author?query=%s&apiKey=%s";

    /**
     * Tests the Scopus APIs. It looks for an author and returns an author match
     * along with its publications.
     *
     * @throws Exception
     */
    @Test
    public void testAuthorSearchSingleAuthor() throws Exception {
        testResource(String.format(templateURL, "authfirst(victor)authlast(saquicela)", apiKey));
    }

    /**
     * Tests the Scopus APIs. It looks for authors and returns all matches along
     * with its publications.
     *
     * @throws Exception
     */
    @Test
    public void testAuthorSearchManyAuthor() throws Exception {
        testResource(
                String.format(templateURL, "authfirst(mauricio)authlast(espinoza)", apiKey),
                "scopus-author-search.sparql");
    }
}
