package at.newmedialab.lmf.worker.test;

import at.newmedialab.lmf.worker.services.WorkerRuntime;
import at.newmedialab.lmf.worker.services.WorkerServiceImpl;
import org.apache.marmotta.commons.sesame.filter.SesameFilter;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@ApplicationScoped
public class MockWorkerServiceImpl extends WorkerServiceImpl<MockWorkerRuntime,MockWorkerConfiguration> {

    @Inject
    private Logger log;

    @Override
    public String getName() {
        return "Mock Worker Service";
    }

    @Override
    public MockWorkerRuntime createWorker(MockWorkerConfiguration config) {
        return new MockWorkerRuntime(config);
    }

    @Override
    public List<MockWorkerConfiguration> listWorkerConfigurations() {
        return Collections.singletonList(new MockWorkerConfiguration("Mock Configuration"));
    }

    public Map<String,MockWorkerRuntime> getRuntimes() {
        return runtimes;
    }

    public Set<SesameFilter<Statement>> getStatementFilters() {
        return statementFilters;
    }
}
