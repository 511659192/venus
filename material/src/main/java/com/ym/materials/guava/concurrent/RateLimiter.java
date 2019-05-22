//package com.ym.materials.guava.concurrent;
//
//import com.google.common.base.Preconditions;
//import com.google.common.math.LongMath;
//
//import java.util.concurrent.TimeUnit;
//
//import static com.google.common.base.Preconditions.checkArgument;
//import static com.google.common.base.Preconditions.checkNotNull;
//import static java.util.concurrent.TimeUnit.MICROSECONDS;
//import static java.util.concurrent.TimeUnit.NANOSECONDS;
//
///**
// * Created by ym on 2018/7/22.
// */
//// TODO: 是否可以使用cas优化
//public abstract class RateLimiter {
//
//    // 读秒 以及 线程阻塞
//    private final SleepingStopWatch stopwatch;
//
//    public RateLimiter(SleepingStopWatch stopWatch) {
//        this.stopwatch = checkNotNull(stopWatch);
//    }
//
//    public static RateLimiter create(double permitsPerSecon) {
//        return create(permitsPerSecon, SleepingStopWatch.createFromSystemTimer());
//    }
//
//    private static RateLimiter create(double permitsPerSecon, SleepingStopWatch Stopwatch) {
//        RateLimiter rateLimiter = new SmoothRateLimiter.SmoothBursty(Stopwatch, 1.0);
//        rateLimiter.setRate(permitsPerSecon);
//        return rateLimiter;
//    }
//
//    /**
//     * 设置速率
//     * @param permitsPerSecond
//     */
//    public void setRate(double permitsPerSecond) {
//        checkArgument(permitsPerSecond > 0 && !Double.isNaN(permitsPerSecond));
//        synchronized (mutex()) {
//            doSetRate(permitsPerSecond, stopwatch.readMicros());
//        }
//    }
//
//    public double acquire(int permits) {
//        long microsToWait = reserve(permits);
//        stopwatch.sleepMicrosUninterruptibly(microsToWait); // 需要等待 线程阻塞
//        return 1.0 * microsToWait / TimeUnit.SECONDS.toMicros(1L);
//    }
//
//    private long reserve(int permits) {
//        synchronized (mutex()) {
//            return reserveAndGetWaitTime(permits, stopwatch.readMicros());
//        }
//    }
//
//    public boolean tryAcquire(int permits, long timeout, TimeUnit unit) {
//        long timeoutMicros = Math.max(unit.toMicros(timeout), 0);
//        checkArgument(permits > 0);
//        long microsToWait;
//        synchronized (mutex()) {
//            long nowMicros = stopwatch.readMicros();
//            if (!canAcquired(nowMicros, timeoutMicros)) {
//                return false;
//            } else {
//                microsToWait = reserveAndGetWaitTime(permits, nowMicros);
//            }
//        }
//        stopwatch.sleepMicrosUninterruptibly(microsToWait);
//        return true;
//    }
//
//    final long reserveAndGetWaitTime(int permits, long nowMicros) {
//        long momentAvailable = reserveEarliestAvailableTime(permits, nowMicros);
//        return Math.max(momentAvailable - nowMicros, 0);
//    }
//
//    private boolean canAcquired(long nowMicros, long timeout) {
//        return queryEarliestAvailableTime(nowMicros) <= nowMicros + timeout ;
//    }
//
//
//    abstract void doSetRate(double permitsPerSecond, long nowMicros);
//    abstract long queryEarliestAvailableTime(long nowMicros);
//    abstract long reserveEarliestAvailableTime(int permits, long nowMicros);
//
//    private volatile Object mutexDoNotUseDirectly;
//
//    private Object mutex() {
//        Object mutex = mutexDoNotUseDirectly;
//        if (mutex == null) {
//            synchronized (this) {
//                mutex = mutexDoNotUseDirectly;
//                if (mutex == null) {
//                    mutexDoNotUseDirectly = mutex = new Object();
//                }
//            }
//        }
//        return mutex;
//    }
//
//    abstract static class SmoothRateLimiter extends RateLimiter {
//        double storedPermits;
//        double maxPermits;
//        double intervalMicros;
//        private long nextFreeTicketMicros = 0L;
//        public SmoothRateLimiter(SleepingStopWatch stopWatch) {
//            super(stopWatch);
//        }
//
//        @Override
//        long queryEarliestAvailableTime(long nowMicros) {
//            return nextFreeTicketMicros;
//        }
//
//        @Override
//        long reserveEarliestAvailableTime(int requiredPermits, long nowMicros) {
//            resync(nowMicros);
//            long retValue = nextFreeTicketMicros;
//            double storedPermitsToSpend = Math.min(requiredPermits, this.storedPermits);
//            double freshPermits = requiredPermits - storedPermitsToSpend;
//            long waitMicros = storedPermitsToWaitTime(this.storedPermits, storedPermitsToSpend) + (long)(freshPermits * intervalMicros);
//            this.nextFreeTicketMicros = LongMath.saturatedAdd(nextFreeTicketMicros, waitMicros);
//            this.storedPermits -= storedPermitsToSpend;
//            return retValue;
//        }
//
//        @Override
//        void doSetRate(double permitsPerSecond, long nowMicros) {
//            resync(nowMicros);
//            double intervalMicros = TimeUnit.SECONDS.toMicros(1L) / permitsPerSecond;
//            this.intervalMicros = intervalMicros;
//            doSetRate(permitsPerSecond, intervalMicros);
//        }
//
//        void resync(long nowMicros) {
//            if (nowMicros > nextFreeTicketMicros) {
//                double newPermits = (nowMicros - nextFreeTicketMicros) / coolDownIntervalMicros();
//                storedPermits = Math.min(maxPermits, storedPermits + newPermits);
//                nextFreeTicketMicros = nowMicros;
//            }
//        }
//
//        abstract double coolDownIntervalMicros();
//        abstract void doSetRate(double permitsPerSecond, double intervalMicros);
//        abstract long storedPermitsToWaitTime(double storedPermits, double permitsToTake);
//
//        static class SmoothBursty extends SmoothRateLimiter {
//            final double maxBurstSeconds;
//
//            SmoothBursty(SleepingStopWatch stopWatch, double maxBurstSeconds) {
//                super(stopWatch);
//                this.maxBurstSeconds = maxBurstSeconds;
//            }
//
//            @Override
//            protected double coolDownIntervalMicros() {
//                return intervalMicros; // 平滑突发 无CD
//            }
//
//            void doSetRate(double permitsPerSecond, double intervalMicros) {
//                double oldMaxPermits = this.maxPermits;
//                maxPermits = maxBurstSeconds * permitsPerSecond;
//                if (oldMaxPermits == Double.POSITIVE_INFINITY) {
//                    storedPermits = maxPermits;
//                } else {
//                    storedPermits = oldMaxPermits == 0.0 ? 0.0 : storedPermits * maxPermits / oldMaxPermits;
//                }
//            }
//
//            @Override
//            long storedPermitsToWaitTime(double storedPermits, double permitsToTake) {
//                return 0;
//            }
//        }
//    }
//
//    static class SmoothWarmingUp extends SmoothRateLimiter {
//
//        private final long warmupPeriodMicros;
//        private double slope;
//        private double halfPermits;
//        private double coldFactor;
//
//        public SmoothWarmingUp(SleepingStopWatch stopWatch, long warmupPeriod, TimeUnit timeUnit, double coldFactor) {
//            super(stopWatch);
//            this.warmupPeriodMicros = timeUnit.toMicros(warmupPeriod);
//            this.coldFactor = coldFactor;
//        }
//
//        @Override
//        double coolDownIntervalMicros() {
//            return warmupPeriodMicros / intervalMicros;
//        }
//
//        @Override
//        void doSetRate(double permitsPerSecond, double intervalMicros) {
//            double oldMaxPermits = this.maxPermits;
//            double coldIntervalMicros = intervalMicros * coldFactor;
//            // 临界值=热启动期间所能生成的总令牌数量的一半
//            halfPermits = 0.5 * warmupPeriodMicros / intervalMicros;
//            /**
//             * 根据临界值计算
//             *  等价于 （maxPermits-halfPermits)*(intervalMicros+coldIntervalMicros)=2*warmupPeriodMicros
//             *          ^ throttling
//             *          |
//             *    cold  +                  /
//             * interval |                 /.
//             *          |                / .
//             *          |               /  .   ← 预热区域等于maxPermits halfPermits之间的梯形面积
//             *          |              /   .
//             *          |             /    .
//             *          |            /     .
//             *          |           /      .
//             *   stable +----------/       .
//             * interval |          .       .
//             *          |          .       .
//             *          |          .       .
//             *        0 +----------+-------+--------------→ storedPermits
//             *          |   halfPermits maxPermits
//             *          |          .       .
//             *          |          .       .
//             *          +----------+-------+
//             */
//            maxPermits = halfPermits + 2 * warmupPeriodMicros / (intervalMicros + coldIntervalMicros);
//            slope = (coldIntervalMicros - intervalMicros) / (maxPermits - halfPermits);
//            if (oldMaxPermits == Double.POSITIVE_INFINITY) {
//                storedPermits = 0.0;
//            } else {
//                storedPermits = oldMaxPermits == 0.0 ? maxPermits : storedPermits * maxPermits / oldMaxPermits;
//            }
//        }
//
//        @Override
//        long storedPermitsToWaitTime(double storedPermits, double permitsToTake) {
//            double availablePermitsAboveHalfPermits = storedPermits - halfPermits;
//            long micros = 0;
//            if (availablePermitsAboveHalfPermits > 0.0) {
//                double permitsAboveHalfPermitsToTake = Math.min(availablePermitsAboveHalfPermits, permitsToTake);
//                double topTime = permitsToTime(availablePermitsAboveHalfPermits);
//                double bottomTime = permitsToTime(availablePermitsAboveHalfPermits - permitsAboveHalfPermitsToTake);
//                double avgTime = (topTime + bottomTime) / 2;
//                micros = (long) (permitsAboveHalfPermitsToTake * avgTime);
//                permitsToTake -= permitsAboveHalfPermitsToTake;
//            }
//            micros += (intervalMicros * permitsToTake);
//            return micros;
//        }
//
//        private double permitsToTime(double permits) {
//            return intervalMicros + slope * permits;
//        }
//    }
//
//    abstract static class SleepingStopWatch {
//
//        protected SleepingStopWatch() {
//        }
//
//        protected abstract long readMicros();
//
//        protected abstract void sleepMicrosUninterruptibly(long micros);
//
//        public static SleepingStopWatch createFromSystemTimer() {
//
//            Stopwatch stopwatch = Stopwatch.createStarted();
//
//            return new SleepingStopWatch() {
//                @Override
//                protected long readMicros() {
//                    return stopwatch.elapsed(MICROSECONDS);
//                }
//
//                @Override
//                protected void sleepMicrosUninterruptibly(long micros) {
//                    if (micros <= 0) {
//                        return;
//                    }
//                    Uninterruptibles.sleepUninterruptibly(micros, MICROSECONDS);
//                }
//            };
//        }
//    }
//
//    /**
//     * 计时器
//     */
//    static class Stopwatch {
//        private boolean isRunning;
//        private long elapsedNanos;
//        private long startTick;
//        private final Ticker ticker;
//
//        Stopwatch() {
//            this.ticker = Ticker.systemTicker();
//        }
//
//        public static Stopwatch createStarted() {
//            return new Stopwatch().start();
//        }
//
//        private Stopwatch start() {
//            Preconditions.checkState(!isRunning);
//            isRunning = true;
//            startTick = ticker.read();
//            return this;
//        }
//
//        public long elapsed(TimeUnit timeUnit) {
//            return timeUnit.convert(elapsedNanos(), NANOSECONDS);
//        }
//
//        private long elapsedNanos() {
//            return isRunning ? ticker.read() - startTick + elapsedNanos : elapsedNanos;
//        }
//    }
//
//    /**
//     * 机器时钟（纳秒级）
//     */
//    abstract static class Ticker {
//        Ticker() {
//        }
//
//        public abstract long read();
//
//        public static Ticker systemTicker() {
//            return new Ticker() {
//                @Override
//                public long read() {
//                    return System.nanoTime();
//                }
//            };
//        }
//    }
//
//    public static void main(String[] args) {
//        RateLimiter rateLimiter = RateLimiter.create(1);
//        System.out.println(rateLimiter.tryAcquire(1, 1, TimeUnit.MICROSECONDS));
//        System.out.println(rateLimiter.tryAcquire(1, 1, TimeUnit.MICROSECONDS));
//
//    }
//}
