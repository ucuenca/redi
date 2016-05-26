/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.provider.ma.util;

import java.util.List;

/**
 *
 * @author f35
 */
public class Publication {

    private String source;
    private String id;
    private String title;
    private String abstractt;
    private List<Author> authors;
    private String year;
    private List<String> keyWord;
    private String type;
    private String journal;
    private List<String> fullVersionURL;
    private String citationCount;
    private String doi;
    private String referenceCount;

    public String getReferenceCount() {
        return referenceCount;
    }

    public void setReferenceCount(String referenceCount) {
        this.referenceCount = referenceCount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public List<String> getFullVersionURL() {
        return fullVersionURL;
    }

    public void setFullVersionURL(List<String> fullVersionURL) {
        this.fullVersionURL = fullVersionURL;
    }

    public String getCitationCount() {
        return citationCount;
    }

    public void setCitationCount(String citationCount) {
        this.citationCount = citationCount;
    }

    public String getDOI() {
        return doi;
    }

    public void setDOI(String doi) {
        this.doi = doi;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstract() {
        return abstractt;
    }

    public void setAbstract(String abstractt) {
        this.abstractt = abstractt;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public List<String> getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(List<String> keyWord) {
        this.keyWord = keyWord;
    }

    @Override
    public String toString() {
        return "Publication{" + "ID=" + id + ", Title=" + title + ", Abstract=" + abstractt + ", Authors=" + authors + ", Year=" + year + ", KeyWord=" + keyWord + '}';
    }

}
