package at.newmedialab.lmf.worker.test;

import org.apache.marmotta.commons.sesame.filter.AllOfFilter;
import org.apache.marmotta.commons.sesame.filter.SesameFilter;
import org.apache.marmotta.commons.sesame.transactions.model.TransactionData;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.test.base.EmbeddedMarmotta;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the functionalities of the base worker service (using the MockWorkerServiceImpl).
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class WorkerServiceTest {

    private static Logger log = LoggerFactory.getLogger(WorkerServiceTest.class);

    private static EmbeddedMarmotta marmotta;

    private static MockWorkerServiceImpl workerService;

    @BeforeClass
    public static void setupMarmotta() {
        marmotta = new EmbeddedMarmotta();
        workerService = marmotta.getService(MockWorkerServiceImpl.class);
    }


    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }


    /**
     * Test for existance of configured runtimes (i.e. initialisation works)
     * @throws Exception
     */
    @Test
    public void testRuntimes() throws Exception {
        Assert.assertNotNull(workerService.getRuntimes());
        Assert.assertEquals(1, workerService.getRuntimes().size());

    }


    @Test
    public void testFilters() throws Exception {
        Assert.assertNotNull(workerService.getStatementFilters());

        SesameFilter<Statement> filter = new AllOfFilter<Statement>(workerService.getStatementFilters());

        URI r1 = new URIImpl("http://localhost/resource/1");
        URI r2 = new URIImpl("http://localhost/resource/2");
        URI r3 = new URIImpl("http://localhost/resource/3");

        ConfigurationService configurationService = marmotta.getService(ConfigurationService.class);

        URI c1 = new URIImpl(configurationService.getCacheContext());
        URI c2 = new URIImpl("http://localhost/context/default");

        Statement stmt1 = new ContextStatementImpl(r1,r2,r3, c1);
        Statement stmt2 = new ContextStatementImpl(r1,r2,r3, c2);

        Assert.assertFalse(filter.accept(stmt1));
        Assert.assertTrue(filter.accept(stmt2));

    }


    /**
     * Test adding and removing a configuration to/from the worker service.
     * @throws Exception
     */
    @Test
    public void testAddRemoveRuntimes() throws Exception {
        MockWorkerConfiguration configuration = new MockWorkerConfiguration("Mock Configuration Add/Remove");

        workerService.notifyEngineAdd(configuration);

        Assert.assertEquals(2, workerService.getRuntimes().size());

        workerService.notifyEngineRemove(configuration);

        Assert.assertEquals(1, workerService.getRuntimes().size());

    }


    /**
     * Test scheduling and processing of resources. We add another runtime, then reschedule three different
     * resources and check if the processed resources afterwards contain the scheduled resources. The second
     * runtime is then removed again to clean up.
     *
     * @throws Exception
     */
    @Test
    public void testSchedule() throws Exception {

        URI r1 = new URIImpl("http://localhost/resource/1");
        URI r2 = new URIImpl("http://localhost/resource/2");
        URI r3 = new URIImpl("http://localhost/resource/3");

        MockWorkerConfiguration configuration = new MockWorkerConfiguration("Mock Configuration Schedule");

        workerService.notifyEngineAdd(configuration);

        workerService.reschedule(configuration, r1);
        workerService.reschedule(configuration, r2);
        workerService.reschedule(configuration, r3);

        // wait a second for the threads to complete
        Thread.sleep(1000);

        // check if all resources are contained in the runtime processed items
        MockWorkerRuntime runtime = (MockWorkerRuntime) workerService.getRuntimes().get(configuration.getName());
        Assert.assertTrue(runtime.getProcessed().contains(r1.stringValue()));
        Assert.assertTrue(runtime.getProcessed().contains(r2.stringValue()));
        Assert.assertTrue(runtime.getProcessed().contains(r3.stringValue()));

        workerService.notifyEngineRemove(configuration);
    }

    /**
     * Simulate a transaction commit by creating some transaction data. Some of the statements should be
     * in the inferred scope so they are ignored. We create a new runtime to keep track of the processed
     * resources and remove it afterwards to clean up.
     *
     * @throws Exception
     */
    @Test
    public void testCommit() throws Exception {


        URI r1 = new URIImpl("http://localhost/resource/1");
        URI r2 = new URIImpl("http://localhost/resource/2");
        URI r3 = new URIImpl("http://localhost/resource/3");
        URI r4 = new URIImpl("http://localhost/resource/4");
        URI r5 = new URIImpl("http://localhost/resource/5");
        URI r6 = new URIImpl("http://localhost/resource/6");

        ConfigurationService configurationService = marmotta.getService(ConfigurationService.class);

        URI c1 = new URIImpl(configurationService.getCacheContext());
        URI c2 = new URIImpl("http://localhost/context/default");

        Statement stmt1 = new ContextStatementImpl(r1,r2,r3, c1);
        Statement stmt2 = new ContextStatementImpl(r4,r2,r4, c2);
        Statement stmt3 = new ContextStatementImpl(r5,r2,r5, c2);
        Statement stmt4 = new ContextStatementImpl(r6,r2,r6, c2);

        TransactionData data = new TransactionData();
        data.addTriple(stmt1);
        data.addTriple(stmt2);
        data.addTriple(stmt3);
        data.addTriple(stmt4);


        MockWorkerConfiguration configuration = new MockWorkerConfiguration("Mock Configuration Commit");

        workerService.notifyEngineAdd(configuration);

        workerService.notifyTransactionCommit(data);

        // wait a second for the threads to complete
        Thread.sleep(1000);

        // check if all resources are contained in the runtime processed items
        MockWorkerRuntime runtime = (MockWorkerRuntime) workerService.getRuntimes().get(configuration.getName());
        Assert.assertTrue(runtime.getProcessed().contains(r4.stringValue()));
        Assert.assertTrue(runtime.getProcessed().contains(r5.stringValue()));
        Assert.assertTrue(runtime.getProcessed().contains(r6.stringValue()));
        Assert.assertEquals(3, runtime.getProcessed().size());

        workerService.notifyEngineRemove(configuration);

    }
}
