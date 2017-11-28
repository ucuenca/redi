/*
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
package org.apache.marmotta.ucuenca.wk.pubman.utils;

import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class OntologyMapperTest {

    private final String vocabulary
            = "@prefix foaf: <http://xmlns.com/foaf/0.1/> ."
            + "@prefix uc: <http://ucuenca.edu.ec/ontology#> ."
            + "@prefix schema: <http://schema.org/> ."
            + "@prefix bibo: <http://purl.org/ontology/bibo/> ."
            + "@prefix dct: <http://purl.org/dc/terms/> ."
            + "@prefix nature: <http://ns.nature.com/terms/> ."
            + "("
            + " rdf:type,"
            + " foaf:holdsAccount,"
            + " uc:citationCount,"
            + " schema:memberOf,"
            + " uc:academicsID,"
            + " dct:title,"
            + " dct:language,"
            + " nature:coverDate,"
            + " bibo:created,"
            + " bibo:issue,"
            + " dct:isPartOf,"
            + " bibo:abstract,"
            + " bibo:doi,"
            + " bibo:pageStart,"
            + " bibo:pageEnd,"
            + " bibo:volume,"
            + " bibo:uri,"
            + " bibo:quote,"
            + " bibo:cites,"
            + " foaf:topic_interest,"
            + " dct:contributor,"
            + " foaf:publications,"
            + " dct:provenance,"
            + " owl:oneOf"
            + ")";
    private static Model academicsKnowledgeModel;
    private static InputStream academicsKnowledgeMapper;
    private static final Logger log = LoggerFactory.getLogger(OntologyMapperTest.class);

    @BeforeClass
    public static void setUpClass() {
        try {
            academicsKnowledgeModel = Rio.parse(OntologyMapperTest.class.getResourceAsStream("/providers/data/academics_knowledge.n3"), "", RDFFormat.N3);
        } catch (IOException | RDFParseException | UnsupportedRDFormatException ex) {
            log.error("cannot read file.");
        }
        academicsKnowledgeMapper = OntologyMapper.class.getResourceAsStream("/mapping/academics_knowledge.ttl");
    }

    /**
     * Test of the mapping of academics knowledge Mapping.
     */
    @Test
    public void testAcademicsKnowledgeOntologyMapping() {
        int expSize = 1238;
        Model result = OntologyMapper.map(academicsKnowledgeModel, academicsKnowledgeMapper, vocabulary);
        assertEquals(result.size(), expSize);
    }

}
