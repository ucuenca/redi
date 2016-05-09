/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.service;

/**
 *
 * @author Satellite
 */
public interface ComparisonNames {
 
     /**
     * Full Name formats FROM DBLP: Avila=Gonzales:Carlos_Andres FROM SCOPUS:
     * Avila Gonzales:Carlos Andres FROM LOCAL: Avila Gonzales:Carlos Andres
     *
     * @param args: args[0] source of full name A, args[1] full name A, args[2]
     * source of full name B, args[3] full name B
     * @return
     */
    boolean syntacticComparison(String... args);
    
}
