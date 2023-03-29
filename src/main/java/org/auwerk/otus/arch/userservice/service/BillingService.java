package org.auwerk.otus.arch.userservice.service;

import java.util.UUID;

import io.smallrye.mutiny.Uni;

public interface BillingService {
    
    Uni<UUID> createUserAccount(String userName);
}
