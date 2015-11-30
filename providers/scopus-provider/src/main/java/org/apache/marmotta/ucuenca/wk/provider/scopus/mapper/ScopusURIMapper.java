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
package org.apache.marmotta.ucuenca.wk.provider.scopus.mapper;

import org.apache.marmotta.ldclient.provider.xml.mapping.XPathValueMapper;
/*import org.apache.marmotta.ucuenca.wk.provider.dblp.DBLPAuthorProvider;
import org.apache.marmotta.ucuenca.wk.provider.dblp.DBLPResourceProvider;*/
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Common URI Mapper
 * <p/>
 * Author: Jose Luis Cullcay
 */
public class ScopusURIMapper extends XPathValueMapper {

	private static String nsEntity;
	
	public ScopusURIMapper(String namespace, String xpath) {
    	super(xpath);
    	nsEntity = namespace;
    }
	
	public ScopusURIMapper(String xpath) {
    	super(xpath);
    }

    public ScopusURIMapper(String xpath, Map<String, String> namespaces) {
        super(xpath, namespaces);
    }

    @Override
    public List<Value> map(String resourceUri, String selectedValue, ValueFactory factory) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Take the selected value, process it according to the mapping definition, and create Sesame Values using the
     * factory passed as argument.
     *
     *
     * @param resourceUri
     * @param selectedValue
     * @param factory
     * @return
     */
    /*@Override
    public List<Value> map(String resourceUri, String selectedValue, ValueFactory factory) {
    	Value uri = null;
    	if( selectedValue.matches(DBLPAuthorProvider.LEGACY_PATTERN) || 
    			selectedValue.matches(DBLPAuthorProvider.PATTERN)) {
    		uri = (Value)factory.createURI(selectedValue.replaceAll("/pers/", "/pers/xr/"));
    	} else if( selectedValue.matches(DBLPResourceProvider.LEGACY_PATTERN) ) {
    		uri = (Value)factory.createURI(selectedValue.replaceAll("http://dblp.uni-trier.de/rec/", "http://dblp.org/rec/rdf/"));
    	} else if( selectedValue.matches(DBLPResourceProvider.PATTERN) ) {
    		uri = (Value)factory.createURI(selectedValue.replaceAll("/rec/", "/rec/rdf/"));
    	} else if(nsEntity != null) {
    		uri = (Value)factory.createURI(nsEntity + selectedValue);
    	} else {
    		uri = (Value)factory.createURI(selectedValue);
    	}
    	return Collections.singletonList(uri);
    }*/
}
