package appeng.api.features;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

/**
 * Internal access to non-public API of {@link AEWorldGen}.
 */
public class AEWorldGenInternal {

    public static void setConfigBlacklists(
            List<ResourceLocation> quartzBiomeBlacklist,
            List<ResourceLocation> meteoriteBiomeBlacklist) {
        AEWorldGen.setConfigBlacklists(quartzBiomeBlacklist, meteoriteBiomeBlacklist);
    }

}
