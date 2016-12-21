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

//import com.google.gson.Gson;
//import com.google.gson.JsonArray;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.Author;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.ProfileHandler;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.Publication;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.PublicationHandler;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.SearchHandler;

import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
//import org.apache.marmotta.ldclient.model.ClientConfiguration;
//import org.apache.marmotta.ldclient.model.ClientResponse;
//import org.apache.marmotta.ldclient.services.ldclient.LDClient;
//import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
//import org.apache.marmotta.ucuenca.wk.provider.gs.util.GSXMLHandler;
//import org.apache.marmotta.ucuenca.wk.provider.gs.util.GSresult;
//import org.apache.marmotta.ucuenca.wk.provider.gs.util.JSONtoRDF;
//import org.jdom2.Document;
//import org.jdom2.Element;
//import org.jdom2.JDOMException;
//import org.jdom2.filter.ElementFilter;
//import org.jdom2.input.SAXBuilder;
//import org.jdom2.input.sax.XMLReaders;
//import org.jdom2.xpath.XPathFactory;
import org.openrdf.model.Model;
//import org.openrdf.model.ValueFactory;
//import org.openrdf.model.impl.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
//import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
//import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.MapAuthor;
import org.apache.marmotta.ucuenca.wk.provider.gs.vocabulary.BIBO;
import org.apache.marmotta.ucuenca.wk.provider.gs.vocabulary.REDI;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Support Google Scholar information as RDF
 * <p/>
 * Author: Santiago Gonzalez
 *
 * @author Xavier Sumba
 */
public class GoogleScholarProvider extends AbstractHttpProvider {//NOPMD

    public static final String NAME = "Google Scholar Provider";
    public static final String API = "http://scholar.google.com/scholar?start=%s&q=author:%%22%s%%22%s&hl=en&as_sdt=1%%2C15&as_vis=1%s";
    //public static final String PATTERN = "http(s?)://scholar\\.google\\.com/scholar\\?start\\=0\\&q=author\\:%22(.*)%22\\&hl=en\\&as_sdt\\=1%2C15\\&as_vis\\=1(.*)$";
    //public static final String PATTERN = "http(s?)://scholar\\.google\\.com/citations\\?mauthors\\=(.*)\\&hl=en\\&view_op\\=search_authors(.*)$";
    public static final String PATTERN = "(http(s?)://scholar\\.google\\.com/citations\\?mauthors\\=(.*)\\&hl=en\\&view_op\\=search_authors);(.*)-(.*)-(.*)-(.*)$";
    public static final String SCHOLAR_GOOGLE = "https://scholar.google.com";
    private static String nsUcuenca = "https://www.cedia.org.ec/";
    public static final String URI_START_WITH = "http";

    private static Logger log = LoggerFactory.getLogger(GoogleScholarProvider.class);

    private String stringSearch = null;//, authorSearch = null, advancedSearch = null;
    private MapAuthor mauthor = null;

    private String city;
    private String province;
    private String[] ies;
    private String[] domains;

    public static final ConcurrentMap<String, String> MAPPINGSCHEMA = new ConcurrentHashMap<String, String>();
    public static final ConcurrentMap<String, URI> MAPPING_SCHEMA = new ConcurrentHashMap();

    static {
        MAPPING_SCHEMA.put("name", FOAF.NAME);
        MAPPING_SCHEMA.put("affiliation", FOAF.ORGANIZATION);
        MAPPING_SCHEMA.put("img", FOAF.IMAGE);
        MAPPING_SCHEMA.put("numCitations", REDI.CITATION_COUNT);
        MAPPING_SCHEMA.put("url", BIBO.URI);
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

        MAPPINGSCHEMA.put("entity::type", "http://purl.org/ontology/bibo/Document");
        MAPPINGSCHEMA.put("entity::property:link", "http://purl.org/ontology/bibo/uri");
        MAPPINGSCHEMA.put("entity::property:title", "http://purl.org/dc/terms/title");
        MAPPINGSCHEMA.put("entity::property:text", "http://purl.org/ontology/bibo/abstract");
        MAPPINGSCHEMA.put("entity::property:journal", "http://purl.org/ontology/bibo/Journal");
        MAPPINGSCHEMA.put("entity::property:date", "http://purl.org/dc/elements/1.1/date");
        MAPPINGSCHEMA.put("entity::property:doi", "http://purl.org/ontology/bibo/doi");
        MAPPINGSCHEMA.put("entity::property:authorlist", "http://purl.org/ontology/bibo/authorList");
        MAPPINGSCHEMA.put("entity::property:quote", "http://purl.org/ontology/bibo/Quote");
        MAPPINGSCHEMA.put("entity::property:conference", "http://purl.org/ontology/bibo/Conference");
        MAPPINGSCHEMA.put("entity::property:cites", "http://purl.org/ontology/bibo/cites");
        MAPPINGSCHEMA.put("entity::property:type", nsUcuenca + "type");
        MAPPINGSCHEMA.put("entity::property:referenceCount", nsUcuenca + "referenceCount");
        MAPPINGSCHEMA.put("entity::property:citationCount", nsUcuenca + "citationCount");
        MAPPINGSCHEMA.put("entity::property:contributor", "http://purl.org/dc/terms/contributor");
        MAPPINGSCHEMA.put("entity::property:pdf", nsUcuenca + "pdf");
        MAPPINGSCHEMA.put("entity::property:cites", "http://purl.org/ontology/bibo/cites");
        MAPPINGSCHEMA.put("entity::property:fulltextlink", "http://purl.org/ontology/bibo/content");
        MAPPINGSCHEMA.put("entity::property:creator", "http://purl.org/dc/elements/1.1/creator");

    }

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
        String url = null;
        Matcher m = Pattern.compile(PATTERN).matcher(resource);
        if (m.find()) {
            stringSearch = m.group(3);
            city = m.group(4);
            province = m.group(5);
            ies = m.group(6).split(",");
            domains = m.group(7).split(",");

            log.info(stringSearch);
            log.info(city);
            log.info(province);
            log.info(ies[0]);
            log.info(domains[0]);
//            log.debug("Extracting info for: {0}", stringSearch);
//            if (authorSearch.length() > 0) {
//                log.debug("Extra author search parameters: {0}", authorSearch);
//            }
//            if (advancedSearch.length() > 0) {
//                log.debug("Advanced search parameters: {0}", advancedSearch);
//            }
            url = m.group(1);
        }

        mauthor = new MapAuthor(stringSearch.replace("+", " "));

        return Collections.singletonList(url);
    }
    private Author author = null;

    @Override
    public List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {//NOPMD
        List<String> urls = new ArrayList<>();
        try { //NOPMD
            log.debug("Request Successful to {0}", requestUrl);

            DefaultHandler handler = null;

            // Extract information of authors if they have a gs profile.
            if (requestUrl.contains("https://scholar.google.com/citations?mauthors=")) {
                handler = new SearchHandler();
                extract(input, handler);
                author = chooseCorrectAuthor(((SearchHandler) handler).getResults());
                urls.add(author.getProfile() + "&cstart=0&pagesize=100");
            } else if (requestUrl.contains("https://scholar.google.com/citations?user=") && author != null) {
                // Extract url of publications from author's profile
                handler = new ProfileHandler(author);
                extract(input, handler);

                int start = Integer.parseInt(requestUrl.substring(requestUrl.indexOf("start=") + 6, requestUrl.indexOf("&pagesize"))) + 100;
                if (author.getNumPublications() == start) {
                    urls.add(author.getProfile() + "&cstart=" + start + "&pagesize=100");
                }
//                int i = -1;
                for (Publication p : author.getPublications()) {
                    urls.add(p.getUrl());
//                    i++;
//                    if (i == MapAuthor.MIN_ATTR_PUB) {
//                        break;
//                    }
                }
                triples.addAll(mauthor.map(author));
            } else if (requestUrl.contains("https://scholar.google.com/citations?view_op=view_citation")) {
                // Extract information of each publication URL

                Publication p = new Publication();
                p.setUrl(requestUrl);
//                for (Publication publication : author.getPublications()) {
//                    if (requestUrl.contains(publication.getUrl())) {
//                        p = publication;
//                        break;
//                    }
//                }
                extract(input, new PublicationHandler(p));
                triples.addAll(mauthor.map(p));
            }

        } catch (MalformedURLException ex) {
            java.util.logging.Logger.getLogger(GoogleScholarProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            java.util.logging.Logger.getLogger(GoogleScholarProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(GoogleScholarProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            java.util.logging.Logger.getLogger(GoogleScholarProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GoogleScholarProvider.class.getName()).log(Level.SEVERE, null, ex);
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
        return authors.get(0);
    }

    private void sleep(int ms) throws InterruptedException {
        Thread.sleep(ms);
    }

    private void extract(InputStream input, DefaultHandler handler) throws MalformedURLException, SAXException, InterruptedException {
        int tries = 0;
        while (true) {
            try {
                XMLReader xr = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");

                xr.setContentHandler(handler);
                InputSource is = new InputSource(input);
                is.setEncoding("iso-8859-1");
                xr.parse(is);
                sleep(5000);
                break;
            } catch (IOException e) {
                tries++;
                log.error(String.format("TRIES: %s \n", tries), e);
                final int two_hour = 2 * 60 * 60 * 1000;
                log.info("WAITING TWO HOURS....");
                sleep(two_hour);
            }
        }
    }

}
