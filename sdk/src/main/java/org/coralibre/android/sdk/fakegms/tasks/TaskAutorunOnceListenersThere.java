package org.coralibre.android.sdk.fakegms.tasks;

import androidx.annotation.NonNull;

/**
 * A class that waits until a success and a failure listener have been set and then runs the
 * {@link #runInternal()} method.
 *
 * @param <T> the result type of the task
 * @deprecated This subclass makes some very fragile assumptions: 1. It will run the
 * underlying task on the same thread that sets the last listener, quite possibly the UI thread.
 * 2. It requires both listeners to be set. 3. It assumes there won't be more than one listener per
 * type. 4. Listeners added after the task has been run won't get called. 5. The runInternal()
 * method is expected to call the listeners.
 */
@Deprecated
public abstract class TaskAutorunOnceListenersThere<T> extends Task<T> {
    private boolean runInvoked = false;
    private OnSuccessListener<? super T> listenerSuccess;
    private OnFailureListener listenerFailure;

    @Override
    public boolean isComplete() {
        return runInvoked;
    }

    protected OnSuccessListener<? super T> getListenerSuccess() {
        return listenerSuccess;
    }

    protected OnFailureListener getListenerFailure() {
        return listenerFailure;
    }

    /**
     * Checks if both a success listener and a failure listener have been set.
     *
     * @return true if both listeners are non-null
     * @deprecated this method is based on huge assumptions and shouldn't be used.
     */
    @Deprecated
    final boolean hasListeners() {
        return listenerFailure != null && listenerSuccess != null;
    }

    @Override
    public boolean isSuccessful() {
        // Doing legacy non-implementations a favor.
        return true;
    }

    // Set to true, once the task starts running, but never set to false again.
    @NonNull
    @Override
    public final Task<T> addOnSuccessListener(OnSuccessListener<? super T> listener) {
        this.listenerSuccess = listener;
        runIfListenersSet();
        return this;
    }

    @NonNull
    @Override
    public final Task<T> addOnFailureListener(OnFailureListener listener) {
        this.listenerFailure = listener;
        runIfListenersSet();
        return this;
    }

    private void runIfListenersSet() {
        // We assume this
        if (!runInvoked && hasListeners()) {
            runInvoked = true;
            runInternal();
        }
    }

    public abstract void runInternal();
}
