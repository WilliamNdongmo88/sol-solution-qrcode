CREATE TABLE pdf_metadata (
    id BIGSERIAL PRIMARY KEY,
    unique_id VARCHAR(255) NOT NULL UNIQUE,
    original_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    upload_date TIMESTAMP,
    content_type VARCHAR(255)
);
