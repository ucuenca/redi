/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ec.edu.cedia.redi.ldclient.test.springer;

import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.test.provider.ProviderTestBase;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.repository.RepositoryConnection;

/**
 *
 * @author Xavier Sumba
 */
public class TestSpringerProvider extends ProviderTestBase {

    private final String KEY = "a6bf2dbe42d9e7d523fadd7c40dcc43d";
    private final String TEMPLATE = "http://api.springer.com/meta/v1/json?q=%s&api_key=" + KEY + "&p=50&s=0";

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testSpringerAuthorWithIsbn() throws Exception {
        String uri = String.format(TEMPLATE, "((name:victor OR name:hugo) AND name:saquicela)");
        testResource(uri);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testSpringerAuthorWithIssn() throws Exception {
        String uri = String.format(TEMPLATE, "name:Xi+Hongxia");
        testResource(uri);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    @Ignore("This test makes many requests bc of pagination.")
    public void testSpringerAuthorPagination() throws Exception {
        String uri = String.format(TEMPLATE, "name:Xi AND name:Mei");
        testResource(uri);
    }

    @Override
    protected void testResource(String uri) throws Exception {
        // Override method bc cannot ping service. 
        ClientResponse response = ldclient.retrieveResource(uri);

        RepositoryConnection connection = ModelCommons.asRepository(response.getData()).getConnection();
        try {
            connection.begin();
            Assert.assertTrue(connection.size() > 0);
        } finally {
            connection.commit();
            connection.close();
        }
    }

}
