package org.coralibre.android.sdk.fakegms.tasks;

import android.os.Looper;

import org.coralibre.android.sdk.AsyncTestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ResultTaskTest {
    private static final String TEST_STRING = "Test";
    private Task<String> stringTask;
    private Task<Void> nullTask;

    @Rule
    public final ExpectedException expected = ExpectedException.none();

    @Before
    public void initializeTasks() {
        stringTask = new ResultTask<>(TEST_STRING);
        nullTask = new ResultTask<>(null);
    }

    @Test
    public void nullIsSuccessfulAndComplete() {
        assertTrue(nullTask.isSuccessful());
        assertTrue(nullTask.isComplete());
    }

    @Test
    public void nonnullIsSuccessful() {
        assertTrue(stringTask.isSuccessful());
        assertTrue(stringTask.isComplete());
    }

    @Test
    public void nonnullSingleSuccessListenerCalled() throws InterruptedException {
        String value = AsyncTestUtil.waitUntilSet((result, continuation) -> {
            stringTask.addOnSuccessListener((s) -> {
                result.set(s);
                continuation.run();
            });
        });

        assertEquals(TEST_STRING, value);
    }

    @Test
    public void nullSingleSuccessListenerCalled() throws InterruptedException {
        Void value = AsyncTestUtil.waitUntilSet((result, continuation) -> {
            nullTask.addOnSuccessListener((s) -> {
                result.set(s);
                continuation.run();
            });
        });

        assertNull(value);
    }

    @Test
    public void successListenerCalledOnMainThread() throws InterruptedException {
        Thread executingThread = AsyncTestUtil.waitUntilSet((result, continuation) -> {
            stringTask.addOnSuccessListener((s) -> {
                result.set(Thread.currentThread());
                continuation.run();
            });
        });

        assertEquals(Looper.getMainLooper().getThread(), executingThread);
    }

    @Test
    public void failureListenerNotCalled() throws InterruptedException {
        expected.expect(InterruptedException.class);
        AsyncTestUtil.waitUntilSet((result, continuation) -> {
            stringTask.addOnFailureListener((e) -> {
                result.set(e);
                continuation.run();
            });
        });
    }
}
