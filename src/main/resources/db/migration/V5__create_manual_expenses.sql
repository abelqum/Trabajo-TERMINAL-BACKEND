CREATE TABLE manual_expenses (
                                 id BIGSERIAL PRIMARY KEY,

                                 concept VARCHAR(150) NOT NULL,

                                 category VARCHAR(30) NOT NULL,

                                 amount NUMERIC(14, 2) NOT NULL,

                                 expense_date DATE NOT NULL,

                                 description VARCHAR(500),

                                 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT chk_manual_expenses_category
                                     CHECK (
                                         category IN (
                                                      'OPERATION',
                                                      'SERVICES',
                                                      'RENT',
                                                      'PAYROLL',
                                                      'PURCHASES',
                                                      'MAINTENANCE',
                                                      'TAXES',
                                                      'ADVERTISING',
                                                      'TRANSPORTATION',
                                                      'OTHER'
                                             )
                                         ),

                                 CONSTRAINT chk_manual_expenses_amount
                                     CHECK (
                                         amount > 0
                                         )
);


CREATE INDEX idx_manual_expenses_date
    ON manual_expenses(expense_date);


CREATE INDEX idx_manual_expenses_category
    ON manual_expenses(category);