-- Esquema inicial de sonidovivo (PuntoRetiro). Generado a partir de las entidades JPA con el dialecto MySQL.
-- Usa CREATE TABLE IF NOT EXISTS para convivir con bases creadas antes por Hibernate (ddl-auto: update).

create table if not exists puntos_retiro (
    lat float(53) not null,
    lng float(53) not null,
    principal bit not null,
    id bigint not null auto_increment,
    tipo varchar(50) not null,
    horario varchar(80) not null,
    nombre varchar(120) not null,
    direccion varchar(200) not null,
    primary key (id)
) engine=InnoDB;
