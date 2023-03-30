package org.auwerk.otus.arch.userservice.api;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

import org.auwerk.otus.arch.userservice.api.dto.UpdateUserProfileRequestDto;
import org.auwerk.otus.arch.userservice.exception.UserProfileNotFoundException;
import org.auwerk.otus.arch.userservice.mapper.UserProfileMapper;
import org.auwerk.otus.arch.userservice.service.UserProfileService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/profile")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class UserProfileResource {

    private final UserProfileService userService;
    private final UserProfileMapper userProfileMapper;

    @GET
    public Uni<Response> getMyProfile() {
        return userService.getMyProfile()
                .map(profile -> Response.ok(userProfileMapper.toMyProfileDto(profile)).build())
                .onFailure(UserProfileNotFoundException.class)
                .recoverWithItem(failure -> Response.status(Status.NOT_FOUND).entity(failure.getMessage()).build())
                .onFailure()
                .recoverWithItem(failure -> Response.serverError().entity(failure.getMessage()).build());
    }

    @PUT
    public Uni<Response> updateMyProfile(UpdateUserProfileRequestDto requestDto) {
        return userService.updateMyProfile(userProfileMapper.fromUpdateUserProfileRequestDto(requestDto))
                .replaceWith(Response.ok().build())
                .onFailure(UserProfileNotFoundException.class)
                .recoverWithItem(failure -> Response.status(Status.NOT_FOUND).entity(failure.getMessage()).build())
                .onFailure()
                .recoverWithItem(failure -> Response.serverError().entity(failure.getMessage()).build());
    }

    @DELETE
    public Uni<Response> deleteMyProfile() {
        return userService.deleteMyProfile()
                .replaceWith(Response.ok().build())
                .onFailure(UserProfileNotFoundException.class)
                .recoverWithItem(failure -> Response.status(Status.NOT_FOUND).entity(failure.getMessage()).build())
                .onFailure()
                .recoverWithItem(failure -> Response.serverError().entity(failure.getMessage()).build());
    }
}
