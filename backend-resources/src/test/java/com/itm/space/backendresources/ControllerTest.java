package com.itm.space.backendresources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.controller.RestExceptionHandler;
import com.itm.space.backendresources.service.UserService;
import com.itm.space.backendresources.service.UserServiceImpl;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.core.Response;

import java.net.URI;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    private final String USERNAME = "gleb";
    private final String PASSWORD = "gleb";

    public ControllerTest() {
    }
    //    @BeforeEach
////НЕ СТАТИК метод должен выполняться перед каждым методом @Test, @RepeatedTest, @ParameterizedTest, или @TestFactory в текущем классе.

    @Test
    @SneakyThrows
    //позволяет обрабатывать проверяемые исключения как непроверяемые исключения (unchecked exceptions). Она автоматически оборачивает
    // проверяемые исключения в RuntimeException, чтобы не требовать явного обработчика исключений или объявления их в сигнатуре метода.
    public void helloMethodShouldBeOk1() {
        MockHttpServletResponse response = mockMvc.perform(get("/api/users/hello")).andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("gleb", response.getContentAsString());
    }

    @Test
    public void helloMethodShouldBeOk2() throws Exception {
        this.mockMvc.perform(get("/api/users/hello"))
                .andDo(print())
                .andExpect(status().isOk())  //andExpect обертка над asserThat //ожидаем статус 200 по гет запросу на указанный адрес
                .andExpect(content().string(containsString("gleb")));
    }

    @Test
    public void userFormTest() { //проверка шаблона формы(Response)  по http://backend-gateway-client:9090/api/users/hello/ UUID
        JsonObject jsonObject = Json.createObjectBuilder()
                .add("firstName", "")
                .add("lastName", "")
                .add("email", JsonValue.NULL)
                .add("roles", Json.createArrayBuilder().add("default-roles-itm"))
                .add("groups", Json.createArrayBuilder().add("Moderators"))
                .build();

        assertTrue(jsonObject.containsKey("firstName"));
        assertTrue(jsonObject.containsKey("lastName"));
        assertTrue(jsonObject.containsKey("email"));
        assertTrue(jsonObject.containsKey("roles"));
        assertTrue(jsonObject.containsKey("groups"));
    }


    private static HttpEntity<MultiValueMap<String, String>> getMultiValueMapHttpEntity(String username, String password) {
        String clientId = "backend-gateway-client";
        String clientSecret = "5Rrlk1xQyoIg48Zva2WYgtgI4igWzg8z";
        String grantType = "password";
        String scope = "openid";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", grantType);
        map.add("username", username);
        map.add("password", password);
        map.add("scope", scope);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, httpHeaders);
        return request;
    }

    @SneakyThrows
    private String getKeycloakAccessToken(String username, String password) {
        HttpEntity<MultiValueMap<String, String>> request = getMultiValueMapHttpEntity(username, password);

        RestTemplate restTemplate = new RestTemplate();

        String URL = "http://backend-keycloak-auth:8080/auth/realms/ITM/protocol/openid-connect/token";
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(URL, request, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(responseEntity.getBody());

        return node.get("access_token").asText();
    }


    private final String ACCESS_TOKEN = "Bearer " + getKeycloakAccessToken(USERNAME, PASSWORD);

    RealmResource realmResource = Mockito.mock(RealmResource.class);
    UsersResource usersResource = Mockito.mock(UsersResource.class);
    UserRepresentation user = new UserRepresentation();
    Response response = Mockito.mock(Response.class); //????


    @Test
    public void createUserByModerator() throws Exception {

        Mockito.when(keycloak.realm(ArgumentMatchers.anyString())).thenReturn(realmResource);
        Mockito.when(realmResource.users()).thenReturn(usersResource);
        Mockito.when(usersResource.create(ArgumentMatchers.any(UserRepresentation.class))).thenReturn(response);
        Mockito.when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);

        UserRequest testUserGleb = new UserRequest("gleb", "test@mail.ru", "gleb", "Gleb", "Emelyanov");
        this.mvc.perform(requestWithContent(post("/api/users")
                        .header("Authorization", ACCESS_TOKEN), testUserGleb))
                .andDo(print())
                .andExpect(status().is(200));
    }

    @Test
    public void helloMethodShouldBeOk3() throws Exception {
        this.mvc.perform(get("/api/users/hello")
                        .header("Authorization", ACCESS_TOKEN))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void getUserByIdByModerator() throws Exception {
        String UID = "3d40251d-829c-454e-b389-ec5e9c38a4cc";
        this.mvc.perform(get("/api/users/" + UID)
                        .header("Authorization", ACCESS_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.email").value("test@mail.ru"))
                .andExpect(jsonPath("$.name").value("gleb"));
    }

}