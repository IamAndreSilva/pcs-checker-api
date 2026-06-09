package nl.andresilva.pcschecker.votes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, UUID> {

    Optional<Vote> findByTmdbMovieIdAndAnonymousToken(Integer tmdbMovieId, String anonymousToken);

    Optional<Vote> findByTmdbMovieIdAndUserId(Integer tmdbMovieId, UUID userId);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.tmdbMovieId = :movieId AND v.hasPcs = true")
    long countYesByMovieId(@Param("movieId") Integer movieId);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.tmdbMovieId = :movieId AND v.hasPcs = false")
    long countNoByMovieId(@Param("movieId") Integer movieId);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.ipAddress = :ip AND v.createdAt > :since")
    long countByIpAddressSince(@Param("ip") String ipAddress, @Param("since") Instant since);

    @Query("SELECT v.tmdbMovieId, SUM(CASE WHEN v.hasPcs = true THEN 1 ELSE 0 END), SUM(CASE WHEN v.hasPcs = false THEN 1 ELSE 0 END) FROM Vote v WHERE v.tmdbMovieId IN :ids GROUP BY v.tmdbMovieId")
    List<Object[]> countVotesByMovieIds(@Param("ids") List<Integer> tmdbMovieIds);
}
