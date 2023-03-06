ALTER TABLE user DROP COLUMN last_login;
DELETE FROM flyway_schema_history AS f WHERE f.version='4';
