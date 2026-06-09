package nl.andresilva.pcschecker.votes;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies/{tmdbId}/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping
    public ResponseEntity<VoteSummaryDto> castVote(
            @PathVariable Integer tmdbId,
            @RequestBody VoteRequestDto request,
            @RequestHeader(value = "X-Anonymous-Token", required = false) String anonymousToken,
            @AuthenticationPrincipal OAuth2User oauthUser,
            HttpServletRequest httpRequest) {

        VoteSummaryDto summary = voteService.castVote(
                tmdbId, request.hasPcs(), anonymousToken, oauthUser, resolveIp(httpRequest));
        return ResponseEntity.ok(summary);
    }

    @DeleteMapping
    public ResponseEntity<VoteSummaryDto> retractVote(
            @PathVariable Integer tmdbId,
            @RequestHeader(value = "X-Anonymous-Token", required = false) String anonymousToken,
            @AuthenticationPrincipal OAuth2User oauthUser) {

        VoteSummaryDto summary = voteService.retractVote(tmdbId, anonymousToken, oauthUser);
        return ResponseEntity.ok(summary);
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
