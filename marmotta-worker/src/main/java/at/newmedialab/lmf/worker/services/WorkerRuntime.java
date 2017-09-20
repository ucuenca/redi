package at.newmedialab.lmf.worker.services;

import at.newmedialab.lmf.worker.model.ResourceStatus;
import at.newmedialab.lmf.worker.model.WorkerConfiguration;
import org.apache.marmotta.platform.core.api.statistics.StatisticsModule;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.apache.marmotta.platform.core.util.CDIContext;
import org.apache.marmotta.platform.core.util.LinkedHashSetBlockingQueue;
import org.openrdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Base class for a worker runtime that executes the job.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class WorkerRuntime<T extends WorkerConfiguration> {

    private static Logger log = LoggerFactory.getLogger(WorkerRuntime.class);

    // configuration of the worker
    protected T config;

    // for generating unique thread names
    private int workerCounter = 0;


    /**
     * The queue that is used as producer-consumer by the workers and scheduler
     */
    private BlockingQueue<Resource> resourceQueue;

    // workers
    private Set<Worker> workers;

    // statistics collection
    private final WorkerStatsModule statistics;


    private ReentrantLock rescheduleLock;

    private boolean aborted;

    /**
     * Initialise the enhancement runtime, start a new queue and start up the configured number of workers.
     *
     * @param config the worker configuration
     */
    protected WorkerRuntime(T config) {
        this.config = config;

        rescheduleLock = new ReentrantLock();

        workers = new HashSet<Worker>();

        resourceQueue = new LinkedHashSetBlockingQueue<Resource>(config.getQueueSize());
//        resourceQueue = new HashedLinkedBlockingQueue<Resource>();

        statistics = new WorkerStatsModule();

        aborted    = false;

        addThreads(config.getThreads());
    }

    public ReentrantLock getRescheduleLock() {
        return rescheduleLock;
    }

    /**
     * Return the statistics module for this runtime.
     * @return
     */
    public StatisticsModule getStatistics() {
        return statistics;
    }

    /**
     * Return the engine configuration of this runtime
     * @return
     */
    public T getConfiguration() {
        return config;
    }

    /**
     * Update the engine configuration used by this runtime
     * @param engine
     */
    public void setConfiguration(T engine) {
        this.config = engine;

        // update worker pool
        if(engine.getThreads() != this.getThreads()) {
            this.setThreads(engine.getThreads());
        }
    }

    public boolean schedule(Resource resource) {
        synchronized (resourceQueue) {
            if(config.accept(resource)) {
                resourceQueue.remove(resource);
                if (!resourceQueue.offer(resource)) {
                    log.info("waiting for resource queue to become available ...");
                    try {
                        resourceQueue.put(resource);
                        log.info("resource queue available, added resource {}", resource.toString());
                    } catch (InterruptedException e) {
                        log.error("interrupted while waiting for resource queue to become available ...");
                    }
                };
                return true;
            } else
                return false;
        }
    }

    /**
     * Return the number of currently active workers of this runtime.
     * @return
     */
    public int getThreads() {
        return workers.size();
    }

    /**
     * Add the number of worker threads given as argument to the current runtime. The threads are started immediately
     * and added to the worker pool.
     *
     * @param count
     */
    public void addThreads(int count) {
        log.info("starting {} new worker threads",count);
        synchronized (workers) {
            for(int i=0; i<count; i++) {
                Worker worker = new Worker();
                workers.add(worker);
            }

        }
    }

    /**
     * Remove up to "count" number of worker threads. This method will first shutdown each worker and then remove it
     * from the worker pool. If the worker pool is empty, no more workers will be removed.
     * @param count
     */
    public void removeThreads(int count) {
        log.info("shutting down up to {} worker threads",count);
        synchronized (workers) {
            int removed = 0;
            for(Iterator<Worker> it = workers.iterator(); it.hasNext() && removed < count; ) {
                Worker worker = it.next();
                worker.shutdown();
                it.remove();
                removed ++;
            }
        }
    }

    /**
     * Set the number of worker threads to exactly the number given as argument.
     * @param count
     */
    public void setThreads(int count) {
        synchronized (workers) {
            if(workers.size() > count) {
                removeThreads(workers.size() - count);
            } else if(workers.size() < count) {
                addThreads(count - workers.size());
            }
        }
    }

    /**
     * Get the status of the given resource with respect to this runtime.
     * @param resource
     * @return
     */
    public ResourceStatus getResourceStatus(Resource resource) {
        if(resourceQueue.contains(resource)) {
            return ResourceStatus.WAITING;
        } else {
            for(Worker worker : workers) {
                if(resource.equals(worker.getProcessing())) {
                    return ResourceStatus.PROCESSING;
                }
            }

            return ResourceStatus.NONE;
        }
    }



    public void abort() {
        aborted = true;

        if(resourceQueue.size() > 0) {
            log.info("aborting current resource queue (size: {})",resourceQueue.size());

            resourceQueue.clear();
        }
    }


    public boolean isAborted() {
        return aborted;
    }

    public void setAborted(boolean aborted) {
        this.aborted = aborted;
    }


    /**
     * Return when none of the threads are performing actions (i.e. all are waiting for resources).
     * @return
     */
     public boolean isRunning() {
        for(Worker t : workers) {
            if(t.getState() == Thread.State.RUNNABLE) return true;
        }
        return false;
    }


    /**
     * Execute the worker for the resource given as argument. Needs to be implemented by subclasses.
     *
     * @param resource
     */
    protected abstract void execute(Resource resource);


    /**
     * Shutdown the enhancement runtime cleanly, stop all worker threads, empty the queue.
     */
    public void shutdown() {
        log.info("shutting down worker runtime {}", config.getName());
        synchronized (workers) {
            for(Iterator<Worker> it = workers.iterator(); it.hasNext(); ) {
                Worker worker = it.next();
                worker.shutdown();
                it.remove();
            }
        }

        // clear the queue; this operation might block until all workers are finished with their last task
        if(resourceQueue.size() > 0) {
            log.warn("resource queue for engine {} is not empty when shutting down, it may be necessary to restart the computation", config.getName());
            resourceQueue.clear();
        }

        log.info("shutdown of worker runtime {} successfully completed",config.getName());
    }


    protected TaskManagerService getTaskManagerService() {
        return CDIContext.getInstance(TaskManagerService.class);
    }


    private class Worker extends Thread {


        private boolean shutdown = false;

        private long         itemsProcessed    = 0, lastProcessingTime = 0, avgProcessingTime = 0;
        private long         processingHistory = 25;

        private Resource processing;

        private TaskManagerService taskManagerService = getTaskManagerService();

        private Worker() {
            super(config.getType() + " Worker Thread ("+config.getName()+",id=" + ++workerCounter+")");
            setDaemon(true);

            start();
        }

        public void shutdown() {
            shutdown = true;
            this.interrupt();
        }

        public Resource getProcessing() {
            return processing;
        }


        @Override
        public void run() {
            log.info("{} {} starting up ...", config.getType(), getName());
            itemsProcessed = 0;
            Task task = taskManagerService.createTask(getName(), config.getType());

            while (!shutdown || resourceQueue.size() > 0) {
                try {
                    task.updateMessage("idle");

                    // wait until a resource becomes availabe
                    Resource resource = resourceQueue.take();
                    processing = resource;
                    task.updateMessage("processing resource " + getProcessing().toString());

                    long pStart = System.nanoTime();

                    // run the enhancement engine
                    try {
                        execute(resource);
                    } catch(Exception ex) {
                        log.error("error while executing worker thread: {}",ex.getMessage());
                        log.debug("Exception:",ex);
                    }

                    processing = null;

                    // update the statistics of this worker
                    lastProcessingTime = System.nanoTime() - pStart;
                    itemsProcessed++;
                    task.updateProgress(itemsProcessed);

                    updateStatistics();

                } catch (InterruptedException ex) {
                    if(shutdown) {
                        log.info("worker execution interrupted on shutdown");
                    }
                }
            }
            try {
                task.endTask();
            } catch (Exception ex) {
            }
            log.info("{} shutting down ...", getName());
        }

        private void updateStatistics() {
            if (itemsProcessed <= processingHistory) {
                avgProcessingTime = (avgProcessingTime * (itemsProcessed - 1) + lastProcessingTime) / itemsProcessed;
            } else {
                avgProcessingTime = (avgProcessingTime * (processingHistory - 1) + lastProcessingTime) / processingHistory;
            }
        }

    }



    private class WorkerStatsModule implements StatisticsModule {

        boolean enabled = true;

        @Override
        public void enable() {
            enabled = true;
        }

        @Override
        public void disable() {
            enabled = false;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public List<String> getPropertyNames() {
            LinkedList<String> props = new LinkedList<String>();
            props.add("worker threads");
            props.add("resource queue size");
            props.add("resource queue maxSize");
            props.add("resource queue load");

            for (Worker w : workers) {
                props.addAll(getWorkerProps(w));
            }

            return props;
        }

        @Override
        public Map<String, String> getStatistics() {
            if (!enabled) return Collections.emptyMap();
            LinkedHashMap<String, String> stats = new LinkedHashMap<String, String>();

            stats.put("worker threads", String.format("%d", workers.size()));
            final int qSize = resourceQueue.size();
            final int qFree = resourceQueue.remainingCapacity();
            stats.put("resource queue size", String.format("%d", qSize));
            stats.put("resource queue maxSize", String.format("%d", qSize + qFree));
            stats.put("resource queue load", String.format("%.1f%%", 100f * qSize / (qSize + qFree)));

            for (Worker w : workers) {
                stats.putAll(getWorkerStats(w));
            }

            return stats;
        }

        @Override
        public String getName() {
            return "Worker Statistics (Engine "+config.getType() + " " + config.getName()+")";
        }


        private List<String> getWorkerProps(Worker indexer) {
            LinkedList<String> props = new LinkedList<String>();

            props.add(indexer.getName() + " processed items");
            props.add(indexer.getName() + " last processing time");
            props.add(indexer.getName() + " average processing time");
            props.add(indexer.getName() + " average processing time history");

            return props;
        }


        private Map<String, String> getWorkerStats(Worker indexer) {
            LinkedHashMap<String, String> stats = new LinkedHashMap<String, String>();

            stats.put(indexer.getName() + " processed items", String.valueOf(indexer.itemsProcessed));
            stats.put(indexer.getName() + " last processing time", String.format("%.3fms", indexer.lastProcessingTime / 1e6d));
            stats.put(indexer.getName() + " average processing time", String.format("%.3fms", indexer.avgProcessingTime / 1e6d));
            stats.put(indexer.getName() + " average processing time history", String.valueOf(indexer.processingHistory));

            return stats;
        }

    }

}
