CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    code_acces VARCHAR(50) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    actif BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP,
    date_modification TIMESTAMP
);