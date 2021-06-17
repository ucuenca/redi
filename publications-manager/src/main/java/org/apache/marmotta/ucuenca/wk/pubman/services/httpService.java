/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author joe
 */
public class httpService {

  public httpService() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
      SSLContext sslcontext = SSLContexts.custom()
              .loadTrustMaterial(null, new TrustSelfSignedStrategy())
              .build();

      SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
      CloseableHttpClient httpclient = HttpClients.custom()
              .setSSLSocketFactory(sslsf)
              .build();
      Unirest.setHttpClient(httpclient);
  }
  
  public HttpResponse<JsonNode> callhttp ( String host , Map <String, Object> params) throws UnirestException {
    System.out.print (host);
    return  Unirest.get(host).queryString(params).asJson();
//                .queryString("uri", clusterId)
//                .asJson()
  }
  
}
