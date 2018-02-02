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
package org.apache.marmotta.ucuenca.wk.provider.scielo;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.xpath.XPathFactory;
import org.openrdf.model.Model;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.marmotta.ucuenca.wk.commons.function.URLUtils;
import org.openrdf.model.vocabulary.OWL;

/**
 * Support Scielo Author information as RDF
 * <p/>
 * Author: Jose Ortiz
 */
public class ScieloRawProvider extends AbstractHttpProvider {

    public static final String NAME = "Scielo Raw Provider";
    public static final String API = "https://search.scielo.org/?q=%s&output=xml";
    public static final String PATTERN = "https://search\\.scielo\\.org/search/.*";

    private static Logger log = LoggerFactory.getLogger(ScieloRawProvider.class);

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
            "text/xml"
        };
    }

    /**
     * Build the URL to use to call the webservice in order to retrieve the data
     * for the resource passed as argument. In many cases, this will just return
     * the URI of the resource (e.g. Linked Data), but there might be data
     * providers that use different means for accessing the data for a resource,
     * e.g. SPARQL or a Cache.
     *
     *
     * @param resource
     * @param endpoint endpoint configuration for the data provider (optional)
     * @return
     */
    @Override
    public List<String> buildRequestUrl(String resource, Endpoint endpoint) {
        String url = null;
        Preconditions.checkState(StringUtils.isNotBlank(resource));
        String id = resource.substring(resource.lastIndexOf('/') + 1);
        url = String.format(API, URLEncoder.encode(getQuery(id)));
        return Collections.singletonList(url);
    }

    @Override
    public List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {
        log.debug("Request Successful to {0}", requestUrl);
        try {
            List<String> candidates = new ArrayList<String>();
            ValueFactory factory = ValueFactoryImpl.getInstance();
            final Document doc = new SAXBuilder(XMLReaders.NONVALIDATING).build(input);
            for (Element element : queryElements(doc, "/response/result/doc")) {
                String candidate = element.getText();
                candidate = candidate.replaceFirst("pid", "rec/pid");
                candidate = URLUtils.getFinalURL(candidate, 0);
                triples.add(factory.createStatement(factory.createURI(candidate), OWL.ONEOF, factory.createURI(resource)));
                candidates.add(candidate);
            }

        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing HTML response", e);
        } catch (JDOMException e) {
            throw new DataRetrievalException("could not parse XML response. It is not in proper XML format", e);
        }
        return Collections.emptyList();
    }

    protected static List<Element> queryElements(Document n, String query) {
        return XPathFactory.instance().compile(query, new ElementFilter()).evaluate(n);
    }

    private String getQuery(String id) {
        String[] split = id.split("_");
        String query = "";
        for (int i = 0; i < split.length; i++) {
            List<String> ls = Lists.newArrayList(split[i].split("-"));
            String queryP = "";
            queryP += "(au:(";
            for (int j = 0; j < ls.size(); j++) {
                String n = ls.get(j);
                String tk = (n.length() == 1) ? n + "*" : n;
                queryP += tk.toLowerCase() + (j == ls.size() - 1 ? " " : " OR ");
            }
            queryP += "))";

            query += queryP + (i == split.length - 1 ? " " : " AND ");
        }
        return query;
    }

}