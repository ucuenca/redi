/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.marmotta.ucuenca.wk.pubman.model;

/**
 *
 * @author joe
 */

public class AuthorProfile {
    private String uri;
    private String name;
    private String orcid;
    private String bio;
    private String [] orgs;
    private String img;
    private String [] emails;
    private String [] homepages;
    private String citation;
    private String hindex;
    private String i10;
    private String [] otheraf;
    private String npub;
    
    
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getNpub() {
        return npub;
    }

    public void setNpub(String npub) {
        this.npub = npub;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrcid() {
        return orcid;
    }

    public void setOrcid(String orcid) {
        this.orcid = orcid;
    }

    public String[] getOrgs() {
        return orgs;
    }

    public void setOrg(String[] orgs) {
        this.orgs = orgs;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String[] getEmails() {
        return emails;
    }

    public void setEmails(String[] emails) {
        this.emails = emails;
    }

    public String[] getHomepages() {
        return homepages;
    }

    public void setHomepages(String[] homepages) {
        this.homepages = homepages;
    }

    public String getCitation() {
        return citation;
    }

    public void setCitation(String citation) {
        this.citation = citation;
    }

    public String getHindex() {
        return hindex;
    }

    public void setHindex(String hindex) {
        this.hindex = hindex;
    }

    public String getI10() {
        return i10;
    }

    public void setI10(String i10) {
        this.i10 = i10;
    }

    public String[] getOtheraf() {
        return otheraf;
    }

    public void setOtheraf(String[] otheraf) {
        this.otheraf = otheraf;
    }

    public String[] getCluster() {
        return clusters;
    }

    public void setCluster(String[] cluster) {
        this.clusters = cluster;
    }

    public String[] getTopics() {
        return topics;
    }

    public void setTopics(String[] topics) {
        this.topics = topics;
    }

    public String[] getOtherProfile() {
        return otherProfile;
    }

    public void setOtherProfile(String[] otherProfile) {
        this.otherProfile = otherProfile;
    }
    private String [] clusters;
    private String [] topics;
    private String [] otherProfile;
    
    
}
