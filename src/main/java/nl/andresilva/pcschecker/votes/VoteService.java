package nl.andresilva.pcschecker.votes;

import nl.andresilva.pcschecker.auth.User;
import nl.andresilva.pcschecker.auth.UserRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class VoteService {

    private static final int IP_RATE_LIMIT_PER_HOUR = 20;

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;

    public VoteService(VoteRepository voteRepository, UserRepository userRepository) {
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public VoteSummaryDto castVote(Integer tmdbMovieId, boolean hasPcs,
                                   String anonymousToken, OAuth2User oauthUser, String ipAddress) {
        checkRateLimit(ipAddress);

        if (oauthUser != null) {
            User user = resolveUser(oauthUser);
            voteRepository.findByTmdbMovieIdAndUserId(tmdbMovieId, user.getId())
                    .ifPresentOrElse(
                            v -> v.setHasPcs(hasPcs),
                            () -> voteRepository.save(new Vote(tmdbMovieId, hasPcs, null, user, ipAddress))
                    );
            return buildSummary(tmdbMovieId, null, user.getId());
        }

        validateAnonymousToken(anonymousToken);
        voteRepository.findByTmdbMovieIdAndAnonymousToken(tmdbMovieId, anonymousToken)
                .ifPresentOrElse(
                        v -> v.setHasPcs(hasPcs),
                        () -> voteRepository.save(new Vote(tmdbMovieId, hasPcs, anonymousToken, null, ipAddress))
                );
        return buildSummary(tmdbMovieId, anonymousToken, null);
    }

    @Transactional
    public VoteSummaryDto retractVote(Integer tmdbMovieId, String anonymousToken,
                                      OAuth2User oauthUser) {
        if (oauthUser != null) {
            User user = resolveUser(oauthUser);
            voteRepository.findByTmdbMovieIdAndUserId(tmdbMovieId, user.getId())
                    .ifPresent(voteRepository::delete);
            return buildSummary(tmdbMovieId, null, user.getId());
        }

        validateAnonymousToken(anonymousToken);
        voteRepository.findByTmdbMovieIdAndAnonymousToken(tmdbMovieId, anonymousToken)
                .ifPresent(voteRepository::delete);
        return buildSummary(tmdbMovieId, anonymousToken, null);
    }

    @Transactional(readOnly = true)
    public VoteSummaryDto getSummary(Integer tmdbMovieId, String anonymousToken, OAuth2User oauthUser) {
        UUID userId = oauthUser != null ? resolveUser(oauthUser).getId() : null;
        return buildSummary(tmdbMovieId, anonymousToken, userId);
    }

    private VoteSummaryDto buildSummary(Integer tmdbMovieId, String anonymousToken, UUID userId) {
        long yes = voteRepository.countYesByMovieId(tmdbMovieId);
        long no  = voteRepository.countNoByMovieId(tmdbMovieId);

        Boolean userVote = null;
        if (userId != null) {
            Optional<Vote> v = voteRepository.findByTmdbMovieIdAndUserId(tmdbMovieId, userId);
            if (v.isPresent()) userVote = v.get().getHasPcs();
        } else if (anonymousToken != null) {
            Optional<Vote> v = voteRepository.findByTmdbMovieIdAndAnonymousToken(tmdbMovieId, anonymousToken);
            if (v.isPresent()) userVote = v.get().getHasPcs();
        }

        return new VoteSummaryDto(yes, no, yes + no, userVote);
    }

    private void checkRateLimit(String ipAddress) {
        if (ipAddress == null) return;
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        long count = voteRepository.countByIpAddressSince(ipAddress, oneHourAgo);
        if (count >= IP_RATE_LIMIT_PER_HOUR) {
            throw new RateLimitExceededException();
        }
    }

    private void validateAnonymousToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Anonymous token required");
        }
        try {
            UUID.fromString(token);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid anonymous token format");
        }
    }

    private User resolveUser(OAuth2User oauthUser) {
        return userRepository.findByOauthId(oauthUser.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB"));
    }
}
