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
import org.apache.marmotta.ucuenca.wk.provider.gs.GoogleScholarSearchProvider;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.Author;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.Publication;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public final class ProfileHandler extends IHandler {

    private boolean numEntriesDiv = false;
    private boolean isPublication = false;
    private final Author author;

    private static final String ANCHOR = "a";

    public ProfileHandler(Author author) {
        this.author = author;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (attributes.getType("id") != null && attributes.getValue("id").equals("gsc_a_nn")) {
            numEntriesDiv = true;
        } else if (attributes.getType("class") != null && attributes.getValue("class").equals("gsc_a_tr")) {
            isPublication = true;
        } else if (isPublication && ANCHOR.equals(localName) && attributes.getValue("class").equals("gsc_a_at")) {
            String url = GoogleScholarSearchProvider.SCHOLAR_GOOGLE + attributes.getValue("href");
            Publication pub = new Publication(url);
            author.addPublications(pub);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (numEntriesDiv && localName.endsWith("span")) {
            numEntriesDiv = false;
        } else if (isPublication && ANCHOR.equals(localName)) {
            isPublication = false;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (numEntriesDiv) {
            int numPublications = Integer.parseInt(new String(ch, start, length).replace("–", ""));
            author.setNumPublications(numPublications);
        }
    }

    @Override
    public List<Author> getResults() {
        return Arrays.asList(author);
    }

}
