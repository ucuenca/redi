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
package org.apache.marmotta.ucuenca.wk.provider.gs.handler;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.Publication;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public final class PublicationHandler extends IHandler {

    private static final String ANCHOR = "a";
    private static final String DIV = "div";

    private boolean isPublication = false;
    private boolean extract = false;
    private boolean isField = false;
    private boolean isValue = false;
    private boolean isTitle = false;
    private String key;
    private final ConcurrentHashMap<String, String> fields = new ConcurrentHashMap<>();
    private final Publication publication;

    public PublicationHandler(Publication publication) {
        this.publication = publication;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName.equals("div") && (attributes.getType("id") != null || attributes.getType("class") != null)) {
            String value = attributes.getValue("class") == null ? attributes.getValue("id") : attributes.getValue("class");
            switch (value) {
                case "gsc_ccl":
                    isPublication = true;
                    break;
                case "gsc_title_gg":
                    extract = isPublication;
                    break;
                case "gsc_title":
                    extract = isPublication;
                    isTitle = true;
                    break;
                case "gsc_field":
                    isField = true;
                    break;
                case "gsc_value":
                    isValue = true;
                    break;
                default:
                    break;
            }
        } else if (extract && ANCHOR.equals(localName)) {
            publication.addResources(attributes.getValue("href"));
        } else if (extract && ANCHOR.equals(localName)) {
            publication.addResources(attributes.getValue("href"));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (extract && (ANCHOR.equals(localName) || DIV.equals(localName))) {
            extract = false;
        } else if (isField && DIV.equals(localName)) {
            isField = false;
        } else if (isValue && DIV.equals(localName)) {
            isValue = false;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        publication.map(fields);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (extract && isTitle) {
            publication.setTitle(new String(ch, start, length));
        } else if (isField) {
            key = new String(ch, start, length);
            if (!fields.containsKey(key)) {
                fields.put(key, "");
            }
        } else if (isValue) {
            fields.replace(key, fields.get(key) + new String(ch, start, length));
        }
    }

    @Override
    public List<Publication> getResults() {
        return Arrays.asList(publication);
    }

}
