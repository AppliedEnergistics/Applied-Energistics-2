package appeng.util;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

import appeng.core.AppEngBootstrap;
import appeng.core.CreativeTab;

public class BootstrapMinecraftExtension implements Extension, BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        AppEngBootstrap.runEarlyStartup();
        CreativeTab.init();
    }
}
