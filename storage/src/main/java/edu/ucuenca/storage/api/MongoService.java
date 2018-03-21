/*
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
package edu.ucuenca.storage.api;

import edu.ucuenca.storage.exceptions.FailMongoConnectionException;

public interface MongoService {

    static final String DATABASE = "redi";

    /**
     * Returns JSON-LD from an URI of an author given.
     *
     * @param iri
     * @return
     */
    public String getAuthor(String uri);

    public String getStatistics(String id);
    
    public String getRelatedAuthors(String uri);

    /**
     * Create a connection to {@link com.mongodb.MongoClient}.
     *
     * @throws FailMongoConnectionException
     */
    public void connect() throws FailMongoConnectionException;

    public enum Collection {
        AUTHORS("authors"), PUBLICATIONS("publications"), STATISTICS("statistics"), RELATEDAUTHORS("relatedauthors");

        private final String value;

        private Collection(String name) {
            this.value = name;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
}
