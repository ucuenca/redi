package cepra.vivo;

import com.beust.jcommander.internal.Lists;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.core.UriBuilder;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;

public class WikiCFP {

  public static void main(String[] args) throws URISyntaxException, IOException {

    WikiCFP x = new WikiCFP();
    Model m = new LinkedHashModel();
    x.query(m, "ecuador");

  }

  private void query(Model m, String q) throws URISyntaxException, IOException {
    URI build = UriBuilder.fromUri("http://www.wikicfp.com/cfp/servlet/tool.search").queryParam("q", q).queryParam("year", "a").build();
    Document get = Jsoup.connect(build.toString()).get();

    Elements select = get.body().select(".contsec > table > tbody > tr > td > table > tbody > tr > td > a");

    for (Element x : select) {
      String cfpURI = x.attr("href");
      queryCFP(m, cfpURI);
    }
  }

  private void queryCFP(Model m, String u) throws IOException {
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

    int i = 0;

    
    
    
  }

}
