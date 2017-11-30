/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
    public static final URI ACADEMICS_PROVIDER;
    public static final URI EXTRACTION_EVENT;

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
    public static final URI ACADEMICS_ID;
    public static final URI ORCID;
    public static final URI SUBJECT_AREA;
    public static final URI EID;
    public static final URI SURNAME;
    public static final URI GIVEN_NAME;
    public static final URI INITIALS;
    public static final URI AFFILIATION_NAME;
    public static final URI PROVIDER;
    public static final URI MAIN;
    public static final URI AFFILIATION_ID;
    public static final URI DISPLAY_NAME;
    public static final URI YEAR;
    public static final URI ESTIMATED_CITATION_COUNT;
    public static final URI ACADEMICS_REFERENCE_ID;
    public static final URI CONFERENCE_ID;
    public static final URI CONFERENCE_NAME;
    public static final URI POSITION;
    public static final URI VENUE_FULL_NAME;
    public static final URI VENUE_SHORT_NAME;

    static {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        // Classes
        ENDPOINT = factory.createURI(REDI.NAMESPACE, "Endpoint");
        SCOPUS_PROVIDER = factory.createURI(REDI.NAMESPACE, "ScopusProvider");
        ACADEMICS_PROVIDER = factory.createURI(REDI.NAMESPACE, "AcademicsKnowledgeProvider");
        EXTRACTION_EVENT = factory.createURI(REDI.NAMESPACE, "ExtractionEvent");

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
        ACADEMICS_ID = factory.createURI(REDI.NAMESPACE, "academicsId");
        ORCID = factory.createURI(REDI.NAMESPACE, "orcid");
        SUBJECT_AREA = factory.createURI(REDI.NAMESPACE, "subjectArea");
        EID = factory.createURI(REDI.NAMESPACE, "eid");
        GIVEN_NAME = factory.createURI(REDI.NAMESPACE, "givenName");
        SURNAME = factory.createURI(REDI.NAMESPACE, "surname");
        INITIALS = factory.createURI(REDI.NAMESPACE, "initilias");
        AFFILIATION_NAME = factory.createURI(REDI.NAMESPACE, "affiliationName");
        PROVIDER = factory.createURI(REDI.NAMESPACE, "Provider");
        MAIN = factory.createURI(REDI.NAMESPACE, "main");
        AFFILIATION_ID = factory.createURI(REDI.NAMESPACE, "affiliationId");
        DISPLAY_NAME = factory.createURI(REDI.NAMESPACE, "displayName");
        YEAR = factory.createURI(REDI.NAMESPACE, "year");
        ESTIMATED_CITATION_COUNT = factory.createURI(REDI.NAMESPACE, "estimatedCitationCount");
        ACADEMICS_REFERENCE_ID = factory.createURI(REDI.NAMESPACE, "academicsReferenceId");
        CONFERENCE_ID = factory.createURI(REDI.NAMESPACE, "conferenceId");
        CONFERENCE_NAME = factory.createURI(REDI.NAMESPACE, "conferenceName");
        POSITION = factory.createURI(REDI.NAMESPACE, "position");
        VENUE_FULL_NAME = factory.createURI(REDI.NAMESPACE, "venueFullName");
        VENUE_SHORT_NAME = factory.createURI(REDI.NAMESPACE, "venueShortName");
    }
}
