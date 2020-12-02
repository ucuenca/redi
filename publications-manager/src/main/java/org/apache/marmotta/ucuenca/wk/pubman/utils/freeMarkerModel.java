/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.utils;

import com.beust.jcommander.internal.Lists;
import java.util.List;

/**
 *
 * @author cedia
 */
public class freeMarkerModel {

  public notification newNotification(String name, String URI, List<String> newPublicationsURI, List<String> newPublicationsTitle) {
    return new notification(name, URI, newPublicationsURI, newPublicationsTitle);
  }

  public researcherInvitation newResearcherInvitation(String name, String URI, List<String> emails) {
    return new researcherInvitation(URI, name, emails);
  }

  public class researcherInvitation {

    private String URI;
    private String name;
    private List<String> emails;

    public researcherInvitation(String URI, String name, List<String> emails) {
      this.URI = URI;
      this.name = name;
      this.emails = emails;
    }

    public String getURI() {
      return URI;
    }

    public void setURI(String URI) {
      this.URI = URI;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public List<String> getEmails() {
      return emails;
    }

    public void setEmails(List<String> emails) {
      this.emails = emails;
    }

  }

  public class notification {

    public notification(String name, String URI, List<String> newPublicationsURI, List<String> newPublicationsTitle) {
      this.name = name;
      this.URI = URI;
      this.lsPapers = Lists.newArrayList();
      for (int i = 0; i < newPublicationsTitle.size(); i++) {
        this.lsPapers.add(new paper(newPublicationsTitle.get(i), newPublicationsURI.get(i)));
      }
    }

    private String name;
    private String URI;
    private List<paper> lsPapers;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getURI() {
      return URI;
    }

    public void setURI(String URI) {
      this.URI = URI;
    }

    public List<paper> getLsPapers() {
      return lsPapers;
    }

    public void setLsPapers(List<paper> lsPapers) {
      this.lsPapers = lsPapers;
    }

  }

  public class paper {

    public paper(String title, String URL) {
      this.title = title;
      this.URL = URL;
    }

    private String title;
    private String URL;

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getURL() {
      return URL;
    }

    public void setURL(String URL) {
      this.URL = URL;
    }

  }

}
