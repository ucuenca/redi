/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.utils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 *
 * @author cedia
 */
public class ScopusMapper {

  private JSONObject raw;

  private Model scopus;
  private Model endpoints;
  private Model authors;

  public ScopusMapper(JSONObject raw) {
    this.raw = raw;
    scopus = new LinkedHashModel();
    endpoints = new LinkedHashModel();
    authors = new LinkedHashModel();

  }

  public List<String> obtainAuthors(String aff, String org) {
    List<String> auths = new ArrayList();
    ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
    String aur = "https://api.elsevier.com/content/affiliation/affiliation_id/" + aff;
    String aurx = "https://redi.cedia.edu.ec/resource/scopus_endpoint/affiliation_id/" + aff;
    Set<Resource> subjects = scopus.filter(null, instance.createURI("http://schema.org/memberOf"), instance.createURI(aur)).subjects();
    for (Resource rux : subjects) {
      auths.add(((URI) rux).getLocalName());
      String aurxx = "https://redi.cedia.edu.ec/resource/scopus_author/" + ((URI) rux).getLocalName();
      URI ru = instance.createURI(aurxx);
      for (Statement st : scopus.filter(rux, RDF.TYPE, null)) {
        authors.add(ru, st.getPredicate(), st.getObject());
      }
      for (Statement st : scopus.filter(rux, FOAF.NAME, null)) {
        authors.add(ru, st.getPredicate(), st.getObject());
      }
      for (Statement st : scopus.filter(rux, FOAF.GIVEN_NAME, null)) {
        authors.add(ru, FOAF.FIRST_NAME, st.getObject());
      }
      for (Statement st : scopus.filter(rux, FOAF.FAMILY_NAME, null)) {
        authors.add(ru, FOAF.LAST_NAME, st.getObject());
      }
      authors.add(ru, instance.createURI("http://purl.org/dc/terms/provenance"), instance.createURI(aurx));
      endpoints.add(instance.createURI(aurx), RDF.TYPE, instance.createURI("http://ucuenca.edu.ec/ontology#Endpoint"));
      endpoints.add(instance.createURI(aurx), instance.createURI("http://ucuenca.edu.ec/ontology#belongTo"), instance.createURI(org));
      //OneOf
      String mquery = "https://redi.cedia.edu.ec/resource/mock_query/" + aff + "_" + ((URI) rux).getLocalName();
      scopus.add(instance.createURI(mquery), OWL.ONEOF, ru);
      scopus.add(rux, OWL.ONEOF, instance.createURI(mquery));

    }
    return auths;
  }

  public void run() {

    //Scopus
    ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
    URI pubURI = instance.createURI(this.raw.getString("prism:url"));

    scopus.add(pubURI, RDF.TYPE, instance.createURI("http://purl.org/ontology/bibo/AcademicArticle"));
    addStatementIfExists(scopus, pubURI, instance.createURI("http://purl.org/dc/terms/title"), this.raw, "dc:title");
    addStatementIfExists(scopus, pubURI, instance.createURI("http://purl.org/ontology/bibo/abstract"), this.raw, "dc:description");
    addStatementIfExists(scopus, pubURI, instance.createURI("http://purl.org/ontology/bibo/issn"), this.raw, "prism:issn");
    addStatementIfExists(scopus, pubURI, instance.createURI("http://purl.org/ontology/bibo/issn"), this.raw, "prism:eIssn");
    addStatementIfExists(scopus, pubURI, instance.createURI("http://ns.nature.com/terms/coverDate"), this.raw, "prism:coverDate");
    addStatementIfExists(scopus, pubURI, instance.createURI("http://ucuenca.edu.ec/ontology#scopusId"), this.raw, "dc:identifier", true);
    addStatementIfExists(scopus, pubURI, instance.createURI("http://purl.org/ontology/bibo/doi"), this.raw, "prism:doi");
    extractAndAddStatementIfExists(scopus, pubURI, instance.createURI("http://purl.org/ontology/bibo/isbn"), this.raw, "prism:isbn");

    String[] kw = this.raw.has("authkeywords") ? this.raw.getString("authkeywords").split("\\|") : new String[0];
    for (String k : kw) {
      String nk = k.trim();
      String kuri = "https://api.elsevier.com/content/keyword/" + URLEncoder.encode(nk);
      scopus.add(instance.createURI(kuri), RDFS.LABEL, instance.createLiteral(nk));
      scopus.add(pubURI, instance.createURI("http://purl.org/dc/terms/subject"), instance.createURI(kuri));
    }

    JSONArray links = this.raw.getJSONArray("link");
    for (int i = 0; i < links.length(); i++) {
      String link = links.getJSONObject(i).getString("@href");
      scopus.add(pubURI, instance.createURI("http://purl.org/ontology/bibo/uri"), instance.createLiteral(link));
    }

    String jou = this.raw.getString("prism:publicationName");
    String ty = this.raw.getString("prism:aggregationType");

    String kuri = "https://api.elsevier.com/content/aggregation/" + URLEncoder.encode(jou);
    scopus.add(pubURI, instance.createURI("http://purl.org/dc/terms/isPartOf"), instance.createURI(kuri));
    scopus.add(instance.createURI(kuri), instance.createURI("http://ucuenca.edu.ec/ontology#index"), instance.createURI("http://ucuenca.edu.ec/ontology#ScopusProvider"));
    scopus.add(instance.createURI(kuri), RDFS.LABEL, instance.createLiteral(jou));
    switch (ty) {
      case "Conference Proceeding":
        scopus.add(instance.createURI(kuri), RDF.TYPE, instance.createURI("http://purl.org/ontology/bibo/Conference"));
        break;
      case "Book":
      case "Book Series":
        scopus.add(instance.createURI(kuri), RDF.TYPE, instance.createURI("http://purl.org/ontology/bibo/Proceedings"));
        break;
      case "Journal":
      default:
        scopus.add(instance.createURI(kuri), RDF.TYPE, instance.createURI("http://purl.org/ontology/bibo/Journal"));
        break;
    }

    JSONArray aff = this.raw.has("affiliation") ? this.raw.getJSONArray("affiliation") : new JSONArray();
    for (int i = 0; i < aff.length(); i++) {
      String afu = aff.getJSONObject(i).getString("affiliation-url");
      scopus.add(instance.createURI(afu), RDF.TYPE, FOAF.ORGANIZATION);
      scopus.add(instance.createURI(afu), FOAF.NAME, instance.createLiteral(aff.getJSONObject(i).getString("affilname")));

    }
    JSONArray aut = this.raw.getJSONArray("author");
    for (int i = 0; i < aut.length(); i++) {
      String afu = aut.getJSONObject(i).getString("author-url");
      String name = aut.getJSONObject(i).getString("authname");
      String suname = aut.getJSONObject(i).getString("surname");
      String gname = aut.getJSONObject(i).getString("given-name");
      scopus.add(instance.createURI(afu), RDF.TYPE, FOAF.PERSON);
      scopus.add(instance.createURI(afu), FOAF.NAME, instance.createLiteral(name));
      scopus.add(instance.createURI(afu), FOAF.GIVEN_NAME, instance.createLiteral(gname));
      scopus.add(instance.createURI(afu), FOAF.FAMILY_NAME, instance.createLiteral(suname));
      scopus.add(instance.createURI(afu), FOAF.PUBLICATIONS, pubURI);
      if (i == 0) {
        scopus.add(pubURI, instance.createURI("http://purl.org/dc/terms/creator"), instance.createURI(afu));
      }
      scopus.add(pubURI, instance.createURI("http://purl.org/dc/terms/contributor"), instance.createURI(afu));

      JSONArray jsonArray = aut.getJSONObject(i).has("afid") ? aut.getJSONObject(i).getJSONArray("afid") : new JSONArray();
      for (int j = 0; j < jsonArray.length(); j++) {
        String idu = jsonArray.getJSONObject(j).getString("$");
        String aur = "https://api.elsevier.com/content/affiliation/affiliation_id/" + idu;
        scopus.add(instance.createURI(afu), instance.createURI("http://schema.org/memberOf"), instance.createURI(aur));
      }

    }

  }

  private void addStatementIfExists(Model m, URI s, URI p, JSONObject obj, String pro) {
    addStatementIfExists(m, s, p, obj, pro, false);
  }

  private void addStatementIfExists(Model m, URI s, URI p, JSONObject obj, String pro, boolean removeLetters) {
    if (obj.has(pro)) {
      if (removeLetters) {
        m.add(s, p, ValueFactoryImpl.getInstance().createLiteral(obj.getString(pro).replaceAll("[^\\d]", "")));
      } else {
        m.add(s, p, ValueFactoryImpl.getInstance().createLiteral(obj.getString(pro)));
      }
    }
  }

  private void extractAndAddStatementIfExists(Model m, URI s, URI p, JSONObject obj, String pro) {
    if (obj.has(pro)) {
      JSONArray arr = obj.getJSONArray(pro);
      for (int i = 0; i < arr.length(); i++) {
        JSONObject jsonObject = arr.getJSONObject(i);
        m.add(s, p, ValueFactoryImpl.getInstance().createLiteral(jsonObject.getString("$")));
      }

    }
  }

  public Model getScopus() {
    return scopus;
  }

  public Model getEndpoints() {
    return endpoints;
  }

  public Model getAuthors() {
    return authors;
  }

}
