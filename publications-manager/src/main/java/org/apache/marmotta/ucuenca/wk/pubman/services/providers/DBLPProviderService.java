/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services.providers;

import edu.emory.mathcs.backport.java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;
import org.apache.marmotta.ucuenca.wk.pubman.api.AbstractProviderService;

/**
 *
 * @author José Ortiz
 */
public class DBLPProviderService extends AbstractProviderService {

    @Inject
    private ConstantService constantService;

    @Inject
    private CommonsServices commonsServices;

    @Override
    protected List<String> buildURLs(String firstname, String lastname) {
        String NS_DBLP = "http://rdf.dblp.com/ns/search/";
        String nameToFind = commonsServices.removeAccents(priorityFindQueryBuilding(1, firstname, lastname));
        String URI = NS_DBLP + nameToFind;
        return Collections.singletonList(URI);
    }

    @Override
    protected String getProviderGraph() {
        return constantService.getDBLPGraph();
    }

    @Override
    protected String getProviderName() {
        return "DBLP";
    }

    public String priorityFindQueryBuilding(int priority, String firstName, String lastName) {
        String[] fnamelname = {"", "", "", "", ""};
        /**
         * fnamelname[0] is a firstName A, fnamelname[1] is a firstName B
         * fnamelname[2] is a lastName A, fnamelname[3] is a lastName B
         *
         */
        String nameProcess = "";
        for (String name : (firstName + " " + lastName).split(" ")) {
            if (name.length() > 1) {
                nameProcess += name + " ";
            }
        }
        if (nameProcess.split(" ").length > 2) {
            for (int i = 0; i < firstName.split(" ").length; i++) {
                fnamelname[i] = firstName.split(" ")[i];
            }

            for (int i = 0; i < lastName.split(" ").length; i++) {
                fnamelname[i + 2] = lastName.split(" ")[i];
            }

            switch (priority) {
//            case 5:
//                return fnamelname[3];
                case 1:
                    return fnamelname[0] + "_" + fnamelname[2];
                case 3:
                    return fnamelname[1] + "_" + fnamelname[2] + "_" + fnamelname[3];
                case 2:
                    return fnamelname[0] + "_" + fnamelname[2] + "_" + fnamelname[3];
                case 4:
                    return fnamelname[0] + "_" + fnamelname[1] + "_" + fnamelname[2] + "_" + fnamelname[3];
            }

        } else {
            for (int i = 0; i < firstName.split(" ").length; i++) {
                fnamelname[i] = firstName.split(" ")[i];
            }

            for (int i = 0; i < lastName.split(" ").length; i++) {
                fnamelname[i + 1] = lastName.split(" ")[i];
            }

            return fnamelname[0] + "_" + fnamelname[1];

        }
        return "";
    }

}
