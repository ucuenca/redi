/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lyncode.xoai.dataprovider.core.Granularity;
import com.lyncode.xoai.dataprovider.exceptions.WritingXmlException;
import com.lyncode.xoai.dataprovider.xml.XmlOutputContext;
import com.lyncode.xoai.dataprovider.xml.xoai.Element;
import com.lyncode.xoai.dataprovider.xml.xoai.Metadata;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.AbstractMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.apache.marmotta.ucuenca.wk.pubman.api.OAI_Indexer;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.openrdf.model.Literal;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author cedia
 */
public class OAI_IndexerImpl implements OAI_Indexer {

  @Inject
  private ConstantService constantService;
  @Inject
  private ExternalSPARQLService sparqlService;
  @Inject
  private org.slf4j.Logger log;
  @Inject
  private TaskManagerService taskManagerService;

  @Override
  public void run() {
    try {
      runIdxPubs();
      //runIdxAuthors();
      //runIdxOrgs();
      //runIdxEvents();
      //runIdxProjects();
      runIdxPatents();
      //runIdxDatasets();
      
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void runIdxAuthors() throws SolrServerException, IOException, WritingXmlException, RepositoryException, MalformedQueryException, QueryEvaluationException, javax.xml.stream.XMLStreamException {

    SolrServer httpSolrServer = new HttpSolrServer("http://localhost/solrIdx/oai");

    Map<String, String> publications = getAuthors();

    log.info("Indexing authors");
    Task createSubTask = taskManagerService.createSubTask("Create OAI Index for authors");
    int i = 0;
    for (Map.Entry<String, String> ent : publications.entrySet()) {
      createSubTask.updateMessage(String.format("Index  author from %s organization", ent.getKey()));
      createSubTask.updateDetailMessage("Progress ", i + "/" + publications.size());

      log.info("Indexando... {} = {}/{}", ent.getKey(), i, publications.size());

      SolrInputDocument doc = new SolrInputDocument();
      doc.addField("item.id", "author_" + ent.getValue());
      doc.addField("item.handle", ent.getKey());
      doc.addField("item.identifier", ent.getKey());
      doc.addField("item.public", true);
      doc.addField("item.type", "rp");
      doc.addField("item.deleted", false);
      doc.addField("item.lastmodified", new Date());
      doc.addField("item.submitter", "jose.ortizv@ucuenca.edu.ec");
      doc.addField("item.collections", "col_redi");
      doc.addField("item.communities", "com_redi");

      List<Map.Entry<String, Literal>> vals = Lists.newArrayList();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      XmlOutputContext xmlContext = XmlOutputContext.emptyContext(out, Granularity.Second);
      retrieveMetadataAuthors(ent.getKey(), ent.getValue(), vals).write(xmlContext);
      xmlContext.getWriter().flush();
      xmlContext.getWriter().close();

      for (Map.Entry<String, Literal> dat : vals) {
        switch (dat.getKey()) {
          case "name":
            doc.addField("metadata.crisrp.fullName", dat.getValue().stringValue());
            break;
          case "email":
            doc.addField("metadata.crisrp.email", dat.getValue().stringValue());
            break;
          case "orgs":
            String[] orgs = dat.getValue().stringValue().split("@");
            for (String org : orgs) {
              String[] split = org.split("\\|");
              String orgId = split[0];
              String orgName = split[1];
              doc.addField("metadata.crisrp.dept", orgName);
              doc.addField("metadata.crisrp.dept.authority", orgId);
              doc.addField("metadata.crisrp.dept.confidence", "600");
            }
            break;
        }
      }

      doc.addField("item.compile", out.toString());
      httpSolrServer.add(doc);

      //System.out.println(i + "/" + publications.size());
      i++;

    }
    httpSolrServer.commit();

    taskManagerService.endTask(createSubTask);
  }

  private void runIdxOrgs() throws SolrServerException, IOException, WritingXmlException, RepositoryException, MalformedQueryException, QueryEvaluationException, javax.xml.stream.XMLStreamException {

    SolrServer httpSolrServer = new HttpSolrServer("http://localhost/solrIdx/oai");

    Map<String, String> publications = getOrganizations();

    log.info("Indexing Organizations");
    Task createSubTask = taskManagerService.createSubTask("Create OAI Index for organizations");
    int i = 0;
    for (Map.Entry<String, String> ent : publications.entrySet()) {
      createSubTask.updateMessage(String.format("Index  organization from %s organization", ent.getKey()));
      createSubTask.updateDetailMessage("Progress ", i + "/" + publications.size());

      log.info("Indexando... {} = {}/{}", ent.getKey(), i, publications.size());

      SolrInputDocument doc = new SolrInputDocument();
      doc.addField("item.id", "organization_" + ent.getValue());
      doc.addField("item.handle", ent.getKey());
      doc.addField("item.identifier", ent.getKey());
      doc.addField("item.public", true);
      doc.addField("item.type", "ou");
      doc.addField("item.deleted", false);
      doc.addField("item.lastmodified", new Date());
      doc.addField("item.submitter", "jose.ortizv@ucuenca.edu.ec");
      doc.addField("item.collections", "col_redi");
      doc.addField("item.communities", "com_redi");

      List<Map.Entry<String, Literal>> vals = Lists.newArrayList();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      XmlOutputContext xmlContext = XmlOutputContext.emptyContext(out, Granularity.Second);
      retrieveMetadataOrganizations(ent.getKey(), ent.getValue(), vals).write(xmlContext);
      xmlContext.getWriter().flush();
      xmlContext.getWriter().close();

      for (Map.Entry<String, Literal> dat : vals) {
        switch (dat.getKey()) {
          case "name":
            doc.addField("metadata.crisou.description", dat.getValue().stringValue());
            break;
          case "acro":
            doc.addField("metadata.crisou.name", dat.getValue().stringValue());
            break;
        }
      }

      doc.addField("item.compile", out.toString());
      httpSolrServer.add(doc);

      //System.out.println(i + "/" + publications.size());
      i++;

    }
    httpSolrServer.commit();

    taskManagerService.endTask(createSubTask);
  }

  private void runIdxEvents() throws SolrServerException, IOException, WritingXmlException, RepositoryException, MalformedQueryException, QueryEvaluationException, javax.xml.stream.XMLStreamException {

    SolrServer httpSolrServer = new HttpSolrServer("http://localhost/solrIdx/oai");

    Map<String, String> publications = getEvents();

    log.info("Indexing Events");
    Task createSubTask = taskManagerService.createSubTask("Create OAI Index for events");
    int i = 0;
    for (Map.Entry<String, String> ent : publications.entrySet()) {
      createSubTask.updateMessage(String.format("Index  event from %s organization", ent.getKey()));
      createSubTask.updateDetailMessage("Progress ", i + "/" + publications.size());

      log.info("Indexando... {} = {}/{}", ent.getKey(), i, publications.size());

      SolrInputDocument doc = new SolrInputDocument();
      doc.addField("item.id", "event_" + ent.getValue());
      doc.addField("item.handle", ent.getKey());
      doc.addField("item.identifier", ent.getKey());
      doc.addField("item.public", true);
      doc.addField("item.type", "events");
      doc.addField("item.deleted", false);
      doc.addField("item.lastmodified", new Date());
      doc.addField("item.submitter", "jose.ortizv@ucuenca.edu.ec");
      doc.addField("item.collections", "col_redi");
      doc.addField("item.communities", "com_redi");

      List<Map.Entry<String, Literal>> vals = Lists.newArrayList();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      XmlOutputContext xmlContext = XmlOutputContext.emptyContext(out, Granularity.Second);
      retrieveMetadataEvents(ent.getKey(), ent.getValue(), vals).write(xmlContext);
      xmlContext.getWriter().flush();
      xmlContext.getWriter().close();

      for (Map.Entry<String, Literal> dat : vals) {
        switch (dat.getKey()) {
          case "name":
            //doc.addField("metadata.crisou.description", dat.getValue().stringValue());
            break;
          case "acro":
            //doc.addField("metadata.crisou.name", dat.getValue().stringValue());
            break;
        }
      }

      doc.addField("item.compile", out.toString());
      httpSolrServer.add(doc);

      //System.out.println(i + "/" + publications.size());
      i++;

    }
    httpSolrServer.commit();

    taskManagerService.endTask(createSubTask);
  }

  private void runIdxPatents() throws SolrServerException, IOException, WritingXmlException, RepositoryException, MalformedQueryException, QueryEvaluationException, javax.xml.stream.XMLStreamException {

    SolrServer httpSolrServer = new HttpSolrServer("http://localhost/solrIdx/oai");

    Map<String, String> publications = getPatents();

    log.info("Indexing Patents");
    Task createSubTask = taskManagerService.createSubTask("Create OAI Index for patents");
    int i = 0;
    for (Map.Entry<String, String> ent : publications.entrySet()) {
      createSubTask.updateMessage(String.format("Index  patent from %s organization", ent.getKey()));
      createSubTask.updateDetailMessage("Progress ", i + "/" + publications.size());

      log.info("Indexando... {} = {}/{}", ent.getKey(), i, publications.size());

      SolrInputDocument doc = new SolrInputDocument();
      doc.addField("item.id", "patent_" + ent.getValue());
      doc.addField("item.handle", ent.getKey());
      doc.addField("item.identifier", ent.getKey());
      doc.addField("item.public", true);
      doc.addField("item.type", "item");
      doc.addField("item.deleted", false);
      doc.addField("item.lastmodified", new Date());
      doc.addField("item.submitter", "jose.ortizv@ucuenca.edu.ec");
      doc.addField("item.collections", "col_redi");
      doc.addField("item.communities", "com_redi");

      List<Map.Entry<String, Literal>> vals = Lists.newArrayList();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      XmlOutputContext xmlContext = XmlOutputContext.emptyContext(out, Granularity.Second);
      retrieveMetadataPatents(ent.getKey(), ent.getValue(), vals).write(xmlContext);
      xmlContext.getWriter().flush();
      xmlContext.getWriter().close();

      for (Map.Entry<String, Literal> dat : vals) {
        switch (dat.getKey()) {
          case "name":
            //doc.addField("metadata.crisou.description", dat.getValue().stringValue());
            break;
          case "acro":
            //doc.addField("metadata.crisou.name", dat.getValue().stringValue());
            break;
        }
      }
      doc.addField("metadata.item.cerifentitytype", "Patents");
      doc.addField("item.compile", out.toString());
      httpSolrServer.add(doc);

      //System.out.println(i + "/" + publications.size());
      i++;

    }
    httpSolrServer.commit();

    taskManagerService.endTask(createSubTask);
  }

  private void runIdxDatasets() throws SolrServerException, IOException, WritingXmlException, RepositoryException, MalformedQueryException, QueryEvaluationException, javax.xml.stream.XMLStreamException {

    SolrServer httpSolrServer = new HttpSolrServer("http://localhost/solrIdx/oai");

    Map<String, String> publications = getDatasets();

    log.info("Indexing Datasets");
    Task createSubTask = taskManagerService.createSubTask("Create OAI Index for datasets");
    int i = 0;
    for (Map.Entry<String, String> ent : publications.entrySet()) {
      createSubTask.updateMessage(String.format("Index  dataset from %s organization", ent.getKey()));
      createSubTask.updateDetailMessage("Progress ", i + "/" + publications.size());

      log.info("Indexando... {} = {}/{}", ent.getKey(), i, publications.size());

      SolrInputDocument doc = new SolrInputDocument();
      doc.addField("item.id", "dataset_" + ent.getValue());
      doc.addField("item.handle", ent.getKey());
      doc.addField("item.identifier", ent.getKey());
      doc.addField("item.public", true);
      doc.addField("item.type", "item");
      doc.addField("item.deleted", false);
      doc.addField("item.lastmodified", new Date());
      doc.addField("item.submitter", "jose.ortizv@ucuenca.edu.ec");
      doc.addField("item.collections", "col_redi");
      doc.addField("item.communities", "com_redi");

      List<Map.Entry<String, Literal>> vals = Lists.newArrayList();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      XmlOutputContext xmlContext = XmlOutputContext.emptyContext(out, Granularity.Second);
      retrieveMetadataDatasets(ent.getKey(), ent.getValue(), vals).write(xmlContext);
      xmlContext.getWriter().flush();
      xmlContext.getWriter().close();

      for (Map.Entry<String, Literal> dat : vals) {
        switch (dat.getKey()) {
          case "name":
            //doc.addField("metadata.crisou.description", dat.getValue().stringValue());
            break;
          case "acro":
            //doc.addField("metadata.crisou.name", dat.getValue().stringValue());
            break;
        }
      }
      doc.addField("metadata.item.cerifentitytype", "Products");
      doc.addField("item.compile", out.toString());
      httpSolrServer.add(doc);

      //System.out.println(i + "/" + publications.size());
      i++;

    }
    httpSolrServer.commit();

    taskManagerService.endTask(createSubTask);
  }

  private void runIdxProjects() throws SolrServerException, IOException, WritingXmlException, RepositoryException, MalformedQueryException, QueryEvaluationException, javax.xml.stream.XMLStreamException {

    SolrServer httpSolrServer = new HttpSolrServer("http://localhost/solrIdx/oai");

    Map<String, String> publications = getProjects();

    log.info("Indexing Projects");
    Task createSubTask = taskManagerService.createSubTask("Create OAI Index for projects");
    int i = 0;
    for (Map.Entry<String, String> ent : publications.entrySet()) {
      createSubTask.updateMessage(String.format("Index  project from %s organization", ent.getKey()));
      createSubTask.updateDetailMessage("Progress ", i + "/" + publications.size());

      log.info("Indexando... {} = {}/{}", ent.getKey(), i, publications.size());

      SolrInputDocument doc = new SolrInputDocument();
      doc.addField("item.id", "project_" + ent.getValue());
      doc.addField("item.handle", ent.getKey());
      doc.addField("item.identifier", ent.getKey());
      doc.addField("item.public", true);
      doc.addField("item.type", "project");
      doc.addField("item.deleted", false);
      doc.addField("item.lastmodified", new Date());
      doc.addField("item.submitter", "jose.ortizv@ucuenca.edu.ec");
      doc.addField("item.collections", "col_redi");
      doc.addField("item.communities", "com_redi");

      List<Map.Entry<String, Literal>> vals = Lists.newArrayList();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      XmlOutputContext xmlContext = XmlOutputContext.emptyContext(out, Granularity.Second);
      retrieveMetadataProjects(ent.getKey(), ent.getValue(), vals).write(xmlContext);
      xmlContext.getWriter().flush();
      xmlContext.getWriter().close();

      for (Map.Entry<String, Literal> dat : vals) {
        switch (dat.getKey()) {
          case "name":
            //doc.addField("metadata.crisou.description", dat.getValue().stringValue());
            break;
          case "acro":
            //doc.addField("metadata.crisou.name", dat.getValue().stringValue());
            break;
        }
      }

      doc.addField("item.compile", out.toString());
      httpSolrServer.add(doc);

      //System.out.println(i + "/" + publications.size());
      i++;

    }
    httpSolrServer.commit();

    taskManagerService.endTask(createSubTask);
  }

  private Map<String, String> getAuthors() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
    Map<String, String> mp = Maps.newConcurrentMap();
    RepositoryConnection connection = sparqlService.getRepositoryConnetion();

    TupleQueryResult evaluate = connection.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix dct: <http://purl.org/dc/terms/>\n"
            + "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "select distinct ?res ?pid{\n"
            + "    graph <" + constantService.getCentralGraph() + "> {\n"
            + "        ?org <http://ucuenca.edu.ec/ontology#memberOf> <https://redi.cedia.edu.ec/> .\n"
            + "        ?res <http://schema.org/memberOf> ?org .\n"
            + "        ?res foaf:publications [] .\n"
            + "        filter (regex(str(?res),'resource/author','i')) .\n"
            + "        bind(REPLACE (str(?res), 'https://redi.cedia.edu.ec/resource/author/', '') as ?pid).\n"
            + "    }\n"
            + "}").evaluate();

    while (evaluate.hasNext()) {
      BindingSet next = evaluate.next();
      String p = next.getBinding("res").getValue().stringValue();
      String pid = next.getBinding("pid").getValue().stringValue();
      mp.put(p, pid);
      //mp.put("https://redi.cedia.edu.ec/resource/publication/1106785", "1106785");
    }

    connection.close();
    return mp;
  }

  private Map<String, String> getOrganizations() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
    Map<String, String> mp = Maps.newConcurrentMap();
    RepositoryConnection connection = sparqlService.getRepositoryConnetion();

    TupleQueryResult evaluate = connection.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix dct: <http://purl.org/dc/terms/>\n"
            + "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "select distinct ?org ?pid{\n"
            + "    graph <" + constantService.getCentralGraph() + "> {\n"
            + "        ?org <http://ucuenca.edu.ec/ontology#memberOf> <https://redi.cedia.edu.ec/> .\n"
            + "        bind (md5(str(?org)) as ?pid).\n"
            + "    }\n"
            + "}").evaluate();

    while (evaluate.hasNext()) {
      BindingSet next = evaluate.next();
      String p = next.getBinding("org").getValue().stringValue();
      String pid = next.getBinding("pid").getValue().stringValue();
      mp.put(p, pid);
      //mp.put("https://redi.cedia.edu.ec/resource/publication/1106785", "1106785");
    }

    connection.close();
    return mp;
  }

  private Map<String, String> getEvents() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
    Map<String, String> mp = Maps.newConcurrentMap();
    RepositoryConnection connection = sparqlService.getRepositoryConnetion();

    TupleQueryResult evaluate = connection.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix dct: <http://purl.org/dc/terms/>\n"
            + "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "select distinct ?org ?pid{\n"
            + "    graph <" + constantService.getCentralGraph() + "> {\n"
            + "        ?org a <http://eurocris.org/ontology/cerif#Event> .\n"
            + "        bind (md5(str(?org)) as ?pid).\n"
            + "    }\n"
            + "}").evaluate();

    while (evaluate.hasNext()) {
      BindingSet next = evaluate.next();
      String p = next.getBinding("org").getValue().stringValue();
      String pid = next.getBinding("pid").getValue().stringValue();
      mp.put(p, pid);
      //mp.put("https://redi.cedia.edu.ec/resource/publication/1106785", "1106785");
    }

    connection.close();
    return mp;
  }

  private Map<String, String> getPatents() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
    Map<String, String> mp = Maps.newConcurrentMap();
    RepositoryConnection connection = sparqlService.getRepositoryConnetion();

    TupleQueryResult evaluate = connection.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix dct: <http://purl.org/dc/terms/>\n"
            + "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "select distinct ?org ?pid{\n"
            + "    graph <" + constantService.getCentralGraph() + "> {\n"
            + "        ?org a <http://www.eurocris.org/ontologies/cerif/1.3/Patent> .\n"
            + "        bind (md5(str(?org)) as ?pid).\n"
            + "    }\n"
            + "}").evaluate();

    while (evaluate.hasNext()) {
      BindingSet next = evaluate.next();
      String p = next.getBinding("org").getValue().stringValue();
      String pid = next.getBinding("pid").getValue().stringValue();
      mp.put(p, pid);
      //mp.put("https://redi.cedia.edu.ec/resource/publication/1106785", "1106785");
    }

    connection.close();
    return mp;
  }

  private Map<String, String> getDatasets() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
    Map<String, String> mp = Maps.newConcurrentMap();
    RepositoryConnection connection = sparqlService.getRepositoryConnetion();

    TupleQueryResult evaluate = connection.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix dct: <http://purl.org/dc/terms/>\n"
            + "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "select distinct ?org ?pid{\n"
            + "    graph <" + constantService.getCentralGraph() + "> {\n"
            + "        ?org a <http://schema.org/Dataset> .\n"
            + "        bind (md5(str(?org)) as ?pid).\n"
            + "    }\n"
            + "}").evaluate();

    while (evaluate.hasNext()) {
      BindingSet next = evaluate.next();
      String p = next.getBinding("org").getValue().stringValue();
      String pid = next.getBinding("pid").getValue().stringValue();
      mp.put(p, pid);
      //mp.put("https://redi.cedia.edu.ec/resource/publication/1106785", "1106785");
    }

    connection.close();
    return mp;
  }

  private Map<String, String> getProjects() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
    Map<String, String> mp = Maps.newConcurrentMap();
    RepositoryConnection connection = sparqlService.getRepositoryConnetion();

    TupleQueryResult evaluate = connection.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix dct: <http://purl.org/dc/terms/>\n"
            + "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "select distinct ?org ?pid{\n"
            + "    graph <" + constantService.getCentralGraph() + "> {\n"
            + "        ?org a <http://xmlns.com/foaf/0.1/Project> .\n"
            + "        bind (md5(str(?org)) as ?pid).\n"
            + "    }\n"
            + "}").evaluate();

    while (evaluate.hasNext()) {
      BindingSet next = evaluate.next();
      String p = next.getBinding("org").getValue().stringValue();
      String pid = next.getBinding("pid").getValue().stringValue();
      mp.put(p, pid);
      //mp.put("https://redi.cedia.edu.ec/resource/publication/1106785", "1106785");
    }

    connection.close();
    return mp;
  }

  private Map<String, String> getPublications() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
    Map<String, String> mp = Maps.newConcurrentMap();
    RepositoryConnection connection = sparqlService.getRepositoryConnetion();

    TupleQueryResult evaluate = connection.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix dct: <http://purl.org/dc/terms/>\n"
            + "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "select distinct ?p ?pid{\n"
            + "    graph <" + constantService.getCentralGraph() + "> {\n"
            + "        ?org <http://ucuenca.edu.ec/ontology#memberOf> <https://redi.cedia.edu.ec/> .\n"
            + "        ?res <http://schema.org/memberOf> ?org .\n"
            + "        ?res foaf:publications ?p .\n"
            + "        filter (regex(str(?p),'resource/publication','i')) .\n"
            + "        bind(REPLACE (str(?p), 'https://redi.cedia.edu.ec/resource/publication/', '') as ?pid).\n"
            + "    }\n"
            + "}").evaluate();

    while (evaluate.hasNext()) {
      BindingSet next = evaluate.next();
      String p = next.getBinding("p").getValue().stringValue();
      String pid = next.getBinding("pid").getValue().stringValue();
      mp.put(p, pid);
      //mp.put("https://redi.cedia.edu.ec/resource/publication/1106785", "1106785");
    }

    connection.close();
    return mp;
  }

  private void runIdxPubs() throws SolrServerException, IOException, WritingXmlException, RepositoryException, MalformedQueryException, QueryEvaluationException, javax.xml.stream.XMLStreamException {

    SolrServer httpSolrServer = new HttpSolrServer("http://localhost/solrIdx/oai");

    Map<String, String> publications = getPublications();

    log.info("Indexando publicaciones");
    Task createSubTask = taskManagerService.createSubTask("Creando index OAI de publicaciones");
    int i = 0;
    for (Map.Entry<String, String> ent : publications.entrySet()) {
      createSubTask.updateMessage(String.format("Indexando publication from %s organization", ent.getKey()));
      createSubTask.updateDetailMessage("Porcentaje", i + "/" + publications.size());

      log.info("Indexando... {} = {}/{}", ent.getKey(), i, publications.size());

      SolrInputDocument doc = new SolrInputDocument();
      doc.addField("item.id", "publication_" + ent.getValue());
      doc.addField("item.handle", ent.getKey());
      doc.addField("item.identifier", ent.getKey());
      doc.addField("item.public", true);
      doc.addField("item.type", "item");
      doc.addField("item.deleted", false);
      doc.addField("item.lastmodified", new Date());
      doc.addField("item.submitter", "jose.ortizv@ucuenca.edu.ec");
      doc.addField("item.collections", "col_redi");
      doc.addField("item.communities", "com_redi");

      List<Map.Entry<String, Literal>> vals = Lists.newArrayList();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      XmlOutputContext xmlContext = XmlOutputContext.emptyContext(out, Granularity.Second);
      retrieveMetadataPublications(ent.getKey(), ent.getValue(), vals).write(xmlContext);
      xmlContext.getWriter().flush();
      xmlContext.getWriter().close();

      for (Map.Entry<String, Literal> dat : vals) {
        switch (dat.getKey()) {
          case "title":
            doc.addField("metadata.dc.title", dat.getValue().stringValue());
            break;
          case "creator":
            doc.addField("metadata.dc.contributor.author", dat.getValue().stringValue());
            break;
          case "doi":
            doc.addField("metadata.dc.identifier.doi", dat.getValue().stringValue());
            break;
          case "date":
            doc.addField("metadata.dc.date.issued", dat.getValue().stringValue());
            doc.addField("metadata.dc.date.available", dat.getValue().stringValue());
            doc.addField("metadata.dc.date.accessioned", dat.getValue().stringValue());
            break;
        }
      }

      doc.addField("metadata.dc.identifier.uri", ent.getKey());
      doc.addField("metadata.dc.identifier.url", ent.getKey());

      doc.addField("metadata.item.cerifentitytype", "Publications");
      
      doc.addField("item.compile", out.toString());
      httpSolrServer.add(doc);

      //System.out.println(i + "/" + publications.size());
      i++;

    }
    httpSolrServer.commit();

    taskManagerService.endTask(createSubTask);
  }

  private static Element getElement(List<Element> list, String name) {
    for (Element e : list) {
      if (name.equals(e.getName())) {
        return e;
      }
    }

    return null;
  }

  private static Element create(String name) {
    Element e = new Element();
    e.setName(name);
    return e;
  }

  private static Element.Field createValue(
          String name, String value) {
    Element.Field e = new Element.Field();
    e.setValue(value);
    e.setName(name);
    return e;
  }

  public Metadata retrieveMetadataPublications(String URIitem, String idpub, List<Map.Entry<String, Literal>> mpp) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
    Metadata metadata;

    // read all metadata into Metadata Object
    metadata = new Metadata();

    RepositoryConnection con = sparqlService.getRepositoryConnetion();

    TupleQueryResult evaluate = con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX bibo: <http://purl.org/ontology/bibo/>\n"
            + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
            + "prefix dct: <http://purl.org/dc/terms/>\n"
            + "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "\n"
            + "select distinct ?title ?description ?subject ?publisher ?date ?type ?language ?creator ?doi  { \n"
            + "    {\n"
            + "        select distinct ?identifier ?title ?description ?subject ?publisher ?date ?type ?language ?doi {\n"
            + "            graph <" + constantService.getCentralGraph() + "> {\n"
            + "                bind (<" + URIitem + "> as ?identifier) .\n"
            + "                ?identifier dct:title ?title .\n"
            + "                ?identifier a ?typex.\n"
            + "                bind(str(?typex) as ?type).\n"
            + "                {?identifier bibo:abstract ?description . }\n"
            + "                union {?identifier dct:subject ?s . ?s rdfs:label ?subject. }\n"
            + "                union {?identifier dct:publisher ?p . ?p rdfs:label ?publisher. }\n"
            + "                union {?identifier <http://ns.nature.com/terms/coverDate> ?date. }\n"
            + "                union {?identifier <http://purl.org/dc/terms/language> ?language. }\n"
            + "                union {?identifier bibo:doi ?doi. }\n"
            + "            }\n"
            + "		}\n"
            + "    } union\n"
            + "    {\n"
            + "        select ?identifier ?au (sample(?namex) as ?creator) { \n"
            + "            graph <" + constantService.getCentralGraph() + "> {\n"
            + "                bind (<" + URIitem + "> as ?identifier) .\n"
            + "                ?au foaf:publications ?identifier .\n"
            + "                ?au foaf:name ?namex .\n"
            + "            }\n"
            + "        }  group by ?identifier ?au\n"
            + "    } .\n"
            + "}").evaluate();
    List<Map.Entry<String, Literal>> mp = Lists.newArrayList();

    Set<String> hs = Sets.newConcurrentHashSet();

    while (evaluate.hasNext()) {
      BindingSet next = evaluate.next();
      for (Binding bind : next) {

        if (bind.getValue() instanceof Literal) {
          Literal lit = ((Literal) bind.getValue());
          Literal createLiteral = ValueFactoryImpl.getInstance().createLiteral(Normalizer.normalize(lit.stringValue(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").replaceAll("\\p{C}", ""), lit.getLanguage());
          String key = bind.getName().hashCode() + "_" + createLiteral.stringValue().hashCode();
          if (!hs.contains(key)) {
            mp.add(new AbstractMap.SimpleEntry<String, Literal>(bind.getName(), createLiteral));
            hs.add(key);
          }
        } else {
          System.out.println(bind);
        }
      }
    }

    mp.add(new AbstractMap.SimpleEntry<String, Literal>("identifierURI", ValueFactoryImpl.getInstance().createLiteral(URIitem)));

    con.close();

    mpp.addAll(mp);

    Element item = create("item");
    metadata.getElement().add(item);
    Element cerifentitytype = create("cerifentitytype");
    item.getElement().add(cerifentitytype);
    Element create = create("none");
    cerifentitytype.getElement().add(create);
    create.getField().add(createValue("value", "Publications"));

    Element openairecristype = create("openairecristype");
    item.getElement().add(openairecristype);
    Element create2 = create("none");
    openairecristype.getElement().add(create2);
    create2.getField().add(createValue("value", "Publications"));
    
    int i = 0;
    for (Map.Entry<String, Literal> val : mp) {
      i++;
      boolean diffNone = false;
      String field = val.getKey();
      // Qualified element?
      String qua = "";

      switch (field) {
        case "identifierURI":
          qua = "uri";
          field = "identifier";
          break;
        case "doi":
          qua = "doi";
          field = "identifier";
          break;
        case "creator":
          qua = "author";
          field = "contributor";
          diffNone = true;
          break;
        case "language":
          qua = "iso";
          field = "language";
          val.setValue(ValueFactoryImpl.getInstance().createLiteral(val.getValue().stringValue(), "es_ES"));
          break;
        case "date":
          qua = "issued";
          field = "date";
          break;

        default:
          qua = null;
          break;
      }
      Element valueElem = null;
      Element schema = getElement(metadata.getElement(), "dc");
      if (schema == null) {
        schema = create("dc");
        metadata.getElement().add(schema);
      }
      valueElem = schema;

      // Has element.. with XOAI one could have only schema and value
      if (field != null && !field.equals("")) {
        Element element = getElement(schema.getElement(),
                field);
        if (element == null) {
          element = create(field);
          schema.getElement().add(element);
        }
        valueElem = element;

        if (qua != null && !qua.equals("")) {
          Element qualifier = getElement(element.getElement(),
                  qua);
          if (qualifier == null) {
            qualifier = create(qua);
            element.getElement().add(qualifier);
          }
          valueElem = qualifier;
        }
      }

      // Language?
      if (val.getValue().getLanguage() != null && !val.getValue().getLanguage().equals("")) {
        Element language = getElement(valueElem.getElement(),
                val.getValue().getLanguage());
        if (language == null) {
          language = create(val.getValue().getLanguage());
          valueElem.getElement().add(language);
        }
        valueElem = language;
      } else {
        String noneNma = diffNone ? "none_" + i : "none";

        Element language = getElement(valueElem.getElement(),
                noneNma);
        if (language == null) {
          language = create(noneNma);
          valueElem.getElement().add(language);
        }
        valueElem = language;
      }

      valueElem.getField().add(createValue("value", val.getValue().stringValue()));
    }
    // Done! Metadata has been read!
    // Now adding bitstream info
    Element bundles = create("bundles");
    metadata.getElement().add(bundles);

    String[] bs = new String[]{URIitem};
    for (String b : bs) {
      Element bundle = create("bundle");
      bundles.getElement().add(bundle);
      bundle.getField()
              .add(createValue("name", "ORIGINAL"));

      Element bitstreams = create("bitstreams");
      bundle.getElement().add(bitstreams);
      for (String bit : bs) {
        Element bitstream = create("bitstream");
        bitstreams.getElement().add(bitstream);
        String url = "";
        String cks = "0";
        String cka = "NONE";
        String oname = idpub;
        String name = idpub;
        String description = idpub;

        if (name != null) {
          bitstream.getField().add(
                  createValue("name", name));
        }
        if (oname != null) {
          bitstream.getField().add(
                  createValue("originalName", name));
        }
        if (description != null) {
          bitstream.getField().add(
                  createValue("description", description));
        }
        bitstream.getField().add(
                createValue("format", "text/html"));
        bitstream.getField().add(
                createValue("size", "0"));
        bitstream.getField().add(createValue("url", url));
        bitstream.getField().add(
                createValue("checksum", cks));
        bitstream.getField().add(
                createValue("checksumAlgorithm", cka));
        bitstream.getField().add(
                createValue("sid", "1"));
      }
    }

    // Other info
    addOtherRepository(metadata.getElement(), URIitem, idpub, "item");

    return metadata;
  }

  public Metadata retrieveMetadataAuthors(String URIitem, String idpub, List<Map.Entry<String, Literal>> mpp) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
    Metadata metadata;

    // read all metadata into Metadata Object
    metadata = new Metadata();

    RepositoryConnection con = sparqlService.getRepositoryConnetion();

    TupleQueryResult evaluate = con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX schema: <http://schema.org/>\n"
            + "select ?a (sample(distinct ?n) as ?name) (sample(distinct?em) as ?email) (group_concat(distinct ?orgb ; separator='@') as ?orgs) {\n"
            + "    graph <" + constantService.getCentralGraph() + "> {\n"
            + "        bind (<" + URIitem + "> as ?a ) .\n"
            + "        ?org <http://ucuenca.edu.ec/ontology#memberOf> <https://redi.cedia.edu.ec/> .\n"
            + "        ?a foaf:name ?n .\n"
            + "        ?a schema:memberOf ?org .\n"
            + "        ?org  foaf:name ?orgn .\n"
            + "        bind (concat (md5(str(?org)), '|', str(?orgn)) as ?orgb).\n"
            + "        optional {\n"
            + "            ?a <http://www.w3.org/2006/vcard/ns#hasEmail> ?em .\n"
            + "        }\n"
            + "    }\n"
            + "} group by ?a").evaluate();
    List<Map.Entry<String, Literal>> mp = Lists.newArrayList();

    Set<String> hs = Sets.newConcurrentHashSet();

    while (evaluate.hasNext()) {
      BindingSet next = evaluate.next();
      for (Binding bind : next) {

        if (bind.getValue() instanceof Literal) {
          Literal lit = ((Literal) bind.getValue());
          Literal createLiteral = ValueFactoryImpl.getInstance().createLiteral(Normalizer.normalize(lit.stringValue(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").replaceAll("\\p{C}", ""), lit.getLanguage());
          String key = bind.getName().hashCode() + "_" + createLiteral.stringValue().hashCode();
          if (!hs.contains(key)) {
            mp.add(new AbstractMap.SimpleEntry<String, Literal>(bind.getName(), createLiteral));
            hs.add(key);
          }
        } else {
          //System.out.println(bind);
        }
      }
    }

    mp.add(new AbstractMap.SimpleEntry<String, Literal>("identifierURI", ValueFactoryImpl.getInstance().createLiteral(URIitem)));

    con.close();

    mpp.addAll(mp);

    for (Map.Entry<String, Literal> val : mp) {
      String field = val.getKey();
      // Qualified element?
      String qua = "";

      switch (field) {
        case "name":
          qua = "none";
          field = "fullName";
          break;
        case "email":
          qua = "none";
          field = "email";
          break;

        case "orgs":
          addOrgs(metadata, val.getValue().stringValue(), "crisrp", "dept");
          continue;
        default:
          qua = null;
          break;
      }
      Element valueElem = null;
      Element schema = getElement(metadata.getElement(), "crisrp");
      if (schema == null) {
        schema = create("crisrp");
        metadata.getElement().add(schema);
      }
      valueElem = schema;

      // Has element.. with XOAI one could have only schema and value
      if (field != null && !field.equals("")) {
        Element element = getElement(schema.getElement(),
                field);
        if (element == null) {
          element = create(field);
          schema.getElement().add(element);
        }
        valueElem = element;

        if (qua != null && !qua.equals("")) {
          Element qualifier = getElement(element.getElement(),
                  qua);
          if (qualifier == null) {
            qualifier = create(qua);
            element.getElement().add(qualifier);
          }
          valueElem = qualifier;
        }
      }

      // Language?
      if (val.getValue().getLanguage() != null && !val.getValue().getLanguage().equals("")) {
        Element language = getElement(valueElem.getElement(),
                val.getValue().getLanguage());
        if (language == null) {
          language = create(val.getValue().getLanguage());
          valueElem.getElement().add(language);
        }
        valueElem = language;
      } else {
        Element language = getElement(valueElem.getElement(),
                "none");
        if (language == null) {
          language = create("none");
          valueElem.getElement().add(language);
        }
        valueElem = language;
      }

      valueElem.getField().add(createValue("value", val.getValue().stringValue()));
    }

    addOtherRepository(metadata.getElement(), URIitem, idpub, "rp");

    return metadata;
  }

  public Metadata retrieveMetadataOrganizations(String URIitem, String idpub, List<Map.Entry<String, Literal>> mpp) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
    Metadata metadata;

    // read all metadata into Metadata Object
    metadata = new Metadata();

    RepositoryConnection con = sparqlService.getRepositoryConnetion();

    TupleQueryResult evaluate = con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX schema: <http://schema.org/>\n"
            + "select ?a (sample(?n) as ?name) (sample (?ac) as ?acro) {\n"
            + "    graph <" + constantService.getCentralGraph() + "> {\n"
            + "        bind (<" + URIitem + "> as ?a ) .\n"
            + "		?a <http://www.eurocris.org/ontologies/cerif/1.3#acronym> ?ac .\n"
            + "        ?a <http://xmlns.com/foaf/0.1/name> ?n .\n"
            + "    }\n"
            + "} group by ?a").evaluate();
    List<Map.Entry<String, Literal>> mp = Lists.newArrayList();

    Set<String> hs = Sets.newConcurrentHashSet();

    while (evaluate.hasNext()) {
      BindingSet next = evaluate.next();
      for (Binding bind : next) {

        if (bind.getValue() instanceof Literal) {
          Literal lit = ((Literal) bind.getValue());
          Literal createLiteral = ValueFactoryImpl.getInstance().createLiteral(Normalizer.normalize(lit.stringValue(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").replaceAll("\\p{C}", ""), lit.getLanguage());
          String key = bind.getName().hashCode() + "_" + createLiteral.stringValue().hashCode();
          if (!hs.contains(key)) {
            mp.add(new AbstractMap.SimpleEntry<String, Literal>(bind.getName(), createLiteral));
            hs.add(key);
          }
        } else {
          //System.out.println(bind);
        }
      }
    }

    mp.add(new AbstractMap.SimpleEntry<String, Literal>("identifierURI", ValueFactoryImpl.getInstance().createLiteral(URIitem)));

    con.close();

    mpp.addAll(mp);

    for (Map.Entry<String, Literal> val : mp) {
      String field = val.getKey();
      // Qualified element?
      String qua = "";

      switch (field) {
        case "name":
          qua = "none";
          field = "description";
          break;
        case "acro":
          qua = "none";
          field = "name";
          break;
        default:
          qua = null;
          break;
      }
      Element valueElem = null;
      Element schema = getElement(metadata.getElement(), "crisou");
      if (schema == null) {
        schema = create("crisou");
        metadata.getElement().add(schema);
      }
      valueElem = schema;

      // Has element.. with XOAI one could have only schema and value
      if (field != null && !field.equals("")) {
        Element element = getElement(schema.getElement(),
                field);
        if (element == null) {
          element = create(field);
          schema.getElement().add(element);
        }
        valueElem = element;

        if (qua != null && !qua.equals("")) {
          Element qualifier = getElement(element.getElement(),
                  qua);
          if (qualifier == null) {
            qualifier = create(qua);
            element.getElement().add(qualifier);
          }
          valueElem = qualifier;
        }
      }

      // Language?
      if (val.getValue().getLanguage() != null && !val.getValue().getLanguage().equals("")) {
        Element language = getElement(valueElem.getElement(),
                val.getValue().getLanguage());
        if (language == null) {
          language = create(val.getValue().getLanguage());
          valueElem.getElement().add(language);
        }
        valueElem = language;
      } else {
        Element language = getElement(valueElem.getElement(),
                "none");
        if (language == null) {
          language = create("none");
          valueElem.getElement().add(language);
        }
        valueElem = language;
      }

      valueElem.getField().add(createValue("value", val.getValue().stringValue()));
    }

    addOtherRepository(metadata.getElement(), URIitem, idpub, "ou");

    return metadata;
  }

  public Metadata retrieveMetadataEvents(String URIitem, String idpub, List<Map.Entry<String, Literal>> mpp) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
    Metadata metadata;

    // read all metadata into Metadata Object
    metadata = new Metadata();

    RepositoryConnection con = sparqlService.getRepositoryConnetion();

    TupleQueryResult evaluate = con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX schema: <http://schema.org/>\n"
            + "select * {\n"
            + "    graph <" + constantService.getCentralGraph() + "> {\n"
            + "        bind (<" + URIitem + "> as ?a) .\n"
            + "        optional {\n"
            + "            ?a <http://eurocris.org/ontology/cerif#has_nameAbbreviation> ?ab .\n"
            + "        }\n"
            + "        optional {\n"
            + "            ?a <http://eurocris.org/ontology/cerif#has_name> ?n .\n"
            + "        }\n"
            + "        optional {\n"
            + "            ?a <http://eurocris.org/ontology/cerif#has_description> ?d .\n"
            + "        }\n"
            + "        optional {\n"
            + "            ?a <http://eurocris.org/ontology/cerif#has_cityTown> ?l .\n"
            + "        }\n"
            + "        optional {\n"
            + "            ?a <http://eurocris.org/ontology/cerif#has_keywords> ?k .\n"
            + "        }\n"
            + "        optional {\n"
            + "            ?a <http://eurocris.org/ontology/cerif#has_startDate> ?s .\n"
            + "        }\n"
            + "        optional {\n"
            + "            ?a <http://eurocris.org/ontology/cerif#has_endDate> ?e .\n"
            + "        }\n"
            + "    }\n"
            + "}").evaluate();
    List<Map.Entry<String, Literal>> mp = Lists.newArrayList();

    Set<String> hs = Sets.newConcurrentHashSet();

    while (evaluate.hasNext()) {
      BindingSet next = evaluate.next();
      for (Binding bind : next) {

        if (bind.getValue() instanceof Literal) {
          Literal lit = ((Literal) bind.getValue());
          Literal createLiteral = ValueFactoryImpl.getInstance().createLiteral(Normalizer.normalize(lit.stringValue(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").replaceAll("\\p{C}", ""), lit.getLanguage());
          String key = bind.getName().hashCode() + "_" + createLiteral.stringValue().hashCode();
          if (!hs.contains(key)) {
            mp.add(new AbstractMap.SimpleEntry<String, Literal>(bind.getName(), createLiteral));
            hs.add(key);
          }
        } else {
          //System.out.println(bind);
        }
      }
    }

    mp.add(new AbstractMap.SimpleEntry<String, Literal>("identifierURI", ValueFactoryImpl.getInstance().createLiteral(URIitem)));

    con.close();

    mpp.addAll(mp);

    for (Map.Entry<String, Literal> val : mp) {
      String field = val.getKey();
      // Qualified element?
      String qua = "";

      switch (field) {
        case "n":
          qua = "none";
          field = "eventsname";
          break;
        case "d":
          qua = "none";
          field = "eventsdescription";
          break;
        case "ab":
          qua = "none";
          field = "eventsacronym";
          break;
        case "l":
          qua = "none";
          field = "eventslocation";
          break;
        case "s":
          qua = "none";
          field = "eventsstartdate";
          break;
        case "e":
          qua = "none";
          field = "eventsenddate";
          break;
        case "k":
          qua = "none";
          field = "eventskeywords";
          break;
        default:
          qua = null;
          break;
      }
      Element valueElem = null;
      Element schema = getElement(metadata.getElement(), "crisevents");
      if (schema == null) {
        schema = create("crisevents");
        metadata.getElement().add(schema);
      }
      valueElem = schema;

      // Has element.. with XOAI one could have only schema and value
      if (field != null && !field.equals("")) {
        Element element = getElement(schema.getElement(),
                field);
        if (element == null) {
          element = create(field);
          schema.getElement().add(element);
        }
        valueElem = element;

        if (qua != null && !qua.equals("")) {
          Element qualifier = getElement(element.getElement(),
                  qua);
          if (qualifier == null) {
            qualifier = create(qua);
            element.getElement().add(qualifier);
          }
          valueElem = qualifier;
        }
      }

      // Language?
      if (val.getValue().getLanguage() != null && !val.getValue().getLanguage().equals("")) {
        Element language = getElement(valueElem.getElement(),
                val.getValue().getLanguage());
        if (language == null) {
          language = create(val.getValue().getLanguage());
          valueElem.getElement().add(language);
        }
        valueElem = language;
      } else {
        Element language = getElement(valueElem.getElement(),
                "none");
        if (language == null) {
          language = create("none");
          valueElem.getElement().add(language);
        }
        valueElem = language;
      }

      valueElem.getField().add(createValue("value", val.getValue().stringValue()));
    }

    addOtherRepository(metadata.getElement(), URIitem, idpub, "events");

    return metadata;
  }

  public Metadata retrieveMetadataPatents(String URIitem, String idpub, List<Map.Entry<String, Literal>> mpp) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
    Metadata metadata;

    // read all metadata into Metadata Object
    metadata = new Metadata();

    RepositoryConnection con = sparqlService.getRepositoryConnetion();

    TupleQueryResult evaluate = con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "select ?title ?abstract ?subject ?rd ?ad ?pn ?in {\n"
            + "    {\n"
            + "        select * { \n"
            + "            graph <" + constantService.getCentralGraph() + "> {\n"
            + "                bind(<" + URIitem + "> as ?a) .\n"
            + "                ?a dct:title ?title.\n"
            + "                ?a dct:abstract ?abstract.\n"
            + "                ?a dct:subject ?subject.\n"
            + "                ?a <http://www.eurocris.org/ontologies/cerif/1.3/registrationDate> ?rd .\n"
            + "                ?a <http://www.eurocris.org/ontologies/cerif/1.3/approvalDate> ?ad .\n"
            + "                ?a <http://www.eurocris.org/ontologies/cerif/1.3/patentNumber> ?pn .\n"
            + "            }\n"
            + "        } \n"
            + "    } union {\n"
            + "        select ?p (sample(?cn) as ?in) { \n"
            + "            graph <" + constantService.getCentralGraph() + "> {\n"
            + "                bind(<" + URIitem + "> as ?a) .\n"
            + "                ?a <http://www.eurocris.org/ontologies/cerif/1.3/linkToPerson> ?p .\n"
            + "                ?p foaf:name ?cn.\n"
            + "            }\n"
            + "        } group by ?p\n"
            + "    }\n"
            + "}").evaluate();
    List<Map.Entry<String, Literal>> mp = Lists.newArrayList();

    Set<String> hs = Sets.newConcurrentHashSet();

    while (evaluate.hasNext()) {
      BindingSet next = evaluate.next();
      for (Binding bind : next) {

        if (bind.getValue() instanceof Literal) {
          Literal lit = ((Literal) bind.getValue());
          Literal createLiteral = ValueFactoryImpl.getInstance().createLiteral(Normalizer.normalize(lit.stringValue(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").replaceAll("\\p{C}", ""), lit.getLanguage());
          String key = bind.getName().hashCode() + "_" + createLiteral.stringValue().hashCode();
          if (!hs.contains(key)) {
            mp.add(new AbstractMap.SimpleEntry<String, Literal>(bind.getName(), createLiteral));
            hs.add(key);
          }
        } else {
          //System.out.println(bind);
        }
      }
    }

    mp.add(new AbstractMap.SimpleEntry<String, Literal>("identifierURI", ValueFactoryImpl.getInstance().createLiteral(URIitem)));

    con.close();

    mpp.addAll(mp);

    Element item = create("item");
    metadata.getElement().add(item);
    Element cerifentitytype = create("cerifentitytype");
    item.getElement().add(cerifentitytype);
    Element create = create("none");
    cerifentitytype.getElement().add(create);
    create.getField().add(createValue("value", "Patents"));

    Element openairecristype = create("openairecristype");
    item.getElement().add(openairecristype);
    Element create2 = create("none");
    openairecristype.getElement().add(create2);
    create2.getField().add(createValue("value", "Patents"));

    int i = 0;
    for (Map.Entry<String, Literal> val : mp) {
      i++;
      String field = val.getKey();
      // Qualified element?
      String qua = "";
      boolean diffNone = false;

      switch (field) {
        case "title":
          qua = null;
          field = "title";
          break;
        case "abstract":
          qua = "abstract";
          field = "description";
          break;
        case "subject":
          qua = null;
          field = "subject";
          break;
        case "rd":
          qua = "issued";
          field = "date";
          break;
        case "ad":
          qua = null;
          field = "dateAccepted";
          break;
        case "pn":
          qua = "patentno";
          field = "identifier";
          break;
        case "in":
          qua = "author";
          field = "contributor";
          diffNone = true;
          break;
        default:
          qua = null;
          break;
      }
      Element valueElem = null;
      Element schema = getElement(metadata.getElement(), "dc");
      if (schema == null) {
        schema = create("dc");
        metadata.getElement().add(schema);
      }
      valueElem = schema;

      // Has element.. with XOAI one could have only schema and value
      if (field != null && !field.equals("")) {
        Element element = getElement(schema.getElement(),
                field);
        if (element == null) {
          element = create(field);
          schema.getElement().add(element);
        }
        valueElem = element;

        if (qua != null && !qua.equals("")) {
          Element qualifier = getElement(element.getElement(),
                  qua);
          if (qualifier == null) {
            qualifier = create(qua);
            element.getElement().add(qualifier);
          }
          valueElem = qualifier;
        }
      }

      // Language?
      if (val.getValue().getLanguage() != null && !val.getValue().getLanguage().equals("")) {
        Element language = getElement(valueElem.getElement(),
                val.getValue().getLanguage());
        if (language == null) {
          language = create(val.getValue().getLanguage());
          valueElem.getElement().add(language);
        }
        valueElem = language;
      } else {
        String noneNma = diffNone ? "none_" + i : "none";

        Element language = getElement(valueElem.getElement(),
                noneNma);
        if (language == null) {
          language = create(noneNma);
          valueElem.getElement().add(language);
        }
        valueElem = language;
      }

      valueElem.getField().add(createValue("value", val.getValue().stringValue()));
    }

    addOtherRepository(metadata.getElement(), URIitem, idpub, "item");

    return metadata;
  }

  public Metadata retrieveMetadataDatasets(String URIitem, String idpub, List<Map.Entry<String, Literal>> mpp) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
    Metadata metadata;

    // read all metadata into Metadata Object
    metadata = new Metadata();

    RepositoryConnection con = sparqlService.getRepositoryConnetion();

    TupleQueryResult evaluate = con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "PREFIX bibo: <http://purl.org/ontology/bibo/>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "select ?t ?abs ?subject ?doi ?uri ?in {\n"
            + "    {\n"
            + "        select * where { \n"
            + "            graph <" + constantService.getCentralGraph() + "> {\n"
            + "                bind (<" + URIitem + "> as ?a).\n"
            + "                ?a dct:title ?t .\n"
            + "                ?a bibo:abstract ?abs .\n"
            + "                ?a bibo:doi ?doi .\n"
            + "                ?a bibo:uri ?uri .\n"
            + "            }\n"
            + "        } \n"
            + "    } union {\n"
            + "        select ?p (sample(?na) as ?in) { \n"
            + "            graph <" + constantService.getCentralGraph() + "> {\n"
            + "                bind (<" + URIitem + "> as ?a).\n"
            + "                ?p foaf:publications ?a .\n"
            + "                ?p foaf:name ?na.\n"
            + "            }\n"
            + "        } group by ?p\n"
            + "    }\n"
            + "}").evaluate();
    List<Map.Entry<String, Literal>> mp = Lists.newArrayList();

    Set<String> hs = Sets.newConcurrentHashSet();

    while (evaluate.hasNext()) {
      BindingSet next = evaluate.next();
      for (Binding bind : next) {

        if (bind.getValue() instanceof Literal) {
          Literal lit = ((Literal) bind.getValue());
          Literal createLiteral = ValueFactoryImpl.getInstance().createLiteral(Normalizer.normalize(lit.stringValue(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").replaceAll("\\p{C}", ""), lit.getLanguage());
          String key = bind.getName().hashCode() + "_" + createLiteral.stringValue().hashCode();
          if (!hs.contains(key)) {
            mp.add(new AbstractMap.SimpleEntry<String, Literal>(bind.getName(), createLiteral));
            hs.add(key);
          }
        } else {
          //System.out.println(bind);
        }
      }
    }

    mp.add(new AbstractMap.SimpleEntry<String, Literal>("identifierURI", ValueFactoryImpl.getInstance().createLiteral(URIitem)));

    con.close();

    mpp.addAll(mp);

    Element item = create("item");
    metadata.getElement().add(item);
    Element cerifentitytype = create("cerifentitytype");
    item.getElement().add(cerifentitytype);
    Element create = create("none");
    cerifentitytype.getElement().add(create);
    create.getField().add(createValue("value", "Products"));

    Element openairecristype = create("openairecristype");
    item.getElement().add(openairecristype);
    Element create2 = create("none");
    openairecristype.getElement().add(create2);
    create2.getField().add(createValue("value", "Products"));

    int i = 0;
    for (Map.Entry<String, Literal> val : mp) {
      i++;
      String field = val.getKey();
      // Qualified element?
      String qua = "";
      boolean diffNone = false;

      switch (field) {
        case "t":
          qua = null;
          field = "title";
          break;
        case "abs":
          qua = null;
          field = "description";
          break;
        case "doi":
          qua = "doi";
          field = "identifier";
          break;
        case "uri":
          qua = "uri";
          field = "identifier";
          break;
        case "in":
          qua = "author";
          field = "contributor";
          diffNone = true;
          break;
        default:
          qua = null;
          break;
      }
      Element valueElem = null;
      Element schema = getElement(metadata.getElement(), "dc");
      if (schema == null) {
        schema = create("dc");
        metadata.getElement().add(schema);
      }
      valueElem = schema;

      // Has element.. with XOAI one could have only schema and value
      if (field != null && !field.equals("")) {
        Element element = getElement(schema.getElement(),
                field);
        if (element == null) {
          element = create(field);
          schema.getElement().add(element);
        }
        valueElem = element;

        if (qua != null && !qua.equals("")) {
          Element qualifier = getElement(element.getElement(),
                  qua);
          if (qualifier == null) {
            qualifier = create(qua);
            element.getElement().add(qualifier);
          }
          valueElem = qualifier;
        }
      }

      // Language?
      if (val.getValue().getLanguage() != null && !val.getValue().getLanguage().equals("")) {
        Element language = getElement(valueElem.getElement(),
                val.getValue().getLanguage());
        if (language == null) {
          language = create(val.getValue().getLanguage());
          valueElem.getElement().add(language);
        }
        valueElem = language;
      } else {
        String noneNma = diffNone ? "none_" + i : "none";

        Element language = getElement(valueElem.getElement(),
                noneNma);
        if (language == null) {
          language = create(noneNma);
          valueElem.getElement().add(language);
        }
        valueElem = language;
      }

      valueElem.getField().add(createValue("value", val.getValue().stringValue()));
    }

    addOtherRepository(metadata.getElement(), URIitem, idpub, "item");

    return metadata;
  }

  public Metadata retrieveMetadataProjects(String URIitem, String idpub, List<Map.Entry<String, Literal>> mpp) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
    Metadata metadata;

    // read all metadata into Metadata Object
    metadata = new Metadata();

    RepositoryConnection con = sparqlService.getRepositoryConnetion();

    TupleQueryResult evaluate = con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX schema: <http://schema.org/>\n"
            + "select * {\n"
            + "    {\n"
            + "        select ?pr (sample (?t) as ?title) (sample (?sd) as ?sdate) (sample (?ed) as ?edate) (concat(sample (?fm),'||',sample (?fn)) as ?fund)  { \n"
            + "            graph <" + constantService.getCentralGraph() + "> {\n"
            + "                bind (<" + URIitem + "> as ?pr) .\n"
            + "                ?pr dct:title ?t .\n"
            + "                ?pr <https://www.openaire.eu/cerif-profile/1.1/StartDate> ?sd .\n"
            + "                ?pr <https://www.openaire.eu/cerif-profile/1.1/EndDate> ?ed .\n"
            + "                ?pr foaf:fundedBy ?f .\n"
            + "                ?f foaf:name ?fn .\n"
            + "                bind (md5(str(?f)) as ?fm ) .\n"
            + "                bind (md5(str(?pr)) as ?prm ) .\n"
            + "            }\n"
            + "        } group by ?pr        \n"
            + "    } union {\n"
            + "        select ?m (sample (?out) as ?mdata) { \n"
            + "            graph <" + constantService.getCentralGraph() + "> {\n"
            + "                bind (<" + URIitem + "> as ?pr) .\n"
            + "                ?m <https://www.openaire.eu/cerif-profile/1.1/MemberOf> ?pr .\n"
            + "                ?m schema:memberOf ?org .\n"
            + "                bind (md5(str(?org)) as ?orgm ) .\n"
            + "                bind (md5(str(?m)) as ?mm ) .\n"
            + "                ?m foaf:name ?mn .\n"
            + "                ?org <http://ucuenca.edu.ec/ontology#memberOf> <https://redi.cedia.edu.ec/> .\n"
            + "                ?org foaf:name ?orgn .\n"
            + "                bind (concat(?mm, '||', ?mn) as ?out).\n"
            + "            }\n"
            + "        } group by ?m\n"
            + "    }\n"
            + "}").evaluate();
    List<Map.Entry<String, Literal>> mp = Lists.newArrayList();

    Set<String> hs = Sets.newConcurrentHashSet();

    while (evaluate.hasNext()) {
      BindingSet next = evaluate.next();
      for (Binding bind : next) {

        if (bind.getValue() instanceof Literal) {
          Literal lit = ((Literal) bind.getValue());
          Literal createLiteral = ValueFactoryImpl.getInstance().createLiteral(Normalizer.normalize(lit.stringValue(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").replaceAll("\\p{C}", ""), lit.getLanguage());
          String key = bind.getName().hashCode() + "_" + createLiteral.stringValue().hashCode();
          if (!hs.contains(key)) {
            mp.add(new AbstractMap.SimpleEntry<String, Literal>(bind.getName(), createLiteral));
            hs.add(key);
          }
        } else {
          //System.out.println(bind);
        }
      }
    }

    mp.add(new AbstractMap.SimpleEntry<String, Literal>("identifierURI", ValueFactoryImpl.getInstance().createLiteral(URIitem)));

    con.close();

    mpp.addAll(mp);
    Element valueElem = null;
    Element schema = getElement(metadata.getElement(), "crisproject");
    if (schema == null) {
      schema = create("crisproject");
      metadata.getElement().add(schema);
    }
    valueElem = schema;
    for (Map.Entry<String, Literal> val : mp) {
      String field = val.getKey();
      // Qualified element?
      String qua = "";

      switch (field) {
        case "title":
          qua = "none";
          field = "title";
          break;
        case "startdate":
          qua = "none";
          field = "eventsdescription";
          break;
        case "fund":
          qua = "none";
          field = "startdate";
          addOrgs(metadata, val.getValue().stringValue().replaceAll("\\|\\|", "\\|"), "crisproject", "coordinator");
          addOrgs(metadata, val.getValue().stringValue().replaceAll("\\|\\|", "\\|"), "crisproject", "funder");
          continue;
        case "mdata":
          qua = "none";
          field = "eventslocation";
          addPerson(metadata, val.getValue().stringValue(), "crisproject", "coinvestigators");
          continue;
        default:
          qua = null;
          continue;
      }

      // Has element.. with XOAI one could have only schema and value
      if (field != null && !field.equals("")) {
        Element element = getElement(schema.getElement(),
                field);
        if (element == null) {
          element = create(field);
          schema.getElement().add(element);
        }
        valueElem = element;

        if (qua != null && !qua.equals("")) {
          Element qualifier = getElement(element.getElement(),
                  qua);
          if (qualifier == null) {
            qualifier = create(qua);
            element.getElement().add(qualifier);
          }
          valueElem = qualifier;
        }
      }

      // Language?
      if (val.getValue().getLanguage() != null && !val.getValue().getLanguage().equals("")) {
        Element language = getElement(valueElem.getElement(),
                val.getValue().getLanguage());
        if (language == null) {
          language = create(val.getValue().getLanguage());
          valueElem.getElement().add(language);
        }
        valueElem = language;
      } else {
        Element language = getElement(valueElem.getElement(),
                "none");
        if (language == null) {
          language = create("none");
          valueElem.getElement().add(language);
        }
        valueElem = language;
      }

      valueElem.getField().add(createValue("value", val.getValue().stringValue()));
    }

    addOtherRepository(metadata.getElement(), URIitem, idpub, "project");

    return metadata;
  }

  public void addOrgs(Metadata md, String orgs, String ty, String auth) {

    Element element = getElement(md.getElement(), ty);

    Element dept = create(auth);
    element.getElement().add(dept);
    Element none = create("none");
    dept.getElement().add(none);

    String[] split = orgs.split("@");
    for (String org : split) {
      String[] orgd = org.split("\\|");
      Element noneOrg = create("none");
      none.getElement().add(noneOrg);
      noneOrg.getField().add(createValue("value", orgd[1]));
      noneOrg.getField().add(createValue("authority", orgd[0]));
      noneOrg.getField().add(createValue("confidence", "600"));

      Element authority = create("authority");
      noneOrg.getElement().add(authority);
      Element crisou = create("crisou");
      authority.getElement().add(crisou);
      Element crisouName = create("name");
      crisou.getElement().add(crisouName);
      Element crisouNameNone = create("none");
      crisouName.getElement().add(crisouNameNone);
      Element crisouNameNoneNone = create("none");
      crisouNameNone.getElement().add(crisouNameNoneNone);
      crisouNameNoneNone.getField().add(createValue("value", orgd[1]));

      addOtherRepository(authority.getElement(), orgd[0], orgd[0], "ou");

    }

  }

  public void addPerson(Metadata md, String orgs, String ty, String auth) {

    Element element = getElement(md.getElement(), ty);

    Element dept = create(auth);
    element.getElement().add(dept);
    Element none = create("none");
    dept.getElement().add(none);

    String[] split = orgs.split("@");
    for (String org : split) {
      String[] orgd = org.split("\\|\\|");
      Element noneOrg = create("none");
      none.getElement().add(noneOrg);
      noneOrg.getField().add(createValue("value", orgd[1]));
      noneOrg.getField().add(createValue("authority", orgd[0]));
      noneOrg.getField().add(createValue("confidence", "600"));

      Element authority = create("authority");
      noneOrg.getElement().add(authority);
      Element crisou = create("crisrp");
      authority.getElement().add(crisou);
      Element crisouName = create("fullName");
      crisou.getElement().add(crisouName);
      Element crisouNameNone = create("none");
      crisouName.getElement().add(crisouNameNone);
      Element crisouNameNoneNone = create("none");
      crisouNameNone.getElement().add(crisouNameNoneNone);
      crisouNameNoneNone.getField().add(createValue("value", orgd[1]));

      addOtherRepository(authority.getElement(), orgd[0], orgd[0], "rp");

    }

  }

  public void addOtherRepository(List<Element> mt, String URIitem, String idpub, String type) {

    // Other info
    Element other = create("others");

    other.getField().add(
            createValue("handle", URIitem));
    other.getField().add(
            createValue("identifier", idpub));
    other.getField().add(
            createValue("lastModifyDate", new Date().toString()));
    if (type != null) {
      other.getField().add(
              createValue("type", type));

    }

    mt.add(other);

    // Repository Info
    Element repository = create("repository");
    repository.getField().add(
            createValue("name",
                    "Repositorio Ecuatoriano de Investigadores"));
    repository.getField().add(
            createValue("mail",
                    "redi@cedia.edu.ec"));
    mt.add(repository);

  }

}
