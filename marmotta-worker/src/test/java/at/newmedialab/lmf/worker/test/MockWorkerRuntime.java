package at.newmedialab.lmf.worker.test;

import at.newmedialab.lmf.worker.model.WorkerConfiguration;
import at.newmedialab.lmf.worker.services.WorkerRuntime;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.openrdf.model.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MockWorkerRuntime extends WorkerRuntime<MockWorkerConfiguration> {

    private List<String> processed = new ArrayList<String>();

    public MockWorkerRuntime(MockWorkerConfiguration config) {
        super(config);
    }

    @Override
    protected void execute(Resource resource) {
        processed.add(resource.stringValue());
    }

    @Override
    protected TaskManagerService getTaskManagerService() {
        return new MockTaskManagerService();
    }

    public List<String> getProcessed() {
        return processed;
    }
}
