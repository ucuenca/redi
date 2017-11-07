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

import com.google.common.base.Joiner;
import com.jayway.jsonpath.Predicate;
import com.jayway.jsonpath.ReadContext;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rebuilds a publication's abstract.
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class ScopusExtendedAbstractMapper extends ScopusExtendedMetaLiteraldataMapper {

    public ScopusExtendedAbstractMapper(String path, String extendedPath, Predicate... filters) {
        super(path, extendedPath, filters);
    }

    @Override
    protected String cleanValue(ReadContext ctx) {
        LinkedHashMap<String, Object> result = ctx.read(extendedPath);
        if (result == null) {
            return null;
        }
        int lenght = (int) result.get("IndexLength");
        Map<String, List> values = (HashMap<String, List>) result.get("InvertedIndex");
        return buildAbstract(lenght, values);
    }

    private String buildAbstract(int lenght, Map<String, List> values) {
        String[] plainAbstract = new String[lenght];
        for (String key : values.keySet()) {
            List<Integer> indexes = values.get(key);
            for (Integer index : indexes) {
                plainAbstract[index] = key;
            }
        }
        return Joiner.on(" ").skipNulls().join(plainAbstract);
    }

}
