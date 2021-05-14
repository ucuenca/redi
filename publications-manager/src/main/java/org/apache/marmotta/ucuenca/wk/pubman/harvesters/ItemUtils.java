/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.apache.marmotta.ucuenca.wk.pubman.harvesters;

//import com.google.common.collect.Lists;
//import com.google.common.collect.Sets;
//
//import com.lyncode.xoai.dataprovider.xml.xoai.Element;
//import com.lyncode.xoai.dataprovider.xml.xoai.Metadata;
//import java.util.AbstractMap;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//import org.openrdf.model.Literal;
//import org.openrdf.model.impl.ValueFactoryImpl;
//import org.openrdf.query.Binding;
//import org.openrdf.query.BindingSet;
//import org.openrdf.query.MalformedQueryException;
//import org.openrdf.query.QueryEvaluationException;
//import org.openrdf.query.QueryLanguage;
//import org.openrdf.query.TupleQueryResult;
//import org.openrdf.repository.RepositoryConnection;
//import org.openrdf.repository.RepositoryException;
//import org.openrdf.repository.sparql.SPARQLRepository;

/**
 *
 * @author Lyncode Development Team <dspace@lyncode.com>
 */
@SuppressWarnings("deprecation")
public class ItemUtils {

//  //private static final Logger log = LogManager.getLogger(ItemUtils.class);
//
//  private static Element getElement(List<Element> list, String name) {
//    for (Element e : list) {
//      if (name.equals(e.getName())) {
//        return e;
//      }
//    }
//
//    return null;
//  }
//
//  private static Element create(String name) {
//    Element e = new Element();
//    e.setName(name);
//    return e;
//  }
//
//  private static Element.Field createValue(
//          String name, String value) {
//    Element.Field e = new Element.Field();
//    e.setValue(value);
//    e.setName(name);
//    return e;
//  }
//
//  public static Metadata retrieveMetadata(SPARQLRepository gRepository, String URIitem, String idpub, List<Entry<String, Literal>> mpp) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
//    Metadata metadata;
//
//    // read all metadata into Metadata Object
//    metadata = new Metadata();
//
//    RepositoryConnection con = gRepository.getConnection();
//
//    TupleQueryResult evaluate = con.prepareTupleQuery(QueryLanguage.SPARQL, "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
//            + "PREFIX bibo: <http://purl.org/ontology/bibo/>\n"
//            + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
//            + "prefix dct: <http://purl.org/dc/terms/>\n"
//            + "prefix foaf: <http://xmlns.com/foaf/0.1/>\n"
//            + "\n"
//            + "select distinct ?title ?description ?subject ?publisher ?date ?type ?language ?creator ?doi  { \n"
//            + "    {\n"
//            + "        select distinct ?identifier ?title ?description ?subject ?publisher ?date ?type ?language ?doi {\n"
//            + "            graph <https://redi.cedia.edu.ec/context/redi> {\n"
//            + "                bind (<" + URIitem + "> as ?identifier) .\n"
//            + "                ?identifier dct:title ?title .\n"
//            + "                ?identifier a ?typex.\n"
//            + "                bind(str(?typex) as ?type).\n"
//            + "                {?identifier bibo:abstract ?description . }\n"
//            + "                union {?identifier dct:subject ?s . ?s rdfs:label ?subject. }\n"
//            + "                union {?identifier dct:publisher ?p . ?p rdfs:label ?publisher. }\n"
//            + "                union {?identifier <http://ns.nature.com/terms/coverDate> ?date. }\n"
//            + "                union {?identifier <http://purl.org/dc/terms/language> ?language. }\n"
//            + "                union {?identifier bibo:doi ?doi. }\n"
//            + "            }\n"
//            + "		}\n"
//            + "    } union\n"
//            + "    {\n"
//            + "        select ?identifier ?au (sample(?namex) as ?creator) { \n"
//            + "            graph <https://redi.cedia.edu.ec/context/redi> {\n"
//            + "                bind (<" + URIitem + "> as ?identifier) .\n"
//            + "                ?au foaf:publications ?identifier .\n"
//            + "                ?au foaf:name ?namex .\n"
//            + "            }\n"
//            + "        }  group by ?identifier ?au\n"
//            + "    } .\n"
//            + "}").evaluate();
//    List<Entry<String, Literal>> mp = Lists.newArrayList();
//
//    Set<String> hs = Sets.newConcurrentHashSet();
//    
//    while (evaluate.hasNext()) {
//      BindingSet next = evaluate.next();
//      for (Binding bind : next) {
//
//        if (bind.getValue() instanceof Literal) {
//          String key = bind.getName().hashCode()+"_"+((Literal) bind.getValue()).stringValue().hashCode();
//          if (!hs.contains(key)){
//            mp.add(new AbstractMap.SimpleEntry<String, Literal>(bind.getName(), (Literal) bind.getValue()));
//            hs.add(key);
//          }
//          
//        }else {
//          System.out.println(bind);
//        }
//      }
//    }
//    
//    
//    mp.add(new AbstractMap.SimpleEntry<String, Literal>("identifierURI", ValueFactoryImpl.getInstance().createLiteral(URIitem)));
//
//    con.close();
//    
//    mpp.addAll(mp);
//
//    for (Entry<String, Literal> val : mp) {
//      String field = val.getKey();
//        // Qualified element?
//        String qua = "";
//
//        switch (field) {
//          case "identifierURI":
//            qua = "uri";
//            field = "identifier";
//            break;
//          case "doi":
//            qua = "doi";
//            field = "identifier";
//            break;
//          case "creator":
//            qua = "author";
//            field = "contributor";
//            break;
//          case "language":
//            qua = "iso";
//            field = "language";
//            val.setValue(ValueFactoryImpl.getInstance().createLiteral(val.getValue().stringValue(), "es_ES"));
//            break;
//          case "date":
//            qua = "issued";
//            field = "date";
//            break;
//            
//          default:
//            qua = null;
//            break;
//        }
//      Element valueElem = null;
//      Element schema = getElement(metadata.getElement(), "dc");
//      if (schema == null) {
//        schema = create("dc");
//        metadata.getElement().add(schema);
//      }
//      valueElem = schema;
//
//      // Has element.. with XOAI one could have only schema and value
//      if (field != null && !field.equals("")) {
//        Element element = getElement(schema.getElement(),
//                field);
//        if (element == null) {
//          element = create(field);
//          schema.getElement().add(element);
//        }
//        valueElem = element;
//
//
//
//        if (qua != null && !qua.equals("")) {
//          Element qualifier = getElement(element.getElement(),
//                  qua);
//          if (qualifier == null) {
//            qualifier = create(qua);
//            element.getElement().add(qualifier);
//          }
//          valueElem = qualifier;
//        }
//      }
//
//      // Language?
//      if (val.getValue().getLanguage() != null && !val.getValue().getLanguage().equals("")) {
//        Element language = getElement(valueElem.getElement(),
//                val.getValue().getLanguage());
//        if (language == null) {
//          language = create(val.getValue().getLanguage());
//          valueElem.getElement().add(language);
//        }
//        valueElem = language;
//      } else {
//        Element language = getElement(valueElem.getElement(),
//                "none");
//        if (language == null) {
//          language = create("none");
//          valueElem.getElement().add(language);
//        }
//        valueElem = language;
//      }
//
//      valueElem.getField().add(createValue("value", val.getValue().stringValue()));
//    }
//    // Done! Metadata has been read!
//    // Now adding bitstream info
//    Element bundles = create("bundles");
//    metadata.getElement().add(bundles);
//
//    String[] bs = new String[]{URIitem};
//    for (String b : bs) {
//      Element bundle = create("bundle");
//      bundles.getElement().add(bundle);
//      bundle.getField()
//              .add(createValue("name", "ORIGINAL"));
//
//      Element bitstreams = create("bitstreams");
//      bundle.getElement().add(bitstreams);
//      for (String bit : bs) {
//        Element bitstream = create("bitstream");
//        bitstreams.getElement().add(bitstream);
//        String url = "";
//        String cks = "0";
//        String cka = "NONE";
//        String oname = idpub;
//        String name = idpub;
//        String description = idpub;
//
//        if (name != null) {
//          bitstream.getField().add(
//                  createValue("name", name));
//        }
//        if (oname != null) {
//          bitstream.getField().add(
//                  createValue("originalName", name));
//        }
//        if (description != null) {
//          bitstream.getField().add(
//                  createValue("description", description));
//        }
//        bitstream.getField().add(
//                createValue("format", "text/html"));
//        bitstream.getField().add(
//                createValue("size", "0"));
//        bitstream.getField().add(createValue("url", url));
//        bitstream.getField().add(
//                createValue("checksum", cks));
//        bitstream.getField().add(
//                createValue("checksumAlgorithm", cka));
//        bitstream.getField().add(
//                createValue("sid", "1"));
//      }
//    }
//
//    // Other info
//    Element other = create("others");
//
//    other.getField().add(
//            createValue("handle", URIitem));
//    other.getField().add(
//            createValue("identifier", idpub));
//    other.getField().add(
//            createValue("lastModifyDate", new Date().toString()));
//    metadata.getElement().add(other);
//
//    // Repository Info
//    Element repository = create("repository");
//    repository.getField().add(
//            createValue("name",
//                    "Repositorio Ecuatoriano de Investigadores"));
//    repository.getField().add(
//            createValue("mail",
//                    "redi@cedia.edu.ec"));
//    metadata.getElement().add(repository);
//
//    return metadata;
//  }
}
