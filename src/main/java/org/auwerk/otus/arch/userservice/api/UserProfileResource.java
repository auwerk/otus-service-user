package org.auwerk.otus.arch.userservice.api;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import org.auwerk.otus.arch.userservice.dao.UserProfileDao;
import org.auwerk.otus.arch.userservice.mapper.UserProfileMapper;
import org.auwerk.otus.arch.userservice.service.UserService;
import org.jboss.resteasy.reactive.NoCache;
import org.jboss.resteasy.reactive.RestPath;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/profile")
@Produces(MediaType.APPLICATION_JSON)
public class UserProfileResource {

    @Inject
    UserProfileDao userProfileDao;

    @Inject
    UserService userService;

    @GET
    @Authenticated
    @NoCache
    public Uni<Response> myProfile() {
        return userService.getMyProfile()
                .onItem().transform(profile ->
                        Response.ok(UserProfileMapper.INSTANCE.toMyProfileResponseDto(profile)).build());
    }

    @GET
    @Path("/{id:\\d+}")
    public Uni<Response> userProfile(@RestPath Long id) {
        return userProfileDao.findById(id)
                .onFailure().transform(throwable -> new WebApplicationException(Response.Status.NOT_FOUND))
                .onItem().transform(profile ->
                        Response.ok(UserProfileMapper.INSTANCE.toPublicProfileResponseDto(profile)).build());
    }
}
