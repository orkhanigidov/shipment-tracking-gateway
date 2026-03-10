CREATE TABLE IF NOT EXISTS shipments (
    id                 BIGSERIAL     PRIMARY KEY,
    tracking_number    VARCHAR(50)   NOT NULL UNIQUE,
    carrier            VARCHAR(20)   NOT NULL,
    status             VARCHAR(30)   NOT NULL,
    origin             VARCHAR(100),
    destination        VARCHAR(100),
    estimated_delivery DATE,
    created_at         TIMESTAMP     NOT NULL
);