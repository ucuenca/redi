package at.newmedialab.lmf.search.services.cores;

import org.apache.marmotta.commons.sesame.filter.SesameFilter;
import org.apache.marmotta.ldpath.backend.sesame.SesameConnectionBackend;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.util.Collections;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class LDPathProgramFilter implements SesameFilter<Resource> {

    private ExternalSPARQLService sesameService;
    private SolrCoreConfiguration configuration;

    public LDPathProgramFilter(SolrCoreConfiguration configuration, ExternalSPARQLService sesameService) {
        this.sesameService = sesameService;
        this.configuration = configuration;
    }

    @Override
    public boolean accept(Resource resource) {
        if(configuration.getProgram() != null && configuration.getProgram().getFilter() != null) {
            try {
                RepositoryConnection connection = sesameService.getRepositoryConnetion();
                try {
                    connection.begin();
                    SesameConnectionBackend backend = SesameConnectionBackend.withConnection(connection);

                    return configuration.getProgram().getFilter().apply(backend, resource, Collections.singleton((Value) resource));

                } finally {
                    connection.commit();
                    connection.close();
                }

            } catch (RepositoryException e) {
                return false;
            }
        }
        return true;
    }
}
