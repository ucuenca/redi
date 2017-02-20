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

    // Change domain accord with redi's domain
    //private static final String DOMAIN = "http://190.15.141.85:8080/resource/";
    //private static final String BASE_DOMAIN = "http://localhost:8080";

    public static final String PREFIX = "uc";
    public static final String NAMESPACE = "http://ucuenca.edu.ec/ontology#";

    // Resources
    //public static final String AUTHOR_RESOURCE = BASE_DOMAIN + "/resource/author/";
    //public static final String BOOK_RESOURCE = BASE_DOMAIN + "/resource/book/";
    //public static final String PUBLICATION_RESOURCE = BASE_DOMAIN + "/resource/publication/";
    //public static final String ENDPOINT_RESOURCE = BASE_DOMAIN + "/resource/endpoint/";

    // Graphs
    //public static final String ENDPOINT_GRAPH = BASE_DOMAIN + "/context/endpoints";
    //public static final String AUTHOR_GRAPH = BASE_DOMAIN + "/context/authors";

    // Properties
    public static final URI CITATION_COUNT;
    public static final URI GSCHOLAR_PUB;
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

    static {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        // Properties
        CITATION_COUNT = factory.createURI(REDI.NAMESPACE, "citationCount");
        GSCHOLAR_PUB = factory.createURI(REDI.NAMESPACE, "googlescholarURL");
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

    }
}
