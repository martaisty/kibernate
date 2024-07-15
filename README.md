# Kibernate

## Test DB setup 

Spin up Postgres:
```shell
podman run --name kibernate -e POSTGRES_PASSWORD=postgress -p 5432:5432 -d postgres
```

Test data:
```postgresql
create table users
(
    id   bigserial primary key not null,
    name text,
    age  integer
);

insert into users (name, age)
values ('Joe', 15),
       ('Alice', 21),
       ('Bob', 25),
       ('Mark', 18);

```

```postgresql
create table skills
(
    id      bigserial primary key not null,
    name    text,
    user_id bigint references users (id)
);

insert into skills(name, user_id)
values ('Guitar', (select u.id from users u where u.name = 'Joe')),
       ('Dancing', (select u.id from users u where u.name = 'Joe')),
       ('Singing', (select u.id from users u where u.name = 'Alice')),
       ('Powerlifting', (select u.id from users u where u.name = 'Alice')),
       ('Swimming', (select u.id from users u where u.name = 'Bob'));
```

