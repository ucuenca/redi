/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.impl;

import com.google.common.collect.Lists;
import java.util.ArrayList;
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
    public Double isGivenName(String gn) throws MarmottaException {
        String q = "PREFIX inst: <http://www.ontotext.com/connectors/lucene/instance#>\n"
                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "PREFIX : <http://www.ontotext.com/connectors/lucene#>\n"
                + "select ?t where { \n"
                + "    bind('xxyyzz " + gn + "' as ?n) .\n"
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
    @SuppressWarnings("PMD")
    public Map<String, String> separateName(String fullname) throws MarmottaException {
        List<String> tokensList = Lists.newArrayList(fullname.replaceAll(",|\\.|-|;|/", " ").replaceAll("\\s+", " ").trim().split(" "));
        List<Double> probabilitiesList = Lists.newArrayList();
        ConcurrentHashMap<String, String> dividedNameMap = new ConcurrentHashMap<>();
        double countNaN = 0;
        for (String token : tokensList) {
            Double givenName = isGivenName(token);
            if (givenName.isNaN()) {
                givenName = 0.5;
                countNaN++;
            }
            probabilitiesList.add(givenName);
        }
        if (countNaN / tokensList.size() > 0.30) {
            //Prone to errors, it is better to ignore
            return dividedNameMap;
        }
        double bestScore = Double.MAX_VALUE;
        double bestCutPosition = 0;
        boolean startsWithFirstName = false;
        for (double i = 0.5; i < probabilitiesList.size() - 1; i++) {
            double countSumLeft = 0;
            double countSumRight = 0;
            double cumulativeSumLeft = 0;
            double cumulativeSumRight = 0;
            double stringLengthLeft = 0;
            double stringLengthRight = 0;

            for (int j = 0; j < probabilitiesList.size(); j++) {
                if (j < i) {
                    countSumLeft++;
                    cumulativeSumLeft += probabilitiesList.get(j);
                    stringLengthLeft += tokensList.get(j).length();
                }
                if (j > i) {
                    countSumRight++;
                    cumulativeSumRight += probabilitiesList.get(j);
                    stringLengthRight += tokensList.get(j).length();
                }
            }

            if (countSumLeft > 0 && countSumRight > 0) {
                double averageProbabilityLeft = cumulativeSumLeft / countSumLeft;
                double averageProbabilityRight = cumulativeSumRight / countSumRight;
                double complementAverageProbabilityLeft = 1 - averageProbabilityLeft;
                double complementAverageProbabilityRight = 1 - averageProbabilityRight;
                double averageStringLengthLeft = stringLengthLeft / (stringLengthLeft + stringLengthRight);
                double averageStringLengthRight = stringLengthRight / (stringLengthLeft + stringLengthRight);

                double partialBestCut = i;
                double partitalBestScore = 0;
                boolean partialStartsWithFirstName = averageProbabilityLeft > averageProbabilityRight;
                if (partialStartsWithFirstName) {
                    partitalBestScore = Math.abs(averageProbabilityLeft - complementAverageProbabilityRight) + Math.abs(averageStringLengthLeft - averageStringLengthRight);

                } else {
                    partitalBestScore = Math.abs(complementAverageProbabilityLeft - averageProbabilityRight) + Math.abs(averageStringLengthLeft - averageStringLengthRight);
                }
                if (partitalBestScore < bestScore) {
                    bestCutPosition = partialBestCut;
                    bestScore = partitalBestScore;
                    startsWithFirstName = partialStartsWithFirstName;
                }
            }
        }
        String dividedFirstName = "";
        String dividedLastName = "";
        for (int i = 0; i < tokensList.size(); i++) {
            if (i < bestCutPosition) {
                dividedFirstName += startsWithFirstName ? tokensList.get(i) + " " : "";
                dividedLastName += !startsWithFirstName ? tokensList.get(i) + " " : "";
            } else {
                dividedFirstName += !startsWithFirstName ? tokensList.get(i) + " " : "";
                dividedLastName += startsWithFirstName ? tokensList.get(i) + " " : "";
            }
        }
        dividedNameMap.put("firstName", dividedFirstName.trim());
        dividedNameMap.put("lastName", dividedLastName.trim());
        return dividedNameMap;
    }

}
