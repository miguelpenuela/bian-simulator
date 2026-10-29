-- Identity and Access
CREATE TABLE app_user (
    id            uuid         PRIMARY KEY,
    username      varchar(64)  NOT NULL,
    display_name  varchar(120) NOT NULL,
    password_hash varchar(255) NOT NULL,
    enabled       boolean      NOT NULL DEFAULT true,
    created_at    timestamptz  NOT NULL,
    updated_at    timestamptz  NOT NULL,
    CONSTRAINT uk_app_user_username UNIQUE (username),
    CONSTRAINT ck_app_user_username_lower CHECK (username = lower(username))
);

CREATE TABLE role (
    code        varchar(40)  PRIMARY KEY,
    name        varchar(80)  NOT NULL,
    description varchar(500) NOT NULL
);

CREATE TABLE permission (
    code        varchar(60)  PRIMARY KEY,
    description varchar(500) NOT NULL
);

CREATE TABLE role_permission (
    role_code       varchar(40) NOT NULL REFERENCES role (code) ON DELETE CASCADE,
    permission_code varchar(60) NOT NULL REFERENCES permission (code) ON DELETE CASCADE,
    PRIMARY KEY (role_code, permission_code)
);

CREATE TABLE user_role (
    user_id   uuid        NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    role_code varchar(40) NOT NULL REFERENCES role (code),
    PRIMARY KEY (user_id, role_code)
);

CREATE INDEX ix_user_role_role ON user_role (role_code);
