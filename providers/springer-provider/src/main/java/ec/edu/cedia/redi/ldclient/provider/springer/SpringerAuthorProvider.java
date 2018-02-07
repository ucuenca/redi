/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ec.edu.cedia.redi.ldclient.provider.springer;

import ec.edu.cedia.redi.ldclient.provider.json.AbstractJSONDataProvider;
import ec.edu.cedia.redi.ldclient.provider.json.mappers.JsonPathValueMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.openrdf.model.URI;

/**
 * Support Springer information as JSON. Be aware that the provider makes extra
 * calls to retrieve publications of each author found.
 * <p>
 * @see <a href="https://dev.springer.com">Documentation guide</a>.
 *
 * @author Xavier Sumba
 */
public class SpringerProvider extends AbstractJSONDataProvider implements DataProvider {

    public static final String NAME = "Springer Provider";
    public static final String PATTERN = "https://springer\\.cognitive\\.microsoft\\.com/v1\\.0/evaluate.expr=Composite\\(AA.AuId=(.*)\\)&.*";
    /**
     * Default Springer for resources.
     */
    public static final String SPRINGER_URL = "https://springer.com/";

    private final ConcurrentMap<String, JsonPathValueMapper> ontologyMapping = new ConcurrentHashMap<>();
    private final String templatePublication = "https://westus.api.cognitive.microsoft.com/academic/v1.0/evaluate?"
            + "expr=Composite(AA.AuId=%s)&attributes=Id,Ti,L,Y,D,CC,ECC,AA.AuN,AA.AuId,AA.AfN,AA.AfId,AA.S,F.FN,F.FId,"
            + "J.JN.J.JId,C.CN,C.CId,RId,W,E&model=latest&subscription-key=%s";

    private String apiKey;

    /**
     * Return the name of this data provider. To be used e.g. in the
     * configuration and in log messages.
     *
     * @return
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Return the list of mime types accepted by this data provider.
     *
     * @return
     */
    @Override
    public String[] listMimeTypes() {
        return new String[]{
            "application/json"
        };
    }

    /**
     * Build the URL to use to call the web service in order to retrieve the
     * data for the resource passed as argument. In many cases, this will just
     * return the URI of the resource (e.g. Linked Data), but there might be
     * data providers that use different means for accessing the data for a
     * resource, e.g. SPARQL or a Cache.
     *
     *
     * @param resource
     * @param endpoint endpoint configuration for the data provider (optional)
     * @return
     */
    @Override
    public List<String> buildRequestUrl(String resource, Endpoint endpoint) {

        return Collections.emptyList();
    }

    /**
     * Returns an empty list. Springer describes URIs with an Id, so there is
     * not difference between resources. All types are assinged in
     * {@link #parseResponse}
     *
     * @param resource
     * @return
     */
    @Override
    protected final List<String> getTypes(URI resource) {
        return Collections.emptyList();
    }

    @Override
    protected Map<String, JsonPathValueMapper> getMappings(String string, String string1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
