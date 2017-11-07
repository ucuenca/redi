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

import com.jayway.jsonpath.Predicate;
import ec.edu.cedia.redi.ldclient.provider.json.mappers.JsonPathValueMapper;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.marmotta.commons.util.DateUtils;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 * Maps the given value to a date datatype.
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class ScopusDateMapper extends JsonPathValueMapper {

    public ScopusDateMapper(String path, Predicate... filters) {
        super(path, filters);
    }

    @Override
    public List<Value> map(String resourceUri, String selectedValue, ValueFactory factory) {
        Date date = DateUtils.parseDate(selectedValue);
        return Collections.singletonList((Value) factory.createLiteral(date));
    }

}
