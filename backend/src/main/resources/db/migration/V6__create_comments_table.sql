CREATE TABLE comments (
    id         SERIAL PRIMARY KEY,
    note_id    INTEGER   NOT NULL REFERENCES notes(id),
    user_id    INTEGER   NOT NULL REFERENCES users(id),
    content    TEXT      NOT NULL,
    is_deleted BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
