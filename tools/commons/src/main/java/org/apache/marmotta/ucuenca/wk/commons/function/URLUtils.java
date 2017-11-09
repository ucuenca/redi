/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.function;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author José Ortiz
 */
public final class URLUtils {

    private static int retryCode = 429;

    private URLUtils() {
    }

    public static String getFinalURL(String url, int lvl) {
        try {
            Delay.call();
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setInstanceFollowRedirects(false);
            con.connect();
            con.getInputStream();
            if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP || con.getResponseCode() == HttpURLConnection.HTTP_SEE_OTHER) {
                String redirectUrl = con.getHeaderField("Location");

                if (lvl <= 0) {
                    return redirectUrl;
                } else {
                    return getFinalURL(redirectUrl, lvl - 1);
                }
            } else if (con.getResponseCode() == retryCode) {
                String time = con.getHeaderField("Retry-After");
                int parseInt = Integer.parseInt(time);
                Thread.sleep(1000 * parseInt);
                return getFinalURL(url, lvl);
            }

        } catch (Exception ex) {
        }
        return url;
    }

}
