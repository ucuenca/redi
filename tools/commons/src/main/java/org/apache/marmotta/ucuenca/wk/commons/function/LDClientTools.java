/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.function;

import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;


/**
 *
 * @author José Ortiz
 */
public final class LDClientTools {
    
    private LDClientTools() {
    }

    public static ClientResponse retryLDClient(LDClient ldClient, String resource, int triesNum, int waitSec) throws DataRetrievalException {
        ClientResponse respo = null;
        int num = 0;
        boolean keepTrying = true;
        DataRetrievalException err = null;
        do {
            try {
                respo = ldClient.retrieveResource(resource);
                keepTrying = false;
            } catch (DataRetrievalException s) {
                err = s;
            }
            if (keepTrying) {
                num++;
                if (num < triesNum) {
                    try {
                        Thread.sleep(waitSec * 1000);
                    } catch (InterruptedException ex) {
                    }
                } else {
                    throw err;
                }
            }
        } while (keepTrying);

        return respo;
    }

}
