CREATE TABLE  IF NOT EXISTS hits
(
    id        BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    app       VARCHAR(128) NOT NULL,
    uri       VARCHAR(255) NOT NULL,
    ip        VARCHAR(16) NOT NULL,
    timestamp timestamp WITHOUT TIME ZONE NOT NULL
    );