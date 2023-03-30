package org.auwerk.otus.arch.userservice.client;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.auwerk.otus.arch.userservice.client.dto.billing.CreateUserAccountResponseDto;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@RegisterRestClient(configKey = "billing-service-api")
@Path("/management/account")
public interface BillingAccountManagementClient {

    @POST
    @Path("/{userName}")
    Uni<CreateUserAccountResponseDto> createUserAccount(@PathParam("userName") String userName);

    @DELETE
    @Path("/{userName}")
    Uni<Void> deleteUserAccount(@PathParam("userName") String userName);
}
