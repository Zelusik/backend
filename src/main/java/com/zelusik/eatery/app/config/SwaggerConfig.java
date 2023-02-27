package com.zelusik.eatery.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI eateryApi(@Value("eatery.app.version") String appVersion) {
        // TODO: 로그인 기능 구현 후 Swagger에서 access-token을 header에 첨부할 수 있도록 security scheme component 추가 필요.
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Eatery API Docs")
                                .description("CMC 12th 앱 프로젝트 Eatery의 API 명세서")
                                .version(appVersion)
                )
                .externalDocs(
                        new ExternalDocumentation()
                                .description("Github organization of team zelusik")
                                .url("https://github.com/Zelusik")
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "access-token",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("Bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }
}
