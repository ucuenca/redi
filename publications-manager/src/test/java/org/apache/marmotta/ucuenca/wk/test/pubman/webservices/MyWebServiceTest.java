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
package org.apache.marmotta.ucuenca.wk.test.pubman.webservices;

//import static com.jayway.restassured.RestAssured.expect;
//import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.DecoderConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.http.ContentType;

import org.junit.AfterClass;
import org.junit.BeforeClass;
//import org.junit.Ignore;
//import org.junit.Ignore;
import org.junit.Test;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.ucuenca.wk.pubman.webservices.PubWebService;

public class MyWebServiceTest {

    private static JettyMarmotta marmotta;
    
    private static final String BASE_URL = "/pubman";

    @BeforeClass
    public static void beforeClass() {
        marmotta = new JettyMarmotta("/base-module-test", 9090, PubWebService.class);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 9090;
        RestAssured.basePath = "/base-module-test";
        RestAssured.config = RestAssuredConfig.newConfig().decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"));
    }

    @AfterClass
    public static void afterClass() {
        if (marmotta != null) {
            marmotta.shutdown();
        }
    }


    
}
