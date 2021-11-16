package appeng.util;

import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.fabricmc.loader.impl.launch.LauncherAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

import appeng.core.AEConfig;
import appeng.core.AppEngBootstrap;

public class BootstrapMinecraftExtension implements Extension, BeforeAllCallback, AfterAllCallback {

    Path configDir;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        LauncherAccessor.init();

        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        AppEngBootstrap.runEarlyStartup();

        configDir = Files.createTempDirectory("ae2config");
        if (AEConfig.instance() == null) {
            AEConfig.load(configDir.toFile());
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (configDir != null && Files.exists(configDir)) {
            MoreFiles.deleteRecursively(configDir, RecursiveDeleteOption.ALLOW_INSECURE);
        }
    }
}
