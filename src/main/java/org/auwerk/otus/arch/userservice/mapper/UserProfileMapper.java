package org.auwerk.otus.arch.userservice.mapper;

import org.auwerk.otus.arch.userservice.api.dto.RegisterUserRequestDto;
import org.auwerk.otus.arch.userservice.domain.UserProfile;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserProfileMapper {

    UserProfileMapper INSTANCE = Mappers.getMapper(UserProfileMapper.class);

    UserProfile fromRegisterUserRequestDto(RegisterUserRequestDto dto);

    @Mapping(target = "username", source = "userName")
    void updateUserRepresentationFromProfile(UserProfile profile, @MappingTarget UserRepresentation userRepresentation);
}
