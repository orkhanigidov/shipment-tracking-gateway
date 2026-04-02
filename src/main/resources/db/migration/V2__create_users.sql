CREATE TABLE IF NOT EXISTS users (
    id       BIGSERIAL    PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    api_key  VARCHAR(100) NOT NULL
);

INSERT INTO users (username, api_key) VALUES ('alice', 'key-alice-001');
INSERT INTO users (username, api_key) VALUES ('bob', 'key-bob-002');