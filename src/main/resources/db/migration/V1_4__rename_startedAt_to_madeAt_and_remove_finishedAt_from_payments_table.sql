ALTER TABLE payments
    CHANGE started_at made_at datetime;

ALTER TABLE payments
    DROP COLUMN finished_at;