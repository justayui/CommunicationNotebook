CREATE TABLE note_reads (
    id         SERIAL PRIMARY KEY,
    note_id    INTEGER   NOT NULL REFERENCES notes(id),
    user_id    INTEGER   NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_note_reads_note_id_user_id UNIQUE (note_id, user_id)
);
