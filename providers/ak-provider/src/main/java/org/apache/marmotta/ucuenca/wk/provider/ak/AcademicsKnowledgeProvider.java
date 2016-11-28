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
package org.apache.marmotta.ucuenca.wk.provider.ak;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.io.BufferedReader;

import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;

import org.openrdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.marmotta.commons.vocabulary.FOAF;
import org.apache.marmotta.ucuenca.wk.provider.ak.util.JSONtoRDF;
import org.apache.marmotta.ucuenca.wk.provider.ak.util.MapPublications;
import org.apache.marmotta.ucuenca.wk.provider.ak.util.Publication;
import org.json.simple.parser.ParseException;

/**
 * Support Google Scholar information as RDF
 * <p/>
 * Author: Santiago Gonzalez
 */
public class AcademicsKnowledgeProvider extends AbstractHttpProvider {

    public static final String NAME = "Academics Knowlodge Provider";
    public static final String API = "https://api.projectoxford.ai/academic/v1.0/evaluate?expr=Composite(AA.AuN==%27victor%20saquicela%27)&attributes=Id,Ti,Y,D,CC,ECC,AA.AuN,AA.AuId,AA.AfN,AA.AfId,F.FN,F.FId,J.JN,J.JId,C.CN,C.CId,RId,W,E,D&E=DN,D,S,S.Ty,S.U,VFN,VSN,V,I,FP,LP,DOI&subscription-key=f66e8b1a39634d9591151a8efd80cfc2";
    public static final String PATTERN = "https://api\\.projectoxford\\.ai/academic/(.*)";
    //https://api\\.projectoxford\\.ai/academic/v1\\.0/evaluate?expr\\=Composite\\(AA\\.AuN==%27(.*)%27)\\&attributes=Id,Ti,Y,D,CC,ECC,AA.AuN,AA.AuId,AA.AfN,AA.AfId,F.FN,F.FId,J.JN,J.JId,C.CN,C.CId,RId,W,E,D\\&E=DN,D,S,S.Ty,S.U,VFN,VSN,V,I,FP,LP,DOI\\&subscription-key=(.*)\\&count=100$
    public static final String PATTERNA = "http://academic\\.research\\.microsoft\\.com/json\\.svc/search\\?AppId\\=(.*)\\&AuthorQuery\\=(.*)\\&ResultObjects\\=Publication\\&PublicationContent\\=AllInfo\\&StartIdx\\=1\\&EndIdx\\=1(.*)$";

    private static String nsRedi = "http://redi.cedia.org.ec/namespace1_0/";
    private static Logger log = LoggerFactory.getLogger(AcademicsKnowledgeProvider.class);
    public String stringSearch = null, authorSearch = null, advancedSearch = null, appId = null;
    public static final ConcurrentMap<String, String> MAPPINGSCHEMA = new ConcurrentHashMap<String, String>();
    private MapPublications mapPublications = new MapPublications();
    private int limitPublications = 100;

    static {
        MAPPINGSCHEMA.put("entity::type", "http://purl.org/ontology/bibo/Document");
        MAPPINGSCHEMA.put("entity::property:uri", "http://purl.org/ontology/bibo/uri");
        MAPPINGSCHEMA.put("entity::property:title", "http://purl.org/dc/terms/title");
        MAPPINGSCHEMA.put("entity::property:abstractt", "http://purl.org/ontology/bibo/abstract");
        MAPPINGSCHEMA.put("entity::list:journals", "http://purl.org/ontology/bibo/Journal");
        MAPPINGSCHEMA.put("entity::property:creator", "http://purl.org/dc/elements/1.1/creator");
        MAPPINGSCHEMA.put("entity::property:year", "http://purl.org/dc/elements/1.1/date");
        MAPPINGSCHEMA.put("entity::property:created", "http://purl.org/dc/terms/created");
        MAPPINGSCHEMA.put("entity::property:doi", "http://purl.org/ontology/bibo/doi");
        MAPPINGSCHEMA.put("entity::property:authorlist", "http://purl.org/ontology/bibo/authorList");
        MAPPINGSCHEMA.put("entity::list:keyWord", "http://purl.org/ontology/bibo/Quote");
        MAPPINGSCHEMA.put("entity::property:conference", "http://purl.org/ontology/bibo/Conference");
        MAPPINGSCHEMA.put("entity::property:contributor", "http://purl.org/dc/terms/contributor");
        MAPPINGSCHEMA.put("entity::list:fields", "http://purl.org/dc/terms/subject");
        MAPPINGSCHEMA.put("entity::property:references", "http://purl.org/dc/terms/references");
        MAPPINGSCHEMA.put("entity::property:text", "http://purl.org/ontology/bibo/content");
        MAPPINGSCHEMA.put("entity::property:name", FOAF.name.toString());
        MAPPINGSCHEMA.put("entity::property:type", nsRedi + "Type");
        MAPPINGSCHEMA.put("entity::property:referenceCount", nsRedi + "referenceCount");
        MAPPINGSCHEMA.put("entity::property:citationCount", nsRedi + "citationCount");
        MAPPINGSCHEMA.put("entity::property:estimatedCitationCount", nsRedi + "estimatedCitationCount");
        MAPPINGSCHEMA.put("entity::property:fullversionurl", nsRedi + "FullVersionURL");
        MAPPINGSCHEMA.put("entity::property:affiliation", nsRedi + "affiliation");

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
        Matcher m = Pattern.compile(PATTERN).matcher(resource);
        if (m.find()) {

            url = resource;
        }
        return Collections.singletonList(url);
    }

    @Override
    public List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {
        log.debug("Request Successful to {0}", requestUrl);

        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr = streamReader.readLine();
            while (inputStr != null) {
                responseStrBuilder.append(inputStr);
                inputStr = streamReader.readLine();
            }
            List<Publication> resultOutput = new ArrayList();

            resultOutput = mapPublications.getPublications(responseStrBuilder.toString());
            if (resultOutput.size() == limitPublications) {
                resultOutput = resultOutput.subList(0, 20);
            }

            Gson gson = new Gson();
            JsonArray json = new JsonArray();
            for (Publication d : resultOutput) {
                json.add(gson.toJsonTree(d).getAsJsonObject());
            }
            JSONtoRDF parser = new JSONtoRDF(MAPPINGSCHEMA, json, triples);
            //Model model2 = new TreeModel();

            try {
                parser.parse();
            } catch (Exception e) {
                throw new DataRetrievalException("I/O exception while retrieving resource: " + requestUrl, e);
            }

        } catch (IOException e) {
            //e.printStackTrace();
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(AcademicsKnowledgeProvider.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Collections.emptyList();
    }

}
