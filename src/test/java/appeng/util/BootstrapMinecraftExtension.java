package appeng.util;

import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.entrypoint.EntrypointUtils;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

import appeng.client.guidebook.Guide;
import appeng.core.AEConfig;
import appeng.core.AppEngBootstrap;

public class BootstrapMinecraftExtension implements Extension, BeforeAllCallback, AfterAllCallback {

    private static boolean modInitialized;

    Path configDir;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        AppEngBootstrap.runEarlyStartup();

        configDir = Files.createTempDirectory("ae2config");
        if (AEConfig.instance() == null) {
            AEConfig.load(configDir);
        }

        if (!modInitialized) {
            modInitialized = true;

            EntrypointUtils.invoke("preLaunch", PreLaunchEntrypoint.class, PreLaunchEntrypoint::onPreLaunch);
            EntrypointUtils.invoke("main", ModInitializer.class, ModInitializer::onInitialize);
            EntrypointUtils.invoke("server", DedicatedServerModInitializer.class,
                    DedicatedServerModInitializer::onInitializeServer);

            Guide.runDatapackReload();
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (configDir != null && Files.exists(configDir)) {
            MoreFiles.deleteRecursively(configDir, RecursiveDeleteOption.ALLOW_INSECURE);
        }
    }
}
