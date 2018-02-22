/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.function;

import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.lang3.StringEscapeUtils;

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

    public static String unEscapeHTML4(String name) {
        char eq = '=';
        String finalResult = "";
        boolean betweenEqs = false;
        String partialText = "";
        for (char a : name.toCharArray()) {
            partialText += a;
            if (a == eq) {
                if (!betweenEqs) {
                    betweenEqs = true;
                    partialText = "=";
                } else {
                    String unescapeText = partialText.replaceAll("\\=([^=:]{1,})\\=", "&$1;");
                    String escapeText = StringEscapeUtils.unescapeHtml4(unescapeText);
                    if (!escapeText.equals(unescapeText)) {
                        finalResult += escapeText;
                        betweenEqs = false;
                        partialText = "";
                        continue;
                    } else {
                        finalResult += partialText.substring(0, partialText.length() - 1);
                        partialText = "=";
                        betweenEqs = true;
                    }
                }
            }
            if (!betweenEqs) {
                partialText = "";
                finalResult += a;
            }
        }
        finalResult += partialText;
        String stopChars = "\\=|\\-|\\_|\\.|\\||\\&|\\?";
        finalResult = finalResult.replaceAll(stopChars, " ").replaceAll("(\\s+)", " ");
        return finalResult.trim();
    }

}
