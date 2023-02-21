-- scratch1984.course definition

CREATE TABLE IF NOT EXISTS `course` (
    `id` int NOT NULL AUTO_INCREMENT,
    `title` varchar(255) NOT NULL,
    `description` text,
    `content` text,
    `active` bit(1) NOT NULL DEFAULT b'0',
    `last_changed` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `course_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- scratch1984.course_participant definition

CREATE TABLE IF NOT EXISTS `course_participant` (
    `user_id` int NOT NULL,
    `course_id` int NOT NULL,
    `added` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`,`course_id`),
    KEY `course_id` (`course_id`),
    CONSTRAINT `course_participant_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `course_participant_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- scratch1984.course_experiment definition

CREATE TABLE IF NOT EXISTS `course_experiment` (
    `course_id` int NOT NULL,
    `experiment_id` int NOT NULL,
    `added` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`course_id`,`experiment_id`),
    UNIQUE KEY `experiment_id` (`experiment_id`),
    CONSTRAINT `course_experiment_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
    CONSTRAINT `course_experiment_ibfk_2` FOREIGN KEY (`experiment_id`) REFERENCES `experiment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
