/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.model;

import java.util.List;

/**
 *
 * @author José Ortiz
 */
public class JournalLatindex {

    private String URI;
    private String Name;
    private String ISSN;
    private List<String> Topics;
    private int StartYear;

    public JournalLatindex(String URI, String Name, String ISSN, List<String> Topics, int StartYear) {
        this.URI = URI;
        this.Name = Name;
        this.ISSN = ISSN;
        this.Topics = Topics;
        this.StartYear = StartYear;
    }

    
    
    
    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getISSN() {
        return ISSN;
    }

    public void setISSN(String ISSN) {
        this.ISSN = ISSN;
    }

    public List<String> getTopics() {
        return Topics;
    }

    public void setTopics(List<String> Topics) {
        this.Topics = Topics;
    }

    public int getStartYear() {
        return StartYear;
    }

    public void setStartYear(int StartYear) {
        this.StartYear = StartYear;
    }
    
    
    

}
