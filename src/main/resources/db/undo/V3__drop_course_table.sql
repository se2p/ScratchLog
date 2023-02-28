DROP TABLE scratch1984.course_participant;
DROP TABLE scratch1984.course_experiment;
DROP TABLE scratch1984.course;
DELETE FROM scratch1984.flyway_schema_history AS f WHERE f.version='3';
