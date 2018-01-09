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
package ec.edu.cedia.redi.ldclient.provider;

import com.google.common.collect.ImmutableList;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.provider.xml.AbstractXMLDataProvider;
import org.apache.marmotta.ldclient.provider.xml.mapping.XPathLiteralMapper;
import org.apache.marmotta.ldclient.provider.xml.mapping.XPathURIMapper;
import org.apache.marmotta.ldclient.provider.xml.mapping.XPathValueMapper;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.apache.tika.io.IOUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParserRegistry;

/**
 * Support Scopus Author information as XML and RDF + XML. It returns all
 * authors along with its publications.
 *
 * @author Freddy Sumba
 * @author Jose Luis Cullcay
 * @author Xavier Sumba
 */
public class ScopusAuthorProvider extends AbstractXMLDataProvider {

    private String apiKey;

    // Namespaces used to map XML.
    private static final ConcurrentHashMap<String, String> SCOPUS_NAMESPACES = new ConcurrentHashMap<>();

    static {
        SCOPUS_NAMESPACES.put("prism", "http://prismstandard.org/namespaces/basic/2.0/");
        SCOPUS_NAMESPACES.put("dc", "http://purl.org/dc/elements/1.1/");
        SCOPUS_NAMESPACES.put("atom", "http://www.w3.org/2005/Atom");
        SCOPUS_NAMESPACES.put("opensearch", "http://a9.com/-/spec/opensearch/1.1/");
    }
    // Table describing mapping for XML results to RDF.
    private final ConcurrentHashMap<String, XPathValueMapper> authorsMapping = new ConcurrentHashMap<>();
    // Name of the provider to match with an endpoint.
    public static final String NAME = "Scopus Author Search";
    // Matches URLs from a response of author search in SCOPUS; it returns XML.
    public static final String PATTERN = "http://api\\.elsevier\\.com/content/search/author\\?.*\\&apiKey\\=(.*)?\\&?.*";
    private final Pattern patternAuthorSearch = Pattern.compile(PATTERN);
    // Matches author's profile in RDF.
    private final Pattern patternAuthorRetrieve = Pattern.compile("http://api.elsevier.com/content/author/author_id/.*");
    // Matches query to search a Scopus to find all publications from an author. It returns XML.
    private final Pattern patternPublicationSearch = Pattern.compile("http://api.elsevier.com/content/search/scopus.*");
    // Matches a publication resource.
    private final Pattern patternPublicationRetrieve = Pattern.compile("http://api.elsevier.com/content/abstract/scopus_id/.*");

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
            "application/xml",
            "application/rdf+xml"
        };
    }

    /**
     * Build the URL to use to call the web service in order to retrieve the
     * data for the resource passed as argument. URL should include an apyKey
     * query to access Scopus API.
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
            apiKey = m.group(1);
            return Collections.singletonList(url);
        } else {
            throw new RuntimeException("Invalid Scopus Author Search URI: " + resource);
        }
    }

    /**
     * Parse the HTTP response entity returned by the web service call and
     * return its contents as a Sesame RDF repository. There are four web
     * services that can be called. The method makes many calls to Scopus API,
     * so if it can return all data, it throws a {@link DataRetrievalException}.
     * <p>
     * The logic to return authors with its publications consists in four steps:
     *
     * <ol>
     * <li>Search for authors.</li>
     * <li>Retrieve authors data.</li>
     * <li>Search publications for each author found.</li>
     * <li>Retrieve publications data and assign to its corresponding
     * author.</li>
     * </ol>
     *
     *
     *
     * @see
     * <a href="https://dev.elsevier.com/academic_research_scopus.html">Academic
     * Research</a>, <a href="https://dev.elsevier.com/sc_apis.html">Elsevier
     * Scopus APIs</a>,
     * <a href="http://api.elsevier.com/documentation/AUTHORSearchAPI.wadl">Authors
     * search API</a>,
     * <a href="https://api.elsevier.com/documentation/AuthorRetrievalAPI.wadl">Author
     * retrieval API</a>,
     * <a href="http://api.elsevier.com/documentation/SCOPUSSearchAPI.wadl">
     * Scopus Search API</a>, and
     * <a href="http://api.elsevier.com/documentation/AbstractRetrievalAPI.wadl">
     * Abstract Retrieval API</a>.
     *
     * @param resource
     * @param requestUrl
     * @param triples
     * @param input
     * @param contentType
     * @return
     * @throws DataRetrievalException
     */
    @Override
    public List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {

        try {
            if (patternAuthorSearch.matcher(requestUrl).matches()) {
                return parseResponseAuthorsSearch(input, resource, requestUrl, triples, contentType);
            } else if (patternAuthorRetrieve.matcher(requestUrl).matches()) {
                return parseResponseAuthorsProfile(input, requestUrl, triples, contentType);
            } else if (patternPublicationSearch.matcher(requestUrl).matches()) {
                return parseSearchPub(input, requestUrl, triples);
            } else if (patternPublicationRetrieve.matcher(requestUrl).matches()) {
                // store all RDF.
                ModelCommons.add(triples, input, requestUrl, RDFFormat.RDFXML);
                return Collections.emptyList();
            } else {
                throw new DataRetrievalException("Cannot find pattern for URL: " + requestUrl);
            }

        } catch (IOException | RDFParseException ex) {
            throw new DataRetrievalException("Cannot read input stream.", ex);
        }
    }

    /**
     * Parse each XML result of publications. Assings each publication resource
     * to its author. See
     * <a href="http://api.elsevier.com/documentation/SCOPUSSearchAPI.wadl">Scopus
     * Search API</a>.
     *
     * @param input
     * @param requestUrl
     * @param triples
     * @return list of publication resources
     * @throws DataRetrievalException
     */
    private List<String> parseSearchPub(InputStream input, String requestUrl, final Model triples) throws DataRetrievalException {
        try {
            List<String> publications = new ArrayList<>();
            ValueFactory vf = ValueFactoryImpl.getInstance();
            String authorId = requestUrl.substring(requestUrl.indexOf("au-id(") + 6, requestUrl.indexOf(")&"));
            URI author = vf.createURI("http://api.elsevier.com/content/author/author_id/", authorId);

            final Document doc = new SAXBuilder(XMLReaders.NONVALIDATING).build(input);
            XPathExpression<Attribute> path = XPathFactory
                    .instance()
                    .compile("/atom:search-results/atom:entry/atom:link[@ref='self']/@href",
                            Filters.attribute(), null,
                            Namespace.getNamespace("atom", "http://www.w3.org/2005/Atom"));
            List<Attribute> publicationsFound = path.evaluate(doc);
            for (int i = 0; i < publicationsFound.size(); i++) {
                String pubResource = publicationsFound.get(i).getValue();
                triples.add(author, FOAF.PUBLICATIONS, vf.createURI(pubResource));
                publications.add(pubResource + "?apiKey=" + apiKey + "&httpAccept=application/rdf%2Bxml");
            }
            return publications;
        } catch (JDOMException | IOException ex) {
            throw new DataRetrievalException(ex);
        }
    }

    /**
     * Parse all information retrieved from the call to the web service. See
     * <a href="https://api.elsevier.com/documentation/AuthorRetrievalAPI.wadl">Author
     * retrieval API.</a>
     *
     * @param input
     * @param requestUrl
     * @param triples
     * @param contentType
     * @return list of resources of abstract documents associated with an
     * author.
     * @throws DataRetrievalException
     */
    private List<String> parseResponseAuthorsProfile(InputStream input, String requestUrl, final Model triples, String contentType) throws DataRetrievalException {
        try {
            //<editor-fold defaultstate="collapsed" desc="Fix URIs of scopus. There's no protocol in resources. Delete this section once Scopus fix this issue.">
            BufferedReader buffer = new BufferedReader(new InputStreamReader(input));
            StringBuilder parsed = new StringBuilder();
            String line = buffer.readLine();
            while (line != null) {
                if (line.contains("://api.elsevier.com")) {
                    line = line.replaceAll("://api.elsevier.com", "http://api.elsevier.com");
                }
                parsed.append(line);
                line = buffer.readLine();
            }
            input = IOUtils.toInputStream(parsed.toString());
            //</editor-fold>

            RDFFormat format = RDFParserRegistry.getInstance().getFileFormatForMIMEType(contentType, RDFFormat.RDFXML);
            ModelCommons.add(triples, input, requestUrl, format);

            String authorId = requestUrl.substring(
                    requestUrl.indexOf("/author_id/") + 11,
                    requestUrl.indexOf('?'));
            return Collections.singletonList(
                    String.format("http://api.elsevier.com/content/search/scopus?query=au-id(%s)&apiKey=%s&view=complete",
                            authorId, apiKey)
            );
        } catch (IOException | RDFParseException ex) {
            throw new DataRetrievalException(ex);
        }
    }

    /**
     * Maps each author from XML to RDF using default implementation of
     * {@link AbstractXMLDataProvider#parseResponse}.
     *
     * @see
     * <a href="http://api.elsevier.com/documentation/AUTHORSearchAPI.wadl">Authors
     * search API.</a>
     *
     * @param input
     * @param resource
     * @param requestUrl
     * @param triples
     * @param contentType
     * @return list of resources of authors found.
     * @throws DataRetrievalException
     */
    private List<String> parseResponseAuthorsSearch(InputStream input, String resource, String requestUrl, Model triples, String contentType) throws DataRetrievalException {
        try {

            // List of authors to extract perfil information such as publications, affiliations, etc.
            List<String> authorsFound = new ArrayList();
            ValueFactory vf = ValueFactoryImpl.getInstance();
            // Keep stream for various reads.
            byte[] response = IOUtils.toByteArray(input);
            final Document doc = new SAXBuilder(XMLReaders.NONVALIDATING).build(new ByteArrayInputStream(response));
            // get only URI of authors
            XPathExpression<Text> path = XPathFactory
                    .instance()
                    .compile("/atom:search-results/atom:entry/prism:url/text()",
                            Filters.textOnly(), null,
                            Namespace.getNamespace("atom", "http://www.w3.org/2005/Atom"),
                            Namespace.getNamespace("prism", "http://prismstandard.org/namespaces/basic/2.0/"));
            // Map each author XML to RDF using default implementationf parseResponse method from AbstractXMLDataProvider.
            List<Text> auhtorsFound = path.evaluate(doc);
            for (int i = 0; i < auhtorsFound.size(); i++) {
                setAuthorXPathMappings(i);
                String authorsResource = auhtorsFound.get(i).getValue();
                super.parseResponse(authorsResource, requestUrl, triples, new ByteArrayInputStream(response), contentType);
                authorsFound.add(authorsResource + "?apiKey=" + apiKey + "&httpAccept=application/rdf%2Bxml&view=ENHANCED");
                triples.add(vf.createURI(authorsResource), OWL.ONEOF, vf.createURI(resource));
            }
            return authorsFound;
        } catch (JDOMException | IOException | DataRetrievalException ex) {
            throw new DataRetrievalException(ex);
        }
    }

    /**
     * Modifies XPath to get information about ith result.
     *
     * @param i starts in 1.
     */
    private void setAuthorXPathMappings(int i) {
        String entrypoint = String.format("/atom:search-results/atom:entry[%s]", i + 1);
        authorsMapping.put(REDI.SCOPUS_AUTHOR_ID.toString(), new XPathURIMapper("/dc:identifier", SCOPUS_NAMESPACES));
        authorsMapping.put(REDI.ORCID.toString(), new XPathLiteralMapper(entrypoint + "/atom:orcid/text()", SCOPUS_NAMESPACES));
        authorsMapping.put(REDI.EID.toString(), new XPathLiteralMapper(entrypoint + "/atom:eid/text()", SCOPUS_NAMESPACES));
        authorsMapping.put(REDI.SUBJECT_AREA.toString(), new XPathLiteralMapper(entrypoint + "/atom:subject-area/text()", SCOPUS_NAMESPACES));
        authorsMapping.put(REDI.SURNAME.toString(), new XPathLiteralMapper(entrypoint + "/atom:preferred-name/atom:surname/text()", SCOPUS_NAMESPACES));
        authorsMapping.put(REDI.GIVEN_NAME.toString(), new XPathLiteralMapper(entrypoint + "/atom:preferred-name/atom:given-name/text()", SCOPUS_NAMESPACES));
        authorsMapping.put(REDI.INITIALS.toString(), new XPathLiteralMapper(entrypoint + "/atom:preferred-name/atom:initials/text()", SCOPUS_NAMESPACES));
        authorsMapping.put(REDI.AFFILIATION_NAME.toString(), new XPathLiteralMapper(entrypoint + "/atom:affiliation-current/atom:affiliation-name/text()", SCOPUS_NAMESPACES));
    }

    @Override
    protected Map<String, XPathValueMapper> getXPathMappings(String requestUrl) {
        if (patternAuthorSearch.matcher(requestUrl).matches()) {
            return authorsMapping;
        }
        throw new RuntimeException("Cannot parse the URL: " + requestUrl);
    }

    @Override
    protected List<String> getTypes(URI resource) {
        // http://api.elsevier.com/content/author/author_id/57193429229 - PERSON
        return ImmutableList.of(FOAF.PERSON.toString(), REDI.SCOPUS_PROVIDER.toString());
    }
}
