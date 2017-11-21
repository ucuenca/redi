/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.marmotta.ucuenca.wk.pubman.services;

/**
 *
 * @author joe
 */
public class AuthorsInfo {
    
    public String identifier;
    public String [] name;
    public String [] afiliation ;
    public String [] coautors;
    public String [] topics;
    public String [] articles;
    
    public AuthorsInfo( String resourceId) {
      this.identifier = resourceId;
    }

    public AuthorsInfo(String identifier, String[] name, String[] afiliation, String[] coautors, String[] topics, String[] articles) {
        this.identifier = identifier;
        this.name = name;
        this.afiliation = afiliation;
        this.coautors = coautors;
        this.topics = topics;
        this.articles = articles;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String[] getName() {
        return name;
    }

    public void setName(String[] name) {
        this.name = name;
    }

    public String[] getAfiliation() {
        return afiliation;
    }

    public void setAfiliation(String[] afiliation) {
        this.afiliation = afiliation;
    }

    public String[] getCoautors() {
        return coautors;
    }

    public void setCoautors(String[] coautors) {
        this.coautors = coautors;
    }

    public String[] getTopics() {
        return topics;
    }

    public void setTopics(String[] topics) {
        this.topics = topics;
    }

    public String[] getArticles() {
        return articles;
    }

    public void setArticles(String[] articles) {
        this.articles = articles;
    }
    
   
    
}
