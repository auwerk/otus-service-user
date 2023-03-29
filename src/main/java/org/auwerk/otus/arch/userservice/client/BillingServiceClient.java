package org.auwerk.otus.arch.userservice.client;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.auwerk.otus.arch.userservice.client.dto.billing.CreateUserAccountRequestDto;
import org.auwerk.otus.arch.userservice.client.dto.billing.CreateUserAccountResponseDto;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@RegisterRestClient(configKey = "billing-service-api")
public interface BillingServiceClient {

    @POST
    @Path("/account")
    Uni<CreateUserAccountResponseDto> createUserAccount(CreateUserAccountRequestDto request);
}
