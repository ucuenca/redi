/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.authors.api;

import java.util.List;

/**
 *
 * @author joe
 */
public class EndpointDataverse extends EndpointObject {

  public EndpointDataverse(String status, String org, String access, String type, String graph, String resourceId) {
    super(status, org, access, type, graph, resourceId);
  }



  
  
  @Override
  public Boolean prepareQuery() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List querySource(String query) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Boolean closeconnection() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
