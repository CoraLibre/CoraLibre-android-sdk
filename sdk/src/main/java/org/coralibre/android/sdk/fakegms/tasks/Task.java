package org.coralibre.android.sdk.fakegms.tasks;

import androidx.annotation.NonNull;

/**
 * Minimal Task class providing interfaces currently required by the RKI app.
 */
public abstract class Task<T> {
    public abstract boolean isComplete();

    public abstract boolean isSuccessful();

    @NonNull
    public abstract Task<T> addOnSuccessListener(OnSuccessListener<? super T> listener);

    @NonNull
    public abstract Task<T> addOnFailureListener(OnFailureListener listener);
}
