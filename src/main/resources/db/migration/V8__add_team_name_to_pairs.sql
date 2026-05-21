ALTER TABLE tournament_pairs ADD COLUMN IF NOT EXISTS team_name TEXT NOT NULL DEFAULT '';
CREATE INDEX IF NOT EXISTS idx_tournament_pairs_player1 ON tournament_pairs (player1_id);
CREATE INDEX IF NOT EXISTS idx_tournament_pairs_player2 ON tournament_pairs (player2_id);
