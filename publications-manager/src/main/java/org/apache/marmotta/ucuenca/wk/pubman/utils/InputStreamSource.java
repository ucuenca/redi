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
package org.apache.marmotta.ucuenca.wk.pubman.utils;

import com.avengerpenguin.r2r.ExportableSource;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Implementation of the Source Interface which reads a file or URI resource
 * into memory.
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class InputStreamSource implements ExportableSource {

    private Model model;

    public InputStreamSource(InputStream in, String base) {
        model = ModelFactory.createDefaultModel();
        model.read(in, base, "TTL");
    }

    @Override
    public void exportMappings(Writer out, String serializationFormat) {
        model.write(out, serializationFormat);
    }

    @Override
    public void exportMappings(OutputStream out, String serializationFormat) {
        model.write(out, serializationFormat);
    }

    @Override
    public void exportMappings(Writer out) {
        model.write(out, "N-TRIPLE");
    }

    @Override
    public void exportMappings(OutputStream out) {
        model.write(out, "N-TRIPLE");
    }

    @Override
    public QueryExecution executeQuery(String query) {
        return QueryExecutionFactory.create(query, model);
    }

    @Override
    public Model executeDescribeQuery(String query) {
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        Model model = qe.execDescribe();
        qe.close();
        return model;
    }

    @Override
    public Model executeConstructQuery(String query) {
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        Model model = qe.execConstruct();
        qe.close();
        return model;
    }

}
