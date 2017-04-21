/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.provider.gs;

import com.google.common.base.Preconditions;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.apache.marmotta.ucuenca.wk.endpoint.gs.GoogleScholarPublicationEndpoint;
import org.apache.marmotta.ucuenca.wk.provider.gs.handler.IHandler;
import org.apache.marmotta.ucuenca.wk.provider.gs.handler.PublicationHandler;
import org.apache.marmotta.ucuenca.wk.provider.gs.mapper.MapperObjectRDF;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.Publication;
import org.openrdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class GoogleScholarPublicationProvider extends AbstractHttpProvider {

    private MapperObjectRDF mapper = null;
    private Logger log = LoggerFactory.getLogger(GoogleScholarPublicationProvider.class);

    @Override
    protected List<String> buildRequestUrl(String resourceUri, Endpoint endpoint) throws DataRetrievalException {
        Preconditions.checkArgument(endpoint instanceof GoogleScholarPublicationEndpoint);
        Preconditions.checkArgument(((GoogleScholarPublicationEndpoint) endpoint).getAuthorURI() instanceof String);
        GoogleScholarPublicationEndpoint publicationEndpoint = (GoogleScholarPublicationEndpoint) endpoint;
        String authorURI = publicationEndpoint.getAuthorURI();
        // There is a constraint that the resource author has the word "author/"
        String base = authorURI.substring(0, authorURI.lastIndexOf("author/"));
        mapper = new MapperObjectRDF(authorURI, base);
        return Collections.singletonList(resourceUri);
    }

    @Override
    protected List<String> parseResponse(String resourceUri, String requestUrl, Model model, InputStream input, String contentType) throws DataRetrievalException {
        try {
            Publication p = new Publication(requestUrl);
            IHandler handler = new PublicationHandler(p);
            handler.extract(input);
            model.addAll(mapper.map(p));
        } catch (MalformedURLException | SAXException | InterruptedException | IllegalArgumentException | IllegalAccessException ex) {
            log.error("Error to map and parse to RDF publication. {}", ex.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return "Google Scholar Publication";
    }

    @Override
    public String[] listMimeTypes() {
        return new String[]{
            "text/html"
        };
    }

}
