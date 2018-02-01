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
package ec.edu.cedia.redi.ldclient.provider.scholar.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.ldclient.provider.html.mapping.CssSelectorMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class ScholarTableTextLiteralMapper extends CssSelectorMapper {

    protected String datatype;
    protected Locale language;
    protected final String classKey;
    protected final String cssSelectorVal;
    protected final List<String> keys = new ArrayList<>();

    private ScholarTableTextLiteralMapper(String cssSelector, String classKey, String cssSelectorValue, Locale lang, String key, String... keys) {
        this(cssSelector, classKey, cssSelectorValue, key, keys);
        language = lang;
    }

    private ScholarTableTextLiteralMapper(String cssSelector, String cssSelectorKey, String cssSelectorValue, String datatype, String key, String... keys) {
        this(cssSelector, cssSelectorKey, cssSelectorValue, key, keys);
        this.datatype = datatype;
    }

    public ScholarTableTextLiteralMapper(String cssSelector, String cssSelectorKey, String cssSelectorValue, String key, String... keys) {
        super(cssSelector);
        this.classKey = cssSelectorKey;
        this.cssSelectorVal = cssSelectorValue;
        this.keys.addAll(Arrays.asList(keys));
        this.keys.add(key);
        datatype = null;
        language = null;
    }

    protected String cleanValue(String value) {
        return value.trim();
    }

    @Override
    public List<Value> map(String resourceUri, Element elem, ValueFactory factory) {
        String key = elem.getElementsByClass(classKey).first().text();
        if (keys.contains(key)) {
            for (Element e : Jsoup.parse(elem.html()).select(cssSelectorVal)) {
                String v = e.text();
                final String value = cleanValue(v);
                if (StringUtils.isBlank(value)) {
                    return Collections.emptyList();
                }

                if (language != null) {
                    return Collections.singletonList((Value) factory.createLiteral(value, language.toString()));
                }
                if (datatype != null) {
                    return Collections.singletonList((Value) factory.createLiteral(value, factory.createURI(Namespaces.NS_XSD + datatype)));
                } else {
                    return Collections.singletonList((Value) factory.createLiteral(value));
                }
            }
        }
        return Collections.emptyList();

    }

}
