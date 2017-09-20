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

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

import at.newmedialab.lmf.util.geonames.api.GeoLookup;

/**
 * Builder to build a new instance of GeoLookup.
 * <p>
 * <strong>Usage:</strong><br>
 * <pre>
 * GeoLookup geo = GeoLookupBuilder.geoLookup()
 *                     // provide Username
 *                     .withAccount(username, password)
 *                     // filter by FeatureClass
 *                     .includeFeatureClass(FeatureClass.PopulatedPlace)
 *                     // filter by Continent
 *                     .restrictToContinent(Continent.Europe)
 *                     // configure cache-size
 *                     .cacheSize(10000)
 *                     // be threadsafe
 *                     .multiThreadable()
 *                     .create();
 * </pre>
 * 
 * @author Jakob Frank <jakob.frank@salzburgresearch.at>
 *
 */
public class GeoLookupBuilder {

    /**
     * The old geonames service url (still valid, allows anonymous requests).
     */
    public static final String GEONAMES_LEGACY = "http://ws.geonames.org/";
    /**
     * New geonames service url, requires valid username&password.
     */
    public static final String GEONAMES_API = "http://api.geonames.org/";

    /**
     * Provides user-friendly, readable names for the continent-codes in the geonames service.
     */
    public enum Continent {
        Africa("AF"), Asia("AS"), Europe("EU"), NortAmerica("NA"), Oceania("OC"), SouthAmerica("SA"), Antarctica("AN");

        private final String code;
        private Continent(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
    }
    
    /**
     * Provides user-friendly, readable names for the geonames feature classes. <b>Note:</b> Some feature-classes are aliases for other feature-classes.
     * @see <a href="http://www.geonames.org/export/codes.html">http://www.geonames.org/export/codes.html</a>
     */
    public enum FeatureClass {
        A('A'), Country('A'), State('A'), Region('A'), 
        H('H'), Stream('H'), Lake('H'), 
        L('L'), Park('L'), Area('L'), 
        P('P'), PopulatedPlace('P'), City('P'), Village('P'), 
        R('R'), Road('R'), Railroad('R'), 
        S('S'), Spot('S'), Building('S'), Farm('S'), 
        T('T'), Terrain('T'), Mountain('T'), Hill('T'), Rock('T'), 
        U('U'), Undersea('U'), 
        V('V'), Vegetation('V'), Forrest('V'), Heath('V');
        
        private final char clazz;

        private FeatureClass(char clazz) {
            this.clazz = clazz;
        }
        
        public char getClassCode() {
            return clazz;
        }
    }
    
    private HttpClient http = null;
    private String geoNamesServer = null;
    private String geoNamesUser = null, geoNamesPasswd = null;

    private String countries[] = {}, continentCodes[] = {}, countryBias = null, featureCodes[] = {};
    private char featureClasses[] = {};
    private float fuzzy = 1;
    private int cacheSize = 50000;
    private int cacheTimeout = 60;
    private boolean multiThreading = false;

    
    private GeoLookupBuilder() {
        // created via static factory.
    }

    /**
     * Creates a new {@link GeoLookupBuilder}
     */
    public static GeoLookupBuilder geoLookup() {
        return new GeoLookupBuilder();
    }

    /**
     * Creates a new {@link GeoLookupBuilder} using the provided geoNamesServer
     * @param geoNamesServer the geonames server url
     */
    public static GeoLookupBuilder geoLookup(String geoNamesServer) {
        return new GeoLookupBuilder().usingServer(geoNamesServer);
    }
    
    /**
     * Make all requests through this {@link HttpClient}.
     * Multi-threading must be handled by the client, the {@link #multiThreadable()} setting is ignored if a {@link HttpClient} is provided.
     * @param httpClient the {@link HttpClient} to use for the requests.
     */
    public GeoLookupBuilder throughHttpClient(HttpClient httpClient) {
        this.http = httpClient;
        return this;
    }
    
    /**
     * Set the login credentials
     * @param username the username
     * @param passwd the password
     */
    public GeoLookupBuilder withAccount(String username, String passwd) {
        geoNamesUser = username;
        geoNamesPasswd = passwd;
        return this;
    }
    
    /**
     * Set the geoNamesServer
     * @param geonamesServer the geonames server url
     * @return
     */
    public GeoLookupBuilder usingServer(String geonamesServer) {
        this.geoNamesServer = geonamesServer;
        return this;
    }
    
    /**
     * Change the size of the cache (default: {@code 50000}).
     * Set to {@code 0} to disable caching.
     * @param cacheSize the size of the cache
     */
    public GeoLookupBuilder withCacheSize(int cacheSize) {
        this.cacheSize  = cacheSize;
        return this;
    }
    
    /**
     * Disable caching. Has the same effect as setting the cache size to {@code 0}.
     * @see #withCacheSize(int)
     */
    public GeoLookupBuilder whithoutCaching() {
        return withCacheSize(0);
    }
    
    /**
     * Change the cache expire time (default: {@code 60}).
     * @param minutes the expire time in minutes.
     * @return
     */
    public GeoLookupBuilder withCacheTimeout(int minutes) {
        this.cacheTimeout  = minutes;
        return this;
    }
    
    /**
     * Set the country-bias for the lookup.
     * @param countryCode prefer results from this country (ISO-3166 country-code)
     * @see <a href="http://en.wikipedia.org/wiki/ISO_3166">http://en.wikipedia.org/wiki/ISO_3166</a>
     */
    public GeoLookupBuilder applyCountryBias(String countryCode) {
        countryBias = countryCode;
        return this;
    }
    
    /**
     * Set the fuzziness of the search-term (default: {@code 1}).
     * @param fuzzy the fuzziness
     */
    public GeoLookupBuilder useFuzzy(float fuzzy) {
        this.fuzzy = Math.max(0, Math.min(fuzzy, 1));
        return this;
    }
    
    /**
     * Restrict results to these countries (default: all countries).
     * @param countryCodes ISO-3166 country-codes
     * @see <a href="http://en.wikipedia.org/wiki/ISO_3166">http://en.wikipedia.org/wiki/ISO_3166</a>
     */
    public GeoLookupBuilder restrictToCountries(String... countryCodes){
        countries = countryCodes;
        return this;
    }
    
    /**
     * Restrict results to these continents (default: all continents).
     * @param continentCodes two-letter continentCode: {@code AF, AS, EU, NA, OC, SA, AN}
     */
    public GeoLookupBuilder restrictToContinents(String... continentCodes) {
        this.continentCodes = continentCodes;
        return this;
    }

    /**
     * Restrict results to these continents (default: all continents).
     * @param continents the continents
     * @see Continent
     */
    public GeoLookupBuilder restrictToContinents(Continent... continents) {
        String[] cc = new String[continents.length];
        for (int i = 0; i < cc.length; i++) {
            cc[i] = continents[i].getCode();
        }
        
        this.continentCodes = cc;
        return this;
    }
    
    /**
     * Include (only) results whith one of these feature codes (default: all feature codes).
     * @param featureCodes the feature codes
     * @see <a href="http://www.geonames.org/export/codes.html">http://www.geonames.org/export/codes.html</a>
     */
    public GeoLookupBuilder includeFeatureCodes(String... featureCodes) {
        this.featureCodes = featureCodes;
        return this;
    }
    
    /**
     * Include (only) results with one of these feature classes (default: all classes)
     * @param featureClasses the feature classes
     * @see <a href="http://www.geonames.org/export/codes.html">http://www.geonames.org/export/codes.html</a>
     * @see #includeFeatureClasses(FeatureClass...)
     */
    public GeoLookupBuilder includeFeatureClasses(char... featureClasses) {
        this.featureClasses = featureClasses;
        return this;
    }

    /**
     * Include (only) results with one of these feature classes (default: all classes)
     * @param featureClasses the feature classes
     * @see FeatureClass
     */
    public GeoLookupBuilder includeFeatureClasses(FeatureClass... featureClasses) {
        char[] cc = new char[featureClasses.length];
        for (int i = 0; i < cc.length; i++) {
            cc[i] = featureClasses[i].getClassCode();
        }
        this.featureClasses = cc;
        
        return this;
    }

    /**
     * Make the {@link GeoLookupImpl} threadsafe.
     */
    public GeoLookupBuilder multiThreadable() {
        multiThreading  = true;
        return this;
    }


    /**
     * Create the {@link GeoLookupImpl} instance based on the settings in this {@link GeoLookupBuilder}.
     * @return the ready-to-use {@link GeoLookupImpl}
     */
    public GeoLookup create() {
        GeoLookupImpl service = new GeoLookupImpl(getHttpClient(), getServerUrl(), geoNamesUser, geoNamesPasswd, cacheSize, cacheTimeout);
        
        service.setContinentCode(continentCodes);
        service.setCountry(countries);
        service.setCountryBias(countryBias);
        service.setFeatureClass(featureClasses);
        service.setFeatureCode(featureCodes);
        service.setFuzzy(fuzzy);
        
        return service;
    }

    private HttpClient getHttpClient() {
        if (http == null) {
            if (multiThreading) {
                return new DefaultHttpClient(new PoolingClientConnectionManager());
            } else {
                return new DefaultHttpClient();
            }
        } else {
            return http;
        }
    }
    
    private String getServerUrl() {
        if (geoNamesServer == null) {
            if (geoNamesUser == null) {
                return GEONAMES_LEGACY;
            } else {
                return GEONAMES_API;
            }
        } else {
            final String url;
            if (StringUtils.startsWithAny(geoNamesServer, new String[] {"http://", "https://"})) {
                url = geoNamesServer;
            } else {
                url = "http://" + geoNamesServer;
            }
            if (!url.endsWith("/")) {
                return url + "/";
            } else {
                return url;
            }
        }
    }
    
}
