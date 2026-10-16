CREATE TABLE inventory_catalog_items (
                                         id BIGSERIAL PRIMARY KEY,

                                         catalog_type VARCHAR(30) NOT NULL,

                                         name VARCHAR(150) NOT NULL,

                                         link VARCHAR(500),

                                         allow_price_update BOOLEAN NOT NULL DEFAULT FALSE,

                                         created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                         updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                         CONSTRAINT chk_inventory_catalog_items_type
                                             CHECK (
                                                 catalog_type IN (
                                                                  'CATEGORY',
                                                                  'SUPPLIER',
                                                                  'BRAND',
                                                                  'UNIT_OF_MEASURE',
                                                                  'WAREHOUSE',
                                                                  'CONDITION',
                                                                  'MEASURE'
                                                     )
                                                 ),

                                         CONSTRAINT chk_inventory_catalog_items_name
                                             CHECK (
                                                 BTRIM(name) <> ''
                                                 ),

                                         CONSTRAINT chk_inventory_catalog_items_link
                                             CHECK (
                                                 catalog_type = 'SUPPLIER'
                                                     OR link IS NULL
                                                 ),

                                         CONSTRAINT chk_inventory_catalog_items_price_update
                                             CHECK (
                                                 catalog_type = 'CONDITION'
                                                     OR allow_price_update = FALSE
                                                 )
);


CREATE UNIQUE INDEX uq_inventory_catalog_items_type_name_lower
    ON inventory_catalog_items (
                                catalog_type,
                                LOWER(name)
        );


CREATE INDEX idx_inventory_catalog_items_type
    ON inventory_catalog_items (
                                catalog_type
        );


CREATE INDEX idx_inventory_catalog_items_name_lower
    ON inventory_catalog_items (
                                LOWER(name)
        );