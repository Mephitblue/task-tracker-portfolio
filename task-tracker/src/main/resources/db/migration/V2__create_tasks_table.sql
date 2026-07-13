CREATE TABLE tasks
(
    id          BIGSERIAL    PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    priority    VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM',
    user_id     BIGINT       NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT chk_status
        CHECK (status IN ('OPEN', 'IN_PROGRESS', 'DONE')),
    CONSTRAINT chk_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'))
);

CREATE INDEX idx_tasks_user_id ON tasks (user_id);
CREATE INDEX idx_tasks_status  ON tasks (status);
CREATE INDEX idx_tasks_priority ON tasks (priority);