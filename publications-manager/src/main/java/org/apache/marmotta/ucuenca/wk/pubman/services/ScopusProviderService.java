/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.services;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.apache.marmotta.ucuenca.wk.commons.service.ConstantService;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class ScopusProviderService extends AbstractProviderService {

    @Inject
    private ConstantService constantService;

    private final String URLSEARCHSCOPUS = "http://api.elsevier.com/content/search/author?query=authfirst%28FIRSTNAME%29authlast%28LASTNAME%29affil%28PAIS%29&apiKey=a3b64e9d82a8f7b14967b9b9ce8d513d&httpAccept=application/xml";

    @Override
    protected List<String> buildURLs(String firstname, String lastname) {

        //    private List<String> buildURLs(String firstname, String lastname) {
        //firstNameSearch = firstName.split(" ").length > 1 ? firstName.split(" ")[0] : firstName;
        //String secondNameSearch = firstName.split(" ").length > 1 ? firstName.split(" ")[1] : "";
        //String lastNameSearch2 = lastName.split(" ").length > 1 ? lastName.split(" ")[1] : "";
        //String surnamesSearch = lastNameSearch.split(" ").length > 1 ? firstNameSearch + "+OR+" + secondNameSearch: firstName;
        //uri_search.add(URLSEARCHSCOPUS.replace("FIRSTNAME", firstNameSearch.replace(" ", "+OR+")).replace("LASTNAME", lastNameSearch).replace(AFFILIATIONPARAM, ""));//.replace("PAIS", "Ecuador"));
        int numApellidos = cleanNameAuthor(lastname).split(" ").length;
        String fullname = "&fullName=" + firstname.replace(" ", "%20") + "%20%20" + lastname.replace(" ", "%20");
        List<String> uri_search = new ArrayList<>();
        String lastNameSearch = numApellidos > 1 && numApellidos < 3 ? cleanNameAuthor(lastname).split(" ")[0] : lastname.replace(" ", "+OR+");
        uri_search.add(URLSEARCHSCOPUS.replace("FIRSTNAME", firstname.replace(" ", "+OR+")).replace("LASTNAME", lastname.replace(" ", "+AND+")).replace("PAIS", "Ecuador") + fullname);// .replace(AFFILIATIONPARAM, "")
        String URLSearch = URLSEARCHSCOPUS.replace("FIRSTNAME", firstname.replace(" ", "+OR+")).replace("LASTNAME", lastNameSearch).replace("PAIS", "Ecuador");
        if (!uri_search.contains(URLSearch)) {
            uri_search.add(URLSearch + fullname);
        }

        return uri_search;
    }

    private String cleanNameAuthor(String value) {
        value = value.toLowerCase().trim();
        value = value.replaceAll("  ", " ");
        value = value.replaceAll(" de ", " ");
        value = value.replaceAll(" del ", " ");
        value = value.replaceAll(" los ", " ");
        value = value.replaceAll(" y ", " ");
        value = value.replaceAll(" las ", " ");
        value = value.replaceAll(" la ", " ");

        value = value.replaceAll("^de ", "");
        value = value.replaceAll("^del ", "");
        value = value.replaceAll("^los ", "");
        value = value.replaceAll("^y ", "");
        value = value.replaceAll("^las ", "");
        value = value.replaceAll("^la ", "");

        value = value.replaceAll(" de$", "");
        value = value.replaceAll(" del$", "");
        value = value.replaceAll(" los$", "");
        value = value.replaceAll(" y$", "");
        value = value.replaceAll(" las$", "");
        value = value.replaceAll(" la$", "");

        return value.trim();
    }

    @Override
    protected String getProviderGraph() {
        return constantService.getScopusGraph();
    }

    @Override
    protected String getProviderName() {
        return "SCOPUS";
    }
}
