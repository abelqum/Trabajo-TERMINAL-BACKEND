CREATE TABLE inventory_kit_components (
                                          id BIGSERIAL PRIMARY KEY,

                                          kit_product_id BIGINT NOT NULL,

                                          component_product_id BIGINT NOT NULL,

                                          required_quantity INTEGER NOT NULL,

                                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                          CONSTRAINT fk_inventory_kit_components_kit
                                              FOREIGN KEY (kit_product_id)
                                                  REFERENCES inventory_products(id)
                                                  ON DELETE CASCADE,

                                          CONSTRAINT fk_inventory_kit_components_component
                                              FOREIGN KEY (component_product_id)
                                                  REFERENCES inventory_products(id)
                                                  ON DELETE RESTRICT,

                                          CONSTRAINT uq_inventory_kit_components
                                              UNIQUE (kit_product_id, component_product_id),

                                          CONSTRAINT chk_inventory_kit_components_quantity
                                              CHECK (required_quantity > 0),

                                          CONSTRAINT chk_inventory_kit_components_different_products
                                              CHECK (kit_product_id <> component_product_id)
);


CREATE INDEX idx_inventory_kit_components_kit
    ON inventory_kit_components(kit_product_id);


CREATE INDEX idx_inventory_kit_components_component
    ON inventory_kit_components(component_product_id);


-- Los movimientos forman parte del historial.
-- A partir de V9 no permitimos borrar un producto
-- que ya tenga movimientos registrados.

ALTER TABLE inventory_stock_movements
DROP CONSTRAINT fk_inventory_stock_movements_product;


ALTER TABLE inventory_stock_movements
    ADD CONSTRAINT fk_inventory_stock_movements_product
        FOREIGN KEY (product_id)
            REFERENCES inventory_products(id)
            ON DELETE RESTRICT;