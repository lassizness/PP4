package com.itm.space.backendresources.service;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.exception.BackendResourcesException;
import com.itm.space.backendresources.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final Keycloak keycloakClient;
    private final UserMapper userMapper;

    @Value("${keycloak.realm}") // указывает, что поле realm будет получать значение из конфигурационного файла или другого источника с помощью выражения ${keycloak.realm}. Значение будет внедрено в поле realm при создании экземпляра класса.
    private String realm;

    public void createUser(UserRequest userRequest) {
        CredentialRepresentation password = preparePasswordRepresentation(userRequest.getPassword());
        UserRepresentation user = prepareUserRepresentation(userRequest, password);
        try {
            Response response = keycloakClient.realm(realm).users().create(user);
            String userId = CreatedResponseUtil.getCreatedId(response);
            log.info("Created UserId: {}", userId);
        } catch (WebApplicationException ex) {
            log.error("Exception on \"createUser\": ", ex);
            throw new BackendResourcesException(ex.getMessage(), HttpStatus.resolve(ex.getResponse().getStatus()));

        }
    }

    @Override
    public UserResponse getUserById(UUID id) { //Это объявление метода getUserById, который принимает объект UUID в качестве параметра и возвращает объект UserResponse. Метод будет использоваться для получения информации о пользователе по его идентификатору.
        UserRepresentation userRepresentation; //Объявляется переменная userRepresentation типа UserRepresentation, которая будет использоваться для хранения представления пользователя.
        List<RoleRepresentation> userRoles; //Объявляется переменная userRoles типа List<RoleRepresentation>, которая будет использоваться для хранения списка ролей пользователя.
        List<GroupRepresentation> userGroups; //Объявляется переменная userGroups типа List<GroupRepresentation>, которая будет использоваться для хранения списка групп, к которым принадлежит пользователь.
        try {
            userRepresentation = keycloakClient.realm(realm).users().get(String.valueOf(id)).toRepresentation(); //Получается представление пользователя с помощью метода toRepresentation(), вызванного на объекте get(String.valueOf(id)) объекта users() объекта realm объекта keycloakClient. Результат сохраняется в переменную userRepresentation.
            userRoles = keycloakClient.realm(realm)
                    .users().get(String.valueOf(id)).roles().getAll().getRealmMappings(); //Получается список ролей пользователя с помощью метода getRealmMappings(), вызванного на объекте getAll() объекта roles() объекта get(String.valueOf(id)) объекта users() объекта realm объекта keycloakClient. Результат сохраняется в переменную userRoles.
            userGroups = keycloakClient.realm(realm).users().get(String.valueOf(id)).groups(); //Получается список групп, к которым принадлежит пользователь, с помощью метода groups(), вызванного на объекте get(String.valueOf(id)) объекта users() объекта realm объекта keycloakClient. Результат сохраняется в переменную userGroups.
        } catch (RuntimeException ex) {
            log.error("Exception on \"getUserById\": ", ex);
            throw new BackendResourcesException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); //В случае возникновения исключения типа RuntimeException, выполняется блок кода внутри catch. В данном случае, исключение будет обработано и выброшено новое исключение BackendResourcesException с сообщением и статусом из исходного исключения.
        }
        return userMapper.userRepresentationToUserResponse(userRepresentation, userRoles, userGroups);//Возвращается результат вызова метода userRepresentationToUserResponse объекта userMapper, который принимает представление пользователя, список ролей и список групп в качестве параметров и возвращает объект UserResponse.
    }

    private CredentialRepresentation preparePasswordRepresentation(String password) { //Метод будет использоваться для подготовки представления пароля.
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation(); //Создается новый объект CredentialRepresentation и сохраняется в переменную credentialRepresentation.
        credentialRepresentation.setTemporary(false); //Устанавливается значение false для свойства temporary объекта credentialRepresentation. Это означает, что пароль не является временным.
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD); //станавливается тип PASSWORD для свойства type объекта credentialRepresentation. Это указывает, что представление пароля является представлением пароля.
        credentialRepresentation.setValue(password); //Устанавливается значение password для свойства value объекта credentialRepresentation. Это устанавливает значение пароля в представлении пароля.
        return credentialRepresentation; //Возвращается объект credentialRepresentation, который содержит подготовленное представление пароля.
    }

    private UserRepresentation prepareUserRepresentation(UserRequest userRequest,
                                                         CredentialRepresentation credentialRepresentation) {
        UserRepresentation newUser = new UserRepresentation();
        newUser.setUsername(userRequest.getUsername());
        newUser.setEmail(userRequest.getEmail());
        newUser.setCredentials(List.of(credentialRepresentation));
        newUser.setEnabled(true);
        newUser.setFirstName(userRequest.getFirstName());
        newUser.setLastName(userRequest.getLastName());
        return newUser;
    }
}
