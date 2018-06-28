package com.ym.materials.guava.concurrent;

import org.junit.Assert;

import java.util.concurrent.ScheduledExecutorService;

public class ScheduledListeningDecorator extends ListeningDecorator {
    private ScheduledExecutorService delegate;

    public ScheduledListeningDecorator(ScheduledExecutorService delegate) {
        super(delegate);
        Assert.assertNotNull(delegate);
        this.delegate = delegate;
    }
}
