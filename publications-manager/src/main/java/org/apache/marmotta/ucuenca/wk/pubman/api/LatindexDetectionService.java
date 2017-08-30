/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.api;

/**
 *
 * @author José Ortiz
 */
public interface LatindexDetectionService {
    
    //Starts a background process that verifies whether the journals/publications registered in the central graph are or are not latindex.
    public String startProcess();
}
