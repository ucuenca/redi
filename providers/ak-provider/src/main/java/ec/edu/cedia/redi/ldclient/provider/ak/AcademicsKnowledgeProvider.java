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
package ec.edu.cedia.redi.ldclient.provider.ak;

import com.google.common.base.Preconditions;
import com.jayway.jsonpath.JsonPath;
import ec.edu.cedia.redi.ldclient.provider.ak.mapping.ScopusDateMapper;
import ec.edu.cedia.redi.ldclient.provider.ak.mapping.ScopusExtendedAbstractMapper;
import ec.edu.cedia.redi.ldclient.provider.ak.mapping.ScopusExtendedMetaLiteralListMapper;
import ec.edu.cedia.redi.ldclient.provider.ak.mapping.ScopusExtendedMetaLiteraldataMapper;
import ec.edu.cedia.redi.ldclient.provider.json.AbstractJSONDataProvider;
import ec.edu.cedia.redi.ldclient.provider.json.mappers.JsonPathLiteralMapper;
import ec.edu.cedia.redi.ldclient.provider.json.mappers.JsonPathValueMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
import org.openrdf.model.vocabulary.RDFS;

/**
 * Support Academics Knowledge information as JSON. Be aware that the provider
 * makes extra calls to retrieve publications of each author found.
 * <p>
 * See the
 * <a href="https://docs.microsoft.com/en-us/azure/cognitive-services/academic-knowledge/home">documentation
 * guide</a> for more information about the information returned by the Academic
 * Knowledge API.
 *
 * @author Xavier Sumba
 */
public class AcademicsKnowledgeProvider extends AbstractJSONDataProvider implements DataProvider {

    public static final String NAME = "Academics Knowledge Provider";
    public static final String PATTERN_AUTHOR = "https://westus\\.api\\.cognitive\\.microsoft\\.com/academic/v1\\.0/evaluate.+subscription-key=(.*).*";
    public static final String PATTERN_PUBLICATION = "https://westus\\.api\\.cognitive\\.microsoft\\.com/academic/v1\\.0/evaluate.expr=Composite\\(AA.AuId=(.*)\\)&.*";
    /**
     * Default Academics Knowledge for resources.
     */
    public static final String ACADEMICS_URL = "https://academic.microsoft.com/#/detail/";

    private final ConcurrentMap<String, JsonPathValueMapper> ontologyMapping = new ConcurrentHashMap<>();
    private final String templatePublication = "https://westus.api.cognitive.microsoft.com/academic/v1.0/evaluate?"
            + "expr=Composite(AA.AuId=%s)&attributes=Id,Ti,L,Y,D,CC,ECC,AA.AuN,AA.AuId,AA.AfN,AA.AfId,AA.S,F.FN,F.FId,"
            + "J.JN.J.JId,C.CN,C.CId,RId,W,E&subscription-key=%s";

    private String apiKey;

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
        Matcher m = Pattern.compile(PATTERN_AUTHOR).matcher(resource);
        if (m.find()) {
            apiKey = m.group(1);
            String url = resource;
            return Collections.singletonList(url);
        }
        return Collections.emptyList();
    }

    @Override
    protected List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {
        try {                            //Wait to send other request, api restrictions.
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
        if (Pattern.matches(PATTERN_PUBLICATION, requestUrl)) {
            return parsePublications(input, requestUrl, triples, contentType);
        } else if (Pattern.matches(PATTERN_AUTHOR, requestUrl)) {
            return parseAuthors(input, requestUrl, triples, contentType);
        } else {
            throw new DataRetrievalException("Cannot parse information for request " + requestUrl);
        }
    }

    private List<String> parseAuthors(InputStream input, String requestUrl, Model triples, String contentType) throws DataRetrievalException {
        try {
            byte[] data = IOUtils.toByteArray(input); // keep data for some reads
            List<String> authors = new ArrayList<>();
            ValueFactory vf = ValueFactoryImpl.getInstance();

            // Iterate authors and return new calls to their profiles.
            List ids = JsonPath
                    .parse(new ByteArrayInputStream(data), getConfiguration())
                    .read("$.entities[*].Id");
            for (int i = 0; i < ids.size(); i++) {
                String authorResource = ACADEMICS_URL + ids.get(i);
                String authorUrl = String.format(templatePublication, ids.get(i), apiKey);
                setMappings(i, Type.AUTHOR);
                authors.add(authorUrl);
                triples.add(vf.createURI(authorResource), OWL.ONEOF, vf.createURI(requestUrl));
                triples.add(vf.createURI(authorResource), DCTERMS.PROVENANCE, REDI.ACADEMICS_PROVIDER);
                triples.add(vf.createURI(authorResource), RDF.TYPE, FOAF.PERSON);
                super.parseResponse(authorResource, requestUrl, triples, new ByteArrayInputStream(data), contentType);
            }
            return authors;
        } catch (IOException ex) {
            throw new DataRetrievalException(ex);
        }
    }

    private List<String> parsePublications(InputStream input, String requestUrl, Model triples, String contentType) throws DataRetrievalException {
        try {
            byte[] data = IOUtils.toByteArray(input); // keep data for some reads

            // build auhtor resource URI
            ValueFactory vf = ValueFactoryImpl.getInstance();
            Matcher m = Pattern.compile(PATTERN_PUBLICATION).matcher(requestUrl);
            String authorId;
            if (m.find()) {
                authorId = m.group(1);
            } else {
                throw new DataRetrievalException("Cannot find authorId to build resource URI");
            }
            URI authorResource = vf.createURI(ACADEMICS_URL + authorId);

            // Iterate publications and transform to RDF
            List publicationIds = JsonPath
                    .parse(new ByteArrayInputStream(data), getConfiguration())
                    .read("$.entities[*].Id");
            for (int i = 0; i < publicationIds.size(); i++) {
                String publicationResource = ACADEMICS_URL + publicationIds.get(i);
                triples.add(authorResource, FOAF.PUBLICATIONS, vf.createURI(publicationResource));
                triples.add(vf.createURI(publicationResource), DCTERMS.PROVENANCE, REDI.ACADEMICS_PROVIDER);
                triples.add(vf.createURI(publicationResource), RDF.TYPE, BIBO.ACADEMIC_ARTICLE);
                setMappings(i, Type.PUBLICATION);
                super.parseResponse(publicationResource, requestUrl, triples, new ByteArrayInputStream(data), contentType);
                // Iterate authors and trasform to RDF
                List contributorsIds = JsonPath
                        .parse(new ByteArrayInputStream(data), getConfiguration())
                        .read(String.format("$.entities[%d].AA[*].AuId", i));
                for (int j = 0; j < contributorsIds.size(); j++) {
                    String contributorResource = ACADEMICS_URL + contributorsIds.get(j);
                    setMappings(i, j, Type.CONTRIBUTOR);
                    triples.add(vf.createURI(publicationResource), DCTERMS.CONTRIBUTOR, vf.createURI(contributorResource));
                    triples.add(vf.createURI(contributorResource), DCTERMS.PROVENANCE, REDI.ACADEMICS_PROVIDER);
                    triples.add(vf.createURI(contributorResource), RDF.TYPE, FOAF.PERSON);
                    super.parseResponse(contributorResource, requestUrl, triples, new ByteArrayInputStream(data), contentType);
                }
                // Iterate fields and transform to RDF
                List fieldsIds = JsonPath
                        .parse(new ByteArrayInputStream(data), getConfiguration())
                        .read(String.format("$.entities[%d].F[*].FId", i));
                for (int j = 0; j < fieldsIds.size(); j++) {
                    String fieldResource = ACADEMICS_URL + fieldsIds.get(j);
                    setMappings(i, j, Type.FIELD);
                    triples.add(vf.createURI(publicationResource), FOAF.TOPIC_INTEREST, vf.createURI(fieldResource));
                    super.parseResponse(fieldResource, requestUrl, triples, new ByteArrayInputStream(data), contentType);
                }
            }

            return Collections.emptyList();
        } catch (IOException ex) {
            throw new DataRetrievalException(ex);
        }
    }

    private void setMappings(int i, Type type) {
        setMappings(i, -1, type);
    }

    private void setMappings(int i, int j, Type type) {
        if (type == Type.CONTRIBUTOR || type == Type.FIELD) {
            Preconditions.checkArgument(j >= 0);
        } else {
            Preconditions.checkArgument(j == -1);
        }
        String integerDatatype = "integer";
        String root;
        ontologyMapping.clear();
        switch (type) {
            case AUTHOR:
                root = String.format("$.entities[%d]", i);
                ontologyMapping.put(REDI.ACADEMICS_ID.stringValue(), new JsonPathLiteralMapper(root + ".Id", integerDatatype));
                ontologyMapping.put(FOAF.NAME.stringValue(), new JsonPathLiteralMapper(root + ".AuN"));
                ontologyMapping.put(REDI.DISPLAY_NAME.stringValue(), new JsonPathLiteralMapper(root + ".DAuN"));
                ontologyMapping.put(REDI.CITATION_COUNT.stringValue(), new JsonPathLiteralMapper(root + ".CC", integerDatatype));
                ontologyMapping.put(REDI.AFFILIATION_NAME.stringValue(), new ScopusExtendedMetaLiteraldataMapper(root + ".E", "$.LKA.AfN"));
                ontologyMapping.put(REDI.AFFILIATION_ID.stringValue(), new ScopusExtendedMetaLiteraldataMapper(root + ".E", "$.LKA.AfId", integerDatatype));
                break;
            case CONTRIBUTOR:
                root = String.format("$.entities[%d].AA[%d]", i, j);
                ontologyMapping.put(REDI.ACADEMICS_ID.stringValue(), new JsonPathLiteralMapper(root + ".AuId", integerDatatype));
                ontologyMapping.put(FOAF.NAME.stringValue(), new JsonPathLiteralMapper(root + ".AuN"));
                ontologyMapping.put(REDI.AFFILIATION_ID.stringValue(), new JsonPathLiteralMapper(root + ".AfId", integerDatatype));
                ontologyMapping.put(REDI.AFFILIATION_NAME.stringValue(), new JsonPathLiteralMapper(root + ".AfN"));
                ontologyMapping.put(REDI.POSITION.stringValue(), new JsonPathLiteralMapper(root + ".S", integerDatatype));
                break;
            case FIELD:
                root = "$.entities[" + i + "].F[" + j + "]";
                ontologyMapping.put(RDFS.LABEL.stringValue(), new JsonPathLiteralMapper(root + ".FN"));
                ontologyMapping.put(REDI.ACADEMICS_ID.stringValue(), new JsonPathLiteralMapper(root + ".FId", integerDatatype));
                break;
            case PUBLICATION:
                root = "$.entities[" + i + "]";
                ontologyMapping.put(REDI.ACADEMICS_ID.stringValue(), new JsonPathLiteralMapper(root + ".Id", integerDatatype));
                ontologyMapping.put(DCTERMS.TITLE.stringValue(), new JsonPathLiteralMapper(root + ".Ti"));
                ontologyMapping.put(DCTERMS.LANGUAGE.stringValue(), new JsonPathLiteralMapper(root + ".L"));
                ontologyMapping.put(REDI.YEAR.stringValue(), new JsonPathLiteralMapper(root + ".Y"));
                ontologyMapping.put(DCTERMS.CREATED.stringValue(), new ScopusDateMapper(root + ".D"));
                ontologyMapping.put(REDI.CITATION_COUNT.stringValue(), new JsonPathLiteralMapper(root + ".CC", integerDatatype));
                ontologyMapping.put(REDI.ESTIMATED_CITATION_COUNT.stringValue(), new JsonPathLiteralMapper(root + ".ECC", integerDatatype));
                ontologyMapping.put(REDI.ACADEMICS_REFERENCE_ID.stringValue(), new JsonPathLiteralMapper(root + ".RId[*]", integerDatatype));
                ontologyMapping.put(FOAF.TOPIC.stringValue(), new JsonPathLiteralMapper(root + ".W[*]"));
                ontologyMapping.put(REDI.CONFERENCE_ID.stringValue(), new JsonPathLiteralMapper(root + ".C.CId", integerDatatype));
                ontologyMapping.put(REDI.CONFERENCE_NAME.stringValue(), new JsonPathLiteralMapper(root + ".C.CN"));
                ontologyMapping.put(BIBO.ABSTRACT.stringValue(), new ScopusExtendedAbstractMapper(root + ".E", "$.IA"));
                ontologyMapping.put(BIBO.DOI.stringValue(), new ScopusExtendedMetaLiteraldataMapper(root + ".E", "$.DOI"));
                ontologyMapping.put(BIBO.PAGE_START.stringValue(), new ScopusExtendedMetaLiteraldataMapper(root + ".E", "$.FP"));
                ontologyMapping.put(BIBO.PAGE_END.stringValue(), new ScopusExtendedMetaLiteraldataMapper(root + ".E", "$.LP"));
                ontologyMapping.put(BIBO.URI.stringValue(), new ScopusExtendedMetaLiteralListMapper(root + ".E", "$.S[*].U"));
                ontologyMapping.put(REDI.DISPLAY_NAME.stringValue(), new ScopusExtendedMetaLiteraldataMapper(root + ".E", "$.DN"));
                ontologyMapping.put(BIBO.VOLUME.stringValue(), new ScopusExtendedMetaLiteraldataMapper(root + ".E", "$.V"));
                ontologyMapping.put(BIBO.ISSUE.stringValue(), new ScopusExtendedMetaLiteraldataMapper(root + ".E", "$.I"));
                ontologyMapping.put(REDI.VENUE_FULL_NAME.stringValue(), new ScopusExtendedMetaLiteraldataMapper(root + ".E", "$.VFN"));
                ontologyMapping.put(REDI.VENUE_SHORT_NAME.stringValue(), new ScopusExtendedMetaLiteraldataMapper(root + ".E", "$.VSN"));
                break;
            default:
                throw new RuntimeException("Cannot map Type " + type);
        }
    }

    @Override
    protected Map<String, JsonPathValueMapper> getMappings(String resource, String requestUrl) {
        return ontologyMapping;
    }

    /**
     * Returns an empty list. Academics Knowledge describes URIs with an Id, so
     * there is not difference between resources. All types are assinged in
     * {@link #parseResponse}
     *
     * @param resource
     * @return
     */
    @Override
    protected final List<String> getTypes(URI resource) {
        return Collections.emptyList();
    }

    /**
     * Types to map resources.
     */
    private enum Type {
        AUTHOR, PUBLICATION, CONTRIBUTOR, FIELD;
    }
}
