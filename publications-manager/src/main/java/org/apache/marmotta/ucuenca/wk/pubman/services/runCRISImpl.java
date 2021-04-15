/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import javax.inject.Inject;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.DisambiguationUtilsService;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.apache.marmotta.ucuenca.wk.pubman.api.runCRIS;
import org.apache.marmotta.ucuenca.wk.pubman.harvesters.EventosCEDIA;
import org.apache.marmotta.ucuenca.wk.pubman.harvesters.RETEC;
import org.apache.marmotta.ucuenca.wk.pubman.harvesters.WikiCFP;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 *
 * @author cedia
 */
public class runCRISImpl implements runCRIS {

  @Inject
  private org.slf4j.Logger log;
  @Inject
  private ConstantService constantService;
  @Inject
  private ExternalSPARQLService sparqlService;
  @Inject
  private ConfigurationService conf;
  @Inject
  private DisambiguationUtilsService disambiguationUtils;

  @Override
  public void init() {
    String baseEvent = constantService.getBaseResource() + "event/";
    EventosCEDIA events = new EventosCEDIA();
    try {
      Model runRSS = events.runRSS(baseEvent);
      sparqlService.getGraphDBInstance().addBuffer(ValueFactoryImpl.getInstance().createURI(constantService.getAuthorsGraph()), runRSS);
      sparqlService.getGraphDBInstance().dumpBuffer();
    } catch (Exception ex) {
      log.info(ex.toString());
    }

    WikiCFP wikicfp = new WikiCFP(baseEvent);
    Model m = new LinkedHashModel();
    try {
      wikicfp.query(m, "ecuador");
      sparqlService.getGraphDBInstance().addBuffer(ValueFactoryImpl.getInstance().createURI(constantService.getAuthorsGraph()), m);
      sparqlService.getGraphDBInstance().dumpBuffer();
    } catch (Exception ex) {
      log.info(ex.toString());
    }

    String baseOffer = constantService.getBaseResource() + "offer/";

    RETEC retec = new RETEC(conf.getStringConfiguration("retec.url"),
            conf.getStringConfiguration("retec.api"),
            conf.getStringConfiguration("retec.btoken"),
            conf.getStringConfiguration("retec.user"),
            conf.getStringConfiguration("retec.pass"),
            baseOffer);

    try {
      retec.login();
      Model harvest = retec.harvest(disambiguationUtils);
      sparqlService.getGraphDBInstance().addBuffer(ValueFactoryImpl.getInstance().createURI(constantService.getAuthorsGraph()), harvest);
      sparqlService.getGraphDBInstance().dumpBuffer();
    } catch (Exception ex) {
      log.info(ex.toString());
    }
  }

}
