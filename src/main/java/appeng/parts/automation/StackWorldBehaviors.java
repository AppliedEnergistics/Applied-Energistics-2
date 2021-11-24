package appeng.parts.automation;

import appeng.api.storage.AEKeyFilter;

public final class StackWorldBehaviors {
    private StackWorldBehaviors() {
    }

    /**
     * {@return filter that matches any key for which there is in-world behavior}
     */
    public static AEKeyFilter supportedFilter() {
        return what -> true;
    }
}
