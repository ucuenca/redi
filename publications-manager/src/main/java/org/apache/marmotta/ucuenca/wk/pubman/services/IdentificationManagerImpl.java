/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.apache.marmotta.ucuenca.wk.commons.util.SPARQLUtils;
import org.apache.marmotta.ucuenca.wk.pubman.api.IdentificationManager;
import org.apache.marmotta.ucuenca.wk.pubman.utils.BucketType;
import org.apache.marmotta.ucuenca.wk.pubman.utils.MapSetWID;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

@ApplicationScoped
public class IdentificationManagerImpl implements IdentificationManager {

  @Inject
  private ConstantService constantService;

  @Inject
  private ExternalSPARQLService sparqlService;

  @Inject
  private ConfigurationService conf;

  @Inject
  private org.slf4j.Logger log;

  @Override
  public synchronized void addBucket(BucketType typ, Set<String> set, Map<BucketType, Long> tc, MapSetWID mpp) throws Exception {
    List<String> buckets = Lists.newArrayList(seekBucket(set, mpp));
    if (buckets.isEmpty()) {
      buckets = Lists.newArrayList(getBaseURI(typ) + nextID(tc, typ));
      eventNewBucket(typ, buckets.get(0));
    }
    boolean fix = buckets.size() > 1;
    if (fix) {
      mpp.deleteRest(buckets);
    }
    mpp.put(buckets.get(0), set);
  }

  public void eventNewBucket(BucketType typ, String b) throws Exception {
    log.info("New Bucket, Type={}, Bucket={}", typ.name(), b);
    URI createURI = ValueFactoryImpl.getInstance().createURI(constantService.getBaseResource() + "type/" + typ.name());
    URI createURIp = ValueFactoryImpl.getInstance().createURI("https://redi.cedia.edu.ec/ont#newBucket");
    URI createURIo = ValueFactoryImpl.getInstance().createURI(b);
    URI createURIc = ValueFactoryImpl.getInstance().createURI(constantService.getBaseContext() + "notifications");
    Model m = new LinkedHashModel();
    m.add(createURI, createURIp, createURIo, createURIc);
    sparqlService.getGraphDBInstance().addBuffer(createURIc, m);
  }

  public Set<String> seekBucket(Set<String> set, MapSetWID mpp) throws MarmottaException, Exception {
    return mpp.findBuckets(set);
  }

  public String toRDFList(Set<String> set) {
    String q = "";
    for (String x : set) {
      q += " <" + x + "> ";
    }
    return q;
  }

  private String getBaseURI(BucketType bt) {
    return constantService.getBaseResource() + bt.name() + "/";
  }

  @Override
  public void applyFix() throws MarmottaException, Exception {

    String q1 = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
            + "delete {\n"
            + "    graph <https://redi.cedia.edu.ec/context/buckets> {\n"
            + "        ?a <https://redi.cedia.edu.ec/ont#element> ?b .\n"
            + "    }\n"
            + "} insert {\n"
            + "    graph <https://redi.cedia.edu.ec/context/buckets> {\n"
            + "        ?x <https://redi.cedia.edu.ec/ont#type> 'author' .\n"
            + "        ?x <https://redi.cedia.edu.ec/ont#element> ?b .\n"
            + "    }\n"
            + "} where {\n"
            + "    graph <https://redi.cedia.edu.ec/context/bucketsManualFix> {\n"
            + "        ?x owl:is ?b .\n"
            + "    }\n"
            + "    optional {\n"
            + "        graph <https://redi.cedia.edu.ec/context/buckets> {\n"
            + "        	?a <https://redi.cedia.edu.ec/ont#type> 'author' .\n"
            + "        	?a <https://redi.cedia.edu.ec/ont#element> ?b .\n"
            + "    	}\n"
            + "    }\n"
            + "} ";
    String q2 = "delete {\n"
            + "    graph <https://redi.cedia.edu.ec/context/buckets> {\n"
            + "        ?c <https://redi.cedia.edu.ec/ont#element> ?b .\n"
            + "    }\n"
            + "} where {\n"
            + "    graph <https://redi.cedia.edu.ec/context/buckets> {\n"
            + "        ?a <https://redi.cedia.edu.ec/ont#type> 'author' .\n"
            + "        ?c <https://redi.cedia.edu.ec/ont#type> 'ext_author' .\n"
            + "        ?a <https://redi.cedia.edu.ec/ont#element> ?b .\n"
            + "        ?c <https://redi.cedia.edu.ec/ont#element> ?b .\n"
            + "        filter (str(?a)!=str(?c)).\n"
            + "    }\n"
            + "} ";
    String q3 = "delete {\n"
            + "    graph <https://redi.cedia.edu.ec/context/buckets> {\n"
            + "        ?a <https://redi.cedia.edu.ec/ont#type> ?t .\n"
            + "    }\n"
            + "} where {\n"
            + "    graph <https://redi.cedia.edu.ec/context/buckets> {\n"
            + "        ?a <https://redi.cedia.edu.ec/ont#type> ?t .\n"
            + "        filter not exists {\n"
            + "        	?a <https://redi.cedia.edu.ec/ont#element> ?b .    \n"
            + "        }\n"
            + "    }\n"
            + "} ";

    sparqlService.getSparqlService().update(QueryLanguage.SPARQL, q1);
    sparqlService.getSparqlService().update(QueryLanguage.SPARQL, q2);
    sparqlService.getSparqlService().update(QueryLanguage.SPARQL, q3);

  }

  @Override
  public List<String> getBuckets(BucketType typ) throws MarmottaException, Exception {
    String q = "PREFIX redi: <https://redi.cedia.edu.ec/ont#>\n"
            + "select distinct ?b {\n"
            + "    graph <" + getGraph() + "> {\n"
            + "        ?b redi:type '" + typ.name() + "' .\n"
            + "        ?b redi:element ?e .\n"
            + "    }\n"
            + "}";
    List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, q);

    List<String> b = Lists.newArrayList();
    for (Map<String, Value> m : query) {
      b.add(m.get("b").stringValue());
    }
    return b;
  }

  @Override
  public String getGraph() throws MarmottaException, Exception {
    return constantService.getBaseContext() + "buckets";
  }

  public long getCurrentID(BucketType bt) throws MarmottaException, RepositoryException, Exception {
    //String k = "redi.count." + bt.name();
    //long nnext = conf.getLongConfiguration(k, 1000000);
    //conf.setLongConfiguration(k, nnext);
    URI createURI = ValueFactoryImpl.getInstance().createURI(constantService.getBaseResource() + "counter/" + bt.name());
    URI createURIp = ValueFactoryImpl.getInstance().createURI("https://redi.cedia.edu.ec/ont#count");
    URI createURIc = ValueFactoryImpl.getInstance().createURI(getGraph());

    RepositoryConnection repositoryConnetionCustom = sparqlService.getRepositoryConnetionCustom();
    RepositoryResult<Statement> statements = repositoryConnetionCustom.getStatements(createURI, createURIp, null, false, createURIc);
    long v = 1000000;
    if (statements.hasNext()) {
      Statement next = statements.next();
      v = Long.parseLong(next.getObject().stringValue());
    }
    repositoryConnetionCustom.close();
    return v;
  }

  public long nextID(Map<BucketType, Long> m, BucketType bt) throws MarmottaException {
    Long get = m.get(bt) + 1;
    m.put(bt, get);
    return get;
  }

  @Override
  public Map<BucketType, Long> getCounters() throws MarmottaException, Exception {
    Map<BucketType, Long> n = new HashMap<>();
    n.put(BucketType.author, getCurrentID(BucketType.author));
    n.put(BucketType.collection, getCurrentID(BucketType.collection));
    n.put(BucketType.ext_author, getCurrentID(BucketType.ext_author));
    n.put(BucketType.project, getCurrentID(BucketType.project));
    n.put(BucketType.publication, getCurrentID(BucketType.publication));
    n.put(BucketType.subject, getCurrentID(BucketType.subject));
    return n;
  }

  @Override
  public void saveCounters(Map<BucketType, Long> map) throws MarmottaException, Exception {
    for (Iterator<Map.Entry<BucketType, Long>> it = map.entrySet().iterator(); it.hasNext();) {
      Map.Entry<BucketType, Long> next = it.next();
      //String k = "redi.count." + next.getKey().name();
      //conf.setLongConfiguration(k, next.getValue());
      URI createURI = ValueFactoryImpl.getInstance().createURI(constantService.getBaseResource() + "counter/" + next.getKey().name());
      URI createURIp = ValueFactoryImpl.getInstance().createURI("https://redi.cedia.edu.ec/ont#count");
      URI createURIc = ValueFactoryImpl.getInstance().createURI(getGraph());
      Literal createLiteral = ValueFactoryImpl.getInstance().createLiteral(next.getValue() + "");
      RepositoryConnection repositoryConnetionCustom = sparqlService.getRepositoryConnetionCustom();

      repositoryConnetionCustom.begin();
      repositoryConnetionCustom.remove(createURI, createURIp, null, createURIc);
      repositoryConnetionCustom.add(createURI, createURIp, createLiteral, createURIc);
      repositoryConnetionCustom.commit();

      repositoryConnetionCustom.close();
    }
  }

  @Override
  public MapSetWID getBucketsContent(BucketType bt) throws MarmottaException, Exception {
    MapSetWID ss = new MapSetWID();
    String q = "PREFIX redi: <https://redi.cedia.edu.ec/ont#>\n"
            + "select distinct ?b ?e {\n"
            + "    graph <" + getGraph() + "> {\n"
            + "        ?b redi:type '" + bt.name() + "' .\n"
            + "        ?b redi:element ?e .\n"
            + "    }\n"
            + "}";
    List<Map<String, Value>> query = sparqlService.getSparqlService().query(QueryLanguage.SPARQL, q);
    Map<String, Set<String>> b = new HashMap<>();
    for (Map<String, Value> m : query) {
      if (!b.containsKey(m.get("b").stringValue())) {
        b.put(m.get("b").stringValue(), new HashSet<String>());
      }
      b.get(m.get("b").stringValue()).add(m.get("e").stringValue());
    }
    ss.put(b);
    return ss;
  }

  @Override
  public void saveBucketsContent(MapSetWID mswid, BucketType t) throws MarmottaException, Exception {
    ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
    RepositoryConnection repositoryConnetion = sparqlService.getRepositoryConnetion();
    repositoryConnetion.begin();
    Set<String> delete = mswid.getDelete();
    for (String g : delete) {
      repositoryConnetion.remove(instance.createURI(g), null, null, instance.createURI(getGraph()));
    }
    repositoryConnetion.commit();
    repositoryConnetion.close();
    for (Map.Entry<String, Set<String>> it : mswid.getData().entrySet()) {
      Model m = new LinkedHashModel();
      for (String x : it.getValue()) {
        m.add(instance.createURI(it.getKey()), instance.createURI("https://redi.cedia.edu.ec/ont#element"), instance.createURI(x));
      }
      m.add(instance.createURI(it.getKey()), instance.createURI("https://redi.cedia.edu.ec/ont#type"), instance.createLiteral(t.name()));
      sparqlService.getGraphDBInstance().addBuffer(instance.createURI(getGraph()), m);
    }
    sparqlService.getGraphDBInstance().dumpBuffer();
  }

}
