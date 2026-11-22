-- Marca las cuentas que todavía usan la contraseña temporal enviada por correo.
-- Las cuentas existentes quedan en 0 (no se les obliga a cambiarla).
alter table usuarios add column debe_cambiar_contrasena bit not null default 0;
