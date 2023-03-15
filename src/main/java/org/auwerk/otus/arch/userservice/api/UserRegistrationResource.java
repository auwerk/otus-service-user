package org.auwerk.otus.arch.userservice.api;

import io.smallrye.mutiny.Uni;
import org.auwerk.otus.arch.userservice.api.dto.RegisterUserRequestDto;
import org.auwerk.otus.arch.userservice.mapper.UserProfileMapper;
import org.auwerk.otus.arch.userservice.service.UserService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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

    @Inject
    UserProfileMapper userProfileMapper;

    @ConfigProperty(name = "otus.cluster.user.public-path")
    String publicPath;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Response> registerUser(RegisterUserRequestDto requestDto) {
        return userService.createUser(userProfileMapper.fromRegisterUserRequestDto(requestDto), requestDto.getPassword())
                .onFailure().transform(throwable -> new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR))
                .onItem().transform(userId -> Response.created(URI.create(publicPath + "/profile/" + userId)).build());
    }
}
