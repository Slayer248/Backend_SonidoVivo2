@echo off
REM Levanta todos los microservicios en el orden correcto (necesita Java 21 y MySQL de Laragon encendido).
cd /d "%~dp0"
REM Necesita Java 21: la variable de entorno JAVA_HOME debe apuntar a un JDK 21 (ver README).
start "Eureka" cmd /k "cd EurekaMicroservicio && mvnw.cmd spring-boot:run"
timeout /t 25 /nobreak >nul
start "Rol" cmd /k "cd RolMicroservicio && mvnw.cmd spring-boot:run"
timeout /t 20 /nobreak >nul
start "Usuario" cmd /k "cd UsuarioMicroservicio && mvnw.cmd spring-boot:run"
start "Producto" cmd /k "cd ProductoMicroservicio && mvnw.cmd spring-boot:run"
start "Servicio" cmd /k "cd ServicioMicroservicio && mvnw.cmd spring-boot:run"
start "Novedad" cmd /k "cd NovedadMicroservicio && mvnw.cmd spring-boot:run"
start "PuntoRetiro" cmd /k "cd PuntoRetiroMicroservicio && mvnw.cmd spring-boot:run"
timeout /t 25 /nobreak >nul
start "Pedido" cmd /k "cd PedidoMicroservicio && mvnw.cmd spring-boot:run"
timeout /t 15 /nobreak >nul
start "Venta" cmd /k "cd VentaMicroservicio && mvnw.cmd spring-boot:run"
start "Gateway" cmd /k "cd GatewayMicroservicio && mvnw.cmd spring-boot:run"
echo.
echo Eureka:  http://localhost:8761
echo Gateway: http://localhost:8080/swagger-ui.html
