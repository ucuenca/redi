/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.DistanceService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.ucuenca.wk.pubman.api.IndexCentralGraph;
import org.apache.marmotta.ucuenca.wk.pubman.api.SparqlFunctionsService;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;
import org.slf4j.Logger;

/**
 *
 * @author Jose Luis Cullcay
 */
@ApplicationScoped
public class IndexCentralGraphImpl implements IndexCentralGraph, Runnable {

    @Inject
    private SparqlService sparqlService;
    
    @Inject
    private Logger log;
    
    @Inject
    private QueriesService queriesService;
    
    @Inject
    private ConstantService constant;
    
    @Inject
    private CommonsServices commonsServices;
    
    @Inject
    private DistanceService distanceService;
    
    @Inject
    private SparqlFunctionsService sparqlFunctionsService;
    
    @Override
    public String LoadIndexCentralGraph() {
        try {

            // Create path and index
            Path p1 = Paths.get("idxCentralGraph");
            FSDirectory index = FSDirectory.open(p1);

            //IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
            //IndexWriter writer = new IndexWriter(dir, config);
            // 0. Specify the analyzer for tokenizing text.
            //    The same analyzer should be used for indexing and searching
            StandardAnalyzer analyzer = new StandardAnalyzer();

            // 1. create the index
            //Directory index = new TextFileIndexer("");
            IndexWriterConfig config = new IndexWriterConfig(analyzer);

            IndexWriter w = new IndexWriter(index, config);
            /*addDoc(w, "Lucene in Action", "193398817");
            addDoc(w, "Lucene for Dummies", "55320055Z");
            addDoc(w, "Managing Gigabytes", "55063554A");
            addDoc(w, "The Art of Computer Science", "9900333X");*/
            
            
            List<Map<String, Value>> resultPublications = sparqlService.query(QueryLanguage.SPARQL, queriesService.getPublicationsTitlesQuery());

            for (Map<String, Value> pubresource : resultPublications) {
                String publicationResource = pubresource.get("pub").stringValue();
                String publicationTitle = StringEscapeUtils.unescapeJava(pubresource.get("title").stringValue());
                addDoc(w, publicationTitle, publicationResource);
            }
            
            w.close();
            
            // 2. query
            String querystr = "semantic";

            // the "title" arg specifies the default field to use
            // when no field is explicitly specified in the query.
            Query q = new QueryParser("title", analyzer).parse(querystr);

            // 3. search
            int hitsPerPage = 10;
            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(q, hitsPerPage);
            ScoreDoc[] hits = docs.scoreDocs;

            // 4. display results
            //System.out.println("Found " + hits.length + " hits.");
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                System.out.println((i + 1) + ". " + d.get("id") + "\t" + d.get("title"));
            }

            // reader can only be closed when there
            // is no need to access the documents any more.
            reader.close();
            
            
            return "La indexacion de las publicaciones se han cargado exitosamente.";
        } catch (InvalidArgumentException ex) {
            return "error:  " + ex;
        } catch (MarmottaException ex) {
            return "error:  " + ex;
        } catch (IOException ex) {
            return "error:  " + ex;
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(IndexCentralGraphImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    @Override
    public void run() {
        LoadIndexCentralGraph();
    }
    
    private static void addDoc(IndexWriter w, String title, String id) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("title", title, Field.Store.YES));

        // use a string field for isbn because we don't want it tokenized
        doc.add(new StringField("id", id, Field.Store.YES));
        w.addDocument(doc);
    }
    
}
