package com.banquito.paymentprocessor.procesatransaccion.banquito.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI openAPI() {
        Server devServer = new Server()
                .url("http://localhost:8081")
                .description("Servidor de Desarrollo");
                
        Server prodServer = new Server()
                .url("https://api.banquito.com/procesatransaccion")
                .description("Servidor de Producción");

        Contact contact = new Contact()
                .name("Banquito - Equipo de Procesamiento de Pagos")
                .url("https://banquito.com")
                .email("pagos@banquito.com");

        License license = new License()
                .name("Apache 2.0")
                .url("http://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
                .title("API de Procesamiento de Transacciones")
                .version("1.0")
                .contact(contact)
                .description("API del procesador de transacciones para el sistema de pagos Banquito. " +
                        "Permite recibir transacciones desde el gateway, validarlas y procesarlas, " +
                        "actualizando su estado y registrando un historial de cambios.")
                .license(license);

        List<Tag> tags = Arrays.asList(
                new Tag().name("Transacciones").description("Operaciones relacionadas con el procesamiento de transacciones"),
                new Tag().name("Gateway").description("Operaciones relacionadas con la configuración de gateways"),
                new Tag().name("Historial").description("Operaciones para consulta de historial de estados de transacciones")
        );

        Components components = new Components()
                .addSecuritySchemes("api_key", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-KEY")
                        .description("Clave API para autorización"));

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer))
                .tags(tags)
                .components(components);
    }
} 