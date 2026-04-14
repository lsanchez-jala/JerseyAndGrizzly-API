CREATE TABLE IF NOT EXISTS customer (
    id          UUID                        PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name  VARCHAR(255)                NOT NULL,
    last_name   VARCHAR(255)                NOT NULL,
    email       VARCHAR(255)                NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS product (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255)    NOT NULL,
    sku         VARCHAR(100)    NOT NULL,
    price       NUMERIC(12, 2)  NOT NULL,
    stock       INT             NOT NULL DEFAULT 0,
    category    VARCHAR(100)
    );

CREATE TABLE IF NOT EXISTS orders (
    id              UUID                        PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id     UUID,
    shipment_id     UUID,
    status          VARCHAR(50)                 NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS order_item (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id    UUID            NOT NULL,
    product_id  UUID            NOT NULL,
    quantity    INT             NOT NULL,
    unit_price  NUMERIC(12, 2)  NOT NULL
    );

CREATE TABLE IF NOT EXISTS shipment (
    id              UUID                        PRIMARY KEY DEFAULT gen_random_uuid(),
    tracking_code   VARCHAR(255)                NOT NULL,
    carrier         VARCHAR(255)                NOT NULL,
    status          VARCHAR(100)                NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now()
    );
