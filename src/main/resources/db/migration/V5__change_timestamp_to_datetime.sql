ALTER TABLE block_event MODIFY date DATETIME NOT NULL DEFAULT NOW();
ALTER TABLE click_event MODIFY date DATETIME NOT NULL DEFAULT NOW();
ALTER TABLE debugger_event MODIFY date DATETIME NOT NULL DEFAULT NOW();
ALTER TABLE question_event MODIFY date DATETIME NOT NULL DEFAULT NOW();
ALTER TABLE file MODIFY date DATETIME NOT NULL DEFAULT NOW();
ALTER TABLE participant MODIFY start DATETIME NULL DEFAULT NULL, MODIFY finish DATETIME NULL DEFAULT NULL;
ALTER TABLE resource_event MODIFY date DATETIME NOT NULL DEFAULT NOW();
ALTER TABLE token MODIFY expiration DATETIME NOT NULL DEFAULT NOW();
ALTER TABLE sb3_zip MODIFY date DATETIME NOT NULL DEFAULT NOW();
ALTER TABLE course MODIFY last_changed DATETIME NOT NULL DEFAULT NOW();
ALTER TABLE course_participant MODIFY added DATETIME NOT NULL DEFAULT NOW();
ALTER TABLE course_experiment MODIFY added DATETIME NOT NULL DEFAULT NOW();
