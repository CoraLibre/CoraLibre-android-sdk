package org.coralibre.android.sdk.fakegms.tasks;

import androidx.annotation.NonNull;

final class ResultTask<T> extends Task<T> {
    private final T value;

    ResultTask(T value) {
        this.value = value;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }

    @NonNull
    @Override
    public Task<T> addOnSuccessListener(OnSuccessListener<? super T> listener) {
        TaskExecutors.MAIN_THREAD.execute(() -> listener.onSuccess(value));
        return this;
    }

    @NonNull
    @Override
    public Task<T> addOnFailureListener(OnFailureListener listener) {
        return this;
    }
}
