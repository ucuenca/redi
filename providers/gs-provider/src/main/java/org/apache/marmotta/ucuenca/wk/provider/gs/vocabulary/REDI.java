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
public class REDI {

    public static final String NAMESPACE = "http://ucuenca.edu.ec/ontology#";
    public static final String NAMESPACE_AUTHOR = "http://ucuenca.edu.ec/resource/author/";
    public static final String NAMESPACE_BOOK = "http://ucuenca.edu.ec/resource/book/";
    public static final String NAMESPACE_PUBLICATION = "http://ucuenca.edu.ec/wkhuska/publication/";
    public static final String PREFIX = "uc";
    public static final URI CITATION_COUNT;
    public static final URI GSCHOLAR_PUB;

    static {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        // Properties
        CITATION_COUNT = factory.createURI(REDI.NAMESPACE, "citationCount");
        GSCHOLAR_PUB = factory.createURI(REDI.NAMESPACE, "googlescholarURL");
    }
}
