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
import java.net.URL;
import java.util.List;
import org.bson.Document;
import org.json.simple.JSONObject;

public interface MongoService {

  /**
   * Returns JSON-LD from an URI of an author given.
   *
   * @param uri
   * @return
   */
  public String getAuthor(String uri);

  public String getSPARQL(String qry);

  public String getStatistics(String id);

  public String getRelatedAuthors(String uri);

  public String getAuthorsByDiscipline(String uri);

  public String getCluster(String uri);

  public List<Document> getClusters();

  public List<Document> getClustersTotals();

  public List<Document> getSubClustersTotals(String uri);

  public String getAuthorsByArea(String cluster, String subcluster);

  public List<Document> getCountries();

  public String getStatisticsByInst(String id);

  public String getStatisticsByAuthor(String id);

  public void registerSession(String orcid, String token);

  public Document getProfileValAuthor(String id);

  public JSONObject obtainNewProfiles(String org) throws Exception;

  public Document removeProfileValAuthor(String id);

  public boolean checkSession(String orcid, String token);

  /**
   * Create a connection to {@link com.mongodb.MongoClient}.
   *
   * @throws FailMongoConnectionException
   */
  public void connect() throws FailMongoConnectionException;

  public enum Collection {
    SPARQLS("sparqls"), AUTHORS("authors"), PUBLICATIONS("publications"), STATISTICS("statistics"), STATISTICS_INST("statistics_inst"),
    RELATEDAUTHORS("relatedauthors"), AUTHORS_DISCPLINE("authors_discipline"), AUTHORS_AREA("authors_area"),
    COUNTRIES("countries"), CLUSTERS("clusters"), CLUSTERSTOTALS("clusterstotals"), STATISTICS_AUTHOR("statistics_author"), PROFILE_AUTHOR("profile_author"), SESSIONS("sessions");

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

  public enum Database {
    NAME;
    private String dbname;
    private final String redi = "redi";

    private Database() {
    }

    public String getDBName() {
      URL resource = getClass().getResource("/");
      String path = resource.getPath();
      String appName = path.substring(path.substring(0, path.indexOf("/WEB-INF")).lastIndexOf('/') + 1, path.indexOf("/WEB-INF"));
      dbname = redi + "_" + appName;
      if (appName.equals("ROOT")) {
        dbname = redi;
      }
      return dbname;
    }
  }
}
