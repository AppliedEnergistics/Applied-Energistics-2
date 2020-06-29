package appeng.core;

import appeng.client.AppEngClient;
import net.fabricmc.api.ClientModInitializer;

@SuppressWarnings("unused")
public class AppEngClientStartup implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AppEngHolder.INSTANCE = new AppEngClient();
    }
}
