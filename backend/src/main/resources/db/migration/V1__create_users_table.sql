CREATE TABLE users (
    id          SERIAL PRIMARY KEY,
    employee_id VARCHAR(50)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    is_admin    BOOLEAN      NOT NULL DEFAULT FALSE,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_users_employee_id UNIQUE (employee_id)
);
