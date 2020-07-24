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

}
