/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.provider.gs.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class Publication {

    private static Logger log = LoggerFactory.getLogger(Publication.class);

    private String url;
    private String title;
    private int numCitations;
    private String date;
    private String description;
    private String pages;
    private String publisher;
    private String conference;
    private String journal;
    private String volume;
    private String issue;
    private String book;

    private List<String> authors = new ArrayList<>();
    private List<String> resources = new ArrayList<>();

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the resources
     */
    public List<String> getResources() {
        return resources;
    }

    /**
     * @param resources the resources to set
     */
    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    /**
     * @param resource the resources to set
     */
    public void addResources(String resource) {
        this.resources.add(resource);
    }

    /**
     * @return the numCitations
     */
    public int getCitations() {
        return numCitations;
    }

    /**
     * @param citations the numCitations to set
     */
    public void setCitations(int citations) {
        this.numCitations = citations;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return the authors
     */
    public List<String> getAuthors() {
        return authors;
    }

    /**
     * @param authors the authors to set
     */
    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the pages
     */
    public String getPages() {
        return pages;
    }

    /**
     * @param pages the pages to set
     */
    public void setPages(String pages) {
        this.pages = pages;
    }

    /**
     * @return the publisher
     */
    public String getPublisher() {
        return publisher;
    }

    /**
     * @param publisher the publisher to set
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    /**
     * @return the conference
     */
    public String getConference() {
        return conference;
    }

    /**
     * @param conference the conference to set
     */
    public void setConference(String conference) {
        this.conference = conference;
    }

    /**
     * @return the journal
     */
    public String getJournal() {
        return journal;
    }

    /**
     * @param journal the journal to set
     */
    public void setJournal(String journal) {
        this.journal = journal;
    }

    /**
     * @return the volume
     */
    public String getVolume() {
        return volume;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(String volume) {
        this.volume = volume;
    }

    /**
     * @return the issue
     */
    public String getIssue() {
        return issue;
    }

    /**
     * @param issue the issue to set
     */
    public void setIssue(String issue) {
        this.issue = issue;
    }

    /**
     * @return the book
     */
    public String getBook() {
        return book;
    }

    /**
     * @param book the book to set
     */
    public void setBook(String book) {
        this.book = book;
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity"})
    public void map(Map<String, String> fields) {
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String value = entry.getValue();
            switch (entry.getKey()) {
                case "Total citations":
                    this.setCitations(Integer.parseInt(value.replaceAll("[^0-9]", "")));
                    break;

                case "Publication date":
                    this.setDate(value);
                    break;

                case "Authors":
                    this.setAuthors(Arrays.asList(value.split(",")));
                    break;

                case "Description":
                    this.setDescription(value);
                    break;

                case "Pages":
                    this.setPages(value);
                    break;

                case "Publisher":
                    this.setPublisher(value);
                    break;

                case "Conference":
                    this.setConference(value);
                    break;

                case "Journal":
                    this.setJournal(value);
                    break;

                case "Volume":
                    this.setVolume(value);
                    break;

                case "Issue":
                    this.setIssue(value);
                    break;

                case "Book":
                    this.setBook(value);
                    break;
                default:
                    log.info("ADD TO MAP => " + entry.getKey() + ":" + entry.getValue());
                    break;
            }
        }
    }

    @Override
    public String toString() {
        return "Publication{" + "url=" + url + ", title=" + title + ", citations=" + numCitations + ", date=" + date + ", authors=" + authors + ", description=" + description + ", pages=" + pages + ", publisher=" + publisher + ", conference=" + conference + ", journal=" + journal + ", volume=" + volume + ", issue=" + issue + ", book=" + book + ", resources=" + resources + '}';
    }

}
