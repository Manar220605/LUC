package com.luc.qa.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI lucOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Lebanese University Connect API")
                .description("LUC backend REST API")
                .version("0.0.1"));
    }
}
