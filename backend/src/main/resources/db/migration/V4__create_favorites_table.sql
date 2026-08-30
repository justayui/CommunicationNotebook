CREATE TABLE favorites (
    id         SERIAL PRIMARY KEY,
    user_id    INTEGER   NOT NULL REFERENCES users(id),
    note_id    INTEGER   NOT NULL REFERENCES notes(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_favorites_user_id_note_id UNIQUE (user_id, note_id)
);
