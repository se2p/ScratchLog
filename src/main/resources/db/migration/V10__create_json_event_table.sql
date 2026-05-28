-- json_event table definition

CREATE TABLE IF NOT EXISTS `json_event` (
    `id` int NOT NULL AUTO_INCREMENT,
    `user_id` int NOT NULL,
    `experiment_id` int NOT NULL,
    `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `event_type` varchar(255) NOT NULL,
    `event` varchar(255) NOT NULL,
    `name` varchar(255) DEFAULT NULL,
    `content` JSON NOT NULL,
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`),
    KEY `experiment_id` (`experiment_id`),
    CONSTRAINT `json_event_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `json_event_ibfk_2` FOREIGN KEY (`experiment_id`) REFERENCES `experiment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- user_num_json_events view source

CREATE OR REPLACE VIEW `user_num_json_events` (`user`, `experiment`, `count`, `event`) AS
select
    `r`.`user_id` AS `user_id`,
    `r`.`experiment_id` AS `experiment_id`,
    count(`r`.`event`) AS `COUNT(r.event)`,
    `r`.`event` AS `event`
from
    `json_event` `r`
group by
    `r`.`user_id`,
    `r`.`experiment_id`,
    `r`.`event`;
