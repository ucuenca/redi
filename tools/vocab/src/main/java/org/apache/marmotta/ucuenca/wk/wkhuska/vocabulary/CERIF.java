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
 * @author joe
 */
public class CERIF {
    public static final String NAMESPACE = "http://eurocris.org/ontology/cerif#";
    public static final String PREFIX = "bibo";
    public static final URI LINK;
    public static final URI HAS_IDENTIFIER;
    public static final URI HAS_TITLE;
    public static final URI HAS_CLASIFICATION;
    public static final URI HAS_ABSTRACT;
    public static final URI REGISTRATION_DATE;
    public static final URI APPROVAL_DATE;
    public static final URI END_DATE;
    public static final URI PATENT;
    public static final URI LINKS_TO_PROJECT;
    public static final URI LINKS_TO_PERSON;
    public static final URI LINKS_TO_PATENT;
    

   
      
     static {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        LINK = factory.createURI(NAMESPACE, "link");
        HAS_IDENTIFIER = factory.createURI(NAMESPACE, "has_identifier");
        HAS_TITLE = factory.createURI(NAMESPACE, "has_title");
        HAS_CLASIFICATION = factory.createURI(NAMESPACE, "has_classification");
        HAS_ABSTRACT = factory.createURI(NAMESPACE, "has_abstract");
        REGISTRATION_DATE = factory.createURI(NAMESPACE, "registrationDate");
        APPROVAL_DATE = factory.createURI(NAMESPACE, "approvalDate");
        END_DATE = factory.createURI(NAMESPACE, "endDate");
        PATENT = factory.createURI(NAMESPACE, "Patent");
        LINKS_TO_PROJECT = factory.createURI(NAMESPACE, "linksToProject");
        LINKS_TO_PERSON = factory.createURI(NAMESPACE, "linksToPerson");
        LINKS_TO_PATENT = factory.createURI(NAMESPACE, "linksToPatent");
     }  
}
