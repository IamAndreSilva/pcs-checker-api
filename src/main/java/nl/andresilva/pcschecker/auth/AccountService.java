package nl.andresilva.pcschecker.auth;

import nl.andresilva.pcschecker.votes.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final VoteRepository voteRepository;

    public AccountService(UserRepository userRepository, VoteRepository voteRepository) {
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
    }

    /**
     * Permanently removes the user and every vote cast while signed in.
     */
    @Transactional
    public void deleteAccount(String oauthId) {
        userRepository.findByOauthId(oauthId).ifPresent(user -> {
            voteRepository.deleteByUserId(user.getId());
            userRepository.delete(user);
        });
    }
}
