ALTER TABLE komplettering_tillstand
    DROP COLUMN cloud_event_data,
    ADD COLUMN regel_request_id      UUID         NOT NULL,
    ADD COLUMN aktivitet_id          UUID         NOT NULL,
    ADD COLUMN type                  VARCHAR(255) NOT NULL,
    ADD COLUMN kogitorootprocid      VARCHAR(255) NOT NULL,
    ADD COLUMN kogitorootprociid     UUID         NOT NULL,
    ADD COLUMN kogitoparentprociid   UUID         NOT NULL,
    ADD COLUMN kogitoprocid          VARCHAR(255) NOT NULL,
    ADD COLUMN kogitoprocinstanceid  UUID         NOT NULL,
    ADD COLUMN kogitoprocist         VARCHAR(255) NOT NULL,
    ADD COLUMN kogitoprocversion     VARCHAR(255) NOT NULL;
