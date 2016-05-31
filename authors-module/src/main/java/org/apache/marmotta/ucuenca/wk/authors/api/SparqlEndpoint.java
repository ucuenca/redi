/**
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
package org.apache.marmotta.ucuenca.wk.authors.api;

import java.util.HashSet;
import java.util.Set;
import static org.apache.marmotta.commons.http.MarmottaHttpUtils.parseAcceptHeader;
import org.apache.marmotta.commons.http.ContentType;

/*
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
*/

public class SparqlEndpoint implements Comparable<SparqlEndpoint> {

    /**
     * constant indicating high priority of this endpoint definition
     */
    public final static int PRIORITY_HIGH = 3;

    /**
     * constant indicating medium priority of this endpoint definition
     */
    public final static int PRIORITY_MEDIUM = 2;

    /**
     * constant indicating low priority of this endpoint definition
     */
    public final static int PRIORITY_LOW = 1;

    /**
     * A state enable to work ( true || false ) for this endpoint.
     */
    private String status;
    
    /**
     * A human-readable name for this endpoint.
     */
    private String name;

    /**
     * The HTTP URL to access the endpoint. 
     * Examples:
     * http://dbpedia.org/sparql
     */
    private String endpointUrl;

    /**
     * graph where the information will be extracted
     */
    private String graph;

    /**
     * A URI of a endpoint resource
     * combine with HASH CODE of (name+url+graph)
     * this is using for find an author ( example:  Delete Case )
     */
    private String resourceId;

    /**
     * Full name for this endpoint.
     */
    private String fullName;
    
    /**
     * City name for this endpoint.
     */
    private String city;
    
    /**
     * Province name for this endpoint.
     */
    private String province;
    
    /**
     * Latitude for this endpoint.
     */
    private String latitude;
    
    /**
     * Longitude for this endpoint.
     */
    private String longitude;
    
    /**
     * The type of the endpoint. Either the name of a data provider, or the special name "NONE" to indicate that
     * the data for these URIs should not be retrieved.
     */
    //private String type;


    public static final String REGEX_INDICATOR = "~";

    /**
     * A regular expression describing for which URIs the endpoint applies. The endpoint will be applied to all
     * resource requests matching with this pattern.
     * 
     * @see Pattern
     */
    //private String uriPattern;

    //private Pattern uriPatternCompiled = null;

    /**
     * Flag to temporarily enable/disable the endpoint configuration at runtime.
     */
    private boolean            active;

    /**
     * The priority of this endpoint configuration; endpoints with higher priority take precedence over
     * endpoints with lower priority in case both would be applicable.
     */
    private int priority = PRIORITY_MEDIUM;

    
    
    /**
     * The content type (MIME) returned by this endpoint. Used to determine how to parse the result.
     */
    private Set<ContentType> contentTypes;

    /**
     * The default expiry time in seconds to use for this endpoint if the HTTP request does not explicitly return an
     * expiry time.
     */
   /** private Long defaultExpiry;
 
    
    /**
     * Additional configuration options used by this endpoint. Can be accessed in the different providers.
     */
    //private Map<String,String> properties;

    public SparqlEndpoint() {
        this.active = true;
    //    this.contentTypes = new HashSet<ContentType>();
    //    this.properties = new HashMap<String, String>();    	
    }

    public SparqlEndpoint(String resourceId, String name, String endpointUrl, String graph) {
    	this();
        this.resourceId = resourceId;
        this.name = name;
        this.endpointUrl = endpointUrl;
        this.graph = graph;
    }

    public SparqlEndpoint(String resourceId, String name,  String endpointUrl, String contentType, String graph) {
        this(resourceId, name,endpointUrl,graph);
        this.contentTypes = new HashSet<ContentType>(parseAcceptHeader(contentType));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
      public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }
    
      public String getGraph() {
        return graph;
    }

    public void setGraph(String graph) {
        this.graph = graph;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
  
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public Set<ContentType> getContentTypes() {
        return contentTypes;
    }
    public void addContentType(ContentType type) {
        contentTypes.add(type);
    }
    public void setContentTypes(Set<ContentType> contentTypes) {
        this.contentTypes = contentTypes;
    }
  
/*
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
*/
    /*
    public String getUriPattern() {
        return uriPattern;
    }
*/
  
/*
    public void setUriPattern(String uriPattern) {
        this.uriPattern = uriPattern;
        this.uriPatternCompiled = null;
    }
*/
    

    
  

    
    public int getPriority() {
        return priority;
    }

    
    public void setPriority(int priority) {
        this.priority = priority;
    }

  /*
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
*/
    /*
    public String getProperty(String key) {
        return properties.get(key);
    }
*/
    /*
    public void setProperty(String key, String value) {
        properties.put(key,value);
    }
*/
    /*
    public Pattern getUriPatternCompiled() {
        if (uriPatternCompiled == null) {
            try {
                if (uriPattern.startsWith(REGEX_INDICATOR)) {
                    // backwards compatibility
                    uriPatternCompiled = Pattern.compile(uriPattern.substring(REGEX_INDICATOR.length()));
                } else {
                    uriPatternCompiled = Pattern.compile(uriPattern);
                }
            } catch (PatternSyntaxException pse) {
            }
        }
        return uriPatternCompiled;
    }
*/
    /**
     * Check if this {@link Endpoint} handles (is responsible) for this URI.
     * 
     * @param uri the URI to check
     * @return <code>true</code> if the uri matches the endpoint's {@link #uriPattern}
     * 
     * @see #uriPattern
     */
  /*  public boolean handles(String uri) {
        if (isActive() && getUriPatternCompiled() != null)
        {
            return getUriPatternCompiled().matcher(uri).find();
        }
        return false;
    }
*/
    /*
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SparqlEndpoint endpoint = (SparqlEndpoint) o;

        if (name != null ? !name.equals(endpoint.name) : endpoint.name != null) return false;
        if (type != null ? !type.equals(endpoint.type) : endpoint.type != null) return false;
        if (uriPattern != null ? !uriPattern.equals(endpoint.uriPattern) : endpoint.uriPattern != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (uriPattern != null ? uriPattern.hashCode() : 0);
        return result;
    }

*/
    /**
     * Compares this object with the specified object for order according to priority.
     * Returns a negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @see #getPriority()
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
  
    @Override
    public int compareTo(SparqlEndpoint o) {
        if(getPriority() > o.getPriority()) {
            return -1;
        } else if(getPriority() < o.getPriority()) {
            return 1;
        } else {
            return 0;
        }
    }
    
}
