/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cepra.vivo;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Person;
import org.apache.marmotta.ucuenca.wk.commons.util.BoundedExecutor;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.query.Binding;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;

/**
 *
 * @author cedia
 */
public class SameAsVIVO {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {

    final SPARQLRepository data_rp = gRepository("data");
    final SPARQLRepository vivo_rp = gRepository("test_projs");

    Map<String, Person> redi = ObtPerson(query(data_rp, QueryLanguage.SPARQL, "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX schema: <http://schema.org/>\n"
            + "select distinct ?o ?p ?p_n ?o_n ?first ?last {\n"
            + "    graph <https://redi.cedia.edu.ec/context/redi> {\n"
            //+ "        ?p foaf:img [] .\n"
            + "        ?p schema:memberOf ?o .\n"
            + "        ?o foaf:name ?o_n .\n"
            + "?o <http://ucuenca.edu.ec/ontology#memberOf> <https://redi.cedia.edu.ec/> .\n"
            + "        {\n"
            + "            ?p foaf:name ?p_n .\n"
            + "        } union {\n"
            + "            ?p foaf:firstName ?first .\n"
            + "            ?p foaf:lastName ?last .\n"
            + "        }\n"
            + "    }    \n"
            + "} "));

    Map<String, Person> cepra = ObtPerson(query(vivo_rp, QueryLanguage.SPARQL, "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "select distinct ?o ?p ?o_n ?p_n {\n"
            + "	graph <https://redi.cedia.edu.ec/context/fix_vivo> {\n"
            + "        ?r a <http://vivoweb.org/ontology/core#FacultyPosition>.\n"
            + "        ?r <http://vivoweb.org/ontology/core#relates> ?p .\n"
            + "        ?r <http://vivoweb.org/ontology/core#relates> ?o .\n"
            + "        ?o a <http://vivoweb.org/ontology/core#University> .\n"
            + "        ?p a <http://vivoweb.org/ontology/core#FacultyMember> .\n"
            + "        ?o rdfs:label ?o_n .\n"
            + "        ?p rdfs:label ?p_n .\n"
            + "    }\n"
            + "}"));

    BoundedExecutor threadPool = BoundedExecutor.getThreadPool(4);
    for (final Map.Entry<String, Person> a : redi.entrySet()) {
      for (final Map.Entry<String, Person> b : cepra.entrySet()) {

        threadPool.submitTask(new Runnable() {
          @Override
          public void run() {
            try {
              Boolean checkName = a.getValue().checkName(b.getValue(), true);
              if (checkName != null && checkName) {
                Boolean checkAffiliations = a.getValue().checkAffiliations(b.getValue());
                if (checkAffiliations != null && checkAffiliations) {

                  RepositoryConnection connection = vivo_rp.getConnection();
                  connection.begin();
                  connection.add(ValueFactoryImpl.getInstance().createURI(a.getKey()),
                          OWL.SAMEAS, ValueFactoryImpl.getInstance().createURI(b.getKey()),
                          ValueFactoryImpl.getInstance().createURI("https://redi.cedia.edu.ec/context/vivo-cepra-fix"));
                  connection.commit();
                  connection.close();
                }
              }
            } catch (RepositoryException ex) {
              Logger.getLogger(SameAsVIVO.class.getName()).log(Level.SEVERE, null, ex);
            }
          }
        });

      }
    }

    threadPool.end();
    vivo_rp.shutDown();
    data_rp.shutDown();
  }

  public static SPARQLRepository gRepository(String dbs) throws RepositoryException {
    final SPARQLRepository data = new SPARQLRepository("http://201.159.222.25:8180/repositories/" + dbs, "http://201.159.222.25:8180/repositories/" + dbs + "/statements");
    ConcurrentHashMap<String, String> additionalHttpHeaders = new ConcurrentHashMap<>();
    additionalHttpHeaders.put("Accept", "application/sparql-results+json,*/*;q=0.9");
    data.setAdditionalHttpHeaders(additionalHttpHeaders);
    data.setUsernameAndPassword("rediclon", "5783b10a8f22$mznx");
    data.initialize();

    return data;
  }

  public static Map<String, Person> ObtPerson(List<Map<String, Value>> lst) {
    Map<String, Set<String>> aff = new HashMap<>();
    for (Map<String, Value> ar : lst) {
      String o = ar.get("o").stringValue();
      String o_n = ar.get("o_n").stringValue();
      if (!aff.containsKey(o)) {
        aff.put(o, new HashSet<String>());
      }
      aff.get(o).add(o_n);
    }

    Map<String, Person> pers = new HashMap<>();
    for (Map<String, Value> ar : lst) {
      String p = ar.get("p").stringValue();
      if (!pers.containsKey(p)) {
        Person p1 = new Person();
        pers.put(p, p1);
        p1.Name = new ArrayList<>();
        p1.Affiliations = new ArrayList<>();
      }
      if (ar.containsKey("p_n")) {
        String o_n = ar.get("p_n").stringValue();
        pers.get(p).Name.add(Lists.newArrayList(o_n));
      }
      if (ar.containsKey("first") && ar.containsKey("last")) {
        String f = ar.get("first").stringValue();
        String l = ar.get("last").stringValue();
        pers.get(p).Name.add(Lists.newArrayList(f, l));
      }
      pers.get(p).Affiliations.addAll(aff.get(ar.get("o").stringValue()));
    }

    return pers;
  }

  public static List<Map<String, Value>> query(SPARQLRepository rx, QueryLanguage ql, String string) throws MarmottaException {
    List<Map<String, Value>> r = new ArrayList<>();
    try {
      RepositoryConnection connection = rx.getConnection();
      TupleQueryResult evaluate = connection.prepareTupleQuery(ql, string).evaluate();
      while (evaluate.hasNext()) {
        Iterator<Binding> iterator = evaluate.next().iterator();
        ConcurrentHashMap<String, Value> mp = new ConcurrentHashMap<>();
        while (iterator.hasNext()) {
          Binding next = iterator.next();
          mp.put(next.getName(), next.getValue());
        }
        r.add(mp);
      }
      connection.close();
    } catch (Exception ex) {
      throw new MarmottaException(ex + "" + string);
    }
    return r;
  }

}
