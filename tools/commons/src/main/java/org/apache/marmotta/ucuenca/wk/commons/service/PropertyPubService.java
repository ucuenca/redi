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
public interface PropertyPubService {
    
     String PUBPROPERTY="http://xmlns.com/foaf/0.1/publications";
     
     String TITLEPROPERTY = "http://purl.org/dc/terms/title";
    
     String getPubProperty();
     
     String getTittleProperty();
    
}
