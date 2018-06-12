/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services.providers;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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
import org.apache.marmotta.ucuenca.wk.pubman.api.AbstractProviderService;
import org.apache.marmotta.ucuenca.wk.pubman.exceptions.QuotaLimitException;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class GoogleScholarProviderService extends AbstractProviderService {

    private final String template = "https://scholar.google.com/citations?mauthors=%s&hl=en&view_op=search_authors";

    @Override
    protected List<String> buildURLs(String firstname, String lastname, List<String> organizations) {
        List<String> queries = new ArrayList<>(organizations.size());

        String[] names = firstname.split(" ");
        if (names.length > 1) {
            firstname = names[0] + " OR " + names[1];
        } else {
            firstname = names[0];
        }

        // (^\\h*)|(\\h*$) = Replace non breaking space (#160 ascii)
        String pattern = "(^[\t\u00A0\u1680\u180e\u2000\u200a\u202f\u205f\u3000]*)|([\t\u00A0\u1680\u180e\u2000\u200a\u202f\u205f\u3000]*$)";
        firstname = firstname.replaceAll(pattern, "");
        lastname = lastname.split(" ")[0].replaceAll(pattern, "");
        for (String organization : organizations) {
            organization = organization.toLowerCase();
            String query = String.format("%s %s %s", firstname, lastname, organization)
                    .trim()
                    .replace(' ', '+');
            queries.add(String.format(template, query));
        }
        return queries;
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

    @Override
    protected void retryPlan() {
        // TODO: come up with a better plan.
        throw new RuntimeException(
                new QuotaLimitException("Cannot continue scrapping Google Scholar. "
                        + "Restart the process once you can continue sending requests."));
    }

    @Override
    protected String getProviderGraph() {
        return constantService.getGoogleScholarGraph();
    }

    @Override
    protected String getProviderName() {
        return "Google Scholar";
    }

}
