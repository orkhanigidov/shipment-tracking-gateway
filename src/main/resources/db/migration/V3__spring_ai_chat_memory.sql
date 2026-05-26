CREATE TABLE IF NOT EXISTS spring_ai_chat_memory (
    id              BIGSERIAL    PRIMARY KEY,
    conversation_id VARCHAR(255)  NOT NULL,
    content         TEXT  NOT NULL,
    type            VARCHAR(50)  NOT NULL,
    "timestamp"     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chat_memory_conversation_id
    ON spring_ai_chat_memory (conversation_id);