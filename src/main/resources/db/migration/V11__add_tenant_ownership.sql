-- =========================================================
-- V11
-- OWNERSHIP DE DATOS POR TENANT
-- =========================================================
--
-- En este punto solamente existe el tenant inicial creado
-- por V10.
--
-- Todos los datos históricos pertenecen a ese tenant.
--
-- Las tablas dependientes obtienen su tenant desde su
-- entidad padre cuando es posible.
-- =========================================================


-- =========================================================
-- CLIENTS
-- =========================================================

ALTER TABLE clients
    ADD COLUMN tenant_id BIGINT;


UPDATE clients
SET tenant_id = (
    SELECT MIN(id)
    FROM tenants
);


ALTER TABLE clients
    ALTER COLUMN tenant_id SET NOT NULL;


ALTER TABLE clients
    ADD CONSTRAINT fk_clients_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants(id)
            ON DELETE RESTRICT;


CREATE INDEX idx_clients_tenant_id
    ON clients(tenant_id);


-- El RFC deja de ser globalmente único.
-- Debe ser único solamente dentro de una empresa.

DROP INDEX uq_clients_rfc_upper;


CREATE UNIQUE INDEX uq_clients_rfc_upper
    ON clients (
                tenant_id,
                UPPER(rfc)
        )
    WHERE rfc IS NOT NULL
      AND BTRIM(rfc) <> '';


-- =========================================================
-- INCOMES
-- =========================================================

ALTER TABLE incomes
    ADD COLUMN tenant_id BIGINT;


UPDATE incomes income
SET tenant_id = client.tenant_id
    FROM clients client
WHERE client.id = income.client_id;


ALTER TABLE incomes
    ALTER COLUMN tenant_id SET NOT NULL;


ALTER TABLE incomes
    ADD CONSTRAINT fk_incomes_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants(id)
            ON DELETE RESTRICT;


CREATE INDEX idx_incomes_tenant_id
    ON incomes(tenant_id);


-- =========================================================
-- TRAVELS
-- =========================================================

ALTER TABLE travels
    ADD COLUMN tenant_id BIGINT;


UPDATE travels
SET tenant_id = (
    SELECT MIN(id)
    FROM tenants
);


ALTER TABLE travels
    ALTER COLUMN tenant_id SET NOT NULL;


ALTER TABLE travels
    ADD CONSTRAINT fk_travels_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants(id)
            ON DELETE RESTRICT;


CREATE INDEX idx_travels_tenant_id
    ON travels(tenant_id);


-- =========================================================
-- TRAVEL EXPENSES
-- =========================================================

ALTER TABLE travel_expenses
    ADD COLUMN tenant_id BIGINT;


UPDATE travel_expenses expense
SET tenant_id = travel.tenant_id
    FROM travels travel
WHERE travel.id = expense.travel_id;


ALTER TABLE travel_expenses
    ALTER COLUMN tenant_id SET NOT NULL;


ALTER TABLE travel_expenses
    ADD CONSTRAINT fk_travel_expenses_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants(id)
            ON DELETE RESTRICT;


CREATE INDEX idx_travel_expenses_tenant_id
    ON travel_expenses(tenant_id);


-- =========================================================
-- MANUAL EXPENSES
-- =========================================================

ALTER TABLE manual_expenses
    ADD COLUMN tenant_id BIGINT;


UPDATE manual_expenses
SET tenant_id = (
    SELECT MIN(id)
    FROM tenants
);


ALTER TABLE manual_expenses
    ALTER COLUMN tenant_id SET NOT NULL;


ALTER TABLE manual_expenses
    ADD CONSTRAINT fk_manual_expenses_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants(id)
            ON DELETE RESTRICT;


CREATE INDEX idx_manual_expenses_tenant_id
    ON manual_expenses(tenant_id);


-- =========================================================
-- TASKS
-- =========================================================

ALTER TABLE tasks
    ADD COLUMN tenant_id BIGINT;


UPDATE tasks task
SET tenant_id = user_account.tenant_id
    FROM usuarios user_account
WHERE user_account.id = task.creator_user_id;


ALTER TABLE tasks
    ALTER COLUMN tenant_id SET NOT NULL;


ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants(id)
            ON DELETE RESTRICT;


CREATE INDEX idx_tasks_tenant_id
    ON tasks(tenant_id);


-- =========================================================
-- TASK EVIDENCE
-- =========================================================

ALTER TABLE task_evidence
    ADD COLUMN tenant_id BIGINT;


UPDATE task_evidence evidence
SET tenant_id = task.tenant_id
    FROM tasks task
WHERE task.id = evidence.task_id;


ALTER TABLE task_evidence
    ALTER COLUMN tenant_id SET NOT NULL;


ALTER TABLE task_evidence
    ADD CONSTRAINT fk_task_evidence_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants(id)
            ON DELETE RESTRICT;


CREATE INDEX idx_task_evidence_tenant_id
    ON task_evidence(tenant_id);


-- =========================================================
-- INVENTORY CATALOGS
-- =========================================================

ALTER TABLE inventory_catalog_items
    ADD COLUMN tenant_id BIGINT;


UPDATE inventory_catalog_items
SET tenant_id = (
    SELECT MIN(id)
    FROM tenants
);


ALTER TABLE inventory_catalog_items
    ALTER COLUMN tenant_id SET NOT NULL;


ALTER TABLE inventory_catalog_items
    ADD CONSTRAINT fk_inventory_catalog_items_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants(id)
            ON DELETE RESTRICT;


CREATE INDEX idx_inventory_catalog_items_tenant_id
    ON inventory_catalog_items(tenant_id);


-- Los nombres de los catálogos deben ser únicos
-- únicamente dentro de cada tenant.

DROP INDEX uq_inventory_catalog_items_type_name_lower;


CREATE UNIQUE INDEX uq_inventory_catalog_items_type_name_lower
    ON inventory_catalog_items (
                                tenant_id,
                                catalog_type,
                                LOWER(name)
        );


-- =========================================================
-- INVENTORY PRODUCTS
-- =========================================================

ALTER TABLE inventory_products
    ADD COLUMN tenant_id BIGINT;


UPDATE inventory_products
SET tenant_id = (
    SELECT MIN(id)
    FROM tenants
);


ALTER TABLE inventory_products
    ALTER COLUMN tenant_id SET NOT NULL;


ALTER TABLE inventory_products
    ADD CONSTRAINT fk_inventory_products_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants(id)
            ON DELETE RESTRICT;


CREATE INDEX idx_inventory_products_tenant_id
    ON inventory_products(tenant_id);


-- =========================================================
-- INVENTORY STOCK MOVEMENTS
-- =========================================================

ALTER TABLE inventory_stock_movements
    ADD COLUMN tenant_id BIGINT;


UPDATE inventory_stock_movements movement
SET tenant_id = product.tenant_id
    FROM inventory_products product
WHERE product.id = movement.product_id;


ALTER TABLE inventory_stock_movements
    ALTER COLUMN tenant_id SET NOT NULL;


ALTER TABLE inventory_stock_movements
    ADD CONSTRAINT fk_inventory_stock_movements_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants(id)
            ON DELETE RESTRICT;


CREATE INDEX idx_inventory_stock_movements_tenant_id
    ON inventory_stock_movements(tenant_id);


-- =========================================================
-- INVENTORY KIT COMPONENTS
-- =========================================================

ALTER TABLE inventory_kit_components
    ADD COLUMN tenant_id BIGINT;


UPDATE inventory_kit_components component
SET tenant_id = kit.tenant_id
    FROM inventory_products kit
WHERE kit.id = component.kit_product_id;


ALTER TABLE inventory_kit_components
    ALTER COLUMN tenant_id SET NOT NULL;


ALTER TABLE inventory_kit_components
    ADD CONSTRAINT fk_inventory_kit_components_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants(id)
            ON DELETE RESTRICT;


CREATE INDEX idx_inventory_kit_components_tenant_id
    ON inventory_kit_components(tenant_id);