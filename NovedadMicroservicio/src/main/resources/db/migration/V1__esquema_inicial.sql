-- Esquema inicial de sonidovivo (Novedad). Generado a partir de las entidades JPA con el dialecto MySQL.
-- Usa CREATE TABLE IF NOT EXISTS para convivir con bases creadas antes por Hibernate (ddl-auto: update).

create table if not exists novedades (
    fecha date not null,
    id bigint not null auto_increment,
    categoria varchar(50) not null,
    autor varchar(100) not null,
    titulo varchar(200) not null,
    imagen varchar(500),
    resumen varchar(500) not null,
    descripcion_completa TEXT not null,
    primary key (id)
) engine=InnoDB;
