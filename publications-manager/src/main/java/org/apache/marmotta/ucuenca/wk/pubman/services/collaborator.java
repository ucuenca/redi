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
public class collaborator {
    private String authorBase ;
    private String uri ;
    private Double subjectScore;
    private Boolean coauthor;
    private Boolean cluster ;
    private String Subjects;
    private static Double COAUTHOR = 1.0;
    private static Double NOCOAUTHOR = 0.5;
    private static Double CLUSTER = 1.0;
    private static Double NOCLUSTER = 0.75;
    private String organization;
    private String imgUri;
    private String lastName;
    private String [] targets;

    public String[] getTargets() {
        return targets;
    }

    public void setTargets(String[] targets) {
        this.targets = targets;
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }

    //  private int coauthorScore;
    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
  //  private int clusterScore;
    private String cName ; 

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
     public  collaborator (String authorBase , String uri , String cName , String lName,  String org , String Subjects ){
   this.authorBase = authorBase;
   this.uri = uri;
   this.cName = cName ;
   this.organization = org;
   this.Subjects = Subjects;
   this.lastName = lName;
   }
   
   public  collaborator (String authorBase , String uri , String cName , String lName,  Double subjectScore ,Boolean coauthor, Boolean cluster , String org , String Subjects ){
   this.authorBase = authorBase;
   this.uri = uri;
   this.cName = cName ;
   this.subjectScore = subjectScore;
   this.cluster = cluster;
   this.coauthor = coauthor;
   this.organization = org;
   this.Subjects = Subjects;
   this.lastName =  lName;
   }

    public String getSubjects() {
        return Subjects;
    }

    public void setSubjects(String Subjects) {
        this.Subjects = Subjects;
    }

    public String getAuthorBase() {
        return authorBase;
    }

    public Boolean isCoauthor() {
        return coauthor;
    }

    public void setCoauthor(Boolean coauthor) {
        this.coauthor = coauthor;
    }

    public Boolean isCluster() {
        return cluster;
    }

    public void setCluster(Boolean cluster) {
        this.cluster = cluster;
    }

    public void setAuthorBase(String authorBase) {
        this.authorBase = authorBase;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Double getSubjectScore() {
        return subjectScore;
    }

    public void setSubjectScore(Double subjectScore) {
        this.subjectScore = subjectScore;
    }

    public Double getCoauthorScore() {
        if (this.coauthor){
        return COAUTHOR;
        }else {
        return NOCOAUTHOR;
        }
    }



    public double getClusterScore() {
        if (this.cluster){
        return CLUSTER ;
        }else {
        return NOCLUSTER;
        }
        
    }

  

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }
   
    
     public Double calcScore () {
     return this.subjectScore*this.getClusterScore()*this.getCoauthorScore();
      
     }
   

    
}
