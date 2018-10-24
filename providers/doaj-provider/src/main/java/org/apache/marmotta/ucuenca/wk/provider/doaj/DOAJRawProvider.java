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
package org.apache.marmotta.ucuenca.wk.provider.doaj;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Person;
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
public class DOAJRawProvider extends AbstractHttpProvider {

    private static Logger log = LoggerFactory.getLogger(DOAJRawProvider.class);
    public static final int MAX_RESULTS = 1000;
    private ConcurrentHashMap<String, Integer> stats = new ConcurrentHashMap<String, Integer>();
    public static final String NAME = "DOAJ Raw Provider";
    public static final String PATTERN = "https://doaj\\.org/search/.*";
    public static final String SEARCHAPI = "https://doaj.org/api/v1/search/articles/%s?pageSize=" + MAX_RESULTS;
    public static final String DESCRIBEAPI = "https://doaj.org/article/%s";
    public static final String DOAJPREFIX = "https://doaj.org/ontology/";

    private static final String NAMEFIELD = "name";
    private static final int MAX_AUTHORS_PER_PAPER = 20;
    private static final String MAIN_NODE = "$.results[";

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
        if (requestUrl.startsWith("https://doaj.org")) {
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
                log.debug("DOAJ error", e);
                return Collections.emptyList();
            }
            List<String> nameOrg = Lists.newArrayList(getExtractName(resource));
            Person p1 = new Person();
            p1.Name = new ArrayList<>();
            p1.Name.add(nameOrg);
            List<LinkedHashMap<String, String>> readJsonObj = readJsonObj(doc, "$.results[*]");
            for (int i = 0; i < readJsonObj.size(); i++) {
                LinkedHashMap<String, String> get = readJsonObj.get(i);
                String id = get.get("id");
                String aDocURL = String.format(DESCRIBEAPI, id);
                List<LinkedHashMap<String, String>> readJsonObj1 = readJsonObj(doc, MAIN_NODE + i + "].bibjson.author[*]");
                boolean check = false;
                Model tmpAu = new LinkedHashModel();
                for (int j = 0; j < readJsonObj1.size(); j++) {
                    LinkedHashMap<String, String> get1 = readJsonObj1.get(j);
                    String auUR = aDocURL + "_" + j;
                    URI createURI = factory.createURI(auUR);
                    Person p2 = new Person();
                    p2.Name = new ArrayList<>();
                    if (get1.containsKey(NAMEFIELD)) {
                        p2.Name.add(Lists.newArrayList(get1.get(NAMEFIELD)));
                    }
                    Boolean checkName = p1.checkName(p2, false);
                    if (checkName != null && checkName) {
                        check = true;
                        tmpAu.add(createURI, OWL.ONEOF, factory.createURI(resource));
                    }
                    tmpAu.add(factory.createURI(aDocURL), factory.createURI(DOAJPREFIX + "contributor"), createURI);
                    if (j == 0) {
                        tmpAu.add(factory.createURI(aDocURL), factory.createURI(DOAJPREFIX + "creator"), createURI);
                    }
                    addProperty(readJsonObj1.get(j), NAMEFIELD, DOAJPREFIX + NAMEFIELD, tmpAu, auUR);
                    addProperty(readJsonObj1.get(j), "email", DOAJPREFIX + "email", tmpAu, auUR);
                    addProperty(readJsonObj1.get(j), "affiliation", DOAJPREFIX + "affiliation", tmpAu, auUR);
                }
                if (check && readJsonObj1.size() > MAX_AUTHORS_PER_PAPER) {
                    log.info("Ignoring {} it has more than 20 authors", aDocURL);
                    check = false;
                }
                if (check) {
                    triples.addAll(tmpAu);
                    List<LinkedHashMap<String, String>> bibj = readJsonObj(doc, MAIN_NODE + i + "].bibjson");
                    addProperty(bibj.get(0), "title", DOAJPREFIX + "title", triples, aDocURL);
                    addProperty(bibj.get(0), "year", DOAJPREFIX + "year", triples, aDocURL);
                    addProperty(bibj.get(0), "abstract", DOAJPREFIX + "abstract", triples, aDocURL);
                    addProperty(bibj.get(0), "start_page", DOAJPREFIX + "start_page", triples, aDocURL);
                    addProperty(bibj.get(0), "end_page", DOAJPREFIX + "end_page", triples, aDocURL);
                    bibj = readJsonObj(doc, MAIN_NODE + i + "].bibjson.link[*]");
                    addProperties(bibj, "url", DOAJPREFIX + "url", triples, aDocURL);
                    bibj = readJsonObj(doc, MAIN_NODE + i + "].bibjson.subject[*]");
                    addProperties(bibj, "term", DOAJPREFIX + "term", triples, aDocURL);
                    bibj = readJsonObj(doc, MAIN_NODE + i + "].bibjson.identifier[?(@.type=='doi')]");
                    addProperties(bibj, "id", DOAJPREFIX + "doi", triples, aDocURL);
                    bibj = readJsonObj(doc, MAIN_NODE + i + "].bibjson.identifier[?(@.type=='eissn' || @.type=='pissn')]");
                    addProperties(bibj, "id", DOAJPREFIX + "issn", triples, aDocURL);
                    bibj = readJsonObj(doc, MAIN_NODE + i + "].bibjson.journal");
                    addProperty(bibj.get(0), "publisher", DOAJPREFIX + "publisher", triples, aDocURL);
                    addProperty(bibj.get(0), "title", DOAJPREFIX + "journal", triples, aDocURL);
                    addProperty(bibj.get(0), "country", DOAJPREFIX + "country", triples, aDocURL);
                    addProperty(bibj.get(0), "number", DOAJPREFIX + "number", triples, aDocURL);
                    addProperty(bibj.get(0), "volume", DOAJPREFIX + "volume", triples, aDocURL);
                    List<String> readJsonStr = readJsonStr(doc, MAIN_NODE + i + "].bibjson.journal.issns[*]");
                    addPropertiesLiteral(readJsonStr, DOAJPREFIX + "issn", triples, aDocURL);
                    readJsonStr = readJsonStr(doc, MAIN_NODE + i + "].bibjson.journal.language[*]");
                    addPropertiesLiteral(readJsonStr, DOAJPREFIX + "language", triples, aDocURL);
                    readJsonStr = readJsonStr(doc, MAIN_NODE + i + "].bibjson.keywords[*]");
                    addPropertiesLiteral(readJsonStr, DOAJPREFIX + "keyword", triples, aDocURL);
                }
            }
        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing HTML response", e);
        } catch (Exception e) {
            throw new DataRetrievalException("could not parse XML response. It is not in proper XML format", e);
        }
        return Collections.emptyList();
    }

    private void addProperties(List<LinkedHashMap<String, String>> data, String prop, String ontProp, Model triples, String doc) {
        for (LinkedHashMap<String, String> a : data) {
            addProperty(a, prop, ontProp, triples, doc);
        }
    }

    private void addPropertiesLiteral(List<String> data, String ontProp, Model triples, String doc) {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        for (String a : data) {
            Literal createLiteral = factory.createLiteral(a);
            URI propU = factory.createURI(ontProp);
            URI docU = factory.createURI(doc);
            triples.add(docU, propU, createLiteral);
        }
    }

    private void addProperty(Map<String, String> data, String prop, String ontProp, Model triples, String doc) {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        String get = data.get(prop);
        if (get != null && !get.trim().isEmpty()) {
            Literal createLiteral = factory.createLiteral(get);
            URI propU = factory.createURI(ontProp);
            URI docU = factory.createURI(doc);
            triples.add(docU, propU, createLiteral);
        }
    }

    private String getExtractName(String resource) {
        String id = URLDecoder.decode(resource.substring(resource.lastIndexOf('/') + 1));
        return id.replaceAll("_", " ").replaceAll("-", " ");
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
        return "bibjson.author.name:" + query + "";
    }

    private List<String> readJsonStr(DocumentContext jsonDocument, String query) {
        return jsonDocument.read(query);
    }

    private List<LinkedHashMap<String, String>> readJsonObj(DocumentContext jsonDocument, String query) {
        return jsonDocument.read(query);
    }

    protected Configuration getConfiguration() {
        return Configuration.defaultConfiguration()
                .addOptions(Option.ALWAYS_RETURN_LIST)
                .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL)
                .addOptions(Option.SUPPRESS_EXCEPTIONS);
    }
}
