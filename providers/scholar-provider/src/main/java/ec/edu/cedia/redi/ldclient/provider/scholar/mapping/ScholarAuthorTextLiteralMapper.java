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
package ec.edu.cedia.redi.ldclient.provider.scholar.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class ScholarAuthorTextLiteralMapper extends ScholarTableTextLiteralMapper {

    public ScholarAuthorTextLiteralMapper(String cssSelector, String classKey, String cssSelectorVal, String key, String... keys) {
        super(cssSelector, classKey, cssSelectorVal, key, keys);
    }

    @Override
    public List<Value> map(String resourceUri, Element elem, ValueFactory factory) {
        String key = elem.getElementsByClass(classKey).first().text();
        if (keys.contains(key)) {
            List<Value> authornames = new ArrayList<>();
            for (Element element : Jsoup.parse(elem.html()).select(cssSelectorVal)) {
                String v = element.text();
                for (String authorname : v.split(",")) {
                    final String value = cleanValue(authorname);
                    if (StringUtils.isBlank(value)) {
                        continue;
                    }
                    Value author;
                    if (language != null) {
                        author = (Value) factory.createLiteral(value, language.toString());
                    }
                    if (datatype != null) {
                        author = (Value) factory.createLiteral(value, factory.createURI(Namespaces.NS_XSD + datatype));
                    } else {
                        author = (Value) factory.createLiteral(value);
                    }
                    authornames.add(author);
                }
            }
            return authornames;
        }
        return Collections.emptyList();
    }

}
