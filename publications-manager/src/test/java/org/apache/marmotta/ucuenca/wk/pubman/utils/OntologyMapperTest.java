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
import org.openrdf.rio.RDFHandlerException;
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
  private static Model scopusModelORCID;
  private static Model dblpModel;
  private static Model scholarModel;
  private static Model scieloModel;
  private static Model springerModel;
  private static Model doajModel;
  private static Model orcidModel;
  private static InputStream academicsKnowledgeMapper;
  private static InputStream scopusMapper;
  private static InputStream scopusMapperORCID;
  private static InputStream dblpMapper;
  private static InputStream scholarMapper;
  private static InputStream scieloMapper;
  private static InputStream springerMapper;
  private static InputStream doajMapper;
  private static InputStream orcidMapper;
  private static InputStream emptyMapper;
  private static final Logger log = LoggerFactory.getLogger(OntologyMapperTest.class);

  @BeforeClass
  public static void setUpClass() {
    try {
      academicsKnowledgeModel = Rio.parse(OntologyMapperTest.class.getResourceAsStream("/providers/data/academics_knowledge.n3"), "", RDFFormat.N3);
      scopusModel = Rio.parse(OntologyMapperTest.class.getResourceAsStream("/providers/data/scopus.n3"), "", RDFFormat.N3);
      scopusModelORCID = Rio.parse(OntologyMapperTest.class.getResourceAsStream("/providers/data/scopus_orcid.ttl"), "", RDFFormat.TURTLE);
      dblpModel = Rio.parse(OntologyMapperTest.class.getResourceAsStream("/providers/data/dblp.rdf"), "", RDFFormat.RDFXML);
      scholarModel = Rio.parse(OntologyMapperTest.class.getResourceAsStream("/providers/data/scholar.rdf"), "", RDFFormat.RDFXML);
      scieloModel = Rio.parse(OntologyMapperTest.class.getResourceAsStream("/providers/data/scielo.rdf"), "", RDFFormat.RDFXML);
      doajModel = Rio.parse(OntologyMapperTest.class.getResourceAsStream("/providers/data/doaj.rdf"), "", RDFFormat.RDFXML);
      orcidModel = Rio.parse(OntologyMapperTest.class.getResourceAsStream("/providers/data/orcid.rdf"), "", RDFFormat.RDFXML);
      springerModel = Rio.parse(OntologyMapperTest.class.getResourceAsStream("/providers/data/springer.ttl"), "", RDFFormat.TURTLE);
    } catch (IOException | RDFParseException | UnsupportedRDFormatException ex) {
      log.error("cannot read file.", ex);
    }
    academicsKnowledgeMapper = OntologyMapper.class.getResourceAsStream("/mapping/academics_knowledge.ttl");
    scopusMapper = OntologyMapper.class.getResourceAsStream("/mapping/scopus.ttl");
    scopusMapperORCID = OntologyMapper.class.getResourceAsStream("/mapping/scopus.ttl");
    dblpMapper = OntologyMapper.class.getResourceAsStream("/mapping/dblp.ttl");
    scholarMapper = OntologyMapper.class.getResourceAsStream("/mapping/google_scholar.ttl");
    springerMapper = OntologyMapper.class.getResourceAsStream("/mapping/springer.ttl");
    scieloMapper = OntologyMapper.class.getResourceAsStream("/mapping/scielo.ttl");
    doajMapper = OntologyMapper.class.getResourceAsStream("/mapping/doaj.ttl");
    orcidMapper = OntologyMapper.class.getResourceAsStream("/mapping/orcid.ttl");
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
    assertEquals(resultWithMapperFile.size(), 2709);
    assertEquals(resultEmptyMapperFile.size(), 0);
  }

  /**
   * Test ontology mapping of Scopus of ORCID property.
   */
  @Test
  public void testScopusOntologyMappingORCID() {
    assertEquals(scopusModelORCID.size(), 4);
    Model resultWithMapperFile = OntologyMapper.map(scopusModelORCID, scopusMapperORCID, vocabulary);
    Model resultEmptyMapperFile = OntologyMapper.map(scopusModelORCID, emptyMapper, vocabulary);
    assertEquals(resultWithMapperFile.size(), 3);
    assertEquals(resultEmptyMapperFile.size(), 0);
  }

  /**
   * Test ontology mapping of DBLP vocabulary.
   */
  @Test
  public void testDBLPOntologyMapping() {
    assertEquals(dblpModel.size(), 788);
    Model resultWithMapperFile = OntologyMapper.map(dblpModel, dblpMapper, vocabulary);
    Model resultEmptyMapperFile = OntologyMapper.map(dblpModel, emptyMapper, vocabulary);
    assertEquals(resultWithMapperFile.size(), 694);
    assertEquals(resultEmptyMapperFile.size(), 0);
  }

  /**
   * Test ontology mapping of Google Scholar vocabulary.
   */
  @Test
  public void testScholarOntologyMapping() {
    assertEquals(scholarModel.size(), 920);
    Model resultWithMapperFile = OntologyMapper.map(scholarModel, scholarMapper, vocabulary);
    Model resultEmptyMapperFile = OntologyMapper.map(scholarModel, emptyMapper, vocabulary);
    assertEquals(resultWithMapperFile.size(), 1850);
    assertEquals(resultEmptyMapperFile.size(), 0);
  }

  /**
   * Test ontology mapping of Springer vocabulary.
   */
  @Test
  public void testSpringerOntologyMapping() {
    assertEquals(springerModel.size(), 320);
    Model resultWithMapperFile = OntologyMapper.map(springerModel, springerMapper, vocabulary);
    Model resultEmptyMapperFile = OntologyMapper.map(springerModel, emptyMapper, vocabulary);
    assertEquals(resultWithMapperFile.size(), 543);
    assertEquals(resultEmptyMapperFile.size(), 0);
  }

  /**
   * Test ontology mapping of Scielo vocabulary.
   */
  @Test
  public void testScieloOntologyMapping() {
    assertEquals(scieloModel.size(), 108);
    Model resultWithMapperFile = OntologyMapper.map(scieloModel, scieloMapper, vocabulary);
    Model resultEmptyMapperFile = OntologyMapper.map(scieloModel, emptyMapper, vocabulary);
    assertEquals(resultWithMapperFile.size(), 219);
    assertEquals(resultEmptyMapperFile.size(), 0);
  }

  /**
   * Test ontology mapping of DOAJ vocabulary.
   */
  @Test
  public void testDOAJOntologyMapping() throws RDFHandlerException {
    assertEquals(doajModel.size(), 850);
    Model resultWithMapperFile = OntologyMapper.map(doajModel, doajMapper, vocabulary);
    Model resultEmptyMapperFile = OntologyMapper.map(doajModel, emptyMapper, vocabulary);
    assertEquals(resultWithMapperFile.size(), 1753);
    assertEquals(resultEmptyMapperFile.size(), 0);
  }

  /**
   * Test ontology mapping of DOAJ vocabulary.
   */
  @Test
  public void testORCIDOntologyMapping() throws RDFHandlerException {
    assertEquals(orcidModel.size(), 1049);
    Model resultWithMapperFile = OntologyMapper.map(orcidModel, orcidMapper, vocabulary);
    Model resultEmptyMapperFile = OntologyMapper.map(orcidModel, emptyMapper, vocabulary);
    assertEquals(resultWithMapperFile.size(), 2194);
    assertEquals(resultEmptyMapperFile.size(), 0);
  }

}
