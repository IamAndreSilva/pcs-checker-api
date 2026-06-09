package nl.andresilva.pcschecker.votes;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException() {
        super("Vote rate limit exceeded");
    }
}
