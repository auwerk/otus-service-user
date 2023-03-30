package org.auwerk.otus.arch.userservice.service.impl;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.auwerk.otus.arch.userservice.client.BillingAccountManagementClient;
import org.auwerk.otus.arch.userservice.service.BillingService;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class BillingServiceImpl implements BillingService {

    @Inject
    @RestClient
    BillingAccountManagementClient billingServiceClient;

    @Override
    public Uni<UUID> createUserAccount(String userName) {
        return billingServiceClient.createUserAccount(userName)
                .map(response -> response.getAccountId());
    }
}
