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
package org.apache.marmotta.ucuenca.wk.pubman.services;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.vocabulary.FOAF;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.AuthorAttributes;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.semarglproject.vocab.RDF;
import org.semarglproject.vocab.RDFS;
import org.slf4j.Logger;

/**
 * Extract attributes (dct:subject, foaf:img, dct:provenance) from other authors
 * (sameAs).
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class AuthorAttributesImpl implements AuthorAttributes, Runnable {

    @Inject
    private Logger log;
    @Inject
    private QueriesService queriesService;
    @Inject
    private ConstantService constant;
    @Inject
    private SparqlService sparqlService;

    private final static List<String> PROPERTIES = Arrays.asList("dct:provenance", "dct:subject", "foaf:img");

    @Override
    public void run() {
        extractAttributes();
    }

    @Override
    public String extractAttributes() {
        try {
            int size = Integer.parseInt(
                    sparqlService.query(QueryLanguage.SPARQL, queriesService.getAuthorsCentralGraphSize())
                    .get(0).get("tot").stringValue()
            );
            for (int i = 0; i < size; i += 1000) {
                List<Map<String, Value>> authors = sparqlService.query(QueryLanguage.SPARQL, queriesService.getAuthorsCentralGraph(1000, i));
                for (Map<String, Value> author : authors) {
                    Set<String> sameAsURIs = new HashSet<>();
                    String authorResource = author.get("author").stringValue();
                    List<Map<String, Value>> sameAsResult = sparqlService.query(QueryLanguage.SPARQL, queriesService.getSameAuthorsLvl2(authorResource));
                    for (Map<String, Value> sameAs : sameAsResult) {
                        sameAsURIs.add(sameAs.get("other").stringValue());
                        sameAsURIs.add(sameAs.get("general").stringValue());
                    }
                    for (String sameAsURI : sameAsURIs) {
                        for (String property : PROPERTIES) {
                            String insertquery;
                            List<Map<String, Value>> attributes = sparqlService.query(QueryLanguage.SPARQL, queriesService.getOptionalProperties(sameAsURI, property));
                            for (Map<String, Value> attribute : attributes) {
                                if (attribute.isEmpty()) {
                                    continue;
                                }
                                String attr = attribute.get("attr").stringValue();
                                if (null != property) {
                                    switch (property) {
                                        case "dct:subject":
                                            String uri = constant.getTopicResource() + StringUtils.stripAccents(StringEscapeUtils.unescapeJava(attr))
                                                    .toLowerCase()
                                                    .replace(" ", "-");
                                            insertquery = queriesService.buildInsertQuery(constant.getCentralGraph(), uri, RDFS.LABEL, attr);
                                            sparqlService.update(QueryLanguage.SPARQL, insertquery);
                                            insertquery = queriesService.buildInsertQuery(constant.getCentralGraph(), authorResource, FOAF.topic_interest.toString(), uri);
                                            sparqlService.update(QueryLanguage.SPARQL, insertquery);
                                            break;
                                        case "foaf:img":
                                            insertquery = queriesService.buildInsertQuery(constant.getCentralGraph(), authorResource, FOAF.img.toString(), attr);
                                            sparqlService.update(QueryLanguage.SPARQL, insertquery);
                                            insertquery = queriesService.buildInsertQuery(constant.getCentralGraph(), attr, RDF.TYPE, FOAF.Image.toString());
                                            sparqlService.update(QueryLanguage.SPARQL, insertquery);
                                            break;
                                        case "dct:provenance":
                                            insertquery = queriesService.buildInsertQuery(constant.getCentralGraph(), authorResource, DCTERMS.PROVENANCE.toString(), attr);
                                            sparqlService.update(QueryLanguage.SPARQL, insertquery);
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                        }

                    }
                }
            }
        } catch (MarmottaException | MalformedQueryException | UpdateExecutionException | InvalidArgumentException ex) {
            log.error("Error: {}", ex);
        }
        return "Successful extracted attributes for authors.";
    }

}
