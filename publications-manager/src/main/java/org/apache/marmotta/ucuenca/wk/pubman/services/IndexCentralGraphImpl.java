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
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
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
            FSDirectory index = FSDirectory.open(p1.toFile());

            //  Specify the analyzer for tokenizing text.
            //    The same analyzer should be used for indexing and searching
            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);

            // Create the index
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_43, analyzer);

            IndexWriter w = new IndexWriter(index, config);
            
            List<Map<String, Value>> resultPublications = sparqlService.query(QueryLanguage.SPARQL, queriesService.getPublicationsTitlesQuery());

            for (Map<String, Value> pubresource : resultPublications) {
                String publicationResource = pubresource.get("pub").stringValue();
                String publicationTitle = StringEscapeUtils.unescapeJava(pubresource.get("title").stringValue());
                String publicationAbstract = StringEscapeUtils.unescapeJava(pubresource.get("abstract").stringValue());
                addDoc(w, publicationTitle + " " + publicationAbstract, publicationResource);
            }
            
            w.close();
            
            return "La indexacion de las publicaciones se han cargado exitosamente.";
        } catch (InvalidArgumentException ex) {
            return "error:  " + ex;
        } catch (MarmottaException ex) {
            return "error:  " + ex;
        } catch (IOException ex) {
            return "error:  " + ex;
        } /*catch (ParseException ex) {
            java.util.logging.Logger.getLogger(IndexCentralGraphImpl.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        //return "";
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
