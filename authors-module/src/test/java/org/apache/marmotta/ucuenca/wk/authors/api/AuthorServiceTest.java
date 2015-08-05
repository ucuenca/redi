/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.authors.api;

import org.apache.marmotta.platform.core.test.base.EmbeddedMarmotta;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.DaoException;
import org.apache.marmotta.ucuenca.wk.authors.exceptions.UpdateException;
import org.apache.marmotta.ucuenca.wk.authors.services.AuthorServiceImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.query.QueryEvaluationException;

/**
 *
 * @author Satellite
 */
public class AuthorServiceTest {
      private static EmbeddedMarmotta marmotta;
    private static AuthorService myService;
  
    public AuthorServiceTest() {
    }
    
      @BeforeClass
    public static void setUp() {
        marmotta = new EmbeddedMarmotta();
        myService = marmotta.getService(AuthorService.class);
    }

    @AfterClass
    public static void tearDownClass() {
        marmotta.shutdown();
    }
  
    
    @After
    public void tearDown() {
    }
     /**
     * Test of runAuthorsUpdateSingleEP method, of class AuthorService.
     */
    @Test
    public void testRunAuthorsUpdateSingleEP() throws Exception {
    /*System.out.println("runAuthorsUpdateSingleEP: ");
        String sparqlEndpoint = "http://190.15.141.102:8890/sparql";
        String graphUri = "http://190.15.141.102:8080/dspace/";

        String expResult = "ENDPOINT: " + sparqlEndpoint + ":  " + "Se actualizo correctamente";
        String result = myService.runAuthorsUpdateSingleEP(sparqlEndpoint, graphUri);
        Assert.assertEquals(expResult, result);  
    */
    }

    
    
}
