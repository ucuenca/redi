package org.apache.marmotta.ucuenca.wk.provider.scopus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javolution.util.function.Predicate;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.openrdf.model.Model;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

@ApplicationScoped
@Deprecated
public class ScopusPublicationProvider
        extends AbstractHttpProvider {

    public static final String NAME = "Scopus Publication Provider";
    public static final String API = "http://api.elsevier.com/content/abstract/doi/?apiKey=&httpAccept=application/rdf%2Bxml";
    public static final String PATTERN = "http://api\\.elsevier\\.com/content/abstract/doi/(.*)\\?apiKey\\=(.*)\\&httpAccept\\=application/rdf%2Bxml(.*)";
    //private static Logger log = LoggerFactory.getLogger((Class) ScopusPublicationProvider.class);

    public String getName() {
        return "Scopus Publication Provider";
    }

    public String[] listMimeTypes() {
        return new String[]{"application/rdf+xml"};
    }

    public List<String> buildRequestUrl(String resource, Endpoint endpoint) {
        String url = null;
        Matcher m = Pattern.compile("http://api\\.elsevier\\.com/content/abstract/doi/(.*)\\?apiKey\\=(.*)\\&httpAccept\\=application/rdf%2Bxml(.*)").matcher(resource);
        if (m.find()) {
            url = resource;
        }
        return Collections.singletonList(url);
    }

    public List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {
        //log.debug("Request Successful to {0}", (Object) requestUrl);
        RDFFormat format = RDFFormat.forMIMEType((String) contentType);
        try {
            ModelCommons.add((Model) triples, (InputStream) input, (String) resource, (RDFFormat) format, (Predicate[]) new Predicate[0]);
        } catch (RDFParseException e) {
            throw new DataRetrievalException("Error while parsing response", (Throwable) e);
        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing response", (Throwable) e);
        }
        return Collections.emptyList();
    }
}
