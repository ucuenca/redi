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
package org.apache.marmotta.ucuenca.wk.provider.dblp;

import org.apache.marmotta.commons.sesame.model.ModelCommons;

import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.openrdf.model.Model;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.marmotta.ucuenca.wk.commons.function.Delay;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Support DBLP Resource Data as RDF
 * <p/>
 * Author: Santiago Gonzalez
 */
public class DBLPResourceRawProvider extends AbstractHttpProvider {

    public static final String NAME = "DBLP Resource Raw Provider";
    public static final String PATTERN = "(http://dblp\\.org/rec/)(.*)";
    //public static final String LEGACY_PATTERN = "(http://dblp\\.uni\\-trier\\.de/rec/)(.*)";

    private static Logger log = LoggerFactory.getLogger(DBLPResourceRawProvider.class);

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
            "application/rdf+xml"
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
        Delay.call();
        return Collections.singletonList(resource.replaceFirst("/html/", "/rdf/").concat(".rdf"));
    }

    @Override
    public List<String> parseResponse(final String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {
        log.debug("Request {0} succesful", requestUrl);
        RDFFormat format = RDFFormat.forMIMEType(contentType);
        try {
            ValueFactory factory = ValueFactoryImpl.getInstance();
            ModelCommons.add(triples, input, resource, format);
            /*ValueFactory factory = ValueFactoryImpl.getInstance();
			Resource subject = triples.subjects().iterator().next();
			triples.add(subject, OWL.SAMEAS, factory.createURI(resource));*/
            //Optional Add coauthors names
            try {
                Model unmodifiable = triples.unmodifiable();
                Model coauthors = unmodifiable.filter(null, factory.createURI(DBLPAuthorRawProvider.dblpNamespaces.get("dblp") + "authoredBy"), null);
                Set<Value> coauthorsList = coauthors.objects();
                ConcurrentHashMap<String, String> ls = new ConcurrentHashMap();
                if (!coauthorsList.isEmpty()) {
                    for (Value coauthorURI : coauthorsList) {
                        String coauthorURIS = ((Resource) coauthorURI).stringValue();
                        String codedName = coauthorURIS.substring(coauthorURIS.lastIndexOf('/') + 1);
                        String[] decodedName = extractNameDBLP(codedName);
                        String lastName = decodedName[0];
                        String firstName = decodedName[1];
                        ls.put(coauthorURI.stringValue(), lastName + ", " + firstName);
                    }
                }
                for (Entry<String, String> en : ls.entrySet()) {
                    triples.add(factory.createURI(en.getKey()), factory.createURI(DBLPAuthorRawProvider.dblpNamespaces.get("dblp") + "otherFullPersonName"), factory.createLiteral(en.getValue()));
                }
            } catch (Exception oi) {
                log.debug(oi.toString());
            }
        } catch (RDFParseException e) {
            throw new DataRetrievalException("Error while parsing response", e);
        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing response", e);
        }
        return Collections.emptyList();
    }

    private String[] extractNameDBLP(String name) {
        String[] result = name.split(":");
        result[0] = unEscapeHTML4(result[0]);
        result[1] = unEscapeHTML4(result[1]);
        return result;
    }

    private String unEscapeHTML4(String name) {
        char eq='=';
        String finalResult = "";
        boolean betweenEqs = false;
        String partialText = "";
        for (char a : name.toCharArray()) {
            partialText += a;
            if (a == eq) {
                if (!betweenEqs) {
                    betweenEqs = true;
                    partialText = "=";
                } else {
                    String unescapeText = partialText.replaceAll("\\=([^=:]{1,})\\=", "&$1;");
                    String escapeText = StringEscapeUtils.unescapeHtml4(unescapeText);
                    if (!escapeText.equals(unescapeText) ) {
                        finalResult += escapeText;
                        betweenEqs = false;
                        partialText = "";
                        continue;
                    } else {
                        finalResult += partialText.substring(0, partialText.length() - 1);
                        partialText = "=";
                        betweenEqs = true;
                    }
                }
            }
            if (!betweenEqs) {
                partialText = "";
                finalResult += a;
            }
        }
        finalResult += partialText;
        String stopChars = "\\=|\\-|\\_|\\.|\\||\\&|\\?";
        finalResult = finalResult.replaceAll(stopChars, " ").replaceAll("(\\s+)", " ");
        return finalResult.trim();
    }

}
