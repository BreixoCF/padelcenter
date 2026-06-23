CREATE TABLE tournaments (
    tournament_id  UUID        NOT NULL DEFAULT gen_random_uuid(),
    center_id      UUID        NOT NULL,
    name           TEXT        NOT NULL,
    description    TEXT,
    format         TEXT        NOT NULL
        CHECK (format IN ('ROUND_ROBIN', 'ELIMINATION', 'GROUPS_AND_ELIMINATION')),
    status         TEXT        NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT', 'REGISTRATION_OPEN', 'REGISTRATION_CLOSED',
                          'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    max_pairs      INTEGER     NOT NULL CHECK (max_pairs >= 4),
    start_date     DATE        NOT NULL,
    end_date       DATE        NOT NULL,
    created_by     UUID,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by    UUID,
    modified_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_by     UUID,
    deleted_at     TIMESTAMPTZ,

    CONSTRAINT pk_tournaments PRIMARY KEY (tournament_id),
    CONSTRAINT fk_tournaments_center
        FOREIGN KEY (center_id) REFERENCES centers (center_id)
);

CREATE TABLE tournament_pairs (
    pair_id        UUID        NOT NULL DEFAULT gen_random_uuid(),
    tournament_id  UUID        NOT NULL,
    player1_id     UUID        NOT NULL,
    player2_id     UUID        NOT NULL,
    status         TEXT        NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED')),
    registered_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT pk_tournament_pairs PRIMARY KEY (pair_id),
    CONSTRAINT fk_pairs_tournament
        FOREIGN KEY (tournament_id) REFERENCES tournaments (tournament_id),
    CONSTRAINT uq_pair_players
        UNIQUE (tournament_id, player1_id, player2_id)
);

CREATE TABLE tournament_matches (
    match_id       UUID        NOT NULL DEFAULT gen_random_uuid(),
    tournament_id  UUID        NOT NULL,
    pair_a_id      UUID,
    pair_b_id      UUID,
    round          INTEGER     NOT NULL,
    group_name     TEXT,
    status         TEXT        NOT NULL DEFAULT 'SCHEDULED'
        CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    scheduled_at   TIMESTAMPTZ,
    winner_pair_id UUID,
    score_a        INTEGER,
    score_b        INTEGER,
    reported_by    UUID,
    reported_at    TIMESTAMPTZ,
    created_by     UUID,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by    UUID,
    modified_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_by     UUID,
    deleted_at     TIMESTAMPTZ,

    CONSTRAINT pk_tournament_matches PRIMARY KEY (match_id),
    CONSTRAINT fk_matches_tournament
        FOREIGN KEY (tournament_id) REFERENCES tournaments (tournament_id)
);

CREATE INDEX idx_tournaments_center_id
    ON tournaments (center_id);
CREATE INDEX idx_tournament_pairs_tournament_id
    ON tournament_pairs (tournament_id);
CREATE INDEX idx_tournament_pairs_player1
    ON tournament_pairs (player1_id);
CREATE INDEX idx_tournament_pairs_player2
    ON tournament_pairs (player2_id);
CREATE INDEX idx_tournament_matches_tournament_id
    ON tournament_matches (tournament_id);
CREATE INDEX idx_tournament_matches_round
    ON tournament_matches (tournament_id, round);

COMMENT ON TABLE tournaments IS 'Padel tournaments organized by centers';
COMMENT ON TABLE tournament_pairs IS 'Registered pairs for a tournament';
COMMENT ON TABLE tournament_matches IS 'Matches generated for a tournament';
