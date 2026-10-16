CREATE TABLE incomes (
                         id BIGSERIAL PRIMARY KEY,

                         client_id BIGINT NOT NULL,

                         record_type VARCHAR(20) NOT NULL,

                         concept VARCHAR(300) NOT NULL,

                         subtotal NUMERIC(14, 2) NOT NULL,

                         tax_applied BOOLEAN NOT NULL DEFAULT FALSE,

                         tax_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,

                         total NUMERIC(14, 2) NOT NULL,

                         status VARCHAR(20) NOT NULL,

                         record_date DATE NOT NULL,

                         payment_date DATE,

                         payment_method VARCHAR(80),

                         created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_incomes_client
                             FOREIGN KEY (client_id)
                                 REFERENCES clients(id)
                                 ON DELETE RESTRICT,

                         CONSTRAINT chk_incomes_record_type
                             CHECK (
                                 record_type IN ('INVOICE', 'INCOME')
                                 ),

                         CONSTRAINT chk_incomes_status
                             CHECK (
                                 status IN ('PENDING', 'PAID', 'CANCELED')
                                 ),

                         CONSTRAINT chk_incomes_positive_subtotal
                             CHECK (
                                 subtotal > 0
                                 ),

                         CONSTRAINT chk_incomes_non_negative_tax
                             CHECK (
                                 tax_amount >= 0
                                 ),

                         CONSTRAINT chk_incomes_total
                             CHECK (
                                 total = subtotal + tax_amount
                                 ),

                         CONSTRAINT chk_incomes_income_paid
                             CHECK (
                                 record_type <> 'INCOME'
                                     OR status = 'PAID'
                                 ),

                         CONSTRAINT chk_incomes_paid_information
                             CHECK (
                                 status <> 'PAID'
                                     OR (
                                     payment_date IS NOT NULL
                                         AND payment_method IS NOT NULL
                                         AND BTRIM(payment_method) <> ''
                                     )
                                 )
);


CREATE INDEX idx_incomes_client_id
    ON incomes(client_id);


CREATE INDEX idx_incomes_record_date
    ON incomes(record_date);


CREATE INDEX idx_incomes_payment_date
    ON incomes(payment_date);


CREATE INDEX idx_incomes_status
    ON incomes(status);


CREATE INDEX idx_incomes_record_type
    ON incomes(record_type);