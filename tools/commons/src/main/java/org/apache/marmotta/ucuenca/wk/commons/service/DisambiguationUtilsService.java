/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.service;

import java.util.List;
import java.util.Map;
import org.apache.marmotta.platform.core.exception.MarmottaException;

/**
 *
 * @author cedia
 */
public interface DisambiguationUtilsService {
 
  List<String> lookForOrganizations(List <String> aff) throws MarmottaException;
  
  double isGivenName(String aff) throws MarmottaException;
  Map<String,String> separateName (String fullname) throws MarmottaException;
  
}
