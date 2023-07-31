package com.itm.space.backendresources;


import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.controller.RestExceptionHandler;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockHttpServletRequestDsl;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.BDDAssertions.and;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
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


    @BeforeEach
//�� ������ ����� ������ ����������� ����� ������ ������� @Test, @RepeatedTest, @ParameterizedTest, ��� @TestFactory � ������� ������.
    void preStartTestSettings() {
        okUserRequest = new UserRequest("username", "user@mail.ru", "userpassword", "Patri�k", "Bateman");
        badUserRequest = new UserRequest("u", "", "_", "Patri�k", "Bateman");
    }

    @Test
    @SneakyThrows
    //��������� ������������ ����������� ���������� ��� ������������� ���������� (unchecked exceptions). ��� ������������� �����������
    // ����������� ���������� � RuntimeException, ����� �� ��������� ������ ����������� ���������� ��� ���������� �� � ��������� ������.
    public void helloMethodShouldBeOk() {
        MockHttpServletResponse response = mockMvc.perform(get("/api/users/hello")).andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("gleb", response.getContentAsString());

    }
}
