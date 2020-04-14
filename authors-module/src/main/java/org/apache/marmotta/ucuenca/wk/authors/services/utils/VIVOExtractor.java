/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.authors.services.utils;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Set;
import org.apache.tika.io.IOUtils;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author cedia
 */
final public class VIVOExtractor {

    private VIVOExtractor() {
    }
    
    
    @SuppressWarnings("PMD")
    public static void download(String tipe, String base, Model str, Resource p, String parent, String endp, boolean recursive) throws UnirestException, IOException, RDFParseException, Exception {
        HttpResponse<String> respn = null;
        respn = Unirest.get(base + "/individual?uri=" + URLEncoder.encode(p.stringValue()) + "&format=rdfxml").asString();

        if (respn.getStatus() == 200) {
            InputStream toInputStream = IOUtils.toInputStream(respn.getBody());
            Model parse = Rio.parse(toInputStream, "https://redi.cedia.edu.ec/context", RDFFormat.RDFXML);
            switch (tipe) {
                case "person":
                    str.add(p, ValueFactoryImpl.getInstance().createURI("http://purl.org/dc/terms/provenance"), ValueFactoryImpl.getInstance().createURI(endp));
                    str.addAll(extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "construct { "
                            + "?pp a foaf:Person ."
                            + "?pp foaf:name ?l . "
                            + "} where { values ?pp {<" + p.stringValue() + ">} . ?pp rdfs:label ?l . }"));
                    str.addAll(extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "construct { "
                            + "?pp <http://xmlns.com/foaf/0.1/topic_interest> ?l ."
                            + "?l rdfs:label ?ll . "
                            + "} where { values ?pp {<" + p.stringValue() + ">} . "
                            + "?pp <http://vivoweb.org/ontology/core#hasResearchArea> ?l . "
                            + "?l rdfs:label ?ll ."
                            + "}"));

                    Set<Value> vls = parse.filter(p, ValueFactoryImpl.getInstance().createURI("http://purl.obolibrary.org/obo/ARG_2000028"), null).objects();
                    for (Value x : vls) {
                        download("vcard", base, str, (Resource) x, p.stringValue(), endp, true);
                    }
                    vls = parse.filter(p, ValueFactoryImpl.getInstance().createURI("http://vivoweb.org/ontology/core#relatedBy"), null).objects();
                    for (Value x : vls) {
                        if (!parse.filter((Resource) x, RDF.TYPE, ValueFactoryImpl.getInstance().createURI("http://vivoweb.org/ontology/core#Position")).objects().isEmpty()) {
                            download("Position", base, str, (Resource) x, p.stringValue(), endp, true);
                        }
                        if (!parse.filter((Resource) x, RDF.TYPE, ValueFactoryImpl.getInstance().createURI("http://vivoweb.org/ontology/core#Authorship")).objects().isEmpty()) {
                            download("Authorship", base, str, (Resource) x, null, endp, true);
                        }

                    }
                    break;
                case "Authorship":
                    str.addAll(extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                            + "PREFIX schema: <http://schema.org/> "
                            + "construct { "
                            + " ?l <http://xmlns.com/foaf/0.1/publications> ?lx. "
                            + "} where { values ?pp {<" + p.stringValue() + ">} . "
                            + "?pp <http://vivoweb.org/ontology/core#relates> ?l . "
                            + "?pp <http://vivoweb.org/ontology/core#relates> ?lx ."
                            + "?l a <http://xmlns.com/foaf/0.1/Person> . "
                            + "?lx a <http://purl.org/ontology/bibo/Document> . "
                            + "}"));

                    if (parent != null) {
                        str.addAll(extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                                + "PREFIX schema: <http://schema.org/> "
                                + "construct { "
                                + " ?lx <http://purl.org/dc/terms/creator> ?l. "
                                + "} where { values ?pp {<" + p.stringValue() + ">} . "
                                + "?pp <http://vivoweb.org/ontology/core#relates> ?l . "
                                + "?pp <http://vivoweb.org/ontology/core#relates> ?lx ."
                                + "?l a <http://xmlns.com/foaf/0.1/Person> . "
                                + "?lx a <http://purl.org/ontology/bibo/Document> . "
                                + "}"));
                    } else {
                        str.addAll(extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                                + "PREFIX schema: <http://schema.org/> "
                                + "construct { "
                                + " ?lx <http://purl.org/dc/terms/contributor> ?l. "
                                + "} where { values ?pp {<" + p.stringValue() + ">} . "
                                + "?pp <http://vivoweb.org/ontology/core#relates> ?l . "
                                + "?pp <http://vivoweb.org/ontology/core#relates> ?lx ."
                                + "?l a <http://xmlns.com/foaf/0.1/Person> . "
                                + "?lx a <http://purl.org/ontology/bibo/Document> . "
                                + "}"));
                    }
                    Set<Resource> rspl = extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                            + "PREFIX schema: <http://schema.org/> "
                            + "construct { "
                            + " ?l a <http://purl.org/ontology/bibo/Document> . "
                            + "} where { values ?pp {<" + p.stringValue() + ">} . ?pp <http://vivoweb.org/ontology/core#relates> ?l . "
                            + "?l a <http://purl.org/ontology/bibo/Document> . }").subjects();
                    for (Resource x : rspl) {
                        if (recursive) {
                            download("document", base, str, x, p.stringValue(), endp, true);
                        }
                    }
                    break;
                case "document":
                    vls = extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                            + "PREFIX schema: <http://schema.org/> "
                            + "construct { "
                            + " ?pp <http://vivoweb.org/ontology/core#relatedBy> ?auth . "
                            + "} where { "
                            + "values ?pp {<" + p.stringValue() + ">} . "
                            + "?pp a <http://purl.org/ontology/bibo/Document> . "
                            + "?pp <http://vivoweb.org/ontology/core#relatedBy> ?auth ."
                            + "?auth a <http://vivoweb.org/ontology/core#Authorship> ."
                            + "}").objects();
                    int c = 0;
                    for (Value x : vls) {
                        if (c == 0) {
                            download("Authorship", base, str, (Resource) x, "creator", endp, false);
                        } else {
                            //contributor
                            download("Authorship", base, str, (Resource) x, null, endp, false);
                        }
                        c++;
                    }

                    str.addAll(extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "construct { "
                            + "?pp <http://purl.org/dc/terms/title> ?ll."
                            + "?pp <http://purl.org/ontology/bibo/abstract> ?a. "
                            + "} where { values ?pp {<" + p.stringValue() + ">} . "
                            + "?pp rdfs:label ?ll ."
                            + "optional { ?pp <http://purl.org/ontology/bibo/abstract> ?a}"
                            + "}"));

                    vls = extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                            + "PREFIX schema: <http://schema.org/> "
                            + "construct { "
                            + " ?pp <http://vivoweb.org/ontology/core#dateTimeValue> ?da . "
                            + "} where { "
                            + "values ?pp {<" + p.stringValue() + ">} . "
                            + "?pp <http://vivoweb.org/ontology/core#dateTimeValue> ?da ."
                            + "}").objects();
                    for (Value x : vls) {
                        download("dateTime", base, str, (Resource) x, p.stringValue(), endp, true);
                    }

                    vls = extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                            + "PREFIX schema: <http://schema.org/> "
                            + "construct { "
                            + " ?pp <http://vivoweb.org/ontology/core#hasPublicationVenue> ?da . "
                            + "} where { "
                            + "values ?pp {<" + p.stringValue() + ">} . "
                            + "?pp <http://vivoweb.org/ontology/core#hasPublicationVenue> ?da ."
                            + "}").objects();
                    for (Value x : vls) {
                        str.add(p, ValueFactoryImpl.getInstance().createURI("http://purl.org/dc/terms/isPartOf"), x);
                        download("journal", base, str, (Resource) x, p.stringValue(), endp, true);
                    }

                    ////////////////////
                    break;
                case "journal":

                    str.addAll(extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "construct { "
                            + "?pp a <http://purl.org/ontology/bibo/Journal>."
                            + "?pp rdfs:label ?ll ."
                            + "?pp <http://purl.org/ontology/bibo/issn> ?is ."
                            + "} where { values ?pp {<" + p.stringValue() + ">} . "
                            + "?pp rdfs:label ?ll ."
                            + "optional { ?pp <http://purl.org/ontology/bibo/issn> ?is .}"
                            + "}"));

                    break;
                case "dateTime":
                    str.addAll(extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "construct { "
                            + "<" + parent + "> <http://ns.nature.com/terms/coverDate> ?tm."
                            + "} where { values ?pp {<" + p.stringValue() + ">} . "
                            + "?pp <http://vivoweb.org/ontology/core#dateTime> ?tm ."
                            + "}"));
                    break;

                case "Position":
                    str.addAll(extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                            + "PREFIX schema: <http://schema.org/> "
                            + "construct { "
                            + "<" + parent + "> schema:affiliation ?aff . "
                            + "} where { values ?pp {<" + p.stringValue() + ">} . ?pp <http://vivoweb.org/ontology/core#relates> ?l . "
                            + "?l a <http://vivoweb.org/ontology/core#University> . ?l rdfs:label ?aff .}"));
                    break;
                case "vcard":
                    Set<Value> vlsx = extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "construct { "
                            + "?pp <http://www.w3.org/2006/vcard/ns#hasName> ?l ."
                            + "} where { values ?pp {<" + p.stringValue() + ">} . ?pp <http://www.w3.org/2006/vcard/ns#hasName> ?l . }").objects();
                    for (Value x : vlsx) {
                        download("vcard_name", base, str, (Resource) x, parent, endp, true);
                    }
                    vlsx = extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "construct { "
                            + "?pp <http://www.w3.org/2006/vcard/ns#hasEmail> ?l ."
                            + "} where { values ?pp {<" + p.stringValue() + ">} . ?pp <http://www.w3.org/2006/vcard/ns#hasEmail> ?l . }").objects();
                    for (Value x : vlsx) {
                        download("vcard_email", base, str, (Resource) x, parent, endp, true);
                    }
                    break;
                case "vcard_name":
                    str.addAll(extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "construct { "
                            + "<" + parent + "> foaf:firstName ?first . "
                            + "<" + parent + "> foaf:lastName ?last . "
                            + "} where { values ?pp {<" + p.stringValue() + ">} . ?pp <http://www.w3.org/2006/vcard/ns#givenName> ?first ."
                            + "?pp <http://www.w3.org/2006/vcard/ns#familyName> ?last . }"));
                    break;
                case "vcard_email":
                    str.addAll(extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "PREFIX vcard: <http://www.w3.org/2006/vcard/ns#>"
                            + "construct { "
                            + "<" + parent + "> vcard:hasEmail ?email ."
                            + "} where { values ?pp {<" + p.stringValue() + ">} . ?pp <http://www.w3.org/2006/vcard/ns#email> ?email . }"));
                    break;
            }

        } else {
            throw new Exception("Invalid VIVO URI " + p.stringValue());
        }
    }

    public static Model extractData(Model modelIn, String spq) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        Model md = new LinkedHashModel();
        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();
        RepositoryConnection connection = repository.getConnection();
        connection.begin();
        connection.add(modelIn);
        connection.commit();
        GraphQueryResult evaluate = connection.prepareGraphQuery(QueryLanguage.SPARQL, spq).evaluate();
        while (evaluate.hasNext()) {
            md.add(evaluate.next());
        }
        connection.close();
        repository.shutDown();

        return md;
    }

}
