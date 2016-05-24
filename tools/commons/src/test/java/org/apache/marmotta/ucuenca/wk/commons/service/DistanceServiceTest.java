/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.service;

import java.util.ArrayList;
import java.util.List;
import org.apache.marmotta.ucuenca.wk.commons.impl.DistanceServiceImpl;
import org.apache.marmotta.ucuenca.wk.commons.impl.QueriesServiceImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author FernandoBac
 */
public class DistanceServiceTest {

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

    @Test
    public void testSemanticDistance() {
        System.out.println("getAuthorsQuery");
        DistanceService instance = new DistanceServiceImpl();
        List<String> lista1 = new ArrayList<>();
        lista1.add("semantic");
        lista1.add("ontology");
        lista1.add("internet");

        List<String> lista2 = new ArrayList<>();
        lista2.add("linked data");
        lista2.add("web");
        lista2.add("ontologia");

        List<String> lista3 = new ArrayList<>();
        lista3.add("filosofia");
        lista3.add("ontologia");
        lista3.add("derechos");

        List<String> lista4 = new ArrayList<>();
        lista4.add("plantas");
        lista4.add("biologia");

        List<String> lista5 = new ArrayList<>();
        lista5.add("plants");
        lista5.add("biology");
        lista5.add("pluricelular");

        Assert.assertTrue(instance.semanticComparison(lista1, lista2));

        Assert.assertFalse(instance.semanticComparison(lista1, lista3));
        
        Assert.assertTrue(instance.semanticComparison(lista4, lista5));

    }

}
