package appeng.core;

import net.fabricmc.api.ClientModInitializer;

import appeng.client.AppEngClient;

@SuppressWarnings("unused")
public class AppEngClientStartup implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AppEngHolder.INSTANCE = new AppEngClient();
    }
}
