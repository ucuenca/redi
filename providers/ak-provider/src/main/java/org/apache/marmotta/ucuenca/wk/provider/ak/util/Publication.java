/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.provider.ak.util;

import java.util.List;

/**
 *
 * @author Freddy Sumba
 */
public class Publication {

    private String source;
    private long id;
    private String title;
    private String abstractt;
    private String year;
    private String created;
    private String type;
    private String citationCount;
    private String estimatedCitationcount;
    private String doi;
    private String conference;
    private List<String> keyWord;
    private List<String> fields;
    private List<String> referencesId;
    private List<Author> authors;
    private List<String> sources;
    private List<String> journals;

    public List<String> getJournals() {
        return journals;
    }

    public void setJournals(List<String> journals) {
        this.journals = journals;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEstimatedCitationcount() {
        return estimatedCitationcount;
    }

    public void setEstimatedCitationcount(String estimatedCitationcount) {
        this.estimatedCitationcount = estimatedCitationcount;
    }


    public String getAbstractt() {
        return abstractt;
    }

    public void setAbstractt(String abstractt) {
        this.abstractt = abstractt;
    }

    public String getDatePublication() {
        return created;
    }

    public void setDatePublication(String datePublication) {
        this.created = datePublication;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getConference() {
        return conference;
    }

    public void setConference(String conference) {
        this.conference = conference;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public List<String> getReferencesId() {
        return referencesId;
    }

    public void setReferencesId(List<String> referencesId) {
        this.referencesId = referencesId;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
