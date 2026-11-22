-- Esquema inicial de sonidovivo (Servicio). Generado a partir de las entidades JPA con el dialecto MySQL.
-- Usa CREATE TABLE IF NOT EXISTS para convivir con bases creadas antes por Hibernate (ddl-auto: update).

create table if not exists consultas (
    atendida bit not null,
    fecha datetime(6) not null,
    fecha_respuesta datetime(6),
    id bigint not null auto_increment,
    usuario_id bigint,
    email varchar(100) not null,
    nombre varchar(100) not null,
    mensaje varchar(1000) not null,
    respuesta varchar(1000),
    primary key (id)
) engine=InnoDB;

create table if not exists servicios (
    id bigint not null auto_increment,
    icono varchar(50) not null,
    titulo varchar(100) not null,
    texto varchar(500) not null,
    primary key (id)
) engine=InnoDB;
