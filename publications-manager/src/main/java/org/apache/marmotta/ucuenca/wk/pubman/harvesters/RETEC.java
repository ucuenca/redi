/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.harvesters;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Maps;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.commons.service.DisambiguationUtilsService;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;

/**
 *
 * @author cedia
 */
public class RETEC {

  private String retecUrl;
  private String retecUrlApi;
  private String btoken;
  private String user;
  private String pass;
  private String base;
  private String token;
  private DisambiguationUtilsService utls;

  public RETEC(String retecUrl, String retecUrlApi, String btoken, String user, String pass, String base) {
    this.retecUrl = retecUrl;
    this.retecUrlApi = retecUrlApi;
    this.btoken = btoken;
    this.user = user;
    this.pass = pass;
    this.base = base;
  }

  public Model harvest(DisambiguationUtilsService utls) throws RDFHandlerException, ParseException, MarmottaException {
    this.utls = utls;
    Model m = new LinkedHashModel();
    Model offers = offers();
    m.addAll(offers);

    return m;
  }

  public Model offers() throws ParseException, MarmottaException {
    ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
    Model m = new LinkedHashModel();
    String off = this.retecUrlApi + "/bussines/offers?fields=T&createdBy=00000000-0000-0000-0000-000000000000&regStatus=C,H,P,X,E&institutionIds=00000000-0000-0000-0000-000000000000";
    int page = 0;
    int total = 1;
    Map<String, String> loadAreas = loadAreas();

    while (page < total) {
      JSONObject object = new JSONObject(Unirest.get(off)
              .header("Authorization", "Bearer " + this.token)
              .header("X-API-VERSION", "1")
              .queryString("page", page).queryString("size", "20").asString().getBody());
      total = object.getJSONObject("offers").getInt("totalPages");

      JSONArray jsonArray = object.getJSONObject("offers").getJSONArray("content");

      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject offer = jsonArray.getJSONObject(i);
        String string = offer.getString("createdIn");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date dd = sdf.parse(string);
        String id = offer.getString("id");
        String name = offer.getString("name");
        String description = offer.getString("description");
        String url = this.retecUrl + "/search/item/" + offer.getString("id");
        List<String> keywords = Lists.newArrayList();
        JSONArray jsonArray1 = offer.getJSONArray("keywords");
        for (int j = 0; j < jsonArray1.length(); j++) {
          keywords.add(jsonArray1.getString(j));
        }
        jsonArray1 = offer.getJSONArray("serviceTypes");
        List<String> types = Lists.newArrayList();
        for (int j = 0; j < jsonArray1.length(); j++) {
          for (int k = 0; k < object.getJSONArray("types").length(); k++) {
            if (jsonArray1.getString(j).equals(object.getJSONArray("types").getJSONObject(k).getString("id"))) {
              types.add(object.getJSONArray("types").getJSONObject(k).getString("typeOrCategory"));
            }
          }
        }

        JSONObject inst = offer.get("institution") != null ? offer.getJSONObject("institution") : null;

        String institution = null;
        List<Value> rls = Lists.newArrayList();
        if (inst != null) {
          institution = inst.getString("institutionRootName");

          List<String> lookForOrganizations = utls.lookForOrganizations(Lists.newArrayList(institution));
          for (String ourl : lookForOrganizations) {
            rls.add(ValueFactoryImpl.getInstance().createURI(ourl));
          }
          if (rls.isEmpty()) {
            rls.add(ValueFactoryImpl.getInstance().createLiteral(institution));
          }

        }

        List<String> areas = Lists.newArrayList();
        jsonArray1 = offer.getJSONArray("scientificDisciplines");
        for (int j = 0; j < jsonArray1.length(); j++) {
          areas.add(loadAreas.get(jsonArray1.getString(j)));
        }
        URI ouri = instance.createURI(this.base + id);

        m.add(ouri, RDF.TYPE, instance.createURI("http://eurocris.org/ontology/cerif#Service"));
        m.add(ouri, instance.createURI("http://eurocris.org/ontology/cerif#has_startDate"), instance.createLiteral(dd));
        m.add(ouri, instance.createURI("http://eurocris.org/ontology/cerif#has_name"), instance.createLiteral(name));
        m.add(ouri, instance.createURI("http://eurocris.org/ontology/cerif#has_description"), instance.createLiteral(description));
        m.add(ouri, instance.createURI("http://eurocris.org/ontology/cerif#has_URI"), instance.createLiteral(url));
        for (String sg : keywords) {
          m.add(ouri, instance.createURI("http://eurocris.org/ontology/cerif#has_keywords"), instance.createLiteral(sg));
        }
        for (Value d : rls) {
          m.add(ouri, instance.createURI("http://eurocris.org/ontology/cerif#has_organisationUnit"), d);
        }
        for (String sg : areas) {
          m.add(ouri, instance.createURI("http://eurocris.org/ontology/cerif#has_researchArea"), instance.createLiteral(sg));
        }
        for (String sg : types) {
          m.add(ouri, instance.createURI("http://eurocris.org/ontology/cerif#has_serviceType"), instance.createLiteral(sg));
        }

      }

      page++;
    }
    return m;
  }

  public Map<String, String> loadAreas() {
    String url = this.retecUrlApi + "/bussines/scientificDisciplines/byLevels?levels=2&page=0&size=100";
    JSONObject object = new JSONObject(Unirest.get(url)
            .header("Authorization", "Bearer " + this.token)
            .header("X-API-VERSION", "1").asString().getBody());

    Map<String, String> areas = Maps.newConcurrentMap();
    JSONArray jsonArray = object.getJSONArray("content");
    for (int i = 0; i < jsonArray.length(); i++) {
      JSONObject jsonObject = jsonArray.getJSONObject(i);
      areas.put(jsonObject.getString("id"), jsonObject.getString("disciplineOrClassification"));
    }
    return areas;
  }

  public void login() {
    String loginUrl = this.retecUrlApi + "/auth/oauth/token";
    JSONObject body = new JSONObject(Unirest.post(loginUrl)
            .field("grant_type", "password")
            .field("username", this.user)
            .field("password", this.pass)
            .header("Authorization", this.btoken)
            .asString().getBody());
    this.token = body.getString("access_token");
  }

}
