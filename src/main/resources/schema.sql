/**************************
 *  Definition of tables.  *
 **************************/

-- experiment table definition

CREATE TABLE IF NOT EXISTS `experiment` (
    `id` int NOT NULL AUTO_INCREMENT,
    `title` varchar(255) NOT NULL,
    `description` text,
    `postscript` text,
    `infotext` text,
    `active` bit(1) NOT NULL DEFAULT b'0',
    `project` longblob NULL DEFAULT NULL,
    `gui_url` varchar(2000) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `experiment_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- `user` table definition

CREATE TABLE IF NOT EXISTS `user` (
    `id` int NOT NULL AUTO_INCREMENT,
    `username` varchar(255) NOT NULL,
    `role` varchar(255) NOT NULL DEFAULT 'PARTICIPANT',
    `language` varchar(255) NOT NULL DEFAULT 'ENGLISH',
    `password` varchar(255) NULL DEFAULT NULL,
    `secret` varchar(255) NULL DEFAULT NULL,
    `attempts` int NOT NULL DEFAULT '0',
    `active` bit(1) NOT NULL DEFAULT b'0',
    `email` varchar(255) DEFAULT NULL,
    `last_login` DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`id`),
    UNIQUE KEY `username` (`username`),
    UNIQUE KEY `secret` (`secret`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- block_event table definition

CREATE TABLE IF NOT EXISTS `block_event` (
    `id` int NOT NULL AUTO_INCREMENT,
    `user_id` int NOT NULL,
    `experiment_id` int NOT NULL,
    `date` DATETIME NOT NULL DEFAULT NOW(),
    `event_type` varchar(255) NOT NULL,
    `event` varchar(255) NOT NULL,
    `spritename` varchar(255) DEFAULT NULL,
    `metadata` varchar(255) DEFAULT NULL,
    `xml` text,
    `json` text,
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`),
    KEY `experiment_id` (`experiment_id`),
    CONSTRAINT `block_event_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `block_event_ibfk_2` FOREIGN KEY (`experiment_id`) REFERENCES `experiment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- click_event table definition

CREATE TABLE IF NOT EXISTS `click_event` (
    `id` int NOT NULL AUTO_INCREMENT,
    `user_id` int NOT NULL,
    `experiment_id` int NOT NULL,
    `date` DATETIME NOT NULL DEFAULT NOW(),
    `event_type` varchar(255) NOT NULL,
    `event` varchar(255) NOT NULL,
    `metadata` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`),
    KEY `experiment_id` (`experiment_id`),
    CONSTRAINT `click_event_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `click_event_ibfk_2` FOREIGN KEY (`experiment_id`) REFERENCES `experiment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- debugger_event table definition

CREATE TABLE IF NOT EXISTS `debugger_event` (
    `id` int NOT NULL AUTO_INCREMENT,
    `user_id` int NOT NULL,
    `experiment_id` int NOT NULL,
    `date` DATETIME NOT NULL DEFAULT NOW(),
    `event_type` varchar(255) NOT NULL,
    `event` varchar(255) NOT NULL,
    `block_target_id` varchar(255) DEFAULT NULL,
    `name_opcode` varchar(255) DEFAULT NULL,
    `original` int DEFAULT NULL,
    `execution` int DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`),
    KEY `experiment_id` (`experiment_id`),
    CONSTRAINT `debugger_event_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `debugger_event_ibfk_2` FOREIGN KEY (`experiment_id`) REFERENCES `experiment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- question_event table definition

CREATE TABLE IF NOT EXISTS `question_event` (
    `id` int NOT NULL AUTO_INCREMENT,
    `user_id` int NOT NULL,
    `experiment_id` int NOT NULL,
    `date` DATETIME NOT NULL DEFAULT NOW(),
    `event_type` varchar(255) NOT NULL,
    `event` varchar(255) NOT NULL,
    `feedback` int DEFAULT NULL,
    `q_type` varchar(255) DEFAULT NULL,
    `q_values` varchar(255) DEFAULT NULL,
    `category` varchar(255) DEFAULT NULL,
    `form` varchar(255) DEFAULT NULL,
    `block_id` varchar(255) DEFAULT NULL,
    `opcode` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`),
    KEY `experiment_id` (`experiment_id`),
    CONSTRAINT `question_event_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `question_event_ibfk_2` FOREIGN KEY (`experiment_id`) REFERENCES `experiment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- file table definition

CREATE TABLE IF NOT EXISTS `file` (
    `id` int NOT NULL AUTO_INCREMENT,
    `user_id` int NOT NULL,
    `experiment_id` int NOT NULL,
    `date` DATETIME NOT NULL DEFAULT NOW(),
    `name` varchar(255) NOT NULL,
    `type` varchar(255) NOT NULL,
    `content` longblob NOT NULL,
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`),
    KEY `experiment_id` (`experiment_id`),
    CONSTRAINT `file_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `file_ibfk_2` FOREIGN KEY (`experiment_id`) REFERENCES `experiment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- participant table definition

CREATE TABLE IF NOT EXISTS `participant` (
    `user_id` int NOT NULL,
    `experiment_id` int NOT NULL,
    `start` DATETIME NULL DEFAULT NULL,
    `finish` DATETIME NULL DEFAULT NULL,
    PRIMARY KEY (`user_id`,`experiment_id`),
    KEY `experiment_id` (`experiment_id`),
    CONSTRAINT `participant_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `participant_ibfk_2` FOREIGN KEY (`experiment_id`) REFERENCES `experiment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- resource_event table definition

CREATE TABLE IF NOT EXISTS `resource_event` (
    `id` int NOT NULL AUTO_INCREMENT,
    `user_id` int NOT NULL,
    `experiment_id` int NOT NULL,
    `date` DATETIME NOT NULL DEFAULT NOW(),
    `event_type` varchar(255) NOT NULL,
    `event` varchar(255) NOT NULL,
    `name` varchar(255) DEFAULT NULL,
    `md5` varchar(255) DEFAULT NULL,
    `type` varchar(255) DEFAULT NULL,
    `library` int DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`),
    KEY `experiment_id` (`experiment_id`),
    CONSTRAINT `resource_event_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `resource_event_ibfk_2` FOREIGN KEY (`experiment_id`) REFERENCES `experiment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- token table definition

CREATE TABLE IF NOT EXISTS `token` (
    `value` varchar(255) NOT NULL,
    `type` varchar(255) NOT NULL,
    `expiration` DATETIME NOT NULL DEFAULT NOW(),
    `metadata` varchar(255) DEFAULT NULL,
    `user_id` int NOT NULL,
    PRIMARY KEY (`value`),
    KEY `user_id` (`user_id`),
    CONSTRAINT `token_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- sb3_zip table definition

CREATE TABLE IF NOT EXISTS `sb3_zip` (
    `id` int NOT NULL AUTO_INCREMENT,
    `user_id` int NOT NULL,
    `experiment_id` int NOT NULL,
    `date` DATETIME NOT NULL DEFAULT NOW(),
    `name` varchar(255) NOT NULL,
    `content` longblob NOT NULL,
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`),
    KEY `experiment_id` (`experiment_id`),
    CONSTRAINT `sb3_zip_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `sb3_zip_ibfk_2` FOREIGN KEY (`experiment_id`) REFERENCES `experiment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- course table definition

CREATE TABLE IF NOT EXISTS `course` (
    `id` int NOT NULL AUTO_INCREMENT,
    `title` varchar(255) NOT NULL,
    `description` text,
    `content` text,
    `active` bit(1) NOT NULL DEFAULT b'0',
    `last_changed` DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`id`),
    UNIQUE KEY `course_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- course_participant table definition

CREATE TABLE IF NOT EXISTS `course_participant` (
    `user_id` int NOT NULL,
    `course_id` int NOT NULL,
    `added` DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`user_id`,`course_id`),
    KEY `course_id` (`course_id`),
    CONSTRAINT `course_participant_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `course_participant_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- course_experiment table definition

CREATE TABLE IF NOT EXISTS `course_experiment` (
    `course_id` int NOT NULL,
    `experiment_id` int NOT NULL,
    `added` DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`course_id`,`experiment_id`),
    UNIQUE KEY `experiment_id` (`experiment_id`),
    CONSTRAINT `course_experiment_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
    CONSTRAINT `course_experiment_ibfk_2` FOREIGN KEY (`experiment_id`) REFERENCES `experiment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/**************************
 *  Definition of views.  *
 **************************/

-- user_num_block_events view source

CREATE OR REPLACE VIEW `user_num_block_events` (`user`, `experiment`, `count`, `event`) AS
select
    `b`.`user_id` AS `user_id`,
    `b`.`experiment_id` AS `experiment_id`,
    count(`b`.`event`) AS `COUNT(b.event)`,
    `b`.`event` AS `event`
from
    `block_event` `b`
group by
    `b`.`user_id`,
    `b`.`experiment_id`,
    `b`.`event`;


-- user_num_click_events view source

CREATE OR REPLACE VIEW `user_num_click_events` (`user`, `experiment`, `count`, `event`) AS
select
    `b`.`user_id` AS `user_id`,
    `b`.`experiment_id` AS `experiment_id`,
    count(`b`.`event`) AS `COUNT(b.event)`,
    `b`.`event` AS `event`
from
    `click_event` `b`
group by
    `b`.`user_id`,
    `b`.`experiment_id`,
    `b`.`event`;


-- user_num_resource_events view source

CREATE OR REPLACE VIEW `user_num_resource_events` (`user`, `experiment`, `count`, `event`) AS
select
    `r`.`user_id` AS `user_id`,
    `r`.`experiment_id` AS `experiment_id`,
    count(`r`.`event`) AS `COUNT(r.event)`,
    `r`.`event` AS `event`
from
    `resource_event` `r`
group by
    `r`.`user_id`,
    `r`.`experiment_id`,
    `r`.`event`;

-- experiment_data view source

CREATE OR REPLACE VIEW `experiment_data` (`experiment`, `participants`, `started`, `finished`) AS
select
    `p`.`experiment_id` AS `experiment_id`,
    count(`p`.`user_id`) AS `COUNT(p.user_id)`,
    count(`p`.`start`) AS `COUNT(p.start)`,
    count(`p`.`finish`) AS `COUNT(p.finish)`
from
    (`experiment` `e`
        join `participant` `p`)
where
    (`p`.`experiment_id` = `e`.`id`)
group by
    `p`.`experiment_id`;

-- codes_data view source

CREATE OR REPLACE VIEW `codes_data` (`user`, `experiment`, `count`) AS
select
    `b`.`user_id` AS `user_id`,
    `b`.`experiment_id` AS `experiment_id`,
    count(`b`.`xml`) AS `COUNT(b.xml)`
from
    `block_event` `b`
where
    (`b`.`xml` is not null)
group by
    `b`.`user_id`,
    `b`.`experiment_id`;
