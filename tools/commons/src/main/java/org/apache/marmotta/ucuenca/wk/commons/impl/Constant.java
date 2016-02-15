/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.impl;

import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.openrdf.model.vocabulary.FOAF;

/**
 *
 * @author Satellite
 */
public class Constant implements ConstantService{

    @Override
    public String getPubProperty() {
        return PUBPROPERTY;
    }

    @Override
    public String getTittleProperty() {
         return TITLEPROPERTY;
    
    }
    
    @Override
    public String getWkhuskaGraph() {
        return "http://ucuenca.edu.ec/wkhuska";
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
    public String uc(String pred){
        return "<http://ucuenca.edu.ec/resource/" + pred + ">";
    }
    
    @Override
    public String foaf(String pred){
        return "<" + FOAF.NAMESPACE + pred + ">";
    }
    
    @Override
    public String owl(String pred) {
        return "<http://www.w3.org/2002/07/owl#" + pred + ">";
    }
    
    @Override
    public String dblp(String pred) {
        return "<http://dblp.dagstuhl.de/rdf/schema-2015-01-26#" + pred + ">";
    }
    
    public FOAF foaf() {
        return new FOAF();
    }
    
    
}
