/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.provider.gs.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
@SuppressWarnings("PMD.ShortClassName")
public class BIBO {

    public static final String NAMESPACE = "http://purl.org/ontology/bibo/";
    public static final String PREFIX = "bibo";
    public static final URI ABSTRACT;
    public static final URI PAGES;
    public static final URI CONFERENCE;
    public static final URI VOLUME;
    public static final URI ISSUE;
    public static final URI BOOK;
    public static final URI URI;
    public static final URI JOURNAL;
    public static final URI DOCUMENT;
    public static final URI ACADEMIC_ARTICLE;

    static {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        ABSTRACT = factory.createURI(NAMESPACE, "abstract");
        PAGES = factory.createURI(NAMESPACE, "pages");
        CONFERENCE = factory.createURI(NAMESPACE, "Conference");
        VOLUME = factory.createURI(NAMESPACE, "volume");
        ISSUE = factory.createURI(NAMESPACE, "issue");
        BOOK = factory.createURI(NAMESPACE, "Book");
        URI = factory.createURI(NAMESPACE, "uri");
        JOURNAL = factory.createURI(NAMESPACE, "Journal");
        DOCUMENT = factory.createURI(NAMESPACE, "Document");
        ACADEMIC_ARTICLE = factory.createURI(NAMESPACE, "AcademicArticle");
    }
}
