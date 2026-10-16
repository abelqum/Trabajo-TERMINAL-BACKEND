CREATE TABLE clients (
                         id BIGSERIAL PRIMARY KEY,

                         business_name VARCHAR(200) NOT NULL,

                         rfc VARCHAR(13),

                         created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE UNIQUE INDEX uq_clients_rfc_upper
    ON clients (UPPER(rfc))
    WHERE rfc IS NOT NULL
      AND BTRIM(rfc) <> '';


CREATE INDEX idx_clients_business_name_lower
    ON clients (LOWER(business_name));


CREATE TABLE client_phones (
                               client_id BIGINT NOT NULL,

                               phone VARCHAR(30) NOT NULL,

                               CONSTRAINT fk_client_phones_client
                                   FOREIGN KEY (client_id)
                                       REFERENCES clients(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT pk_client_phones
                                   PRIMARY KEY (client_id, phone)
);


CREATE TABLE client_emails (
                               client_id BIGINT NOT NULL,

                               email VARCHAR(200) NOT NULL,

                               CONSTRAINT fk_client_emails_client
                                   FOREIGN KEY (client_id)
                                       REFERENCES clients(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT pk_client_emails
                                   PRIMARY KEY (client_id, email)
);