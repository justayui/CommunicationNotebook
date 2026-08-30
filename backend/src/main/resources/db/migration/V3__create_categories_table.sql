CREATE TABLE categories (
    id   SERIAL      PRIMARY KEY,
    name VARCHAR(50)  NOT NULL,
    CONSTRAINT uk_categories_name UNIQUE (name)
);

INSERT INTO categories (name) VALUES
    ('手順変更'),
    ('委員会'),
    ('勉強会'),
    ('その他');
