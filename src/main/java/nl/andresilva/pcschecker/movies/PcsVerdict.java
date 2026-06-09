package nl.andresilva.pcschecker.movies;

public enum PcsVerdict {
    YES, NO, UNKNOWN;

    public static PcsVerdict fromVotes(long yes, long no) {
        long total = yes + no;
        if (total < 3) return UNKNOWN;
        double ratio = (double) yes / total;
        if (ratio >= 0.6) return YES;
        if (ratio <= 0.4) return NO;
        return UNKNOWN;
    }
}
