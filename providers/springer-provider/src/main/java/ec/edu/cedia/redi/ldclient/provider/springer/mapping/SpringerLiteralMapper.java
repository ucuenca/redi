/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ec.edu.cedia.redi.ldclient.provider.springer.mapping;

import com.jayway.jsonpath.Predicate;
import ec.edu.cedia.redi.ldclient.provider.json.mappers.JsonPathLiteralMapper;
import java.util.Collections;
import java.util.List;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class SpringerLiteralMapper extends JsonPathLiteralMapper {

    public SpringerLiteralMapper(String path, String datatype, Predicate... filters) {
        super(path, datatype, filters);
    }

    public SpringerLiteralMapper(String path, Predicate... filters) {
        super(path, filters);
    }

    protected String cleanValue(String value) {
        return value.trim();
    }

    @Override
    public List<Value> map(String resourceUri, String selectedValue, ValueFactory factory) {
        Value value;
        selectedValue = cleanValue(selectedValue);
        if ("".equals(selectedValue)) {
            return Collections.emptyList();
        }
        if (datatype != null) {
            value = factory.createLiteral(selectedValue, factory.createURI(XMLSchema.NAMESPACE, datatype));
        } else {
            value = factory.createLiteral(selectedValue);
        }
        return Collections.singletonList(value);
    }
}
