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
package org.apache.marmotta.ucuenca.wk.provider.dblp;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.vocabulary.DCTERMS;
import org.apache.marmotta.commons.vocabulary.FOAF;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.provider.xml.AbstractXMLDataProvider;
import org.apache.marmotta.ldclient.provider.xml.mapping.XPathLiteralMapper;
import org.apache.marmotta.ldclient.provider.xml.mapping.XPathValueMapper;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ucuenca.wk.endpoint.dblp.DBLPResourceEndpoint;
import org.apache.marmotta.ucuenca.wk.provider.dblp.mapper.DBLPDateMapper;
import org.apache.marmotta.ucuenca.wk.provider.dblp.mapper.DBLPURIMapper;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Support DBLP Author data lookup
 * <p/>
 * Author: Santiago Gonzalez
 */
@Deprecated
public class DBLPAuthorProvider extends AbstractXMLDataProvider implements DataProvider {

    public static final String NS_AUTHOR = "http://rdf.dblp.com/ns/author/";
    public static final String NAME = "DBLP Author Provider";
    public static final String PATTERN = "http://dblp\\.org/pers/(.*)";
    public static final String LEGACY_PATTERN = "(http://dblp\\.uni\\-trier\\.de/pers/)(.*)";
    
    private static ConcurrentMap<String,String> dblpNamespaces = new ConcurrentHashMap<String, String>();
    static {
        dblpNamespaces.put("dblp","http://dblp.dagstuhl.de/rdf/schema-2015-01-26#" );
        dblpNamespaces.put("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        dblpNamespaces.put("owl","http://www.w3.org/2002/07/owl#");
        dblpNamespaces.put("dcterms","http://purl.org/dc/terms/");
        dblpNamespaces.put("foaf","http://xmlns.com/foaf/0.1");
        dblpNamespaces.put("bibtex","http://data.bibbase.org/ontology/#");
    }
    
    private static final String ROOT = "/rdf:RDF/dblp:Person/";

    private static ConcurrentMap<String,XPathValueMapper> mediaOntMappings = new ConcurrentHashMap<String, XPathValueMapper>();
    static {
    	mediaOntMappings.put(FOAF.name.stringValue(), new XPathLiteralMapper(ROOT + "dblp:primaryFullPersonName", dblpNamespaces));
    	//mediaOntMappings.put(FOAF.name.stringValue(), new XPathLiteralMapper(ROOT + "*[dblp::primaryFullPersonName or dblp::otherFullPersonName]", dblpNamespaces));
    	mediaOntMappings.put(DCTERMS.modified.stringValue(), new DBLPDateMapper(ROOT+ "dblp:personLastModifiedDate", dblpNamespaces));
    	mediaOntMappings.put(DCTERMS.license.stringValue(), new DBLPURIMapper(ROOT + "dcterms:license/@rdf:resource", dblpNamespaces));
        mediaOntMappings.put(FOAF.publications.stringValue(), new DBLPURIMapper(ROOT + "dblp:authorOf/@rdf:resource", dblpNamespaces));
        
    }

    private static Logger log = LoggerFactory.getLogger(DBLPAuthorProvider.class);

    /**
     * Return the name of this data provider. To be used e.g. in the configuration and in log messages.
     *
     * @return
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Return the list of mime types accepted by this data provider.
     *
     * @return
     */
    @Override
    public String[] listMimeTypes() {
        return new String[] {
                "application/rdf+xml"
        };
    }

    /**
     * Build the URL to use to call the webservice in order to retrieve the data for the resource passed as argument.
     * In many cases, this will just return the URI of the resource (e.g. Linked Data), but there might be data providers
     * that use different means for accessing the data for a resource, e.g. SPARQL or a Cache.
     *
     *
     * @param resource
     * @param endpoint endpoint configuration for the data provider (optional)
     * @return
     */
    @Override
    public List<String> buildRequestUrl(String resource, Endpoint endpoint) {
    	String uri = "http://dblp.dagstuhl.de/pers/xr/";
    	Matcher m = Pattern.compile(LEGACY_PATTERN).matcher(resource);
    	if(m.find()) {
    		uri += m.group(2);
    	} else {
    		m = Pattern.compile(PATTERN).matcher(resource);
        	Preconditions.checkState(StringUtils.isNotBlank(resource) && m.find());
        	uri = resource;
    	}
        return Collections.singletonList(uri);
    }
    
    @Override
    public List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {
    	super.parseResponse(resource, requestUrl, triples, input, contentType);
    	log.debug("Request {0} succesful");
    	ValueFactory factory = ValueFactoryImpl.getInstance();
    	
    	ClientConfiguration conf = new ClientConfiguration();
        conf.addEndpoint(new DBLPResourceEndpoint());
        LDClient ldClient = new LDClient(conf);
        Set<Value> resources = triples.filter(factory.createURI(resource), FOAF.publications, null).objects();
        if(!resources.isEmpty()) {
	        Model resourceModel = null;
	        for(Value dblpResource: resources) {
	    		String resourceDoc = ((Resource)dblpResource).stringValue();
	    		ClientResponse response = ldClient.retrieveResource(resourceDoc);
	        	Model rsModel = response.getData();
	        	if(resourceModel == null) {
	        		resourceModel = rsModel;
	        	} else {
	        		resourceModel.addAll(rsModel);
	        	}
	    	}
	        triples.addAll(resourceModel);
        }
        if(!resource.matches(PATTERN)) {
    		triples.add(factory.createURI(resource), OWL.SAMEAS, factory.createURI(requestUrl));
    	}
    	return Collections.emptyList();

    }



    /**
     * Return a mapping table mapping from RDF properties to XPath Value Mappers. Each entry in the map is evaluated
     * in turn; in case the XPath expression yields a result, the property is added for the processed resource.
     *
     * @return
     * @param requestUrl
     */
    @Override
    protected Map<String, XPathValueMapper> getXPathMappings(String requestUrl) {
        return mediaOntMappings;
    }

    /**
     * Return a list of URIs that should be added as types for each processed resource.
     *
     * @return
     * @param resource
     */
    @Override
    protected List<String> getTypes(org.openrdf.model.URI resource) {
        return ImmutableList.of(FOAF.Person.stringValue());
    }
    
    /**
     * Provide namespace mappings for the XPath expressions from namespace prefix to namespace URI. May be overridden
     * by subclasses as appropriate, the default implementation returns an empty map.
     *
     * @return
     */
    @Override
    protected Map<String, String> getNamespaceMappings() {
        return dblpNamespaces;
    }

}
