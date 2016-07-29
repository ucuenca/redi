/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.commons.service.DistanceService;
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

    private DistanceService distanceService = new DistanceServiceImpl();

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
        List<Map<String, Value>> resultFilterAuthors = new LinkedList<Map<String, Value>>();
        String fichero = "";
        InputStream inputStream = null;
        String dspaceName = "";
        String fileName = "";
        try {
            String getAllAuthorsDataQuery = queriesService.getAuthorsDataQuery(constantService.getAuthorsGraph(), constantService.getEndpointsGraph());
            resultAllAuthors = sparqlService.query(QueryLanguage.SPARQL, getAllAuthorsDataQuery);

            /**
             * Filter by Specific List of Authors
             */
            boolean filterByList = Boolean.parseBoolean(commonService.readPropertyFromFile("parameters.properties", "filterByList"));

            /**
             * the format of the names in the file should be {LastName
             * FirstName}
             */
            if (filterByList) {
                fichero = commonService.readPropertyFromFile("parameters.properties", "fileToFilter"); //Formato:  {1Nombre+1Apellido}
                //Get file from resources folder

                ClassLoader classLoader = this.getClass().getClassLoader();
                inputStream = classLoader.getResourceAsStream(fichero);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line = reader.readLine();
                while (line!=null) {
                    fileName = getNameFromString(line, 1);
                    Iterator<Map<String, Value>> iter = resultAllAuthors.iterator();

                    while (iter.hasNext()) {
                        Map<String, Value> element = iter.next();
                        String firstName = element.get("fname").stringValue();
                        String lastName = element.get("lname").stringValue();
                        dspaceName = lastName + ":" + firstName;
                        //log.info("Analyzed names - fileName: " + fileName + " - dspaceName: " + dspaceName);
                        if (distanceService.syntacticComparisonNames("local", fileName, "local", dspaceName)) {
                            resultFilterAuthors.add(element);
                        }
//                        if (firstName.contains(firstNameFile) && lastName.contains(lastNameFile)) {
//                            resultFilterAuthors.add(element);
//                        }
                    }

//                    for (Map<String, Value> map : resultAllAuthors) {
//                        String firstName = map.get("fname").stringValue();
//                        String lastName = map.get("lname").stringValue();
//                        if (firstName.contains(firstNameFile) && lastName.contains(lastNameFile)) {
//                            resultFilterAuthors.add(map);
//                        }
//                    }
                    try {
                        line = reader.readLine();
                    } catch (Exception e) {
                        line = "";
                        log.error("finished reading names from filterByList File ");
                    }
                }
                reader.close();
                inputStream.close();
                return resultFilterAuthors;
            }
        } catch (MarmottaException ex) {
            log.error("MarmottaException in GetListOfAuthors" + ex);
        } catch (Exception ex) {
            log.error("Exception fileName: " + fileName + " - " + " dspaceName: " + dspaceName);
            log.error("Exception reading file " + fichero + ": " + ex);
        }

        return resultAllAuthors;
    }

    /**
     *
     * Format 1: {1LastName 1FirstName}. Example Perez Juan | Format 2:
     * {1LastName 1FirstName 2FirstName}. Example Perez Juan Carlos | Format 3:
     * ...
     *
     * @param line String name of the person who comes from the file
     * @param format int format of line param
     * @return fullLastName:fullFirstName
     */
    public String getNameFromString(String line, int format) {
        String firstNameFile = "";
        String lastNameFile = "";
        String fileName = line;

        try {
            switch (format) {
                case 1: {
                    if (line.length() > format) {
                        firstNameFile = line.split(" ")[0];
                        lastNameFile = line.split(" ")[1];
                        fileName = lastNameFile + ":" + firstNameFile;
                    }
                    break;
                }
                case 2: { //change split format
                    firstNameFile = line.split(" ")[0];
                    lastNameFile = line.split(" ")[1];
                    fileName = lastNameFile + ":" + firstNameFile;
                    break;
                }
                case 3: {//change split format
                    firstNameFile = line.split(" ")[0];
                    lastNameFile = line.split(" ")[1];
                    fileName = lastNameFile + ":" + firstNameFile;
                    break;
                }

                default: {
                    firstNameFile = line.split(" ")[0];
                    lastNameFile = line.split(" ")[1];
                    fileName = lastNameFile + ":" + firstNameFile;
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Problems split name " + firstNameFile + " - " + lastNameFile + " from filter line. ");
        }
        return fileName;
    }
}
