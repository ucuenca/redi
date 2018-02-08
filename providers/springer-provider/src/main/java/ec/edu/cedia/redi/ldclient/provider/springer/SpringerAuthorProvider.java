/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ec.edu.cedia.redi.ldclient.provider.springer;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import ec.edu.cedia.redi.ldclient.provider.json.AbstractJSONDataProvider;
import ec.edu.cedia.redi.ldclient.provider.json.mappers.JsonPathLiteralMapper;
import ec.edu.cedia.redi.ldclient.provider.json.mappers.JsonPathValueMapper;
import ec.edu.cedia.redi.ldclient.provider.springer.utils.Utils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.BIBO;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;

/**
 * Support Springer information as JSON. Be aware that the provider makes extra
 * calls to retrieve publications of each author found.
 * <p>
 * @see <a href="https://dev.springer.com">Documentation guide</a>.
 *
 * @author Xavier Sumba
 */
public class SpringerAuthorProvider extends AbstractJSONDataProvider implements DataProvider {

    public static final String NAME = "Springer Author Provider";
    public static final String PATTERN = "http://api\\.springer\\.com/meta/v1/json\\?q=(.*)&api_key=.*&p=50&s=(.*)";
    public static final String SPRINGER_URL = "https://link.springer.com/";

    private final ConcurrentMap<String, JsonPathValueMapper> mapper = new ConcurrentHashMap<>();

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

    @Override
    protected List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {
        try {
            byte[] data = IOUtils.toByteArray(input); // keep data for some reads
            ValueFactory vf = ValueFactoryImpl.getInstance();
            String authorname = Utils.buildNameFromRequest(resource);
            ReadContext ctx = JsonPath.parse(new ByteArrayInputStream(data));
            Map resultsStatistics = ctx.read("$.result[0]");

            int docs = Integer.parseInt((String) resultsStatistics.get("recordsDisplayed"));

            for (int i = 0; i < docs; i++) {
                String doi;
                try {
                    doi = ctx.read(String.format("$.records[%s].identifier", i));
                } catch (Exception e) {
                    doi = String.valueOf(Math.random() * 100);
                }
                URI author = Utils.generateURI(SPRINGER_URL + "author/", authorname + doi);
                URI publication = Utils.generateURI(SPRINGER_URL + "publication/", doi);
                triples.add(author, FOAF.NAME, vf.createLiteral(authorname));
                triples.add(author, OWL.ONEOF, vf.createLiteral(resource));
                triples.add(author, RDF.TYPE, FOAF.PERSON);
                triples.add(author, FOAF.PUBLICATIONS, publication);
                setMapper(i);
                super.parseResponse(publication.toString(), requestUrl, triples, new ByteArrayInputStream(data), contentType);
            }
            // https://link.springer.com/article/10.1007/s10853-017-1751-9 --> journal
            // https://link.springer.com/chapter/10.1007/978-3-319-66562-7_49 --> book

            return Collections.emptyList();
        } catch (IOException ex) {
            throw new DataRetrievalException(ex);
        }
    }

    /**
     * Build the URL to use to call the web service in order to retrieve the
     * data for the resource passed as argument. In many cases, this will just
     * return the URI of the resource (e.g. Linked Data), but there might be
     * data providers that use different means for accessing the data for a
     * resource, e.g. SPARQL or a Cache.
     *
     *
     * @param resource
     * @param endpoint endpoint configuration for the data provider (optional)
     * @return
     */
    @Override
    public List<String> buildRequestUrl(String resource, Endpoint endpoint) {
        final Matcher matcher = Pattern.compile(PATTERN).matcher(resource);
        if (matcher.find()) {
            try {
                String query = "q=" + URLEncoder.encode(matcher.group(1), "UTF-8");
                resource = resource.replaceFirst("q=[^\\&]+", query);
                return Collections.singletonList(resource);
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException(
                        new DataRetrievalException("Cannot encode parameter 'q' in resource: " + matcher.group(1), ex));
            }
        }
        throw new RuntimeException(new DataRetrievalException("Incorrect resource request."));
    }

    /**
     * Returns an empty list. Springer describes URIs with an Id, so there is
     * not difference between resources. All types are assinged in
     * {@link #parseResponse}
     *
     * @param resource
     * @return
     */
    @Override
    protected final List<String> getTypes(URI resource) {
        return Collections.emptyList();
    }

    @Override
    protected Map<String, JsonPathValueMapper> getMappings(String string, String string1) {
        return mapper;
    }

    private void setMapper(int i) {
        String root = String.format("$.records[%s]", i);
        mapper.put(DCTERMS.NAMESPACE + "title", new JsonPathLiteralMapper(root + ".title", "string"));
        mapper.put(BIBO.NAMESPACE + "doi", new JsonPathLiteralMapper(root + ".doi", "string"));
        mapper.put(REDI.NAMESPACE + "pisbn", new JsonPathLiteralMapper(root + ".printIsbn", "string"));
        mapper.put(REDI.NAMESPACE + "eisbn", new JsonPathLiteralMapper(root + ".electronicIsbn", "string"));
        mapper.put(REDI.NAMESPACE + "isbn", new JsonPathLiteralMapper(root + ".isbn", "string"));
        mapper.put(REDI.NAMESPACE + "issn", new JsonPathLiteralMapper(root + ".issn", "string"));
        mapper.put(REDI.NAMESPACE + "eissn", new JsonPathLiteralMapper(root + ".eissn", "string"));
        mapper.put(REDI.NAMESPACE + "springerJournalId", new JsonPathLiteralMapper(root + ".journalid", "string"));
        mapper.put(DCTERMS.NAMESPACE + "publisher", new JsonPathLiteralMapper(root + ".publisher", "string"));
        mapper.put(BIBO.NAMESPACE + "created1", new JsonPathLiteralMapper(root + ".publicationDate", "string"));
        mapper.put(BIBO.NAMESPACE + "created2", new JsonPathLiteralMapper(root + ".onlineDate", "string"));
        mapper.put(BIBO.NAMESPACE + "created3", new JsonPathLiteralMapper(root + ".printDate", "string"));
        mapper.put(REDI.NAMESPACE + "coverDate", new JsonPathLiteralMapper(root + ".coverDate", "string"));
        mapper.put(BIBO.NAMESPACE + "volume", new JsonPathLiteralMapper(root + ".volume", "string"));
        mapper.put(BIBO.NAMESPACE + "issue", new JsonPathLiteralMapper(root + ".number", "string"));
        mapper.put(BIBO.NAMESPACE + "pageStart", new JsonPathLiteralMapper(root + ".startingPage", "string"));
        mapper.put(REDI.NAMESPACE + "copyrightYear", new JsonPathLiteralMapper(root + ".copyright", "string"));
//        mapper.put(DCTERMS.NAMESPACE + "genre", new JsonPathLiteralMapper(root + ".genre", "string"));
        mapper.put(BIBO.NAMESPACE + "abstract", new JsonPathLiteralMapper(root + ".abstract", "string"));
        mapper.put(BIBO.NAMESPACE + "uri", new JsonPathLiteralMapper(root + ".url[*].value", "string"));
        mapper.put(DCTERMS.NAMESPACE + "contributor", new JsonPathLiteralMapper(root + ".creators[*].creator", "string"));
        mapper.put(DCTERMS.NAMESPACE + "name", new JsonPathLiteralMapper(root + ".publicationName", "string"));
    }

}
