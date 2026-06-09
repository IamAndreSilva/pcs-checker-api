package nl.andresilva.pcschecker.votes;

public record VoteSummaryDto(long yes, long no, long total, Boolean userVote) {}
