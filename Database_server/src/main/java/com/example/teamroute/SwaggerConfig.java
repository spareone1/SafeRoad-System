package com.example.teamroute; 
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TeamRoute API 명세서")
                        .description("캡스톤 디자인 TeamRoute 프로젝트 API 명세입니다.")
                        .version("1.0.0"));
    }
}