package org.coralibre.android.sdk.fakegms.tasks;

import android.os.Looper;

import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TaskExecutorsTest {

    @Test
    public void executedOnMainThread() throws InterruptedException {
        Lock lock = new ReentrantLock();
        Condition done = lock.newCondition();

        Executor executor = TaskExecutors.MAIN_THREAD;
        AtomicReference<Thread> executorThread = new AtomicReference<>();

        lock.lock();
        executor.execute(() -> {
            lock.lock();
            try {
                executorThread.set(Thread.currentThread());
                done.signal();
            } finally {
                lock.unlock();
            }
        });
        assertTrue(done.await(2, TimeUnit.SECONDS));
        assertEquals(Looper.getMainLooper().getThread(), executorThread.get());
        lock.unlock();
    }
}
