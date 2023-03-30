package org.auwerk.otus.arch.userservice.saga;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.auwerk.otus.arch.userservice.saga.impl.ExecutionLogImpl;
import org.auwerk.otus.arch.userservice.saga.impl.StoryImpl;

import io.smallrye.mutiny.Uni;

public class Saga {

    private final ExecutionLog log = new ExecutionLogImpl();
    private final List<Story> stories = new ArrayList<>();

    public UUID addStory(Supplier<Uni<Void>> workload, Supplier<Uni<Void>> compensation) {
        final var story = new StoryImpl(log, workload, compensation);
        stories.add(story);
        return story.getId();
    }

    public Uni<Void> execute() {
        return executeAll()
                .onFailure()
                .recoverWithUni(compensateAllComplete().invoke(() -> {
                    throw new SagaException(log.mapFailures());
                }));
    }

    private Uni<Void> executeAll() {
        return Uni.createFrom().voidItem().call(() -> {
            final var unis = stories.stream()
                    .map(story -> story.execute())
                    .toList();
            if (unis.isEmpty()) {
                return Uni.createFrom().voidItem();
            }
            return Uni.combine().all().unis(unis).collectFailures().discardItems();
        }).replaceWithVoid();
    }

    private Uni<Void> compensateAllComplete() {
        return Uni.createFrom().voidItem().call(() -> {
            final var unis = stories.stream()
                    .filter(story -> log.checkStoryCompletion(story.getId()))
                    .map(story -> story.compensate())
                    .toList();
            if (unis.isEmpty()) {
                return Uni.createFrom().nothing();
            }
            return Uni.combine().all().unis(unis)
                    .discardItems();
        }).replaceWithVoid();
    }
}
