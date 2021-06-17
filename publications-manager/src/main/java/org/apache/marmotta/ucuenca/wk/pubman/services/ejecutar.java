/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import static java.lang.Integer.parseInt;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
//import org.json.simple.JSONArray;
import org.json.JSONArray;

/**
 *
 * @author joe
 */
public class ejecutar {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
    ReportsImpl ri = new ReportsImpl ();
    
    //String[] a = ri.getJSONAuthorsCluster2( "http://dbpedia.org/resource/Sociology",  "clusterName" , "https://rediclon.cedia.edu.ec/", null  ,  null );
    //System.out.println(a[0]);
   // System.out.println(a[2]);
    
    System.out.println("NUEVOS");
    String[] b = ri.getTrends( "http://dbpedia.org/resource/Physics" , "https://rediclon.cedia.edu.ec/");
    System.out.println(b[0]);
    
 
    List <String> param  = Arrays.asList("http://dbpedia.org/resource/Physics" , "Fisica");
    String realPath = "/home/joe/REDI/redi/webapp/src/main/webapp";
 
    //ri.createReport("https://rediclon.cedia.edu.ec/", realPath, "ReportStatisticsPub", "pdf", param);
    ri.createReport("https://rediclon.cedia.edu.ec/", realPath, "ReportTrendsTotal", "pdf", param);
   // ri.createReport("https://rediclon.cedia.edu.ec/", realPath, "ReportAuthorCluster2", "pdf", param);
  }
  
  public void query () throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
     httpService http = new httpService ();
     try {
     
      //https://rediclon.cedia.edu.ec/mongo/pubByArea?cluster=
      Map param = new HashMap () ;
      param.put("cluster", "");
      HttpResponse<JsonNode> asJson;
      asJson = http.callhttp( "https://rediclon.cedia.edu.ec/mongo/pubByArea" , param);
      if (asJson.getStatus() == HttpURLConnection.HTTP_OK) {
          org.json.JSONArray nA = new org.json.JSONArray();
          //System.out.println (asJson.getStatus());
          System.out.println (asJson.getBody());
          org.json.JSONArray jsonArray = asJson.getBody().getArray();
         // System.out.println ("Enumer");
         for (int w = 0; w < jsonArray.length(); w++) {
         JSONObject jsonObject = jsonArray.getJSONObject(w);
         JSONObject no = new JSONObject();
         //System.out.println (jsonObject.toString());
        // System.out.println (jsonObject.get("area").toString());
         no.put( "area" , jsonObject.get("area").toString() ) ;
         String area = jsonObject.get("area").toString();
         org.json.JSONArray jA  = jsonObject.getJSONArray("data");
        
            for (int j = 0; j <  jA.length(); j++  ) {
              // System.out.println (jA.get(j));
               String total =  jA.getJSONObject(j).get("total").toString();
               String año = jA.getJSONObject(j).get("y").toString();
           
               if (parseInt(año)  >= 2010) {
                  no.put( año , total );
               } 
               
               
            }
            
            nA.put(no);
        }
         System.out.println (nA.toString());
        /*System.out.println (asJson.getBody());
        org.json.JSONArray jsonArray = object.getJSONArray("data");
        JSONArray authors = new JSONArray();
        for (int w = 0; w < jsonArray.length(); w++) {
         JSONObject jsonObject = jsonArray.getJSONObject(w); 
        }*/
      
      }
    
  } catch (UnirestException ex) {
      Logger.getLogger(ejecutar.class.getName()).log(Level.SEVERE, null, ex);
    }
  
  }
}
