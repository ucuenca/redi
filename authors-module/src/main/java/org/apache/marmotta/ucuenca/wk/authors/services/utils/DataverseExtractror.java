/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.authors.services.utils;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;

/**
 *
 * @author cedia
 */
@SuppressWarnings("PMD")
public class DataverseExtractror {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnirestException {
        // TODO code application logic here

        DataverseExtractror mm = new DataverseExtractror();

        mm.run();
    }

    private String url = "https://dataverse.harvard.edu/oai";
    private String rt = null;
    private Document parse;

    public void run() throws UnirestException {
        String r = null;
        while (askRecords()) {

            Elements select = parse.body().select("OAI-PMH > ListRecords > record");
            for (Element e : select) {
                Dataverse2redi(e);
            }

            break;
        }
    }

    private Model Dataverse2redi(Element r) {

        LinkedHashModel linkedHashModel = new LinkedHashModel();
        if(!r.selectFirst("header").attributes().hasKey("status")){
            Element meta = r.selectFirst("metadata > resource");
            String doi = meta.selectFirst("identifier").text().trim();
            
        }
        return linkedHashModel;
    }

    private boolean askRecords() throws UnirestException {
        boolean b = false;
        String df = "oai_datacite";
        HttpRequest queryString = Unirest.get(url).queryString("verb", "ListRecords").queryString("metadataPrefix", df);
        if (rt != null) {
            queryString = queryString.queryString("resumptionToken", rt);
        } else {
            queryString = queryString.queryString("metadataPrefix", df);
        }
        HttpResponse<String> asString = queryString.asString();
        if (asString.getStatus() == 200) {
            String body = asString.getBody();
            this.parse = Jsoup.parse(body);
            Elements elementsByTag = this.parse.getElementsByTag("resumptionToken");
            if (!elementsByTag.isEmpty()) {
                Element get = elementsByTag.get(0);
                rt = get.text().trim();
                b = true;
            } else {
                rt = null;
            }
        } else {
            throw new UnirestException("Unexpected error");
        }
        return b;
    }

}
