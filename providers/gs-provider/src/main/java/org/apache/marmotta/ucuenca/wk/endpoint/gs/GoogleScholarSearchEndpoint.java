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
package org.apache.marmotta.ucuenca.wk.endpoint.gs;

import java.nio.charset.Charset;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ucuenca.wk.commons.service.DistanceService;

/**
 * Endpoint for accessing Google Scholar Data as RDF.
 *
 * Constraints: In order to avoid unnecessary requests authors are selected
 * according to a profile. So, city, province, ies, domains and resource should
 * be added.
 *
 * @author Santiago Gonzalez
 * @author Xavier Sumba
 */
public class GoogleScholarSearchEndpoint extends Endpoint {

    // TODO: find another way to read this parameters
//    private String city;
//    private String province;
    private String[] ies;
    private String[] provinces;
    private String[] cities;
    private String[] domains;
    private String resource;
    private String firstName;
    private String lastName;
    private DistanceService distance;

    public GoogleScholarSearchEndpoint() {
        super("Google Scholar Search Endpoint",
                "Google Scholar Search",
                "http(s?)://scholar\\.google\\.com/citations\\?mauthors\\=(.*)\\&hl=en\\&view_op\\=search_authors",
                null, 86400L);
        setPriority(PRIORITY_MEDIUM);
//        addContentType(new ContentType("text", "turtle", 1.0));
//        addContentType(new ContentType("text", "plain", 0.2));
        addContentType(new ContentType("text", "html", Charset.forName("UTF-8")));
        addContentType(new ContentType("*", "*", 0.1));
    }

    public String[] getCities() {
        return cities;
    }

    public void setCity(String... cities) {
        this.cities = cities;
    }

    public String[] getProvinces() {
        return provinces;
    }

    public void setProvince(String... provinces) {
        this.provinces = provinces;
    }

    public String[] getIes() {
        return ies;
    }

    public void setIes(String... ies) {
        this.ies = ies;
    }

    public String[] getDomains() {
        return domains;
    }

    public void setDomains(String... domains) {
        this.domains = domains;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public DistanceService getDistance() {
        return this.distance;
    }

    public void setDistance(DistanceService distance) {
        this.distance = distance;
    }

}
