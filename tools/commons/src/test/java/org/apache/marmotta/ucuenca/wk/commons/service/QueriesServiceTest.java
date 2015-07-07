/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.service;

import javax.inject.Inject;
import org.apache.marmotta.ucuenca.wk.commons.impl.Queries;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.junit.Assert;

/**
 *
 * @author Satellite
 */
public class QueriesServiceTest {

    public QueriesServiceTest() {
    }

    private QueriesService queriesService;

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getAuthorsQuery method, of class QueriesService.
     */
    @Test
    public void testGetAuthorsQuery() {
        System.out.println("getAuthorsQuery");
        QueriesService instance = new Queries();
        String expResult = "SELECT DISTINCT ?o WHERE {  ?s <http://id.loc.gov/vocabulary/relators/aut> ?o } ORDER BY ?o";
        String result = instance.getAuthorsQuery();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getRetrieveResourceQuery method, of class QueriesService.
     */
    @Test
    public void testGetRetrieveResourceQuery() {
        System.out.println("getRetrieveResourceQuery");
        QueriesService instance = new Queries();

        String expResult = "SELECT ?x ?y ?z WHERE { ?x ?y ?z }";
        String result = instance.getRetrieveResourceQuery();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getInsertDataLiteralQuery method, of class QueriesService.
     */
    // @Test
    public void testGetInsertDataLiteralQuery() {
        System.out.println("getInsertDataLiteralQuery");
        String s = "";
        String p = "";
        String o = "";
        QueriesService instance = new Queries();
        String expResult = "";
        String result = instance.getInsertDataLiteralQuery(s, p, o);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getInsertDataUriQuery method, of class QueriesService.
     */
    @Test
    public void testGetInsertDataUriQuery() {
        System.out.println("getInsertDataUriQuery");
        String subject = "http://example1.ec/resource/Juan_Perez";
        String predicate = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        String object = "http://xmlns.com/foaf/0.1/Person";
        QueriesService instance = new Queries();
        String expResult = "INSERT DATA { <" + subject + "> <" + predicate + "> <" + object + "> }";
        String result = instance.getInsertDataUriQuery(subject, predicate, object);
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of isURI method, of class QueriesService.
     */
    @Test
    public void testIsURI() {
        System.out.println("isURI");
        String object = "http://cedia.example.org";
        String objectNoUri = "Cedia";
        QueriesService instance = new Queries();
        Assert.assertTrue(instance.isURI(object));
        Assert.assertFalse(instance.isURI(objectNoUri));
    }

    /**
     * Test of getAskQuery method, of class QueriesService.
     */
    @Test
    public void testGetAskQuery() {
        System.out.println("getAskQuery");
        String resource = "http://example.test";
        QueriesService instance = new Queries();
        String expResult = "ASK { <http://example.test> ?p ?o }";
        String result = instance.getAskQuery(resource);
        Assert.assertEquals(expResult, result);
    }

}
