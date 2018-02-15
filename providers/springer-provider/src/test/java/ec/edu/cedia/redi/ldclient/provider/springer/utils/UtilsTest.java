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
package ec.edu.cedia.redi.ldclient.provider.springer.utils;

import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class UtilsTest {

    public UtilsTest() {
    }

    /**
     * Test of buildNameFromRequest method, of class SpringerUtility.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildNameRequest() throws Exception {
        String resource = "http://api.springer.com/meta/v1/json?q=((name:victor+OR+name:hugo)+AND+name:saquicela)&api_key=1234&p=50&s=0";
        String expResult = "Victor Hugo Saquicela";
        assertEquals(expResult, SpringerUtility.buildNameFromRequest(resource));

        resource = "http://api.springer.com/meta/v1/json?q=((name:victor OR name:hugo) AND name:saquicela)&api_key=1234&p=50&s=0";
        expResult = "Victor Hugo Saquicela";
        assertEquals(expResult, SpringerUtility.buildNameFromRequest(resource));

        resource = "http://api.springer.com/meta/v1/json?q=(name:victor AND name:saquicela)&api_key=1234&p=50&s=0";
        expResult = "Victor Saquicela";
        assertEquals(expResult, SpringerUtility.buildNameFromRequest(resource));

        resource = "http://api.springer.com/meta/v1/json?q=(name:saquicela)&api_key=1234&p=50&s=0";
        expResult = "Saquicela";
        assertEquals(expResult, SpringerUtility.buildNameFromRequest(resource));
    }

    /**
     * Test of buildNameFromQuery method, of class SpringerUtility.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildNameQuery() throws Exception {
        String query = "((name:victor OR name:hugo) AND name:saquicela)";
        String expResult = "Victor Hugo Saquicela";
        assertEquals(expResult, SpringerUtility.buildNameFromQuery(query));

        query = "((name:victor+OR+name:hugo)+AND+name:saquicela)";
        expResult = "Victor Hugo Saquicela";
        assertEquals(expResult, SpringerUtility.buildNameFromQuery(query));

        query = "(name:victor AND name:saquicela)";
        expResult = "Victor Saquicela";
        assertEquals(expResult, SpringerUtility.buildNameFromQuery(query));

        query = "name:saquicela";
        expResult = "Saquicela";
        assertEquals(expResult, SpringerUtility.buildNameFromQuery(query));

        query = "(name:saquicela)";
        expResult = "Saquicela";
        assertEquals(expResult, SpringerUtility.buildNameFromQuery(query));
    }

    /**
     * Test of buildNameFromRequest method, of class SpringerUtility when the
     * name parameter is empty.
     *
     * @throws java.lang.Exception
     */
    @Test(expected = DataRetrievalException.class)
    public void testBuildNameEmptyNameinRequest() throws Exception {
        String resource = "http://api.springer.com/meta/v1/json?q=(name:)&api_key=1234&p=50&s=0";
        SpringerUtility.buildNameFromRequest(resource);
    }

    /**
     * Test of buildNameFromRequest method, of class SpringerUtility when the
     * name parameter is empty.
     *
     * @throws java.lang.Exception
     */
    @Test(expected = DataRetrievalException.class)
    public void testBuildNameEmptyNameinQuery() throws Exception {
        String query = "(name:)";
        SpringerUtility.buildNameFromQuery(query);
    }

    /**
     * Test of buildNameFromRequest method, of class SpringerUtility when the
     * name parameter is empty.
     *
     * @throws java.lang.Exception
     */
    @Test(expected = DataRetrievalException.class)
    public void testBuildNameEmptyString() throws Exception {
        String query = "";
        SpringerUtility.buildNameFromQuery(query);
    }

    /**
     * Test of buildNameFromRequest method, of class SpringerUtility when the
     * name parameter is empty.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testHashGeneration() throws Exception {
        String query = "This is my hash.";
        assertEquals("b0051e6655550461f9cf80dd99338049", SpringerUtility.generateHash(query));
    }

}
