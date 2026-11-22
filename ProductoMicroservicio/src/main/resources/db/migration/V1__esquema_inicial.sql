-- Esquema inicial de sonidovivo (Producto). Generado a partir de las entidades JPA con el dialecto MySQL.
-- Usa CREATE TABLE IF NOT EXISTS para convivir con bases creadas antes por Hibernate (ddl-auto: update).

create table if not exists productos (
    precio integer not null,
    stock integer not null,
    id bigint not null auto_increment,
    categoria varchar(50) not null,
    nombre varchar(150) not null,
    img varchar(500),
    primary key (id)
) engine=InnoDB;
