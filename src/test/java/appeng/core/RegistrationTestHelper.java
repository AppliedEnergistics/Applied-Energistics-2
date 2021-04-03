package appeng.core;

import appeng.capabilities.Capabilities;

/**
 * Sets up internal registries needed for creating proper Grids that are usually part of
 * the common setup phase.
 */
public class RegistrationTestHelper {

    public static void setupInternals() {
        Capabilities.register();
        Registration.setupInternalRegistries();
        Registration.postInit();
    }

}