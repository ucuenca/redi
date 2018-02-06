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
public class AcademicsExtendedAbstractMapper extends AcademicsExtendedMetaLiteraldataMapper {

    public AcademicsExtendedAbstractMapper(String path, String extendedPath, Predicate... filters) {
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
        /**
         * There are some problems with invisible control characters and unused
         * code points. \p{C} should detect those unexpected characters and
         * replace them with an empty value.
         * <p>
         * Read this for more info about the regex patter.
         * https://stackoverflow.com/questions/44034232/undocumented-java-regex-character-class-pc
         */
        return Joiner.on(" ").skipNulls().join(plainAbstract).replaceAll("\\p{C}", "");
    }

}
