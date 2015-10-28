/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.impl;

import org.apache.marmotta.ucuenca.wk.commons.service.PropertyPubService;

/**
 *
 * @author Satellite
 */
public class PropertyPubServiceImpl implements PropertyPubService{

    @Override
    public String getPubProperty() {
        return PUBPROPERTY;
    }

    @Override
    public String getTittleProperty() {
         return TITLEPROPERTY;
    
    }
    
    
    
}
