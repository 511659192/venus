package com.ym.materials.guava.concurrent;

import com.google.common.base.Preconditions;
import com.google.common.math.LongMath;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.apache.commons.math3.analysis.function.Max;

import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Created by ym on 2018/7/22.
 */
public abstract class RateLimiter {

    private final SleepingStopWatch stopwatch;

    public RateLimiter(SleepingStopWatch stopWatch) {
        this.stopwatch = checkNotNull(stopWatch);
    }

    public static RateLimiter create(double permitsPerSecon) {
        return create(permitsPerSecon, SleepingStopWatch.createFromSystemTimer());
    }

    private static RateLimiter create(double permitsPerSecon, SleepingStopWatch Stopwatch) {
        RateLimiter rateLimiter = new SmoothRateLimiter.SmoothBursty(Stopwatch, 1.0);
        rateLimiter.setRate(permitsPerSecon);
        return rateLimiter;
    }

    public void setRate(double permitsPerSecond) {
        checkArgument(permitsPerSecond > 0 && !Double.isNaN(permitsPerSecond));
        synchronized (mutex()) {
            doSetRate(permitsPerSecond, stopwatch.readMicros());
        }
    }

    public boolean tryAcquire(int permits, long timeout, TimeUnit unit) {
        long timeoutMicros = Math.max(unit.toMicros(timeout), 0);
        checkArgument(permits > 1);
        long microsToWait;
        synchronized (mutex()) {
            long nowMicros = stopwatch.readMicros();
            if (!canAcquired(nowMicros, timeoutMicros)) {
                return false;
            } else {
                microsToWait = reserveAndGetWaitLength(permits, nowMicros);
            }
        }
        stopwatch.sleepMicrosUninterruptibly(microsToWait);
        return true;
    }

    final long reserveAndGetWaitLength(int permits, long nowMicros) {
        long momentAvailable = reserveEarliestAvailable(permits, nowMicros);
        return Math.max(momentAvailable - nowMicros, 0);
    }

    private boolean canAcquired(long nowMicros, long timeout) {
        return queryEarliestAvailable(nowMicros) <= nowMicros + timeout ;
    }


    abstract void doSetRate(double permitsPerSecond, long nowMicros);
    abstract long queryEarliestAvailable(long nowMicros);
    abstract long reserveEarliestAvailable(int permits, long nowMicros);

    private volatile Object mutexDoNotUseDirectly;

    private Object mutex() {
        Object mutex = mutexDoNotUseDirectly;
        if (mutex == null) {
            synchronized (this) {
                mutex = mutexDoNotUseDirectly;
                if (mutex == null) {
                    mutexDoNotUseDirectly = mutex = new Object();
                }
            }
        }
        return mutex;
    }


    abstract static class SmoothRateLimiter extends RateLimiter {
        double storedPermits;
        double maxPermits;
        double intervalMicros;
        private long nextFreeTicketMicros = 0L;
        public SmoothRateLimiter(SleepingStopWatch stopWatch) {
            super(stopWatch);
        }

        @Override
        long queryEarliestAvailable(long nowMicros) {
            return nextFreeTicketMicros;
        }

        @Override
        long reserveEarliestAvailable(int requiredPermits, long nowMicros) {
            resync(nowMicros);
            long retValue = nextFreeTicketMicros;
            double storedPermitsToSpend = Math.min(requiredPermits, this.storedPermits);
            double freshPermits = requiredPermits - storedPermitsToSpend;
            long waitMicros = storedPermitsToWaitTime(this.storedPermits, storedPermitsToSpend) + (long)(freshPermits * intervalMicros);
            this.nextFreeTicketMicros = LongMath.saturatedAdd(nextFreeTicketMicros, waitMicros);
            this.storedPermits -= storedPermitsToSpend;
            return retValue;
        }

        @Override
        void doSetRate(double permitsPerSecond, long nowMicros) {
            resync(nowMicros);
            double intervalMicros = TimeUnit.SECONDS.toMicros(1L) / permitsPerSecond;
            this.intervalMicros = intervalMicros;
            doSetRate(permitsPerSecond, intervalMicros);
        }

        void resync(long nowMicros) {
            if (nowMicros > nextFreeTicketMicros) {
                double newPermits = (nowMicros - nextFreeTicketMicros) / coolDownIntervalMicros();
                storedPermits = Math.min(maxPermits, storedPermits + newPermits);
                nextFreeTicketMicros = nowMicros;
            }
        }

        protected abstract double coolDownIntervalMicros();
        abstract void doSetRate(double permitsPerSecond, double stableIntervalMicros);
        abstract long storedPermitsToWaitTime(double storedPermits, double permitsToTake);

        static class SmoothBursty extends SmoothRateLimiter {
            final double maxBurstSeconds;

            SmoothBursty(SleepingStopWatch stopWatch, double maxBurstSeconds) {
                super(stopWatch);
                this.maxBurstSeconds = maxBurstSeconds;
            }

            @Override
            protected double coolDownIntervalMicros() {
                return intervalMicros;
            }

            void doSetRate(double permitsPerSecond, double intervalMicros) {
                double oldMaxPermits = this.maxPermits;
                maxPermits = maxBurstSeconds * permitsPerSecond;
                if (oldMaxPermits == Double.POSITIVE_INFINITY) {
                    storedPermits = maxPermits;
                } else {
                    storedPermits = oldMaxPermits == 0.0 ? 0.0 : storedPermits * maxPermits / oldMaxPermits;
                }
            }

            @Override
            long storedPermitsToWaitTime(double storedPermits, double permitsToTake) {
                return 0;
            }
        }
    }

    abstract static class SleepingStopWatch {

        protected SleepingStopWatch() {
        }

        protected abstract long readMicros();

        protected abstract void sleepMicrosUninterruptibly(long micros);

        public static SleepingStopWatch createFromSystemTimer() {

            Stopwatch stopwatch = Stopwatch.createStarted();

            return new SleepingStopWatch() {
                @Override
                protected long readMicros() {
                    return stopwatch.elapsed(MICROSECONDS);
                }

                @Override
                protected void sleepMicrosUninterruptibly(long micros) {
                    if (micros <= 0) {
                        return;
                    }
                    Uninterruptibles.sleepUninterruptibly(micros, MICROSECONDS);
                }
            };
        }
    }

    static class Stopwatch {
        private boolean isRunning;
        private long elapsedNanos;
        private long startTick;
        private final Ticker ticker;

        Stopwatch() {
            this.ticker = Ticker.systemTicker();
        }

        public static Stopwatch createStarted() {
            return new Stopwatch().start();
        }

        private Stopwatch start() {
            Preconditions.checkState(!isRunning);
            isRunning = true;
            startTick = ticker.read();
            return this;
        }

        public long elapsed(TimeUnit timeUnit) {
            return timeUnit.convert(elapsedNanos(), NANOSECONDS);
        }

        private long elapsedNanos() {
            return isRunning ? ticker.read() - startTick + elapsedNanos : elapsedNanos;
        }
    }

    abstract static class Ticker {
        Ticker() {
        }

        public abstract long read();

        public static Ticker systemTicker() {
            return new Ticker() {
                @Override
                public long read() {
                    return System.nanoTime();
                }
            };
        }
    }

}
