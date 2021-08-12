package appeng.api.features;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

import net.minecraft.resources.ResourceLocation;

/**
 * Allows other mods to interact with AE2's world generation.
 * <p/>
 * This class is thread-safe and may be used in your mod's constructor.
 */
@ThreadSafe
public class AEWorldGen {

    private static final Map<AEWorldGenType, TypeSet> settings = new EnumMap<>(AEWorldGenType.class);

    static {
        for (AEWorldGenType type : AEWorldGenType.values()) {
            settings.put(type, new TypeSet());
        }
    }

    /**
     * Forces a given AE2 world-generation type to be disabled for a given biome.
     */
    public static void disableWorldGenForBiome(AEWorldGenType type, ResourceLocation biomeId) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(biomeId);

        settings.get(type).modBiomeBlacklist.add(biomeId);
    }

    /**
     * Checks if the given world-generation type is disabled for the given biome id.
     * <p>
     * This also takes AE2's configuration file into account.
     */
    public static boolean isWorldGenDisabledForBiome(AEWorldGenType type, ResourceLocation biomeId) {
        TypeSet typeSettings = settings.get(type);
        return typeSettings.configBiomeBlacklist.contains(biomeId)
                || typeSettings.modBiomeBlacklist.contains(biomeId);
    }

    /**
     * This is used by AE2 to set the biome blacklist from AE2's own configuration file.
     */
    static void setConfigBlacklists(
            List<ResourceLocation> quartzBiomeBlacklist,
            List<ResourceLocation> meteoriteBiomeBlacklist) {
        settings.get(AEWorldGenType.CERTUS_QUARTZ).configBiomeBlacklist.clear();
        settings.get(AEWorldGenType.CERTUS_QUARTZ).configBiomeBlacklist.addAll(quartzBiomeBlacklist);
        settings.get(AEWorldGenType.CHARGED_CERTUS_QUARTZ).configBiomeBlacklist.clear();
        settings.get(AEWorldGenType.CHARGED_CERTUS_QUARTZ).configBiomeBlacklist.addAll(quartzBiomeBlacklist);
        settings.get(AEWorldGenType.METEORITES).configBiomeBlacklist.clear();
        settings.get(AEWorldGenType.METEORITES).configBiomeBlacklist.addAll(meteoriteBiomeBlacklist);
    }

    private static class TypeSet {
        /**
         * Biomes blacklisted by other mods.
         */
        final Set<ResourceLocation> modBiomeBlacklist = new HashSet<>();
        /**
         * Biomes blacklisted in the user's config.
         */
        final Set<ResourceLocation> configBiomeBlacklist = new HashSet<>();
    }

}
