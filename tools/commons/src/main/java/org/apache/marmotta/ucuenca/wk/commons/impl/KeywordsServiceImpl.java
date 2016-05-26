/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.commons.service.KeywordsService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.DistanceService;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;

/**
 *
 * @author FernandoBac
 */
public class KeywordsServiceImpl implements KeywordsService {

    //@Inject
    private QueriesService queriesService = new QueriesServiceImpl();

    @Inject
    private SparqlService sparqlService;

    private final static int MAXKEYWORDS = 10; //maximo retorna 10 keywords desde el texto ( primeras )

    @Override
    public List<String> getKeywords(String abstracttext, String titletext) throws IOException, ClassNotFoundException {

        abstracttext = cleaningText(abstracttext);
        titletext = cleaningText(titletext);

        List<String> keyAbstract = new ArrayList<String>();
        List<String> keyTitle = new ArrayList<String>();
        List<String> keywords = new ArrayList<String>();

        keyAbstract = splitKeywords(abstracttext, 2);
        keyTitle = splitKeywords(titletext, 2);

        for (String keyabs : keyAbstract) {
            for (String keytit : keyTitle) {
                if (keytit.compareTo(keyabs) == 0) {
                    keywords.add(keytit);
                }
            }
        }
        return keywords;
    }

    public List<String> splitKeywords(String text, int minletters) {
        List<String> keywords = new ArrayList();
        int i = 0;

        for (String key : text.split(" ")) {
            if ((!isConstant(key)) && (key.length() > minletters)) {
                keywords.add(key);
            }
            i++;
            if (i == MAXKEYWORDS) {
                return keywords;
            }
        }
        return keywords;
    }

    @Override
    public List<String> getKeywords(String text) throws IOException, ClassNotFoundException {
        text = cleaningText(text);
        List<String> keywords = new ArrayList<String>();
        keywords = splitKeywords(text, 3);
        return keywords;
    }

    @Override
    public List<String> getKeywordsOfAuthor(String authorUri) {
        List<String> keywords = new ArrayList<>();
        try {
            String getAllKeywordsQuery = queriesService.getAuthorsKeywordsQuery(authorUri);
            List<Map<String, Value>> resultKeywords = sparqlService.query(QueryLanguage.SPARQL, getAllKeywordsQuery);
            int i = 0;
            for (Map<String, Value> key : resultKeywords) {
                String keyword = key.get("keyword").stringValue();
                keyword = cleaningText(keyword);
                if (!isConstant(keyword)) {
                    keywords.add(keyword);
                }
            }
            DistanceService distance = new DistanceServiceImpl();
            String analizedKeyword = keywords.get(0);
            List<String> keywordsG1 = new ArrayList<>();
            List<String> keywordsG2 = new ArrayList<>();
            int sumG1 = 0;
            int sumG2 = 0;
            for (String key : keywords) {

                List<String> onlyKeyword2 = new ArrayList<>();
                onlyKeyword2.add(key);
                if (distance.semanticComparison(analizedKeyword, onlyKeyword2)) {
                    keywordsG1.add(keywords.get(i));
                    sumG1++;
                } else {
                    keywordsG2.add(keywords.get(i));
                    sumG2++;

                }
                i++;
                if (i == MAXKEYWORDS) {
                    if (sumG1 == 0) {
                        return keywordsG2;
                    }
                    if (sumG2 == 0) {
                        return keywordsG1;
                    }
                    return keywordsG1;
                }
            }
            return keywordsG1;
        } catch (MarmottaException ex) {
            Logger.getLogger(KeywordsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return keywords;
    }


    public boolean isConstant(String text) {
        //estas palabras deben ir en recursos en el archivo de configuracion de este modulo 
        String[] articlesEs = {"promas","tesis","etapa","al", "a", "modelo", "cuanto", "porque", "cuales", "cuando", "debe", "donde", "del", "lo", "mayor", "menor", "nueva", "nuevo", "otros", "otras", "objeto", "por", "problema", "resumen", "todo", "tanto", "su", "se", "ve", "mas", "vez", "de", "el", "la", "los", "en", "con", "por", "que", "sin", "ellos", "aquellos", "las", "cuenca", "ecuador", "promas", "quito", "guayaquil", "paute", "cajas", ""};
        for (String word : articlesEs) {
            if (word.contains("-") || word.contains("_")) {
                return true;
            }
            if (text.toLowerCase().compareTo(word) == 0) {
                return true;
            }
        }
        String[] articlesEn = {"a", "are", "an", "abstract", "been", "by", "change", "chapter", "challenging", "do", "dont", "don't", "for", "has", "into", "model", "moreover", "lower", "hight", "of", "object", "problems", "related", "they", "that", "the", "we", "what", "when", "where", "with", "for", "the", "in", "a", "an", "with", "is", "to", "and", "of", "high", "to", "any", "on", "cuenca", "ecuador"};
        for (String article : articlesEn) {
            if (text.toLowerCase().compareTo(article) == 0) {
                return true;
            }
        }
        return false;

    }

    public String cleaningText(String text) {
        CommonsServicesImpl commonsservices = new CommonsServicesImpl();
        text = text.replace(".", "");
        text = text.replace("-", "");
        text = text.replace("_", "");
        text = text.replace("?", "");
        text = text.replace("\"", "");
        text = text.replace("^", "");
        text = text.replace("%", "");
        text = text.replace("#", "");
        text = text.replace("TESIS DE MAESTRIA EN", "");
        text = text.replace("TESIS DE", "");
        text = text.replace("TESIS EN", "");
        text = text.replace("TESIS", "");
        text = text.replace("MAESTRIA", "");
        text = text.replace("FACULTAD", "");
        text = text.replace("PUBLICA", "");

        text = commonsservices.removeAccents(text);
        return text;
    }
}
