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

//import org.apache.commons.lang3.StringUtils;
//import org.apache.marmotta.commons.vocabulary.FOAF;
//import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.GSXMLHandler;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.GSresult;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.JSONtoRDF;
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
//import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
//import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Support Google Scholar information as RDF
 * <p/>
 * Author: Santiago Gonzalez
 */
public class GoogleScholarProvider extends AbstractHttpProvider {

    public static final String NAME = "Google Scholar Provider";
    public static final String API = "http://scholar.google.com/scholar?start=%s&q=author:%%22%s%%22%s&hl=en&as_sdt=1%%2C15&as_vis=1%s";
    public static final String PATTERN = "http(s?)://scholar\\.google\\.com/scholar\\?start\\=0\\&q=author\\:%22(.*)%22\\&hl=en\\&as_sdt\\=1%2C15\\&as_vis\\=1(.*)$";
    private static String nsUcuenca = "https://www.cedia.org.ec/";

    private static Logger log = LoggerFactory.getLogger(GoogleScholarProvider.class);

    private String stringSearch = null, authorSearch = null, advancedSearch = null;

    public static final ConcurrentMap<String, String> MAPPINGSCHEMA = new ConcurrentHashMap<String, String>();

    static {
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
            stringSearch = m.group(2);
            authorSearch = m.group(3);
            advancedSearch = m.group(3);
            log.debug("Extracting info for: {0}", stringSearch);
            if (authorSearch.length() > 0) {
                log.debug("Extra author search parameters: {0}", authorSearch);
            }
            if (advancedSearch.length() > 0) {
                log.debug("Advanced search parameters: {0}", advancedSearch);
            }
            url = resource;
        }
        return Collections.singletonList(url);
    }

    @Override
    public List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {
        log.debug("Request Successful to {0}", requestUrl);
        final GSXMLHandler gsXMLHandler = new GSXMLHandler();
        gsXMLHandler.clearGSresultList();
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
            xr.setContentHandler(gsXMLHandler);
            InputSource gsxml = new InputSource(input);
            gsxml.setEncoding("iso-8859-1");
            xr.parse(gsxml);

            final Set<GSresult> gsresultlist = gsXMLHandler.getGSresultList();
            Gson gson = new Gson();
            JsonArray json = new JsonArray();
            for (GSresult d : gsresultlist) {
                json.add(gson.toJsonTree(d).getAsJsonObject());
            }
            JSONtoRDF parser = new JSONtoRDF(resource, MAPPINGSCHEMA, json, triples);
            try {
                parser.parse();
            } catch (Exception e) {
                throw new DataRetrievalException("I/O exception while retrieving resource: " + requestUrl, e);
            }
            int numPages = (int) ((double) (gsXMLHandler.getNumResults() / 10)) + 1;
            int pagesLoaded = 1;
            Model model = null;
            while (pagesLoaded < numPages) {
                String pagenumquery = Integer.toString(pagesLoaded * 10);
                String moreDataUrl = String.format(API, pagenumquery, stringSearch, authorSearch, advancedSearch);
                ClientConfiguration conf = new ClientConfiguration();
                LDClient ldClient = new LDClient(conf);
                ClientResponse response = ldClient.retrieveResource(moreDataUrl);
                Model pageModel = response.getData();
                if (model == null) {
                    model = pageModel;
                } else {
                    model.addAll(pageModel);
                }
                pagesLoaded++;
            }
            triples.addAll(model);

        } catch (SAXException | IOException e) {
            throw new DataRetrievalException("I/O exception while retrieving resource: " + requestUrl, e);
        }

//    	try {
//    		List<String> candidates = new ArrayList<String>();
//    		ValueFactory factory = ValueFactoryImpl.getInstance();
//    		final Document doc = new SAXBuilder(XMLReaders.NONVALIDATING).build(input);
//	    	for(Element element: queryElements(doc, "/result/hits/hit/info/url")) {
//	    		String candidate = element.getText();
//	    		triples.add(factory.createStatement(factory.createURI( resource ), FOAF.member, factory.createURI( candidate ) ));
//	    		candidates.add(candidate);
//	    	}
//	    	ClientConfiguration conf = new ClientConfiguration();
//	        LDClient ldClient = new LDClient(conf);
//	        if(!candidates.isEmpty()) {
//		        Model candidateModel = null;
//		    	for(String author: candidates) {
//		    		ClientResponse response = ldClient.retrieveResource(author);
//		        	Model authorModel = response.getData();
//		        	if(candidateModel == null) {
//		        		candidateModel = authorModel;
//		        	} else {
//		        		candidateModel.addAll(authorModel);
//		        	}
//		    	}
//		    	triples.addAll(candidateModel);
//	        }
//    	}catch (IOException e) {
//            throw new DataRetrievalException("I/O error while parsing HTML response", e);
//        }catch (JDOMException e) {
//            throw new DataRetrievalException("could not parse XML response. It is not in proper XML format", e);
//        }
        return Collections.emptyList();
    }

//    protected static List<Element> queryElements(Document n, String query) {
//        return XPathFactory.instance().compile(query, new ElementFilter()).evaluate(n);
//    }
}
