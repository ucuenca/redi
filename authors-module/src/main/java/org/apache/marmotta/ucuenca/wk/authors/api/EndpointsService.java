/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.marmotta.ucuenca.wk.authors.api;

/**
 *
 * @author joe
 */
public interface EndpointsService {

     String listEndpoints();
     
     String registerSPARQL (String type , String org, String url , String graph );
     String registerORCID (String type , String org );
    
     String registerOAI ( String type , String  org , String url , Boolean severemode);
   
     String registerFile (String type, String  org , String file );
     
     String deleteEndpoint (String uri );
     
     String updateStatus (String uri , String oldstatus );
     
     String updateExtractionDate (String uri , String date);
    
}
