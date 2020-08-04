/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.impl;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Person;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.DisambiguationUtilsService;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;

/**
 *
 * @author cedia
 */
public class DisambiguationUtilsServiceImpl implements DisambiguationUtilsService {

    @Inject
    private ExternalSPARQLService sparqlService;
    @Inject
    private ConstantService con;

    @Override
    public List<String> lookForOrganizations(List<String> aff) throws MarmottaException {
        Person mock = new Person();
        mock.Affiliations = new ArrayList<>();
        mock.Affiliations.addAll(aff);
        List<String> uris = Lists.newArrayList();
        Map<String, Set<String>> organizations = getOrganizations();
        for (Map.Entry<String, Set<String>> org : organizations.entrySet()) {
            Person foo = new Person();
            foo.Affiliations = new ArrayList<>();
            foo.Affiliations.addAll(org.getValue());
            if (mock.checkAffiliations(foo)) {
                uris.add(org.getKey());
            }
        }
        return uris;
    }

    private Map<String, Set<String>> getOrganizations() throws MarmottaException {
        ConcurrentHashMap<String, Set<String>> mp = new ConcurrentHashMap<>();
        String qry = "select * {\n"
                + "    graph <" + con.getOrganizationsGraph() + "> {\n"
                + "        ?o <http://ucuenca.edu.ec/ontology#name>|<http://ucuenca.edu.ec/ontology#alias>|<http://ucuenca.edu.ec/ontology#fullName> ?n .\n"
                + "    }\n"
                + "}";
        List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, qry);
        for (Map<String, Value> m : query) {
            String uri = m.get("o").stringValue();
            String[] name = m.get("n").stringValue().split(";");
            if (!mp.containsKey(uri)) {
                mp.put(uri, new HashSet<String>());
            }
            mp.get(uri).addAll(Lists.newArrayList(name));
        }
        return mp;
    }

    @Override
    public double isGivenName(String gn) throws MarmottaException {
        String q = "PREFIX inst: <http://www.ontotext.com/connectors/lucene/instance#>\n"
                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "PREFIX : <http://www.ontotext.com/connectors/lucene#>\n"
                + "select ?t where { \n"
                + "    bind('" + gn + "' as ?n) .\n"
                + "    bind (replace(replace(lcase(?n),':|,|-|\\\\.|/',' '),'\\\\s+',' ') as ?nn_) .\n"
                + "    bind (replace(concat(replace(replace(?nn_,' $',''),'^ ',''), ' '),' ',' ') as ?nn) .\n"
                + "    bind (concat ('givenName:(',?nn, ')') as ?gq).\n"
                + "    bind (concat ('familyName:(',?nn, ')') as ?fq).\n"
                + "    ?s1 a inst:namesidx  ;\n"
                + "    	:query  ?gq;\n"
                + "        :totalHits ?tg .             \n"
                + "    ?s2 a inst:namesidx  ;\n"
                + "    	:query  ?fq;\n"
                + "        :totalHits ?tf .         \n"
                + "    bind ( ?tg/(?tf+?tg) as ?t ) . \n"
                + "}";

        List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, q);
        double r = Double.NaN;
        for (Map<String, Value> mp : query) {
            Value get = mp.get("t");
            if (get != null) {
                r = Double.parseDouble(get.stringValue());
            }
        }
        return r;
    }
    
    @Override
    public HashMap<String,String> separateName (String fullname) throws MarmottaException {
 
       HashMap hp = new HashMap ();
       String fnames = "";
       String lnames = "";
       for (String name :fullname.split("\\s+")){
         if (isGivenName(name) >= 0.8) {
          fnames = fnames+" "+name;
         }else {
          lnames = lnames +" "+name; 
         }
        }
       
       hp.put("firstName", fnames);
       hp.put("lastName", lnames);
       return hp;
    }

}
