# pcs-checker-api

Post-credit scene voting API. Lets users search movies (via TMDB), vote on whether a movie has a post-credit scene (PCS), and see the aggregated verdict.

## Tech stack

- Java 21, Spring Boot 4
- PostgreSQL (data) + Flyway (migrations)
- Redis (caching + session store)
- Spring Security with Google OAuth2 login
- TMDB API (movie search/metadata)

## Prerequisites

- JDK 21
- Docker (for Postgres and Redis)
- A [TMDB API key](https://www.themoviedb.org/settings/api)
- A Google OAuth2 client ID/secret (only needed if you're testing login; the app runs fine without it otherwise)

## Running locally

1. **Start dependencies** (Postgres on `5432`, Redis on `6379`):

   ```bash
   docker-compose up -d
   ```

2. **Configure local secrets.** `application-local.properties` is git-ignored, so create it yourself under `src/main/resources/`:

   ```properties
   tmdb.api.key=YOUR_TMDB_API_KEY

   spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
   spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
   ```

   Without this file the app still starts, using the `changeme` placeholders from `application.properties` — movie search/details and OAuth login just won't work until real credentials are supplied.

3. **Run the app** with the `local` profile active:

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

   The API is served at `http://localhost:8080`. Flyway migrations run automatically on startup.

Alternatively, skip step 2 and pass credentials as environment variables against the default profile:

```bash
TMDB_API_KEY=xxx GOOGLE_CLIENT_ID=xxx GOOGLE_CLIENT_SECRET=xxx ./mvnw spring-boot:run
```

## Configuration reference

| Property / env var | Default | Purpose |
|---|---|---|
| `spring.datasource.url` / `.username` / `.password` | `jdbc:postgresql://localhost:5432/pcschecker` / `postgres` / `secret` | Postgres connection, matches `docker-compose.yml` |
| `spring.data.redis.host` / `.port` | `localhost` / `6379` | Redis connection |
| `TMDB_API_KEY` | `changeme` | TMDB API key for movie search/details |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | `changeme` | Google OAuth2 login credentials |
| `app.frontend-url` | `http://localhost:5173` | Allowed CORS origin and OAuth2 redirect target |

The frontend is expected to run separately at `http://localhost:5173`.

## API overview

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/movies/search?q=` | public | Search movies via TMDB, annotated with current vote-based verdict |
| `GET` | `/api/movies/{tmdbId}` | public | Movie details, vote summary and verdict |
| `POST` | `/api/movies/{tmdbId}/votes` | public | Cast a vote (`{"hasPcs": true/false}`); identified by session (logged-in) or `X-Anonymous-Token` header |
| `DELETE` | `/api/movies/{tmdbId}/votes` | public | Retract a vote |
| `GET` | `/api/auth/me` | public | Current authenticated user, or `null` |
| `POST` | `/api/auth/logout` | public | Log out |
| `GET` | `/oauth2/authorization/google` | public | Start Google OAuth2 login (Spring Security default endpoint) |

Anonymous voting is tracked via an `X-Anonymous-Token` request header (client-generated); authenticated users vote via their OAuth2 session instead.

## Running tests

```bash
./mvnw test
```

There's no test-specific configuration or embedded/testcontainers setup yet, so the test suite boots the full Spring context against the same Postgres/Redis from `docker-compose.yml` — make sure those are running first (step 1 above).
