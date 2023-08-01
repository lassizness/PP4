package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor  //Эта аннотация создает конструктор, который принимает все final поля класса в качестве параметров. В данном случае, создается конструктор, который принимает объект UserService в качестве параметра.
public class UserController {
    private final UserService userService;

    @PostMapping
    @Secured("ROLE_MODERATOR")
    @SecurityRequirement(name = "oauth2_auth_code")
    public void create(@RequestBody @Valid UserRequest userRequest) {
        userService.createUser(userRequest);
    }

    @GetMapping("/{id}") //метод getUserById будет обрабатывать HTTP GET запросы с переменной пути "id".
    @Secured("ROLE_MODERATOR") //метод getUserById доступен только для пользователей с ролью "ROLE_MODERATOR".
    @SecurityRequirement(name = "oauth2_auth_code") //требует аутентификации с использованием OAuth 2.0 с авторизационным кодом.
    public UserResponse getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @GetMapping("/hello")
    @Secured("ROLE_MODERATOR")
    @SecurityRequirement(name = "oauth2_auth_code")
    public String hello() {
        return SecurityContextHolder.getContext().getAuthentication().getName(); // Возвращается имя аутентифицированного пользователя,
        // полученное из объекта SecurityContextHolder. Это позволяет получить имя пользователя, который выполнил текущий запрос.
    }
}
