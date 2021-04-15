/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.storage.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jayway.jsonpath.JsonPath;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.json.JSONObject;

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

    public String traductorIBM(String palabras) throws IOException, UnirestException, Exception {
        Map<String, String> keys = Maps.newHashMap();
        String keyss = conf.getStringConfiguration("ibm.translate.keys");
        String[] split = keyss.split(";;;");
        for (String m : split) {
            String[] split1 = m.split("|||");
            keys.put(split1[0], split1[1]);
        }
        List<Map.Entry<String, String>> klist = Lists.newArrayList(keys.entrySet());
        String contextEs = "contexto ;;; " + palabras;
        int strike = 0;
        String res = "";
        do {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("target", "en");
                JSONArray jsonArray = new JSONArray();
                jsonArray.add(contextEs);
                jsonObject.put("text", jsonArray);
                Random rand = new Random();
                Map.Entry<String, String> get = klist.get(rand.nextInt(klist.size()));
                com.mashape.unirest.http.HttpResponse<JsonNode> asJson = Unirest.post(get.getKey() + "/v3/translate?version=2018-05-01")
                        .basicAuth("apikey", get.getValue())
                        .header("Content-Type", "application/json")
                        .body(jsonObject).asJson();
                if (asJson.getStatus() == 200) {
                    JsonObject parse = JSON.parse(asJson.getBody().toString());
                    JsonArray asArray = parse.get("translations").getAsArray();
                    if (asArray.size() > 0) {
                        res = asArray.get(0).getAsObject().get("translation").toString();
                        break;
                    } else {
                        strike++;
                    }
                } else {
                    strike++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                strike++;
            }
        } while (strike < 4);
        if (strike >= 4) {
            throw new Exception("Translation API Error " + palabras);
        }

        return res;
    }

}
