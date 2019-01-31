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
package org.apache.marmotta.ucuenca.wk.provider.orcid;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.IOException;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.openrdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.xpath.XPathFactory;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;

/**
 * Support ORCID Author information as RDF
 * <p/>
 * Author: Jose Ortiz
 */
public class ORCIDRawProvider extends AbstractHttpProvider {

    private static Logger log = LoggerFactory.getLogger(ORCIDRawProvider.class);
    private ConcurrentHashMap<String, Integer> stats = new ConcurrentHashMap<String, Integer>();
    public static final String NAME = "ORCID Raw Provider";
    public static final String PATTERN = "https://orcid\\.org/search/.*";
    public static final String SEARCHAPI = "https://pub.orcid.org/v2.1/search/?q=%s";
    public static final String DESCRIBEAPI = "https://pub.orcid.org/v2.0/%s/record";

    public static final String ORCIDPREFIX = "https://orcid.org/ontology/";

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
        String id = URLDecoder.decode(resource.substring(resource.lastIndexOf('/') + 1));
        url = String.format(SEARCHAPI, URLEncoder.encode(createSolrQuery(id)));
        stats.put(resource, 0);
        return Collections.singletonList(url);
    }

    @Override
    public List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {
        log.debug("Request Successful to {0}", requestUrl);
        if (requestUrl.startsWith("https://pub.orcid.org/v2.1/search")) {
            return parseSearchAuthors(input, resource, triples);
        } else if (requestUrl.startsWith("https://pub.orcid.org/v2.0/") && requestUrl.endsWith("/record")) {
            return parseAuthor(input, requestUrl, triples);
        } else if (requestUrl.startsWith("https://pub.orcid.org/v2.1/") && requestUrl.contains("/work/")) {
            return parseDocs(input, requestUrl, triples);
        }
        return Collections.emptyList();
    }

    private List<String> parseDocs(InputStream input, String requestUrl, Model triples) throws DataRetrievalException {
        try {
            List<String> lsURLs = new ArrayList<>();
            Document doc = new SAXBuilder(XMLReaders.NONVALIDATING).build(input);

            mapProp(true, requestUrl, ORCIDPREFIX + "title", triples, doc, "/work:work/work:title/common:title");
            mapProp(true, requestUrl, ORCIDPREFIX + "url", triples, doc, "/work:work/work:url");
            mapPropRe(requestUrl, ORCIDPREFIX + "creator", ORCIDPREFIX + "contributors", ORCIDPREFIX + "name", triples, doc, "/work:work/work:contributors/work:contributor/work:credit-name");

            mapProp(true, requestUrl, ORCIDPREFIX + "date", triples, doc, "/work:work/common:publication-date/common:year");
            mapProp(true, requestUrl, ORCIDPREFIX + "type", triples, doc, "/work:work/work:type");

            mapProp(true, requestUrl, ORCIDPREFIX + "collection", triples, doc, "/work:work/work:journal-title");

            mapProp(true, requestUrl, ORCIDPREFIX + "doi", triples, doc, "/work:work/common:external-ids/common:external-id[common:external-id-type='doi']/common:external-id-value");
            mapProp(true, requestUrl, ORCIDPREFIX + "issn", triples, doc, "/work:work/common:external-ids/common:external-id[common:external-id-type='issn']/common:external-id-value");
            mapProp(true, requestUrl, ORCIDPREFIX + "isbn", triples, doc, "/work:work/common:external-ids/common:external-id[common:external-id-type='isbn']/common:external-id-value");

            return lsURLs;
        } catch (JDOMException e) {
            throw new DataRetrievalException("could not parse XML response. It is not in proper XML format", e);
        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing HTML response", e);
        }
    }

    private List<String> parseAuthor(InputStream input, String requestUrl, Model triples) throws DataRetrievalException {
        try {
            ValueFactory factory = ValueFactoryImpl.getInstance();
            List<String> lsURLs = new ArrayList<>();
            Document doc = new SAXBuilder(XMLReaders.NONVALIDATING).build(input);
            mapProp(true, requestUrl, ORCIDPREFIX + "familyName", triples, doc, "/record:record/person:person/person:name/personal-details:family-name");
            mapProp(true, requestUrl, ORCIDPREFIX + "givenName", triples, doc, "/record:record/person:person/person:name/personal-details:given-names");
            mapProp(true, requestUrl, ORCIDPREFIX + "name", triples, doc, "/record:record/person:person/person:name/personal-details:credit-name");
            mapProp(true, requestUrl, ORCIDPREFIX + "name", triples, doc, "/record:record/person:person/other-name:other-names/other-name:other-name/other-name:content");
            //MapLiteral(requestUrl, ORCIDPREFIX+"name", triples, doc, "/record:record/person:person/other-name:other-names/other-name:other-name/common:source/common:source-name");
            mapProp(true, requestUrl, ORCIDPREFIX + "mail", triples, doc, "/record:record/person:person/email:emails/email:email/email:email");
            mapProp(true, requestUrl, ORCIDPREFIX + "account", triples, doc, "/record:record/person:person/external-identifier:external-identifiers/external-identifier:external-identifier/common:external-id-url");
            mapProp(true, requestUrl, ORCIDPREFIX + "topics", triples, doc, "/record:record/person:person/keyword:keywords/keyword:keyword/keyword:content");
            mapProp(true, requestUrl, ORCIDPREFIX + "account", triples, doc, "/record:record/person:person/researcher-url:researcher-urls/researcher-url:researcher-url/researcher-url:url");

            mapProp(true, requestUrl, ORCIDPREFIX + "aff", triples, doc, "/record:record/activities:activities-summary/activities:educations/education:education-summary/education:organization/common:name");
            mapProp(true, requestUrl, ORCIDPREFIX + "aff", triples, doc, "/record:record/activities:activities-summary/activities:employments/employment:employment-summary/employment:organization/common:name");

            List<String> att = getAtt(doc, "/record:record/activities:activities-summary/activities:works/activities:group/work:work-summary", "put-code");
            for (String wrks : att) {
                String replaceAll = requestUrl.replaceAll("/record", "/work/%s").replaceAll("/v2.0", "/v2.1");
                String urlWoks = String.format(replaceAll, wrks);
                lsURLs.add(urlWoks);
                triples.add(factory.createStatement(factory.createURI(requestUrl), factory.createURI(ORCIDPREFIX + "publications"), factory.createURI(urlWoks)));
            }
            return lsURLs;
        } catch (JDOMException e) {
            throw new DataRetrievalException("could not parse XML response. It is not in proper XML format", e);
        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing HTML response", e);
        }
    }

    private List<String> parseSearchAuthors(InputStream input, String resource, Model triples) throws DataRetrievalException {
        try {
            List<String> lsURLs = new ArrayList<>();
            ValueFactory factory = ValueFactoryImpl.getInstance();
            Document doc = new SAXBuilder(XMLReaders.NONVALIDATING).build(input);
            for (Element element : queryElements(doc, "/search:search/search:result/common:orcid-identifier/common:path")) {
                String urlNewRs = String.format(DESCRIBEAPI, element.getText());
                triples.add(factory.createStatement(factory.createURI(urlNewRs), OWL.ONEOF, factory.createURI(resource)));
                triples.add(factory.createStatement(factory.createURI(urlNewRs), factory.createURI(ORCIDPREFIX + "orcid"), factory.createLiteral(element.getText())));
                lsURLs.add(urlNewRs);
            }
            return lsURLs;
        } catch (JDOMException e) {
            throw new DataRetrievalException("could not parse XML response. It is not in proper XML format", e);
        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing HTML response", e);
        }
    }

    private String createSolrQuery(String id) {
        String[] split = id.split("_");
        String queryFN = "";
        String queryLN = "";
        for (int i = 0; i < split.length; i++) {
            String query = "";
            List<String> ls = Lists.newArrayList(split[i].split("-"));
            String queryP = "";
            for (int j = 0; j < ls.size(); j++) {
                String n = ls.get(j).trim();
                String tk = n;
                queryP += tk.toLowerCase() + (j == ls.size() - 1 ? "" : " ");
            }
            query += queryP;
            if (i == 0) {
                queryFN = query;
            } else {
                queryLN = query;
            }
        }
        return "family-name:" + queryLN + " AND given-names:" + queryFN;
    }

    protected static List<Element> queryElements(Document n, String query) {
        return XPathFactory.instance().compile(query, new ElementFilter(), null,
                Namespace.getNamespace("external-identifier", "http://www.orcid.org/ns/external-identifier"),
                Namespace.getNamespace("email", "http://www.orcid.org/ns/email"),
                Namespace.getNamespace("other-name", "http://www.orcid.org/ns/other-name"),
                Namespace.getNamespace("personal-details", "http://www.orcid.org/ns/personal-details"),
                Namespace.getNamespace("person", "http://www.orcid.org/ns/person"),
                Namespace.getNamespace("record", "http://www.orcid.org/ns/record"),
                Namespace.getNamespace("search", "http://www.orcid.org/ns/search"),
                Namespace.getNamespace("keyword", "http://www.orcid.org/ns/keyword"),
                Namespace.getNamespace("employment", "http://www.orcid.org/ns/employment"),
                Namespace.getNamespace("researcher-url", "http://www.orcid.org/ns/researcher-url"),
                Namespace.getNamespace("activities", "http://www.orcid.org/ns/activities"),
                Namespace.getNamespace("education", "http://www.orcid.org/ns/education"),
                Namespace.getNamespace("work", "http://www.orcid.org/ns/work"),
                Namespace.getNamespace("common", "http://www.orcid.org/ns/common")
        ).evaluate(n);
    }

    private void mapProp(boolean literal, String rs, String prop, Model tr, Document dc, String qr) {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        URI createURI = factory.createURI(rs);
        URI createURI1 = factory.createURI(prop);
        List<Element> queryElements = queryElements(dc, qr);
        for (Element e : queryElements) {
            Value createObj = null;
            if (literal) {
                createObj = factory.createLiteral(e.getText());
            } else {
                createObj = factory.createURI(e.getText());
            }
            tr.add(createURI, createURI1, createObj);
        }
    }

    private List<String> getAtt(Document dc, String qr, String fl) {
        List<String> ls = new ArrayList<>();
        List<Element> queryElements = queryElements(dc, qr);
        for (Element e : queryElements) {
            ls.add(e.getAttribute(fl).getValue());
        }
        return ls;
    }

    private void mapPropRe(String rs, String prop0, String prop, String prop2, Model tr, Document dc, String qr) {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        URI createURI = factory.createURI(rs);
        URI createURI1 = factory.createURI(prop);
        URI createURI2 = factory.createURI(prop2);
        URI createURI3 = factory.createURI(prop0);
        List<Element> queryElements = queryElements(dc, qr);
        int i = 0;
        for (Element e : queryElements) {
            URI createObj = factory.createURI(rs + "_" + i);
            tr.add(createURI, createURI1, createObj);
            if (i == 0) {
                tr.add(createURI, createURI3, createObj);
            }
            tr.add(createObj, createURI2, factory.createLiteral(e.getText()));
            i++;
        }
    }

}
