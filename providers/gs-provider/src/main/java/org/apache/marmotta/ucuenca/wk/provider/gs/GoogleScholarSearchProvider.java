/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.marmotta.ucuenca.wk.provider.gs;

import com.google.common.base.Preconditions;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.apache.marmotta.ucuenca.wk.commons.impl.DistanceServiceImpl;
import org.apache.marmotta.ucuenca.wk.commons.service.DistanceService;
import org.apache.marmotta.ucuenca.wk.endpoint.gs.GoogleScholarSearchEndpoint;
import org.apache.marmotta.ucuenca.wk.provider.gs.handler.IHandler;
import org.apache.marmotta.ucuenca.wk.provider.gs.handler.ProfileHandler;
import org.apache.marmotta.ucuenca.wk.provider.gs.handler.PublicationHandler;
import org.apache.marmotta.ucuenca.wk.provider.gs.handler.SearchHandler;
import org.apache.marmotta.ucuenca.wk.provider.gs.mapper.MapperObjectRDF;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.Author;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.Publication;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.BIBO;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * <p>
 * Support Google Scholar information as RDF
 * <p/>
 * Author: Santiago Gonzalez
 *
 * @author Xavier Sumba
 */
public class GoogleScholarSearchProvider extends AbstractHttpProvider {//NOPMD

    private final DistanceService distance =new DistanceServiceImpl();
    private static final Logger LOG = LoggerFactory.getLogger(GoogleScholarSearchProvider.class);
    public static final String SCHOLAR_GOOGLE = "https://scholar.google.com";
    public static final String URI_START_WITH = "http";

    private MapperObjectRDF mapper = null;

    private String[] cities;
    private String[] provinces;
    private String fname;
    private String lname;
    private String[] ies;
    private String[] domains;
    private Author author = null;

    public static final ConcurrentMap<String, URI> MAPPING_SCHEMA = new ConcurrentHashMap();

    static {
        MAPPING_SCHEMA.put("name", FOAF.NAME);
        MAPPING_SCHEMA.put("affiliation", FOAF.ORGANIZATION);
        MAPPING_SCHEMA.put("img", FOAF.IMG);
        MAPPING_SCHEMA.put("numCitations", REDI.CITATION_COUNT);
        MAPPING_SCHEMA.put("profile", BIBO.URI);
        MAPPING_SCHEMA.put("areas", DCTERMS.SUBJECT);

        MAPPING_SCHEMA.put("title", DCTERMS.TITLE);
        MAPPING_SCHEMA.put("description", BIBO.ABSTRACT);
        MAPPING_SCHEMA.put("pages", BIBO.PAGES);
        MAPPING_SCHEMA.put("publisher", DCTERMS.PUBLISHER);
        MAPPING_SCHEMA.put("conference", BIBO.CONFERENCE);
        MAPPING_SCHEMA.put("journal", BIBO.JOURNAL);
        MAPPING_SCHEMA.put("volume", BIBO.VOLUME);
        MAPPING_SCHEMA.put("issue", BIBO.ISSUE);
        MAPPING_SCHEMA.put("date", DCTERMS.CREATED);
    }

    /**
     * Return the name of this data provider. To be used e.g. in the
     * configuration and in log messages.
     *
     * @return
     */
    @Override
    public String getName() {
        return "Google Scholar Search";
    }

    /**
     * Return the list of mime types accepted by this data provider.
     *
     * @return
     */
    @Override
    public String[] listMimeTypes() {
        return new String[]{
            "text/html"
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
        Preconditions.checkArgument(endpoint instanceof GoogleScholarSearchEndpoint);
        Matcher m = endpoint.getUriPatternCompiled().matcher(resource);
        String baseResource = "";
        String searchName = null;

        if (endpoint instanceof GoogleScholarSearchEndpoint) {
            GoogleScholarSearchEndpoint gsEndpoint = ((GoogleScholarSearchEndpoint) endpoint);
            cities = gsEndpoint.getCities();
            provinces = gsEndpoint.getProvinces();
            ies = gsEndpoint.getIes();
            domains = gsEndpoint.getDomains();
            fname = gsEndpoint.getFirstName();
            lname = gsEndpoint.getLastName();
            baseResource = gsEndpoint.getResource();
            LOG.info(Arrays.toString(cities));
            LOG.info(Arrays.toString(provinces));
        }
        if (m.find()) {
            searchName = m.group(2);
            mapper = new MapperObjectRDF(searchName.replace("+", " "), baseResource);
        }
        return Collections.singletonList(resource);
    }

    @Override
    public List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {//NOPMD
        List<String> urls = new ArrayList<>();
        try { //NOPMD
            LOG.debug("Request Successful to {0}", requestUrl);

            IHandler handler = null;

            // Extract information of authors if they have a gs profile.
            if (requestUrl.contains("https://scholar.google.com/citations?mauthors=")) {
                handler = new SearchHandler();
                handler.extract(input);
                author = chooseCorrectAuthor(((SearchHandler) handler).getResults());
                if (author != null) {
                    urls.add(author.getProfile() + "&cstart=0&pagesize=100");
                }
            } else if (requestUrl.contains("https://scholar.google.com/citations?user=") && author != null) {
                // Extract url of publications from author's profile
                handler = new ProfileHandler(author);
                handler.extract(input);
                boolean isDone = true;
                int maxPub = Integer.parseInt(requestUrl.substring(requestUrl.indexOf("start=") + 6, requestUrl.indexOf("&pagesize"))) + 100;
                if (author.getNumPublications() == maxPub) {
                    urls.add(author.getProfile() + "&cstart=" + maxPub + "&pagesize=100");
                    isDone = false;
                }

                // Just add all publications URL and return all triples from author when there is not left publications URL
                if (isDone) {
                    for (Publication p : author.getPublications()) {
                        urls.add(p.getUrl());
                    }
                    triples.addAll(mapper.map(author));
                }
            } else if (requestUrl.contains("https://scholar.google.com/citations?view_op=view_citation")) {
                // Extract information of each publication URL
                Publication p = new Publication(requestUrl);
                handler = new PublicationHandler(p);
                handler.extract(input);
                triples.addAll(mapper.map(p));
            }

        } catch (MalformedURLException | SAXException | InterruptedException | IllegalArgumentException | IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GoogleScholarSearchProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
//<editor-fold defaultstate="collapsed" desc="old code">
////////////////////////////////////////////////////////
//        final GSXMLHandler gsXMLHandler = new GSXMLHandler();
//        gsXMLHandler.clearGSresultList();
//        try {
//            XMLReader xr = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
//            xr.setContentHandler(gsXMLHandler);
//            InputSource gsxml = new InputSource(input);
//            gsxml.setEncoding("iso-8859-1");
//            xr.parse(gsxml);
//
//            final Set<GSresult> gsresultlist = gsXMLHandler.getGSresultList();
//            Gson gson = new Gson();
//            JsonArray json = new JsonArray();
//            for (GSresult d : gsresultlist) {
//                json.add(gson.toJsonTree(d).getAsJsonObject());
//            }
//            JSONtoRDF parser = new JSONtoRDF(resource, MAPPINGSCHEMA, json, triples);
//            try {
//                parser.parse();
//            } catch (Exception e) {
//                throw new DataRetrievalException("I/O exception while retrieving resource: " + requestUrl, e);
//            }
//            int numPages = (int) ((double) (gsXMLHandler.getNumResults() / 10)) + 1;
//            int pagesLoaded = 1;
//            Model model = null;
//            while (pagesLoaded < numPages) {
//
//                String pagenumquery = Integer.toString(pagesLoaded * 10);
//                String moreDataUrl = String.format(API, pagenumquery, stringSearch, authorSearch, advancedSearch);
//                ClientConfiguration conf = new ClientConfiguration();
//                LDClient ldClient = new LDClient(conf);
//                ClientResponse response = ldClient.retrieveResource(moreDataUrl);
//                Model pageModel = response.getData();
//                if (model == null) {
//                    model = pageModel;
//                } else {
//                    model.addAll(pageModel);
//                }
//                pagesLoaded++;
//            }
//            triples.addAll(model);
//
//        } catch (SAXException | IOException e) {
//            throw new DataRetrievalException("I/O exception while retrieving resource: " + requestUrl, e);
//        }
//</editor-fold>
        return urls;
    }

    private Author chooseCorrectAuthor(List<Author> authors) {

        for (Author a : authors) {

            // If the authors domain correspond with IES domain, the author belongs to the IES
            for (String domain : domains) {
                if (distance.getEqualNames(fname, lname, a.getName()) && domain.equals(a.getDomain())) {
                    return a;
                }
            }
            for (String i : ies) {
                if (distance.getEqualNames(fname, lname, a.getName()) && a.getAffiliation().contains(i)) {
                    return a;
                }
            }
            // Just compare names
//            if (distance.getEqualNames(fname, lname, a.getName())) {
//                return a;
//            }
            // compare University, sometimes Universities has the name of the city in it
            // compare(author.getAffiliation(), ies)
//            if (distance.getEqualNames(fname, lname, author.getName()) && a.getAffiliation().toLowerCase().contains(cities.toLowerCase())
//                    || a.getAffiliation().toLowerCase().contains(province.toLowerCase())) {
//                return a;
//            }

        }
        return null;
    }

}
