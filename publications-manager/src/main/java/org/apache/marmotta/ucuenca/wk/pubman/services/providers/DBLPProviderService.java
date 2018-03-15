/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services.providers;

import com.google.common.base.Preconditions;
import edu.emory.mathcs.backport.java.util.Collections;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.pubman.api.AbstractProviderService;

/**
 *
 * @author José Ortiz
 */
public class DBLPProviderService extends AbstractProviderService {

    @Inject
    private CommonsServices commonsServices;

    @Override
    protected List<String> buildURLs(String firstName, String lastName, List<String> organizations) {
        Preconditions.checkArgument(firstName != null && !"".equals(firstName.trim()));
        Preconditions.checkArgument(lastName != null && !"".equals(lastName.trim()));
        firstName = or(firstName);
        lastName = or(lastName);
        String NS_DBLP = "http://rdf.dblp.com/ns/search/";
        String URI = NS_DBLP + URLEncoder.encode(firstName + "_" + lastName);
        return Collections.singletonList(URI);
    }

    @Override
    protected String getProviderGraph() {
        return constantService.getDBLPGraph();
    }

    @Override
    protected String getProviderName() {
        return "DBLP";
    }

    public String or(String name) {
        name = StringUtils.stripAccents(name).trim().toLowerCase().replaceAll("\\.|,|;|:|-|\n|\\\\|\\||\"|\'|_|/", " ").trim();
        String s = "";
        String[] tokens = name.split(" ");
        List<String> list = new ArrayList<String>(Arrays.asList(tokens));
        list.removeAll(Arrays.asList("", null));
        for (int i = 0; i < list.size(); i++) {
            s += list.get(i) + (i == list.size() - 1 ? "" : "-");
        }
        return s;
    }

    @Override
    protected LDClient buildDefaultLDClient() {
        // Set custom HTTPClient to skip certificate problems with scholar
        HttpParams httpParams = new BasicHttpParams();
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs,
                        String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs,
                        String authType) {
                }
            }}, new SecureRandom());
            SSLSocketFactory sf = new SSLSocketFactory(sslcontext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            schemeRegistry.register(new Scheme("https", 443, sf));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
        cm.setMaxTotal(20);
        cm.setDefaultMaxPerRoute(10);
        DefaultHttpClient client = new DefaultHttpClient(cm, httpParams);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setHttpClient(client);
        return new LDClient(conf);
    }
}
