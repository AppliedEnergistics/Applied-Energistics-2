package appeng.api.storage;

import appeng.api.stacks.AEKey;

@FunctionalInterface
public interface AEKeyFilter {
    static AEKeyFilter none() {
        return NoOpKeyFilter.INSTANCE;
    }

    boolean matches(AEKey what);
}
