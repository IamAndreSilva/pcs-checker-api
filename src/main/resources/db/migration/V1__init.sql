CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email      VARCHAR(255) UNIQUE NOT NULL,
    name       VARCHAR(255),
    avatar_url VARCHAR(500),
    oauth_id   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE votes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tmdb_movie_id   INTEGER NOT NULL,
    has_pcs         BOOLEAN NOT NULL,
    anonymous_token VARCHAR(36),
    user_id         UUID REFERENCES users(id),
    ip_address      VARCHAR(45),
    created_at      TIMESTAMP DEFAULT now(),
    CONSTRAINT uq_vote_anonymous UNIQUE (tmdb_movie_id, anonymous_token),
    CONSTRAINT uq_vote_user      UNIQUE (tmdb_movie_id, user_id),
    CONSTRAINT chk_voter CHECK (
        (anonymous_token IS NOT NULL AND user_id IS NULL) OR
        (anonymous_token IS NULL AND user_id IS NOT NULL)
    )
);

CREATE INDEX idx_votes_tmdb_movie_id ON votes (tmdb_movie_id);
CREATE INDEX idx_votes_ip_created    ON votes (ip_address, created_at);
