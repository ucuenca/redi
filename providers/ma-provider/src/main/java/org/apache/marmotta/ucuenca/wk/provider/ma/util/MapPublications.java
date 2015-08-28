/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.provider.ma.util;

//import java.io.InputStreamReader;
//import java.net.URL;
//import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author f35
 */
public class MapPublications {

    public List<Publication> getPublications(String json) throws ParseException {
        List<Publication> resultOutput = new ArrayList();

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(json);
        JSONObject jsonObject = (JSONObject) obj;
        JSONObject d = (JSONObject) jsonObject.get("d");
        JSONObject publication = (JSONObject) d.get("Publication");
        JSONArray result = (JSONArray) publication.get("Result");
        for (Object publicationResultObject : result) {
            JSONObject publicationResult = (JSONObject) publicationResultObject;
            String title = (String) publicationResult.get("Title");
            String abstractt = (String) publicationResult.get("Abstract");
            String year = "" + publicationResult.get("Year").toString();
            String id = "" + publicationResult.get("ID").toString();

            String doi = "" + publicationResult.get("DOI").toString();
            String type = "" + publicationResult.get("Type").toString();
            String citationcount = "" + publicationResult.get("CitationCount").toString();
            String referencecount = "" + publicationResult.get("ReferenceCount").toString();

            List<String> fullVersionURL = new ArrayList();
            JSONArray fullVersionURLArray = (JSONArray) publicationResult.get("FullVersionURL");
            for (Object fullVersion : fullVersionURLArray) {

                fullVersionURL.add((String) fullVersion);
            }

            List<String> keyWords = new ArrayList();
            JSONArray keyWordArray = (JSONArray) publicationResult.get("Keyword");
            for (Object keyWord : keyWordArray) {
                JSONObject keyWordResult = (JSONObject) keyWord;
                keyWords.add((String) keyWordResult.get("Name"));
            }
            List<Author> authors = new ArrayList();
            JSONArray authorArray = (JSONArray) publicationResult.get("Author");
            for (Object author : authorArray) {
                JSONObject authorResult = (JSONObject) author;
                Author newaut = new Author();
                newaut.setFirstName((String) authorResult.get("FirstName"));
                newaut.setLastName((String) authorResult.get("LastName"));
                newaut.setMiddleName((String) authorResult.get("MiddleName"));
                newaut.setNativeName((String) authorResult.get("NativeName"));
                newaut.setID(authorResult.get("ID").toString());
                authors.add(newaut);
            }
            Publication newpub = new Publication();
            newpub.setSource("MSA");
            newpub.setAbstract(abstractt);
            newpub.setKeyWord(keyWords);
            newpub.setTitle(title);
            newpub.setYear(year);
            newpub.setID(id);
            newpub.setAuthors(authors);
            newpub.setDOI(doi);
            newpub.setCitationCount(citationcount);
            newpub.setType(type);
            newpub.setFullVersionURL(fullVersionURL);
            newpub.setReferenceCount(referencecount);

            resultOutput.add(newpub);
        }
        return resultOutput;
    }
}

