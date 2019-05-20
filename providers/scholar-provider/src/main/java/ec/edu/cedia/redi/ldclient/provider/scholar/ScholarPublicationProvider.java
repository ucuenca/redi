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
package ec.edu.cedia.redi.ldclient.provider.scholar;

import com.google.common.collect.ImmutableList;
import ec.edu.cedia.redi.ldclient.provider.scholar.mapping.ScholarAbstractTextLiteralMapper;
import ec.edu.cedia.redi.ldclient.provider.scholar.mapping.ScholarAuthorTextLiteralMapper;
import ec.edu.cedia.redi.ldclient.provider.scholar.mapping.ScholarCitationTextLiteralMapper;
import ec.edu.cedia.redi.ldclient.provider.scholar.mapping.ScholarEmailTextLiteralMapper;
import ec.edu.cedia.redi.ldclient.provider.scholar.mapping.ScholarImageUriAttrMapper;
import ec.edu.cedia.redi.ldclient.provider.scholar.mapping.ScholarIndexesTextLiteralMapper;
import ec.edu.cedia.redi.ldclient.provider.scholar.mapping.ScholarTableDateMapper;
import ec.edu.cedia.redi.ldclient.provider.scholar.mapping.ScholarTableTextLiteralMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.provider.html.AbstractHTMLDataProvider;
import org.apache.marmotta.ldclient.provider.html.mapping.CssTextLiteralMapper;
import org.apache.marmotta.ldclient.provider.html.mapping.CssUriAttrBlacklistQueryParamsMapper;
import org.apache.marmotta.ldclient.provider.html.mapping.CssUriAttrMapper;
import org.apache.marmotta.ldclient.provider.html.mapping.CssUriAttrWhitelistQueryParamsMapper;
import org.apache.marmotta.ldclient.provider.html.mapping.JSoupMapper;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.BIBO;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.OWL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class ScholarPublicationProvider extends AbstractHTMLDataProvider implements DataProvider {//NOPMD

    private static Logger log = LoggerFactory.getLogger(ScholarPublicationProvider.class);
    public static final String PROVIDER_NAME = "Google Scholar Publications";
    private final ConcurrentHashMap<String, JSoupMapper> postMappings = new ConcurrentHashMap<>();
    private final ValueFactory vf = ValueFactoryImpl.getInstance();
    private static final int PUBLICATIONS_PAGINATION = 100;
    /**
     * Pattern matchings
     */
    public static final String AUTHORS_SEARCH = "^https?://scholar\\.google\\.com/citations\\?mauthors\\=(.*)\\&hl=en\\&view_op\\=search_authors";
    public static final String PROFILE = "^https?:\\/\\/scholar\\.google\\.com\\/citations\\?user=.*";
    public static final String PUBLICATION = "^https?:\\/\\/scholar\\.google\\.com\\/citations\\?view_op=view_citation.*";

    @Override
    protected List<String> getTypes(URI resource) {
        if (resource.stringValue().matches(PROFILE)) {
            return ImmutableList.of(FOAF.NAMESPACE + "Person");
        } else if (resource.stringValue().matches(PUBLICATION)) {
            return ImmutableList.of(BIBO.NAMESPACE + "AcademicArticle");
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    @Override
    protected Map<String, JSoupMapper> getMappings(String resource, String requestUrl) {
        postMappings.clear();
        if (requestUrl.matches(AUTHORS_SEARCH)) {
            postMappings.put(REDI.NAMESPACE + "googlescholarURL", new CssUriAttrBlacklistQueryParamsMapper("div .gs_ai_name a", "href", "hl", "oe"));
        } else if (requestUrl.matches(PROFILE)) {
            postMappings.put(FOAF.NAMESPACE + "publications", new CssUriAttrWhitelistQueryParamsMapper("div .gsc_a_tr .gsc_a_t a", "data-href", "view_op", "citation_for_view"));
            postMappings.put(FOAF.NAMESPACE + "img", new ScholarImageUriAttrMapper("div#gsc_prf_pua img", "src", "view_op", "user"));
            postMappings.put(FOAF.NAMESPACE + "name", new CssTextLiteralMapper("div#gsc_prf_in"));
            postMappings.put(REDI.NAMESPACE + "affiliationName", new CssTextLiteralMapper(".gsc_prf_il:nth-child(2) a"));
            postMappings.put(REDI.NAMESPACE + "altAffiliationName", new CssTextLiteralMapper(".gsc_prf_il:nth-child(2)"));
            postMappings.put(FOAF.NAMESPACE + "topic_interest", new CssTextLiteralMapper("div#gsc_prf_int a"));
            postMappings.put(REDI.NAMESPACE + "domain", new ScholarEmailTextLiteralMapper("#gsc_prf_ivh"));
            postMappings.put(FOAF.NAMESPACE + "homepage", new CssUriAttrMapper("#gsc_prf_ivh a", "href"));
            postMappings.put(REDI.NAMESPACE + "citationCount", new ScholarIndexesTextLiteralMapper("#gsc_rsb_st tbody tr", "gsc_rsb_sc1", ".gsc_rsb_std", "Citations"));
            postMappings.put(REDI.NAMESPACE + "hindex", new ScholarIndexesTextLiteralMapper("#gsc_rsb_st tbody tr", "gsc_rsb_sc1", ".gsc_rsb_std", "h-index"));
            postMappings.put(REDI.NAMESPACE + "i10", new ScholarIndexesTextLiteralMapper("#gsc_rsb_st tbody tr", "gsc_rsb_sc1", ".gsc_rsb_std", "i10-index"));
        } else if (requestUrl.matches(PUBLICATION)) {
            postMappings.put(DCTERMS.NAMESPACE + "title", new CssTextLiteralMapper("div#gsc_vcd_title"));
            postMappings.put(BIBO.NAMESPACE + "uri1", new CssUriAttrMapper("div#gsc_vcd_title a", "href"));
            postMappings.put(BIBO.NAMESPACE + "uri2", new CssUriAttrMapper("div#gsc_vcd_title_gg a", "href"));
            postMappings.put(DCTERMS.NAMESPACE + "contributor", new ScholarAuthorTextLiteralMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value", "Authors"));
            postMappings.put(BIBO.NAMESPACE + "created", new ScholarTableDateMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value", "Publication date"));
            postMappings.put(REDI.NAMESPACE + "conference", new ScholarTableTextLiteralMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value", "Conference"));
            postMappings.put(REDI.NAMESPACE + "pages", new ScholarTableTextLiteralMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value", "Pages"));
            postMappings.put(DCTERMS.NAMESPACE + "publisher", new ScholarTableTextLiteralMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value", "Publisher"));
            postMappings.put(BIBO.NAMESPACE + "issue", new ScholarTableTextLiteralMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value", "Issue"));
            postMappings.put(REDI.NAMESPACE + "journal", new ScholarTableTextLiteralMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value", "Journal"));
            postMappings.put(REDI.NAMESPACE + "book", new ScholarTableTextLiteralMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value", "Book"));
            postMappings.put(REDI.NAMESPACE + "source", new ScholarTableTextLiteralMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value", "Source"));
            postMappings.put(BIBO.NAMESPACE + "volume", new ScholarTableTextLiteralMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value", "Volume"));
            postMappings.put(BIBO.NAMESPACE + "abstract", new ScholarAbstractTextLiteralMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value", "Description"));
            postMappings.put(REDI.NAMESPACE + "citationCount", new ScholarCitationTextLiteralMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value div[style] a  ", "Total citations"));

        }
        return postMappings;
    }

    @Override
    protected List<String> buildRequestUrl(String resourceUri, Endpoint endpoint) throws DataRetrievalException {
        return Collections.singletonList(resourceUri);
    }

    @Override
    public List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {
        try {
            // Set the correct encoding.
            byte[] data = IOUtils.toByteArray(input);
            InputStream in = new ByteArrayInputStream(data);
            if (!contentType.contains("charset")) {
                String charset = detectEncoding(new ByteArrayInputStream(data), requestUrl);
                contentType += "; charset=" + charset;
            }

            List<String> urls = new ArrayList<>();
            if (requestUrl.matches(AUTHORS_SEARCH)) {
                urls = super.parseResponse(resource, requestUrl, triples, in, contentType);
                registerAuthorProfile(resource, triples);
            } else if (requestUrl.matches(PROFILE)) {
                String r = requestUrl.replaceAll("&cstart=.*&pagesize=.*", "");
                urls = super.parseResponse(r, requestUrl, triples, in, contentType);
            } else if (requestUrl.matches(PUBLICATION)) {
                urls = super.parseResponse(requestUrl, requestUrl, triples, in, contentType);
                URI r = vf.createURI(requestUrl);
                triples.add(r, BIBO.URI, r);
            }
            delay(); // wait after each call.
            return urls;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Detect the stream's encoding using Tika, otherwise return default
     * encoding utf-8.
     *
     * @param input      InputStream to be transformed
     * @param requestUrl Resource URL - Just for logging
     * @return New ByteArray Stream transformed to the UTF-8 Charset.
     */
    public String detectEncoding(InputStream input, String requestUrl/*Just for logging purposes*/) {
        try {
            CharsetDetector charsetDetector = new CharsetDetector();
            charsetDetector.setText(input);
            CharsetMatch matchFound = charsetDetector.detect();
            if (matchFound != null && matchFound.getName() != null) {
                return matchFound.getName();
            }
        } catch (IOException ex) {
            log.error("cannot detect encodig for url {}", requestUrl);
        }
        return "utf-8";
    }

    @Override
    protected List<String> findAdditionalRequestUrls(String resource, Document document, String requestUrl) {
        List<String> urls = new ArrayList<>();
        if (requestUrl.matches(AUTHORS_SEARCH)) {
            JSoupMapper mapper = getMappings(resource, requestUrl).get(REDI.NAMESPACE + "googlescholarURL");
            Elements profiles = mapper.select(document);
            for (Element profile : profiles) {
                for (Value v : mapper.map(resource, profile, vf)) {
                    String url = String.format("%s&cstart=%s&pagesize=100", v.stringValue(), 0);
                    urls.add(url);
                }
            }
        } else if (requestUrl.matches(PROFILE)) {
            JSoupMapper mapper = getMappings(resource, requestUrl).get(FOAF.NAMESPACE + "publications");
            Elements publications = mapper.select(document);
            if (publications.size() == PUBLICATIONS_PAGINATION) {
                Matcher matcher = Pattern.compile("^.*&cstart=(.*)&.*$").matcher(requestUrl);
                if (matcher.find()) {
                    String start = String.format("cstart=%s", matcher.group(1));
                    String newstart = String.format("cstart=%s", Integer.parseInt(matcher.group(1)) + 100);
                    String pagination = requestUrl.replaceFirst(start, newstart);
                    urls.add(pagination);
                }
            }
            for (Element publication : publications) {
                for (Value value : mapper.map(resource, publication, vf)) {
                    urls.add(value.stringValue());
                }
            }
        }
        return urls;
    }

    @Override
    public String getName() {
        return PROVIDER_NAME;
    }

    @Override
    public String[] listMimeTypes() {
        return new String[]{"text/html"};
    }

    /**
     * Waits between one and three seconds.
     */
    private static void delay() {
        try {
            Random r = new Random();
            int max = 3000,
                    min = 1000;
            int milseconds = r.nextInt(max - min) + min;
            Thread.sleep(milseconds);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void registerAuthorProfile(String resource, Model triples) {
        List<URI> profiles = new ArrayList<>();
        for (Value object : triples.filter(null, REDI.GSCHOLAR_URl, null).objects()) {
            profiles.add((URI) object);
        }
        triples.remove(null, REDI.GSCHOLAR_URl, null);
        for (URI profile : profiles) {
            triples.add(profile, OWL.ONEOF, vf.createURI(resource));
            triples.add(profile, FOAF.HOLDS_ACCOUNT, profile);
        }
    }
}
