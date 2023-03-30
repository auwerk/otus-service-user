package org.auwerk.otus.arch.userservice.saga.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.auwerk.otus.arch.userservice.saga.ExecutionEventType;
import org.auwerk.otus.arch.userservice.saga.ExecutionLog;
import org.auwerk.otus.arch.userservice.saga.StoryWorkloadMock;
import org.junit.jupiter.api.Test;

import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;

public class StoryImplTest {

    private final ExecutionLog executionLog = mock(ExecutionLog.class);

    @Test
    void executeCompletingStory() {
        final var workload = new StoryWorkloadMock();
        final var story = new StoryImpl(executionLog, () -> workload.execute(), () -> workload.compensate());

        final var subscriber = story.execute().subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.assertCompleted();
        assertTrue(workload.isExecuted());
        assertFalse(workload.isCompensated());

        verify(executionLog, times(1)).logEvent(eq(story.getId()),
                argThat(event -> ExecutionEventType.STARTED.equals(event.getType())));
        verify(executionLog, times(1)).logEvent(eq(story.getId()),
                argThat(event -> ExecutionEventType.COMPLETED.equals(event.getType())));
        verifyNoMoreInteractions(executionLog);
    }

    @Test
    void executeFailingStory() {
        final var failure = new RuntimeException();
        final var workload = new StoryWorkloadMock(failure);
        final var story = new StoryImpl(executionLog, () -> workload.execute(), () -> workload.compensate());

        final var subscriber = story.execute().subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.assertFailedWith(RuntimeException.class);
        assertTrue(workload.isExecuted());
        assertFalse(workload.isCompensated());

        verify(executionLog, times(1)).logEvent(eq(story.getId()),
                argThat(event -> ExecutionEventType.STARTED.equals(event.getType())));
        verify(executionLog, times(1)).logEvent(eq(story.getId()),
                argThat(event -> ExecutionEventType.FAILED.equals(event.getType()) && failure == event.getThrowable()));
        verifyNoMoreInteractions(executionLog);
    }

    @Test
    void compensateStory() {
        final var workload = new StoryWorkloadMock();
        final var story = new StoryImpl(executionLog, () -> workload.execute(), () -> workload.compensate());

        final var subscriber = story.compensate().subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.assertCompleted();
        assertTrue(workload.isCompensated());
        assertFalse(workload.isExecuted());
    }
}
