CREATE TABLE visits
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    dtype       VARCHAR(31)           NULL,
    entry_time  datetime              NULL,
    ticket_code BIGINT                NULL,
    exit_time   datetime              NULL,
    CONSTRAINT pk_visits PRIMARY KEY (id)
);


CREATE TABLE payments
(
    id       BIGINT AUTO_INCREMENT NOT NULL,
    visit_id BIGINT                NULL,
    made_at  datetime              NULL,
    amount   DOUBLE                NULL,
    CONSTRAINT pk_payments PRIMARY KEY (id)
);

ALTER TABLE payments
    ADD CONSTRAINT FK_PAYMENTS_ON_VISIT FOREIGN KEY (visit_id) REFERENCES visits (id);


CREATE TABLE configuration
(
    id    BIGINT AUTO_INCREMENT NOT NULL,
    dtype VARCHAR(31)           NULL,
    `key` VARCHAR(255)          NULL,
    value VARCHAR(255)          NULL,
    CONSTRAINT pk_configuration PRIMARY KEY (id)
);

ALTER TABLE configuration
    ADD CONSTRAINT uc_configuration_key UNIQUE (`key`);


CREATE TABLE parking_tariffs
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    upper_limit BIGINT                NULL,
    fee         DOUBLE                NOT NULL,
    CONSTRAINT pk_parking_tariffs PRIMARY KEY (id)
);

ALTER TABLE parking_tariffs
    ADD CONSTRAINT uc_parking_tariffs_upperlimit UNIQUE (upper_limit);


CREATE TABLE hibernate_sequence (
    next_val bigint DEFAULT NULL
);

INSERT INTO hibernate_sequence VALUES (1);