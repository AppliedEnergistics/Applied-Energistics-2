package appeng.init.internal;

import appeng.api.features.IRegistryContainer;
import appeng.api.features.IWirelessTermHandler;
import appeng.core.Api;
import appeng.core.api.definitions.ApiItems;

public final class InitWirelessHandlers {

    private InitWirelessHandlers() {
    }

    public static void init() {
        final IRegistryContainer registries = Api.instance().registries();

        // Wireless Terminal Handler
        registries.wireless().registerWirelessHandler((IWirelessTermHandler) ApiItems.WIRELESS_TERMINAL.item());
    }

}
