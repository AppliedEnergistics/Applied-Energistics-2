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

    public StackWatcher(InterestManager<StackWatcher<T>> interestManager, T host) {
        this.interestManager = interestManager;
        this.myHost = host;
    }

    public T getHost() {
        return this.myHost;
    }

    @Override
    public void setWatchAll(boolean watchAll) {
        interestManager.setWatchAll(watchAll, this);
    }

    @Override
    public boolean add(AEKey e) {
        if (this.myInterests.contains(e)) {
            return false;
        }

        return this.myInterests.add(e) && interestManager.put(e, this);
    }

    @Override
    public boolean remove(AEKey o) {
        return this.myInterests.remove(o) && interestManager.remove(o, this);
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
}
