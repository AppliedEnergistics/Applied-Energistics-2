package appeng.me.helpers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import appeng.api.networking.IStackWatcher;
import appeng.api.stacks.AEKey;

/**
 * Maintain my interests, and a global watch list, they should always be fully synchronized.
 */
public class StackWatcher<T> implements IStackWatcher {

    private final InterestManager<StackWatcher<T>> interestManager;
    private final T myHost;
    private final Set<AEKey> myInterests = new HashSet<>();
    private boolean destroyed = false;

    public StackWatcher(InterestManager<StackWatcher<T>> interestManager, T host) {
        this.interestManager = interestManager;
        this.myHost = host;
    }

    public T getHost() {
        return this.myHost;
    }

    @Override
    public void setWatchAll(boolean watchAll) {
        if (!destroyed) {
            interestManager.setWatchAll(watchAll, this);
        }
    }

    @Override
    public void add(AEKey e) {
        if (!destroyed && this.myInterests.add(e)) {
            interestManager.put(e, this);
        }
    }

    @Override
    public void remove(AEKey o) {
        if (!destroyed && this.myInterests.remove(o)) {
            interestManager.remove(o, this);
        }
    }

    @Override
    public void reset() {
        setWatchAll(false);

        final Iterator<AEKey> i = this.myInterests.iterator();

        while (i.hasNext()) {
            interestManager.remove(i.next(), this);
            i.remove();
        }
    }

    /**
     * Call this when the watcher is not going to be used anymore, to reset it and disable it forever. It's important
     * that we disable the watcher, since some hosts (e.g. level emitter) might still hold a reference to it and try to
     * modify it later, which could lead to invalid state and potentially crashes down the line.
     */
    public void destroy() {
        reset();
        destroyed = true;
    }
}
