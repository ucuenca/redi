/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.marmotta.ucuenca.wk.commons.service;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author FernandoBac
 */
public interface KeywordsService {
     
    List<String>  getKeywords(String abstracttext, String titletext)throws IOException, ClassNotFoundException ;

    List<String>  getKeywords(String abstracttext)throws IOException, ClassNotFoundException ;

}
