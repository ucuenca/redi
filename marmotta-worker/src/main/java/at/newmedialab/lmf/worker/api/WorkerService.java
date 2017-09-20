package at.newmedialab.lmf.worker.api;

import java.util.List;

import org.apache.marmotta.commons.sesame.transactions.model.TransactionData;
import org.openrdf.model.Resource;

import at.newmedialab.lmf.worker.model.WorkerConfiguration;
import at.newmedialab.lmf.worker.services.WorkerRuntime;

/**
 * The worker service manages and runs the configured LMF workers and provides common utility functions,
 * e.g. for re-running an enhancer or updating the enhancements of a resource.
 * <p/>
 * The enhancer service maintains a producer-consumer model for resource updates (similar to the SolrIndexingService),
 * i.e. when a transaction completes, all changed resources are added to blocking queues in the thread completing
 * the transaction, while separate enhancer threads pick up resources from the queue in turn and run the worker
 * engines on them.
 * <p/>
 * Author: Sebastian Schaffert
 */
public interface WorkerService<S extends WorkerRuntime<T>,T extends WorkerConfiguration> {


    /**
     * Return a name to identify this worker service implementation. Needs to be implemented by subclasses.
     * @return
     */
    public String getName();

    /**
     * Reschedule all resources for all configured workers.
     */
    public void reschedule();

    /**
     * Reschedule the worker engine passed as argument for all resources. This method will simply place all resources
     * into the resource queue for the given engine.
     *
     * @param engine the configuration of the worker to run
     */
    public void reschedule(T engine);


    /**
     * Schedule execution of the given resource using the given worker engine. This method will simply place the
     * resource into the resource queue for the given engine.
     *
     * @param engine
     * @param resource
     */
    public void reschedule(T engine, Resource resource);


    /**
     * Startup the enhancement engine workers and initialise the queues for all currently configured enhancement
     * engines.
     */
    public void initialise();


    /**
     * Shutdown the enhancement service, abort all workers, terminate all threads, and clean up any active
     * resources.
     */
    public void shutdown();


    /**
     * React on commit of a transaction and recompute all enhancements for the updated resources. This method will
     * simply schedule all changed resources in the scheduling queues of all enhancement engines.
     * <p/>
     * This method should take care to ignore updates that have been triggered by the enhancement process itself,
     * or otherwise it might run into an undesirable endless loop.
     *
     * @param data
     */
    public void notifyTransactionCommit(TransactionData data);


    /**
     * Notify the enhancer engine service that the given enhancement engine has been added to the system. This method
     * will trigger the creation of a queue for the engine and trigger a complete rescheduling of all resources for
     * the given engine.
     *
     * @param engine
     */
    public void notifyEngineAdd(T engine);


    /**
     * Notify the enhancer engine service that the given enhancement engine has been updated. This method will
     * trigger a complete rescheduling of all resources for the given engine.
     *
     * @param engine
     */
    public void notifyEngineUpdate(T engine);


    /**
     * Notify the enhancer engine service that the given enhancement engine has been removed from the system.
     * This method will trigger removing all enhancement results of this engine and cleanup all resources currently
     * occupied by the engine (e.g. enhancement queue).
     *
     * @param engine
     */
    public void notifyEngineRemove(T engine);

    public void registerWorkerRuntime(S runtime);

    /**
     * Create a worker runtime using the configuration passed as argument. Needs to be implemented by subclasses.
     * @param config
     * @return
     */
    public S createWorker(T config);

    /**
     * Return a list of all currently active worker configurations. Needs to be implemented by subclasses.
     * @return
     */
    List<T> listWorkerConfigurations();


    /**
     * Return a list of all currently active worker runtimes.
     */
    public List<S> listWorkerRuntimes();

    void doBeforeReschedule(T config);

    void doBeforeReschedule(T config, Resource resource);

    void removeWorkerRuntime(S runtime);

    void doAfterReschedule(T config, Resource resource);

}
