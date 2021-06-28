/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import java.io.IOException;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

/**
 *
 * @author cedia
 */
public class NewMain {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws SolrServerException, IOException {
    // TODO code application logic here
    
    
    SolrServer httpSolrServer = new HttpSolrServer("http://localhost/solrIdx/oai");
    
    httpSolrServer.deleteById("retert");
    
    httpSolrServer.commit();
    
  }
  
}
