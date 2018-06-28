package com.ym.materials.guava.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public final class MoreExecutors {
    private MoreExecutors() {
    }

    public static ListeningExecutorService listeningDecorator(ExecutorService delegate) {
        if (delegate instanceof ListeningExecutorService) {
            return (ListeningExecutorService) delegate;
        }
        if (delegate instanceof ScheduledExecutorService) {
            return new ScheduledListeningDecorator((ScheduledExecutorService) delegate);
        }
        return new ListeningDecorator(delegate);
    }
}
