package com.itm.space.backendresources;


import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.controller.RestExceptionHandler;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc //спринг автоматически создает структуру классов которая подменяет слой MVС
@WithMockUser(username = "gleb", password = "gleb", authorities = "ROLE_MODERATOR")
public class ControllerTest extends BaseIntegrationTest {
    @MockBean
    private Keycloak keycloak;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestExceptionHandler restExceptionHandler;

    @Value("${keycloak.realm}")
    private String realmItm;

    private UserRequest okUserRequest;
    private UserRequest badUserRequest;

    //добавляем объекты классов кейклока
    private RealmResource realmResourceMock;
    private UsersResource usersResourceMock;
    private UserRepresentation userRepresentationMock;
    private UserResource userResourceMock;


    @BeforeEach
//НЕ СТАТИК метод должен выполняться перед каждым методом @Test, @RepeatedTest, @ParameterizedTest, или @TestFactory в текущем классе.
    void preStartTestSettings() {
        okUserRequest = new UserRequest("username", "user@mail.ru", "userpassword", "Patriсk", "Bateman");
        badUserRequest = new UserRequest("u", "", "_", "Patriсk", "Bateman");

        realmResourceMock = mock(RealmResource.class);
        usersResourceMock = mock(UsersResource.class);
        userRepresentationMock = mock(UserRepresentation.class);
        userResourceMock = mock(UserResource.class);
        //мокаем все что будем использовать
    }

    @Test
    @SneakyThrows
    //позволяет обрабатывать проверяемые исключения как непроверяемые исключения (unchecked exceptions). Она автоматически оборачивает
    // проверяемые исключения в RuntimeException, чтобы не требовать явного обработчика исключений или объявления их в сигнатуре метода.
    public void helloMethodShouldBeOk() {
        MockHttpServletResponse response = mockMvc.perform(get("/api/users/hello")).andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("gleb", response.getContentAsString());
    }

    @Test
    public void helloMethodShouldBeOk1() throws Exception {
        this.mockMvc.perform(get("/api/users/hello"))
                .andDo(print())
                .andExpect(status().isOk())  //andExpect обертка над asserThat //ожидаем статус 200 по гет запросу на указанный адрес
                .andExpect(content().string(containsString("gleb")));
    }

}
