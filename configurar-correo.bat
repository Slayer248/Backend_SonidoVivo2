@echo off
chcp 65001 >nul
REM Crea correo.local.properties (junto a este archivo) para que Usuario y Venta envíen correos.
REM Ese archivo está en .gitignore: las credenciales nunca se suben a GitHub.
cd /d "%~dp0"
setlocal

echo ============================================================
echo   Configurar el correo de Sonido Vivo
echo ============================================================
echo.
echo   1) Gmail: los correos llegan de verdad a la bandeja de entrada.
echo      Necesitas una cuenta de Gmail con verificacion en 2 pasos
echo      y una "contraseña de aplicacion" de 16 letras.
echo.
echo   2) Mailpit de Laragon: los correos NO salen a internet; se ven en
echo      http://localhost:8025 (Mailpit debe estar encendido en Laragon).
echo.
set "OPCION="
set /p "OPCION=Elige 1 o 2: "

if exist correo.local.properties (
    echo.
    set "SOBRESCRIBIR="
    set /p "SOBRESCRIBIR=Ya existe correo.local.properties. ¿Reemplazarlo? [s/n]: "
)
if exist correo.local.properties if /i not "%SOBRESCRIBIR%"=="s" (
    echo No se hicieron cambios.
    goto :fin
)

if "%OPCION%"=="1" goto :gmail
if "%OPCION%"=="2" goto :mailpit
echo Opcion no valida. No se hicieron cambios.
goto :fin

:gmail
echo.
set "CORREO="
set "CLAVE="
set /p "CORREO=Correo de Gmail de la tienda: "
set /p "CLAVE=Contraseña de aplicacion (con o sin espacios): "
if "%CORREO%"=="" goto :faltan
if "%CLAVE%"=="" goto :faltan
set "CLAVE=%CLAVE: =%"
(
    echo # Generado por configurar-correo.bat - NO subir a GitHub
    echo spring.mail.username=%CORREO%
    echo spring.mail.password=%CLAVE%
) > correo.local.properties
goto :listo

:mailpit
(
    echo # Generado por configurar-correo.bat - correos locales con Mailpit de Laragon
    echo spring.mail.host=localhost
    echo spring.mail.port=1025
    echo spring.mail.username=tienda@sonidovivo.cl
    echo spring.mail.password=
    echo spring.mail.properties.mail.smtp.auth=false
    echo spring.mail.properties.mail.smtp.starttls.enable=false
) > correo.local.properties
echo.
echo Los correos se veran en http://localhost:8025
goto :listo

:faltan
echo Faltan datos. No se hicieron cambios.
goto :fin

:listo
echo.
echo Listo: se creo correo.local.properties
echo Reinicia los microservicios Usuario y Venta para que tomen la configuracion.

:fin
echo.
pause
endlocal
