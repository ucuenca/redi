/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.service;

import java.util.List;
import java.util.Map;
import org.openrdf.model.Value;

/**
 *
 * @author Satellite
 */
public interface GetAuthorsGraphData {
    List<Map<String, Value>> getListOfAuthors(String... organizations) ;
}
