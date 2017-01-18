/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Namespace BIBO.
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
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
