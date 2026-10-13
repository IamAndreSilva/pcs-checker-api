package nl.andresilva.pcschecker;

import nl.andresilva.pcschecker.auth.User;
import nl.andresilva.pcschecker.auth.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountDeletionEndpointTests {

    private static final String ACCOUNT_URL = "/api/auth/me";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void anonymousCallerIsRejected() throws Exception {
        mockMvc.perform(delete(ACCOUNT_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signedInUserCanDeleteOwnAccount() throws Exception {
        String oauthId = "test-" + UUID.randomUUID();
        userRepository.saveAndFlush(new User(oauthId + "@example.test", "Test User", null, oauthId));

        mockMvc.perform(delete(ACCOUNT_URL)
                        .with(oauth2Login().attributes(attributes -> attributes.put("sub", oauthId))))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findByOauthId(oauthId)).isEmpty();
    }
}
