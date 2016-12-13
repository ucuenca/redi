/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.provider.gs.util;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class Author {

    private String name;
    private String affiliation;
    private String profile;
    private String img;
    private String domain;
    private int numCitations;
    private int numPublications;
    private final List<String> areas;
    private List<Publication> publications;

    private static Logger log = LoggerFactory.getLogger(Author.class);

    public Author() {
        this.areas = new ArrayList<>();
        this.publications = new ArrayList<>();
        this.affiliation = "";
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the affiliation
     */
    public String getAffiliation() {
        return affiliation;
    }

    /**
     * @param affiliation the affiliation to set
     */
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    /**
     * @return the profile
     */
    public String getProfile() {
        return profile;
    }

    /**
     * @param url the profile to set
     */
    public void setProfile(String url) {
        this.profile = url;
    }

    /**
     * @return the img
     */
    public String getImg() {
        return img;
    }

    /**
     * @param img the img to set
     */
    public void setImg(String img) {
        this.img = img;
    }

    /**
     * @return the domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * @return the numCitations
     */
    public int getNumCitations() {
        return numCitations;
    }

    /**
     * @param numCitations the numCitations to set
     */
    public void setNumCitations(int numCitations) {
        this.numCitations = numCitations;
    }

    /**
     * @return the numPublications
     */
    public int getNumPublications() {
        return numPublications;
    }

    /**
     * @param numPublications the numPublications to set
     */
    public void setNumPublications(int numPublications) {
        this.numPublications = numPublications;
    }

    /**
     * @return the areas
     */
    public List<String> getAreas() {
        return areas;
    }

    /**
     * @param area the areas to set
     */
    public void addAreas(String area) {
        areas.add(area);
    }

    /**
     * @return the publications
     */
    public List<Publication> getPublications() {
        return publications;
    }

    /**
     * @param publication
     */
    public void addPublications(Publication publication) {
        publications.add(publication);
    }

    /**
     * @param publications the publications to set
     */
    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }

    /**
     * Convert an author object to RDF.
     */
    public void map() {
        int myvar = 1 + 1;
        log.info(String.valueOf(myvar));

    }

    /**
     * Stores the extracted value to an author object.
     *
     * @param attr
     * @param value
     */
    public void updateAuthor(ProfileAttributes attr, String value) {
        Author author = this;
        switch (attr) {
            case AFFILIATION:
                author.setAffiliation(author.getAffiliation() + value);
                break;
            case AREA:
                if (!value.trim().equals("")) {
                    author.addAreas(value);
                }
                break;
            case CITE:
                author.setNumCitations(Integer.parseInt(value.replaceAll("[^0-9]", "")));
                break;
            case DOMAIN:
                author.setDomain(value.replace("@", ""));
                break;
            case NAME:
                author.setName(value);
                break;
            case PHOTO:
                author.setImg(value);
                break;
            case URL:
                author.setProfile(value);
                break;
            default:
                log.debug(String.format("'%s' do not match with Author attributes", attr));
                break;
        }
    }

    @Override
    public String toString() {
        return "Author{" + "name=" + name + ", affiliation=" + affiliation + ", profile=" + profile + ", img=" + img + ", domain=" + domain + ", numCitations=" + numCitations + ", areas=" + areas + ", publications=" + publications + '}';
    }
}
