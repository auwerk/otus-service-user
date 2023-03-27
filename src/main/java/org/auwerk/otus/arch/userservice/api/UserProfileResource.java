package org.auwerk.otus.arch.userservice.api;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

import org.auwerk.otus.arch.userservice.api.dto.UpdateUserProfileRequestDto;
import org.auwerk.otus.arch.userservice.exception.UserProfileNotFoundException;
import org.auwerk.otus.arch.userservice.mapper.UserProfileMapper;
import org.auwerk.otus.arch.userservice.service.UserService;
import org.jboss.resteasy.reactive.NoCache;
import org.jboss.resteasy.reactive.RestPath;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/profile")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class UserProfileResource {

    private final UserService userService;
    private final UserProfileMapper userProfileMapper;

    @GET
    @Authenticated
    @NoCache
    public Uni<Response> myProfile() {
        return userService.getMyProfile()
                .map(profile -> Response.ok(userProfileMapper.toMyProfileDto(profile)).build())
                .onFailure(UserProfileNotFoundException.class)
                .recoverWithItem(Response.status(Status.NOT_FOUND).build())
                .onFailure()
                .recoverWithItem(failure -> Response.serverError().entity(failure.getMessage()).build());
    }

    @PUT
    @Authenticated
    public Uni<Response> updateMyProfile(UpdateUserProfileRequestDto requestDto) {
        return userService.updateMyProfile(userProfileMapper.fromUpdateUserProfileRequestDto(requestDto))
                .replaceWith(Response.ok().build())
                .onFailure()
                .recoverWithItem(failure -> Response.serverError().entity(failure.getMessage()).build());
    }

    @GET
    @Path("/{id:\\d+}")
    public Uni<Response> userProfile(@RestPath Long id) {
        return userService.getUserProfile(id)
                .map(profile -> Response.ok(userProfileMapper.toPublicProfileResponseDto(profile)).build())
                .onFailure(UserProfileNotFoundException.class)
                .recoverWithItem(Response.status(Status.NOT_FOUND).build())
                .onFailure()
                .recoverWithItem(failure -> Response.serverError().entity(failure.getMessage()).build());
    }
}
