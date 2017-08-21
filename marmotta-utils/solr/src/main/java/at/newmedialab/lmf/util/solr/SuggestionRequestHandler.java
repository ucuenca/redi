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
package at.newmedialab.lmf.util.solr;

import at.newmedialab.lmf.util.solr.suggestion.params.SuggestionRequestParams;
import at.newmedialab.lmf.util.solr.suggestion.service.SuggestionService;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.plugin.SolrCoreAware;

import java.util.*;

/**
 * https://svn.apache.org/repos/asf/lucene/solr/branches/branch-1.4/src/java/org/apache/solr/handler/
 *
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class SuggestionRequestHandler extends SearchHandler implements SolrCoreAware {

    private SuggestionService suggestionService;

    private static boolean SUGGESTION = true;
    private static String DF = null;
    private static String[] FIELDS = null;
    private static String[] FQS = null;
    private static boolean MULTIVALUE = false;
    private static int LIMIT = Integer.MAX_VALUE;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void inform(SolrCore core) {
        super.inform(core);
        suggestionService = new SuggestionService(core,this.getInitArgs());

        //set default args
        NamedList args = (NamedList)this.getInitArgs().get("defaults");

        SUGGESTION = args.get(SuggestionRequestParams.SUGGESTION) != null ?
                Boolean.parseBoolean((String)args.get(SuggestionRequestParams.SUGGESTION)) : SUGGESTION;

        MULTIVALUE = args.get(SuggestionRequestParams.SUGGESTION_MULTIVALUE) != null ?
                Boolean.parseBoolean((String)args.get(SuggestionRequestParams.SUGGESTION_MULTIVALUE)) : MULTIVALUE;

        LIMIT = args.get(SuggestionRequestParams.SUGGESTION_LIMIT) != null ?
                Integer.parseInt((String)args.get(SuggestionRequestParams.SUGGESTION_LIMIT)) : LIMIT;

        DF = args.get(SuggestionRequestParams.SUGGESTION_DF) != null ?
                (String) args.get(SuggestionRequestParams.SUGGESTION_DF) : DF;

        List<String> fields = args.getAll(SuggestionRequestParams.SUGGESTION_FIELD) != null ?
                args.getAll(SuggestionRequestParams.SUGGESTION_FIELD) : Collections.emptyList();
        if(!fields.isEmpty()) {
            FIELDS = fields.toArray(new String[fields.size()]);
        }

        List<String> fqs = args.getAll(CommonParams.FQ) != null ?
                args.getAll(CommonParams.FQ) : Collections.emptyList();
        if(!fqs.isEmpty()) {
            FQS = fqs.toArray(new String[fields.size()]);
        }

    }

    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {

        SolrParams params = req.getParams();

        if(params.getBool(SuggestionRequestParams.SUGGESTION,SUGGESTION)) {

            String q = params.get(CommonParams.Q);
            if(q == null) {
                rsp.add("error",error(400, "SuggestionRequest needs to have a 'q' parameter"));
                return;
            }

            String[] fields = params.getParams(SuggestionRequestParams.SUGGESTION_FIELD) != null ? params.getParams(SuggestionRequestParams.SUGGESTION_FIELD) : FIELDS;
            if(fields == null) {
                rsp.add("error",error(400,"SuggestionRequest needs to have at least one 'suggestion.field' parameter"));
                return;
            }

            String df = params.get(SuggestionRequestParams.SUGGESTION_DF,DF);
            if(df == null) {
                rsp.add("error",error(400,"SuggestionRequest needs to have a 'df' parameter"));
                return;
            }

            int limit = params.getInt(SuggestionRequestParams.SUGGESTION_LIMIT,LIMIT);
            if(limit < 1) {
                rsp.add("error",error(400,"SuggestionRequest needs to have a 'suggestion.limit' greater than 0"));
                return;
            }

            String[] fqs = params.getParams(CommonParams.FQ) != null ? params.getParams(CommonParams.FQ) : FQS;

            Boolean multivalue = params.getBool(SuggestionRequestParams.SUGGESTION_MULTIVALUE,MULTIVALUE);

            //TODO replace
            if(multivalue) {
                rsp.add("error",error(500,"Multivalue suggestions are not yet supported!"));
                return;
            }

            suggestionService.run(rsp,q,df,fields,fqs,limit,multivalue);

        } else {
            super.handleRequestBody(req,rsp);
        }
    }

    private HashMap<String,Object> error(int code,String msg) {
        HashMap<String,Object> error = new HashMap<String,Object>();
        error.put("msg",msg);
        error.put("code",code);
        return error;
    }


    @Override
    public String getDescription() {
        return "This handler creates suggestions for a faceted search";
    }

    @Override
    public String getSource() {
        return "no source";
    }

    @Override
    public String getVersion() {
        return "0.0.1";
    }
}
