CREATE TABLE user_actions (
    id BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL,
    qr_code_id BIGINT,
    unique_pdf_id VARCHAR(255),
    is_related_to_qr_code BOOLEAN DEFAULT FALSE,
    type_action VARCHAR(50) NOT NULL,
    description TEXT,
    date_action TIMESTAMP,
    CONSTRAINT fk_action_user FOREIGN KEY (utilisateur_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_action_qrcode FOREIGN KEY (qr_code_id) REFERENCES qrcode_metadata(id) ON DELETE CASCADE
);