/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.service;

/**
 *
 * @author Satellite
 */
public interface ConstantService {

    String PUBPROPERTY = "http://xmlns.com/foaf/0.1/publications";

    String TITLEPROPERTY = "http://purl.org/dc/terms/title";

    String INSERTDATA = "INSERT DATA { ";

    String ENDPOINTPREFIX = "http://ucuenca.edu.ec/wkhuska/endpoint/";

    String CENTRALGRAPHPREFIX = "http://ucuenca.edu.ec/resource/";

    String PREFIX = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
            + " PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
            + " PREFIX owl: <http://www.w3.org/2002/07/owl#> ";

    String getPubProperty();

    String getTittleProperty();
    
    String getGraphString(String graph);
    
    String getWkhuskaGraph();

    String getProvenanceProperty();

    String getLimit(String limit);

    String getOffset(String offset);
    
    String uc(String pred);
    
    String foaf(String pred);
    
    String owl(String pred);
    
    String dblp(String pred);

}
