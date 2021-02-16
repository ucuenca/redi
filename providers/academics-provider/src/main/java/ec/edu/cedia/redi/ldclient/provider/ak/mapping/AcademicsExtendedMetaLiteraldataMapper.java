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
package ec.edu.cedia.redi.ldclient.provider.ak.mapping;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.Predicate;
import com.jayway.jsonpath.ReadContext;
import ec.edu.cedia.redi.ldclient.provider.json.mappers.JsonPathValueMapper;
import java.util.Collections;
import java.util.List;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * Maps extended meta data from publications to its value.
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class AcademicsExtendedMetaLiteraldataMapper extends JsonPathValueMapper {

    protected final String extendedPath;
    private String datatype;

    public AcademicsExtendedMetaLiteraldataMapper(String path, String extendedPath, Predicate... filters) {
        super(path, filters);
        this.extendedPath = extendedPath;
    }

    public AcademicsExtendedMetaLiteraldataMapper(String path, String extendedPath, String datatype, Predicate... filters) {
        super(path, filters);
        this.extendedPath = extendedPath;
        this.datatype = datatype;
    }

    protected String cleanValue(ReadContext ctx) {
        return ctx.read(extendedPath, String.class);
    }

    @Override
    public List<Value> map(String resourceUri, String selectedValue, ValueFactory factory) {
        Value value;
        Configuration conf = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
        ReadContext ctx = null;
        ctx = JsonPath.parse(selectedValue, conf);
        String valueStr = cleanValue(ctx);
        if (datatype != null && valueStr != null) {
            value = factory.createLiteral(valueStr, factory.createURI(XMLSchema.NAMESPACE + datatype));
        } else if (valueStr != null) {
            value = factory.createLiteral(valueStr);
        } else {
            return Collections.emptyList();
        }
        return Collections.singletonList(value);
    }
}
