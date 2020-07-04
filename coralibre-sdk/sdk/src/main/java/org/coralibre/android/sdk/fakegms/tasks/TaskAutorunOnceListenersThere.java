package org.coralibre.android.sdk.fakegms.tasks;

public abstract class TaskAutorunOnceListenersThere<T> extends Task<T> {


    // TODO: Further implementation: For that see the calls in the 'Nearby' class, and how they are
    //  accessed in the warn app.


    private boolean runInvoked = false;
        // Set to true, once the task starts running, but never set to false again.


    public Task<T>
    addOnSuccessListener (OnSuccessListener<? super T> listener) {
        this.listenerSuccess = listener;
        runIfListenersSet();
        return this;
    }


    public Task<T>
    addOnFailureListener (OnFailureListener listener) {
        this.listenerFailure = listener;
        runIfListenersSet();
        return this;
    }


    protected void runIfListenersSet() {
        // We assume this
        if (!runInvoked && this.listenerFailure != null && this.listenerSuccess != null) {
            runInvoked = true;
            runInternal();
        }
    }


    public abstract void runInternal();


}
