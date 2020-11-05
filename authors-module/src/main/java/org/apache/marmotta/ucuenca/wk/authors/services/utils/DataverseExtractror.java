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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.tika.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;

/**
 *
 * @author cedia
 */
@SuppressWarnings("PMD")
public class DataverseExtractror {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnirestException, IOException, RDFParseException, RDFHandlerException, RDFHandlerException, TransformerException, RepositoryException, MalformedQueryException, QueryEvaluationException {
        // TODO code application logic here

        DataverseExtractror mm = new DataverseExtractror("https://dataverse.harvard.edu/");

        mm.run();
    }

    public DataverseExtractror(String url) {
        this.url = url + "oai";
        this.url_ = url + "dataset.xhtml?persistentId=";
    }

    private String url;
    private String url_;
    private String rt = null;
    private Document parse;
    private String parse_;

    private String xslt(InputStream in) throws TransformerException, IOException {
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("dataverse/datacite2redi.xslt");
        return transformXML(IOUtils.toString(in), resourceAsStream);

    }

    private String transformXML(String xml, InputStream xslFile) {
        String outputXML = null;
        try {
            System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = factory.newTransformer(new StreamSource(xslFile));
            Source xmlStream = new StreamSource(new StringReader(xml));
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            transformer.transform(xmlStream, result);
            outputXML = writer.getBuffer().toString();
        } catch (TransformerConfigurationException tce) {
            tce.printStackTrace();
        } catch (TransformerException te) {
            te.printStackTrace();
        }
        return outputXML;
    }

    public Model Dataverse2redi(String authBase, String datBase) throws UnirestException, IOException, RDFParseException, TransformerException, RepositoryException, MalformedQueryException, QueryEvaluationException {
        String xslt = xslt(IOUtils.toInputStream(this.parse_, Charset.defaultCharset().name()));
        Model parse1 = Rio.parse(IOUtils.toInputStream(xslt), url, RDFFormat.TRIX);
        parse1.remove(null, null, ValueFactoryImpl.getInstance().createLiteral(""));
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("dataverse/mapping.sparql");
        String toString = IOUtils.toString(resourceAsStream);
        toString = toString.replaceAll("@@@URL@@@", url_);
        toString = toString.replaceAll("@@@Dataset@@@", datBase);
        toString = toString.replaceAll("@@@Author@@@", authBase);
        LinkedHashModel linkedHashModel = new LinkedHashModel();
        Model extractData = VIVOExtractor.extractData(parse1, toString);
        linkedHashModel.addAll(extractData);
        return linkedHashModel;
    }

    public void run() throws UnirestException {
        String df = "oai_datacite";
        HttpRequest queryString = Unirest.get(url).queryString("verb", "ListRecords");
        if (rt != null) {
            queryString = queryString.queryString("resumptionToken", rt);
        } else {
            queryString = queryString.queryString("metadataPrefix", df);
        }
        HttpResponse<String> asString = queryString.asString();
        if (asString.getStatus() == 200) {
            String body = asString.getBody();
            this.parse_ = body;
            this.parse = Jsoup.parse(body);
        } else {
            throw new UnirestException("Unexpected error");
        }
    }

    public boolean askRecords() throws UnirestException {
        boolean b = false;
        Elements elementsByTag = this.parse.getElementsByTag("resumptionToken");
        if (!elementsByTag.isEmpty()) {
            Element get = elementsByTag.get(0);
            rt = get.text().trim();
            b = true;
        } else {
            rt = null;
        }
        return b;
    }

}
