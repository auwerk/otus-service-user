package org.auwerk.otus.arch.userservice.api;

import io.smallrye.mutiny.Uni;
import org.auwerk.otus.arch.userservice.api.dto.RegisterUserRequestDto;
import org.auwerk.otus.arch.userservice.mapper.UserProfileMapper;
import org.auwerk.otus.arch.userservice.service.UserService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/register")
@Consumes(MediaType.APPLICATION_JSON)
public class UserRegistrationResource {

    @Inject
    UserService userService;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Response> registerUser(RegisterUserRequestDto requestDto) {
        final var profile = UserProfileMapper.INSTANCE.fromRegisterUserRequestDto(requestDto);

        return userService.createUser(profile)
                .onFailure().transform(throwable -> new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR))
                .onItem().transform(userId -> Response.created(URI.create("/user/" + userId)).build());
    }
}
