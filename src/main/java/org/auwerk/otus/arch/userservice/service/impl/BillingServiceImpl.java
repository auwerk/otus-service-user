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
    BillingAccountManagementClient accountManagementClient;

    @Override
    public Uni<UUID> createUserAccount(String userName) {
        return accountManagementClient.createUserAccount(userName)
                .map(response -> response.getAccountId());
    }

    @Override
    public Uni<Void> deleteUserAccount(String userName) {
        return accountManagementClient.deleteUserAccount(userName);
    }
}
