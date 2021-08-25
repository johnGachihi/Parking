CREATE TABLE payment_sessions
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    started_at  datetime              NOT NULL,
    amount      DOUBLE                NOT NULL,
    visit_id    BIGINT                NOT NULL,
    status      VARCHAR(255)          NOT NULL,
    finished_at datetime              NULL,
    CONSTRAINT pk_payment_sessions PRIMARY KEY (id)
);

ALTER TABLE payment_sessions
    ADD CONSTRAINT FK_PAYMENT_SESSIONS_ON_VISIT FOREIGN KEY (visit_id) REFERENCES visits (id);