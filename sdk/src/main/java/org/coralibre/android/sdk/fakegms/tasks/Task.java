package org.coralibre.android.sdk.fakegms.tasks;

public abstract class Task<T> {
    // Minimal Task class providing interfaces currently required by the RKI app.


    protected OnSuccessListener<? super T> listenerSuccess = null;
    protected OnFailureListener listenerFailure = null;


    public Task<T>
    addOnSuccessListener (OnSuccessListener<? super T> listener) {
        this.listenerSuccess = listener;
        return this;
    }

    public Task<T>
    addOnFailureListener (OnFailureListener listener) {
        this.listenerFailure = listener;
        return this;
    }

}
