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

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.test.provider.ProviderTestBase;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Xavier Sumba
 */
public class TestSpringerProvider extends ProviderTestBase {

    private final String KEY = "a6bf2dbe42d9e7d523fadd7c40dcc43d";
    private final String TEMPLATE = "http://api.springer.com/meta/v1/json?q=%s&api_key=" + KEY + "&p=50&s=0";
    private final static Logger log = LoggerFactory.getLogger(TestSpringerProvider.class);

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testSpringerAuthorWithIsbn() throws Exception {
        String uri = String.format(TEMPLATE, encodeQuery("((name:victor OR name:hugo) AND name:saquicela)"));
        testResource(uri);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    @Ignore("At the moment the authors has not results because of name matching. Try later!")
    public void testSpringerAuthorWithIssn() throws Exception {
        String uri = String.format(TEMPLATE, encodeQuery("name:Xi+Hongxia"));
        testResource(uri);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    @Ignore("This test makes many requests bc of pagination. Disabled because makes many calls.")
    public void testSpringerAuthorPagination() throws Exception {
        String uri = String.format(TEMPLATE, encodeQuery("name:Xi AND name:Mei"));
        testResource(uri);
    }

    private String encodeQuery(String query) {
        try {
            return URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            log.error("Cannot encode query: " + query, ex);
            return null;
        }
    }

    @Override
    protected void testResource(String uri) throws Exception {
        // Override method bc cannot ping service. 
        // Assume.assumeTrue("LDClient endpoint for <" + uri + "> not available", ldclient.ping(uri));

        ClientResponse response = ldclient.retrieveResource(uri);

        Assume.assumeTrue(response.getHttpStatus() == 200);

        RepositoryConnection connection = ModelCommons.asRepository(response.getData()).getConnection();
        try {
            connection.begin();
            Assert.assertTrue(connection.size() > 0);
            if (log.isDebugEnabled()) {
                StringWriter out = new StringWriter();
                connection.export(Rio.createWriter(RDFFormat.TURTLE, out));
                log.debug("DATA:");
                log.debug(out.toString());
            }
        } finally {
            connection.commit();
            connection.close();
        }
    }

}
