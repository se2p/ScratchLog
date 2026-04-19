create table example_solution (
    id int not null auto_increment,
    experiment_id int not null,
    filename varchar(255),
    sb3_project longblob,
    primary key (id),
    foreign key (experiment_id) references experiment (id) on delete cascade
);
