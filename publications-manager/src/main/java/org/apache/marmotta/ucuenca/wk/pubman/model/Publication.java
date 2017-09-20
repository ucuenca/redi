/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.model;

import java.util.List;

/**
 *
 * @author cedia
 */
public class Publication {
    
    private String URI;
    private List<String> Titles;
    private List<String> Abstracts;

    public Publication(String URI, List<String> Titles, List<String> Abstracts) {
        this.URI = URI;
        this.Titles = Titles;
        this.Abstracts = Abstracts;
    }
    
    
    

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public List<String> getTitles() {
        return Titles;
    }

    public void setTitles(List<String> Titles) {
        this.Titles = Titles;
    }

    public List<String> getAbstracts() {
        return Abstracts;
    }

    public void setAbstracts(List<String> Abstracts) {
        this.Abstracts = Abstracts;
    }
    
    
    
    
    
    
}
