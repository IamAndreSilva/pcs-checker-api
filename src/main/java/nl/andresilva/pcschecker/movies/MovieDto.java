package nl.andresilva.pcschecker.movies;

import nl.andresilva.pcschecker.votes.VoteSummaryDto;

public record MovieDto(
        int tmdbId,
        String title,
        int year,
        String posterUrl,
        String overview,
        PcsVerdict verdict,
        VoteSummaryDto votes
) {}
