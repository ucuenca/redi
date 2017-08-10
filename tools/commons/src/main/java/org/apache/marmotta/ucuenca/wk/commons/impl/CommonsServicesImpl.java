/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.slf4j.Logger;

/**
 *
 * @author FernandoBac
 */
public class CommonsServicesImpl implements CommonsServices {

    @Inject
    private Logger log;
       
    /**
     * Función que elimina acentos y caracteres especiales
     *
     * @param value
     * @return cadena de texto limpia de acentos y caracteres especiales.
     */
    @Override
    public String removeAccents(String input) {
        // Cadena de caracteres original a sustituir.
        String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
        // Cadena de caracteres ASCII que reemplazarán los originales.
        String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
        String output = input;
        for (int i = 0; i < original.length(); i++) {
            // Reemplazamos los caracteres especiales.
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }//end for i
        return output;
    }
    
    @Override
    public String cleanNameArticles(String value) {
        value = value.replace(".", "").trim();
        value = value.replace("??", ".*");
        value = value.replace("?", ".*").toLowerCase();
        value = value.replaceAll(" de ", " ");
        value = value.replaceAll("^del ", " ");
        value = value.replaceAll(" del ", " ");
        value = value.replaceAll(" los ", " ");
        value = value.replaceAll(" y ", " ");
        value = value.replaceAll(" las ", " ");
        value = value.replaceAll(" la ", " ");
        value = value.replaceAll("^de ", " ");
        value = value.replaceAll("^los ", " ");
        value = value.replaceAll("^las ", " ");
        value = value.replaceAll("^la ", " ");
        
        return value;
    }

    /**
     * Return true or false if object is a URI.
     */
    @Override
    public Boolean isURI(String object) {
        URL url = null;
        try {
            url = new URL(object);
        } catch (Exception e1) {
            return false;
        }
        Pattern pat = Pattern.compile("^[hH]ttp(s?)");
        Matcher mat = pat.matcher(url.getProtocol());
        return mat.matches(); // return "http".equals(url.getProtocol()) || "https".equals(url.getProtocol()) ;
    }

    @Override
    public String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String readPropertyFromFile(String file,String property) {
        Properties propiedades = new Properties();
        InputStream entrada = null;
        ConcurrentHashMap<String, String> mapping = new ConcurrentHashMap<String, String>();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            entrada = classLoader.getResourceAsStream(file);
            propiedades.load(entrada);
            for (String source : propiedades.stringPropertyNames()) {
                String target = propiedades.getProperty(source);
                mapping.put(source, target);
            }
        } catch (IOException ex) {
            log.error("IOException in getReadPropertyFromFile CommonsServiceImpl " + ex);
        } catch (Exception ex) {
            log.error("Exception in getReadPropertyFromFile CommonsServiceImpl " + ex);
        } finally {
            if (entrada != null) {
                try {
                    entrada.close();
                } catch (IOException e) {
                    log.error("IOException un getReadPropertyFromFile line 106" + e);
                }
            }
        }
        return mapping.get(property);
    }

    @Override
    public String getIndexedPublicationsFilter(String querystr) {
        
        try {

            // Create path and index
            Path p1 = Paths.get("idxCentralGraph");
            FSDirectory index = FSDirectory.open(p1);

            //IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
            //IndexWriter writer = new IndexWriter(dir, config);
            // 0. Specify the analyzer for tokenizing text.
            //    The same analyzer should be used for indexing and searching
            StandardAnalyzer analyzer = new StandardAnalyzer();

            // 1. query
            Query q = new QueryParser("title", analyzer).parse(querystr);

            // 2. search
            int hitsPerPage = 20;
            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(q, hitsPerPage);
            ScoreDoc[] hits = docs.scoreDocs;

            // 3. display results
            String filter = "";
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                if (i == 0) {
                    filter = "regex(str(?publicationUri), \"" + d.get("id") + "\" )";
                } else {
                    filter += "|| regex(str(?publicationUri), \"" + d.get("id") + "\" )";  
                }
            }

            // reader can only be closed when there
            // is no need to access the documents any more.
            reader.close();
            
            
            return filter;
        } catch (InvalidArgumentException ex) {
            return "error:  " + ex;
        } catch (IOException ex) {
            return "error:  " + ex;
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(CommonsServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
}
