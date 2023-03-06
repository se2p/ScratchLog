ALTER TABLE experiment DROP COLUMN gui_url;
DELETE FROM flyway_schema_history AS f WHERE f.version='2';
