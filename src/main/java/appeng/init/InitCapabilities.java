package appeng.init;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

import appeng.api.storage.IStorageMonitorableAccessor;

public final class InitCapabilities {
    private InitCapabilities() {
    }

    /**
     * Register AE2 provided capabilities.
     */
    public static void init(RegisterCapabilitiesEvent evt) {
        evt.register(IStorageMonitorableAccessor.class);
    }
}
