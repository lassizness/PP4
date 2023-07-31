package com.itm.space.backendresources.configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme( //конфигурация для определелния схемы безопасности Эта конфигурация используется для аутентификации с помощью протокола OAuth 2.0.
        name = "oauth2_auth_code", // имя схемы безопасности как "oauth2_auth_code".
        type = SecuritySchemeType.OAUTH2, //тип схемы безопасности - OAuth 2.0.
        flows = @OAuthFlows( //потоки OAuth для схемы безопасности
                authorizationCode = @OAuthFlow(
                        authorizationUrl = "http://backend-keycloak-auth:8080/auth/realms/ITM/protocol/openid-connect/auth", //Указывает URL, где пользователь может авторизовать приложение
                        tokenUrl = "http://backend-keycloak-auth:8080/auth/realms/ITM/protocol/openid-connect/token", //  URL, где приложение может получить токен доступа
                        scopes = {
                                @OAuthScope(name = "openid", description = "Read access") //Указывает области, которые приложение может запрашивать.
                        }
                )
        ),
        in = SecuritySchemeIn.HEADER //указывает, что схема безопасности применяется в заголовке запроса.
)
public class OpenApiConfiguration {

    @Bean
    public OpenAPI publicApi() {
        return new OpenAPI();
    }
}
