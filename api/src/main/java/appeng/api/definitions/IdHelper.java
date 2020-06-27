package appeng.api.definitions;

import net.minecraft.util.ResourceLocation;

// Private helper class to create AE2 resource locations
final class IdHelper {

    private IdHelper() {
    }

    /**
     * Creates a ResourceLocation namespaced to AE2.
     */
    static ResourceLocation id(String id) {
        return new ResourceLocation("appliedenergistics2", id);
    }

}
