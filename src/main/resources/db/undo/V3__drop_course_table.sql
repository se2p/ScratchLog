DROP TABLE course_participant;
DROP TABLE course_experiment;
DROP TABLE course;
DELETE FROM flyway_schema_history AS f WHERE f.version='3';
