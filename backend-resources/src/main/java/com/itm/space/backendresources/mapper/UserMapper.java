package com.itm.space.backendresources.mapper;

import com.itm.space.backendresources.api.response.UserResponse;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, imports = Collections.class) // указывает, что интерфейс UserMapper является маппером, который будет использоваться для преобразования объектов между различными представлениями.
// Он использует компонентную модель SPRING и импортирует класс Collections.
public interface UserMapper {

    @Mapping(target = "roles", source = "roleList", qualifiedByName = "mapRoleRepresentationToString") //указывает, что поле roles в объекте UserResponse будет заполняться значениями из списка roleList, преобразованными с помощью метода mapRoleRepresentationToString.
    @Mapping(target = "groups", source = "groupList", qualifiedByName = "mapGroupRepresentationToString") //указывает, что поле groups в объекте UserResponse будет заполняться значениями из списка groupList, преобразованными с помощью метода mapGroupRepresentationToString.
    UserResponse userRepresentationToUserResponse(UserRepresentation userRepresentation,
                                                  List<RoleRepresentation> roleList,
                                                  List<GroupRepresentation> groupList);

    @Named("mapRoleRepresentationToString") //указывает, что метод mapRoleRepresentationToString будет использоваться для преобразования списка RoleRepresentation в список строк.
    default List<String> mapRoleRepresentationToString(List<RoleRepresentation> roleList) {
        return roleList.stream().map(RoleRepresentation::getName).toList(); //Это объявление метода mapRoleRepresentationToString, который принимает список объектов RoleRepresentation
        // в качестве параметра и возвращает список строк. Метод использует стрим для преобразования каждого объекта RoleRepresentation в его имя (getName()) и собирает результаты в список.

    }

    @Named("mapGroupRepresentationToString") //метод mapGroupRepresentationToString будет использоваться для преобразования списка GroupRepresentation в список строк.
    default List<String> mapGroupRepresentationToString(List<GroupRepresentation> groupList) { //Это объявление метода mapGroupRepresentationToString, который принимает список объектов GroupRepresentation в качестве
        // параметра и возвращает список строк. Метод использует стрим для преобразования
        // каждого объекта GroupRepresentation в его имя (getName()) и собирает результаты в список.
        return groupList.stream().map(GroupRepresentation::getName).toList();
    }

}
