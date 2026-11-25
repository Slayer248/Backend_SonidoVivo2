# Backend Tienda Sonido Vivo (microservicios)

Spring Boot 4.1.2 · Java 21 · MySQL (Laragon) · Flyway · Eureka · Spring Cloud Gateway · Swagger.

## Servicios y puertos

| Carpeta | Puerto | Base de datos | Ruta en el Gateway |
|---|---|---|---|
| EurekaMicroservicio | 8761 | - | http://localhost:8761 |
| GatewayMicroservicio | 8080 | - | punto de entrada del front |
| RolMicroservicio | 8081 | sonidovivo_roles | /api/roles |
| UsuarioMicroservicio | 8082 | sonidovivo_usuarios | /api/usuarios |
| ProductoMicroservicio | 8083 | sonidovivo_productos | /api/productos |
| ServicioMicroservicio | 8084 | sonidovivo_servicios | /api/servicios y /api/servicios/contacto |
| NovedadMicroservicio | 8085 | sonidovivo_novedades | /api/novedades |
| PuntoRetiroMicroservicio | 8086 | sonidovivo_puntos_retiro | /api/puntos-retiro |
| PedidoMicroservicio | 8087 | sonidovivo_pedidos | /api/pedidos |
| VentaMicroservicio | 8088 | sonidovivo_ventas | /api/ventas |

Las bases se crean solas (`createDatabaseIfNotExist`) con usuario `root` y clave vacía, igual que Laragon por defecto.
Cada servicio carga datos iniciales al arrancar si su tabla está vacía (los mismos del front).

## Migraciones de base de datos (Flyway)

El esquema de cada base lo crea y versiona Flyway; Hibernate solo lo **valida** (`ddl-auto: validate`). Si una entidad
no coincide con las tablas, el servicio no arranca y dice qué columna falta.

| Archivo (en `src/main/resources/db/migration` de cada servicio) | Qué hace |
|---|---|
| `V1__esquema_inicial.sql` | Crea las tablas con `CREATE TABLE IF NOT EXISTS` (generado desde las entidades con dialecto MySQL) |
| `V2__completar_columnas_previas.java` (solo Servicio y Pedido) | Agrega columnas que podían faltar en bases creadas antes de Flyway (`consultas`: `usuario_id`, `respuesta`, `fecha_respuesta`; `pedidos`: `usuario_id`) |

- **Base nueva:** Flyway aplica V1 y V2 y queda registrado en la tabla `flyway_schema_history`.
- **Base que ya existía** (creada por Hibernate): se toma como punto de partida (`baseline-on-migrate`, versión 0), V1 no toca nada y V2 solo agrega lo que falte. No se pierden datos.
- **Cambiar el esquema desde ahora:** modifica la entidad y agrega un archivo nuevo `V3__descripcion.sql` (nunca edites un V ya aplicado: Flyway detecta el cambio y falla).
- Para empezar de cero basta borrar la base en HeidiSQL; al arrancar el servicio se crea otra vez y los datos iniciales se vuelven a cargar.

## Cómo levantarlo

0. Instala el **JDK 21** y define la variable de entorno `JAVA_HOME` apuntando a su carpeta (la que contiene `bin`).
1. Enciende MySQL en Laragon.
2. Ejecuta `iniciar-todo.bat`, o levanta a mano con `mvnw.cmd spring-boot:run` en este orden:
   Eureka → Rol → Usuario / Producto / Servicio / Novedad / PuntoRetiro → Pedido → Venta → Gateway.
3. Swagger de todo el sistema: http://localhost:8080/swagger-ui.html

Rol debe estar arriba antes que Usuario (los usuarios iniciales se crean consultando a Rol).

## Comunicación entre servicios

- Usuario → Rol (valida y nombra el rol de cada usuario)
- Pedido → Producto (lee precio y descuenta stock; repone si el pedido falla) y → PuntoRetiro (valida el punto elegido)
- Venta → Producto (calcula totales) y → Pedido (crea el pedido al aprobar el pago)

## Cuentas de prueba

| Correo | Clave | Rol |
|---|---|---|
| admin@sonidovivo.cl | admin123 | Administrador |
| vendedor@sonidovivo.cl | vendedor123 | Vendedor |
| cliente@sonidovivo.cl | user123 | Cliente |

## Flujo de compra (checkout)

`POST /api/ventas/checkout` con `clienteNombre, rut, email, direccion, puntoRetiroId, numeroTarjeta, items:[{productoId, cantidad}]`.
Valida stock → simula el pago (tarjeta terminada en `0000` = rechazo, HTTP 402, como en el front) → crea el pedido (descuenta stock) → emite la boleta `B-000001`.
El precio lo toma el servidor desde Productos (IVA 19% incluido, se separa neto e IVA). Solo se guardan los últimos 4 dígitos de la tarjeta.

## Seguridad (JWT y roles)

El login (`POST /api/usuarios/login`) devuelve el usuario con un campo `token` (JWT, vence en 8 horas). El front lo envía en cada petición como `Authorization: Bearer <token>`. El Gateway lo valida y aplica las reglas antes de reenviar la petición, y entrega a los microservicios los encabezados `X-Usuario-Id`, `X-Usuario-Rol` y `X-Admin-Id` (los que mande el cliente se descartan).

Antes de que venza, el front lo renueva con `POST /api/usuarios/renovar-token` (enviando el token vigente): el servicio verifica la firma y el vencimiento, vuelve a leer la cuenta (así toma el rol actual y una cuenta bloqueada o con contraseña temporal no puede renovar) y entrega un token nuevo de 8 horas. Un token vencido no se puede renovar: hay que iniciar sesión otra vez.

| Ruta | Quién puede usarla |
|---|---|
| Login, registro, `GET` de productos, novedades, servicios y puntos de retiro, enviar consulta de contacto (si trae sesión, el mensaje queda asociado a su cuenta), Swagger | Público |
| `POST /api/ventas/checkout`, `GET /api/pedidos/mios` (los pedidos de la cuenta) y ver un pedido por código | Cualquier usuario con sesión. Un cliente solo ve pedidos propios (si no, 404); Administrador y Vendedor ven todos |
| Leer todas las consultas de contacto, responderlas, listado de todos los pedidos, crear pedidos directamente, cambiar estado de pedido, listado y resumen de ventas | Administrador y Vendedor |
| Crear, editar o borrar productos, novedades, servicios y puntos de retiro | Administrador |
| `/api/roles` y `/api/usuarios` (listar, crear, cambiar rol, bloquear) | Administrador |
| Renovar el propio token (`POST /api/usuarios/renovar-token`) | Cualquier usuario con sesión |
| Ver o editar la propia cuenta y cambiar la propia contraseña | Cada usuario (solo la suya; un no administrador no puede cambiar su rol) |

Sin token responde 401; con un rol sin permiso, 403.

Variables de entorno (opcionales; tienen valores para desarrollo):

| Variable | Dónde | Para qué |
|---|---|---|
| `JWT_SECRET` | Usuario **y** Gateway (mismo valor) | Clave con que se firman y validan los tokens. Cámbiala fuera de desarrollo |
| `CORS_ORIGINS` | Gateway | Orígenes permitidos, separados por coma. Por defecto `http://localhost:*` y `http://127.0.0.1:*` |

El dueño de cada pedido se guarda en `pedidos.usuario_id`: lo toma Ventas del encabezado `X-Usuario-Id` que pone el Gateway (nunca del cuerpo de la petición). Los pedidos anteriores a este cambio quedan sin dueño y no aparecen en "mis pedidos".

Los servicios internos (Pedido, Venta, Producto…) se llaman entre sí directamente y no pasan por el Gateway; por eso en producción deben quedar accesibles solo desde la red interna y exponerse únicamente el Gateway (puerto 8080).

## Pruebas

```bash
cd GatewayMicroservicio && mvnw.cmd test      # reglas de acceso y JWT
cd UsuarioMicroservicio && mvnw.cmd test      # generación del token
```

## Uso desde el front

- El front (`../front/TiendaSonidoVivo`) consume todo por el Gateway con `fetch`; ver su `README.md`.
- El usuario devuelto trae `rol` ("Administrador" | "Vendedor" | "Cliente"), igual que `UserRole`.
- Los estados de pedido viajan como en el front: "En Preparación", "Despachado", "Entregado".
- Las fechas de novedades llegan en formato ISO (`2026-09-05`) y el front las formatea.
- Los ids son numéricos; los pedidos se identifican por código (`PED-101`).

### Consultas de contacto y respuestas

- `POST /api/servicios/contacto`: público. Si la petición trae un token válido, el Gateway agrega `X-Usuario-Id` y la consulta guarda `consultas.usuario_id`.
- `PUT /api/servicios/contacto/{id}/respuesta` (Administrador y Vendedor): guarda la respuesta (`respuesta`, `fecha_respuesta`) y marca la consulta como atendida.
- `GET /api/servicios/contacto/mias` (cualquier usuario con sesión): devuelve las consultas enviadas con su sesión o con el correo de su cuenta, con sus respuestas. El correo lo toma el Gateway del token (`X-Usuario-Correo`).

## Correos y contraseña temporal

- **Boleta por correo:** al simular el pago, `VentaMicroservicio` envía la boleta al correo del cliente (en segundo plano; si falla, la compra no se ve afectada).
- **Contraseña generada:** al crear un perfil (registro público o creación por un administrador) `UsuarioMicroservicio` genera una contraseña temporal aleatoria, la guarda cifrada y la envía por correo. Si el correo no se puede enviar, responde 503 y no se crea la cuenta.
- **Cambio obligatorio:** el usuario queda con `debe_cambiar_contrasena = 1` (migración `V2__debe_cambiar_contrasena.sql`). El JWT lleva `cpw:true` y el Gateway solo permite `PUT /api/usuarios/{id}/contrasena` hasta que la cambie. Las cuentas existentes y las semilla no se fuerzan.
- **Credenciales de Gmail:** van en `correo.local.properties` (en esta carpeta, ignorado por git; ver `correo.local.properties.example`) o en las variables `MAIL_USERNAME` / `MAIL_PASSWORD`. Sin ellas, Usuario muestra la contraseña temporal en consola y Venta solo avisa.
- Tras actualizar: recargar Maven, reiniciar Usuario, Venta y Gateway.
