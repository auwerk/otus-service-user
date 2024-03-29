package org.auwerk.otus.arch.userservice.mapper;

import org.auwerk.otus.arch.userservice.api.dto.MyProfileDto;
import org.auwerk.otus.arch.userservice.api.dto.UserSignUpRequestDto;
import org.auwerk.otus.arch.userservice.api.dto.UpdateUserProfileRequestDto;
import org.auwerk.otus.arch.userservice.domain.UserProfile;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserProfileMapper {

    UserProfile fromUserSignUpRequestDto(UserSignUpRequestDto dto);

    UserProfile fromUpdateUserProfileRequestDto(UpdateUserProfileRequestDto updateUserProfileRequestDto);

    MyProfileDto toMyProfileDto(UserProfile profile);

    @Mapping(target = "username", source = "userName")
    void updateUserRepresentationFromProfile(UserProfile profile, @MappingTarget UserRepresentation userRepresentation);
}
