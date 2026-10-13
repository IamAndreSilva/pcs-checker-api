package nl.andresilva.pcschecker;

import jakarta.persistence.EntityManager;
import nl.andresilva.pcschecker.auth.AccountService;
import nl.andresilva.pcschecker.auth.User;
import nl.andresilva.pcschecker.auth.UserRepository;
import nl.andresilva.pcschecker.votes.Vote;
import nl.andresilva.pcschecker.votes.VoteIpCleanupJob;
import nl.andresilva.pcschecker.votes.VoteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DataRetentionTests {

    private static final int MOVIE_ID = 999_001;
    private static final String IP_ADDRESS = "203.0.113.7";
    private static final int DAYS_PAST_RETENTION = 4;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoteIpCleanupJob cleanupJob;

    @Autowired
    private AccountService accountService;

    @Autowired
    private EntityManager entityManager;

    @Test
    void cleanupClearsIpOfOldVotesOnly() {
        Vote oldVote = voteRepository.saveAndFlush(anonymousVote());
        Vote recentVote = voteRepository.saveAndFlush(anonymousVote());
        entityManager.createNativeQuery(
                        "UPDATE votes SET created_at = now() - make_interval(days => :days) WHERE id = :id")
                .setParameter("days", DAYS_PAST_RETENTION)
                .setParameter("id", oldVote.getId())
                .executeUpdate();

        cleanupJob.clearExpiredIpAddresses();
        entityManager.clear();

        assertThat(voteRepository.findById(oldVote.getId()).orElseThrow().getIpAddress()).isNull();
        assertThat(voteRepository.findById(recentVote.getId()).orElseThrow().getIpAddress()).isEqualTo(IP_ADDRESS);
    }

    @Test
    void deletingAccountRemovesUserAndTheirVotesOnly() {
        String oauthId = "test-" + UUID.randomUUID();
        User user = userRepository.saveAndFlush(new User(oauthId + "@example.test", "Test User", null, oauthId));
        Vote userVote = voteRepository.saveAndFlush(new Vote(MOVIE_ID, true, null, user, IP_ADDRESS));
        Vote otherVote = voteRepository.saveAndFlush(anonymousVote());

        accountService.deleteAccount(oauthId);
        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findByOauthId(oauthId)).isEmpty();
        assertThat(voteRepository.findById(userVote.getId())).isEmpty();
        assertThat(voteRepository.findById(otherVote.getId())).isPresent();
    }

    private Vote anonymousVote() {
        return new Vote(MOVIE_ID, true, UUID.randomUUID().toString(), null, IP_ADDRESS);
    }
}
