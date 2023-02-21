package org.auwerk.otus.arch.userservice.api;

import io.smallrye.mutiny.Uni;
import org.auwerk.otus.arch.userservice.api.dto.RegisterUserRequestDto;
import org.auwerk.otus.arch.userservice.service.UserService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/user")
public class UserResource {

    @Inject
    UserService userService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> registerUser(RegisterUserRequestDto requestDto) {
        return userService.createUser(requestDto.getUserName(), requestDto.getEmail())
                .onItem().transform(userId -> userId == null
                        ? Response.serverError().build()
                        : Response.created(URI.create("/user/" + userId)).build());
    }
}
