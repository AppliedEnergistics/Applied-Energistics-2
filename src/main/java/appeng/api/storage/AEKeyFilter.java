package appeng.api.storage;

import appeng.api.storage.data.AEKey;

public interface AEKeyFilter {
    static AEKeyFilter none() {
        return NoOpKeyFilter.INSTANCE;
    }

    boolean matches(AEKey what);

    /**
     * Is it possible to enumerate all values in the filter?
     */
    default boolean isEnumerable() {
        return false;
    }
}
