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
package org.apache.marmotta.ucuenca.wk.provider.test.scopus;

import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ldclient.test.provider.ProviderTestBase;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;

/**
 * Some tests over random data to Scopus
 *
 * @author Jose Luis Cullcay
 */
public class TestScopusProvider extends ProviderTestBase {

    private static final String Scopus = "http://api.elsevier.com/content/search/author?query=authfirst%28Mauricio%29authlast%28Espinoza%29&apiKey=a3b64e9d82a8f7b14967b9b9ce8d513d&httpAccept=application/xml";

    /**
     * Tests accessing Author named Victor Saquicela from Scopus.
     *
     * @throws Exception
     *
     */
    @Test
    @Ignore
    public void testScopus() throws Exception {
        testResource(Scopus, "all.Victor_Saquicela.sparql");
    }

    @Test
    @Ignore
    public void testLegacyResolveURI() throws Exception {
        //testResource("http://dblp.uni-trier.de/pers/hd/b/Bl=aacute=zquez:Luis_Manuel_Vilches");
        testResource("http://api.elsevier.com/content/search/author?query=authfirst(Victor)authlast(Saquicela)&apiKey=6492f9c867ddf3e84baa10b5971e3e3d");
    }

    @Test
    public void testMicrosoftAcademics() {
        ClientConfiguration config = new ClientConfiguration();

        LDClientService ldclient = new LDClient(config);
        try {
            ClientResponse res;

            res = ldclient.retrieveResource("http://api.elsevier.com/content/search/author?query=authfirst%28Mauricio%29authlast%28Espinoza%29+AND+affil%28Ecuador%29&apiKey=a3b64e9d82a8f7b14967b9b9ce8d513d&httpAccept=application/xml");

                RDFHandler handler = new RDFXMLPrettyWriter(System.out);
            try {
                res.getTriples().getConnection().export(handler);
            } catch (RepositoryException e) {
                //e.printStackTrace();
            } catch (RDFHandlerException e) {
                //e.printStackTrace();
            }
        } catch (DataRetrievalException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }
    }

}
