CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(127) NOT NULL,
    file_key VARCHAR(500) NOT NULL,
    task_type VARCHAR(63) NOT NULL,
    status VARCHAR(63) NOT NULL,
    priority INT NOT NULL DEFAULT 0,
    result TEXT,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    is_cancel BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_task_type ON tasks(task_type);
CREATE INDEX IF NOT EXISTS idx_tasks_created_at ON tasks(created_at);
CREATE INDEX IF NOT EXISTS idx_tasks_priority_status ON tasks(priority, status);
CREATE INDEX IF NOT EXISTS idx_tasks_pending ON tasks(status, priority, created_at) WHERE status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_tasks_status_created ON tasks(status, created_at);

CREATE TABLE IF NOT EXISTS task_status_history (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    old_status VARCHAR(63),
    new_status VARCHAR(63) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(255) DEFAULT 'SYSTEM',
    metadata JSONB
);

CREATE INDEX IF NOT EXISTS idx_history_task_id ON task_status_history(task_id);
CREATE INDEX IF NOT EXISTS idx_history_changed_at ON task_status_history(changed_at);