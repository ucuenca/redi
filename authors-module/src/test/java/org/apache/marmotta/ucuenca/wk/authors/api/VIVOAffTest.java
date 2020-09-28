/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.authors.api;

import com.google.common.collect.Lists;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Person;
import org.junit.Test;

/**
 *
 * @author cedia
 */
public class VIVOAffTest {

    @Test
    public void testAff() {
        Person mock = new Person();
        mock.Affiliations = Lists.newArrayList("Universidad de Cuenca");
        Person mock2 = new Person();
        mock2.Affiliations = Lists.newArrayList("Universidad Estatal de Cuenca");
        System.out.println(mock.checkAffiliations(mock2));
    }

    @Test
    public void testAff2() {
        Person mock = new Person();
        mock.Affiliations = Lists.newArrayList("Universidad de Cuenca");
        Person mock2 = new Person();
        mock2.Affiliations = Lists.newArrayList("Universidad Catolica de Cuenca");
        System.out.println(mock.checkAffiliations(mock2));
    }
    
    @Test
    public void testAff3() {
        Person mock = new Person();
        mock.Affiliations = Lists.newArrayList("Yachay");
        Person mock2 = new Person();
        mock2.Affiliations = Lists.newArrayList("Yachay Tech");
        System.out.println(mock.checkAffiliations(mock2));
    }
    
    @Test
    public void testAff4() {
        Person mock = new Person();
        mock.Affiliations = Lists.newArrayList("Universidad Yachay Tech");
        Person mock2 = new Person();
        mock2.Affiliations = Lists.newArrayList("Yachay Tech");
        System.out.println(mock.checkAffiliations(mock2));
    }

}
