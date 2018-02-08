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
import org.openrdf.model.impl.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Person;
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

    public static final String MOCKSCIELOONT = "https://search.scielo.org/ont/";
    public static final String MOCKSCIELOBASE = "https://search.scielo.org/";

    private static final String URICONST = "uri";
    private static final String JOURNALCONST = "journal";

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
            final Document doc = new SAXBuilder(XMLReaders.NONVALIDATING).build(input);
            for (Element element : queryElements(doc, "/response/result/doc")) {
                map(element, triples, resource, getName(resource));

            }

        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing HTML response", e);
        } catch (JDOMException e) {
            throw new DataRetrievalException("could not parse XML response. It is not in proper XML format", e);
        }
        return Collections.emptyList();
    }

    private static void map(Element element, Model model, String queryURI, String mainAuthorName) {
        ValueFactoryImpl instance = ValueFactoryImpl.getInstance();

        String paperID = getValue(element, "id");
        String mockPaperURI = MOCKSCIELOBASE + "publication/" + paperID;
        //Autores
        List<String> authors = getValues(element, "au");
        for (int i = 0; i < authors.size(); i++) {
            String mockAuthorURI = MOCKSCIELOBASE + "author/" + paperID + "_" + i;
            model.add(instance.createURI(mockAuthorURI), instance.createURI(MOCKSCIELOONT + "publications"), instance.createURI(mockPaperURI));
            model.add(instance.createURI(mockAuthorURI), instance.createURI(MOCKSCIELOONT + "name"), instance.createLiteral(authors.get(i)));
            if (i == 0) {
                model.add(instance.createURI(mockPaperURI), instance.createURI(MOCKSCIELOONT + "creator"), instance.createURI(mockAuthorURI));
            }
            //Si comparacion nombre
            Person mockPerson1 = new Person();
            ArrayList<String> namePerson1 = Lists.newArrayList(authors.get(i));
            mockPerson1.Name = Lists.newArrayList();
            mockPerson1.Name.add(namePerson1);
            Person mockPerson2 = new Person();
            ArrayList<String> namePerson2 = Lists.newArrayList(mainAuthorName);
            mockPerson2.Name = Lists.newArrayList();
            mockPerson2.Name.add(namePerson2);

            if (mockPerson1.checkName(mockPerson2)) {
                model.add(instance.createURI(mockAuthorURI), OWL.ONEOF, instance.createURI(queryURI));
            }

            //subject_area  wok_subject_categories
            mapProperty(element, mockAuthorURI, "subject_area", MOCKSCIELOONT + "interestArea", null, model);
            mapProperty(element, mockAuthorURI, "wok_subject_categories", MOCKSCIELOONT + "interestArea", null, model);

        }
        //Titulos
        mapProperty(element, mockPaperURI, "ti", MOCKSCIELOONT + "title", null, model);
        mapProperty(element, mockPaperURI, "ti_es", MOCKSCIELOONT + "title", "es", model);
        mapProperty(element, mockPaperURI, "ti_en", MOCKSCIELOONT + "title", "en", model);

        //Abtract
        mapProperty(element, mockPaperURI, "ab", MOCKSCIELOONT + "abstract", null, model);
        mapProperty(element, mockPaperURI, "ab_es", MOCKSCIELOONT + "abstract", "es", model);
        mapProperty(element, mockPaperURI, "ab_en", MOCKSCIELOONT + "abstract", "en", model);

        //Keyword
        mapProperty(element, mockPaperURI, "keyword", MOCKSCIELOONT + "keywords", null, model);
        mapProperty(element, mockPaperURI, "keyword_es", MOCKSCIELOONT + "keywords", "es", model);
        mapProperty(element, mockPaperURI, "keyword_en", MOCKSCIELOONT + "keywords", "en", model);

        //uri
        mapProperty(element, mockPaperURI, "fulltext_pdf", MOCKSCIELOONT + URICONST, null, model);
        mapProperty(element, mockPaperURI, "fulltext_pdf_en", MOCKSCIELOONT + URICONST, null, model);
        mapProperty(element, mockPaperURI, "fulltext_pdf_es", MOCKSCIELOONT + URICONST, null, model);
        mapProperty(element, mockPaperURI, "fulltext_html", MOCKSCIELOONT + URICONST, null, model);
        mapProperty(element, mockPaperURI, "fulltext_html_en", MOCKSCIELOONT + URICONST, null, model);
        mapProperty(element, mockPaperURI, "fulltext_html_es", MOCKSCIELOONT + URICONST, null, model);

        //pages
        mapProperty(element, mockPaperURI, "pg", MOCKSCIELOONT + "pages", null, model);
        mapProperty(element, mockPaperURI, "start_page", MOCKSCIELOONT + "pagesS", null, model);
        mapProperty(element, mockPaperURI, "end_page", MOCKSCIELOONT + "pagesE", null, model);

        //Issue
        mapProperty(element, mockPaperURI, "issue", MOCKSCIELOONT + "issue", null, model);

        //Journal
        mapProperty(element, mockPaperURI, "journal_title", MOCKSCIELOONT + JOURNALCONST, null, model);
        mapProperty(element, mockPaperURI, "ta", MOCKSCIELOONT + JOURNALCONST, null, model);

        //Journal
        mapProperty(element, mockPaperURI, "journal_title", MOCKSCIELOONT + JOURNALCONST, null, model);
        mapProperty(element, mockPaperURI, "ta", MOCKSCIELOONT + JOURNALCONST, null, model);

        //ISSN
        mapProperty(element, mockPaperURI, "issn", MOCKSCIELOONT + "issn", null, model);
        //DOI
        mapProperty(element, mockPaperURI, "doi", MOCKSCIELOONT + "doi", null, model);

        //Date
        mapProperty(element, mockPaperURI, "da", MOCKSCIELOONT + "date", null, model);

    }

    private static void mapProperty(Element e, String uri, String field, String propertyOnt, String lang, Model m) {
        ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
        List<String> values = getValues(e, field);
        for (int i = 0; i < values.size(); i++) {
            m.add(instance.createURI(uri), instance.createURI(propertyOnt), instance.createLiteral(values.get(i), lang));
        }
    }

    public static String getValue(Element m, String key) {
        List<String> values = getValues(m, key);
        assert values.size() == 1;
        return values.get(0);
    }

    private String getName(String uri) {
        String id = uri.substring(uri.lastIndexOf('/') + 1).replaceAll("_", " ").replaceAll("-", " ");
        return id.trim();
    }

    protected static List<Element> queryElements(Document n, String query) {
        return XPathFactory.instance().compile(query, new ElementFilter()).evaluate(n);
    }

    public static List<String> getValues(Element m, String key) {
        List<String> v = new ArrayList<>();
        List<Element> children = m.getChildren();
        for (Element aE : children) {
            if (aE.getAttributeValue("name").equals(key)) {
                if (aE.getName().equals("str")) {
                    v.add(aE.getTextTrim());
                } else {
                    List<Element> children1 = aE.getChildren();
                    for (Element aE2 : children1) {
                        v.add(aE2.getText());
                    }
                }

            }
        }
        return v;
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
