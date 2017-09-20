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
public class Journal {
    
    private String Name;
    private String URI;
    private List<Publication> Publications;
    private JournalLatindex sameAs;

    public Journal(String Name, String URI, List<Publication> Publications, JournalLatindex sameAs) {
        this.Name = Name;
        this.URI = URI;
        this.Publications = Publications;
        this.sameAs = sameAs;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public List<Publication> getPublications() {
        return Publications;
    }

    public void setPublications(List<Publication> Publications) {
        this.Publications = Publications;
    }

    public JournalLatindex getSameAs() {
        return sameAs;
    }

    public void setSameAs(JournalLatindex sameAs) {
        this.sameAs = sameAs;
    }
    
    
    
    
}
