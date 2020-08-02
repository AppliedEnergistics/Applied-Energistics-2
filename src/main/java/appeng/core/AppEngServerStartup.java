package appeng.core;

import appeng.server.AppEngServer;
import net.fabricmc.api.DedicatedServerModInitializer;

@SuppressWarnings("unused")
public class AppEngServerStartup implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        AppEngHolder.INSTANCE = new AppEngServer();
    }
}
