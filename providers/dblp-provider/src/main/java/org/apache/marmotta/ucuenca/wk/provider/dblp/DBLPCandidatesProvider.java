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
package org.apache.marmotta.ucuenca.wk.provider.dblp;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.vocabulary.FOAF;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.provider.xml.AbstractXMLDataProvider;
import org.apache.marmotta.ldclient.provider.xml.mapping.XPathValueMapper;
import org.apache.marmotta.ucuenca.wk.provider.dblp.mapper.DBLPURIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Support DBLP Author candidate search
 * <p/>
 * Author: Santiago Gonzalez
 */
@Deprecated
public class DBLPCandidatesProvider extends AbstractXMLDataProvider implements DataProvider {
	
    public static final String NAME = "DBLP Candidates Provider";
    public static final String API = "http://dblp.uni-trier.de/search/author?xauthor=%s";
    public static final String PATTERN = "http(s?)://rdf\\.dblp\\.com/ns/candidates/.*";
    public static final String NS_CANDIDATES = "http://rdf.dblp.com/ns/candidates/";

    private static ConcurrentMap<String,XPathValueMapper> mediaOntMappings = new ConcurrentHashMap<String, XPathValueMapper>();
    static {
        mediaOntMappings.put(FOAF.member.stringValue(), new DBLPURIMapper(DBLPAuthorProvider.NS_AUTHOR, "/authors/author/@urlpt"));
    }

    private static Logger log = LoggerFactory.getLogger(DBLPCandidatesProvider.class);

    /**
     * Return the name of this data provider. To be used e.g. in the configuration and in log messages.
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
        return new String[] {
                "text/xml"
        };
    }

    /**
     * Build the URL to use to call the webservice in order to retrieve the data for the resource passed as argument.
     * In many cases, this will just return the URI of the resource (e.g. Linked Data), but there might be data providers
     * that use different means for accessing the data for a resource, e.g. SPARQL or a Cache.
     *
     *
     * @param resource
     * @param endpoint endpoint configuration for the data provider (optional)
     * @return
     */
    @Override
    public List<String> buildRequestUrl(String resource, Endpoint endpoint) {
    	Preconditions.checkState(StringUtils.isNotBlank(resource));
        String id = resource.substring(resource.lastIndexOf('/') + 1);
        String url = String.format(API, id.replace('_', '+'));
        log.debug("API URI for Candidates {0}", url);
        return Collections.singletonList(url);
    }

    /**
     * Return a mapping table mapping from RDF properties to XPath Value Mappers. Each entry in the map is evaluated
     * in turn; in case the XPath expression yields a result, the property is added for the processed resource.
     *
     * @return
     * @param requestUrl
     */
    @Override
    protected Map<String, XPathValueMapper> getXPathMappings(String requestUrl) {
        return mediaOntMappings;
    }

    /**
     * Return a list of URIs that should be added as types for each processed resource.
     *
     * @return
     * @param resource
     */
    @Override
    protected List<String> getTypes(org.openrdf.model.URI resource) {
        return ImmutableList.of(FOAF.Group.stringValue());
    }

}
