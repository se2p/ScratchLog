/**************************
 *  Definition of tables.  *
 **************************/

-- scratch1984.experiment definition

CREATE TABLE IF NOT EXISTS `experiment` (
                              `id` int NOT NULL AUTO_INCREMENT,
                              `name` varchar(255) NOT NULL,
                              `description` text,
                              `infotext` text,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- scratch1984.`user` definition

CREATE TABLE IF NOT EXISTS `user` (
                        `id` int NOT NULL AUTO_INCREMENT,
                        `username` varchar(255) NOT NULL,
                        `role` varchar(255) NOT NULL DEFAULT 'PARTICIPANT',
                        `salt` varchar(255) NULL DEFAULT NULL,
                        `password` varchar(255) NULL DEFAULT NULL,
                        `secret` varchar(255) NOT NULL,
                        `reset_password` bit(1) NOT NULL DEFAULT b'0',
                        `active` bit(1) NOT NULL DEFAULT b'0',
                        `email` varchar(255) DEFAULT NULL,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `username` (`username`),
                        UNIQUE KEY `secret` (`secret`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- scratch1984.block_event definition

CREATE TABLE IF NOT EXISTS `block_event` (
                               `id` int NOT NULL AUTO_INCREMENT,
                               `user_id` int NOT NULL,
                               `experiment_id` int NOT NULL,
                               `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               `event_type` varchar(255) NOT NULL,
                               `event` varchar(255) NOT NULL,
                               `spritename` varchar(255) NOT NULL,
                               `metadata` varchar(255) DEFAULT NULL,
                               `xml` text,
                               `json` text,
                               PRIMARY KEY (`id`),
                               KEY `user_id` (`user_id`),
                               KEY `experiment_id` (`experiment_id`),
                               CONSTRAINT `block_event_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
                               CONSTRAINT `block_event_ibfk_2` FOREIGN KEY (`experiment_id`) REFERENCES `experiment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- scratch1984.click_event definition

CREATE TABLE IF NOT EXISTS `click_event` (
                               `id` int NOT NULL AUTO_INCREMENT,
                               `user_id` int NOT NULL,
                               `experiment_id` int NOT NULL,
                               `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               `event_type` varchar(255) NOT NULL,
                               `event` varchar(255) NOT NULL,
                               `x_or_key` varchar(255) NOT NULL,
                               `y_or_code` varchar(255) NOT NULL,
                               `class` varchar(255) DEFAULT NULL,
                               `node_name` varchar(255) DEFAULT NULL,
                               `button` varchar(255) DEFAULT NULL,
                               `data_id` varchar(255) DEFAULT NULL,
                               PRIMARY KEY (`id`),
                               KEY `user_id` (`user_id`),
                               KEY `experiment_id` (`experiment_id`),
                               CONSTRAINT `click_event_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
                               CONSTRAINT `click_event_ibfk_2` FOREIGN KEY (`experiment_id`) REFERENCES `experiment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- scratch1984.file definition

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- scratch1984.participant definition

CREATE TABLE IF NOT EXISTS `participant` (
                               `user_id` int NOT NULL,
                               `experiment_id` int NOT NULL,
                               `start` timestamp NULL DEFAULT NULL,
                               `finish` timestamp NULL DEFAULT NULL,
                               PRIMARY KEY (`user_id`,`experiment_id`),
                               KEY `experiment_id` (`experiment_id`),
                               CONSTRAINT `participant_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
                               CONSTRAINT `participant_ibfk_2` FOREIGN KEY (`experiment_id`) REFERENCES `experiment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- scratch1984.resource_event definition

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/**************************
 *  Definition of views.  *
 **************************/

-- scratch1984.user_num_block_events source

CREATE OR REPLACE VIEW `scratch1984`.`user_num_block_events` (`user`, `experiment`, `count`, `event`) AS
select
    `b`.`user_id` AS `user_id`,
    `b`.`experiment_id` AS `experiment_id`,
    count(`b`.`event`) AS `COUNT(b.event)`,
    `b`.`event` AS `event`
from
    `scratch1984`.`block_event` `b`
group by
    `b`.`user_id`,
    `b`.`experiment_id`,
    `b`.`event`;


-- scratch1984.user_num_click_events source

CREATE OR REPLACE VIEW `scratch1984`.`user_num_click_events` (`user`, `experiment`, `count`, `event`) AS
select
    `c`.`user_id` AS `user_id`,
    `c`.`experiment_id` AS `experiment_id`,
    count(`c`.`event`) AS `COUNT(c.event)`,
    `c`.`event` AS `event`
from
    `scratch1984`.`click_event` `c`
group by
    `c`.`user_id`,
    `c`.`experiment_id`,
    `c`.`event`;


-- scratch1984.user_num_resource_events source

CREATE OR REPLACE VIEW `scratch1984`.`user_num_resource_events` (`user`, `experiment`, `count`, `event`) AS
select
    `r`.`user_id` AS `user_id`,
    `r`.`experiment_id` AS `experiment_id`,
    count(`r`.`event`) AS `COUNT(r.event)`,
    `r`.`event` AS `event`
from
    `scratch1984`.`resource_event` `r`
group by
    `r`.`user_id`,
    `r`.`experiment_id`,
    `r`.`event`;