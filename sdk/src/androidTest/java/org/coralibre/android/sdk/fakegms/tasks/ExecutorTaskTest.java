package org.coralibre.android.sdk.fakegms.tasks;

import android.os.Looper;

import androidx.annotation.NonNull;

import org.coralibre.android.sdk.AsyncTestUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExecutorTaskTest {
    private static final String TEST_STRING = "teststring";
    private Thread thread;
    private Executor executor;

    @Before
    public void initExecutor() {
        thread = null;
        executor = Executors.newSingleThreadExecutor((r) -> {
            if (thread != null) {
                throw new IllegalStateException();
            }
            thread = new Thread(r);
            return thread;
        });
    }

    @Test
    public void runnableCalledWithExecutor() throws InterruptedException {
        Thread calling = AsyncTestUtil.waitUntilSet(((result) -> {
            new ExecutorTask<Void>(executor, () -> {
                result.set(Thread.currentThread());
                return null;
            });
        }));
        assertEquals(thread, calling);
    }

    private void awaitTask(Task<?> task) throws InterruptedException {
        // busy waiting is kind of awful, but simple and doesn't need testing of its own
        while (!task.isComplete()) {
            Thread.sleep(50);
        }
    }

    @NonNull
    private Thread getMainThread() {
        return Looper.getMainLooper().getThread();
    }

    @Test
    public void immediateSuccessCalled() throws InterruptedException {
        Task<String> task = new ExecutorTask<>(executor, () -> TEST_STRING);
        awaitTask(task);
        String value = AsyncTestUtil.waitUntilSet((result) -> {
            task.addOnSuccessListener((v) -> result.set(v));
        });
        assertEquals(TEST_STRING, value);
    }

    @Test
    public void immediateSuccessCalledOnMainThread() throws InterruptedException {
        Task<String> task = new ExecutorTask<>(executor, () -> TEST_STRING);
        awaitTask(task);
        Thread thread = AsyncTestUtil.waitUntilSet((result) -> {
            task.addOnSuccessListener((v) -> result.set(Thread.currentThread()));
        });
        assertEquals(getMainThread(), thread);
    }

    @Test
    public void immediateFailureCalled() throws InterruptedException {
        Exception expectedError = new Exception();
        Task<String> task = new ExecutorTask<>(executor, () -> {
            throw expectedError;
        });
        awaitTask(task);
        Exception error = AsyncTestUtil.waitUntilSet((result) -> {
            task.addOnFailureListener((e) -> result.set(e));
        });
        assertEquals(expectedError, error);
    }

    @Test
    public void immediateFailureCalledOnMainThread() throws InterruptedException {
        Task<String> task = new ExecutorTask<>(executor, () -> {
            throw new Exception();
        });
        awaitTask(task);
        Thread thread = AsyncTestUtil.waitUntilSet((result) -> {
            task.addOnFailureListener((e) -> result.set(Thread.currentThread()));
        });
        assertEquals(getMainThread(), thread);
    }

    @Test
    public void scheduledSuccessCalled() throws InterruptedException {
        Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();
        Task<String> task = new ExecutorTask<>(executor, () -> {
            semaphore.acquire();
            try {
                return TEST_STRING;
            } finally {
                semaphore.release();
            }
        });
        String value = AsyncTestUtil.waitUntilSet((result) -> {
            task.addOnSuccessListener((v) -> result.set(v));
            semaphore.release();
        });
        assertEquals(TEST_STRING, value);
    }

    @Test
    public void scheduledSuccessCalledOnMainThread() throws InterruptedException {
        Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();
        Task<String> task = new ExecutorTask<>(executor, () -> {
            semaphore.acquire();
            try {
                return TEST_STRING;
            } finally {
                semaphore.release();
            }
        });
        Thread thread = AsyncTestUtil.waitUntilSet((result) -> {
            task.addOnSuccessListener((v) -> result.set(Thread.currentThread()));
            semaphore.release();
        });
        assertEquals(getMainThread(), thread);
    }

    @Test
    public void scheduledFailureCalled() throws InterruptedException {
        Exception expectedError = new Exception();
        Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();
        Task<String> task = new ExecutorTask<>(executor, () -> {
            semaphore.acquire();
            try {
                throw expectedError;
            } finally {
                semaphore.release();
            }
        });
        Exception error = AsyncTestUtil.waitUntilSet((result) -> {
            task.addOnFailureListener((e) -> result.set(e));
            semaphore.release();
        });
        assertEquals(expectedError, error);
    }

    @Test
    public void scheduledFailureCalledOnMainThread() throws InterruptedException {
        Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();
        Task<String> task = new ExecutorTask<>(executor, () -> {
            semaphore.acquire();
            try {
                throw new Exception();
            } finally {
                semaphore.release();
            }
        });
        Thread thread = AsyncTestUtil.waitUntilSet((result) -> {
            task.addOnFailureListener((e) -> result.set(Thread.currentThread()));
            semaphore.release();
        });
        assertEquals(getMainThread(), thread);
    }

    @Test(timeout = 500)
    public void completes() throws InterruptedException {
        Task<Void> task = new ExecutorTask<>(executor, () -> null);
        awaitTask(task);
        assertTrue(task.isComplete());
    }

    @Test
    public void succeeds() throws InterruptedException {
        Task<Void> task = new ExecutorTask<>(executor, () -> null);
        awaitTask(task);
        assertTrue(task.isSuccessful());
    }

    @Test
    public void failsCheckedException() throws InterruptedException {
        Task<Void> task = new ExecutorTask<>(executor, () -> {
            throw new Exception();
        });
        awaitTask(task);
        assertFalse(task.isSuccessful());
    }

    @Test
    public void failsUncheckedException() throws InterruptedException {
        Task<Void> task = new ExecutorTask<>(executor, () -> {
            throw new RuntimeException();
        });
        awaitTask(task);
        assertFalse(task.isSuccessful());
    }
}
