-- Esquema inicial de sonidovivo (Usuario). Generado a partir de las entidades JPA con el dialecto MySQL.
-- Usa CREATE TABLE IF NOT EXISTS para convivir con bases creadas antes por Hibernate (ddl-auto: update).

create table if not exists usuarios (
    bloqueado bit not null,
    id bigint not null auto_increment,
    rol_id bigint not null,
    run varchar(12) not null,
    comuna varchar(80),
    region varchar(80),
    apellidos varchar(100),
    correo varchar(100) not null,
    nombre varchar(100) not null,
    direccion varchar(200),
    contrasena varchar(255) not null,
    primary key (id),
    constraint UKksyidfr5g3rl0b04glr24fxjn unique (run),
    constraint UKcdmw5hxlfj78uf4997i3qyyw5 unique (correo)
) engine=InnoDB;
