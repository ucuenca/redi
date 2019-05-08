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

import edu.ucuenca.storage.AbstractREDIMongoEnv;
import edu.ucuenca.storage.api.MongoService;
import edu.ucuenca.storage.api.PopulateMongo;
import edu.ucuenca.storage.exceptions.FailMongoConnectionException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class MongoServiceTest extends AbstractREDIMongoEnv {

    private static MongoService mongoService;
    private static PopulateMongo pmongo;

    @Override
    public void registerServices() {
        mongoService = getMarmotta().getService(MongoService.class);
        pmongo = getMarmotta().getService(PopulateMongo.class);
    }
    
    

    @Test
    @Ignore
    public void testConnect() throws FailMongoConnectionException {
        mongoService.connect();
    }

    @Test
    @Ignore("Fill MongoDB with data")
    public void testAtuhor() {
        Assert.assertTrue("Result should be greater than 1.",
                mongoService.getAuthor("http://localhost:8080/resource/authors/victor-hugo-saquicela-galarza").length() > 1);
    }
    
    @Test
   // @Ignore("Fill MongoDB with data")
    public void testConference() {
       // Assert.assertTrue("Result should be greater than 1.", );
        //getStatsConferencebyAuthor (String uri);
        System.out.print ("RESP");
         //System.out.print (pmongo);
          System.out.print ("sig");
        System.out.print (pmongo.getStatsbyAuthor());
    }

    @Test
    @Ignore("Fill MongoDB with data")
    public void testStatistics() {
        Assert.assertTrue("Result should be greater than 1.", mongoService.getStatistics("barchar").length() > 1);
    }

}
