/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author cedia
 */
public class BingService {

    private String URL_ = "http://www.mblazquez.es/google2down/index.php";
    private String URL2 = "https://www.bing.com/search?q=";

    public boolean Bing(String Query) throws IOException {

       

        String POST = "";
        do {
            try {
                POST = POST2(Query);
                break;
            } catch (Exception ex) {
                try {
                    ex.printStackTrace();
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
        boolean name = !POST.contains("No se encontraron resultados para");
        
        
        
        return name;
    }

    public boolean Google(String Query) throws IOException {
        String POST = "";
        do {
            try {
                POST = POST(Query);
                break;
            } catch (Exception ex) {
                try {
                    ex.printStackTrace();
                    Thread.sleep(10000);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(BingService.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        } while (true);

        return POST.contains("item");
    }

    public String POST2(String Query) throws MalformedURLException, IOException {
        final URL url = new URL(URL2 + URLEncoder.encode(Query, "UTF-8"));
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

    public synchronized String POST(String Query) throws MalformedURLException, IOException {

        String urlParameters = "g=google&p=1&search=Buscar+con+Google&q=" + URLEncoder.encode(Query, "UTF-8");
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;
        String request = URL_;
        URL url = new URL(request);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        conn.setUseCaches(false);
        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(postData);
            wr.flush();
            wr.close();
        }
        String response = "";
        int responseCode = conn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = br.readLine()) != null) {
                response += line;
            }
        } else {
            response = "";

        }
        return response;
    }

}
