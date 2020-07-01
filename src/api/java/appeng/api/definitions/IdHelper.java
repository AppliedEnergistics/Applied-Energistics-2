package appeng.api.definitions;

import net.minecraft.util.Identifier;

// Private helper class to create AE2 resource locations
final class IdHelper {

    private IdHelper() {
    }

    /**
     * Creates a ResourceLocation namespaced to AE2.
     */
    static Identifier id(String id) {
        return new Identifier("appliedenergistics2", id);
    }

}
