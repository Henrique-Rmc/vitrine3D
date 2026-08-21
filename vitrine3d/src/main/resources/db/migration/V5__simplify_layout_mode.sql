-- Remove store_templates: a curadoria de presets pertence ao frontend (businessPresets.json).
-- O frontend envia layoutMode diretamente; preset_slug é metadado opcional.
ALTER TABLE stores
    ADD COLUMN IF NOT EXISTS preset_slug varchar(100),
    DROP COLUMN IF EXISTS store_template_id;

DROP TABLE IF EXISTS store_templates;
