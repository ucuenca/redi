/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author cedia
 */
public class BingService {

    private static final String BING_URL = "https://www.bing.com/search?q=";

    public boolean queryBing(String query) throws IOException {

        String requestResponse = "";
        do {
            try {
                requestResponse = requestPOST(query);
                break;
            } catch (Exception ex) {
                try {
                    Logger.getLogger(BingService.class.getName()).log(Level.SEVERE, null, ex);
                    Thread.sleep(10000);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(BingService.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        } while (true);
        int num = Math.random() < 0.5 ? 1 : 2;
        try {
            Thread.sleep(1000 * (3 + num));
        } catch (InterruptedException ex) {
            Logger.getLogger(BingService.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        return !requestResponse.contains("No se encontraron resultados para");
    }

  

    public String requestPOST(String query) throws MalformedURLException, IOException {
        final URL url = new URL(BING_URL + URLEncoder.encode(query, "UTF-8"));
        final URLConnection connection = url.openConnection();
        String resp = "";
        //connection.setConnectTimeout(10000);
        //connection.setReadTimeout(60000);
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:44.0) Gecko/20100101 Firefox/44.0");
        connection.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        resp = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
        connection.getInputStream().close();

        return resp;
    }

}
