ALTER TABLE stored_files
    ADD COLUMN max_downloads INTEGER DEFAULT 100;

UPDATE stored_files
SET max_downloads = 100
WHERE max_downloads IS NULL;

ALTER TABLE stored_files
    ALTER COLUMN max_downloads SET NOT NULL;

ALTER TABLE stored_files
    ALTER COLUMN max_downloads DROP DEFAULT;
