-- =========================================================
-- V13
-- TENANT ROW LEVEL SECURITY
-- =========================================================
--
-- RLS provides an additional database-level isolation
-- boundary for tenant-owned information.
--
-- The application sets:
--
--     app.current_tenant
--
-- inside the current database transaction.
--
-- If no valid tenant context exists, the policies fail
-- closed and tenant-owned rows are not visible.
--
-- The tenants and usuarios tables are intentionally not
-- protected by these policies because they currently form
-- part of the authentication/control plane.
-- Application-level tenant authorization continues to
-- protect tenant user administration.
-- =========================================================


-- =========================================================
-- CURRENT TENANT FUNCTION
-- =========================================================

CREATE OR REPLACE FUNCTION current_app_tenant_id()
RETURNS BIGINT
LANGUAGE plpgsql
STABLE
AS
$$
DECLARE
tenant_value TEXT;
BEGIN

    tenant_value :=
        current_setting(
            'app.current_tenant',
            TRUE
        );


    IF tenant_value IS NULL
       OR tenant_value = ''
       OR tenant_value !~ '^[1-9][0-9]*$'
    THEN
        RETURN NULL;
END IF;


RETURN tenant_value::BIGINT;
END;
$$;


-- =========================================================
-- CLIENTS
-- =========================================================

ALTER TABLE clients
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE clients
    FORCE ROW LEVEL SECURITY;


CREATE POLICY tenant_isolation
    ON clients
    USING (
        tenant_id = current_app_tenant_id()
    )
    WITH CHECK (
        tenant_id = current_app_tenant_id()
    );


-- =========================================================
-- CLIENT PHONES
-- =========================================================

ALTER TABLE client_phones
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE client_phones
    FORCE ROW LEVEL SECURITY;


CREATE POLICY tenant_isolation
    ON client_phones
    USING (
        tenant_id = current_app_tenant_id()
    )
    WITH CHECK (
        tenant_id = current_app_tenant_id()
    );


-- =========================================================
-- CLIENT EMAILS
-- =========================================================

ALTER TABLE client_emails
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE client_emails
    FORCE ROW LEVEL SECURITY;


CREATE POLICY tenant_isolation
    ON client_emails
    USING (
        tenant_id = current_app_tenant_id()
    )
    WITH CHECK (
        tenant_id = current_app_tenant_id()
    );


-- =========================================================
-- INCOMES
-- =========================================================

ALTER TABLE incomes
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE incomes
    FORCE ROW LEVEL SECURITY;


CREATE POLICY tenant_isolation
    ON incomes
    USING (
        tenant_id = current_app_tenant_id()
    )
    WITH CHECK (
        tenant_id = current_app_tenant_id()
    );


-- =========================================================
-- TRAVELS
-- =========================================================

ALTER TABLE travels
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE travels
    FORCE ROW LEVEL SECURITY;


CREATE POLICY tenant_isolation
    ON travels
    USING (
        tenant_id = current_app_tenant_id()
    )
    WITH CHECK (
        tenant_id = current_app_tenant_id()
    );


-- =========================================================
-- TRAVEL USERS
-- =========================================================

ALTER TABLE travel_users
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE travel_users
    FORCE ROW LEVEL SECURITY;


CREATE POLICY tenant_isolation
    ON travel_users
    USING (
        tenant_id = current_app_tenant_id()
    )
    WITH CHECK (
        tenant_id = current_app_tenant_id()
    );


-- =========================================================
-- TRAVEL EXPENSES
-- =========================================================

ALTER TABLE travel_expenses
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE travel_expenses
    FORCE ROW LEVEL SECURITY;


CREATE POLICY tenant_isolation
    ON travel_expenses
    USING (
        tenant_id = current_app_tenant_id()
    )
    WITH CHECK (
        tenant_id = current_app_tenant_id()
    );


-- =========================================================
-- MANUAL EXPENSES
-- =========================================================

ALTER TABLE manual_expenses
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE manual_expenses
    FORCE ROW LEVEL SECURITY;


CREATE POLICY tenant_isolation
    ON manual_expenses
    USING (
        tenant_id = current_app_tenant_id()
    )
    WITH CHECK (
        tenant_id = current_app_tenant_id()
    );


-- =========================================================
-- TASKS
-- =========================================================

ALTER TABLE tasks
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE tasks
    FORCE ROW LEVEL SECURITY;


CREATE POLICY tenant_isolation
    ON tasks
    USING (
        tenant_id = current_app_tenant_id()
    )
    WITH CHECK (
        tenant_id = current_app_tenant_id()
    );


-- =========================================================
-- TASK ASSIGNEES
-- =========================================================

ALTER TABLE task_assignees
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE task_assignees
    FORCE ROW LEVEL SECURITY;


CREATE POLICY tenant_isolation
    ON task_assignees
    USING (
        tenant_id = current_app_tenant_id()
    )
    WITH CHECK (
        tenant_id = current_app_tenant_id()
    );


-- =========================================================
-- TASK EVIDENCE
-- =========================================================

ALTER TABLE task_evidence
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE task_evidence
    FORCE ROW LEVEL SECURITY;


CREATE POLICY tenant_isolation
    ON task_evidence
    USING (
        tenant_id = current_app_tenant_id()
    )
    WITH CHECK (
        tenant_id = current_app_tenant_id()
    );


-- =========================================================
-- INVENTORY CATALOG ITEMS
-- =========================================================

ALTER TABLE inventory_catalog_items
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE inventory_catalog_items
    FORCE ROW LEVEL SECURITY;


CREATE POLICY tenant_isolation
    ON inventory_catalog_items
    USING (
        tenant_id = current_app_tenant_id()
    )
    WITH CHECK (
        tenant_id = current_app_tenant_id()
    );


-- =========================================================
-- INVENTORY PRODUCTS
-- =========================================================

ALTER TABLE inventory_products
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE inventory_products
    FORCE ROW LEVEL SECURITY;


CREATE POLICY tenant_isolation
    ON inventory_products
    USING (
        tenant_id = current_app_tenant_id()
    )
    WITH CHECK (
        tenant_id = current_app_tenant_id()
    );


-- =========================================================
-- INVENTORY STOCK MOVEMENTS
-- =========================================================

ALTER TABLE inventory_stock_movements
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE inventory_stock_movements
    FORCE ROW LEVEL SECURITY;


CREATE POLICY tenant_isolation
    ON inventory_stock_movements
    USING (
        tenant_id = current_app_tenant_id()
    )
    WITH CHECK (
        tenant_id = current_app_tenant_id()
    );


-- =========================================================
-- INVENTORY KIT COMPONENTS
-- =========================================================

ALTER TABLE inventory_kit_components
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE inventory_kit_components
    FORCE ROW LEVEL SECURITY;


CREATE POLICY tenant_isolation
    ON inventory_kit_components
    USING (
        tenant_id = current_app_tenant_id()
    )
    WITH CHECK (
        tenant_id = current_app_tenant_id()
    );