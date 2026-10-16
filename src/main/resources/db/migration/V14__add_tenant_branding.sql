ALTER TABLE tenants
    ADD COLUMN logo_file_name VARCHAR(255),

    ADD COLUMN primary_color VARCHAR(7)
        NOT NULL
        DEFAULT '#1D4ED8',

    ADD COLUMN sidebar_color VARCHAR(7)
        NOT NULL
        DEFAULT '#172554';


ALTER TABLE tenants
    ADD CONSTRAINT chk_tenants_primary_color
        CHECK (
            primary_color ~ '^#[0-9A-Fa-f]{6}$'
    );


ALTER TABLE tenants
    ADD CONSTRAINT chk_tenants_sidebar_color
        CHECK (
            sidebar_color ~ '^#[0-9A-Fa-f]{6}$'
    );