/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.newmedialab.lmf.util.geonames.builder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.newmedialab.lmf.util.geonames.api.GeoLookup;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Default implementation of {@link GeoLookup}.
 * Supports lazy initialized caching and custom {@link HttpClient}.
 *
 * Create instances using the {@link GeoLookupBuilder}. 
 * 
 * @see <a href="http://www.geonames.org/export/geonames-search.html">http://www.geonames.org/export/geonames-search.html</a>
 * 
 * @author Jakob Frank <jakob.frank@salzburgresearch.at>
 * @author Sebastian Schaffert <sebastian.schaffert@salzburgresearch.at>
 * 
 */
class GeoLookupImpl implements GeoLookup {

    private static Logger log = LoggerFactory.getLogger(GeoLookupImpl.class);

    private static final String GEONAMES_URI_PATTERN = "http://sws.geonames.org/%s/";

    private final HttpClient http;
    private final String geoNamesUrl, geoNamesUser, geoNamesPasswd;

    private String countries[] = {}, continentCodes[] = {}, countryBias = null, featureCodes[] = {};
    private char featureClasses[] = { 'P' };
    private float fuzzy = 1;

    private final LoadingCache<String,String> placeCache;

     GeoLookupImpl(HttpClient http, String geoNamesUrl, String geoNamesUser, String geoNamesPasswd, int cacheSize, int cacheExpiry) {
        this.http = http;
        // make sure that the geoNamesUrl ends with '/'
        this.geoNamesUrl = StringUtils.endsWith(geoNamesUrl, "/")?geoNamesUrl:(geoNamesUrl+"/");
        this.geoNamesUser = geoNamesUser;
        this.geoNamesPasswd = geoNamesPasswd;

        placeCache = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterAccess(cacheExpiry, TimeUnit.MINUTES)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) throws Exception {
                        String result = queryPlace(key);
                        if(StringUtils.isEmpty(result)) {
                            throw new Exception("Could not resolve '" + key + "'");
                        } else {
                            return result;
                        }
                    }
                });
    }


    /**
     * Query the geonames-api for a place with the provided name.
     * @param name the name of the place to lookup at geonames
     * @return the URI of the resolved place, or null if no such place exists.
     */
    private String queryPlace(String name) {
        if (StringUtils.isBlank(name)) return null;

        StringBuilder querySB = new StringBuilder();
        queryStringAppend(querySB, "q", name);

        queryStringAppend(querySB, "countryBias", countryBias);
        for (char fc : featureClasses) {
            queryStringAppend(querySB, "featureClass", String.valueOf(fc));
        }
        for (String fc : featureCodes) {
            queryStringAppend(querySB, "featureCode", fc);
        }
        for (String c : countries) {
            queryStringAppend(querySB, "country", c);
        }
        for (String cc : continentCodes) {
            queryStringAppend(querySB, "continentCode", cc);
        }
        if (fuzzy < 1) {
            queryStringAppend(querySB, "fuzzy", String.valueOf(fuzzy));
        }

        queryStringAppend(querySB, "maxRows", "1");
        queryStringAppend(querySB, "type", "xml");
        queryStringAppend(querySB, "isNameRequired", "true");
        queryStringAppend(querySB, "style", "short");

        if (StringUtils.isNotBlank(geoNamesUser))
            queryStringAppend(querySB, "username", geoNamesUser);
        if (StringUtils.isNotBlank(geoNamesPasswd))
            queryStringAppend(querySB, "password", geoNamesPasswd);

        final String url = geoNamesUrl + "search?" + querySB.toString();
        HttpGet get = new HttpGet(url);

        try {
                return http.execute(get, new ResponseHandler<String>() {
                    /**
                     * Parses the xml-response from the geonames webservice and build the uri for the result
                     * @return the URI of the resolved place, or null if no place was found.
                     */
                    @Override
                    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                        final int statusCode = response.getStatusLine().getStatusCode();
                        if (!(statusCode >= 200 && statusCode < 300)) {
                            return null;
                        }
                        try {
                            SAXBuilder builder = new SAXBuilder();
                            final HttpEntity entity = response.getEntity();
                            if (entity == null)
                                throw new ClientProtocolException("Body Required");
                            final Document doc = builder.build(entity.getContent());

                            final Element root = doc.getRootElement();
                            final Element status = root.getChild("status");
                            if (status != null) {
                                final int errCode = Integer.parseInt(status.getAttributeValue("value"));
                                if (errCode == 15) {
                                    // NO RESULT should not be an exception
                                    return null;
                                }

                                throw new GeoNamesException(errCode, status.getAttributeValue("message"));
                            }
                            final Element gName = root.getChild("geoname");
                            if (gName == null)
                                return null;

                            final String geoId = gName.getChildTextTrim("geonameId");
                            if (geoId == null)
                                return null;

                            return String.format(GEONAMES_URI_PATTERN, geoId);
                        } catch (NumberFormatException e) {
                            throw new ClientProtocolException(e);
                        } catch (IllegalStateException e) {
                            throw new ClientProtocolException(e);
                        } catch (JDOMException e) {
                            throw new IOException(e);
                        }
                    }
                });
        } catch (GeoNamesException e) {
            log.debug("Lookup at GeoNames failed: {} ({})", e.getMessage(), e.getErrCode());
        } catch (ClientProtocolException e) {
            log.error("Could not query geoNames: " + e.getLocalizedMessage(), e);
        } catch (IOException e) {
            log.error("Could not query geoNames: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Append "{@code &paramName=paramValue}" to the provided StringBuilder.
     * The '{@code &}' will be skipped if the StringBuilder is emtpy. 
     * @param sb
     * @param paramName
     * @param paramValue
     */
    private static final void queryStringAppend(StringBuilder sb, String paramName,
            String paramValue) {
        if (StringUtils.isBlank(paramValue) || StringUtils.isBlank(paramName)) return;
        try {
            if (sb.length() > 0) sb.append('&');
            sb.append(URLEncoder.encode(paramName, "UTF-8"));
            sb.append('=');
            sb.append(URLEncoder.encode(paramValue, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is always there
        }
    }

    /* (non-Javadoc)
     * @see at.newmedialab.lmf.importer.geo.IGeoService#resolvePlace(java.lang.String)
     */
    @Override
    public String resolvePlace(String geoName) {
        try {
            return placeCache.get(cleanPlace(geoName));
        } catch (ExecutionException e) {
            log.debug("No result found: {}", e.getCause().getMessage());
            return null;
        }
    }

    /* (non-Javadoc)
     * @see at.newmedialab.lmf.importer.geo.IGeoService#resolvePlaces(java.lang.String)
     */
    @Override
    public List<String> resolvePlaces(String... geoNames) {
        return resolvePlaces(Arrays.asList(geoNames));
    }

    /* (non-Javadoc)
     * @see at.newmedialab.lmf.importer.geo.IGeoService#resolvePlaces(java.util.List)
     */
    @Override
    public List<String> resolvePlaces(List<String> geoNames) {
        LinkedList<String> result = new LinkedList<String>();
        for (String g : geoNames) {
            try {
                String p = placeCache.get(cleanPlace(g));
                if (g != null)
                    result.add(p);
            } catch (ExecutionException e) {
                log.debug("No result found: {}", e.getCause().getMessage());
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see at.newmedialab.lmf.importer.geo.IGeoService#resolvePlaces(java.util.Set)
     */
    @Override
    public Set<String> resolvePlaces(Set <String> geoNames) {
        HashSet<String> result = new HashSet<String>();
        for (String g : geoNames) {
            try {
                String p = placeCache.get(cleanPlace(g));
                if (g != null)
                    result.add(p);
            } catch (ExecutionException e) {
                log.debug("No result found: {}", e.getCause().getMessage());
            }
        }
        return result;
    }

    private String cleanPlace(String name) {
        String result = StringUtils.removeEnd(name,".");
        result = StringUtils.removeEndIgnoreCase(result,"STADT");
        result = StringUtils.removeStartIgnoreCase(result,"STADT");
        result = StringUtils.removeStart(result,"-");
        result = StringUtils.removeEnd(result,"-");
        return result;
    }

    /**
     * Get the current value(s) for the {@code country}-query parameter.
     */
    public String[] getCountries() {
        return countries;
    }

    /**
     * Set the value(s) for the {@code country}-query parameter (optional).
     * @param country country code, ISO-3166
     */
    void setCountry(String... country) {
        this.countries = country;
    }

    /**
     * Get the current value(s) for the {@code continentCode}-query parameter.
     */
    public String[] getContinentCodes() {
        return continentCodes;
    }

    /**
     * Set the value(s) for the {@code continentCode}-query parameter (optional).
     * @param continentCode continent code: AF,AS,EU,NA,OC,SA,AN 
     */
    void setContinentCode(String... continentCode) {
        this.continentCodes = continentCode;
    }

    /**
     * Get the current value for the {@code countryBias}-query parameter.
     */
    public String getCountryBias() {
        return countryBias;
    }

    /**
     * Set the value(s) for the {@code countryBias}-query parameter (optional). 
     * Records from the {@code countryBias} are listed first
     * @param countryBias country code, ISO-3166
     */
    void setCountryBias(String countryBias) {
        this.countryBias = countryBias;
    }

    /**
     * Get the current value(s) for the {@code featureClass}-query parameter.
     * @see <a href="http://www.geonames.org/export/codes.html">http://www.geonames.org/export/codes.html</a>
     */
    public char[] getFeatureClasses() {
        return featureClasses;
    }

    /**
     * Set the value(s) for the {@code featureClass}-query parameter (optional).
     * @param featureClass character A,H,L,P,R,S,T,U,V 
     * @see <a href="http://www.geonames.org/export/codes.html">http://www.geonames.org/export/codes.html</a>
     */
    void setFeatureClass(char... featureClass) {
        this.featureClasses = featureClass;
    }

    /**
     * Get the current value(s) for the {@code featureCode}-query parameter.
     * @see <a href="http://www.geonames.org/export/codes.html">http://www.geonames.org/export/codes.html</a>
     */
    public String[] getFeatureCodes() {
        return featureCodes;
    }

    /**
     * Set the value(s) for the {@code featureCode}-query parameter (optional).
     * @param featureCode the feature code(s)
     * @see <a href="http://www.geonames.org/export/codes.html">http://www.geonames.org/export/codes.html</a>
     */
    void setFeatureCode(String... featureCode) {
        this.featureCodes = featureCode;
    }

    /**
     * Get the current value(s) for the {@code fuzzy}-query parameter.
     * Defaults to {@code 1}.
     */
    public float getFuzzy() {
        return fuzzy;
    }

    /**
     * Set the value(s) for the {@code fuzzy}-query parameter (optional).
     * Default is {@code 1}.
     * @param fuzzy the fuzzy param, ensure {@code 0 <= fuzzy <= 1}
     */
    void setFuzzy(float fuzzy) {
        this.fuzzy = Math.max(0, Math.min(fuzzy, 1));
    }

    private static class GeoNamesException extends ClientProtocolException {

        private static final long serialVersionUID = 1L;
        private final int errCode;

        public GeoNamesException(int errCode, String message) {
            super(message);
            this.errCode = errCode;
        }

        public int getErrCode() {
            return errCode;
        }

    }
}
