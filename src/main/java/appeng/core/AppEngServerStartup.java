package appeng.core;

import net.fabricmc.api.DedicatedServerModInitializer;

import appeng.server.AppEngServer;

@SuppressWarnings("unused")
public class AppEngServerStartup implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        AppEngHolder.INSTANCE = new AppEngServer();
    }
}
