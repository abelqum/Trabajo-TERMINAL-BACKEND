-- =========================================================
-- V12
-- MULTI-TENANT REFERENTIAL INTEGRITY
-- =========================================================
--
-- This migration prevents relationships between resources
-- belonging to different tenants.
--
-- Application-level tenant filtering remains necessary.
-- These constraints provide an additional database-level
-- security and integrity boundary.
-- =========================================================


-- =========================================================
-- PRE-FLIGHT VALIDATION
-- =========================================================

DO
$$
BEGIN

    IF EXISTS (
        SELECT 1
        FROM incomes income
        JOIN clients client
            ON client.id = income.client_id
        WHERE income.tenant_id <> client.tenant_id
    ) THEN
        RAISE EXCEPTION
            'Cross-tenant relationship detected between incomes and clients.';
END IF;


    IF EXISTS (
        SELECT 1
        FROM travel_expenses expense
        JOIN travels travel
            ON travel.id = expense.travel_id
        WHERE expense.tenant_id <> travel.tenant_id
    ) THEN
        RAISE EXCEPTION
            'Cross-tenant relationship detected between travel expenses and travels.';
END IF;


    IF EXISTS (
        SELECT 1
        FROM travel_expenses expense
        JOIN usuarios user_account
            ON user_account.id = expense.created_by_user_id
        WHERE expense.tenant_id <> user_account.tenant_id
    ) THEN
        RAISE EXCEPTION
            'Cross-tenant relationship detected between travel expenses and users.';
END IF;


    IF EXISTS (
        SELECT 1
        FROM tasks task
        JOIN usuarios user_account
            ON user_account.id = task.creator_user_id
        WHERE task.tenant_id <> user_account.tenant_id
    ) THEN
        RAISE EXCEPTION
            'Cross-tenant relationship detected between tasks and creators.';
END IF;


    IF EXISTS (
        SELECT 1
        FROM task_evidence evidence
        JOIN tasks task
            ON task.id = evidence.task_id
        WHERE evidence.tenant_id <> task.tenant_id
    ) THEN
        RAISE EXCEPTION
            'Cross-tenant relationship detected between task evidence and tasks.';
END IF;


    IF EXISTS (
        SELECT 1
        FROM task_evidence evidence
        JOIN usuarios user_account
            ON user_account.id = evidence.author_user_id
        WHERE evidence.tenant_id <> user_account.tenant_id
    ) THEN
        RAISE EXCEPTION
            'Cross-tenant relationship detected between task evidence and users.';
END IF;


    IF EXISTS (
        SELECT 1
        FROM inventory_stock_movements movement
        JOIN inventory_products product
            ON product.id = movement.product_id
        WHERE movement.tenant_id <> product.tenant_id
    ) THEN
        RAISE EXCEPTION
            'Cross-tenant relationship detected between stock movements and products.';
END IF;


    IF EXISTS (
        SELECT 1
        FROM inventory_stock_movements movement
        JOIN usuarios user_account
            ON user_account.id = movement.user_id
        WHERE movement.tenant_id <> user_account.tenant_id
    ) THEN
        RAISE EXCEPTION
            'Cross-tenant relationship detected between stock movements and users.';
END IF;


    IF EXISTS (
        SELECT 1
        FROM inventory_kit_components component
        JOIN inventory_products kit
            ON kit.id = component.kit_product_id
        WHERE component.tenant_id <> kit.tenant_id
    ) THEN
        RAISE EXCEPTION
            'Cross-tenant relationship detected between kit definitions and kits.';
END IF;


    IF EXISTS (
        SELECT 1
        FROM inventory_kit_components kit_component
        JOIN inventory_products component
            ON component.id = kit_component.component_product_id
        WHERE kit_component.tenant_id <> component.tenant_id
    ) THEN
        RAISE EXCEPTION
            'Cross-tenant relationship detected between kits and components.';
END IF;


    IF EXISTS (
        SELECT 1
        FROM travel_users relation
        JOIN travels travel
            ON travel.id = relation.travel_id
        JOIN usuarios user_account
            ON user_account.id = relation.user_id
        WHERE travel.tenant_id <> user_account.tenant_id
    ) THEN
        RAISE EXCEPTION
            'Cross-tenant relationship detected in travel assignments.';
END IF;


    IF EXISTS (
        SELECT 1
        FROM task_assignees relation
        JOIN tasks task
            ON task.id = relation.task_id
        JOIN usuarios user_account
            ON user_account.id = relation.user_id
        WHERE task.tenant_id <> user_account.tenant_id
    ) THEN
        RAISE EXCEPTION
            'Cross-tenant relationship detected in task assignments.';
END IF;

END;
$$;


-- =========================================================
-- COMPOSITE UNIQUE KEYS
-- =========================================================
--
-- PostgreSQL requires the referenced column combination
-- to be UNIQUE before it can be targeted by a composite
-- foreign key.
-- =========================================================

ALTER TABLE clients
    ADD CONSTRAINT uq_clients_tenant_id_id
        UNIQUE (tenant_id, id);


ALTER TABLE incomes
    ADD CONSTRAINT uq_incomes_tenant_id_id
        UNIQUE (tenant_id, id);


ALTER TABLE travels
    ADD CONSTRAINT uq_travels_tenant_id_id
        UNIQUE (tenant_id, id);


ALTER TABLE travel_expenses
    ADD CONSTRAINT uq_travel_expenses_tenant_id_id
        UNIQUE (tenant_id, id);


ALTER TABLE manual_expenses
    ADD CONSTRAINT uq_manual_expenses_tenant_id_id
        UNIQUE (tenant_id, id);


ALTER TABLE tasks
    ADD CONSTRAINT uq_tasks_tenant_id_id
        UNIQUE (tenant_id, id);


ALTER TABLE task_evidence
    ADD CONSTRAINT uq_task_evidence_tenant_id_id
        UNIQUE (tenant_id, id);


ALTER TABLE inventory_catalog_items
    ADD CONSTRAINT uq_inventory_catalog_items_tenant_id_id
        UNIQUE (tenant_id, id);


ALTER TABLE inventory_products
    ADD CONSTRAINT uq_inventory_products_tenant_id_id
        UNIQUE (tenant_id, id);


ALTER TABLE inventory_stock_movements
    ADD CONSTRAINT uq_inventory_stock_movements_tenant_id_id
        UNIQUE (tenant_id, id);


ALTER TABLE inventory_kit_components
    ADD CONSTRAINT uq_inventory_kit_components_tenant_id_id
        UNIQUE (tenant_id, id);


-- =========================================================
-- CLIENT PHONES
-- =========================================================
--
-- Hibernate does not explicitly insert tenant_id into this
-- element collection table. A trigger derives it from the
-- owning client.
-- =========================================================

ALTER TABLE client_phones
    ADD COLUMN tenant_id BIGINT;


UPDATE client_phones phone
SET tenant_id = client.tenant_id
    FROM clients client
WHERE client.id = phone.client_id;


ALTER TABLE client_phones
    ALTER COLUMN tenant_id SET NOT NULL;


CREATE OR REPLACE FUNCTION set_client_phone_tenant()
RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
BEGIN

SELECT tenant_id
INTO NEW.tenant_id
FROM clients
WHERE id = NEW.client_id;


IF NEW.tenant_id IS NULL THEN
        RAISE EXCEPTION
            'Unable to determine tenant for client phone.';
END IF;


RETURN NEW;
END;
$$;


CREATE TRIGGER trg_client_phones_set_tenant
    BEFORE INSERT OR UPDATE OF client_id
                     ON client_phones
                         FOR EACH ROW
                         EXECUTE FUNCTION set_client_phone_tenant();


ALTER TABLE client_phones
DROP CONSTRAINT fk_client_phones_client;


ALTER TABLE client_phones
    ADD CONSTRAINT fk_client_phones_tenant_client
        FOREIGN KEY (
                     tenant_id,
                     client_id
            )
            REFERENCES clients (
                                tenant_id,
                                id
                )
            ON DELETE CASCADE;


CREATE INDEX idx_client_phones_tenant
    ON client_phones(
                     tenant_id,
                     client_id
        );


-- =========================================================
-- CLIENT EMAILS
-- =========================================================

ALTER TABLE client_emails
    ADD COLUMN tenant_id BIGINT;


UPDATE client_emails email_record
SET tenant_id = client.tenant_id
    FROM clients client
WHERE client.id = email_record.client_id;


ALTER TABLE client_emails
    ALTER COLUMN tenant_id SET NOT NULL;


CREATE OR REPLACE FUNCTION set_client_email_tenant()
RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
BEGIN

SELECT tenant_id
INTO NEW.tenant_id
FROM clients
WHERE id = NEW.client_id;


IF NEW.tenant_id IS NULL THEN
        RAISE EXCEPTION
            'Unable to determine tenant for client email.';
END IF;


RETURN NEW;
END;
$$;


CREATE TRIGGER trg_client_emails_set_tenant
    BEFORE INSERT OR UPDATE OF client_id
                     ON client_emails
                         FOR EACH ROW
                         EXECUTE FUNCTION set_client_email_tenant();


ALTER TABLE client_emails
DROP CONSTRAINT fk_client_emails_client;


ALTER TABLE client_emails
    ADD CONSTRAINT fk_client_emails_tenant_client
        FOREIGN KEY (
                     tenant_id,
                     client_id
            )
            REFERENCES clients (
                                tenant_id,
                                id
                )
            ON DELETE CASCADE;


CREATE INDEX idx_client_emails_tenant
    ON client_emails(
                     tenant_id,
                     client_id
        );


-- =========================================================
-- INCOMES -> CLIENTS
-- =========================================================

ALTER TABLE incomes
DROP CONSTRAINT fk_incomes_client;


ALTER TABLE incomes
    ADD CONSTRAINT fk_incomes_tenant_client
        FOREIGN KEY (
                     tenant_id,
                     client_id
            )
            REFERENCES clients (
                                tenant_id,
                                id
                )
            ON DELETE RESTRICT;


-- =========================================================
-- TRAVEL USERS
-- =========================================================

ALTER TABLE travel_users
    ADD COLUMN tenant_id BIGINT;


UPDATE travel_users relation
SET tenant_id = travel.tenant_id
    FROM travels travel
WHERE travel.id = relation.travel_id;


ALTER TABLE travel_users
    ALTER COLUMN tenant_id SET NOT NULL;


CREATE OR REPLACE FUNCTION set_travel_user_tenant()
RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
BEGIN

SELECT tenant_id
INTO NEW.tenant_id
FROM travels
WHERE id = NEW.travel_id;


IF NEW.tenant_id IS NULL THEN
        RAISE EXCEPTION
            'Unable to determine tenant for travel assignment.';
END IF;


RETURN NEW;
END;
$$;


CREATE TRIGGER trg_travel_users_set_tenant
    BEFORE INSERT OR UPDATE OF travel_id
                     ON travel_users
                         FOR EACH ROW
                         EXECUTE FUNCTION set_travel_user_tenant();


ALTER TABLE travel_users
DROP CONSTRAINT fk_travel_users_travel;


ALTER TABLE travel_users
DROP CONSTRAINT fk_travel_users_user;


ALTER TABLE travel_users
    ADD CONSTRAINT fk_travel_users_tenant_travel
        FOREIGN KEY (
                     tenant_id,
                     travel_id
            )
            REFERENCES travels (
                                tenant_id,
                                id
                )
            ON DELETE CASCADE;


ALTER TABLE travel_users
    ADD CONSTRAINT fk_travel_users_tenant_user
        FOREIGN KEY (
                     tenant_id,
                     user_id
            )
            REFERENCES usuarios (
                                 tenant_id,
                                 id
                )
            ON DELETE RESTRICT;


CREATE INDEX idx_travel_users_tenant
    ON travel_users(
                    tenant_id,
                    travel_id
        );


CREATE INDEX idx_travel_users_tenant_user
    ON travel_users(
                    tenant_id,
                    user_id
        );


-- =========================================================
-- TRAVEL EXPENSES
-- =========================================================

ALTER TABLE travel_expenses
DROP CONSTRAINT fk_travel_expenses_travel;


ALTER TABLE travel_expenses
DROP CONSTRAINT fk_travel_expenses_created_by;


ALTER TABLE travel_expenses
    ADD CONSTRAINT fk_travel_expenses_tenant_travel
        FOREIGN KEY (
                     tenant_id,
                     travel_id
            )
            REFERENCES travels (
                                tenant_id,
                                id
                )
            ON DELETE CASCADE;


ALTER TABLE travel_expenses
    ADD CONSTRAINT fk_travel_expenses_tenant_creator
        FOREIGN KEY (
                     tenant_id,
                     created_by_user_id
            )
            REFERENCES usuarios (
                                 tenant_id,
                                 id
                )
            ON DELETE RESTRICT;


-- =========================================================
-- TASKS
-- =========================================================

ALTER TABLE tasks
DROP CONSTRAINT fk_tasks_creator;


ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_tenant_creator
        FOREIGN KEY (
                     tenant_id,
                     creator_user_id
            )
            REFERENCES usuarios (
                                 tenant_id,
                                 id
                )
            ON DELETE RESTRICT;


-- =========================================================
-- TASK ASSIGNEES
-- =========================================================

ALTER TABLE task_assignees
    ADD COLUMN tenant_id BIGINT;


UPDATE task_assignees relation
SET tenant_id = task.tenant_id
    FROM tasks task
WHERE task.id = relation.task_id;


ALTER TABLE task_assignees
    ALTER COLUMN tenant_id SET NOT NULL;


CREATE OR REPLACE FUNCTION set_task_assignee_tenant()
RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
BEGIN

SELECT tenant_id
INTO NEW.tenant_id
FROM tasks
WHERE id = NEW.task_id;


IF NEW.tenant_id IS NULL THEN
        RAISE EXCEPTION
            'Unable to determine tenant for task assignment.';
END IF;


RETURN NEW;
END;
$$;


CREATE TRIGGER trg_task_assignees_set_tenant
    BEFORE INSERT OR UPDATE OF task_id
                     ON task_assignees
                         FOR EACH ROW
                         EXECUTE FUNCTION set_task_assignee_tenant();


ALTER TABLE task_assignees
DROP CONSTRAINT fk_task_assignees_task;


ALTER TABLE task_assignees
DROP CONSTRAINT fk_task_assignees_user;


ALTER TABLE task_assignees
    ADD CONSTRAINT fk_task_assignees_tenant_task
        FOREIGN KEY (
                     tenant_id,
                     task_id
            )
            REFERENCES tasks (
                              tenant_id,
                              id
                )
            ON DELETE CASCADE;


ALTER TABLE task_assignees
    ADD CONSTRAINT fk_task_assignees_tenant_user
        FOREIGN KEY (
                     tenant_id,
                     user_id
            )
            REFERENCES usuarios (
                                 tenant_id,
                                 id
                )
            ON DELETE RESTRICT;


CREATE INDEX idx_task_assignees_tenant
    ON task_assignees(
                      tenant_id,
                      task_id
        );


CREATE INDEX idx_task_assignees_tenant_user
    ON task_assignees(
                      tenant_id,
                      user_id
        );


-- =========================================================
-- TASK EVIDENCE
-- =========================================================

ALTER TABLE task_evidence
DROP CONSTRAINT fk_task_evidence_task;


ALTER TABLE task_evidence
DROP CONSTRAINT fk_task_evidence_author;


ALTER TABLE task_evidence
    ADD CONSTRAINT fk_task_evidence_tenant_task
        FOREIGN KEY (
                     tenant_id,
                     task_id
            )
            REFERENCES tasks (
                              tenant_id,
                              id
                )
            ON DELETE CASCADE;


ALTER TABLE task_evidence
    ADD CONSTRAINT fk_task_evidence_tenant_author
        FOREIGN KEY (
                     tenant_id,
                     author_user_id
            )
            REFERENCES usuarios (
                                 tenant_id,
                                 id
                )
            ON DELETE RESTRICT;


-- =========================================================
-- INVENTORY PRODUCTS -> CATALOGS
-- =========================================================

ALTER TABLE inventory_products
DROP CONSTRAINT fk_inventory_products_category;

ALTER TABLE inventory_products
DROP CONSTRAINT fk_inventory_products_brand;

ALTER TABLE inventory_products
DROP CONSTRAINT fk_inventory_products_unit_measure;

ALTER TABLE inventory_products
DROP CONSTRAINT fk_inventory_products_supplier;

ALTER TABLE inventory_products
DROP CONSTRAINT fk_inventory_products_measure;

ALTER TABLE inventory_products
DROP CONSTRAINT fk_inventory_products_warehouse;

ALTER TABLE inventory_products
DROP CONSTRAINT fk_inventory_products_condition;


ALTER TABLE inventory_products
    ADD CONSTRAINT fk_inventory_products_tenant_category
        FOREIGN KEY (
                     tenant_id,
                     category_id
            )
            REFERENCES inventory_catalog_items (
                                                tenant_id,
                                                id
                )
            ON DELETE RESTRICT;


ALTER TABLE inventory_products
    ADD CONSTRAINT fk_inventory_products_tenant_brand
        FOREIGN KEY (
                     tenant_id,
                     brand_id
            )
            REFERENCES inventory_catalog_items (
                                                tenant_id,
                                                id
                )
            ON DELETE RESTRICT;


ALTER TABLE inventory_products
    ADD CONSTRAINT fk_inventory_products_tenant_unit_measure
        FOREIGN KEY (
                     tenant_id,
                     unit_measure_id
            )
            REFERENCES inventory_catalog_items (
                                                tenant_id,
                                                id
                )
            ON DELETE RESTRICT;


ALTER TABLE inventory_products
    ADD CONSTRAINT fk_inventory_products_tenant_supplier
        FOREIGN KEY (
                     tenant_id,
                     supplier_id
            )
            REFERENCES inventory_catalog_items (
                                                tenant_id,
                                                id
                )
            ON DELETE RESTRICT;


ALTER TABLE inventory_products
    ADD CONSTRAINT fk_inventory_products_tenant_measure
        FOREIGN KEY (
                     tenant_id,
                     measure_id
            )
            REFERENCES inventory_catalog_items (
                                                tenant_id,
                                                id
                )
            ON DELETE RESTRICT;


ALTER TABLE inventory_products
    ADD CONSTRAINT fk_inventory_products_tenant_warehouse
        FOREIGN KEY (
                     tenant_id,
                     warehouse_id
            )
            REFERENCES inventory_catalog_items (
                                                tenant_id,
                                                id
                )
            ON DELETE RESTRICT;


ALTER TABLE inventory_products
    ADD CONSTRAINT fk_inventory_products_tenant_condition
        FOREIGN KEY (
                     tenant_id,
                     condition_id
            )
            REFERENCES inventory_catalog_items (
                                                tenant_id,
                                                id
                )
            ON DELETE RESTRICT;


-- =========================================================
-- INVENTORY STOCK MOVEMENTS
-- =========================================================

ALTER TABLE inventory_stock_movements
DROP CONSTRAINT fk_inventory_stock_movements_product;


ALTER TABLE inventory_stock_movements
DROP CONSTRAINT fk_inventory_stock_movements_user;


ALTER TABLE inventory_stock_movements
    ADD CONSTRAINT fk_inventory_stock_movements_tenant_product
        FOREIGN KEY (
                     tenant_id,
                     product_id
            )
            REFERENCES inventory_products (
                                           tenant_id,
                                           id
                )
            ON DELETE RESTRICT;


ALTER TABLE inventory_stock_movements
    ADD CONSTRAINT fk_inventory_stock_movements_tenant_user
        FOREIGN KEY (
                     tenant_id,
                     user_id
            )
            REFERENCES usuarios (
                                 tenant_id,
                                 id
                )
            ON DELETE RESTRICT;


-- =========================================================
-- INVENTORY KIT COMPONENTS
-- =========================================================

ALTER TABLE inventory_kit_components
DROP CONSTRAINT fk_inventory_kit_components_kit;


ALTER TABLE inventory_kit_components
DROP CONSTRAINT fk_inventory_kit_components_component;


ALTER TABLE inventory_kit_components
    ADD CONSTRAINT fk_inventory_kit_components_tenant_kit
        FOREIGN KEY (
                     tenant_id,
                     kit_product_id
            )
            REFERENCES inventory_products (
                                           tenant_id,
                                           id
                )
            ON DELETE CASCADE;


ALTER TABLE inventory_kit_components
    ADD CONSTRAINT fk_inventory_kit_components_tenant_component
        FOREIGN KEY (
                     tenant_id,
                     component_product_id
            )
            REFERENCES inventory_products (
                                           tenant_id,
                                           id
                )
            ON DELETE RESTRICT;


-- =========================================================
-- TENANT-FOCUSED INDEXES
-- =========================================================

CREATE INDEX idx_incomes_tenant_record_date
    ON incomes(
               tenant_id,
               record_date
        );


CREATE INDEX idx_incomes_tenant_status
    ON incomes(
               tenant_id,
               status
        );


CREATE INDEX idx_travels_tenant_start_date
    ON travels(
               tenant_id,
               start_date
        );


CREATE INDEX idx_travel_expenses_tenant_date
    ON travel_expenses(
                       tenant_id,
                       expense_date
        );


CREATE INDEX idx_travel_expenses_tenant_status
    ON travel_expenses(
                       tenant_id,
                       status
        );


CREATE INDEX idx_manual_expenses_tenant_date
    ON manual_expenses(
                       tenant_id,
                       expense_date
        );


CREATE INDEX idx_tasks_tenant_status
    ON tasks(
             tenant_id,
             status
        );


CREATE INDEX idx_tasks_tenant_due_at
    ON tasks(
             tenant_id,
             due_at
        );


CREATE INDEX idx_inventory_products_tenant_description
    ON inventory_products(
                          tenant_id,
                          LOWER(description)
        );


CREATE INDEX idx_inventory_products_tenant_quantity
    ON inventory_products(
                          tenant_id,
                          quantity
        );


CREATE INDEX idx_inventory_stock_movements_tenant_created
    ON inventory_stock_movements(
                                 tenant_id,
                                 created_at
        );


CREATE INDEX idx_inventory_kit_components_tenant_kit
    ON inventory_kit_components(
                                tenant_id,
                                kit_product_id
        );