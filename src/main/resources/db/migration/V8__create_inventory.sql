CREATE TABLE inventory_products (
                                    id BIGSERIAL PRIMARY KEY,

                                    description VARCHAR(200) NOT NULL,

                                    model VARCHAR(100),

                                    category_id BIGINT,

                                    brand_id BIGINT,

                                    unit_measure_id BIGINT,

                                    supplier_id BIGINT,

                                    measure_id BIGINT,

                                    warehouse_id BIGINT,

                                    condition_id BIGINT,

                                    row_location VARCHAR(50),

                                    unit_price NUMERIC(14, 2) NOT NULL DEFAULT 0,

                                    quantity INTEGER NOT NULL DEFAULT 0,

                                    minimum_stock INTEGER NOT NULL DEFAULT 0,

                                    is_kit BOOLEAN NOT NULL DEFAULT FALSE,

                                    photo_file_name VARCHAR(255),

                                    qr_code VARCHAR(100),

                                    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT fk_inventory_products_category
                                        FOREIGN KEY (category_id)
                                            REFERENCES inventory_catalog_items(id)
                                            ON DELETE RESTRICT,

                                    CONSTRAINT fk_inventory_products_brand
                                        FOREIGN KEY (brand_id)
                                            REFERENCES inventory_catalog_items(id)
                                            ON DELETE RESTRICT,

                                    CONSTRAINT fk_inventory_products_unit_measure
                                        FOREIGN KEY (unit_measure_id)
                                            REFERENCES inventory_catalog_items(id)
                                            ON DELETE RESTRICT,

                                    CONSTRAINT fk_inventory_products_supplier
                                        FOREIGN KEY (supplier_id)
                                            REFERENCES inventory_catalog_items(id)
                                            ON DELETE RESTRICT,

                                    CONSTRAINT fk_inventory_products_measure
                                        FOREIGN KEY (measure_id)
                                            REFERENCES inventory_catalog_items(id)
                                            ON DELETE RESTRICT,

                                    CONSTRAINT fk_inventory_products_warehouse
                                        FOREIGN KEY (warehouse_id)
                                            REFERENCES inventory_catalog_items(id)
                                            ON DELETE RESTRICT,

                                    CONSTRAINT fk_inventory_products_condition
                                        FOREIGN KEY (condition_id)
                                            REFERENCES inventory_catalog_items(id)
                                            ON DELETE RESTRICT,

                                    CONSTRAINT chk_inventory_products_description
                                        CHECK (
                                            BTRIM(description) <> ''
                                            ),

                                    CONSTRAINT chk_inventory_products_unit_price
                                        CHECK (
                                            unit_price >= 0
                                            ),

                                    CONSTRAINT chk_inventory_products_quantity
                                        CHECK (
                                            quantity >= 0
                                            ),

                                    CONSTRAINT chk_inventory_products_minimum_stock
                                        CHECK (
                                            minimum_stock >= 0
                                            )
);


CREATE UNIQUE INDEX uq_inventory_products_qr_code
    ON inventory_products(qr_code)
    WHERE qr_code IS NOT NULL;


CREATE INDEX idx_inventory_products_description_lower
    ON inventory_products(LOWER(description));


CREATE INDEX idx_inventory_products_model_lower
    ON inventory_products(LOWER(model));


CREATE INDEX idx_inventory_products_category
    ON inventory_products(category_id);


CREATE INDEX idx_inventory_products_brand
    ON inventory_products(brand_id);


CREATE INDEX idx_inventory_products_measure
    ON inventory_products(measure_id);


CREATE INDEX idx_inventory_products_warehouse
    ON inventory_products(warehouse_id);


CREATE INDEX idx_inventory_products_quantity
    ON inventory_products(quantity);


CREATE TABLE inventory_stock_movements (
                                           id BIGSERIAL PRIMARY KEY,

                                           product_id BIGINT NOT NULL,

                                           user_id BIGINT NOT NULL,

                                           movement_type VARCHAR(30) NOT NULL,

                                           quantity_change INTEGER NOT NULL,

                                           resulting_quantity INTEGER NOT NULL,

                                           reason VARCHAR(500),

                                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                           CONSTRAINT fk_inventory_stock_movements_product
                                               FOREIGN KEY (product_id)
                                                   REFERENCES inventory_products(id)
                                                   ON DELETE CASCADE,

                                           CONSTRAINT fk_inventory_stock_movements_user
                                               FOREIGN KEY (user_id)
                                                   REFERENCES usuarios(id)
                                                   ON DELETE RESTRICT,

                                           CONSTRAINT chk_inventory_stock_movements_type
                                               CHECK (
                                                   movement_type IN (
                                                                     'INITIAL',
                                                                     'ENTRY',
                                                                     'EXIT',
                                                                     'KIT_ASSEMBLY',
                                                                     'KIT_DISASSEMBLY'
                                                       )
                                                   ),

                                           CONSTRAINT chk_inventory_stock_movements_change
                                               CHECK (
                                                   quantity_change <> 0
                                                   ),

                                           CONSTRAINT chk_inventory_stock_movements_result
                                               CHECK (
                                                   resulting_quantity >= 0
                                                   )
);


CREATE INDEX idx_inventory_stock_movements_product
    ON inventory_stock_movements(product_id);


CREATE INDEX idx_inventory_stock_movements_created_at
    ON inventory_stock_movements(created_at);