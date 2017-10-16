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
package edu.ucuenca.storage.services;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import edu.ucuenca.storage.api.MongoService;
import edu.ucuenca.storage.exceptions.FailMongoConnectionException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.bson.Document;
import org.slf4j.Logger;

;

/**
 * Default Implementation of {@link MongoService}
 */
@ApplicationScoped
public class MongoServiceImpl implements MongoService {

    @Inject
    private Logger log;
    @Inject
    private ConfigurationService configurationService;
    private MongoClient mongoClient;
    private MongoDatabase db;
    private MongoCollection<Document> authors;
    private MongoCollection<Document> statistics;

    @PostConstruct
    public void initialize() throws FailMongoConnectionException {
        connect();
    }

    @Override
    public void connect() throws FailMongoConnectionException {
        String host = configurationService.getStringConfiguration("mongo.host");
        int port = configurationService.getIntConfiguration("mongo.port");

        mongoClient = new MongoClient(host, port);
        db = mongoClient.getDatabase(DATABASE);
        authors = db.getCollection(Collection.AUTHORS.getValue());
        statistics = db.getCollection(Collection.STATISTICS.getValue());
    }

    @Override
    public String getAuthor(String uri) {
        return authors.find(eq("_id", uri))
                .first()
                .toJson();
    }

    @Override
    public String getStatistics(String id) {
        return statistics.find(eq("_id", id))
                .first().toJson();
    }

    @PreDestroy
    public void shutdown() {
        log.info("Killing connection to MongoDB.");
        mongoClient.close();
    }
}
