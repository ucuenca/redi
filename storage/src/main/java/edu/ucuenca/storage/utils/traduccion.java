/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.storage.utils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joe
 */
public class traduccion {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
   TranslatorGoogle tg;
    try {
      tg = new TranslatorGoogle ();
      String traslate = tg.translateText("Hola mundo. Traduceme");
      System.out.print (traslate);
      
    } catch (IOException ex) {
      Logger.getLogger(traduccion.class.getName()).log(Level.SEVERE, null, ex);
    }
  
   
  }
  
}
