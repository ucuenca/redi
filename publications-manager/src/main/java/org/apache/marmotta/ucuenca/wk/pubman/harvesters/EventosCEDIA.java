/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.harvesters;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import kong.unirest.Unirest;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import org.apache.tika.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import net.fortuna.ical4j.model.component.*;
import org.apache.marmotta.ucuenca.wk.commons.function.Cache;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

/**
 *
 * @author cedia
 */
public class EventosCEDIA {

  public Model runRSS(String base) throws IOException, Exception {

    String url = "https://www.cedia.edu.ec/es/noticias-y-eventos/eventos?format=feed&type=rss";
    Model m = new LinkedHashModel();

    Document get = Jsoup.connect(url).get();

    Elements elms = get.select("rss > channel > item > link");

    for (Element e : elms) {
      Thread.sleep(1000 * 5);
      String url2 = e.text().trim() + "/download-ical";

      String body = getHTML(url2);

      body = body.replaceAll("(?m).*-00011130T050000Z.*", "");

      CalendarBuilder builder = new CalendarBuilder();

      Calendar build = builder.build(IOUtils.toInputStream(body));
      ComponentList cs = build.getComponents();

      ValueFactoryImpl instance = ValueFactoryImpl.getInstance();

      for (Object c : cs) {
        if (c instanceof VEvent) {
          VEvent properties = ((VEvent) c);
          String desc = properties.getProperty("DESCRIPTION").getValue();
          String tit = properties.getProperty("SUMMARY").getValue();
          String dat = properties.getProperty("DTSTART").getValue();

          SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
          sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
          java.util.Date d = sdf.parse(dat);

          String link = e.text().trim();

          URI createURI = instance.createURI(base + Cache.getMD5(link));

          m.add(createURI, RDF.TYPE, instance.createURI("http://eurocris.org/ontology/cerif#Event"));
          m.add(createURI, instance.createURI("http://eurocris.org/ontology/cerif#has_name"), instance.createLiteral(tit));
          m.add(createURI, instance.createURI("http://eurocris.org/ontology/cerif#has_description"), instance.createLiteral(desc));
          m.add(createURI, instance.createURI("http://eurocris.org/ontology/cerif#has_startDate"), instance.createLiteral(d));
          m.add(createURI, instance.createURI("http://eurocris.org/ontology/cerif#has_URI"), instance.createLiteral(link));

        }
      }
    }

    return m;

  }

  public static String getHTML(String urlToRead) throws Exception {
    return Unirest.get(urlToRead).asString().getBody();
  }

}
