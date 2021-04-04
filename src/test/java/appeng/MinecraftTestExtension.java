package appeng;

import appeng.core.AppEng;
import appeng.core.RegistrationTestHelper;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModWorkManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * This extension will initialize Minecraft registries and start mods, but only do
 * it for the first test that requests it.
 */
public class MinecraftTestExtension implements BeforeAllCallback {

    private static final Logger LOGGER = LogManager.getLogger();

    private static boolean initialized = false;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (initialized) {
            return;
        }
        initialized = true;

        LOGGER.info("Initializing Minecraft");
        Bootstrap.register();

        LOGGER.info("Initializing Mods");
        ModLoader modLoader = ModLoader.get();
        modLoader.gatherAndInitializeMods(ModWorkManager.syncExecutor(), ModWorkManager.parallelExecutor(), () -> {
        });

        RegistrationTestHelper.setupInternals();

        MinecraftForge.EVENT_BUS.start();
    }

}
