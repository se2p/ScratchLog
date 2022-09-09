ALTER TABLE scratch1984.experiment DROP COLUMN gui_url;
DELETE FROM scratch1984.flyway_schema_history AS f WHERE f.version='2';
