package appeng.mixins.spatial;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BuiltinBiomes;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import appeng.spatial.SpatialStorageBiome;
import appeng.spatial.SpatialStorageDimensionIds;

/**
 * This only needs to be here because the server-side will create a dynamic
 * registry manager long before our mod is initialized.
 */
@Mixin(BuiltinBiomes.class)
public class BuiltinBiomesMixin {

    @Unique
    private static final Logger LOGGER = LogManager.getLogger();

    @Shadow
    private static Int2ObjectMap<RegistryKey<Biome>> BY_RAW_ID;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void registerBiomes(CallbackInfo ci) {
        BuiltinRegistries.add(BuiltinRegistries.BIOME, SpatialStorageDimensionIds.BIOME_KEY.getValue(),
                SpatialStorageBiome.INSTANCE);
        int rawId = BuiltinRegistries.BIOME.getRawId(SpatialStorageBiome.INSTANCE);
        RegistryKey<Biome> prev = BY_RAW_ID.put(rawId, SpatialStorageDimensionIds.BIOME_KEY);
        if (prev != null) {
            LOGGER.warn("Biome with raw-id {} was already registered: {}", rawId, prev);
        }
    }

}
