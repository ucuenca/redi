package at.newmedialab.lmf.worker.model;

/**
 * Represents the status of a resource in a worker configuration
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public enum ResourceStatus {
    WAITING ("waiting in queue"),
    PROCESSING ("processing"),
    NONE ("none");

    private String message;

    private ResourceStatus(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
