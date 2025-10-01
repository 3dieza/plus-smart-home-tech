create table if not exists device
(
    id     varchar(64) primary key,
    hub_id varchar(64) not null,
    type   varchar(64) not null
);

create table if not exists scenario
(
    id     bigserial primary key,
    hub_id varchar(64)  not null,
    name   varchar(255) not null
);

create table if not exists condition
(
    id          bigserial primary key,
    scenario_id bigint      not null references scenario (id) on delete cascade,
    sensor_id   varchar(64) not null,
    type        varchar(32) not null,
    operation   varchar(32) not null,
    int_value   int,
    bool_value  boolean
);

create table if not exists action
(
    id          bigserial primary key,
    scenario_id bigint      not null references scenario (id) on delete cascade,
    sensor_id   varchar(64) not null,
    type        varchar(32) not null,
    value       int
);

create index if not exists idx_scenario_hub on scenario (hub_id);
create index if not exists idx_device_hub on device (hub_id);