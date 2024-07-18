package appeng.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.FabricLoaderImpl;
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

            invokeEntrypoints("preLaunch", PreLaunchEntrypoint.class, PreLaunchEntrypoint::onPreLaunch);
            invokeEntrypoints("main", ModInitializer.class, ModInitializer::onInitialize);
            invokeEntrypoints("server", DedicatedServerModInitializer.class,
                    DedicatedServerModInitializer::onInitializeServer);

            Guide.runDatapackReload();
        }
    }

    private <T> void invokeEntrypoints(String name, Class<T> type, Consumer<? super T> invoker) {
        var entrypoints = FabricLoaderImpl.INSTANCE.getEntrypointContainers(name, type);

        for (var container : entrypoints) {
            var modId = container.getProvider().getMetadata().getId();
            // Fix WTHIT API runtime protection messing our tests up
            if (modId.equals("wthit_api")) {
                continue;
            }
            invoker.accept(container.getEntrypoint());
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (configDir != null && Files.exists(configDir)) {
            MoreFiles.deleteRecursively(configDir, RecursiveDeleteOption.ALLOW_INSECURE);
        }
    }
}
