CREATE TABLE komplettering_tillstand (
    handlaggning_id   UUID         NOT NULL PRIMARY KEY,
    oul_uppgift_id    UUID         NOT NULL,
    reply_to          VARCHAR(255) NOT NULL,
    cloud_event_data  TEXT         NOT NULL,
    version           BIGINT       NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL
);
