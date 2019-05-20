/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.ucuenca.storage.services;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.util.CharArrayMap.EntrySet;

/**
 *
 * @author joe
 */
public class temporal {

    /**
     * @param args the command line arguments
     */
    
    
    public static void main(String[] args) {
       HashMap hmap = new HashMap (); 
       String [] nombres =  new String [] {"M Espinoza","M., Espinoza","Espinoza , Mauricio","Espinoza, Mauricio","mauricio espinoza","Mauricio Espinoza","Espinoza, M.","Espinoza Mejía, Jorge Mauricio","Espinoza Mejía, M","Espinoza Mejia, M","ESPINOZA MEJIA ,   JORGE MAURICIO","mauricio espinoza mejia","Espinoza,Mauricio","Mauricio Espinoza Mejía","Mauricio Espinosa Mejía","Jorge Mauricio Espinoza Mejía","M Espinoza Mejía","M Espinoza-Mejía","M Espinoza-Mejia","Mauricio Espinoza-Mejía","J Mejía","Mauricio Espinoza-Mejıa","J Mejia","Mauricio, Espinoza-Mejla","Mauricio, Espinoza-Mejía","M., Espinoza-Mejla","M., Espinoza-Mejía","Mauricio, Espinoza","M., Mejía","Mauricio Espinoza, Espinoza-Mejla","Mauricio Espinoza, Mejía","Mauricio, Mejía","Mauricio Espinoza, Espinoza-Mejía","Mauricio Espinoza, Espinoza","Mauricio, Espinoza-Mejia","Jorge Mauricio Espinoza","Espinoza-Mejía, M.","Espinoza-Mejía, Mauricio","Espinoza, Mauricio J.","Mauricio J Espinoza","Jorge Mauricio Espinoza Mejia","M., Espinoza-Mejia","Mauricio J., Espinoza","Mauricio J., Espinoza-Mejía","Mauricio J., Espinoza-Mejia","Mauricio J., Espinoza-Mejla"};
       String cadena = "Maurício, Espinoza.";
     
       for (String no : nombres ) {
       String n = no.toUpperCase().replaceAll("[,\\.-]", " ").replaceAll("\\s+", " ");
       String t =  StringUtils.stripAccents(n).trim();
       if (hmap.containsKey(t)){
         hmap.put(t, hmap.get(t)+";"+no);
       }else {
         hmap.put(t, no);
       }
       
       }
        Iterator it = hmap.entrySet().iterator();
        while (it.hasNext()) {
        Map.Entry pair = (Map.Entry)it.next();
        System.out.println(pair.getKey()+"-"+pair.getValue());
        
        }
       
    }
    
}
