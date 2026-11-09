CREATE TABLE users (
                       id            UUID          NOT NULL,
                       email         VARCHAR(255)  NOT NULL,
                       password_hash VARCHAR(255)  NOT NULL,
                       full_name     VARCHAR(100)  NOT NULL,
                       role          VARCHAR(20)   NOT NULL,
                       enabled       BOOLEAN       NOT NULL DEFAULT TRUE,
                       created_at    TIMESTAMPTZ   NOT NULL,
                       updated_at    TIMESTAMPTZ   NOT NULL,

                       CONSTRAINT pk_users       PRIMARY KEY (id),
                       CONSTRAINT uk_users_email UNIQUE (email),
                       CONSTRAINT ck_users_role  CHECK (role IN ('ADMIN', 'DEVELOPER', 'VIEWER'))
);