CREATE TABLE qrcode_metadata (
    id BIGSERIAL PRIMARY KEY,
    unique_id VARCHAR(255) NOT NULL UNIQUE,
    file_path VARCHAR(500) NOT NULL,
    pdf_id VARCHAR(255) NOT NULL,
    qr_content VARCHAR(1000) NOT NULL,
    generation_date TIMESTAMP,
    image_format VARCHAR(100),
    image_size INTEGER
);
