package com.ym.materials.guava.concurrent;

import org.junit.Assert;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    private static class ListeningDecorator extends AbstractListeningExecutorService {
        private ExecutorService delegate;

        public ListeningDecorator(ExecutorService delegate) {
            Assert.assertNotNull(delegate);
            this.delegate = delegate;
        }

        @Override
        public void shutdown() {
            delegate.shutdown();
        }

        @Override
        public List<Runnable> shutdownNow() {
            return delegate.shutdownNow();
        }

        @Override
        public boolean isShutdown() {
            return delegate.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return delegate.isTerminated();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return delegate.awaitTermination(timeout, unit);
        }

        @Override
        public void execute(Runnable command) {
            delegate.execute(command);
        }
    }

    private static class ScheduledListeningDecorator extends ListeningDecorator {
        private ScheduledExecutorService delegate;

        public ScheduledListeningDecorator(ScheduledExecutorService delegate) {
            super(delegate);
            Assert.assertNotNull(delegate);
            this.delegate = delegate;
        }
    }
}
