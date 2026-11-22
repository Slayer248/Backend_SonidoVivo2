-- Esquema inicial de sonidovivo (Venta). Generado a partir de las entidades JPA con el dialecto MySQL.
-- Usa CREATE TABLE IF NOT EXISTS para convivir con bases creadas antes por Hibernate (ddl-auto: update).

create table if not exists ventas (
    iva integer not null,
    neto integer not null,
    tarjeta_ultimos4 varchar(4),
    total integer not null,
    fecha datetime(6) not null,
    id bigint not null auto_increment,
    rut varchar(12),
    numero_boleta varchar(20),
    pedido_codigo varchar(20),
    email varchar(100),
    cliente_nombre varchar(150) not null,
    motivo varchar(255),
    estado_pago enum ('APROBADO','RECHAZADO') not null,
    primary key (id),
    constraint UK3sn1venr8k1j2i1ffufowfm68 unique (numero_boleta)
) engine=InnoDB;
