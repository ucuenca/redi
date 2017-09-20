package at.newmedialab.lmf.worker.test;

import at.newmedialab.lmf.worker.model.WorkerConfiguration;
import org.apache.marmotta.commons.sesame.filter.SesameFilter;
import org.openrdf.model.Resource;

import java.util.Set;

/**
 * Mock worker configuration doing nothing
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MockWorkerConfiguration extends WorkerConfiguration {

    public MockWorkerConfiguration(String name) {
        super(name);
    }

    public MockWorkerConfiguration(String name, Set<SesameFilter<Resource>> filters) {
        super(name, filters);
    }

    @Override
    public String getType() {
        return "Mock";
    }
}
