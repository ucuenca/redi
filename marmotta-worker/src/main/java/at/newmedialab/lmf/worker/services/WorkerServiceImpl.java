package at.newmedialab.lmf.worker.services;

import static org.apache.marmotta.commons.sesame.repository.ExceptionUtils.handleRepositoryException;
import static org.apache.marmotta.commons.sesame.repository.ResourceUtils.listResources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.marmotta.commons.sesame.filter.AllOfFilter;
import org.apache.marmotta.commons.sesame.filter.SesameFilter;
import org.apache.marmotta.commons.sesame.filter.statement.StatementFilter;
import org.apache.marmotta.commons.sesame.transactions.model.TransactionData;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.statistics.StatisticsModule;
import org.apache.marmotta.platform.core.api.statistics.StatisticsService;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;

import at.newmedialab.lmf.worker.api.WorkerService;
import at.newmedialab.lmf.worker.model.WorkerConfiguration;
import java.util.Map;
import java.util.logging.Level;
import org.apache.marmotta.ldpath.model.programs.Program;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.commons.service.ExternalSPARQLService;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.QueryLanguage;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class WorkerServiceImpl<S extends WorkerRuntime<T>, T extends WorkerConfiguration> implements WorkerService<S, T> {

    @Inject
    protected Logger log;

    @Inject
    protected ConfigurationService configurationService;

    @Inject
    protected StatisticsService statisticsService;

    @Inject
    protected ExternalSPARQLService sesameService;

    private Map<String, URI> context;
    private Map<String, Set<URI>> types;

    public void filters(String nm, URI context, Set<URI> types) {
        if (this.context == null) {
            this.context = new HashMap<>();
        }
        if (this.types == null) {
            this.types = new HashMap<>();
        }
        this.context.put(nm, context);
        this.types.put(nm, types);

    }

    /**
     * The filters used by the enhancer to determine which resources are
     * relevant. The filters are and-connected, i.e. all filters need to match.
     */
    protected Set<SesameFilter<Statement>> statementFilters;

    // the currently active worker runtimes
    protected HashMap<String, S> runtimes;

    // used to ensure only one thread tries to rebuild the index
    //private ReentrantLock rebuildLock = new ReentrantLock();
    /**
     * This method is executed before rescheduling of resources in a
     * configuration takes place. Can be used to tun necessary cleanups before
     * execution. By default, does nothing.
     *
     * @param config
     */
    @Override
    public void doBeforeReschedule(T config) {

    }

    /**
     * This method is executed before rescheduling of resources in a
     * configuration takes place. Can be used to tun necessary cleanups before
     * execution. By default, does nothing.
     *
     * @param config
     */
    @Override
    public void doBeforeReschedule(T config, Resource resource) {

    }

    /**
     * This method is executed after rescheduling of resources in a
     * configuration takes place. Can be used to tun necessary cleanups after
     * execution. By default, does nothing.
     *
     * @param config
     */
    @Override
    public void doAfterReschedule(T config, Resource resource) {

    }

    /**
     * Reschedule enhancement for all resources in all enhancement engines,
     * replacing all previous enhancements. This method will clear the
     * enhancement graphs for all engines and place all resources into the
     * enhancement queues of all enhancement engines.
     */
    @Override
    public void reschedule() {
        for (WorkerRuntime<T> runtime : runtimes.values()) {
            reschedule(runtime.getConfiguration());
        }
    }

    /**
     * Reschedule the enhancement engine passed as argument for all resources,
     * replacing all previous enhancements done by this engine. This method will
     * simply remove all enhancements for this engine and place all resources
     * into the enhancement queue for the given engine.
     *
     * @param engine the configuration of the enhancement engine to run
     */
    @Override
    public void reschedule(T engine) {
        WorkerRuntime<T> runtime = runtimes.get(engine.getName());

        if (runtime != null) {

            if (runtime.getRescheduleLock().hasQueuedThreads()) {
                log.warn("{}: not rescheduling processing for {}, as another rescheduling job is already waiting", getName(), engine.getName());
            }

            // abort current queue
            runtime.abort();

            runtime.getRescheduleLock().lock();

            try {
                runtime.setAborted(false);

                doBeforeReschedule(engine);

                try {
                    int count = 0;
                    RepositoryConnection con = sesameService.getRepositoryConnetion();
                    try {
                        con.begin();

                        for (Resource rxc : this.types.get(engine.getName())) {

                            List<Map<String, Value>> query = sesameService.getSparqlService().query(QueryLanguage.SPARQL, "select distinct ?a { graph <" + this.context.get(engine.getName()) + "> { ?a a <" + rxc.stringValue() + "> }} ");
                            for (Map<String, Value> sc : query) {
                                //for (Iterator<Resource> it = listResources(con, rxc, this.context.get(engine.getName())).iterator(); it.hasNext() && !runtime.isAborted();) {
                                Resource r = ValueFactoryImpl.getInstance().createURI(sc.get("a").stringValue());
                                doBeforeReschedule(engine, r);

                                if (runtime.schedule(r)) {
                                    count++;
                                }

                                doAfterReschedule(engine, r);
                            }
                        }
                    } catch (MarmottaException ex) {
                        java.util.logging.Logger.getLogger(WorkerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        con.commit();
                        con.close();
                    }
                    log.info("{}: worker {} scheduled {} resources for processing", getName(), engine.getName(), count);
                } catch (RepositoryException e) {
                    handleRepositoryException(e, WorkerServiceImpl.class);
                }

            } finally {
                runtime.getRescheduleLock().unlock();
            }
        } else {
            log.error("{}: worker runtime for configuration {} does not exist, could not schedule resource", getName(), engine.getName());
        }
    }

    /**
     * Schedule enhancement of the given resource using the given enhancement
     * engine, and replacing all previous enhancements done by this engine for
     * this resource. This method will simply place the resource into the
     * enhancement queue for the given engine.
     *
     * @param engine
     * @param resource
     */
    @Override
    public void reschedule(T engine, Resource resource) {
        WorkerRuntime<T> runtime = runtimes.get(engine.getName());

        if (runtime != null) {
            doBeforeReschedule(engine, resource);

            runtime.schedule(resource);

            doAfterReschedule(engine, resource);
        } else {
            log.error("{}: worker runtime for configuration {} does not exist, could not schedule resource", getName(), engine.getName());
        }
    }

    /**
     * Startup the enhancement engine workers and initialise the queues for all
     * currently configured enhancement engines.
     */
    @Override
    @PostConstruct
    public void initialise() {
        log.info("LMF Worker Runtimes ({}) initializing ...", getName());
        if (runtimes != null) {
            log.warn("enhancement service is already running, aborting startup");
            return;
        }

        statementFilters = new HashSet<SesameFilter<Statement>>();

        StatementFilter filterCached = new StatementFilter() {
            @Override
            public boolean accept(Statement object) {
                if (object.getContext() != null && configurationService.getCacheContext().equals(object.getContext().stringValue())) {
                    return false;
                } else {
                    return true;
                }
            }
        };
        statementFilters.add(filterCached);

        StatementFilter filterInferred = new StatementFilter() {
            @Override
            public boolean accept(Statement object) {
                if (object.getContext() != null && configurationService.getInferredContext().equals(object.getContext().stringValue())) {
                    return false;
                } else {
                    return true;
                }
            }
        };
        statementFilters.add(filterInferred);

        runtimes = new HashMap<String, S>();

        // afterwards need to initialise worker runtimes ...
        for (T engine : listWorkerConfigurations()) {
            S runtime = createWorker(engine);
            registerWorkerRuntime(runtime);
        }

    }

    /**
     * Register the worker passed as argument with the service. Needs to be
     * called e.g. by the initialisation process
     *
     * @param runtime
     */
    @Override
    public void registerWorkerRuntime(S runtime) {
        runtimes.put(runtime.getConfiguration().getName(), runtime);
        final StatisticsModule stats = runtime.getStatistics();
        statisticsService.registerModule(stats.getName(), stats);
        log.info("{}: registered worker runtime {}", getName(), runtime.getConfiguration().getName());
    }

    /**
     * Remove the worker runtime passed as argument from the service. Subclasses
     * should override this method in case special actions need to be taken.
     *
     * @param runtime
     */
    @Override
    public void removeWorkerRuntime(S runtime) {
        runtime.shutdown();
        runtimes.remove(runtime.getConfiguration().getName());
        statisticsService.unregisterModule(runtime.getStatistics());
    }

    /**
     * Return a list of all currently active worker runtimes.
     */
    @Override
    public List<S> listWorkerRuntimes() {
        return new ArrayList<S>(runtimes.values());
    }

    /**
     * Shutdown the enhancement service, abort all workers, terminate all
     * threads, and clean up any active resources.
     */
    @Override
    @PreDestroy
    public void shutdown() {
        log.info("{}: shutting down workers", getName());
        for (WorkerRuntime<T> runtime : runtimes.values()) {
            runtime.shutdown();
            statisticsService.unregisterModule(runtime.getStatistics());
        }
        runtimes.clear();
    }

    /**
     * React on commit of a transaction and recompute all enhancements for the
     * updated resources. This method will simply schedule all changed resources
     * in the scheduling queues of all enhancement engines.
     * <p/>
     * This method should take care to ignore updates that have been triggered
     * by the enhancement process itself, or otherwise it might run into an
     * undesirable endless loop.
     *
     * @param data
     */
    @Override
    public void notifyTransactionCommit(TransactionData data) {

        SesameFilter<Statement> filter = new AllOfFilter<Statement>(statementFilters);

        for (WorkerRuntime<T> rt : runtimes.values()) {
            final T engine = rt.getConfiguration();

            Set<Resource> scheduled = new HashSet<Resource>();
            for (Statement stmt : data.getAddedTriples()) {
                // skip triples from reasoner, cache, and other enhancement steps
                if (filter.accept(stmt)) {
                    scheduled.add(stmt.getSubject());
                }
            }
            for (Statement stmt : data.getRemovedTriples()) {
                // skip triples from reasoner, cache, and other enhancement steps
                if (filter.accept(stmt)) {
                    scheduled.add(stmt.getSubject());
                }
            }
            for (Resource r : scheduled) {
                reschedule(engine, r);
            }

        }

    }

    /**
     * Notify the enhancer engine service that the given enhancement engine has
     * been added to the system. This method will trigger the creation of a
     * queue for the engine and trigger a complete rescheduling of all resources
     * for the given engine.
     *
     * @param engine
     */
    @Override
    public void notifyEngineAdd(T engine) {
        synchronized (runtimes) {
            if (runtimes.get(engine.getName()) == null) {
                S runtime = createWorker(engine);
                registerWorkerRuntime(runtime);
                reschedule(engine);
            } else {
                log.warn("{}: enhancement runtime with name {} already running, updating configuration", getName(), engine.getName());
                notifyEngineUpdate(engine);
            }
        }
    }

    /**
     * Notify the enhancer engine service that the given enhancement engine has
     * been updated. This method will trigger a complete rescheduling of all
     * resources for the given engine.
     *
     * @param engine
     */
    @Override
    public void notifyEngineUpdate(T engine) {
        WorkerRuntime<T> runtime = runtimes.get(engine.getName());

        if (runtime != null) {
            // update runtime configuration
            runtime.setConfiguration(engine);

            reschedule(engine);
        } else {
            log.warn("{}: worker runtime with name {} not found", getName(), engine.getName());
        }
    }

    /**
     * Notify the enhancer engine service that the given enhancement engine has
     * been removed from the system. This method will trigger removing all
     * enhancement results of this engine and cleanup all resources currently
     * occupied by the engine (e.g. enhancement queue).
     *
     * @param engine
     */
    @Override
    public void notifyEngineRemove(T engine) {
        synchronized (runtimes) {
            S runtime = runtimes.get(engine.getName());
            if (runtime != null) {
                removeWorkerRuntime(runtime);
            } else {
                log.warn("{}: could not remove enhancement runtime for engine {}; it is not active", getName(), engine.getName());
            }
        }
    }

}
