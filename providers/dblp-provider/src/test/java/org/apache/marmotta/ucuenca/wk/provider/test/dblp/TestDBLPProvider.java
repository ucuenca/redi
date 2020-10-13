/**
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
package org.apache.marmotta.ucuenca.wk.provider.test.dblp;

import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.test.provider.ProviderTestBase;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;

/**
 * Some tests over random data to DBLP
 *
 * @author Santiago Gonzalez
 */
public class TestDBLPProvider extends ProviderTestBase {

    private static final String DBLP = "http://dblp.uni-trier.de/search/author/api?q=Saquicela+Victor&format=xml";

    /**
     * Tests accessing Author named Victor Saquicela from DBLP.
     *
     * @throws Exception
     *
     */
   

    @Test
    //@Ignore
    public void testLegacyResolveURI() throws Exception {
//    	testResource("http://dblp.uni-trier.de/pers/hd/b/Bl=aacute=zquez:Luis_Manuel_Vilches");
        ClientResponse retrieveResource = ldclient.retrieveResource("http://rdf.dblp.com/ns/search/rodrigo-alejandro_cueva");
        Model data = retrieveResource.getData();
        Rio.write(data, System.out, RDFFormat.RDFXML);
        //testResource();
    }

}
