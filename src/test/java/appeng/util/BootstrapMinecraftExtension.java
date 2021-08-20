package appeng.util;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.fabricmc.loader.launch.common.LauncherAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

import appeng.core.AppEngBootstrap;

public class BootstrapMinecraftExtension implements Extension, BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext context) {
        LauncherAccessor.init();

        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        AppEngBootstrap.runEarlyStartup();
    }
}
