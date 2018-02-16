/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.service;

import org.apache.marmotta.ucuenca.wk.commons.impl.CommonsServicesImpl;
import org.apache.marmotta.ucuenca.wk.commons.impl.QueriesServiceImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
//import org.junit.Ignore;
import org.junit.Test;

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
     * Test of getAuthorsQuery method, of class QueriesServiceImplService.
     */
  /*  @Test
    @Ignore
    public void testGetAuthorsQuery() {
        System.out.println("getAuthorsQuery");
        QueriesService instance = new QueriesServiceImpl();
        String wkhuskagraph = "http://ucuenca.edu.ec/wkhuska";
        String expResult = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT DISTINCT ?s WHERE { GRAPH <" + wkhuskagraph + "> { ?s rdf:type foaf:Person }}";
        String result = instance.getAuthorsQuery(wkhuskagraph, "1");
        Assert.assertEquals(expResult, result);
    }
*/
    /**
     * Test of getRetrieveResourceQuery method, of class
     * QueriesServiceImplService.
     */
    @Test
    public void testGetRetrieveResourceQuery() {
        System.out.println("getRetrieveResourceQuery");
        QueriesService instance = new QueriesServiceImpl();

        String expResult = "SELECT ?x ?y ?z WHERE { ?x ?y ?z }";
        String result = instance.getRetrieveResourceQuery();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getInsertDataLiteralQuery method, of class
     * QueriesServiceImplService.
     */
    // @Test
    public void testGetInsertDataLiteralQuery() {
        System.out.println("getInsertDataLiteralQuery");
        String s = "";
        String p = "";
        String o = "";
        String wkhuskaGraph = "http://ucuenca.edu.ec/wkhuska";

        QueriesService instance = new QueriesServiceImpl();
        String expResult = "";
        String result = instance.getInsertDataLiteralQuery(wkhuskaGraph, s, p, o);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getInsertDataUriQuery method, of class QueriesServiceImplService.
     */
    @Test
    public void testGetInsertDataUriQuery() {
        System.out.println("getInsertDataUriQuery");
        String subject = "http://example1.ec/resource/Juan_Perez";
        String predicate = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        String object = "http://xmlns.com/foaf/0.1/Person";
        String wkhuskaGraph = "http://ucuenca.edu.ec/wkhuska";
        QueriesService instance = new QueriesServiceImpl();
        String expResult = "INSERT DATA {  GRAPH <http://ucuenca.edu.ec/wkhuska>  { <" + subject + "> <" + predicate + "> <" + object + "> }}";
//        String result = instance.getInsertDataUriQuery(wkhuskaGraph, subject, predicate, object);
        //      Assert.assertEquals(expResult, result);
    }

    /**
     * Test of isURI method, of class QueriesServiceImplService.
     */
    @Test
    public void testIsURI() {
        CommonsServices commonsservices = new CommonsServicesImpl();
        System.out.println("isURI");
        String object = "http://cedia.example.org";
        String objectNoUri = "Cedia";
        Assert.assertTrue(commonsservices.isURI(object));
        Assert.assertFalse(commonsservices.isURI(objectNoUri));
    }

    /**
     * Test of getAskQuery method, of class QueriesServiceImplService.
     */
    @Test
    public void testGetAskResourceQuery() {
        System.out.println("getAskQuery");
        String resource = "http://example.test";
        String graph = "http://ucuenca.edu.ec/wkhuska";
        QueriesService instance = new QueriesServiceImpl();
        String expResult = "ASK FROM <http://ucuenca.edu.ec/wkhuska> { <http://example.test> ?p ?o }";
        String result = instance.getAskResourceQuery(graph, resource);
        Assert.assertEquals(expResult, result);
    }

}
