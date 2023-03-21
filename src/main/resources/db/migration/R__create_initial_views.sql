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
    `scratch1984`.`block_event` `b`
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
    `scratch1984`.`resource_event` `r`
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
