/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.storage.utils;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.translate.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.ucuenca.wk.commons.service.TranslationService;
import org.apache.marmotta.ucuenca.wk.pubman.api.CommonService;

/**
 *
 * @author joe
 */



@ApplicationScoped
public class TranslatorGoogle {
  Translate translate;
  int numberChar ;
  static int LIMIT = 100000;
  

  
  public TranslatorGoogle () throws IOException {
      InputStream fin =new FileInputStream(System.getProperty("user.home")+"/clavegoogle/redi-dev-312217-6e3e93fe6a79.json");
      
      translate  = TranslateOptions.newBuilder().setCredentials(ServiceAccountCredentials.fromStream(fin)).build().getService();
      numberChar = 0;
  }
  
  
  
  public String translateText(String text) {
    
  //String resp = tservice.detectLanguage("Palabra a traducir");
  //System.out.print (resp);
  int chars = text.length();
  numberChar = numberChar+ chars;
  if (numberChar < LIMIT ){
  Translation translation = translate.translate(text);
  return translation.getTranslatedText();
  }
  
  return null;
  }
  
}
