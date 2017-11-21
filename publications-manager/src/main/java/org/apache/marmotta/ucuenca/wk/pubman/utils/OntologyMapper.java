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

import com.avengerpenguin.r2r.JenaModelOutput;
import com.avengerpenguin.r2r.JenaModelSource;
import com.avengerpenguin.r2r.Mapper;
import com.avengerpenguin.r2r.Output;
import com.avengerpenguin.r2r.Repository;
import com.avengerpenguin.r2r.Source;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.IOException;
import java.io.InputStream;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class OntologyMapper {

    public static Model map(Model model, InputStream inputMapping, String vocabulary) {
        com.hp.hpl.jena.rdf.model.Model jenaModelIn = JenaSesameUtils.asJenaModel(model);
        com.hp.hpl.jena.rdf.model.Model jenaModelOut = ModelFactory.createDefaultModel();
        Source in = new JenaModelSource(jenaModelIn);
        Output out = new JenaModelOutput(jenaModelOut);
        Repository tempRepository = new Repository(new InputStreamSource(inputMapping, ""));

        Mapper.transform(in, out, tempRepository, vocabulary);
        try {
            out.close();
        } catch (IOException ex) {
        }
        return new LinkedHashModel(JenaSesameUtils.asSesameGraph(jenaModelIn));
    }
}
