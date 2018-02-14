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
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.util.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class ScholarTableDateMapper extends ScholarTableTextLiteralMapper {

    public ScholarTableDateMapper(String cssSelector, String cssSelectorKey, String cssSelectorValue, String key, String... keys) {
        super(cssSelector, cssSelectorKey, cssSelectorValue, key, keys);
    }

    @Override
    protected String cleanValue(String value) {
        return value.replace('/', '-').trim();
    }

    @Override
    public List<Value> map(String resourceUri, Element elem, ValueFactory factory) {
        String key = elem.getElementsByClass(classKey).first().text();
        if (keys.contains(key)) {
            for (Element e : Jsoup.parse(elem.html()).select(cssSelectorVal)) {
                String v = e.text();
                final String value = cleanValue(v);
                Date date = DateUtils.parseDate(value);
                if (StringUtils.isBlank(value) || date == null) {
                    return Collections.emptyList();
                }

                return Collections.singletonList((Value) factory.createLiteral(date));
            }
        }
        return Collections.emptyList();

    }
}
