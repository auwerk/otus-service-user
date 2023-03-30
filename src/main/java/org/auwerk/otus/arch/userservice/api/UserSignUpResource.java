package org.auwerk.otus.arch.userservice.api;

import io.smallrye.mutiny.Uni;
import org.auwerk.otus.arch.userservice.api.dto.UserSignUpRequestDto;
import org.auwerk.otus.arch.userservice.mapper.UserProfileMapper;
import org.auwerk.otus.arch.userservice.service.UserProfileService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/signup")
@Consumes(MediaType.APPLICATION_JSON)
public class UserSignUpResource {

    @Inject
    UserProfileService userProfileService;

    @Inject
    UserProfileMapper userProfileMapper;

    @ConfigProperty(name = "otus.cluster.user.public-path")
    String publicPath;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Response> signUp(UserSignUpRequestDto requestDto) {
        return userProfileService
                .createUserProfile(userProfileMapper.fromUserSignUpRequestDto(requestDto), requestDto.getPassword())
                .map(userId -> Response.created(URI.create(publicPath + "/profile/" + userId)).build())
                .onFailure()
                .recoverWithItem(failure -> Response.serverError().entity(failure.getMessage()).build());
    }
}
