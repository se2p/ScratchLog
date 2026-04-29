create table test_suite (
    id int not null auto_increment,
    experiment_id int not null unique,
    filename varchar(255),
    test_implementation longtext not null,
    primary key (id),
    foreign key (experiment_id) references experiment (id) on delete cascade
);

create table test_case (
    id int not null auto_increment,
    test_suite_id int not null,
    name varchar(255) not null,
    primary key (id),
    foreign key (test_suite_id) references test_suite (id) on delete cascade
);

create table test_result (
    id int not null auto_increment,
    test_case_id int not null,
    project_id int not null,
    result enum('PASS', 'FAIL', 'ERROR', 'SKIP'),
    primary key (id),
    foreign key (test_case_id) references test_case (id) on delete cascade,
    foreign key (project_id) references block_event (id) on delete cascade
);
