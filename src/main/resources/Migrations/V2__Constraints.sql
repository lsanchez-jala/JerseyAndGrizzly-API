-- customer
CREATE UNIQUE INDEX IF NOT EXISTS uq_customer_email ON customer(email);

-- product
CREATE UNIQUE INDEX IF NOT EXISTS uq_product_sku ON product(sku);
ALTER TABLE product ADD CONSTRAINT chk_product_price CHECK (price >= 0);
ALTER TABLE product ADD CONSTRAINT chk_product_stock CHECK (stock >= 0);

-- orders
ALTER TABLE orders ADD CONSTRAINT fk_orders_shipment FOREIGN KEY (shipment_id) REFERENCES shipment(id);
ALTER TABLE orders ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customer(id);

-- order_items
ALTER TABLE order_item ADD CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE;
ALTER TABLE order_item ADD CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE RESTRICT;
ALTER TABLE order_item ADD CONSTRAINT chk_order_item_quantity CHECK (quantity > 0);
ALTER TABLE order_item ADD CONSTRAINT chk_order_item_price CHECK (unit_price >= 0);

-- shipment
CREATE UNIQUE INDEX IF NOT EXISTS uq_shipment_tracking ON shipment(tracking_code);