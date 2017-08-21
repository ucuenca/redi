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

import at.newmedialab.lmf.util.solr.suggestion.params.SuggestionResultParams;

import java.util.*;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class SuggesionResultSingle implements SuggestionResult {

    private int count = 0;
    private HashMap<String,Set<Facet>> fields = new HashMap<String, Set<Facet>>();

    private static final Comparator<Facet> COUNT_SORTER = new Comparator<Facet>() {
        @Override
        public int compare(Facet facet, Facet facet2) {
            return ((Integer)facet.count).compareTo(facet2.count);
        }
    };

    public Object write() {
        Map<String,Object> suggestions = new HashMap<String, Object>();

        HashMap<String,Object> suggestion_facets = new HashMap<String, Object>();

        for(String field : fields.keySet()) {
            HashMap<String,Integer> facets = new HashMap<String,Integer>();

            //sort fields on count
            //Collections.sort(fields.get(field),COUNT_SORTER); TODO result must be an array

            for(Facet facet : fields.get(field)) {
                facets.put(facet.value,facet.count);
                count++;
            }

            suggestion_facets.put(field,facets);
        }
        suggestions.put(SuggestionResultParams.SUGGESTION_COUNT, count);
        if(count>0) suggestions.put(SuggestionResultParams.SUGGESTION_FACETS, suggestion_facets);

        return suggestions;
    }

    public void addFacet(String field, String value, int count) {
        if(fields.get(field) == null) {
            fields.put(field,new HashSet<Facet>());
        }

        fields.get(field).add(new Facet(value,count));
    }

    class Facet implements Comparable<Facet>{

        String value;
        int count;

        Facet(String value, int count) {
            this.value = value;
            this.count = count;
        }

        @Override
        public int compareTo(Facet facet) {
            return value.compareTo(facet.value);
        }
    }

}
