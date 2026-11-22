-- Esquema inicial de sonidovivo (Rol). Generado a partir de las entidades JPA con el dialecto MySQL.
-- Usa CREATE TABLE IF NOT EXISTS para convivir con bases creadas antes por Hibernate (ddl-auto: update).

create table if not exists roles (
    id bigint not null auto_increment,
    nombre varchar(30) not null,
    primary key (id),
    constraint UKldv0v52e0udsh2h1rs0r0gw1n unique (nombre)
) engine=InnoDB;
