package org.coralibre.android.sdk.fakegms.tasks;

import androidx.annotation.NonNull;

/**
 * Represents an asynchronous operation.
 * <p>
 * Minimal Task class providing interfaces currently required by the RKI app.
 */
public abstract class Task<T> {
    /**
     * Returns true if the Task is complete; false otherwise.
     * A Task is complete if it is done running, regardless of whether it was successful or
     * has been cancelled.
     */
    public abstract boolean isComplete();

    /**
     * Returns true if the Task has completed successfully; false otherwise.
     */
    public abstract boolean isSuccessful();

    /**
     * Adds a listener that is called if the Task completes successfully.
     * <p>
     * The listener will be called on the main application thread.
     * If the Task has already completed successfully, a call to the listener will be immediately
     * scheduled. If multiple listeners are added, they will be called in the order in which they
     * were added.
     *
     * @return this Task
     */
    @NonNull
    public abstract Task<T> addOnSuccessListener(OnSuccessListener<? super T> listener);

    /**
     * Adds a listener that is called if the Task fails.
     * <p>
     * The listener will be called on main application thread. If the Task has already failed,
     * a call to the listener will be immediately scheduled. If multiple listeners are added,
     * they will be called in the order in which they were added.
     * <p>
     * A canceled Task is not a failure Task.
     * This listener will not trigger if the Task is canceled.
     *
     * @return this Task
     */
    @NonNull
    public abstract Task<T> addOnFailureListener(OnFailureListener listener);
}
