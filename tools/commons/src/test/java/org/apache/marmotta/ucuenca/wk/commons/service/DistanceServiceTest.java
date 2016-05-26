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

        List<String> lista4 = new ArrayList<>();
        lista4.add("plantas");
        lista4.add("biologia");

        List<String> lista5 = new ArrayList<>();
        lista5.add("plants");
        lista5.add("biology");
        lista5.add("pluricelular");

        List<String> lista1 = new ArrayList<>();
        lista1.add("Arginine");
        lista1.add("vasopressin");
        lista1.add("mediates");
        lista1.add("cardiovascular");
        lista1.add("responses");
        lista1.add("hypoxenia");
        lista1.add("sheen");

        List<String> lista2 = new ArrayList<>();
        lista2.add("ETAPA");
        lista2.add("PROGRAMA EN COMPUTACION");
        lista2.add("RED TELEFONICA");
        lista2.add("SISTEMA DE INFORMACION GEOGRAFICA");
        lista2.add("TESIS EN INFORMATICA");
        lista2.add("TRES CAPAS");
//Arginine vasopressin mediates cardiovascular responses to hypoxemia in fetal sheep
//Role of endogenous opioids in the cardiovascular responses to asphyxia in fetal shee        

        List<String> lista3 = new ArrayList<>();
        lista3.add("Role");
        lista3.add("endogeneous");
        lista3.add("opioids");
        lista3.add("cardiovascular");
        lista3.add("responses");
        lista3.add("fetal");
        lista3.add("shee");

        List<String> lista6 = new ArrayList<>();
        lista6.add("MOTRICIDAD");
        lista6.add("BRONFRENBRENNER");
        lista6.add("EDAD DE DESARROLLO");
        lista6.add("MUSICA");
        lista6.add("COCIENTE DE DESARROLLO");
        lista6.add("PLAN DE INTERVENCION");

        List<String> lista7 = new ArrayList<>();
        lista7.add("Parallel");
        lista7.add("tempering");
        lista7.add("compact");
        lista7.add("asynchronous");
        lista7.add("multispin");
        lista7.add("coding");

//Parallel tempering simulation of the three-dimensional Edwards-Anderson model with compact asynchronous multispin coding on GPU.
// ETAPA, PROGRAMA DE COMPUTACION, RED TELEFONICA, SISTEMA DE INFORMACION GEOGRAFICA, 
        //TESIS EN INFORMATICA, TRES CAPAS
    //    Assert.assertFalse(instance.semanticComparison(lista1, lista2));

   //     Assert.assertTrue(instance.semanticComparison(lista1, lista3));

     //   Assert.assertFalse(instance.semanticComparison(lista2, lista3));
        
    //    Assert.assertFalse(instance.semanticComparison(lista6, lista7));
        
        
        List<String> lista9 = new ArrayList<>();
        lista9.add("SOFTWARE");
        lista9.add("MACHINE LEARNING");
        
        List<String> lista10 = new ArrayList<>();
        lista10.add("MEDIO AMBIENTE");
        lista10.add("CLIMA");
        
    //    Assert.assertFalse(instance.semanticComparison(lista9, lista10));

    }

}
