ALTER TABLE user DROP COLUMN last_login;
DELETE FROM scratch1984.flyway_schema_history AS f WHERE f.version='4';
