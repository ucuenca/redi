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
import org.apache.marmotta.ldclient.provider.html.mapping.CssUriAttrWhitelistQueryParamsMapper;
import org.jsoup.nodes.Element;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class ScholarImageUriAttrMapper extends CssUriAttrWhitelistQueryParamsMapper {

    public ScholarImageUriAttrMapper(String cssSelector, String attr, String... queryParamWhitelist) {
        super(cssSelector, attr, queryParamWhitelist);
    }

    @Override
    public List<Value> map(String resourceUri, Element element, ValueFactory factory) {
        final String uri = rewriteUrl(element.absUrl(attr));
        if (uri.contains("avatar_scholar")) {
            return Collections.emptyList();
        }
        try {
            return Collections.singletonList((Value) factory.createURI(uri));
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

}
