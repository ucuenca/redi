/*
 * Copyright 2018 Xavier Sumba <xavier.sumba93@ucuenca.ec>.
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
package ec.edu.cedia.redi.ldclient.provider.scholar.mapping;

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class ScholarIndexesTextLiteralMapper extends ScholarTableTextLiteralMapper {

    public ScholarIndexesTextLiteralMapper(String cssSelector, String cssSelectorKey, String cssSelectorValue, String key, String... keys) {
        super(cssSelector, cssSelectorKey, cssSelectorValue, key, keys);
    }

    @Override
    public List<Value> map(String resourceUri, Element elem, ValueFactory factory) {
        String key = elem.getElementsByClass(classKey).first().text();
        if (keys.contains(key)) {
            Elements elements = Jsoup.parse(elem.html(), "", Parser.xmlParser()).select(cssSelectorVal);
            if (elements.isEmpty()) {
                return Collections.emptyList();
            }
            String v = elements.first().text();
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
        return Collections.emptyList();

    }
}
