/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.ucuenca.wk.endpoint.gs;

import java.nio.charset.Charset;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;

/**
 * Endpoint for accessing publications from Google Scholar. There is a
 * constraint which is the need to read an author URI.
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class GoogleScholarPublicationEndpoint extends Endpoint {

    // TODO: find another way to read this parameters
    private String authorURI;

    public GoogleScholarPublicationEndpoint() {
        super("Google Scholar Publication Endpoint", "Google Scholar Publication",
                "^https?:\\/\\/scholar\\.google\\.com\\/citations\\?view_op=view_citation.*",
                null, 86400L);
        setPriority(PRIORITY_MEDIUM);
        addContentType(new ContentType("text", "html", Charset.forName("UTF-8")));
        addContentType(new ContentType("*", "*", 0.1));
    }

    public String getAuthorURI() {
        return authorURI;
    }

    public void setAuthorURI(String authorURI) {
        this.authorURI = authorURI;
    }
}
