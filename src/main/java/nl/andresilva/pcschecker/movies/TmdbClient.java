package nl.andresilva.pcschecker.movies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class TmdbClient {

    private static final Logger log = LoggerFactory.getLogger(TmdbClient.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String imageBaseUrl;

    public TmdbClient(
            @Value("${tmdb.api.key}") String apiKey,
            @Value("${tmdb.image.base-url}") String imageBaseUrl) {
        this.apiKey = apiKey;
        this.imageBaseUrl = imageBaseUrl;
        log.info("TMDB API key: {}", apiKey);
        this.restClient = RestClient.builder()
                .baseUrl("https://api.themoviedb.org/3")
                .build();
    }

    @Cacheable("tmdb-search")
    public List<TmdbMovieResult> search(String query) {
        TmdbMovieResult.SearchResponse response = restClient.get()
                .uri("/search/movie?query={q}&api_key={k}&language=en-US&page=1", query, apiKey)
                .retrieve()
                .body(TmdbMovieResult.SearchResponse.class);
        return response != null ? response.results() : List.of();
    }

    @Cacheable("tmdb-movie")
    public TmdbMovieResult getMovie(int tmdbId) {
        return restClient.get()
                .uri("/movie/{id}?api_key={k}&language=en-US", tmdbId, apiKey)
                .retrieve()
                .body(TmdbMovieResult.class);
    }

    public String posterUrl(String posterPath) {
        if (posterPath == null) return null;
        return imageBaseUrl + posterPath;
    }
}
