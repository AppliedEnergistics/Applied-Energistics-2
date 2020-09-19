package appeng.mixins.spatial;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.BiomeRegistry;

import appeng.spatial.SpatialStorageBiome;
import appeng.spatial.SpatialStorageDimensionIds;

/**
 * This only needs to be here because the server-side will create a dynamic
 * registry manager long before our mod is initialized.
 */
@Mixin(BiomeRegistry.class)
public class BiomesMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void registerBiomes(CallbackInfo ci) {
        WorldGenRegistries.register(WorldGenRegistries.BIOME, SpatialStorageDimensionIds.BIOME_KEY.getLocation(),
                SpatialStorageBiome.INSTANCE);
    }

}
