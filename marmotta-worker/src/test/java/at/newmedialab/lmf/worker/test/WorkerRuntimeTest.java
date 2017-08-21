package at.newmedialab.lmf.worker.test;

import at.newmedialab.lmf.worker.model.WorkerConfiguration;
import at.newmedialab.lmf.worker.services.WorkerRuntime;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class WorkerRuntimeTest {

    private WorkerConfiguration config;

    private WorkerRuntime runtime;

    private int executed = 0;

    @Before
    public void setup() {
        config = new MockWorkerConfiguration("MockConfiguration");

        runtime = new WorkerRuntime(config) {
            @Override
            protected void execute(Resource resource) {
                executed++;
            }

            @Override
            protected TaskManagerService getTaskManagerService() {
                return new MockTaskManagerService();
            }
        };
    }

    @After
    public void shutdown() {
        runtime.shutdown();
    }

    /**
     * Test if workers are executed.
     */
    @Test
    public void testExecution() throws Exception {
        URI resource1 = new URIImpl("http://localhost/resource/R1");
        URI resource2 = new URIImpl("http://localhost/resource/R2");

        Assert.assertEquals(0, executed);

        runtime.schedule(resource1);

        // wait for one second and check if item has been processed
        Thread.sleep(1000);

        Assert.assertEquals(1, executed);

        runtime.schedule(resource2);

        Thread.sleep(1000);

        Assert.assertEquals(2, executed);
    }

    /**
     * Test adding and removing threads
     *
     * @throws Exception
     */
    @Test
    public void testThreads() throws Exception {
        Assert.assertEquals(config.getThreads(), runtime.getThreads());

        // add two threads and check if they are started
        runtime.addThreads(2);

        Assert.assertEquals(config.getThreads() + 2, runtime.getThreads());

        // remove threads and check if afterwards only the remaining number of threads are left
        runtime.removeThreads(3);

        Assert.assertEquals(config.getThreads() - 1, runtime.getThreads());

        // set the number of threads explicitly to the number given
        runtime.setThreads(3);

        Assert.assertEquals(3, runtime.getThreads());
    }


}
