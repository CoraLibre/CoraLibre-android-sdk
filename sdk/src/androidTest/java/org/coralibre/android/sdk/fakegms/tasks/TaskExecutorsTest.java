package org.coralibre.android.sdk.fakegms.tasks;

import android.os.Looper;

import org.coralibre.android.sdk.AsyncTestUtil;
import org.junit.Test;

import java.util.concurrent.Executor;

import static org.junit.Assert.assertEquals;

public class TaskExecutorsTest {

    @Test
    public void executedOnMainThread() throws InterruptedException {
        Executor executor = TaskExecutors.MAIN_THREAD;
        Thread executorThread = AsyncTestUtil.waitUntilSet((result) -> {
            executor.execute(() -> result.set(Thread.currentThread()));
        });

        assertEquals(Looper.getMainLooper().getThread(), executorThread);
    }
}
