CREATE TABLE pdf_metadata (
    id BIGSERIAL PRIMARY KEY,
    unique_id VARCHAR(255) UNIQUE NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    upload_date TIMESTAMP,
    content_type VARCHAR(255),
    user_id BIGINT,
    CONSTRAINT fk_pdf_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);