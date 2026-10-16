CREATE TABLE tasks (
                       id BIGSERIAL PRIMARY KEY,

                       title VARCHAR(150) NOT NULL,

                       description VARCHAR(1000),

                       due_at TIMESTAMPTZ,

                       priority VARCHAR(40) NOT NULL,

                       status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

                       creator_user_id BIGINT NOT NULL,

                       feedback VARCHAR(1000),

                       reviewed_at TIMESTAMPTZ,

                       completed_at TIMESTAMPTZ,

                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_tasks_creator
                           FOREIGN KEY (creator_user_id)
                               REFERENCES usuarios(id)
                               ON DELETE RESTRICT,

                       CONSTRAINT chk_tasks_priority
                           CHECK (
                               priority IN (
                                            'URGENT_IMPORTANT',
                                            'IMPORTANT_NOT_URGENT',
                                            'URGENT_NOT_IMPORTANT',
                                            'NOT_URGENT_NOT_IMPORTANT'
                                   )
                               ),

                       CONSTRAINT chk_tasks_status
                           CHECK (
                               status IN (
                                          'PENDING',
                                          'IN_PROGRESS',
                                          'REVIEW',
                                          'COMPLETED'
                                   )
                               )
);


CREATE TABLE task_assignees (
                                task_id BIGINT NOT NULL,

                                user_id BIGINT NOT NULL,

                                CONSTRAINT pk_task_assignees
                                    PRIMARY KEY (task_id, user_id),

                                CONSTRAINT fk_task_assignees_task
                                    FOREIGN KEY (task_id)
                                        REFERENCES tasks(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT fk_task_assignees_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES usuarios(id)
                                        ON DELETE RESTRICT
);


CREATE TABLE task_evidence (
                               id BIGSERIAL PRIMARY KEY,

                               task_id BIGINT NOT NULL,

                               author_user_id BIGINT NOT NULL,

                               file_name VARCHAR(255) NOT NULL,

                               original_name VARCHAR(255) NOT NULL,

                               mime_type VARCHAR(100) NOT NULL,

                               size_bytes BIGINT NOT NULL,

                               created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_task_evidence_task
                                   FOREIGN KEY (task_id)
                                       REFERENCES tasks(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_task_evidence_author
                                   FOREIGN KEY (author_user_id)
                                       REFERENCES usuarios(id)
                                       ON DELETE RESTRICT,

                               CONSTRAINT chk_task_evidence_size
                                   CHECK (size_bytes >= 0)
);


CREATE INDEX idx_tasks_creator
    ON tasks(creator_user_id);

CREATE INDEX idx_tasks_status
    ON tasks(status);

CREATE INDEX idx_tasks_priority
    ON tasks(priority);

CREATE INDEX idx_tasks_due_at
    ON tasks(due_at);

CREATE INDEX idx_task_assignees_user
    ON task_assignees(user_id);

CREATE INDEX idx_task_evidence_task
    ON task_evidence(task_id);