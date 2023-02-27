package org.auwerk.otus.arch.userservice.mapper;

import org.auwerk.otus.arch.userservice.api.dto.MyProfileResponseDto;
import org.auwerk.otus.arch.userservice.api.dto.PublicProfileResponseDto;
import org.auwerk.otus.arch.userservice.api.dto.RegisterUserRequestDto;
import org.auwerk.otus.arch.userservice.domain.UserProfile;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public interface UserProfileMapper {

    UserProfile fromRegisterUserRequestDto(RegisterUserRequestDto dto);

    PublicProfileResponseDto toPublicProfileResponseDto(UserProfile profile);

    MyProfileResponseDto toMyProfileResponseDto(UserProfile profile);

    @Mapping(target = "username", source = "userName")
    void updateUserRepresentationFromProfile(UserProfile profile, @MappingTarget UserRepresentation userRepresentation);
}
