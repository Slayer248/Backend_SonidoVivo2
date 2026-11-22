package cl.sonidovivo.producto.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Microservicio de Productos del catálogo - Tienda Sonido Vivo")
                        .version("1.0")
                        .description("Documentación de endpoints de Microservicio de Productos del catálogo."));
    }
}
