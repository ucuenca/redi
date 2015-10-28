/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 *
 * @author Satellite
 */
public interface CommonService {
   String GetDataFromProvidersService(); 
   
   String Data2GlobalGraph();
   String CountPublications();
   
   JsonArray searchAuthor(String uri);
}
