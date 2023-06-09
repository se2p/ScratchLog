ALTER TABLE block_event MODIFY date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE click_event MODIFY date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE debugger_event MODIFY date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE question_event MODIFY date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE file MODIFY date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE participant MODIFY start TIMESTAMP NULL DEFAULT NULL, MODIFY finish TIMESTAMP NULL DEFAULT NULL;
ALTER TABLE resource_event MODIFY date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE token MODIFY expiration TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE sb3_zip MODIFY date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE course MODIFY last_changed TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE course_participant MODIFY added TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE course_experiment MODIFY added TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
