package org.auwerk.otus.arch.userservice.saga;

import java.util.UUID;

import io.smallrye.mutiny.Uni;

public interface Story {

    UUID getId();

    Uni<Void> execute();

    Uni<Void> compensate();
}
