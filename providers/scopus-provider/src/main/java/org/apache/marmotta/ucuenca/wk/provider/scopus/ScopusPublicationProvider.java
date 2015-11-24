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
package org.apache.marmotta.ucuenca.wk.provider.scopus;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.vocabulary.FOAF;

import com.google.common.base.Preconditions;
import java.io.BufferedReader;

import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.xpath.XPathFactory;
import org.openrdf.model.Model;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdom2.Content;
import org.jdom2.Namespace;


/**
 * Support Scopus Publications information as XML
 * <p/>
 * Author: Jose Luis Cullcay
 */
public class ScopusPublicationProvider extends AbstractHttpProvider {

    public static final String NAME = "Scopus Publication Provider";
    public static final String API = "http://api.elsevier.com/content/abstract/doi/%s&format=xml";
    public static final String SERVICE_PATTERN = "http://api\\.elsevier\\.com/content/abstract/doi/(.*)\\?apiKey\\=(.*)\\&httpAccept\\=application/xml";
    public static final String PATTERN = "http://api\\.elsevier\\.com/content/abstract/doi/(.*)\\?apiKey\\=(.*)\\&httpAccept\\=application/xml";
    //public static final String urlPublicationResource = "http://api.elsevier.com/content/abstract/doi/doiParam?apiKey=apiKeyParam&httpAccept=application/xml";

    private static String nsUcuenca = "https://www.cedia.org.ec/";
    private static Logger log = LoggerFactory.getLogger(ScopusPublicationProvider.class);
    private Namespace namespace_dc;
    private Namespace namespace_prism;
    private String doiParam;
    private static String apiKeyParam = "";

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
        Matcher m = Pattern.compile(SERVICE_PATTERN).matcher(resource);
        if (m.find()) {
            url = resource;
            //doiParam = m.group(1);
            apiKeyParam = m.group(1);
        } else {
            Preconditions.checkState(StringUtils.isNotBlank(resource));
            String id = resource.substring(resource.lastIndexOf('/') + 1);
            url = String.format(API, id.replace('_', '+'));
        }
        return Collections.singletonList(url);
    }
    
    @Override
    public List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {
        log.debug("Request Successful to {0}", requestUrl);
        try {
            ValueFactory factory = ValueFactoryImpl.getInstance();
            final Document doc = new SAXBuilder(XMLReaders.NONVALIDATING).build(input);
            Element aux = doc.getRootElement();
            Element elementoCoreData = aux.getChild("coredata", aux.getNamespace());
            
            namespace_dc = Namespace.getNamespace("dc", "http://purl.org/dc/elements/1.1/");
            namespace_prism = Namespace.getNamespace("prism", "http://prismstandard.org/namespaces/basic/2.0/");
            
            //aux.getChildText("url", namespace_prism);
            
            String publicacionUrl = elementoCoreData!= null ? elementoCoreData.getChildText("url", namespace_prism) : "";
            
            if (publicacionUrl != null && !publicacionUrl.equals("")){
                triples.add(factory.createStatement(factory.createURI(resource), FOAF.member, factory.createURI(publicacionUrl)));

                /*ClientConfiguration conf = new ClientConfiguration();
                LDClient ldClient = new LDClient(conf);
                
                Model candidateModel = null;
                    String publicationUrlResourceCleaned = urlPublicationRawResource.replace("doiParam", doiParam).replace("apiKeyParam", apiKeyParam);
                    ClientResponse response = ldClient.retrieveResource(publicationUrlResourceCleaned);
                    Model publicationsModel = response.getData();
                    if (candidateModel == null) {
                        candidateModel = publicationsModel;
                    } else {
                        candidateModel.addAll(publicationsModel);
                    }*/

                    //triples.addAll(candidateModel);
                
            }

        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing HTML response", e);
        } catch (JDOMException e) {
            throw new DataRetrievalException("could not parse XML response. It is not in proper XML format", e);
        }
        return Collections.emptyList();
    }

    protected static List<Element> queryElements(Document n, String query) {
        return XPathFactory.instance().compile(query, new ElementFilter()).evaluate(n);
    }

}
