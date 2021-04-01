package appeng.mixins.spatial;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import appeng.spatial.SpatialStorageBiome;
import appeng.spatial.SpatialStorageDimensionIds;

/**
 * This only needs to be here because the server-side will create a dynamic registry manager long before our mod is
 * initialized.
 */
@Mixin(BiomeRegistry.class)
public class BuiltinBiomesMixin {

    @Unique
    private static final Logger LOGGER = LogManager.getLogger();

    @Shadow
    private static Int2ObjectMap<RegistryKey<Biome>> idToKeyMap;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void registerBiomes(CallbackInfo ci) {
        WorldGenRegistries.register(WorldGenRegistries.BIOME, SpatialStorageDimensionIds.BIOME_KEY.getLocation(),
                SpatialStorageBiome.INSTANCE);
        int rawId = WorldGenRegistries.BIOME.getId(SpatialStorageBiome.INSTANCE);
        RegistryKey<Biome> prev = idToKeyMap.put(rawId, SpatialStorageDimensionIds.BIOME_KEY);
        if (prev != null) {
            LOGGER.warn("Biome with raw-id {} was already registered: {}", rawId, prev);
        }
    }

}
