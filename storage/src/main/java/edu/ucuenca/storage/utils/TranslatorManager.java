/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.storage.utils;

import com.jayway.jsonpath.JsonPath;
import edu.emory.mathcs.backport.java.util.Arrays;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.minidev.json.JSONArray;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;

/**
 *
 * @author cedia
 */
@ApplicationScoped
public class TranslatorManager {

  @Inject
  private org.slf4j.Logger log;
  @Inject
  private ConfigurationService conf;

  private List<String> keys;
  private HttpClient httpClient = HttpClients.createDefault();
  int auxk = 0;

  public String traductor(String palabras) throws IOException {
    String keyss = conf.getStringConfiguration("yandex.keys");
    String[] split = keyss.split(";");
    keys = Arrays.asList(split);
    return traductor(palabras, 0);
  }

  public String traductor(String palabras, int k) throws IOException {
    String contextEs = "contexto ;;; ";
    while (k + auxk < keys.size()) {
      List<NameValuePair> list = new ArrayList();
      NameValuePair nv1;
      log.info("using k " + (k + auxk));
      nv1 = new BasicNameValuePair("key", keys.get(k + auxk));
      list.add(nv1);
      NameValuePair nv2 = new BasicNameValuePair("lang", "es-en");
      list.add(nv2);
      NameValuePair nv3 = new BasicNameValuePair("text", contextEs + palabras);
      list.add(nv3);
      NameValuePair nv4 = new BasicNameValuePair("options", "1");
      list.add(nv4);
      try {
        URIBuilder builder = new URIBuilder("https://translate.yandex.net/api/v1.5/tr.json/translate");
        HttpPost request = new HttpPost(builder.build());
        request.setEntity(new UrlEncodedFormEntity(list, StandardCharsets.UTF_8));
        Object response = executeServicePath(request, "$.text[*]");
        if (response instanceof Boolean) {
          auxk = auxk + 1;
          log.info("Trying again with " + k + auxk);
        } else {
          JSONArray rsp = (JSONArray)response;
          return rsp.get(0).toString();
        }
      } catch (URISyntaxException ex) {
        log.debug(ex.getMessage());
        return null;
      }
    }
    return null;
  }

  public Object executeServicePath(HttpUriRequest request, String path) {
    while (true) {
      try {
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();

        if (entity != null && response.getStatusLine().getStatusCode() == 200) {

          try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"))) {
            String jsonResult = reader.readLine();
            // Object parserresponse = new JsonParser().parse(jsonResult);
            // return true;
            return JsonPath.read(jsonResult, path);
          } catch (Exception e) {
            System.out.print("Error Path" + e);
            return null;
          }
        } else if (response.getStatusLine().getStatusCode() == 403 || response.getStatusLine().getStatusCode() == 429) {
          log.error(response.toString());
          httpClient = HttpClients.createDefault();
          System.out.print("Change request");
          return false;
        } else {

          log.error(response.toString());
          System.out.print("Trying again");
          httpClient = HttpClients.createDefault();
          //return null;
        }
      } catch (UnknownHostException e) {
        log.error("Can't reach host in service: Detect Language", e);
        httpClient = HttpClients.createDefault();
      } catch (IOException ex) {
        log.error("Cannot make request", ex);
      }
    }
  }

}
