CREATE TABLE qrcode_metadata (
    id BIGSERIAL PRIMARY KEY,
    unique_id VARCHAR(255) UNIQUE NOT NULL,
    qr_name VARCHAR(255),
    file_path VARCHAR(500) NOT NULL,
    pdf_id VARCHAR(255) NOT NULL,
    qr_content VARCHAR(1000) NOT NULL,
    generation_date TIMESTAMP,
    image_format VARCHAR(100),
    image_size INT,
    user_id BIGINT,
    pdf_metadata_id BIGINT,
    CONSTRAINT fk_qrcode_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_qrcode_pdf FOREIGN KEY (pdf_metadata_id) REFERENCES pdf_metadata(id) ON DELETE CASCADE
);