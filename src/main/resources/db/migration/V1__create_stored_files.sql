CREATE TABLE stored_files (
                              id VARCHAR(255) PRIMARY KEY,
                              storage_path VARCHAR(1024) NOT NULL,
                              size_bytes BIGINT NOT NULL,
                              expires_at TIMESTAMPTZ NOT NULL,
                              created_at TIMESTAMPTZ NOT NULL,
                              downloads_remaining INT NOT NULL
);
