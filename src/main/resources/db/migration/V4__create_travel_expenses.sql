CREATE TABLE travels (
                         id BIGSERIAL PRIMARY KEY,

                         name VARCHAR(150) NOT NULL,

                         destination VARCHAR(150) NOT NULL,

                         start_date DATE NOT NULL,

                         end_date DATE NOT NULL,

                         status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

                         budget_food NUMERIC(14, 2) NOT NULL DEFAULT 0,

                         budget_gasoline NUMERIC(14, 2) NOT NULL DEFAULT 0,

                         budget_tolls NUMERIC(14, 2) NOT NULL DEFAULT 0,

                         budget_lodging NUMERIC(14, 2) NOT NULL DEFAULT 0,

                         budget_material NUMERIC(14, 2) NOT NULL DEFAULT 0,

                         budget_other NUMERIC(14, 2) NOT NULL DEFAULT 0,

                         created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT chk_travels_dates
                             CHECK (end_date >= start_date),

                         CONSTRAINT chk_travels_status
                             CHECK (
                                 status IN ('ACTIVE', 'FINISHED')
                                 ),

                         CONSTRAINT chk_travels_budget_food
                             CHECK (budget_food >= 0),

                         CONSTRAINT chk_travels_budget_gasoline
                             CHECK (budget_gasoline >= 0),

                         CONSTRAINT chk_travels_budget_tolls
                             CHECK (budget_tolls >= 0),

                         CONSTRAINT chk_travels_budget_lodging
                             CHECK (budget_lodging >= 0),

                         CONSTRAINT chk_travels_budget_material
                             CHECK (budget_material >= 0),

                         CONSTRAINT chk_travels_budget_other
                             CHECK (budget_other >= 0)
);


CREATE TABLE travel_users (
                              travel_id BIGINT NOT NULL,

                              user_id BIGINT NOT NULL,

                              CONSTRAINT pk_travel_users
                                  PRIMARY KEY (travel_id, user_id),

                              CONSTRAINT fk_travel_users_travel
                                  FOREIGN KEY (travel_id)
                                      REFERENCES travels(id)
                                      ON DELETE CASCADE,

                              CONSTRAINT fk_travel_users_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES usuarios(id)
                                      ON DELETE RESTRICT
);


CREATE TABLE travel_expenses (
                                 id BIGSERIAL PRIMARY KEY,

                                 travel_id BIGINT NOT NULL,

                                 category VARCHAR(30) NOT NULL,

                                 description VARCHAR(300) NOT NULL,

                                 amount NUMERIC(14, 2) NOT NULL,

                                 expense_date DATE NOT NULL,

                                 status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

                                 rejection_reason VARCHAR(500),

                                 receipt_file_name VARCHAR(255),

                                 created_by_user_id BIGINT NOT NULL,

                                 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_travel_expenses_travel
                                     FOREIGN KEY (travel_id)
                                         REFERENCES travels(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_travel_expenses_created_by
                                     FOREIGN KEY (created_by_user_id)
                                         REFERENCES usuarios(id)
                                         ON DELETE RESTRICT,

                                 CONSTRAINT chk_travel_expenses_category
                                     CHECK (
                                         category IN (
                                                      'FOOD',
                                                      'GASOLINE',
                                                      'TOLLS',
                                                      'LODGING',
                                                      'MATERIAL',
                                                      'OTHER'
                                             )
                                         ),

                                 CONSTRAINT chk_travel_expenses_status
                                     CHECK (
                                         status IN (
                                                    'PENDING',
                                                    'APPROVED',
                                                    'REJECTED'
                                             )
                                         ),

                                 CONSTRAINT chk_travel_expenses_amount
                                     CHECK (amount > 0),

                                 CONSTRAINT chk_travel_expenses_rejection
                                     CHECK (
                                         (
                                             status = 'REJECTED'
                                                 AND rejection_reason IS NOT NULL
                                                 AND BTRIM(rejection_reason) <> ''
                                             )
                                             OR
                                         (
                                             status <> 'REJECTED'
                                                 AND rejection_reason IS NULL
                                             )
                                         )
);


CREATE INDEX idx_travels_start_date
    ON travels(start_date);


CREATE INDEX idx_travels_status
    ON travels(status);


CREATE INDEX idx_travel_users_user
    ON travel_users(user_id);


CREATE INDEX idx_travel_expenses_travel
    ON travel_expenses(travel_id);


CREATE INDEX idx_travel_expenses_date
    ON travel_expenses(expense_date);


CREATE INDEX idx_travel_expenses_status
    ON travel_expenses(status);