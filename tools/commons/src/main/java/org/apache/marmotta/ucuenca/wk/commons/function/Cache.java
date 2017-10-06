/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.function;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
//import org.mapdb.serializer.SerializerCompressionWrapper;
//import org.mapdb.serializer.SerializerArrayTuple;

/**
 *
 * @author Jose Luis Cullcay
 */
public final class Cache {

    private DB db = null;
    private DB dbDist = null;
    private HTreeMap<String, String> create = null;
    private HTreeMap<String, Double> distance = null;
    private double limit = 0.002;
    private JsonObject config = null;
    private String base1 = "data.db";
    private String base2 = "distance.db";

    public Cache() {
        if (base1 == null || base2 == null) {
            InputStream resourceAsStream = this.getClass().getResourceAsStream("/config.cnf");
            String theString = null;
            try {
                theString = IOUtils.toString(resourceAsStream, Charset.defaultCharset().toString());
            } catch (IOException ex) {
                //Logger.getLogger(Cache.class.getName()).log(Level.SEVERE, null, ex);
            }
            config = JSON.parse(theString).getAsObject();
            base1 = config.get("CacheFile").getAsString().value();
            base2 = config.get("DistanceFile").getAsString().value();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    //System.out.println("Killing ");
                    kill();
                } catch (Exception ex) {
                    //Logger.getLogger(Cache.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        ));

    }

    public void getInstanceDB() {
        db = DBMaker.newFileDB(new File(base1)).closeOnJvmShutdown().make();
        create = db.getHashMap("cache");
    }

    public void getInstanceDBDistance() {
        dbDist = DBMaker.newFileDB(new File(base2)).closeOnJvmShutdown().make();
        distance = dbDist.getHashMap("distance");
    }

    public void put(String key, String value) {
        create.put(key, value);
        //db.commit();
    }

    public String get(String key) {
        return create.get(key);
    }

    public void putDistance(String word1, String word2, Double dist) {
        distance.put(word1 + "-" + word2, dist);
        //dbDist.commit();
    }

    public Double getDistance(String word1, String word2) {
        Double dist = distance.get(word1 + "-" + word2);
        return (dist != null) ? dist : distance.get(word2 + "-" + word1);
    }

    public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void kill() {

        if (Math.random() < limit) {
            if (db != null) {
                db.commit();
                db.compact();
            }
            if (dbDist != null) {
                dbDist.commit();
                dbDist.compact();
            }
        }

        if (create != null) {
            create.close();
        } else if (distance != null) {
            distance.close();
        }

        if (db != null && !db.isClosed()) {
            db.close();
        } else if (dbDist != null && !dbDist.isClosed()) {
            dbDist.close();
        }

    }

    public static String getFinalURL(String url) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setInstanceFollowRedirects(false);
            con.connect();
            con.getInputStream();

            if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP
                    || con.getResponseCode() == HttpURLConnection.HTTP_SEE_OTHER) {
                String redirectUrl = con.getHeaderField("Location");
                return getFinalURL(redirectUrl);
            }

        } catch (Exception ex) {
        }
        return url;
    }

}
