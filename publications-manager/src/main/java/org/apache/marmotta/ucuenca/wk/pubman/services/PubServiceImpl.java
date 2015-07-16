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
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.fasterxml.jackson.annotation.JsonFormat.Value;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;

import org.apache.marmotta.ucuenca.wk.pubman.api.PubService;
import org.apache.marmotta.ucuenca.wk.pubman.api.SparqlFunctionsService;
import org.apache.marmotta.ucuenca.wk.pubman.exceptions.PubException;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

/**
 * Default Implementation of {@link PubService}
 */
@ApplicationScoped
public class PubServiceImpl implements PubService {

    @Inject
    private Logger log;

    @Inject
    private QueriesService queriesService;

    @Inject
    private SparqlFunctionsService sparqlFunctionsService;

    @Override
    public void doThis(int i) {
        log.debug("Doing that for {} times...", i);
        for (int j = 0; j < i; j++) {
            doThat();
        }
        log.debug("Did this.");
    }

    @Override
    public void doThat() {
        log.debug("Doing THAT");
    }
    @Inject
    private SparqlService sparqlService;

    @Override
    public String helloWorld(String name) {
        log.debug("Greeting {}", name);
        return "Hello " + name;
    }

    @Override
    public String runPublicationsTaskImpl(String param) {
        try {
            //new AuthorVersioningJob(log).proveSomething();
            ClientConfiguration conf = new ClientConfiguration();
            //conf.addEndpoint(new DBLPEndpoint());
            LDClient ldClient = new LDClient(conf);

            //ClientResponse response = ldClient.retrieveResource("http://rdf.dblp.com/ns/m.0wqhskn");
            int cont_aut = 0;
            String getAuthors = queriesService.getPublicationsQuery();
           
            // TupleQueryResult result = sparqlService.query(QueryLanguage.SPARQL, getAuthors);
            String nameToFind = "";
            String authorUri = "";
            List<Map<String, org.openrdf.model.Value>> result = sparqlService.query(QueryLanguage.SPARQL, getAuthors);
            for (Map<String, org.openrdf.model.Value> map : result) {
                cont_aut++;
                authorUri = map.get("subject").stringValue();
                String firstName = map.get("fname").stringValue();
                String lastName = map.get("lname").stringValue();
                String firstNameA = firstName.split(" ")[0];
                String lastNameA = lastName.split(" ")[0];
                nameToFind = firstNameA + "_" + lastNameA;

                String NS_DBLP = "http://rdf.dblp.com/ns/search/";
                ClientResponse response = ldClient.retrieveResource(NS_DBLP + nameToFind);
                String nameEndpointofPublications = ldClient.getEndpoint(NS_DBLP + nameToFind).getName();
                /*ClientResponse response = ldClient.retrieveResource(
                 "http://dblp.uni-trier.de/search/author?xauthor=Saquicela+Victor");*/
                Model model = response.getData();
                /*response = ldClient.retrieveResource("http://dblp.uni-trier.de/pers/a/Alban:Humberto");
                 model = response.getData();*/
                log.info(model.toString());

                /**
                 * temporal
                 *
                 * get data in memory
                 *
                 */
                RepositoryConnection conUri = ModelCommons.asRepository(response.getData()).getConnection();
                conUri.begin();

                //SPARQL obtain all publications of author
                String pubsparql = "Select distinct ?x  ?z where {       ?x <http://xmlns.com/foaf/0.1/publications> ?z. }";
                TupleQuery pubquery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, pubsparql); //
                TupleQueryResult tripletasResult = pubquery.evaluate();
                while (tripletasResult.hasNext()) {
                    BindingSet tripletsResource = tripletasResult.next();
                    String sujeto = tripletsResource.getValue("x").toString();
                    String objeto = tripletsResource.getValue("z").toString();
                    ///insert sparql query, 
                    String querytoUpdate = buildInsertQuery(authorUri, "http://xmlns.com/foaf/0.1/publications", objeto);

                    //load pulications resource to autor resource
                    updatePub(querytoUpdate);
                    //insert provenance triplet query
                    String provenanceQueryInsert = buildInsertQuery(objeto, queriesService.getProvenanceProperty(), "\"" + nameEndpointofPublications +  "\"");
                    updatePub(provenanceQueryInsert);
                }

                // SPARQL to obtain all data of a publication
             
                
                

                String proppubsparql = "Select ?recpub ?proppub ?objpub where { ?x <http://xmlns.com/foaf/0.1/publications> ?recpub. ?recpub ?proppub ?objpub }";
                TupleQuery resourcequery = conUri.prepareTupleQuery(QueryLanguage.SPARQL, proppubsparql); //
                tripletasResult = resourcequery.evaluate();
                while (tripletasResult.hasNext()) {
                    BindingSet tripletsResource = tripletasResult.next();
                    String sujeto = tripletsResource.getValue("recpub").toString();
                    String predicado = tripletsResource.getValue("proppub").toString();
                    String objeto = tripletsResource.getValue("objpub").toString();
                    ///insert sparql query, 
                    String querytoUpdate = buildInsertQuery(sujeto, predicado, objeto);
                    //load values publications to publications resource
                    updatePub(querytoUpdate);
                }
                //** end View Data
                   conUri.commit();
                            conUri.close();              
//                FileOutputStream out = new FileOutputStream("C:\\Users\\Satellite\\Desktop\\" + nameToFind + "_" + cont_aut + "_test.ttl");
//                RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
//                try {
//                    writer.startRDF();
//                    for (Statement st : model) {
//                        writer.handleStatement(st);
//                    }
//                    writer.endRDF();
//                } catch (RDFHandlerException e) {
//                    // oh no, do something!
//                }
            }
            return "True for publications";
            // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        } catch (MarmottaException ex) {
            log.error("Marmotta Exception " + ex);
            //java.util.logging.Logger.getLogger(PubServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DataRetrievalException ex) {
            log.error("DataRetrievalException " + ex);
            //java.util.logging.Logger.getLogger(PubServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
            //java.util.logging.Logger.getLogger(PubServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            log.error("MalformedExceprtion " + ex);
            //java.util.logging.Logger.getLogger(PubServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            log.error("QueryEvaluationExcception " + ex);
            //java.util.logging.Logger.getLogger(PubServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "fail";
    }

    /*
     *   UPDATE - with SPARQL MODULE, to load triplet in marmotta plataform
     *   
     */
    public String updatePub(String querytoUpdate) {

        try {
            sparqlFunctionsService.updatePub(querytoUpdate);
        } catch (PubException ex) {
            log.error("No se pudo insertar: " + querytoUpdate);
            //         java.util.logging.Logger.getLogger(PubServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Correcto";

    }

    //construyendo sparql query insert 
    public String buildInsertQuery(String sujeto, String predicado, String objeto) {
        if (queriesService.isURI(objeto)) {
            return queriesService.getInsertDataUriQuery(queriesService.getWkhuskaGraph(), sujeto, predicado, objeto);
        } else {
            return queriesService.getInsertDataLiteralQuery(queriesService.getWkhuskaGraph(), sujeto, predicado, objeto);
        }
    }
}
