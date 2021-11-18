package appeng.core;

import net.fabricmc.api.ClientModInitializer;

import appeng.datagen.DatagenEntrypoint;

@SuppressWarnings("unused")
public class AppEngClientStartup implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        new AppEngClient();
        DatagenEntrypoint.runIfEnabled();
    }
}
