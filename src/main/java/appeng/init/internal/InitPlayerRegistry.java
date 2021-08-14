package appeng.init.internal;

import appeng.api.features.PlayerRegistryInternal;

public final class InitPlayerRegistry {

    private InitPlayerRegistry() {
    }

    public static void init() {
        PlayerRegistryInternal.init();
    }

}
