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

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

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
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ucuenca.wk.commons.function.Delay;
import org.apache.marmotta.ucuenca.wk.commons.function.URLUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;

/**
 * Support DBLP Author information as RDF
 * <p/>
 * Author: Santiago Gonzalez
 */
public class DBLPRawProvider extends AbstractHttpProvider {

    private static Logger log = LoggerFactory.getLogger(DBLPRawProvider.class);
    public static final String NAME = "DBLP Raw Provider";
    public static final String PATTERN = "http(s?)://rdf\\.dblp\\.com/ns/search/.*";
    public static final String SEARCHAPI = "http://dblp.uni-trier.de/search/author/api?q=%s&format=xml";
    public static final String SERVICE_PATTERN = "http://dblp\\.uni\\-trier\\.de/search/author/api\\?q\\=(.*)(\\&format\\=xml)?$";
    public static ConcurrentMap<String, String> dblpNamespaces = new ConcurrentHashMap<String, String>();
    private static final String DBLP = "dblp";

    static {
        dblpNamespaces.put("dblp", "http://dblp.org/rdf/schema-2017-04-18#");
        dblpNamespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        dblpNamespaces.put("owl", "http://www.w3.org/2002/07/owl#");
        dblpNamespaces.put("dcterms", "http://purl.org/dc/terms/");
        dblpNamespaces.put("foaf", "http://xmlns.com/foaf/0.1");
        dblpNamespaces.put("bibtex", "http://data.bibbase.org/ontology/#");
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
        } else {
            Preconditions.checkState(StringUtils.isNotBlank(resource));
            String query = resource.substring(resource.lastIndexOf('/') + 1);
            //remove special character ’ which is not supported by DBLP
            query = query.replaceAll("%E2%80%99", "");
            String id = URLDecoder.decode(query);
            url = String.format(SEARCHAPI, URLEncoder.encode(id.replace('_', ' ').replace('-', '|')));
        }
        return Collections.singletonList(url);
    }

    @Override
    public List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {
        log.debug("Request Successful to {0}", requestUrl);
        Delay.call();
        if (requestUrl.startsWith("http://dblp.uni-trier.de")) {
            return parseAuthorSearch(requestUrl, input, triples, resource);
        } else if (requestUrl.startsWith("http://dblp.org/pers") || requestUrl.startsWith("https://dblp.org/pers")) {
            return parseAuthor(input, triples);
        } else if (requestUrl.startsWith("http://dblp.org/rec") || requestUrl.startsWith("https://dblp.org/rec")) {
            return parsePublication(requestUrl, input, triples);
        }

        return Collections.emptyList();
    }

    private List<String> parseAuthorSearch(String requestUrl, InputStream input, Model triples, String resource) throws DataRetrievalException {
        List<String> lsURLs = new ArrayList<>();
        try {
            final Document doc = new SAXBuilder(XMLReaders.NONVALIDATING).build(input);
            if (requestUrl.endsWith("format=xml")) {
                List<Element> queryElements = queryElements(doc, "/result/hits");
                Element get = queryElements.get(0);
                int totalResponse = Integer.parseInt(get.getAttributeValue("total"));
                for (int i = 0; i < totalResponse; i += 30) {
                    lsURLs.add(requestUrl + "&h=30&f=" + i);
                }
            } else {
                ValueFactory factory = ValueFactoryImpl.getInstance();
                for (Element element : queryElements(doc, "/result/hits/hit/info/url")) {
                    String candidate = element.getText();
                    candidate = candidate.replaceFirst("pid", "rec/pid");
                    candidate = URLUtils.getFinalURL(candidate, 0);
                    String candidateURI = candidate.replaceFirst("/hd", "");
                    triples.add(factory.createStatement(factory.createURI(candidateURI), OWL.ONEOF, factory.createURI(resource)));
                    lsURLs.add(candidate.replaceFirst("/hd/", "/xr/").concat(".rdf"));
                }

            }
        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing HTML response", e);
        } catch (JDOMException e) {
            throw new DataRetrievalException("could not parse XML response. It is not in proper XML format", e);
        }
        return lsURLs;
    }

    private List<String> parseAuthor(InputStream input, Model triples) throws DataRetrievalException {
        List<String> lsl = new ArrayList<>();
        ValueFactory factory = ValueFactoryImpl.getInstance();
        try {
            ModelCommons.add(triples, input, dblpNamespaces.get(DBLP), RDFFormat.RDFXML);
        } catch (UnsupportedRDFormatException e) {
            throw new DataRetrievalException("Error while parsing response. Unsupported format RDFXML", e);
        } catch (RDFParseException e) {
            throw new DataRetrievalException("Error while parsing response", e);
        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing response", e);
        }
        Model publications = triples.filter(null, factory.createURI(dblpNamespaces.get(DBLP) + "authorOf"), null);
        Set<Value> resources = publications.objects();
        for (Value dblpResource : resources) {
            String resourceDoc = ((Resource) dblpResource).stringValue();
            resourceDoc = resourceDoc.replaceFirst("rec", "rec/rdf").concat(".rdf");
            lsl.add(resourceDoc);
        }
        return lsl;
    }

    private List<String> parsePublication(String requestUrl, InputStream input, Model triplesx) throws DataRetrievalException {
        List<String> lsl = new ArrayList<>();
        try {
            String resourcet = requestUrl.replaceFirst("rec/rdf", "rec");
            String resource = resourcet.substring(0, resourcet.length() - 4);

            Model triples = new LinkedHashModel();
            ValueFactory factory = ValueFactoryImpl.getInstance();
            ModelCommons.add(triples, input, dblpNamespaces.get("dblp"), RDFFormat.RDFXML);
            Model unmodifiable = triples.unmodifiable();
            Model coauthors = unmodifiable.filter(null, factory.createURI(dblpNamespaces.get(DBLP) + "authoredBy"), null);
            Set<Value> coauthorsList = coauthors.objects();
            ConcurrentHashMap<String, String> lfn = new ConcurrentHashMap();
            ConcurrentHashMap<String, String> lln = new ConcurrentHashMap();
            ConcurrentHashMap<String, String> lffn = new ConcurrentHashMap();
            for (Value coauthorURI : coauthorsList) {
                String coauthorURIS = ((Resource) coauthorURI).stringValue();
                String codedName = coauthorURIS.substring(coauthorURIS.lastIndexOf('/') + 1);
                String[] decodedName = extractNameDBLP(codedName);
                String lastName = decodedName[0];
                String firstName = decodedName[1];
                lfn.put(coauthorURI.stringValue(), firstName);
                lln.put(coauthorURI.stringValue(), lastName);
                lffn.put(coauthorURI.stringValue(), lastName + " , " + firstName);
            }
            addCreator(triples);
            for (Map.Entry<String, String> en : lfn.entrySet()) {
                triples.add(factory.createURI(en.getKey()), factory.createURI(dblpNamespaces.get(DBLP) + "firstName"), factory.createLiteral(en.getValue()));
            }
            for (Map.Entry<String, String> en : lln.entrySet()) {
                triples.add(factory.createURI(en.getKey()), factory.createURI(dblpNamespaces.get(DBLP) + "lastName"), factory.createLiteral(en.getValue()));
            }
            for (Map.Entry<String, String> en : lffn.entrySet()) {
                triples.add(factory.createURI(en.getKey()), factory.createURI(dblpNamespaces.get(DBLP) + "otherFullPersonName"), factory.createLiteral(en.getValue()));
            }
            Model superpublication = unmodifiable.filter(null, factory.createURI(dblpNamespaces.get(DBLP) + "publishedAsPartOf"), null);
            Set<Value> pubList = superpublication.objects();
            for (Value superp : pubList) {
                String supURI = ((Resource) superp).stringValue();
                if (supURI.compareTo(resource) != 0) {
                    lsl.add((supURI.replaceFirst("rec", "rec/rdf").concat(".rdf")));
                }
            }
            triplesx.addAll(triples);
        } catch (RDFParseException e) {
            throw new DataRetrievalException("Error while parsing response", e);
        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing response", e);
        }
        return lsl;
    }

    protected static List<Element> queryElements(Document n, String query) {
        return XPathFactory.instance().compile(query, new ElementFilter()).evaluate(n);
    }

    private void addCreator(Model triples) {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        Model unmodifiable = triples.unmodifiable();
        Model coauthors = unmodifiable.filter(null, factory.createURI(dblpNamespaces.get(DBLP) + "authoredBy"), null);
        String creator = null;
        String document = null;
        for (Statement one : coauthors) {
            document = ((Resource) one.getSubject()).stringValue();
            creator = ((Resource) one.getObject()).stringValue();
            if (document != null && creator != null) {
                break;
            }
        }
        if (creator != null && document != null) {
            triples.add(factory.createURI(document), factory.createURI(dblpNamespaces.get(DBLP) + "creator"), factory.createURI(creator));
        }
    }

    private String[] extractNameDBLP(String name) {
        String[] result = name.split(":");
        result[0] = URLUtils.unEscapeHTML4(result[0]);
        result[1] = URLUtils.unEscapeHTML4(result[1]);
        return result;
    }

}
