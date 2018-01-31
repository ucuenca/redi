/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.edu.cedia.redi.ldclient.endpoint.scholar;

import ec.edu.cedia.redi.ldclient.provider.scholar.GSPublicationProvider;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class GSPublicationEndpoint extends Endpoint {

    public GSPublicationEndpoint() {
        super("Google Scholar Endpoint - Publications", GSPublicationProvider.PROVIDER_NAME,
                GSPublicationProvider.AUTHORS_SEARCH,
                null, 86400L);
        setPriority(PRIORITY_HIGH);
        addContentType(new ContentType("text", "html"));
    }

}
