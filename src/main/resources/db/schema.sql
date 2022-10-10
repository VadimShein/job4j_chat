create table if not exists messages(
    id serial primary key,
    text text,
    created timestamp,
    room_id int,
    author_id int
);

create table if not exists roles(
    id serial primary key,
    role_name varchar(255)
);

create table if not exists persons(
    id serial primary key,
    username varchar(255),
    password varchar(255),
    role_id int references roles(id)
);

create table if not exists rooms(
    id serial primary key,
    name varchar(255),
    description varchar(255),
    created timestamp
);
