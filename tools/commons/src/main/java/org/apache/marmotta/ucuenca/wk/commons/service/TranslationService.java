/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.service;

import com.google.gson.JsonObject;
/**
 *
 * @author Jose Luis Cullcay
 */
public interface TranslationService {

    JsonObject translate(String text);
  
}
