-- V1__init_schema.sql: Initial database schema for bookstore application

-- Authors table
CREATE TABLE authors (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    bio         TEXT,
    birth_date  DATE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Books table
CREATE TABLE books (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    isbn            VARCHAR(20)  NOT NULL UNIQUE,
    genre           VARCHAR(100),
    price           NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    stock_quantity  INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    author_id       BIGINT REFERENCES authors(id) ON DELETE SET NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Customers table
CREATE TABLE customers (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    phone       VARCHAR(30),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Orders table
CREATE TABLE orders (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL REFERENCES customers(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),
    total_amount    NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Order items table
CREATE TABLE order_items (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    book_id         BIGINT NOT NULL REFERENCES books(id),
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    price_per_item  NUMERIC(10, 2) NOT NULL CHECK (price_per_item >= 0)
);

-- Indexes for performance
CREATE INDEX idx_books_author_id   ON books(author_id);
CREATE INDEX idx_books_genre       ON books(genre);
CREATE INDEX idx_orders_customer   ON orders(customer_id);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_book  ON order_items(book_id);
