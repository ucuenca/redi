/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.slf4j.Logger;
import org.apache.marmotta.ucuenca.wk.pubman.api.SendNotificationsMarmotta;
import org.apache.marmotta.ucuenca.wk.pubman.utils.freeMarkerModel;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;

/**
 *
 * @author cedia
 */
@ApplicationScoped
public class sendNotImpl implements SendNotificationsMarmotta {

  @Inject
  private Logger log;
  @Inject
  private ExternalSPARQLService ess;

  @Override
  public void init(boolean all) {
    if (!all) {
      notifyExisting();
    } else {
      inviteAll();
    }
  }

  public void inviteAll() {
    String q = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX schema: <http://schema.org/>\n"
            + "select ?a (group_concat(distinct lcase(?e) ; separator='|||') as ?em) (sample(distinct ?n) as ?an) {\n"
            + "    graph <https://redi.cedia.edu.ec/context/redi> {\n"
            + "        ?a a foaf:Person.\n"
            + "        ?a schema:memberOf ?o .\n"
            + "        ?o <http://ucuenca.edu.ec/ontology#memberOf> <https://redi.cedia.edu.ec/> .\n"
            + "        ?a <http://www.w3.org/2006/vcard/ns#hasEmail> ?e .\n"
            + "        filter (regex(lcase(str(?e)), '.edu.ec', 'i' )) .\n"
            + "        ?a foaf:name ?n .\n"
            + "    }\n"
            + "} group by ?a";
    try {
      List<Map<String, Value>> query = ess.getSparqlService().query(QueryLanguage.SPARQL, q);
      int i = 0;
      for (Map<String, Value> mm : query) {
        i++;
        String aURI = mm.get("a").stringValue();
        String aMai = mm.get("em").stringValue();
        String aNam = mm.get("an").stringValue();
        freeMarkerModel.researcherInvitation newResearcherInvitation = new freeMarkerModel().newResearcherInvitation(aNam, aURI, extractMails(aMai));
        if (i < 5) {
          sendMail(newResearcherInvitation, "invitation", "yaymen.sediek@azel.xyz");
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

  }

  private List<String> extractMails(String txt) {
    List<String> ls = Lists.newArrayList();
    Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(txt);
    while (m.find()) {
      ls.add(m.group());
    }
    return ls;
  }

  public void notifyExisting() {
    String q = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "PREFIX redi: <https://redi.cedia.edu.ec/ont#>\n"
            + "select ?a ?p_t_c (sample(?a_n) as ?an) (sample(?p_t) as ?pt) (sample(?p) as ?pu) (group_concat( distinct ?a_e ; separator=';;;') as ?ae) {\n"
            + "    graph <https://redi.cedia.edu.ec/context/notifications> {\n"
            + "        <https://redi.cedia.edu.ec/resource/type/publication> <https://redi.cedia.edu.ec/ont#newBucket> ?p .\n"
            + "    }\n"
            + "    graph <https://redi.cedia.edu.ec/context/buckets> {\n"
            + "        ?a redi:type 'author' .\n"
            + "        ?a redi:element ?oa .\n"
            + "    }\n"
            + "    graph <https://redi.cedia.edu.ec/context/authors> {\n"
            + "        ?oa foaf:name ?a_n .\n"
            + "    }\n"
            + "    graph <https://redi.cedia.edu.ec/context/redi> {\n"
            + "        ?a foaf:publications ?p .\n"
            + "        ?p dct:title ?p_t .\n"
            + "        bind (replace (replace (lcase(str(?p_t)), 'ü|ñ|á|é|í|ó|ú|a|e|i|o|u|,|;|:|-|\\\\(|\\\\)|\\\\||\\\\.' ,' '), ' ' ,'') as ?p_t_c) .\n"
            + "        optional {\n"
            + "            ?a <http://www.w3.org/2006/vcard/ns#hasEmail> ?a_e .\n"
            + "        }\n"
            + "    }\n"
            + "} group by ?a ?p_t_c ";
    try {
      List<Map<String, Value>> query = ess.getSparqlService().query(QueryLanguage.SPARQL, q);

      Map<String, Object[]> mp = Maps.newHashMap();
      for (Map<String, Value> mm : query) {
        String stringValue = mm.get("a").stringValue();
        if (!mp.containsKey(stringValue)) {
          mp.put(stringValue, new Object[]{(String) null, new String[]{}, new ArrayList<String>(), new ArrayList<String>()});
        }
        mp.get(stringValue)[0] = mm.get("an").stringValue();//0: Name
        mp.get(stringValue)[1] = mm.get("ae") != null ? mm.get("ae").stringValue().toLowerCase().split(";;;") : mp.get(stringValue)[1];//1: Email
        ((List<String>) mp.get(stringValue)[2]).add(mm.get("pu").stringValue());
        ((List<String>) mp.get(stringValue)[3]).add(mm.get("pt").stringValue());
      }
      nofifyNewPublications(mp);

      //TODO: delete notifications graph
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void sendMail(Object data, String template, String mail) throws MalformedTemplateNameException, ParseException, IOException, TemplateException {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
    cfg.setClassForTemplateLoading(this.getClass(), "/");
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    cfg.setFallbackOnNullLoopVariable(false);
    Template temp = cfg.getTemplate("freemaker/" + template + ".ftlh");
    StringWriter stringWriter = new StringWriter();
    temp.process(data, stringWriter);
    stringWriter.flush();
    stringWriter.close();
    String toString = stringWriter.toString();
    Mailer mailer = MailerBuilder
            .withSMTPServer("mail.delparamo.net", 8025, "tmp23b0f1a941@delparamo.net", "ih@Npsz_Osy1")
            .clearEmailAddressCriteria() // turns off email validation
            .withDebugLogging(true)
            .buildMailer();
    Email buildEmail = EmailBuilder.startingBlank()
            .from("tmp23b0f1a941@delparamo.net")
            .to(mail)
            .withSubject("Notificacion de REDI")
            .withHTMLText(toString).buildEmail();

    mailer.sendMail(buildEmail, false);
  }

  private void nofifyNewPublications(Map<String, Object[]> mp) throws MalformedTemplateNameException, ParseException, IOException, TemplateException {
    int i = 0;
    for (Map.Entry<String, Object[]> au : mp.entrySet()) {
      i++;
      String authorURI = au.getKey();
      String authorName = ((String) au.getValue()[0]);
      String[] authorEmails = ((String[]) au.getValue()[1]);
      List<String> newPublicationsURI = ((List<String>) au.getValue()[2]);
      List<String> newPublicationsTitle = ((List<String>) au.getValue()[3]);
      freeMarkerModel.notification newNotification = new freeMarkerModel().newNotification(authorName, authorURI, newPublicationsURI, newPublicationsTitle);
      if (i < 5) {
        sendMail(newNotification, "notification", "yaymen.sediek@azel.xyz");
      }
    }
  }

}
