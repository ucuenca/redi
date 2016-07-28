/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.GetAuthorsGraphData;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;

/**
 *
 * @author Satellite
 */
public class GetAuthorsGraphDataImpl implements GetAuthorsGraphData {

    private QueriesService queriesService = new QueriesServiceImpl();

    private CommonsServices commonService = new CommonsServicesImpl();

    private ConstantService constantService = new ConstantServiceImpl();

    @Inject
    private SparqlService sparqlService;

    @Inject
    private Logger log;

    /**
     * Retorna la lista de autores (Desde el grafo de autores ) que seran
     * procesados (Buscados en las Fuentes de publicaciones)
     *
     * @return
     */
    @Override
    public List<Map<String, Value>> getListOfAuthors() {
        List<Map<String, Value>> resultAllAuthors = null;
        List<Map<String, Value>> resultFilterAuthors = null;
        try {
            String getAllAuthorsDataQuery = queriesService.getAuthorsDataQuery(constantService.getAuthorsGraph(), constantService.getEndpointsGraph());
            resultAllAuthors = sparqlService.query(QueryLanguage.SPARQL, getAllAuthorsDataQuery);
            resultFilterAuthors = null;

            /**
             * Filter by Specific List of Authors
             */
            boolean filterByList = Boolean.parseBoolean(commonService.getReadPropertyFromFile("parameters.properties", "filterByList"));

            if (filterByList) {
                String fichero = commonService.getReadPropertyFromFile("parameters.properties", "fileToFilter"); //Formato:  {1Nombre+1Apellido}
                try {
                    FileReader fr = new FileReader(fichero);
                    BufferedReader br = new BufferedReader(fr);
                    String linea = br.readLine();
                    while (!linea.isEmpty()) {

                        String firstNameFile = linea.split(" ")[0];
                        String lastNameFile = linea.split(" ")[1];
                        for (Map<String, Value> map : resultAllAuthors) {
                            String firstName = map.get("fname").stringValue();
                            String lastName = map.get("lname").stringValue();
                            if (firstName.contains(firstNameFile) && lastName.contains(lastNameFile)) {
                                resultFilterAuthors.add(map);
                            }
                        }
                        linea = br.readLine();
                    }
                    fr.close();
                    return resultFilterAuthors;
                } catch (Exception e) {
                    log.error("Exception reading file " + fichero + ": " + e);
                }
                return resultAllAuthors;
            }
        } catch (MarmottaException ex) {
            log.error("MarmottaException in GetListOfAuthors" + ex);
        }
        return resultAllAuthors;
    }

}
