package org.apache.marmotta.ucuenca.wk.pubman.harvesters;

import com.beust.jcommander.internal.Lists;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.ws.rs.core.UriBuilder;
import org.apache.marmotta.ucuenca.wk.commons.function.Cache;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;

public class WikiCFP {

  private String base;

  public WikiCFP(String base) {
    this.base = base;
  }

  public static void main(String[] args) throws URISyntaxException, IOException, ParseException, RDFHandlerException, InterruptedException {

    WikiCFP x = new WikiCFP("https://redi.cedia.edu.ec/resource/event/");
    Model m = new LinkedHashModel();
    x.query(m, "ecuador");

    Rio.write(m, System.out, RDFFormat.RDFXML);

  }

  public void query(Model m, String q) throws URISyntaxException, IOException, ParseException, InterruptedException {
    URI build = UriBuilder.fromUri("http://www.wikicfp.com/cfp/servlet/tool.search").queryParam("q", q).queryParam("year", "a").build();
    Document get = Jsoup.connect(build.toString()).get();

    Elements select = get.body().select(".contsec > table > tbody > tr > td > table > tbody > tr > td > a");

    for (Element x : select) {
      Thread.sleep(500);
      String cfpURI = x.attr("href");
      queryCFP(m, cfpURI);
    }
  }

  private void queryCFP(Model mdl, String u) throws IOException, ParseException {
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    ValueFactoryImpl instance = ValueFactoryImpl.getInstance();
    URI build = UriBuilder.fromUri("http://www.wikicfp.com" + u).build();
    Element get = Jsoup.connect(build.toString()).get().body();

    String completeName = get.select(".contsec > center > table > tbody > tr > td > h2 > span > span[property=v:description]").text();
    String shortName = get.select(".contsec > center > table > tbody > tr > td > h2 > span > span[property=v:summary]").attr("content");
    String type = get.select(".contsec > center > table > tbody > tr > td > h2 > span > span[property=v:eventType]").attr("content");
    String startDate = get.select(".contsec > center > table > tbody > tr > td > h2 > span > span[property=v:startDate]").attr("content");
    String endDate = get.select(".contsec > center > table > tbody > tr > td > h2 > span > span[property=v:endDate]").attr("content");
    String url = get.select(".contsec > center > table > tbody > tr > td > h2 > span > span[rel=v:url]").attr("resource");
    String location = get.select(".contsec > center > table > tbody > tr > td > h2 > span > span[rel=v:location] > span > span").attr("content");

    String url2 = get.select(".contsec > center > table > tbody > tr > td > a").attr("href");

    Elements unwrap = get.select(".contsec > center > table > tbody > tr > td > table > tbody > tr > td > table > tbody > tr > td >table > tbody > tr > td > h5 > a").not("[class]");
    List<String> keywords = Lists.newArrayList();
    for (Element x : unwrap) {
      keywords.add(x.text());
    }

    String abstractx = get.select(".contsec > center > table > tbody > tr > td > div[class=cfp]").text();
    org.openrdf.model.URI createURI = instance.createURI(this.base + Cache.getMD5(build.toString()));
    mdl.add(createURI, RDF.TYPE, instance.createURI("http://eurocris.org/ontology/cerif#Event"));
    mdl.add(createURI, instance.createURI("http://eurocris.org/ontology/cerif#has_nameAbbreviation"), instance.createLiteral(shortName));
    mdl.add(createURI, instance.createURI("http://eurocris.org/ontology/cerif#has_name"), instance.createLiteral(completeName));
    mdl.add(createURI, instance.createURI("http://eurocris.org/ontology/cerif#has_description"), instance.createLiteral(abstractx));
    mdl.add(createURI, instance.createURI("http://eurocris.org/ontology/cerif#has_URI"), instance.createLiteral(url));
    mdl.add(createURI, instance.createURI("http://eurocris.org/ontology/cerif#has_URI"), instance.createLiteral(url2));
    mdl.add(createURI, instance.createURI("http://eurocris.org/ontology/cerif#has_cityTown"), instance.createLiteral(location));
    for (String sg : keywords) {
      mdl.add(createURI, instance.createURI("http://eurocris.org/ontology/cerif#has_keywords"), instance.createLiteral(sg));
    }

    mdl.add(createURI, instance.createURI("http://eurocris.org/ontology/cerif#has_startDate"), instance.createLiteral(format.parse(startDate)));
    mdl.add(createURI, instance.createURI("http://eurocris.org/ontology/cerif#has_endDate"), instance.createLiteral(format.parse(endDate)));
  }

}
