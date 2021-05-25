/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.harvesters;

//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.lyncode.xoai.dataprovider.exceptions.WritingXmlException;
//import com.lyncode.xoai.dataprovider.xml.XmlOutputContext;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.util.Date;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.concurrent.ConcurrentHashMap;
//import static org.apache.marmotta.ucuenca.wk.pubman.harvesters.ItemUtils.retrieveMetadata;
//import org.apache.solr.client.solrj.SolrServer;
//import org.apache.solr.client.solrj.SolrServerException;
//import org.apache.solr.client.solrj.impl.HttpSolrServer;
//import org.apache.solr.common.SolrInputDocument;
//import org.netlib.lapack.Second;
//import org.openrdf.query.BindingSet;
//import org.openrdf.query.MalformedQueryException;
//import org.openrdf.query.QueryEvaluationException;
//import org.openrdf.query.QueryLanguage;
//import org.openrdf.query.TupleQueryResult;
//import org.openrdf.repository.RepositoryConnection;
//import org.openrdf.repository.RepositoryException;
//import org.openrdf.repository.sparql.SPARQLRepository;
//import com.lyncode.xoai.dataprovider.core.Granularity;
//import java.util.List;
//import org.openrdf.model.Literal;
/**
 *
 * @author cedia
 */
public class indexOAI {

//  public static SPARQLRepository gRepository(String dbs) throws RepositoryException {
//    final SPARQLRepository data = new SPARQLRepository("http://201.159.222.25:8180/repositories/" + dbs, "http://201.159.222.25:8180/repositories/" + dbs + "/statements");
//    ConcurrentHashMap<String, String> additionalHttpHeaders = new ConcurrentHashMap<>();
//    additionalHttpHeaders.put("Accept", "application/sparql-results+json,*/*;q=0.9");
//    data.setAdditionalHttpHeaders(additionalHttpHeaders);
//    data.setUsernameAndPassword("rediclon", "");
//    data.initialize();
//
//    return data;
//  }
//
//  private static Map<String, String> getPublications(SPARQLRepository gRepository) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
//    Map<String, String> mp = Maps.newConcurrentMap();
//    RepositoryConnection connection = gRepository.getConnection();
//
//    TupleQueryResult evaluate = connection.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
//            + "prefix dct: <http://purl.org/dc/terms/>\n"
//            + "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
//            + "select distinct ?p ?pid{\n"
//            + "    graph <https://redi.cedia.edu.ec/context/redi> {\n"
//            + "        ?org <http://ucuenca.edu.ec/ontology#memberOf> <https://redi.cedia.edu.ec/> .\n"
//            + "        ?res <http://schema.org/memberOf> ?org .\n"
//            + "        ?res foaf:publications ?p .\n"
//            + "        filter (regex(str(?p),'resource/publication','i')) .\n"
//            + "        bind(REPLACE (str(?p), 'https://redi.cedia.edu.ec/resource/publication/', '') as ?pid).\n"
//            + "    }\n"
//            + "}").evaluate();
//
//    while (evaluate.hasNext()) {
//      BindingSet next = evaluate.next();
//      String p = next.getBinding("p").getValue().stringValue();
//      String pid = next.getBinding("pid").getValue().stringValue();
//      mp.put(p, pid);
//    }
//
//    connection.close();
//    return mp;
//  }
//
//  /**
//   * @param args the command line arguments
//   */
//  public static void main(String[] args) throws SolrServerException, IOException, WritingXmlException, RepositoryException, MalformedQueryException, QueryEvaluationException, javax.xml.stream.XMLStreamException {
//    // TODO code application logic here
//    SPARQLRepository gRepository = gRepository("redi");
//    SolrServer httpSolrServer = new HttpSolrServer("http://redi.cedia.edu.ec/solr/oai");
//
//    Map<String, String> publications = getPublications(gRepository);
//
//    int i=0;
//    for (Entry<String, String> ent : publications.entrySet()) {
//      SolrInputDocument doc = new SolrInputDocument();
//      doc.addField("item.id", ent.getValue());
//      doc.addField("item.handle", ent.getKey());
//      doc.addField("item.identifier", ent.getKey());
//      doc.addField("item.public", true);
//      doc.addField("item.type", "item");
//      doc.addField("item.deleted", false);
//      doc.addField("item.lastmodified", new Date());
//      doc.addField("item.submitter", "jose.ortizv@ucuenca.edu.ec");
//      doc.addField("item.collections", "col_redi");
//      doc.addField("item.communities", "com_redi");
//
//      
//      List<Entry<String, Literal>> vals = Lists.newArrayList();
//      ByteArrayOutputStream out = new ByteArrayOutputStream();
//      XmlOutputContext xmlContext = XmlOutputContext.emptyContext(out, Granularity.Second);
//      retrieveMetadata(gRepository, ent.getKey(), ent.getValue(), vals).write(xmlContext);
//      xmlContext.getWriter().flush();
//      xmlContext.getWriter().close();
//      
//      for (Entry<String, Literal> dat: vals){
//        switch(dat.getKey()){
//          case "title":
//            doc.addField("metadata.dc.title", dat.getValue().stringValue());
//            break;
//          case "creator":
//            doc.addField("metadata.dc.contributor.author", dat.getValue().stringValue());
//            break;
//          case "doi":
//            doc.addField("metadata.dc.identifier.doi", dat.getValue().stringValue());
//            break;
//          case "date":
//            doc.addField("metadata.dc.date.issued", dat.getValue().stringValue());
//            doc.addField("metadata.dc.date.available", dat.getValue().stringValue());
//            doc.addField("metadata.dc.date.accessioned", dat.getValue().stringValue());
//            break;
//        }
//      }
//      
//      doc.addField("metadata.dc.identifier.uri", ent.getKey());
//      doc.addField("metadata.dc.identifier.url", ent.getKey());
//      
//      
//      
//      
//      doc.addField("item.compile", out.toString());
//      httpSolrServer.add(doc);
//      
//      System.out.println(i+"/"+publications.size());
//      
//      i++;
//      
//    }
//    httpSolrServer.commit();
//
//  }

}
