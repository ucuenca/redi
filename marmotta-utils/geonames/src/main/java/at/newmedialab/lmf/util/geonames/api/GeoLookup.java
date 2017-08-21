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
package at.newmedialab.lmf.util.geonames.api;

import java.util.List;
import java.util.Set;

/**
 * Lookup the URI of a place using the geonames webservice-api.
 * If you encounter problems with the public request limit, consider registering an account at geonames.org.
 * 
 * @see GeoLookupBuilder
 * @see <a href="http://www.geonames.org/export/geonames-search.html">http://www.geonames.org/export/geonames-search.html</a>
 * 
 * @author Jakob Frank <jakob.frank@salzburgresearch.at>
 * @author Sebastian Schaffert <sebastian.schaffert@salzburgresearch.at>
 * 
 */
public interface GeoLookup {

    /**
     * Resolve the URI for the provided geoName.
     * @param geoName the name to lookup
     * @return the URI of the place, or null if it was not found.
     */
    public String resolvePlace(String geoName);

    /**
     * Resolve the URIs of the provided geoNames.
     * @param geoNames the names to lookup
     * @return a List of URIs of the names that where found. names that could not be resolved will be skipped.
     */
    public List<String> resolvePlaces(String... geoNames);

    /**
     * Resolve the URIs of the provided geoNames.
     * @param geoNames the names to lookup
     * @return a List of URIs of the names that where found. names that could not be resolved will be skipped.
     */
    public List<String> resolvePlaces(List<String> geoNames);

    /**
     * Resolve the URIs of the provided geoNames.
     * @param geoNames the names to lookup
     * @return a Set of URIs of the names that where found. names that could not be resolved will be skipped.
     */
    public Set<String> resolvePlaces(Set<String> geoNames);

}