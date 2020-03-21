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
package org.apache.marmotta.ucuenca.wk.provider.crossref;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import java.io.ByteArrayInputStream;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.openrdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minidev.json.JSONArray;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Person;
import org.json.JSONException;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;

/**
 * Support Scielo Author information as RDF
 * <p/>
 * Author: Jose Ortiz
 */
@SuppressWarnings("PMD")
public class CrossrefRawProvider extends AbstractHttpProvider {

    private static final int MAX_AUTHORS_PER_PAPER = 20;
    private static Logger log = LoggerFactory.getLogger(CrossrefRawProvider.class);
    public static final int MAX_RESULTS = 500;
    private ConcurrentHashMap<String, Integer> stats = new ConcurrentHashMap<String, Integer>();
    public static final String NAME = "Crossref Raw Provider";
    public static final String PATTERN = "https://search\\.crossref\\.org/search/.*";
    public static final String SEARCHAPI = "https://api.crossref.org/works?query=ecuador+%s&query.author=%s&rows=" + MAX_RESULTS;

    public static final String CROSSREFPREFIX = "http://search.crossref.org/ontology/";
    public static final String CROSSREFBASE = "http://search.crossref.org/data/";
    public static final String CROSSREFBASEPUBLICATION = CROSSREFBASE + "publication/";
    public static final String CROSSREFBASEAUTHOR = CROSSREFBASE + "author/";
    public static final String CROSSREFBASEAFFILIATION = CROSSREFBASE + "affiliation/";

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
        url = String.format(SEARCHAPI, URLEncoder.encode(createSolrQuery(id)), URLEncoder.encode(createSolrQuery(id)));
        stats.put(resource, 0);
        return Collections.singletonList(url);
    }

    @Override
    public List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {
        log.debug("Request Successful to {0}", requestUrl);
        if (requestUrl.startsWith("https://api.crossref.org")) {
            return parseSearchDocs(input, resource, triples);
        }
        return Collections.emptyList();
    }

    private List<String> parseSearchDocs(InputStream input, String resource, Model triples) throws NumberFormatException, DataRetrievalException {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        try {
            //String toString = IOUtils.toString(input);
            byte[] data = IOUtils.toByteArray(input);
            DocumentContext doc = null;
            try {
                doc = JsonPath.parse(new ByteArrayInputStream(data), getConfiguration());
            } catch (Exception e) {
                log.debug("Crossref error", e);
                return Collections.emptyList();
            }
            List<String> nameOrg = Lists.newArrayList(getExtractName(resource));
            Person p1 = new Person();
            p1.Name = new ArrayList<>();
            p1.Name.add(nameOrg);
            List<LinkedHashMap<String, Object>> readJsonObj = readJsonObjt(doc, "$.message.items[*]");
            for (int i = 0; i < readJsonObj.size(); i++) {
                LinkedHashMap<String, Object> get = readJsonObj.get(i);
                String id = get.get("DOI").toString();
                if (id.contains("10.4995/eb.2013.6642")) {
                    int ll = 0;
                }
                String aDocURL = CROSSREFBASEPUBLICATION + URLEncoder.encode(id);
                String aDocURLAu = CROSSREFBASEAUTHOR + URLEncoder.encode(id);
                List<LinkedHashMap<String, Object>> authors = readJsonObjt(doc, "$.message.items[" + i + "].author[*]");
                boolean check = false;
                Model tmpAu = new LinkedHashModel();
                for (int j = 0; j < authors.size(); j++) {
                    LinkedHashMap<String, Object> get1 = authors.get(j);
                    String auUR = aDocURLAu + "_" + j;
                    URI createURI = factory.createURI(auUR);
                    Person p2 = new Person();
                    p2.Name = new ArrayList<>();
                    if (get1.containsKey("family") && get1.containsKey("given")) {
                        p2.Name.add(Lists.newArrayList(get1.get("given").toString(), get1.get("family").toString()));
                    }
                    Boolean checkName = p1.checkName(p2, false);
                    if (checkName != null && checkName) {
                        check = true;
                        tmpAu.add(createURI, OWL.ONEOF, factory.createURI(resource));
                    }
                    tmpAu.add(factory.createURI(aDocURL), factory.createURI(CROSSREFPREFIX + "contributor"), createURI);
                    if (j == 0) {
                        tmpAu.add(factory.createURI(aDocURL), factory.createURI(CROSSREFPREFIX + "creator"), createURI);
                    }
                    addPropertyOR(authors.get(j), "ORCID", CROSSREFPREFIX + "orcid", tmpAu, auUR);
                    addProperty(authors.get(j), "family", CROSSREFPREFIX + "family", tmpAu, auUR);
                    addProperty(authors.get(j), "given", CROSSREFPREFIX + "given", tmpAu, auUR);
                    addPropertyArraySub(authors.get(j), "affiliation", CROSSREFPREFIX + "affiliation", tmpAu, auUR, "name");
                }
                if (check && authors.size() > MAX_AUTHORS_PER_PAPER) {
                    log.info("Ignoring {} it has more than 20 authors", aDocURL);
                    check = false;
                }
                if (check) {
                    addProperty(get, "DOI", CROSSREFPREFIX + "doi", tmpAu, aDocURL);
                    addPropertyArray(get, "ISSN", CROSSREFPREFIX + "issn", tmpAu, aDocURL);
                    addPropertyArray(get, "ISBN", CROSSREFPREFIX + "isbn", tmpAu, aDocURL);
                    addProperty(get, "URL", CROSSREFPREFIX + "uri", tmpAu, aDocURL);
                    addPropertyArray(get, "title", CROSSREFPREFIX + "title", tmpAu, aDocURL);
                    addProperty(get, "abstract", CROSSREFPREFIX + "abstract", tmpAu, aDocURL);

                    addPropertyArray(get, "alternative-id", CROSSREFPREFIX + "doi", tmpAu, aDocURL);

                    addPropertyArray(get, "container-title", CROSSREFPREFIX + "collection", tmpAu, aDocURL);
                    addPropertyType(get, "type", CROSSREFPREFIX + "type", tmpAu, aDocURL);

                    addProperty(get, "publisher", CROSSREFPREFIX + "publisher", tmpAu, aDocURL);
                    
                    addPropertySub(get, "event", CROSSREFPREFIX + "event", tmpAu, aDocURL, "name");

                    addPropertyArraySub(get, "link", CROSSREFPREFIX + "uri", tmpAu, aDocURL, "URL");

                    addProperty(get, "issue", CROSSREFPREFIX + "issue", tmpAu, aDocURL);
                    addProperty(get, "page", CROSSREFPREFIX + "page", tmpAu, aDocURL);
                    addProperty(get, "volume", CROSSREFPREFIX + "volume", tmpAu, aDocURL);
                    addProperty(get, "language", CROSSREFPREFIX + "language", tmpAu, aDocURL);

                    addPropertyArray(get, "subject", CROSSREFPREFIX + "subject", tmpAu, aDocURL);

                    addPropertyArraySubDate(get, "issued", CROSSREFPREFIX + "date", tmpAu, aDocURL, "date-parts");

                    triples.addAll(tmpAu);
                }
            }
        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing HTML response", e);
        } catch (Exception e) {
            throw new DataRetrievalException("could not parse XML response. It is not in proper XML format", e);
        }
        return Collections.emptyList();
    }

    private String[] getExtractName(String resource) {
        String id = URLDecoder.decode(resource.substring(resource.lastIndexOf('/') + 1)).replaceAll("-", " ");
        return id.split("_");
    }

    private String createSolrQuery(String id) {
        String[] split = id.split("_");
        String query = "";
        for (int i = 0; i < split.length; i++) {
            List<String> ls = Lists.newArrayList(split[i].split("-"));
            String queryP = "";
            //if (ls.size() > 1) {
            // queryP += "(";
            //}
            for (int j = 0; j < ls.size(); j++) {
                String n = ls.get(j);
                String tk = (n.length() <= 2) ? n + "" : n;
                queryP += tk.toLowerCase() + (j == ls.size() - 1 ? " " : " ");
            }
            //if (ls.size() > 1) {
            // queryP += ")";
            //}
            query += queryP + (i == split.length - 1 ? " " : " ");
        }
        return "" + query + "";
    }

    private List<String> readJsonStr(DocumentContext jsonDocument, String query) {
        return jsonDocument.read(query);
    }

    private List<LinkedHashMap<String, String>> readJsonObj(DocumentContext jsonDocument, String query) {
        return jsonDocument.read(query);
    }

    private List<LinkedHashMap<String, Object>> readJsonObjt(DocumentContext jsonDocument, String query) {
        return jsonDocument.read(query);
    }

    private void addProperty(Map<String, Object> data, String prop, String ontProp, Model triples, String doc) {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        Object get = data.get(prop);
        if (get != null && !get.toString().trim().isEmpty()) {
            Literal createLiteral = factory.createLiteral(get.toString());
            URI propU = factory.createURI(ontProp);
            URI docU = factory.createURI(doc);
            triples.add(docU, propU, createLiteral);
        }
    }
    
    private void addPropertyOR(Map<String, Object> data, String prop, String ontProp, Model triples, String doc) {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        Object get = data.get(prop);
        if (get != null && !get.toString().trim().isEmpty()) {
            Literal createLiteral = factory.createLiteral(get.toString().replaceAll("http://orcid.org/", "").replaceAll("https://orcid.org/", ""));
            URI propU = factory.createURI(ontProp);
            URI docU = factory.createURI(doc);
            triples.add(docU, propU, createLiteral);
        }
    }
    
    private void addPropertySub(Map<String, Object> data, String prop, String ontProp, Model triples, String doc, String na) {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        Map<String, String> get = (Map<String, String>) data.get(prop);
        if (get != null && !get.toString().trim().isEmpty()) {
            Literal createLiteral = factory.createLiteral(get.get(na));
            URI propU = factory.createURI(ontProp);
            URI docU = factory.createURI(doc);
            triples.add(docU, propU, createLiteral);
        }
    }

    private void addPropertyType(Map<String, Object> data, String prop, String ontProp, Model triples, String doc) {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        Object get = data.get(prop);
        if (get != null && !get.toString().trim().isEmpty()) {
            String typ = get.toString();
            URI createLiteral = factory.createURI("http://purl.org/ontology/bibo/Journal");
            switch (typ) {
                case "journal-article":
                case "journal-volume":
                case "journal-issue":
                case "journal":
                    createLiteral = factory.createURI("http://purl.org/ontology/bibo/Journal");
                    break;
                case "book-part":
                case "book":
                case "book-set":
                case "book-chapter":
                case "reference-book":
                case "book-series":
                case "edited-book":
                case "book-section":
                case "book-track":
                    createLiteral = factory.createURI("http://purl.org/ontology/bibo/Book");
                    break;
                case "proceedings-article":
                case "proceedings-series":
                case "proceedings":
                    createLiteral = factory.createURI("http://purl.org/ontology/bibo/Proceedings");
                    break;

                case "report-series":
                case "standard-series":
                    createLiteral = factory.createURI("http://purl.org/ontology/bibo/Series");
                    break;
            }
            URI propU = factory.createURI(ontProp);
            URI docU = factory.createURI(doc);
            triples.add(docU, propU, createLiteral);
        }
    }

    private void addPropertyArray(Map<String, Object> data, String prop, String ontProp, Model triples, String doc) throws JSONException {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        JSONArray fm = (JSONArray) data.get(prop);
        if (fm != null) {
            for (int iw = 0; iw < fm.size(); iw++) {
                String get = fm.get(iw).toString();
                if (get != null && !get.trim().isEmpty()) {
                    Literal createLiteral = factory.createLiteral(get);
                    URI propU = factory.createURI(ontProp);
                    URI docU = factory.createURI(doc);
                    triples.add(docU, propU, createLiteral);
                }
            }
        }
    }

    private void addPropertyArraySub(Map<String, Object> data, String prop, String ontProp, Model triples, String doc, String su) throws JSONException {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        JSONArray fm = (JSONArray) data.get(prop);
        if (fm != null) {
            for (int iw = 0; iw < fm.size(); iw++) {
                String get = ((LinkedHashMap<String, String>) fm.get(iw)).get(su);
                if (get != null && !get.trim().isEmpty()) {
                    Literal createLiteral = factory.createLiteral(get);
                    URI propU = factory.createURI(ontProp);
                    URI docU = factory.createURI(doc);
                    triples.add(docU, propU, createLiteral);
                }
            }
        }
    }

    private void addPropertyArraySubDate(Map<String, Object> data, String prop, String ontProp, Model triples, String doc, String su) throws JSONException {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        JSONArray fm = (JSONArray) ((Map<String, Object>) data.get(prop)).get(su);
        if (fm != null) {
            for (int iw = 0; iw < fm.size(); iw++) {

                JSONArray getx = ((JSONArray) fm.get(iw));

                String get = "";
                for (Iterator<Object> it = getx.iterator(); it.hasNext();) {
                    Object next2 = it.next();
                    if (next2 != null) {
                        String next = next2.toString();
                        next = (next.length() <= 1 ? "0" : "") + next;
                        get += next + (it.hasNext() ? "-" : "");
                    }
                }

                if (get != null && !get.trim().isEmpty()) {
                    Literal createLiteral = factory.createLiteral(get);
                    URI propU = factory.createURI(ontProp);
                    URI docU = factory.createURI(doc);
                    triples.add(docU, propU, createLiteral);
                }
            }
        }
    }

    protected Configuration getConfiguration() {
        return Configuration.defaultConfiguration()
                .addOptions(Option.ALWAYS_RETURN_LIST)
                .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL)
                .addOptions(Option.SUPPRESS_EXCEPTIONS);
    }
}
