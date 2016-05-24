/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import java.util.Random;
import org.apache.marmotta.ucuenca.wk.commons.service.TranslationService;


/**
 *
 * @author Jose Luis Cullcay
 */
public class TranslationServiceImpl implements TranslationService {

    /**
     * Function to translate a text in any language to English
     *
     * @param text The string to be translated
     * @return The string translated. If there was an error, it returns a blank
     * string "".
     */
    @Override
    public JsonObject translate(String text) {
        JsonParser parser = new JsonParser();
        int count = 0;
        int maxTries = 360;
        Random rand = new Random();
        boolean val = true;
        int client;
        while (val) {
            String id;
            String secret;
            try {
                //Set a valid clientId and clientSecret (choose randomly one of two options).
                client = rand.nextInt(2);
                if (client == 0) {
                    id = "wkhuska";
                    secret = "qEDpYkIKm6SiCP3l0YgB1k4wvr9Q7gTqhuY/fXFmuNY=";
                    Translate.setClientId(id);
                    Translate.setClientSecret(secret);
                } else {
                    id = "keyword_translator";
                    secret = "uBt2JPP+tVNXx4sYvWtbZ/Nj6whXBhaAwAiDy911qnE=";
                    Translate.setClientId(id);
                    Translate.setClientSecret(secret);
                }
                //Ask for a new token every 5 requests (in average)
                //if (rand.nextInt(4) == 0) {
                //    Translate.getToken(id, secret);
                //}

                // Translate an string to English (we can define a different language)
                String translation = Translate.execute(text, Language.SPANISH, Language.ENGLISH);
                /*try {
                 //We will set random between 0 and 1000 milliseconds
                 Thread.sleep(rand.nextInt(1000));//1000 milliseconds is one second.
                 } catch (InterruptedException ex) {
                 Thread.currentThread().interrupt();
                 }*/
                //return the translation

                return parser.parse("{\"result\": \"" + translation + "\"}").getAsJsonObject();
                // break out of loop, or return, on success
            } catch (Exception e) {
                // handle exception
                count++;
                if (count == maxTries) {
                    return  parser.parse("{\"result\": \" No Result \"}").getAsJsonObject(); // Return a blank string. There was a persistant error.
                } else {
                    try {
                        //We will set random between 0 and 5100 milliseconds
                        Thread.sleep(rand.nextInt(5100));//1000 milliseconds is one second.
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        return parser.parse("{\"result\": \" No Result \"}").getAsJsonObject();
    }

}
