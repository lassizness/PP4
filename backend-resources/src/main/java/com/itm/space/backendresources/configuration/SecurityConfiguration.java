package com.itm.space.backendresources.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity // включает поддержку безопасности веб-приложения.
@EnableMethodSecurity(securedEnabled = true) //включает поддержку безопасности методов, позволяя использовать аннотацию @Secured для защиты методов.
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { //Метод будет использоваться для настройки цепочки фильтров безопасности.
        http
                .csrf(AbstractHttpConfigurer::disable) //Этот вызов отключает защиту от CSRF атак, отключая конфигуратор CSRF.
                .authorizeHttpRequests(requests -> requests
                        .anyRequest().permitAll()) //Этот вызов настраивает авторизацию для всех HTTP-запросов, разрешая доступ для любого запроса.
                .oauth2ResourceServer()//Этот вызов настраивает сервер ресурсов OAuth 2.0.
                .jwt() // будет использоваться JWT (JSON Web Token) для аутентификации.
                .jwtAuthenticationConverter(SecurityConfiguration::convertJwtToken); //указывает, какой конвертер будет использоваться для преобразования JWT-токена в объект аутентификации.
        return http.build(); //Этот вызов завершает настройку объекта HttpSecurity и возвращает объект SecurityFilterChain,
        // который будет использоваться для обработки запросов безопасности.
    }

    private static JwtAuthenticationToken convertJwtToken(Jwt jwt) { //Метод будет использоваться для преобразования JWT-токена в объект аутентификации.
        Collection<GrantedAuthority> authorities = new ArrayList<>(); //Создается коллекция authorities, которая будет содержать разрешения (роли) пользователя.
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt, authorities);//Создается объект JwtAuthenticationToken с переданным JWT-токеном и коллекцией разрешений.
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access"); //Получается значение из JWT-токена для ключа "realm_access" и сохраняется в переменной realmAccess в виде карты (Map).
        List<String> roles = (List<String>) realmAccess.get("roles"); //Получается значение из карты realmAccess для ключа "roles" и сохраняется в переменной roles в виде списка строк.
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role)); //проходится по каждой роли в списке roles и создается объект SimpleGrantedAuthority с префиксом "ROLE_" и значением роли. Затем этот объект добавляется в коллекцию authorities.
        }
        return new JwtAuthenticationToken(jwt, authorities, authenticationToken.getName()); //Создается и возвращается новый объект JwtAuthenticationToken с переданным JWT-токеном, коллекцией разрешений и именем аутентификации.
    }
}
