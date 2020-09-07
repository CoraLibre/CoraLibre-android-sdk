package org.coralibre.android.sdk.fakegms.tasks;

import androidx.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

public final class Tasks {
    private Tasks() {
    }

    /**
     * Returns a completed Task with the specified result.
     */
    @NonNull
    public static <T> Task<T> forResult(T result) {
        return new ResultTask<>(result);
    }

    @NonNull
    public static <T> Task<T> call(@NonNull Callable<T> callable) {
        return call(TaskExecutors.MAIN_THREAD, callable);
    }

    @NonNull
    public static <T> Task<T> call(@NonNull Executor executor, @NonNull Callable<T> callable) {
        return new ExecutorTask<>(executor, callable);
    }
}
