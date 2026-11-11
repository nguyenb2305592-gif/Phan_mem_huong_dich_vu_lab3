CREATE SEQUENCE orders_seq START WITH 1001 INCREMENT BY 1;

CREATE TABLE orders (
    id               BIGINT PRIMARY KEY,
    order_number     VARCHAR(15)    NOT NULL UNIQUE CHECK (order_number ~ '^ORD-[0-9]{4}-[0-9]{6}$'),
    customer_id      BIGINT         NOT NULL,
    status           VARCHAR(20)    NOT NULL,
    total_amount     NUMERIC(15, 2) NOT NULL CHECK (total_amount >= 0),
    currency         VARCHAR(3)     NOT NULL,
    idempotency_key  VARCHAR(100) UNIQUE,
    request_hash     VARCHAR(64),
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    version          BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_orders_customer ON orders (customer_id);

CREATE SEQUENCE order_line_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE order_line (
    id          BIGINT PRIMARY KEY,
    order_id    BIGINT NOT NULL REFERENCES orders (id),
    isbn        VARCHAR(13)    NOT NULL,
    quantity    INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price  NUMERIC(15, 2) NOT NULL,
    currency    VARCHAR(3)     NOT NULL
);
