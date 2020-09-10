package org.coralibre.android.sdk;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class AsyncTestUtil {
    private AsyncTestUtil() {
    }

    /**
     * Calls a runnable that sets its result through a AtomicReference parameter and thus may
     * defer to an asynchronous action.
     *
     * @param runnable a Runnable that's called on the same thread that's calling this method
     * @param <T>      the return value that can be set through the runnable's result parameter
     * @return the result set by the runnable
     * @throws InterruptedException if the continuation has not been called before a timeout
     */
    public static <T> T waitUntilSet(AsyncRunnable<T> runnable) throws InterruptedException {
        Lock lock = new ReentrantLock();
        Condition done = lock.newCondition();

        AtomicReference<T> result = new AtomicReference<>();

        lock.lock();

        runnable.run(result, () -> new Thread(() -> {
            lock.lock();
            try {
                done.signal();
            } finally {
                lock.unlock();
            }
        }).start());

        try {
            if (!done.await(2, TimeUnit.SECONDS)) {
                throw new InterruptedException("Continuation wasn't called");
            }
        } finally {
            lock.unlock();
        }

        return result.get();
    }

    @FunctionalInterface
    public interface AsyncRunnable<T> {
        /**
         * Performs an action, possibly asynchronously.
         *
         * @param result       may be used to set a result before calling the continuation
         * @param continuation signals that the action has been performed
         */
        void run(AtomicReference<T> result, Runnable continuation);
    }
}
