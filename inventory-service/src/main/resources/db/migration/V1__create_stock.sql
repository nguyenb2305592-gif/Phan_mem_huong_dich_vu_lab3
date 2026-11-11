CREATE TABLE stock_item (
    isbn        VARCHAR(13) PRIMARY KEY CHECK (isbn ~ '^97[89][0-9]{10}$'),
    available   INTEGER NOT NULL CHECK (available >= 0),
    reserved    INTEGER NOT NULL DEFAULT 0 CHECK (reserved >= 0),
    version     BIGINT  NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
