/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.impl;

import javax.inject.Inject;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;

/**
 *
 * @author Satellite
 */
public class ConstantServiceImpl implements ConstantService {

    @Inject
    private ConfigurationService confService;
    private final static String FOAFNS = "http://xmlns.com/foaf/0.1/";
    private final static String OWLNS = "http://www.w3.org/2002/07/owl#";
    private final static String LOGO_PATH = "./../wkhuska_webapps/ROOT/wkhome/images/logo_wk.png";

    private CommonsServices commonService = new CommonsServicesImpl();

    @Override
    public String getHome() {
        return confService.getHome();
    }

    @Override
    public String getBaseURI() {
        return confService.getBaseUri();
    }

    @Override
    public String getAuthorsGraph() {
        return confService.getBaseContext() + "authors";
    }

    @Override
    public String getSameAuthorsGraph() {
        return confService.getBaseContext() + "sameAuthors";
    }

    @Override
    public String getEndpointsGraph() {
        return confService.getBaseContext() + "endpoints";
    }

    @Override
    public String getGoogleScholarGraph() {
        return confService.getBaseContext() + "provider/GoogleScholarProvider";
    }

    @Override
    public String getAcademicsKnowledgeGraph() {
        return confService.getBaseContext() + "provider/AcademicsKnowledgeProvider";
    }

    @Override
    public String getCentralGraph() {
        return "http://ucuenca.edu.ec/wkhuska";
    }

    @Override
    public String getBaseResource() {
        return getBaseURI() + "resource/";
    }

    @Override
    public String getSPARQLEndpointURL() {
        return getBaseURI() + "sparql/select";
    }

    @Override
    public String getAuthorResource() {
        return getBaseResource() + "authors/";
    }

    @Override
    public String getJournalResource() {
        return getBaseResource() + "journal/";
    }

    @Override
    public String getConferenceResource() {
        return getBaseResource() + "conference/";
    }

    @Override
    public String getBookResource() {
        return getBaseResource() + "book/";
    }

    @Override
    public String getSubjectResource() {
        return getBaseResource() + "subject/";
    }

    @Override
    public String getPublisherResource() {
        return getBaseResource() + "publisher/";
    }

    @Override
    public String getTopicResource() {
        return getBaseResource() + "topic/";
    }

    @Override
    public String getPublicationResource() {
        return getBaseResource() + "publication/";
    }

    @Override
    public String getGoogleScholarResource() {
        return getBaseResource() + "GoogleScholar/";
    }

    @Override
    public String getAcademicsKnowledgeResource() {
        return getBaseResource() + "AcademicsKnowledge/";
    }

    @Override
    public String getEndpointResource() {
        return getBaseResource() + "endpoint/";
    }

    @Override
    public String getPubProperty() {
        return PUBPROPERTY;
    }

    @Override
    public String getTittleProperty() {
        return TITLEPROPERTY;

    }

    /**
     * Wkhuska Graph = http://ucuenca.edu.ec/wkhuska
     *
     * @return
     */
    @Override
    public String getWkhuskaGraph() {
        return getSelectedGraph("wkhuska");
    }

    /**
     * Wkhuska Graph = http://ucuenca.edu.ec/wkhuska , authors = /authors. Then
     * Authors Graph = "http://ucuenca.edu.ec/wkhuska/authors"
     *
     * @return
     */
//    @Override
//    public String getAuthorsGraph() {
//        return getWkhuskaGraph() + getSelectedGraph("authors");
//    }
    @Override
    public String getClusterGraph() {
        return getWkhuskaGraph() + getSelectedGraph("clusters");
    }

    @Override
    public String getExternalAuthorsGraph() {
        return getWkhuskaGraph() + getSelectedGraph("externalauthors");
    }

    @Override
    public String getDBLPGraph() {
        return getWkhuskaGraph() + getSelectedGraph("dblp");
    }

    @Override
    public String getScopusGraph() {
        return getWkhuskaGraph() + getSelectedGraph("scopus");
    }

    @Override
    public String getMAGraph() {
        return getWkhuskaGraph() + getSelectedGraph("microsoftacademics");
    }

    @Override
    public String getGSGraph() {
        return getWkhuskaGraph() + getSelectedGraph("googlescholar");
    }

    @Override
    public String getDspaceGraph() {
        return getWkhuskaGraph() + getSelectedGraph("dspace");
    }

//    @Override
//    public String getEndpointsGraph() {
//        return getWkhuskaGraph() + getSelectedGraph("endpoints");
//    }
    @Override
    public String getProviderNsGraph() {
        return (confService.getBaseContext() + getSelectedGraph("provider")).replace("\\//", "\\/");
    }

    @Override
    public String getLimit(String limit) {
        return " Limit " + limit;
    }

    @Override
    public String getOffset(String offset) {
        return " offset " + offset;
    }

    @Override
    public String getProvenanceProperty() {
        return "http://purl.org/dc/terms/provenance";
    }

    @Override
    public String getGraphString(String graph) {
        return " GRAPH <" + graph + "> ";
    }

    @Override
    public String uc(String pred) {
        return "<http://ucuenca.edu.ec/ontology#" + pred + ">";
    }

    @Override
    public String foaf(String pred) {
        return "<" + FOAFNS + pred + ">";
    }

    @Override
    public String owl(String pred) {
        return "<" + OWLNS + pred + ">";
    }

    @Override
    public String dblp(String pred) {
        return "<http://dblp.dagstuhl.de/rdf/schema-2015-01-26#" + pred + ">";
    }

    @Override
    public String getPrefixes() {
        return PREFIX;
    }

    @Override
    public String getLogoPath() {
        return LOGO_PATH;
    }

    public String getSelectedGraph(String type) {
        return commonService.readPropertyFromFile("parameters.properties", type);
    }

}
