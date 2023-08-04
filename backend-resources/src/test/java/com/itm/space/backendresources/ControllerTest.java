package com.itm.space.backendresources;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.exception.BackendResourcesException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest
//@AutoConfigureMockMvc //спринг автоматически создает структуру классов которая подменяет слой MVС
@WithMockUser(username = "gleb", password = "gleb", authorities = "ROLE_MODERATOR")
public class ControllerTest extends BaseIntegrationTest {

    @MockBean
    private Keycloak keycloak;
    @Value("${keycloak.realm}")
    private String realm;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    public void helloMethodShouldBeOk1() {
        MockHttpServletResponse response = mockMvc.perform(get("/api/users/hello")).andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("gleb", response.getContentAsString());
    }

    @Test
    public void helloMethodShouldBeOk2() throws Exception {
        this.mockMvc.perform(get("/api/users/hello"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("gleb")));
    }

    private RealmResource realmResource;
    private UsersResource usersResource;
    private Response response;
    private UserRequest userRequest;
    private UserResource userResource;
    private UserRepresentation userRepresentation;

    @BeforeEach
    void init() {
        realmResource = mock(RealmResource.class);
        usersResource = mock(UsersResource.class);
        response = mock(Response.class);
        userRequest = new UserRequest("gleb", "test@mail.ru", "gleb", "Gleb", "Emelyanov");
        userResource = mock(UserResource.class);
        userRepresentation = mock(UserRepresentation.class);
    }

    @Test
    public void successfulCreateUserByModerator() throws Exception {
        when(keycloak.realm(ArgumentMatchers.anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(ArgumentMatchers.any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);
        this.mvc.perform(requestWithContent(post("/api/users"), userRequest))
                .andExpect(status().is(200))
                .andDo(print())
                .andReturn();
        verify(usersResource).create(any(UserRepresentation.class));
    }

    @Test
    @SneakyThrows
    public void validationErrorWhenCreatedUserByModerator() {
        UserRequest request = new UserRequest("g", "", "g", "Gleb", "Emelyanov");
        mvc.perform(requestWithContent(post("/api/users"),
                        request))
                .andDo(print())
                .andExpect(status().is(400))
                .andReturn().getResponse();
    }

    @Test
    @SneakyThrows
    public void createUserByModeratorShouldThrowException(){
        when(keycloak.realm(ArgumentMatchers.anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(ArgumentMatchers.any(UserRepresentation.class))).thenReturn(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
        mvc.perform(requestWithContent(post("/api/users"),userRequest)).andExpect(status().is(500));
    }

    @Test
    public void helloMethodShouldBeOk3() throws Exception {
        this.mvc.perform(get("/api/users/hello")
                        .header("Authorization", "fef"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void successfulGetUserByIdByModerator() throws Exception {
        String id = "3d40251d-829c-454e-b389-ec5e9c38a4cc";
        when(keycloak.realm(ArgumentMatchers.anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(realmResource.users().get(eq(id))).thenReturn(userResource);
        when(userResource.roles()).thenReturn(mock(RoleMappingResource.class));
        when(userRepresentation.getId()).thenReturn(id);
        when(userResource.roles().getAll()).thenReturn(mock(MappingsRepresentation.class));
        when(userResource.toRepresentation()).thenReturn(userRepresentation);

        this.mvc.perform(get("/api/users/{id}", id))
                .andExpect(status().is(200))
                .andDo(print());
    }
    @Test
    public void exceptionGetUserByIdByModerator() throws Exception {
        String id = "3d40251d-829c-454e-b389-ec5e9c38a4cc";
        when(keycloak.realm(ArgumentMatchers.anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(realmResource.users().get(eq(id))).thenThrow(new BackendResourcesException("message", HttpStatus.INTERNAL_SERVER_ERROR));
        this.mvc.perform(get("/api/users/{id}", id))
                .andExpect(status().is(500))
                .andDo(print());
    }

    @Test
    public void unsuccessful404GetUserByIdByModerator() throws Exception {
        String id = "";
        when(keycloak.realm(ArgumentMatchers.anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(realmResource.users().get(eq(id))).thenReturn(userResource);
        this.mvc.perform(get("/api/users/{id}", id))
                .andExpect(status().is(404))
                .andDo(print());
    }
}