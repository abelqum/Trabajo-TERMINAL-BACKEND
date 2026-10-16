CREATE TABLE usuarios (
                          id BIGSERIAL PRIMARY KEY,

                          nombre VARCHAR(150) NOT NULL,

                          email VARCHAR(200) NOT NULL,

                          password_hash VARCHAR(255) NOT NULL,

                          rol VARCHAR(20) NOT NULL,

                          creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          actualizado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT chk_usuarios_rol
                              CHECK (rol IN ('admin', 'editor', 'empleado'))
);

CREATE UNIQUE INDEX uq_usuarios_email_lower
    ON usuarios (LOWER(email));