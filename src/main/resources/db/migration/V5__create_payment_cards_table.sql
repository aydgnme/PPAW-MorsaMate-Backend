CREATE TABLE IF NOT EXISTS payment_cards (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT      NOT NULL,
    cardholder_name VARCHAR(128) NOT NULL,
    card_brand      VARCHAR(32)  NOT NULL,
    card_last4      VARCHAR(4)   NOT NULL,
    card_token      VARCHAR(512) NOT NULL,
    cvv_token       VARCHAR(512) NOT NULL,
    expiry_month    INT          NOT NULL,
    expiry_year     INT          NOT NULL,
    is_default      BOOLEAN      NOT NULL DEFAULT FALSE,
    fingerprint     VARCHAR(128) NOT NULL UNIQUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP    NULL,

    CONSTRAINT fk_payment_cards_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_payment_cards_user
    ON payment_cards (user_id);


