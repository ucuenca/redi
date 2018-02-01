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
package ec.edu.cedia.redi.ldclient.test.scholar;

import org.apache.marmotta.ldclient.test.provider.ProviderTestBase;
import org.junit.Test;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class TestScholarProvider extends ProviderTestBase {

    /**
     * This method tests accessing Google Scholar and gets a profile with a
     * publication..
     *
     * @throws Exception
     */
    @Test
    public void testScholarProfile() throws Exception {
        String url = "https://scholar.google.com/citations?mauthors=arevalo+univesidad+de+cuenca&hl=en&view_op=search_authors";
        testResource(url, "scholar-profile.sparql");
    }

}
