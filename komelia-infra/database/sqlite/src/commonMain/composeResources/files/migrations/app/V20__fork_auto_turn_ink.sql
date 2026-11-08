ALTER TABLE ImageReaderSettings
    ADD COLUMN fork_auto_turn_measure TEXT DEFAULT 'CHARS' NOT NULL;
ALTER TABLE ImageReaderSettings
    ADD COLUMN fork_auto_turn_per_ink_percent_ms INTEGER DEFAULT 300 NOT NULL;
