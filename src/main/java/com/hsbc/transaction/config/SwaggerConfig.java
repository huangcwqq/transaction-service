package com.hsbc.transaction.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("银行交易管理API文档")
                        .description("提供交易的增删改查等操作接口")
                        .version("v1.0.0")
                        .contact(new Contact().name("HSBC Dev Team").email("dev@hsbc.com"))
                )
                .externalDocs(new ExternalDocumentation()
                        .description("项目文档")
                        .url("https://github.com/huangcwqq/transaction-service.git"));
    }
}

