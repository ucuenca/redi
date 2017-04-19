/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.provider.gs;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.apache.marmotta.ucuenca.wk.provider.gs.handler.IHandler;
import org.apache.marmotta.ucuenca.wk.provider.gs.handler.ProfileHandler;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.Author;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.Publication;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.openrdf.model.Model;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Provider to extract publication's URL from Google Scholar.
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class GoogleScholarProfileProvider extends AbstractHttpProvider {

    private static Logger log = LoggerFactory.getLogger(GoogleScholarProfileProvider.class);

    @Override
    protected List<String> buildRequestUrl(String resourceUri, Endpoint endpoint) throws DataRetrievalException {
        return Collections.singletonList(resourceUri);
    }

    @Override
    protected List<String> parseResponse(String resourceUri, String requestUrl, Model model, InputStream input, String contentType) throws DataRetrievalException {
        List<String> urls = new ArrayList<>();
        ValueFactory vf = ValueFactoryImpl.getInstance();
        try {
            Author author = new Author();
            IHandler handler = new ProfileHandler(author);
            handler.extract(input);
            boolean noMorePublications = true;
            int maxPub = Integer.parseInt(requestUrl.substring(requestUrl.indexOf("start=") + 6, requestUrl.indexOf("&pagesize"))) + 100;
            if (author.getNumPublications() == maxPub) {
                urls.add(resourceUri + "&cstart=" + maxPub + "&pagesize=100");
                noMorePublications = false;
            }

            if (noMorePublications) {
                for (Publication p : author.getPublications()) {
                    model.add(vf.createStatement(vf.createURI(resourceUri), REDI.GSCHOLAR_PUB, vf.createLiteral(p.getUrl())));
                }
            }
            return urls;
        } catch (MalformedURLException | SAXException | InterruptedException ex) {
            log.error("Cann't parse HTML in author object. Error: {}", ex);
        }
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return "Google Scholar Profile";
    }

    @Override
    public String[] listMimeTypes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
