/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class REDI {

    public static final String PREFIX = "uc";
    public static final String NAMESPACE = "http://ucuenca.edu.ec/ontology#";
    // Classes
    public static final URI ENDPOINT;
    public static final URI SCOPUS_PROVIDER;

    // Properties
    public static final URI CITATION_COUNT;
    public static final URI GSCHOLAR_URl;
    public static final URI ACADEMICS_KNOWLEDGE_URl;
    public static final URI STATUS;
    public static final URI NAME;
    public static final URI URL;
    public static final URI GRAPH;
    public static final URI FULLNAME;
    public static final URI CITY;
    public static final URI PROVINCE;
    public static final URI LATITUDE;
    public static final URI LONGITUDE;
    public static final URI DOMAIN;
    public static final URI COUNTRY;
    public static final URI TYPE;
    public static final URI BELONGTO;
    public static final URI EXTRACTIONDATE;
    public static final URI SCOPUS_AUTHOR_ID;
    public static final URI ORCID;
    public static final URI SUBJECT_AREA;
    public static final URI EID;
    public static final URI SURNAME;
    public static final URI GIVEN_NAME;
    public static final URI INITIALS;
    public static final URI AFFILIATION_NAME;
     public static final URI PROVIDER;
     public static final URI MAIN;

    static {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        // Classes
        ENDPOINT = factory.createURI(REDI.NAMESPACE, "Endpoint");
        SCOPUS_PROVIDER = factory.createURI(REDI.NAMESPACE, "ScopusProvider");

        // Properties
        CITATION_COUNT = factory.createURI(REDI.NAMESPACE, "citationCount");
        GSCHOLAR_URl = factory.createURI(REDI.NAMESPACE, "googlescholarURL");
        ACADEMICS_KNOWLEDGE_URl = factory.createURI(REDI.NAMESPACE, "academicsKnowledgeURL");
        STATUS = factory.createURI(REDI.NAMESPACE, "status");
        NAME = factory.createURI(REDI.NAMESPACE, "name");
        URL = factory.createURI(REDI.NAMESPACE, "url");
        GRAPH = factory.createURI(REDI.NAMESPACE, "graph");
        FULLNAME = factory.createURI(REDI.NAMESPACE, "fullName");
        CITY = factory.createURI(REDI.NAMESPACE, "city");
        PROVINCE = factory.createURI(REDI.NAMESPACE, "province");
        LATITUDE = factory.createURI(REDI.NAMESPACE, "latitude");
        LONGITUDE = factory.createURI(REDI.NAMESPACE, "longitude");
        DOMAIN = factory.createURI(REDI.NAMESPACE, "domain");
        COUNTRY = factory.createURI(REDI.NAMESPACE, "country");
        TYPE = factory.createURI(REDI.NAMESPACE, "type");
        BELONGTO = factory.createURI(REDI.NAMESPACE, "belongTo");
        EXTRACTIONDATE = factory.createURI(REDI.NAMESPACE, "extractionDate");
        SCOPUS_AUTHOR_ID = factory.createURI(REDI.NAMESPACE, "scopusAuthorId");
        ORCID = factory.createURI(REDI.NAMESPACE, "orcid");
        SUBJECT_AREA = factory.createURI(REDI.NAMESPACE, "subjectArea");
        EID = factory.createURI(REDI.NAMESPACE, "eid");
        GIVEN_NAME = factory.createURI(REDI.NAMESPACE, "givenName");
        SURNAME = factory.createURI(REDI.NAMESPACE, "surname");
        INITIALS = factory.createURI(REDI.NAMESPACE, "initilias");
        AFFILIATION_NAME = factory.createURI(REDI.NAMESPACE, "affiliationName");
        PROVIDER   = factory.createURI(REDI.NAMESPACE, "Provider");
        MAIN   = factory.createURI(REDI.NAMESPACE, "main");

    }
}
