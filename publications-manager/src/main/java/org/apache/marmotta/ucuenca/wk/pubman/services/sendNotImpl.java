/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.slf4j.Logger;
import org.apache.marmotta.ucuenca.wk.pubman.api.SendNotificationsMarmotta;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;

/**
 *
 * @author cedia
 */
@ApplicationScoped
public class sendNotImpl implements SendNotificationsMarmotta {

  @Inject
  private Logger log;
  @Inject
  private ExternalSPARQLService ess;

  @Override
  public void init() {
    String q = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "PREFIX redi: <https://redi.cedia.edu.ec/ont#>\n"
            + "select ?a ?p_t_c (sample(?a_n) as ?an) (sample(?p_t) as ?pt) (sample(?p) as ?pu) (group_concat( distinct ?a_e ; separator=';;;') as ?ae) {\n"
            + "    graph <https://redi.cedia.edu.ec/context/notifications> {\n"
            + "        <https://redi.cedia.edu.ec/resource/type/publication> <https://redi.cedia.edu.ec/ont#newBucket> ?p .\n"
            + "    }\n"
            + "    graph <https://redi.cedia.edu.ec/context/buckets> {\n"
            + "        ?a redi:type 'author' .\n"
            + "        ?a redi:element ?oa .\n"
            + "    }\n"
            + "    graph <https://redi.cedia.edu.ec/context/authors> {\n"
            + "        ?oa foaf:name ?a_n .\n"
            + "    }\n"
            + "    graph <https://redi.cedia.edu.ec/context/redi> {\n"
            + "        ?a foaf:publications ?p .\n"
            + "        ?p dct:title ?p_t .\n"
            + "        bind (replace (replace (lcase(str(?p_t)), 'ü|ñ|á|é|í|ó|ú|a|e|i|o|u|,|;|:|-|\\\\(|\\\\)|\\\\||\\\\.' ,' '), ' ' ,'') as ?p_t_c) .\n"
            + "        optional {\n"
            + "            ?a <http://www.w3.org/2006/vcard/ns#hasEmail> ?a_e .\n"
            + "        }\n"
            + "    }\n"
            + "} group by ?a ?p_t_c ";
    try {
      List<Map<String, Value>> query = ess.getSparqlService().query(QueryLanguage.SPARQL, q);

      Map<String, Object[]> mp = Maps.newHashMap();
      for (Map<String, Value> mm : query) {
        String stringValue = mm.get("a").stringValue();
        if (!mp.containsKey(stringValue)) {
          mp.put(stringValue, new Object[]{(String) null, new String[]{}, new ArrayList<String>(), new ArrayList<String>()});
        }
        mp.get(stringValue)[0] = mm.get("an") .stringValue() ;//0: Name
        mp.get(stringValue)[1] = mm.get("ae") != null ? mm.get("ae").stringValue().toLowerCase().split(";;;") : mp.get(stringValue)[1];//1: Email
        ((List<String>) mp.get(stringValue)[2]).add(mm.get("pu").stringValue());
        ((List<String>) mp.get(stringValue)[3]).add(mm.get("pt").stringValue());
      }
      nofifyNewPublications(mp);

    } catch (MarmottaException ex) {
      ex.printStackTrace();
    }
  }

  private void nofifyNewPublications(Map<String, Object[]> mp) {
    for (Map.Entry<String, Object[]> au : mp.entrySet()) {
      String authorURI = au.getKey();
      String authorName = ((String) au.getValue()[0]);
      String[] authorEmails = ((String[]) au.getValue()[1]);
      List<String> newPublicationsURI = ((List<String>) au.getValue()[2]);
      List<String> newPublicationsTitle = ((List<String>) au.getValue()[2]);

      //send notifications
      
      
    }
  }

}
