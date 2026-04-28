CREATE TABLE IF NOT EXISTS users (
    id       BIGSERIAL    PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    api_key  VARCHAR(60)  NOT NULL,
    tier     VARCHAR(20)  NOT NULL DEFAULT 'FREE'
);

INSERT INTO users (username, api_key, tier)
VALUES ('alice', '$2y$12$KfBs2IeKP6DH4qP2iOZpZutqxQkvHH5FZo/w.6kWp6/X8XkUPN6wi', 'FREE');

INSERT INTO users (username, api_key, tier)
VALUES ('bob', '$2y$12$MRxq53Ty.lbycc08KWP7qOWDw44tdGp5dEoCBaSeqynIPlqJPB/fW', 'PREMIUM');