package cl.sonidovivo.venta.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // URL relativa: desde el Swagger del Gateway (puerto 8080) las peticiones pasan por el Gateway,
                // y desde el Swagger propio del servicio van directo al servicio
                .servers(List.of(new Server().url("/").description("Servidor actual")))
                // Botón "Authorize": el token JWT del login se envía como Authorization: Bearer <token>
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                        .description("Pega aquí el token que devuelve POST /api/usuarios/login (sin la palabra Bearer)")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title("API Microservicio de Ventas (checkout y boletas) - Tienda Sonido Vivo")
                        .version("1.0")
                        .description("Documentación de endpoints de Microservicio de Ventas (checkout y boletas)."));
    }
}
