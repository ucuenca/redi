/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ec.edu.cedia.redi.ldclient.provider.springer.utils;

import static ec.edu.cedia.redi.ldclient.provider.springer.SpringerAuthorProvider.PATTERN;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public final class SpringerUtility {

    private SpringerUtility() {
    }

    public static String buildNameFromRequest(String resource) throws DataRetrievalException {
        final Matcher matcher = Pattern.compile(PATTERN).matcher(resource);

        try {
            if (matcher.find()) {
                String nameQuery = URLDecoder.decode(matcher.group(1), "UTF-8");
                return buildNameFromQuery(nameQuery);
            }
        } catch (UnsupportedEncodingException ex) {
            throw new DataRetrievalException(ex);
        }
        throw new DataRetrievalException("Cannot rebuild name for resource " + resource);
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public static String buildNameFromQuery(String query) throws DataRetrievalException {

        final Matcher m = Pattern.compile("name:(\\w*[^\\S]*)").matcher(query);

        String name = "";
        while (m.find()) {
            name += m.group(1) + " ";
        }
        name = name.replaceAll("\\s+", " ").trim();
        if (!"".equals(name)) {
            return WordUtils.capitalizeFully(name);
        }
        throw new DataRetrievalException("Cannot rebuild name for query " + query);
    }

    public static URI generateURI(String namespace, String name) {
        ValueFactory vf = ValueFactoryImpl.getInstance();
        return vf.createURI(namespace, generateHash(name));
    }

    public static String generateHash(String str) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
        }
        byte[] hash = md.digest(str.getBytes());
        return DatatypeConverter.printHexBinary(hash).toLowerCase();
    }
}
