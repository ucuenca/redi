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

import at.newmedialab.lmf.util.solr.suggestion.params.SuggestionResultParams;
import at.newmedialab.lmf.util.solr.suggestion.result.SuggesionResultSingle;
import at.newmedialab.lmf.util.solr.suggestion.result.SuggestionResult;
import at.newmedialab.lmf.util.solr.suggestion.result.SuggestionResultFactory;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.ResultContext;
import org.apache.solr.response.SolrQueryResponse;
import java.util.*;

/**
 * This suggestion service queries a given core for facet suggestions based on an input string
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SuggestionService {

    private String internalFacetLimit = "30";

    private boolean spellcheck_enabled = false;

    private SolrCore solrCore;

    private SearchHandler searchHandler;

    public SuggestionService(SolrCore solrCore, NamedList args) {

        NamedList l = new NamedList();

        //set spellcheck component if there is one
        if(((ArrayList)args.get("first-components")).contains("spellcheck")) {
            List component = new ArrayList<String>();
            component.add("spellcheck");
            l.add("first-components",component);
            spellcheck_enabled = true;
        }

        this.solrCore = solrCore;
        this.searchHandler = new SearchHandler();
        this.searchHandler.init(l);
        this.searchHandler.inform(solrCore);
    }

    public void run(SolrQueryResponse rsp, String query, String df, String[] fields, String[] fqs, int limit, boolean multivalue) {

        //analyze query in advance
        query = FieldAnalyzerService.analyzeString(solrCore, df, query);

        SuggestionResult result = null;

        Object spellcheck_result = null;

        SolrQueryResponse response = query(query,df,fields,fqs);

        //of no results, try spellcheck (if defined and if spellchecked query differs from original)
        if(((ResultContext)response.getValues().get("response")).docs.size() > 0) {
            result = SuggestionResultFactory.createResult(solrCore, response, fields, query, df, multivalue);
        } else if(spellcheck_enabled) {
            String spellchecked_query = getSpellcheckedQuery(query,response);
            spellcheck_result = response.getValues().get("spellcheck");

            //query with spellchecked query
            if(spellchecked_query != null) {
                response = query(spellchecked_query,df,fields,fqs);
                if(((ResultContext)response.getValues().get("response")).docs.size() > 0) {
                    result = SuggestionResultFactory.createResult(solrCore, response, fields, spellchecked_query, df,multivalue);
                }
            }
        }
        
        //add result of spellcheck component
        if(spellcheck_result != null) {
            //TODO remove * on last position of collation
            rsp.add("spellcheck",spellcheck_result);
        }

        //create an empty result
        if(result == null) result = new SuggesionResultSingle();

        rsp.add(SuggestionResultParams.SUGGESTIONS, result.write());
    }

    private String getSpellcheckedQuery(String query, SolrQueryResponse rsp) {
        try {
            if(((NamedList)((NamedList)rsp.getValues().get("spellcheck")).get("suggestions")).size() > 0) {
                String s = (String)((NamedList)((NamedList)((NamedList)rsp.getValues().get("spellcheck")).get("suggestions"))).get("collation");
                return s.substring(0,s.length()-1);
            }
        } catch(NullPointerException e) {
            //TODO is this a nice solution?
        } return null;
    }

    private SolrQueryResponse query(String query, String df, String[] fields, String[] fqs) {

        SolrQueryResponse rsp = new SolrQueryResponse();

        //append *
        if(!query.endsWith("*")) {
            query = query.trim() + "*";
        }

        //Prepare query
        ModifiableSolrParams params = new ModifiableSolrParams();
        SolrQueryRequest req = new LocalSolrQueryRequest( solrCore, params );
        params.add(CommonParams.Q,query.toLowerCase());
        params.add(CommonParams.DF,df);
        params.add("q.op","AND");
        params.add(FacetParams.FACET,"true");
        params.add(FacetParams.FACET_LIMIT, internalFacetLimit);
        params.add(FacetParams.FACET_MINCOUNT, "1");
        for(String field : fields) {
            params.add(FacetParams.FACET_FIELD,field);
        }
        if(fqs != null) {
            for(String fq : fqs) {
                params.add(CommonParams.FQ,fq);
            }
        }

        if(spellcheck_enabled) {
            params.add("spellcheck","true");
            params.add("spellcheck.collate","true");
        }

        try {
            //execute query and return
            searchHandler.handleRequestBody(req,rsp);
            return rsp;
        } catch (SolrException se) {
            throw se;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,"internal server error");
        } finally {
            req.close();
        }
    }

}
