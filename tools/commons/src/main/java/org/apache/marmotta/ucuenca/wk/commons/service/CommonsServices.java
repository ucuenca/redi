/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.service;


/**
 *
 * @author FernandoBac
 */
public interface CommonsServices {

    String removeAccents(String input);

    /**
     * Return true or false if object is a URI
     */
    Boolean isURI(String object);

    String getMD5(String input);
    
    String readPropertyFromFile(String file,String property);

}
