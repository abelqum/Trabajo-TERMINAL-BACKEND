CREATE TABLE tenants (
                         id BIGSERIAL PRIMARY KEY,

                         name VARCHAR(200) NOT NULL,

                         status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

                         created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT chk_tenants_name
                             CHECK (BTRIM(name) <> ''),

                         CONSTRAINT chk_tenants_status
                             CHECK (
                                 status IN (
                                            'ACTIVE',
                                            'SUSPENDED'
                                     )
                                 )
);


CREATE INDEX idx_tenants_status
    ON tenants(status);


CREATE INDEX idx_tenants_name_lower
    ON tenants(LOWER(name));


-- =========================================================
-- TENANT INICIAL
-- =========================================================
--
-- Todos los datos existentes pertenecen actualmente
-- a una sola empresa.
--
-- Creamos un tenant inicial para preservar esos datos.
--

INSERT INTO tenants (
    name,
    status
)
VALUES (
           'Empresa inicial',
           'ACTIVE'
       );


-- =========================================================
-- USERS -> TENANT
-- =========================================================

ALTER TABLE usuarios
    ADD COLUMN tenant_id BIGINT;


UPDATE usuarios
SET tenant_id = (
    SELECT id
    FROM tenants
    ORDER BY id
    LIMIT 1
    );


ALTER TABLE usuarios
    ALTER COLUMN tenant_id SET NOT NULL;


ALTER TABLE usuarios
    ADD CONSTRAINT fk_usuarios_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants(id)
            ON DELETE RESTRICT;


CREATE INDEX idx_usuarios_tenant_id
    ON usuarios(tenant_id);


-- Este UNIQUE será útil después para crear
-- foreign keys compuestas tenant + entidad.
ALTER TABLE usuarios
    ADD CONSTRAINT uq_usuarios_tenant_id_id
        UNIQUE (tenant_id, id);