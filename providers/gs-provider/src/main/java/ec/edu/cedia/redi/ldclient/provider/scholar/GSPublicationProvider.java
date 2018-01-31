/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.edu.cedia.redi.ldclient.provider.scholar;

import com.google.common.collect.ImmutableList;
import ec.edu.cedia.redi.ldclient.provider.scholar.mapping.ScholarAuthorTextLiteralMapper;
import ec.edu.cedia.redi.ldclient.provider.scholar.mapping.ScholarEmailTextLiteralMapper;
import ec.edu.cedia.redi.ldclient.provider.scholar.mapping.ScholarTableTextLiteralMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class GSPublicationProvider extends AbstractHTMLDataProvider implements DataProvider {//NOPMD

    public static final String PROVIDER_NAME = "Google Scholar Publications";
    private final Logger log = LoggerFactory.getLogger(GSPublicationProvider.class);
    private final Map<String, JSoupMapper> postMappings = new ConcurrentHashMap<>();
    private final ValueFactory vf = ValueFactoryImpl.getInstance();
    /**
     * Pattern matchings
     */
    public static String AUTHORS_SEARCH = "^https?://scholar\\.google\\.com/citations\\?mauthors\\=(.*)\\&hl=en\\&view_op\\=search_authors";
    public static String PROFILE = "^https?:\\/\\/scholar\\.google\\.com\\/citations\\?user=.*";
    public static String PUBLICATION = "^https?:\\/\\/scholar\\.google\\.com\\/citations\\?view_op=view_citation.*";

    @Override
    protected List<String> getTypes(URI resource) {
        if (resource.stringValue().matches(PROFILE)) {
            return ImmutableList.of(FOAF.NAMESPACE + "Person");
        } else if (resource.stringValue().matches(PUBLICATION)) {
            return ImmutableList.of(BIBO.NAMESPACE + "AcademicArticle");
        }
        return Collections.emptyList();
    }

    @Override
    protected Map<String, JSoupMapper> getMappings(String resource, String requestUrl) {
        postMappings.clear();
        if (requestUrl.matches(AUTHORS_SEARCH)) {
            postMappings.put(REDI.NAMESPACE + "googlescholarURL", new CssUriAttrBlacklistQueryParamsMapper("div .gsc_oai_name a", "href", "hl", "oe"));
        } else if (requestUrl.matches(PROFILE)) {
            postMappings.put(FOAF.NAMESPACE + "publications", new CssUriAttrWhitelistQueryParamsMapper("div .gsc_a_tr .gsc_a_t a", "data-href", "view_op", "citation_for_view"));
            postMappings.put(FOAF.NAMESPACE + "img", new CssUriAttrWhitelistQueryParamsMapper("div#gsc_prf_pua img", "src", "view_op", "user"));
            postMappings.put(FOAF.NAMESPACE + "name", new CssTextLiteralMapper("div#gsc_prf_in"));
            postMappings.put(REDI.NAMESPACE + "affiliationName", new CssTextLiteralMapper(".gsc_prf_il:nth-child(2) a"));
            postMappings.put(FOAF.NAMESPACE + "topic_interest", new CssTextLiteralMapper("div#gsc_prf_int a"));
            postMappings.put(REDI.NAMESPACE + "domain", new ScholarEmailTextLiteralMapper("#gsc_prf_ivh"));
        } else if (requestUrl.matches(PUBLICATION)) {
            postMappings.put(DCTERMS.NAMESPACE + "title", new CssTextLiteralMapper("div#gsc_vcd_title a"));
            postMappings.put(BIBO.NAMESPACE + "uri1", new CssUriAttrMapper("div#gsc_vcd_title a", "href"));
            postMappings.put(BIBO.NAMESPACE + "uri2", new CssUriAttrMapper("div#gsc_vcd_title_gg a", "href"));
            postMappings.put(DCTERMS.NAMESPACE + "contributor", new ScholarAuthorTextLiteralMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value", "Authors"));
            postMappings.put(BIBO.NAMESPACE + "created", new ScholarTableTextLiteralMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value", "Publication date"));
            postMappings.put(REDI.NAMESPACE + "conference", new ScholarTableTextLiteralMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value", "Conference"));
            postMappings.put(REDI.NAMESPACE + "pages", new ScholarTableTextLiteralMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value", "Pages"));
            postMappings.put(DCTERMS.NAMESPACE + "publisher", new ScholarTableTextLiteralMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value", "Publisher"));
            postMappings.put(BIBO.NAMESPACE + "abstract", new ScholarTableTextLiteralMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value", "Description"));
            postMappings.put(REDI.NAMESPACE + "citationCount", new ScholarTableTextLiteralMapper("div#gsc_vcd_table .gs_scl", "gsc_vcd_field", ".gsc_vcd_value div[style] a  ", "Total citations"));

        }
        return postMappings;
    }

    @Override
    protected List<String> buildRequestUrl(String resourceUri, Endpoint endpoint) throws DataRetrievalException {
        return Collections.singletonList(resourceUri);
    }

    @Override
    public List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream in, String contentType) throws DataRetrievalException {
        List<String> urls = new ArrayList<>();
        if (requestUrl.matches(AUTHORS_SEARCH)) {
            urls = super.parseResponse(resource, requestUrl, triples, in, contentType);
            registerAuthorProfile(resource, triples);
        } else if (requestUrl.matches(PROFILE)) {
            String r = requestUrl.replaceAll("&cstart=.*&pagesize=.*", "");
            urls = super.parseResponse(r, requestUrl, triples, in, contentType);
        } else if (requestUrl.matches(PUBLICATION)) {
            urls = super.parseResponse(requestUrl, requestUrl, triples, in, contentType);
        }
        return urls;
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
            if (publications.size() == 100) {
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

    private void registerAuthorProfile(String resource, Model triples) {
        List<URI> profiles = new ArrayList<>();
        for (Value object : triples.filter(null, REDI.GSCHOLAR_URl, null).objects()) {
            profiles.add((URI) object);
        }
        triples.remove(null, REDI.GSCHOLAR_URl, null);
        for (URI profile : profiles) {
            triples.add(profile, OWL.ONEOF, vf.createURI(resource));
        }
    }
}
