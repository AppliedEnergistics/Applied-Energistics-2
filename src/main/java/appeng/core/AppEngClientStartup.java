package appeng.core;

import net.fabricmc.api.ClientModInitializer;

@SuppressWarnings("unused")
public class AppEngClientStartup implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        new AppEngClient();
    }
}
