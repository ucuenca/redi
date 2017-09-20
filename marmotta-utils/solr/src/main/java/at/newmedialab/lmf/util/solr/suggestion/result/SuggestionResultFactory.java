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
package at.newmedialab.lmf.util.solr.suggestion.result;

import at.newmedialab.lmf.util.solr.suggestion.service.FieldAnalyzerService;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.response.SolrQueryResponse;
import java.util.Iterator;
import java.util.Map;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class SuggestionResultFactory {

    public static SuggestionResult createResult(SolrCore core, SolrQueryResponse rsp, String[] fields, String query, String df, boolean multivalue) {
        if(multivalue) {
            return filterMultiValue(core, rsp,fields,query,df);
        } else {
            return filterSingleValue(core, rsp, fields, query, df);
        }
    }

    private static SuggestionResult filterMultiValue(SolrCore core, SolrQueryResponse rsp, String[] fields, String query, String df) {
        SuggestionResultMulti result = new SuggestionResultMulti();
        return result;
    }

    //get facets and filter them
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static SuggestionResult filterSingleValue(SolrCore core, SolrQueryResponse rsp, String[] fields, String query, String df) {
        SuggesionResultSingle result = new SuggesionResultSingle();
        SimpleOrderedMap facets = (SimpleOrderedMap)((SimpleOrderedMap)rsp.getValues().get("facet_counts")).get("facet_fields");
        for(String field : fields) {
            Iterator<Map.Entry> i = ((NamedList)facets.get(field)).iterator();
            while(i.hasNext()) {
                Map.Entry<String, NamedList<Object>> entry = i.next();
                //analyze result for mapping
                String s = " " + FieldAnalyzerService.analyzeString(core, df, entry.getKey());
                //check if facet should be added
                boolean add = true;
                for(String qp : query.split(" ")) {
                    if(!s.toLowerCase().contains(" "+qp.toLowerCase())) add = false;
                }

                if(add) {
                    Object o = entry.getValue();
                    result.addFacet(field,entry.getKey(),(Integer)o);
                }
            }
        }
        return result;
    }

}
