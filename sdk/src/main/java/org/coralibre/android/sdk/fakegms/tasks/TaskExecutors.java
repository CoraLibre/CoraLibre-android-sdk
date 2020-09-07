package org.coralibre.android.sdk.fakegms.tasks;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

public final class TaskExecutors {
    private TaskExecutors() {
    }

    public static final Executor MAIN_THREAD = new MainTaskExecutor();

    private static final class MainTaskExecutor implements Executor {
        private final Handler handler;

        MainTaskExecutor() {
            this.handler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void execute(@NonNull Runnable command) {
            if (!handler.post(command)) {
                throw new RejectedExecutionException();
            }
        }
    }
}
