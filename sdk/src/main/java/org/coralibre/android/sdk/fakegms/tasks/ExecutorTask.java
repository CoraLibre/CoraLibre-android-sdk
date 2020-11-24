package org.coralibre.android.sdk.fakegms.tasks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

final class ExecutorTask<T> extends Task<T> {

    private final FutureTask<T> task;
    /**
     * Contains all listeners to be called on success. Access should be synchronized by using the
     * ExecutorTask object as a lock.
     */
    private final List<OnSuccessListener<? super T>> successListeners;
    /**
     * Contains all listeners to be called on failure. Access should be synchronized by using the
     * ExecutorTask object as a lock.
     */
    private final List<OnFailureListener> failureListeners;

    ExecutorTask(@NonNull Executor executor, @NonNull Callable<T> callable) {
        this.successListeners = new LinkedList<>();
        this.failureListeners = new LinkedList<>();
        this.task = new FutureTask<>(callable);
        executor.execute(new ListenerInvoker(task));
    }

    @NonNull
    @Override
    public Task<T> addOnSuccessListener(OnSuccessListener<? super T> listener) {
        synchronized (this) {
            if (isSuccessful()) {
                // Call listener immediately, no need to add it to the list
                TaskExecutors.MAIN_THREAD.execute(() -> listener.onSuccess(getResult()));
            } else if (!isComplete()) {
                successListeners.add(listener);
            }
        }
        return this;
    }

    @NonNull
    @Override
    public Task<T> addOnFailureListener(OnFailureListener listener) {
        synchronized (this) {
            if (!isComplete()) {
                failureListeners.add(listener);
            } else if (!isSuccessful()) {
                // Call listener immediately, no need to add it to the list
                TaskExecutors.MAIN_THREAD.execute(() -> listener.onFailure(getException()));
            }
        }

        return this;
    }

    private T getResult() {
        if (!isComplete()) throw new IllegalStateException("Can't get result before completion.");
        try {
            return task.get();
        } catch (InterruptedException e) {
            // Can't happen because the task is done
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            throw new RuntimeExecutionException(e.getCause());
        }
    }

    private <E extends Throwable> T getResult(Class<E> exceptionClass) throws E {
        if (!isComplete()) throw new IllegalStateException("Can't get result before completion.");
        try {
            return task.get();
        } catch (InterruptedException e) {
            // Can't happen because the task is done
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (exceptionClass.isInstance(cause)) {
                E error = exceptionClass.cast(cause);
                assert error != null;
                throw error;
            } else {
                throw new RuntimeExecutionException(cause);
            }
        }
    }

    @Nullable
    private Exception getException() {
        if (!isComplete()) {
            return null;
        }
        try {
            task.get();
        } catch (InterruptedException e) {
            // Can't happen because the task is done
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                return (Exception) cause;
            } else {
                return new Exception(cause);
            }
        }

        return null;
    }

    @Override
    public boolean isComplete() {
        return task.isDone();
    }

    @Override
    public boolean isSuccessful() {
        return isComplete() && getException() == null;
    }

    private void invokeListeners() {
        Executor mainExecutor = TaskExecutors.MAIN_THREAD;
        synchronized (this) {
            if (isSuccessful()) {
                T result = getResult();
                for (OnSuccessListener<? super T> listener : successListeners) {
                    mainExecutor.execute(() -> listener.onSuccess(result));
                }
                // The listeners won't be called again, so we can clear the list.
                successListeners.clear();
            } else {
                Exception exception = getException();
                for (OnFailureListener listener : failureListeners) {
                    mainExecutor.execute(() -> listener.onFailure(exception));
                }
                // The listeners won't be called again, so we can clear the list.
                failureListeners.clear();
            }
        }
    }

    private class ListenerInvoker implements Runnable {
        private final Runnable delegate;

        private ListenerInvoker(Runnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            delegate.run();
            invokeListeners();
        }
    }
}
