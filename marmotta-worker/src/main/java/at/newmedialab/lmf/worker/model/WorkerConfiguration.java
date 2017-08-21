package at.newmedialab.lmf.worker.model;

import org.apache.marmotta.commons.sesame.filter.AlwaysTrueFilter;
import org.apache.marmotta.commons.sesame.filter.SesameFilter;
import org.openrdf.model.Resource;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic worker configuration with support for common configuration options for workers
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class WorkerConfiguration {


    protected String name;

    /**
     * How many threads to start. Default is 2.
     */
    protected int threads = 2;


    /**
     * A set of filters for pre-filtering whether a resource will be accepted or not by the worker.
     * All filters must return true in order for a resource to be accepted. Note that resource
     * filters should only implement methods that are not expensive to compute.
     */
    protected Set<SesameFilter<Resource>> filters;

    /**
     * Queue size for the runtime; if the queue capacity has been reached, adding new elements to the queue will block.
     */
    protected int queueSize = 100000;

    public WorkerConfiguration(String name) {
        this.name = name;
        this.filters = new HashSet<SesameFilter<Resource>>();
        this.filters.add(new AlwaysTrueFilter<Resource>());
    }

    public WorkerConfiguration(String name, Set<SesameFilter<Resource>> filters) {
        this.name = name;
        this.filters = filters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public Set<SesameFilter<Resource>> getFilters() {
        return filters;
    }

    public void setFilters(Set<SesameFilter<Resource>> filters) {
        this.filters = filters;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    /**
     * Check whether the enhancer is applicable to the resource passed as argument. We require that all
     * filters return "accept".
     *
     * @param resource
     * @return
     */
    public boolean accept(Resource resource) {
        for (SesameFilter<Resource> filter : filters) {
            if(!filter.accept(resource)) return false;
        }

        return true;
    }

    /**
     * Return the name of the type of this worker (e.g. "Enhancer" or "SOLR Indexer"). Will be used for status messages.
     * @return
     */
    public abstract String getType();
}
