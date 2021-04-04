package appeng.core;

import appeng.capabilities.Capabilities;
import appeng.core.sync.network.NetworkHandler;
import net.minecraft.util.ResourceLocation;

/**
 * Sets up internal registries needed for creating proper Grids that are usually part of
 * the common setup phase.
 */
public class RegistrationTestHelper {

    public static void setupInternals() {
        Capabilities.register();
        Registration.setupInternalRegistries();
        Registration.postInit();
        NetworkHandler.init(new ResourceLocation(AppEng.MOD_ID, "main"));
    }

}