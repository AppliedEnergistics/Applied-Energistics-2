package appeng.core;

import net.fabricmc.api.DedicatedServerModInitializer;

@SuppressWarnings("unused")
public class AppEngServerStartup implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        new AppEngServer();
    }
}
