/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.function;

import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.ucuenca.wk.commons.impl.CommonsServicesImpl;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;

/**
 *
 * @author FernandoBac
 */
public class TranslateForSemanticDistance {

    // JDBC driver name and database URL
    private String dburl = "";
    //  Database credentials
    private String user = "";
    private String pass = "";
    private CommonsServices commonservices = new CommonsServicesImpl();

    private Connection conn = null;
    //Statement stmt = null;

    private JsonObject config = null;

    public TranslateForSemanticDistance() throws IOException, ClassNotFoundException {
        JsonParser parser = new JsonParser();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/config.cnf");
        //String readFile = readFile("./config.cnf", Charset.defaultCharset());
        String theString = IOUtils.toString(resourceAsStream, Charset.defaultCharset().toString());
        
        config = parser.parse(theString).getAsJsonObject();
        dburl = dburl + config.get("dbServer").getAsString() + "/" + config.get("dbSchema").getAsString();
        user = config.get("dbUser").getAsString();
        pass = config.get("dbPassword").getAsString();

    }

    public void close() throws SQLException {
        //conn.close();
    }

    public List<String> traductor(List<String> join) throws SQLException, IOException, ClassNotFoundException {
       Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection(dburl, user, pass);
        boolean truevalue = true;
        List<String> ls = new ArrayList();
        for (String w : join) {
            if (truevalue) {
                ls.add(traductorYandex(w.trim()).trim().toLowerCase());
            } else {
                ls.add(traductorBing(w.trim()).trim().toLowerCase());
            }
        }
        conn.close();
        return ls;
    }

    private String traductorYandex(String palabras) throws UnsupportedEncodingException, SQLException, IOException {
        String url = "https://translate.yandex.net/api/v1.5/tr.json/translate";
        //String url = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20160321T160516Z.43cfb95e23a69315.6c0a2ae19f56388c134615f4740fbb1d400f15d3&lang=en&text=" + URLEncoder.encode(palabras, "UTF-8");
        ConcurrentHashMap<String, String> mp = new ConcurrentHashMap<>();
        mp.put("key", "trnsl.1.1.20160321T160516Z.43cfb95e23a69315.6c0a2ae19f56388c134615f4740fbb1d400f15d3");
        mp.put("lang", "en");
        mp.put("text", palabras);
        mp.put("options", "1");
        boolean c = true;
        do {
            try {
                String http = http2(url, mp);
                String res = http;
                JsonParser parser = new JsonParser();
                JsonObject parse = parser.parse(res).getAsJsonObject();
                JsonArray asArray = parse.get("text").getAsJsonArray();
                res = asArray.get(0).getAsString();
                palabras = res;
                c = false;
            } catch (Exception e) {
                e.printStackTrace(new PrintStream(System.out));

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SemanticDistance.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //System.out.println("In2");
        } while (c);
        return palabras;

    }

    private String traductorBing(String palabras) {
        boolean falsevalue = false;
        if (falsevalue) {
            Translate.setClientId("fedquest");
            Translate.setClientSecret("ohCuvdnTlx8Sac4r7gfqyHy0xOJJpKK9duFC4tn9Sho=");
        } else {
            Translate.setClientId("karyabad");
            Translate.setClientSecret("viz4JYZAD8samvwuoV6gJ5MczDig8cBHyP0NnY1gRF0=");
        }

        String translatedText;
        try {
            translatedText = Translate.execute(palabras, Language.ENGLISH);

            return translatedText;
        } catch (Exception ex) {

            try {
                String[] ls = palabras.split("\\s\\|\\s");
                int chunk = ls.length / 2; // chunk size to divide
                String pal = "";
                for (int i = 0; i < ls.length; i += chunk) {
                    String[] pr = java.util.Arrays.copyOfRange(ls, i, i + chunk);
                    pr = clean2(pr);
                    String u = Joiner.on(" | ").join(pr);
                    u = traductorBing(u);
                    pal += u + " ";

                }
                return pal;

            } catch (Exception exx) {
                exx.printStackTrace(new PrintStream(System.out));
            }
        }
        return palabras;
    }

    private synchronized String http2(String s, Map<String, String> mp) throws SQLException, IOException {
        String md = s + mp.toString();
        Statement stmt = conn.createStatement();
        String sql;
        sql = "SELECT * FROM cache where cache.key='" + commonservices.getMD5(md) + "'";
        java.sql.ResultSet rs = stmt.executeQuery(sql);
        String resp = "";
        if (rs.next()) {
            resp = rs.getString("value");
            rs.close();
            stmt.close();
        } else {
            rs.close();
            stmt.close();

            HttpClient client = new HttpClient();
            PostMethod method = new PostMethod(s);

            //Add any parameter if u want to send it with Post req.
            for (Map.Entry<String, String> mcc : mp.entrySet()) {
                method.addParameter(mcc.getKey(), mcc.getValue());
            }
            int statusCode = client.executeMethod(method);

            if (statusCode != -1) {
                InputStream in = method.getResponseBodyAsStream();
                final Scanner reader = new Scanner(in, "UTF-8");
                while (reader.hasNextLine()) {
                    final String line = reader.nextLine();
                    resp += line + "\n";
                }
                reader.close();
                try {
                    JsonParser parser = new JsonParser();
                    parser.parse(resp);
                    PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO cache (key, value) values (?, ?)");
                    stmt2.setString(1, commonservices.getMD5(md));
                    stmt2.setString(2, resp);
                    stmt2.executeUpdate();
                    stmt2.close();
                } catch (Exception e) {
                }
            }
        }

        return resp;
    }

    private String[] clean2(final String... v) {
        List<String> list = new ArrayList<String>(java.util.Arrays.asList(v));
        list.removeAll(Collections.singleton(null));
        return list.toArray(new String[list.size()]);
    }

    public List<String> clean(List<String> ls) {
        List<String> al = ls;
        Set<String> hs = new HashSet<>();
        hs.addAll(al);
        al.clear();
        al.addAll(hs);
        JsonArray asArray = config.get("stopwords").getAsJsonArray();
        for (JsonElement s : asArray) {
            al.remove(s.getAsString());
        }

        return al;
    }

}
