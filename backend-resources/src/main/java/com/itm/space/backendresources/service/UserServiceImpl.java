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

    public void createUser(UserRequest userRequest) { //Метод будет использоваться для создания нового пользователя.
        CredentialRepresentation password = preparePasswordRepresentation(userRequest.getPassword()); //Создается объект CredentialRepresentation с помощью метода preparePasswordRepresentation, который принимает пароль пользователя из объекта userRequest и возвращает соответствующее представление пароля.
        UserRepresentation user = prepareUserRepresentation(userRequest, password); //Создается объект UserRepresentation с помощью метода prepareUserRepresentation, который принимает объект userRequest и объект password и возвращает соответствующее представление пользователя.
        try {
            Response response = keycloakClient.realm(realm).users().create(user); //Вызывается метод create объекта users() объекта realm объекта keycloakClient для создания нового пользователя с использованием объекта user. Результат сохраняется в объект response.
            String userId = CreatedResponseUtil.getCreatedId(response); //Получается идентификатор созданного пользователя из объекта response с помощью метода getCreatedId из класса CreatedResponseUtil. Идентификатор сохраняется в переменную userId.
            log.info("Created UserId: {}", userId); //Выводится информация о созданном идентификаторе пользователя в лог.
        } catch (WebApplicationException ex) {
            log.error("Exception on \"createUser\": ", ex);
            throw new BackendResourcesException(ex.getMessage(), HttpStatus.resolve(ex.getResponse().getStatus()));
            //исключение будет обработано и выброшено новое исключение BackendResourcesException с сообщением и статусом из исходного исключения.
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

    private UserRepresentation prepareUserRepresentation(UserRequest userRequest, //бъявление приватного метода prepareUserRepresentation, который принимает объект UserRequest и объект CredentialRepresentation в качестве параметров и возвращает объект UserRepresentation. Метод будет использоваться для подготовки представления пользователя.
                                                         CredentialRepresentation credentialRepresentation) {
        UserRepresentation newUser = new UserRepresentation(); //оздается новый объект UserRepresentation и сохраняется в переменную newUser.
        newUser.setUsername(userRequest.getUsername()); //Устанавливается значение username из объекта userRequest для свойства username объекта newUser. Это устанавливает имя пользователя в представлении пользователя.
        newUser.setEmail(userRequest.getEmail()); //Устанавливается значение email из объекта userRequest для свойства email объекта newUser. Это устанавливает электронную почту пользователя в представлении пользователя.
        newUser.setCredentials(List.of(credentialRepresentation));//Устанавливается список, содержащий объект credentialRepresentation, для свойства credentials объекта newUser. Это устанавливает учетные данные пользователя в представлении пользователя.
        newUser.setEnabled(true);//Устанавливается значение true для свойства enabled объекта newUser. Это указывает, что пользователь активен.
        newUser.setFirstName(userRequest.getFirstName());//Устанавливается значение firstName из объекта userRequest для свойства firstName объекта newUser. Это устанавливает имя пользователя в представлении пользователя.
        newUser.setLastName(userRequest.getLastName()); //Устанавливается значение lastName из объекта userRequest для свойства lastName объекта newUser. Это устанавливает фамилию пользователя в представлении пользователя.
        return newUser; //Возвращается объект newUser, который содержит подготовленное представление пользователя.
    }
}
