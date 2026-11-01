ALTER TABLE ImageReaderSettings
    ADD COLUMN fork_auto_turn_text_aware INTEGER DEFAULT 0 NOT NULL;
ALTER TABLE ImageReaderSettings
    ADD COLUMN fork_auto_turn_per_char_ms INTEGER DEFAULT 100 NOT NULL;
ALTER TABLE ImageReaderSettings
    ADD COLUMN fork_auto_turn_min_seconds INTEGER DEFAULT 2 NOT NULL;
ALTER TABLE ImageReaderSettings
    ADD COLUMN fork_auto_turn_max_seconds INTEGER DEFAULT 30 NOT NULL;
ALTER TABLE ImageReaderSettings
    ADD COLUMN fork_auto_turn_ocr_language TEXT DEFAULT 'JAPANESE' NOT NULL;
