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
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.test.provider.ProviderTestBase;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Xavier Sumba
 */
public class TestSpringerProvider extends ProviderTestBase {

    private final String apikey = "b4091be5da784342b86f6a4d05d9af57";

    /**
     * Tests the extraction of an author and his publications. This test might
     * fail for two reasons.
     * <ol>
     * <li> There API key is not valid.
     * <li> Quota limit.
     * </ol>
     *
     * @throws org.openrdf.repository.RepositoryException
     */
    @Test
    @Ignore
    public void testAcademicsKnowledgeAPI() throws RepositoryException {

        String uri = "https://westus.api.cognitive.microsoft.com/academic/v1.0/evaluate?"
                + "expr=And(Ty=%271%27,%20AuN=%27mauricio%20espinoza%27)&"
                + "attributes=Id,AuN,DAuN,CC,ECC,E&"
                + "model=latest&"
                + "subscription-key=" + apikey;

        ClientResponse response = null;
        try {
            response = ldclient.retrieveResource(uri);
        } catch (DataRetrievalException ex) {
            Assert.assertTrue("Change API-KEY, Quota Exceeded.", ex.getCause().toString().contains("403 Quota Exceeded"));
            return;
        }

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
