-- ============================================================
--  Library Management System — Schema
--  PostgreSQL 18
-- ============================================================

DROP TABLE IF EXISTS loans   CASCADE;
DROP TABLE IF EXISTS books   CASCADE;
DROP TABLE IF EXISTS authors CASCADE;
DROP TABLE IF EXISTS readers CASCADE;

-- ----------------------------------------------------------
--  1. authors
-- ----------------------------------------------------------
CREATE TABLE authors (
    id         SERIAL       PRIMARY KEY,
    full_name  VARCHAR(150) NOT NULL,
    country    VARCHAR(80),
    birth_year SMALLINT
);

-- ----------------------------------------------------------
--  2. books
-- ----------------------------------------------------------
CREATE TABLE books (
    id        SERIAL       PRIMARY KEY,
    title     VARCHAR(250) NOT NULL,
    author_id INT          NOT NULL REFERENCES authors(id) ON DELETE RESTRICT,
    genre     VARCHAR(80),
    isbn      VARCHAR(20)  UNIQUE,
    pub_year  SMALLINT,
    copies    SMALLINT     NOT NULL DEFAULT 1 CHECK (copies >= 0)
);

-- ----------------------------------------------------------
--  3. readers
-- ----------------------------------------------------------
CREATE TABLE readers (
    id        SERIAL       PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email     VARCHAR(120) UNIQUE,
    phone     VARCHAR(20),
    reg_date  DATE         NOT NULL DEFAULT CURRENT_DATE
);

-- ----------------------------------------------------------
--  4. loans
-- ----------------------------------------------------------
CREATE TABLE loans (
    id        SERIAL      PRIMARY KEY,
    book_id   INT         NOT NULL REFERENCES books(id)   ON DELETE RESTRICT,
    reader_id INT         NOT NULL REFERENCES readers(id) ON DELETE RESTRICT,
    loan_date DATE        NOT NULL DEFAULT CURRENT_DATE,
    due_date  DATE        NOT NULL,
    status    VARCHAR(20) NOT NULL DEFAULT 'active'
        CHECK (status IN ('active', 'returned', 'overdue'))
);

-- ----------------------------------------------------------
--  Indexes for FK columns (speed up JOIN / search)
-- ----------------------------------------------------------
CREATE INDEX idx_books_author   ON books (author_id);
CREATE INDEX idx_loans_book     ON loans (book_id);
CREATE INDEX idx_loans_reader   ON loans (reader_id);
CREATE INDEX idx_loans_status   ON loans (status);
