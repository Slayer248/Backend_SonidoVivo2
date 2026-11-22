-- Esquema inicial de sonidovivo (Pedido). Generado a partir de las entidades JPA con el dialecto MySQL.
-- Usa CREATE TABLE IF NOT EXISTS para convivir con bases creadas antes por Hibernate (ddl-auto: update).

create table if not exists pedidos (
    fecha date not null,
    iva integer,
    neto integer,
    total integer not null,
    id bigint not null auto_increment,
    punto_retiro_id bigint,
    usuario_id bigint,
    rut varchar(12),
    codigo varchar(20),
    email varchar(100),
    cliente_nombre varchar(150) not null,
    direccion varchar(200),
    estado enum ('DESPACHADO','ENTREGADO','EN_PREPARACION') not null,
    primary key (id),
    constraint UKopykg0avndf87lga45wx7l02v unique (codigo)
) engine=InnoDB;

create table if not exists pedido_items (
    cantidad integer not null,
    precio integer not null,
    pedido_id bigint not null,
    producto_id bigint not null,
    nombre varchar(150) not null,
    img varchar(500),
    constraint FKnchocvgm3pbl25qxityq2whip foreign key (pedido_id) references pedidos (id)
) engine=InnoDB;
