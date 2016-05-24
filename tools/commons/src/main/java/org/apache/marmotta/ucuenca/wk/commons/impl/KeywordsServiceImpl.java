/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.marmotta.ucuenca.wk.commons.service.KeywordsService;

/**
 *
 * @author FernandoBac
 */
public class KeywordsServiceImpl implements KeywordsService {

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
        for (String key : text.split(" ")) {
            if (!isArticle(key) && key.length() > minletters) {
                keywords.add(key);
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

    public boolean isArticle(String text) {
        String[] articlesEs = {"modelo","del", "de", "el", "la", "los", "en", "con", "por", "que", "sin", "ellos", "aquellos", "las"};
        for (String article : articlesEs) {
            if (text.toLowerCase().compareTo(article) == 0) {
                return true;
            }
        }
        String[] articlesEn = {"model","lower","hight","object","for", "the", "in", "a", "an", "with", "is", "to", "and", "of", "high", "to", "any", "on"};
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
        text = commonsservices.removeAccents(text);
        return text;
    }
}
