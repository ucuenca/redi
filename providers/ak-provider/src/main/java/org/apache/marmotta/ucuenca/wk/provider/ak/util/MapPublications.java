/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.provider.ak.util;

//import java.io.InputStreamReader;
//import java.net.URL;
//import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Freddy Sumba
 */
public class MapPublications {

    public List<Publication> getPublications(String json) throws ParseException {
        List<Publication> resultOutput = new ArrayList();

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(json);
        JSONArray result = (JSONArray) jsonObject.get("entities");
        for (Object publicationResultObject : result) {
            JSONObject publicationResult = (JSONObject) publicationResultObject;
            long id = (long) publicationResult.get("Id");
            String title = (String) publicationResult.get("Ti");
            String year = StringUtils.trimToEmpty(((Long) publicationResult.get("Y")).toString());
            String paperDate = StringUtils.trimToEmpty((String) publicationResult.get("D"));
            String citationcount = StringUtils.trimToEmpty(((Long) publicationResult.get("CC")).toString());
            String estimatedCitationcount = StringUtils.trimToEmpty(((Long) publicationResult.get("ECC")).toString());
            String extendedMetaData = StringUtils.trimToEmpty((String) publicationResult.get("E"));

            List<Author> authors = new ArrayList();
            JSONArray authorArray = (JSONArray) publicationResult.get("AA");
            for (Object author : authorArray) {
                JSONObject authorResult = (JSONObject) author;
                Author newaut = new Author();
                newaut.setName((String) authorResult.get("AuN"));
                newaut.setId((long) authorResult.get("AuId"));
                newaut.setAfiliation(StringUtils.trimToEmpty((String) authorResult.get("AfN")));
                authors.add(newaut);
            }

            //Extract words
            List<String> words = getJsonString(publicationResult, "W");

            //Extract fields
            List<String> fields = new ArrayList();
            if (publicationResult.get("F") != null) {
                JSONArray fieldsArray = (JSONArray) publicationResult.get("F");
                for (Object field : fieldsArray) {
                    JSONObject fieldValue = (JSONObject) field;
                    fields.add((String) fieldValue.get("FN"));
                }
            }

            //Extract journals
            List<String> journals = getJsonStringObjectSimple(publicationResult, "J", "JN");

            //Extract references
            List<String> references = new ArrayList();
            if (publicationResult.get("RId") != null) {
                JSONArray referencesArray = (JSONArray) publicationResult.get("RId");
                for (Object reference : referencesArray) {
                    references.add(Long.toString((long) reference));
                }
            }

            //Extract metadata
            JSONParser parserMD = new JSONParser();
            Object objMD = parserMD.parse(extendedMetaData);
            JSONObject jsonObjectEMD = (JSONObject) objMD;
            String abstractt = buildInvertedAbstractToPlainAbstract((JSONObject) jsonObjectEMD.get("IA"));
            String doi = StringUtils.trimToEmpty((String) jsonObjectEMD.get("DOI"));
            String conference = StringUtils.trimToEmpty((String) jsonObjectEMD.get("VFN"))
                    + StringUtils.trimToEmpty((String) jsonObjectEMD.get("VSN"));

            //Extract Sources
            List<String> sources = new ArrayList();
            if (jsonObjectEMD.get("S") != null) {
                JSONArray sourceArray = (JSONArray) jsonObjectEMD.get("S");
                for (Object source : sourceArray) {
                    JSONObject fieldValue = (JSONObject) source;
                    sources.add(StringUtils.trimToEmpty((String) fieldValue.get("U")));
                }
            }

            Publication newpub = new Publication();
            newpub.setSource("MSA");
            newpub.setId(id);
            newpub.setTitle(title);
            newpub.setYear(year);
            newpub.setDatePublication(paperDate);
            newpub.setCitationCount(citationcount);
            newpub.setEstimatedCitationcount(estimatedCitationcount);
            newpub.setReferencesId(references);
            newpub.setSources(sources);
            newpub.setConference(conference);
            newpub.setDOI(doi);
            newpub.setKeyWord(words);
            newpub.setAuthors(authors);
            newpub.setFields(fields);
            newpub.setAbstract(abstractt);
            newpub.setJournals(journals);
            resultOutput.add(newpub);

        }
        return resultOutput;
    }

    public String buildInvertedAbstractToPlainAbstract(JSONObject invertedAbstract) {
        if (invertedAbstract == null) {
            return "";
        }
        Long size = (long) invertedAbstract.get("IndexLength");
        String[] plainAbs = new String[size.intValue()];
        JSONObject abs = (JSONObject) invertedAbstract.get("InvertedIndex");
        Set<String> keys = abs.keySet();
        for (String key : keys) {
            JSONArray indexes = (JSONArray) abs.get(key);
            for (Object index : indexes) {
                int i = ((Long) index).intValue();
                plainAbs[i] = key;
            }
        }
        return String.join(" ", plainAbs);
    }

    public List<String> getJsonString(JSONObject jSONObject, String identifier) {
        List<String> terms = new ArrayList();
        if (jSONObject.get(identifier) != null) {
            JSONArray wordsArray = (JSONArray) jSONObject.get(identifier);
            for (Object term : wordsArray) {
                terms.add((String) term);
            }
        }
        return terms;
    }

    public List<String> getJsonStringObjectSimple(JSONObject jSONObject, String identifier1, String identifier2) {
        List<String> terms = new ArrayList();
        if (jSONObject.get(identifier1) != null) {
            JSONObject object = (JSONObject) jSONObject.get(identifier1);

            terms.add(StringUtils.trimToEmpty((String) object.get(identifier2)));

        }

        return terms;

    }

    public List<String> getJsonStringObject(JSONObject jSONObject, String identifier1, String identifier2) {
        List<String> terms = new ArrayList();
        if (jSONObject.get(identifier1) != null) {
            JSONArray termArray = (JSONArray) jSONObject.get(identifier1);
            for (Object j : termArray) {
                JSONObject termdValue = (JSONObject) j;
                terms.add((String) termdValue.get(identifier2));
            }
        }
        return terms;

    }

}
