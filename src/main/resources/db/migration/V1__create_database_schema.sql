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
    PRIMARY KEY (`id`),
    UNIQUE KEY `experiment_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- user table definition

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
    PRIMARY KEY (`id`),
    UNIQUE KEY `username` (`username`),
    UNIQUE KEY `secret` (`secret`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- block_event table definition

CREATE TABLE IF NOT EXISTS `block_event` (
    `id` int NOT NULL AUTO_INCREMENT,
    `user_id` int NOT NULL,
    `experiment_id` int NOT NULL,
    `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
    `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
    `start` timestamp NULL DEFAULT NULL,
    `finish` timestamp NULL DEFAULT NULL,
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
    `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
    `expiration` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
    `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `name` varchar(255) NOT NULL,
    `content` longblob NOT NULL,
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`),
    KEY `experiment_id` (`experiment_id`),
    CONSTRAINT `sb3_zip_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `sb3_zip_ibfk_2` FOREIGN KEY (`experiment_id`) REFERENCES `experiment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
