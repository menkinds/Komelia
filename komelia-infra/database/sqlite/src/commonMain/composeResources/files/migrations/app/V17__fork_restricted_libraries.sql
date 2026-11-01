ALTER TABLE AppSettings
    ADD COLUMN fork_restricted_library_ids TEXT NOT NULL DEFAULT '';

UPDATE AppSettings
SET fork_restricted_library_ids = fork_restricted_library_id
WHERE fork_restricted_library_id IS NOT NULL;
