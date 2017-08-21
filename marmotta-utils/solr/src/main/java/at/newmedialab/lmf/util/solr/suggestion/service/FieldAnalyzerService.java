/**
 * Copyright (C) 2013 Salzburg Research.
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
package at.newmedialab.lmf.util.solr.suggestion.service;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.solr.core.SolrCore;

import java.io.IOException;
import java.io.StringReader;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class FieldAnalyzerService {

    /**
     * analyzes string like the default field
     * @param df the name of the default field
     * @param s the string to analyze
     * @return
     */
    public static String analyzeString(SolrCore core, String df, String s) {
        try {
            TokenStream ts = core.getSchema().getFieldType(df).getQueryAnalyzer().tokenStream(df, new StringReader(s));
            StringBuffer b = new StringBuffer();
            ts.reset();
            while(ts.incrementToken()) {
                b.append(" ");
                CharTermAttribute attr = ts.getAttribute(CharTermAttribute.class);
                b.append(attr);
            }
            return b.toString().trim();
        } catch (IOException e) {
            e.printStackTrace();
            return s;
        }
    }

}
