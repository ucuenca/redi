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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
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

    private static String vocabulary;
    private static Model academicsKnowledgeModel;
    private static Model scopusModel;
    private static Model dblpModel;
    private static Model scholarModel;
    private static Model springerModel;
    private static InputStream academicsKnowledgeMapper;
    private static InputStream scopusMapper;
    private static InputStream dblpMapper;
    private static InputStream scholarMapper;
    private static InputStream springerMapper;
    private static InputStream emptyMapper;
    private static final Logger log = LoggerFactory.getLogger(OntologyMapperTest.class);

    @BeforeClass
    public static void setUpClass() {
        try {
            academicsKnowledgeModel = Rio.parse(OntologyMapperTest.class.getResourceAsStream("/providers/data/academics_knowledge.n3"), "", RDFFormat.N3);
            scopusModel = Rio.parse(OntologyMapperTest.class.getResourceAsStream("/providers/data/scopus.n3"), "", RDFFormat.N3);
            dblpModel = Rio.parse(OntologyMapperTest.class.getResourceAsStream("/providers/data/dblp.ttl"), "", RDFFormat.TURTLE);
            scholarModel = Rio.parse(OntologyMapperTest.class.getResourceAsStream("/providers/data/scholar.rdf"), "", RDFFormat.RDFXML);
            springerModel = Rio.parse(OntologyMapperTest.class.getResourceAsStream("/providers/data/springer.ttl"), "", RDFFormat.TURTLE);
        } catch (IOException | RDFParseException | UnsupportedRDFormatException ex) {
            log.error("cannot read file.", ex);
        }
        academicsKnowledgeMapper = OntologyMapper.class.getResourceAsStream("/mapping/academics_knowledge.ttl");
        scopusMapper = OntologyMapper.class.getResourceAsStream("/mapping/scopus.ttl");
        dblpMapper = OntologyMapper.class.getResourceAsStream("/mapping/dblp.ttl");
        scholarMapper = OntologyMapper.class.getResourceAsStream("/mapping/google_scholar.ttl");
        springerMapper = OntologyMapper.class.getResourceAsStream("/mapping/springer.ttl");
        emptyMapper = new ByteArrayInputStream(new byte[]{});

        InputStream resourceAsStream = OntologyMapper.class.getResourceAsStream("/mapping/redi.r2r");
        try {
            vocabulary = IOUtils.toString(resourceAsStream);
        } catch (IOException ex) {
            log.error("cannot parse vocabulary", ex);
        }

    }

    /**
     * Test ontology mapping of academics knowledge vocabulary.
     */
    @Test
    public void testAcademicsKnowledgeOntologyMapping() {
        assertEquals(academicsKnowledgeModel.size(), 1305);
        Model resultWithMapperFile = OntologyMapper.map(academicsKnowledgeModel, academicsKnowledgeMapper, vocabulary);
        Model resultEmptyMapperFile = OntologyMapper.map(academicsKnowledgeModel, emptyMapper, vocabulary);
        assertEquals(resultWithMapperFile.size(), 1662);
        assertEquals(resultEmptyMapperFile.size(), 0);
    }

    /**
     * Test ontology mapping of Scopus vocabulary.
     */
    @Test
    public void testScopusOntologyMapping() {
        assertEquals(scopusModel.size(), 3911);
        Model resultWithMapperFile = OntologyMapper.map(scopusModel, scopusMapper, vocabulary);
        Model resultEmptyMapperFile = OntologyMapper.map(scopusModel, emptyMapper, vocabulary);
        assertEquals(resultWithMapperFile.size(), 2677);
        assertEquals(resultEmptyMapperFile.size(), 0);
    }

    /**
     * Test ontology mapping of DBLP vocabulary.
     */
    @Test
    public void testDBLPOntologyMapping() {
        assertEquals(dblpModel.size(), 229307);
        Model resultWithMapperFile = OntologyMapper.map(dblpModel, dblpMapper, vocabulary);
        Model resultEmptyMapperFile = OntologyMapper.map(dblpModel, emptyMapper, vocabulary);
        assertEquals(resultWithMapperFile.size(), 189011);
        assertEquals(resultEmptyMapperFile.size(), 0);
    }

    /**
     * Test ontology mapping of Google Scholar vocabulary.
     */
    @Test
    public void testScholarOntologyMapping() {
        assertEquals(scholarModel.size(), 919);
        Model resultWithMapperFile = OntologyMapper.map(scholarModel, scholarMapper, vocabulary);
        Model resultEmptyMapperFile = OntologyMapper.map(scholarModel, emptyMapper, vocabulary);
        assertEquals(resultWithMapperFile.size(), 1432);
        assertEquals(resultEmptyMapperFile.size(), 0);
    }

    /**
     * Test ontology mapping of Springer vocabulary.
     */
    @Test
    public void testSpringOntologyMapping() {
        assertEquals(springerModel.size(), 320);
        Model resultWithMapperFile = OntologyMapper.map(springerModel, springerMapper, vocabulary);
        Model resultEmptyMapperFile = OntologyMapper.map(springerModel, emptyMapper, vocabulary);
        assertEquals(resultWithMapperFile.size(), 493);
        assertEquals(resultEmptyMapperFile.size(), 0);
    }
}
