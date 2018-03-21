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
import com.google.common.collect.Sets;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import java.io.ByteArrayInputStream;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Person;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;

/**
 * Support Scielo Author information as RDF
 * <p/>
 * Author: Jose Ortiz
 */
public class ScieloRawProvider extends AbstractHttpProvider {
    
    private static Logger log = LoggerFactory.getLogger(ScieloRawProvider.class);
    public static final String NAME = "Scielo Raw Provider";
    public static final String PATTERN = "https://search\\.scielo\\.org/search/.*";
    public static final String SEARCHAPI = "https://search.scielo.org/?q=%s&output=xml";
    public static final String DESCRIBEAPI = "http://articlemeta.scielo.org/api/v1/article/?code=%s";
    public static final String SCIELOPREFIX = "http://search.scielo.org/ontology/";
    public static final String SCIELOBASE = "http://search.scielo.org/data/";
    public static final String SCIELOBASEPUBLICATION = SCIELOBASE + "publication/";
    public static final String SCIELOBASEAUTHOR = SCIELOBASE + "author/";
    public static final String SCIELOBASEAFFILIATION = SCIELOBASE + "affiliation/";
    private static final String SUE = "issue";
    private static final String VOLUMEN = "volumen";
    private static final String SN = "issn";
    private static final String JOURNAL = "journal";
    private static final String AFFNAME = "nameff";
    private static final String AFFIDPATH2 = "$.article.v240[*].i";
    private static final String AFFIDPATH1 = "$.article.v70[*].i";

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
        return Collections.singletonList(url);
    }
    
    @Override
    public List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {
        log.debug("Request Successful to {0}", requestUrl);
        if (requestUrl.startsWith("https://search.scielo.org")) {
            return parseSearchDocs(requestUrl, input, resource, triples);
        } else if (requestUrl.startsWith("http://articlemeta.scielo.org")) {
            return parseDocs(requestUrl, input, triples);
        }
        return Collections.emptyList();
    }
    
    private List<String> parseDocs(String requestUrl, InputStream input, Model triplesx) throws NumberFormatException, DataRetrievalException {
        try {
            Model triples = new LinkedHashModel();
            ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
            byte[] data = IOUtils.toByteArray(input);
            DocumentContext parse = null;
            try {
                parse = JsonPath.parse(new ByteArrayInputStream(data), getConfiguration());
            } catch (Exception e) {
                log.debug("Scielo error", e);
                return Collections.emptyList();
            }
            String code = URLEncoder.encode(extractDocCode(requestUrl));
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "created", "$.publication_date", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "abstract", "$.article.v83[?(@.l=='es')].a", null, "es", false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "abstract", "$.article.v83[?(@.l=='en')].a", null, "en", false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + SN, "$.article.v35[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "title", "$.article.v12[?(@.l=='es')]._", null, "es", false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "title", "$.article.v12[?(@.l=='en')]._", null, "en", false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "doi", "$.article.v237[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "keyword", "$.article.v85[?(@.l=='es')].k", null, "es", false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "keyword", "$.article.v85[?(@.l=='en')].k", null, "en", false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "uri", "$.fulltexts.*.es", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "uri", "$.fulltexts.*.en", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "uri", "$.fulltexts.*.pt", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "publisher", "$.issue.issue.v480[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + JOURNAL, "$.issue.issue.v151[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + JOURNAL, "$.issue.issue.v130[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "publisher", "$.title.v480[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + JOURNAL, "$.title.v151[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + JOURNAL, "$.title.v130[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "pageS", "$.article.v14[*].f", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "pageE", "$.article.v14[*].l", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "page", "$.article.v14[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + SUE, "$.article.v32[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + SUE, "$.article.v4[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + SUE, "$.article.v882[*].n", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + VOLUMEN, "$.article.v882[*].v", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + VOLUMEN, "$.article.v31[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + SUE, "$.issue.issue.v32[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + SUE, "$.issue.issue.v4[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "interest", "$.title.v854[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + VOLUMEN, "$.article.v31[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + VOLUMEN, "$.issue.issue.v31[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "doi", "$.doi", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + SN, "$.title.issns[*]", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + "publisher", "$.article.v62[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + SN, "$.article.v435[*]._", null, null, false);
            mapProperty(triples, parse, SCIELOBASEPUBLICATION + code, SCIELOPREFIX + SN, "$.article.v936[*].i", null, null, false);
            
            mapProperty(triples, parse, SCIELOBASEAUTHOR + code, SCIELOPREFIX + "fname", "$.article.v10[*].s", null, null, true);
            mapProperty(triples, parse, SCIELOBASEAUTHOR + code, SCIELOPREFIX + "gname", "$.article.v10[*].n", null, null, true);
            mapProperty(triples, parse, SCIELOBASEAUTHOR + code, SCIELOPREFIX + "aff", "$.article.v10[*]['1']", SCIELOBASEAFFILIATION + code, null, true);
            if (readJsonStr(parse, "$.article.v70[*]._").size() == readJsonStr(parse, "$.article.v10[*]._").size()
                    && readJsonStr(parse, "$.article.v70[*].e").size() == readJsonStr(parse, "$.article.v10[*]._").size()) {
                mapProperty(triples, parse, SCIELOBASEAUTHOR + code, SCIELOPREFIX + "email", "$.article.v70[*].e", null, null, true);
            } else {
                mapEmail(triples, parse, code, "$.article.v240[*]");
                mapEmail(triples, parse, code, "$.article.v70[*]");
            }
            mapRelation(triples, parse, SCIELOBASEAFFILIATION + code, SCIELOPREFIX + AFFNAME, AFFIDPATH1, "$.article.v70[*]._", null);
            mapRelation(triples, parse, SCIELOBASEAFFILIATION + code, SCIELOPREFIX + AFFNAME, AFFIDPATH2, "$.article.v240[*]._", null);
            mapRelation(triples, parse, SCIELOBASEAFFILIATION + code, SCIELOPREFIX + AFFNAME, AFFIDPATH1, "$.article.v70[*].1", null);
            mapRelation(triples, parse, SCIELOBASEAFFILIATION + code, SCIELOPREFIX + AFFNAME, AFFIDPATH2, "$.article.v240[*].1", null);
            mapRelation(triples, parse, SCIELOBASEAFFILIATION + code, SCIELOPREFIX + AFFNAME, AFFIDPATH1, "$.article.v70[*].2", null);
            mapRelation(triples, parse, SCIELOBASEAFFILIATION + code, SCIELOPREFIX + AFFNAME, AFFIDPATH2, "$.article.v240[*].2", null);
            mapRelation(triples, parse, SCIELOBASEAFFILIATION + code, SCIELOPREFIX + AFFNAME, AFFIDPATH1, "$.article.v70[*].3", null);
            mapRelation(triples, parse, SCIELOBASEAFFILIATION + code, SCIELOPREFIX + AFFNAME, AFFIDPATH2, "$.article.v240[*].3", null);
            mapRelation(triples, parse, SCIELOBASEAFFILIATION + code, SCIELOPREFIX + AFFNAME, AFFIDPATH1, "$.article.v70[*].9", null);
            mapRelation(triples, parse, SCIELOBASEAFFILIATION + code, SCIELOPREFIX + AFFNAME, AFFIDPATH2, "$.article.v240[*].9", null);
            
            Model unmodifiable = triples.unmodifiable();
            
            Model filter = unmodifiable.filter(null, instance.createURI(SCIELOPREFIX + "fname"), null);
            Model filter1 = unmodifiable.filter(null, instance.createURI(SCIELOPREFIX + "gname"), null);
            Model filter2 = unmodifiable.filter(null, instance.createURI(SCIELOPREFIX + "email"), null);
            Model filter3 = unmodifiable.filter(null, instance.createURI(SCIELOPREFIX + "aff"), null);
            
            Model t = new LinkedHashModel();
            t.addAll(filter);
            t.addAll(filter1);
            t.addAll(filter2);
            t.addAll(filter3);
            
            Model t2 = new LinkedHashModel();
            for (Resource r : t.subjects()) {
                t2.add(instance.createURI(SCIELOBASEPUBLICATION + code), instance.createURI(SCIELOPREFIX + "contributor"), r);
                if (r.stringValue().endsWith("_0")) {
                    t2.add(instance.createURI(SCIELOBASEPUBLICATION + code), instance.createURI(SCIELOPREFIX + "creator"), r);
                }
            }
            triples.addAll(t2);
            triplesx.addAll(triples);
        } catch (IOException ex) {
            throw new DataRetrievalException("Error while parsing response", ex);
        }
        
        return Collections.emptyList();
    }
    
    private List<String> parseSearchDocs(String requestUrl, InputStream input, String resource, Model triples) throws NumberFormatException, DataRetrievalException {
        List<String> docsURLs = new ArrayList<>();
        String pageControl = "&from=%s&count=15";
        String firstRequestSuffix = "&count=15";
        ValueFactory factory = ValueFactoryImpl.getInstance();
        try {
            Document doc = new SAXBuilder(XMLReaders.NONVALIDATING).build(input);
            if (!requestUrl.endsWith(firstRequestSuffix)) {
                int max = 0;
                for (Element element : queryElements(doc, "/response/result")) {
                    if (element.getAttributeValue("name").equals("response")) {
                        String value = element.getAttributeValue("numFound");
                        max = Integer.parseInt(value);
                    }
                }
                List<String> e = new ArrayList<>();
                for (int i = 1; i <= max; i += 15) {
                    e.add(requestUrl + String.format(pageControl, i));
                }
                docsURLs.addAll(e);
            } else {
                List<String> nameOrg = Lists.newArrayList(getExtractName(resource));
                Person p1 = new Person();
                p1.Name = new ArrayList<>();
                p1.Name.add(nameOrg);
                for (Element element : queryElements(doc, "/response/result/doc")) {
                    String value = getValueXML(element, "ur");
                    String code = URLEncoder.encode(value);
                    List<String> values = getValuesXML(element, "au");
                    for (int i = 0; i < values.size(); i++) {
                        Person p2 = new Person();
                        p2.Name = new ArrayList<>();
                        p2.Name.add(Lists.newArrayList(values.get(i)));
                        if (p1.checkName(p2)) {
                            String aDocURL = String.format(DESCRIBEAPI, value);
                            docsURLs.add(aDocURL);
                            String authorURI = SCIELOBASEAUTHOR + code + "_" + i;
                            triples.add(factory.createURI(authorURI), OWL.ONEOF, factory.createURI(resource));
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing HTML response", e);
        } catch (JDOMException e) {
            throw new DataRetrievalException("could not parse XML response. It is not in proper XML format", e);
        }
        return docsURLs;
    }
    
    protected static List<Element> queryElements(Document n, String query) {
        return XPathFactory.instance().compile(query, new ElementFilter()).evaluate(n);
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
            queryP += "(";
            for (int j = 0; j < ls.size(); j++) {
                String n = ls.get(j);
                String tk = (n.length() <= 2) ? n + "*" : n;
                queryP += tk.toLowerCase() + (j == ls.size() - 1 ? " " : " OR ");
            }
            queryP += ")";
            
            query += queryP + (i == split.length - 1 ? " " : " AND ");
        }
        return "au:(" + query + ")";
    }
    
    private String getValueXML(Element m, String key) {
        List<String> values = getValuesXML(m, key);
        assert values.size() == 1;
        return values.get(0);
    }
    
    private List<String> getValuesXML(Element m, String key) {
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
    
    private List<String> readJsonStr(DocumentContext jsonDocument, String query) {
        return jsonDocument.read(query);
    }
    
    private List<LinkedHashMap<String, String>> readJsonObj(DocumentContext jsonDocument, String query) {
        return jsonDocument.read(query);
    }
    
    private void mapRelation(Model model, DocumentContext document, String subject, String property, String query1, String query2, String lang) {
        ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
        List<String> gValues = readJsonStr(document, query1);
        List<String> gValues2 = readJsonStr(document, query2);
        assert gValues.size() == gValues2.size();
        for (int i = 0; i < gValues.size(); i++) {
            String v = gValues.get(i);
            String v2 = gValues2.get(i);
            if (v == null || v.trim().equals("") || v2 == null || v2.trim().equals("")) {
                continue;
            }
            URI createIRI = instance.createURI(subject + "_" + URLEncoder.encode(v));
            URI createIRI1 = instance.createURI(property);
            Value createLiteral = null;
            if (lang != null) {
                createLiteral = instance.createLiteral(v2, lang);
            } else {
                createLiteral = instance.createLiteral(v2);
            }
            model.add(createIRI, createIRI1, createLiteral);
        }
    }
    
    private void mapProperty(Model model, DocumentContext document, String subject, String property, String query, String prefixObject, String lang, boolean addSequence) {
        ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
        List<String> gValues = readJsonStr(document, query);
        for (int i = 0; i < gValues.size(); i++) {
            String v = gValues.get(i);
            if (v == null || v.trim().equals("")) {
                continue;
            }
            URI createIRI = instance.createURI(subject);
            if (addSequence) {
                createIRI = instance.createURI(subject + "_" + i);
            }
            URI createIRI1 = instance.createURI(property);
            Value createLiteral = null;
            if (lang != null) {
                createLiteral = instance.createLiteral(v, lang);
            } else {
                if (prefixObject == null) {
                    createLiteral = instance.createLiteral(v);
                } else {
                    createLiteral = instance.createURI(prefixObject + "_" + URLEncoder.encode(v));
                }
                
            }
            model.add(createIRI, createIRI1, createLiteral);
        }
    }
    
    private void mapEmail(Model triples, DocumentContext parse2, String code, String q) {
        ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
        List<LinkedHashMap<String, String>> affData = readJsonObj(parse2, q);
        List<String> perx = readJsonStr(parse2, "$.article.v10[*]['1']");
        List<Set<String>> per = new ArrayList<>();
        for (String aff : perx) {
            Set<String> affls = Sets.newHashSet();
            if (aff != null && !aff.trim().equals("")) {
                String[] split = aff.trim().split(" ");
                for (String oneAff : split) {
                    affls.add(oneAff);
                }
            }
            if (affls.isEmpty()) {
                affls.add("");
            }
            per.add(affls);
        }
        ConcurrentHashMap<String, Queue<String>> emails = new ConcurrentHashMap<>();
        String last = "";
        for (int k = 0; k < affData.size(); k++) {
            String idAff = affData.get(k).get("i");
            String email = affData.get(k).get("e");
            String tmpAff = idAff;
            if (tmpAff == null || tmpAff.trim().equals("")) {
                tmpAff = last;
            }
            Queue<String> emailByAff = new LinkedList<>();
            if (emails.containsKey(tmpAff)) {
                emailByAff = emails.get(tmpAff);
            } else {
                emails.put(tmpAff, emailByAff);
            }
            emailByAff.add(email);
            last = tmpAff;
        }
        for (int k = 0; k < per.size(); k++) {
            matchEmails(per, k, emails, code, triples, instance);
        }
    }
    
    private void matchEmails(List<Set<String>> per, int k, ConcurrentHashMap<String, Queue<String>> emails, String code, Model triples, ValueFactoryImpl instance) {
        Set<String> affls = per.get(k);
        for (String oaff : affls) {
            String getK = oaff == null || oaff.trim().equals("") ? "" : oaff.trim();
            Queue<String> get = emails.get(getK);
            if (get != null && !get.isEmpty()) {
                String poll = get.poll();
                if (poll != null) {
                    String uri = SCIELOBASEAUTHOR + code + "_" + k;
                    String prop = SCIELOPREFIX + "email";
                    triples.add(instance.createURI(uri), instance.createURI(prop), instance.createLiteral(poll));
                }
            }
        }
    }
    
    private String extractDocCode(String resource) {
        return URLDecoder.decode(resource.substring(resource.lastIndexOf('=') + 1));
    }
    
    protected Configuration getConfiguration() {
        return Configuration.defaultConfiguration()
                .addOptions(Option.ALWAYS_RETURN_LIST)
                .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL)
                .addOptions(Option.SUPPRESS_EXCEPTIONS);
    }
}
