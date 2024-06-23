# Kibernate

## Test DB setup 

Spin up Postgres:
```shell
podman run --name kibernate -e POSTGRES_PASSWORD=postgress -p 5432:5432 -d postgres
```

Test data:
```postgresql
create table Users
(
    id   bigserial primary key not null,
    name text,
    age  integer
);

insert into Users (name, age)
values ('Joe', 15),
       ('Alice', 21),
       ('Bob', 25),
       ('Mark', 18);

```

