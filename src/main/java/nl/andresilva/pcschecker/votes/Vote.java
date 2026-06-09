package nl.andresilva.pcschecker.votes;

import jakarta.persistence.*;
import nl.andresilva.pcschecker.auth.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "votes")
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tmdb_movie_id", nullable = false)
    private Integer tmdbMovieId;

    @Column(name = "has_pcs", nullable = false)
    private Boolean hasPcs;

    @Column(name = "anonymous_token")
    private String anonymousToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    protected Vote() {}

    public Vote(Integer tmdbMovieId, Boolean hasPcs, String anonymousToken, User user, String ipAddress) {
        this.tmdbMovieId = tmdbMovieId;
        this.hasPcs = hasPcs;
        this.anonymousToken = anonymousToken;
        this.user = user;
        this.ipAddress = ipAddress;
    }

    public UUID getId() { return id; }
    public Integer getTmdbMovieId() { return tmdbMovieId; }
    public Boolean getHasPcs() { return hasPcs; }
    public String getAnonymousToken() { return anonymousToken; }
    public User getUser() { return user; }
    public String getIpAddress() { return ipAddress; }
    public Instant getCreatedAt() { return createdAt; }

    public void setHasPcs(Boolean hasPcs) { this.hasPcs = hasPcs; }
}
