package org.auwerk.otus.arch.userservice.saga.impl;

import java.util.UUID;
import java.util.function.Supplier;

import org.auwerk.otus.arch.userservice.saga.ExecutionEvent;
import org.auwerk.otus.arch.userservice.saga.ExecutionLog;
import org.auwerk.otus.arch.userservice.saga.Story;

import io.smallrye.mutiny.Uni;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StoryImpl implements Story {

    @Getter
    private final UUID id = UUID.randomUUID();

    private final ExecutionLog executionLog;
    private final Supplier<Uni<Void>> workload;
    private final Supplier<Uni<Void>> compensation;

    public Uni<Void> execute() {
        executionLog.logEvent(id, ExecutionEvent.started());
        return workload.get()
                .invoke(() -> executionLog.logEvent(id, ExecutionEvent.completed()))
                .onFailure()
                .invoke(ex -> {
                    executionLog.logEvent(id, ExecutionEvent.failed(ex));
                });
    }

    public Uni<Void> compensate() {
        return compensation.get();
    }
}
