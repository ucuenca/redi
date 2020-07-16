/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.authors.services.utils;

import com.google.common.collect.Lists;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.commons.function.Cache;
import org.apache.marmotta.ucuenca.wk.commons.service.DisambiguationUtilsService;
import org.apache.tika.io.IOUtils;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author cedia
 */
final public class VIVOExtractor {

    private VIVOExtractor() {
    }

    private static Model safeParse(String nt) throws IOException, RDFParseException {

        ParseErrorListener mock = new ParseErrorListener() {
            @Override
            public void warning(String msg, int lineNo, int colNo) {
                throw new UnsupportedOperationException(msg); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void error(String msg, int lineNo, int colNo) {
                throw new UnsupportedOperationException(msg); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void fatalError(String msg, int lineNo, int colNo) {
                throw new UnsupportedOperationException(msg); //To change body of generated methods, choose Tools | Templates.
            }

        };
        ParserConfig parserConfig = new ParserConfig();
        parserConfig.set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
        Model mm = new LinkedHashModel();
        InputStream toInputStream = IOUtils.toInputStream(nt);
        Model parse = Rio.parse(toInputStream, "https://redi.cedia.edu.ec/context", RDFFormat.RDFXML, parserConfig, ValueFactoryImpl.getInstance(), mock);
        mm.addAll(parse);
        return mm;
    }

    @SuppressWarnings("PMD")
    public static void linkFundingOrganizations(DisambiguationUtilsService dus, Model m, Model sa) throws RepositoryException, MalformedQueryException, QueryEvaluationException, MarmottaException {

        Model funds = extractData(m, "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "construct {\n"
                + "    ?o foaf:name ?la .\n"
                + "} where {\n"
                + "    ?proj <https://www.openaire.eu/cerif-profile/1.1/Funded> ?o .\n"
                + "    ?o foaf:name ?la .\n"
                + "}");
        ConcurrentHashMap<String, Set<String>> mp = new ConcurrentHashMap<>();
        for (Statement st : funds) {
            String per = st.getSubject().stringValue();
            String aff = st.getObject().stringValue();
            if (!mp.containsKey(per)) {
                mp.put(per, new HashSet<String>());
            }
            mp.get(per).add(aff);
        }
        for (Map.Entry<String, Set<String>> it : mp.entrySet()) {
            URI p = ValueFactoryImpl.getInstance().createURI(it.getKey());
            List<String> lookForOrganizations = dus.lookForOrganizations(Lists.newArrayList(it.getValue()));
            for (String org : lookForOrganizations) {
                sa.add(ValueFactoryImpl.getInstance().createURI(org), OWL.SAMEAS, p);
            }
        }

    }

    @SuppressWarnings("PMD")
    public static void linkOrganizations(DisambiguationUtilsService dus, Model m, Model mend, String endp) throws RepositoryException, MalformedQueryException, QueryEvaluationException, MarmottaException {
        Model affs = extractData(m, "PREFIX schema: <http://schema.org/> \n"
                + "construct {\n"
                + "    ?o schema:affiliation ?aff .\n"
                + "} where {\n"
                + "    ?o schema:affiliation ?aff .\n"
                + "}");
        ConcurrentHashMap<String, Set<String>> mp = new ConcurrentHashMap<>();
        for (Statement st : affs) {
            String per = st.getSubject().stringValue();
            String aff = st.getObject().stringValue();
            if (!mp.containsKey(per)) {
                mp.put(per, new HashSet<String>());
            }
            mp.get(per).add(aff);
        }
        for (Map.Entry<String, Set<String>> it : mp.entrySet()) {
            URI p = ValueFactoryImpl.getInstance().createURI(it.getKey());
            List<String> lookForOrganizations = dus.lookForOrganizations(Lists.newArrayList(it.getValue()));
            for (String org : lookForOrganizations) {
                String mockendp = endp + "_mock_" + org.hashCode();
                URI createURI = ValueFactoryImpl.getInstance().createURI(mockendp);
                URI uriOrg = ValueFactoryImpl.getInstance().createURI(org);
                m.add(p, ValueFactoryImpl.getInstance().createURI("http://purl.org/dc/terms/provenance"), createURI);

                mend.add(createURI, RDF.TYPE, ValueFactoryImpl.getInstance().createURI("http://ucuenca.edu.ec/ontology#Endpoint"));
                mend.add(createURI, ValueFactoryImpl.getInstance().createURI("http://ucuenca.edu.ec/ontology#belongTo"), uriOrg);
                mend.add(createURI, ValueFactoryImpl.getInstance().createURI("http://ucuenca.edu.ec/ontology#type"), ValueFactoryImpl.getInstance().createLiteral("vivo"));
                mend.add(createURI, ValueFactoryImpl.getInstance().createURI("http://ucuenca.edu.ec/ontology#status"), ValueFactoryImpl.getInstance().createLiteral("Active"));
            }
        }
    }

    @SuppressWarnings("PMD")
    public static void download(String tipe, String base, Model str, Resource p, String parent, String endp, boolean recursive, String baseAuthURI, Model mapAuthURI) throws UnirestException, IOException, RDFParseException, Exception {
        HttpResponse<String> respn = null;
        respn = Unirest.get(base + "/individual?uri=" + URLEncoder.encode(p.stringValue()) + "&format=rdfxml").asString();

        if (respn.getStatus() == 200) {

            Model parse = safeParse(respn.getBody());
            switch (tipe) {
                case "person":

                    URI newURIAuth = ValueFactoryImpl.getInstance().createURI(baseAuthURI + Cache.getMD5(p.stringValue()));
                    mapAuthURI.add(newURIAuth, OWL.SAMEAS, p);
                    str.add(p, FOAF.HOLDS_ACCOUNT, p);

                    //str.add(p, ValueFactoryImpl.getInstance().createURI("http://purl.org/dc/terms/provenance"), ValueFactoryImpl.getInstance().createURI(endp));
                    str.addAll(extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "construct { "
                            + "?pp a foaf:Person ."
                            + "?pp foaf:name ?l . "
                            + "?pp foaf:skypeID ?skyp . "
                            + "} where { values ?pp {<" + p.stringValue() + ">} . "
                            + "?pp rdfs:label ?l . "
                            + "optional { ?pp foaf:skypeID ?skyp . } . "
                            + "}"));
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
                        download("vcard", base, str, (Resource) x, p.stringValue(), endp, true, baseAuthURI, mapAuthURI);
                    }
                    vls = parse.filter(p, ValueFactoryImpl.getInstance().createURI("http://vivoweb.org/ontology/core#relatedBy"), null).objects();
                    for (Value x : vls) {
                        if (!parse.filter((Resource) x, RDF.TYPE, ValueFactoryImpl.getInstance().createURI("http://vivoweb.org/ontology/core#Position")).objects().isEmpty()) {
                            download("Position", base, str, (Resource) x, p.stringValue(), endp, true, baseAuthURI, mapAuthURI);
                        }
                        if (!parse.filter((Resource) x, RDF.TYPE, ValueFactoryImpl.getInstance().createURI("http://vivoweb.org/ontology/core#Authorship")).objects().isEmpty()) {
                            download("Authorship", base, str, (Resource) x, null, endp, true, baseAuthURI, mapAuthURI);
                        }
                        if (!parse.filter((Resource) x, RDF.TYPE, ValueFactoryImpl.getInstance().createURI("http://vivoweb.org/ontology/core#Grant")).objects().isEmpty()) {
                            download("Grant", base, str, (Resource) x, null, endp, true, baseAuthURI, mapAuthURI);
                        }

                    }
                    break;
                case "proj_intv_start":
                    str.addAll(extractData(parse, "construct {\n"
                            + "    <" + parent + "> <https://www.openaire.eu/cerif-profile/1.1/StartDate> ?i .\n"
                            + "} where {\n"
                            + "    ?a <http://vivoweb.org/ontology/core#dateTime> ?i .\n"
                            + "}"));
                    break;
                case "proj_intv_end":
                    str.addAll(extractData(parse, "construct {\n"
                            + "    <" + parent + "> <https://www.openaire.eu/cerif-profile/1.1/EndDate> ?i .\n"
                            + "} where {\n"
                            + "    ?a <http://vivoweb.org/ontology/core#dateTime> ?i .\n"
                            + "}"));
                    break;
                case "proj_intv":
                    Set<Value> intvx = extractData(parse, "construct {\n"
                            + "    ?a <http://vivoweb.org/ontology/core#start> ?i .\n"
                            + "} where {\n"
                            + "    ?a <http://vivoweb.org/ontology/core#start> ?i .\n"
                            + "}").objects();
                    for (Value x : intvx) {
                        download("proj_intv_start", base, str, (Resource) x, parent, endp, true, baseAuthURI, mapAuthURI);
                    }
                    intvx = extractData(parse, "construct {\n"
                            + "    ?a <http://vivoweb.org/ontology/core#end> ?i .\n"
                            + "} where {\n"
                            + "    ?a <http://vivoweb.org/ontology/core#end> ?i .\n"
                            + "}").objects();
                    for (Value x : intvx) {
                        download("proj_intv_end", base, str, (Resource) x, parent, endp, true, baseAuthURI, mapAuthURI);
                    }

                    break;

                case "Grant":
                    str.addAll(extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                            + "PREFIX schema: <http://schema.org/> "
                            + "construct { "
                            + " ?pp a <http://xmlns.com/foaf/0.1/Project> . "
                            + " ?pp <https://www.openaire.eu/cerif-profile/1.1/linksToPerson> ?l . "
                            + " ?l <https://www.openaire.eu/cerif-profile/1.1/MemberOf> ?pp ."
                            + " ?pp <http://purl.org/dc/terms/title> ?la ."
                            + "} where { values ?pp {<" + p.stringValue() + ">} . "
                            + "?pp rdfs:label ?la ."
                            + "?pp <http://vivoweb.org/ontology/core#relates> ?l . "
                            + "?l a <http://xmlns.com/foaf/0.1/Person> . "
                            + "}"));

                    str.addAll(extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                            + "PREFIX schema: <http://schema.org/> "
                            + "construct { "
                            + " ?o a <http://xmlns.com/foaf/0.1/Organization> . "
                            + " ?o foaf:name ?la . "
                            + " ?proj <https://www.openaire.eu/cerif-profile/1.1/Funded> ?o . "
                            + "} where { values ?proj { <" + p.stringValue() + "> } . "
                            + "?proj <http://vivoweb.org/ontology/core#assignedBy> ?o . "
                            + "?o rdfs:label ?la . "
                            + "}"));

                    Set<Value> intv = extractData(parse, "construct {\n"
                            + "    ?a <http://vivoweb.org/ontology/core#dateTimeInterval> ?i .\n"
                            + "} where {\n"
                            + "    ?a <http://vivoweb.org/ontology/core#dateTimeInterval> ?i .\n"
                            + "}").objects();
                    for (Value x : intv) {
                        download("proj_intv", base, str, (Resource) x, p.stringValue(), endp, true, baseAuthURI, mapAuthURI);
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
                            download("document", base, str, x, p.stringValue(), endp, true, baseAuthURI, mapAuthURI);
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
                            download("Authorship", base, str, (Resource) x, "creator", endp, false, baseAuthURI, mapAuthURI);
                        } else {
                            //contributor
                            download("Authorship", base, str, (Resource) x, null, endp, false, baseAuthURI, mapAuthURI);
                        }
                        c++;
                    }

                    str.addAll(extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "construct { "
                            + "?pp a <http://purl.org/ontology/bibo/AcademicArticle> ."
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
                        download("dateTime", base, str, (Resource) x, p.stringValue(), endp, true, baseAuthURI, mapAuthURI);
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
                        download("journal", base, str, (Resource) x, p.stringValue(), endp, true, baseAuthURI, mapAuthURI);
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

                    str.addAll(extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                            + "PREFIX schema: <http://schema.org/> "
                            + "construct { "
                            + "<" + parent + "> schema:affiliation ?aff . "
                            + "} where { values ?pp {<" + p.stringValue() + ">} . ?pp <http://vivoweb.org/ontology/core#relates> ?l . "
                            + "?l a <http://vivoweb.org/ontology/core#Department> . ?l rdfs:label ?aff .}"));
                    break;
                case "vcard":
                    Set<Value> vlsx = extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "construct { "
                            + "?pp <http://www.w3.org/2006/vcard/ns#hasName> ?l ."
                            + "} where { values ?pp {<" + p.stringValue() + ">} . ?pp <http://www.w3.org/2006/vcard/ns#hasName> ?l . }").objects();
                    for (Value x : vlsx) {
                        download("vcard_name", base, str, (Resource) x, parent, endp, true, baseAuthURI, mapAuthURI);
                    }
                    vlsx = extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "construct { "
                            + "?pp <http://www.w3.org/2006/vcard/ns#hasEmail> ?l ."
                            + "} where { values ?pp {<" + p.stringValue() + ">} . ?pp <http://www.w3.org/2006/vcard/ns#hasEmail> ?l . }").objects();
                    for (Value x : vlsx) {
                        download("vcard_email", base, str, (Resource) x, parent, endp, true, baseAuthURI, mapAuthURI);
                    }
                    vlsx = extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "construct { "
                            + "?pp <http://www.w3.org/2006/vcard/ns#hasTelephone> ?l ."
                            + "} where { values ?pp {<" + p.stringValue() + ">} . ?pp <http://www.w3.org/2006/vcard/ns#hasTelephone> ?l . }").objects();
                    for (Value x : vlsx) {
                        download("vcard_phone", base, str, (Resource) x, parent, endp, true, baseAuthURI, mapAuthURI);
                    }
                    break;
                case "vcard_phone":
                    str.addAll(extractData(parse, "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                            + "PREFIX vcard: <http://www.w3.org/2006/vcard/ns#>"
                            + "construct { "
                            + "<" + parent + "> foaf:phone ?phone ."
                            + "} where { values ?pp {<" + p.stringValue() + ">} . ?pp <http://www.w3.org/2006/vcard/ns#telephone> ?phone . }"));
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
