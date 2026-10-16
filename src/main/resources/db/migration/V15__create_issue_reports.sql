-- =========================================================
-- V15
-- ISSUE REPORTS
-- =========================================================

CREATE TABLE issue_reports (

                               id BIGSERIAL PRIMARY KEY,

                               tenant_id BIGINT NOT NULL,

                               reporter_user_id BIGINT NOT NULL,

                               page VARCHAR(300) NOT NULL,

                               description VARCHAR(2000) NOT NULL,

                               request_id VARCHAR(100),

                               status VARCHAR(20) NOT NULL DEFAULT 'OPEN',

                               created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               resolved_at TIMESTAMPTZ,


                               CONSTRAINT fk_issue_reports_tenant
                                   FOREIGN KEY (tenant_id)
                                       REFERENCES tenants(id)
                                       ON DELETE RESTRICT,


                               CONSTRAINT fk_issue_reports_tenant_user
                                   FOREIGN KEY (
                                                tenant_id,
                                                reporter_user_id
                                       )
                                       REFERENCES usuarios (
                                                            tenant_id,
                                                            id
                                           )
                                       ON DELETE RESTRICT,


                               CONSTRAINT chk_issue_reports_page
                                   CHECK (
                                       BTRIM(page) <> ''
                                       ),


                               CONSTRAINT chk_issue_reports_description
                                   CHECK (
                                       BTRIM(description) <> ''
                                       ),


                               CONSTRAINT chk_issue_reports_status
                                   CHECK (
                                       status IN (
                                                  'OPEN',
                                                  'RESOLVED'
                                           )
                                       )
);


CREATE INDEX idx_issue_reports_tenant_created
    ON issue_reports (
                      tenant_id,
                      created_at DESC
        );


CREATE INDEX idx_issue_reports_tenant_status
    ON issue_reports (
                      tenant_id,
                      status,
                      created_at DESC
        );


-- =========================================================
-- ROW LEVEL SECURITY
-- =========================================================

ALTER TABLE issue_reports
    ENABLE ROW LEVEL SECURITY;


ALTER TABLE issue_reports
    FORCE ROW LEVEL SECURITY;


CREATE POLICY tenant_isolation
    ON issue_reports
    USING (
        tenant_id = current_app_tenant_id()
    )
    WITH CHECK (
        tenant_id = current_app_tenant_id()
    );