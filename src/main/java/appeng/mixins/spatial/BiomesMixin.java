package appeng.mixins.spatial;

import appeng.spatial.SpatialStorageBiome;
import appeng.spatial.SpatialStorageDimensionIds;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.BiomeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This only needs to be here because the server-side will create a dynamic
 * registry manager long before our mod is initialized.
 */
@Mixin(BiomeRegistry.class)
public class BiomesMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void registerBiomes(CallbackInfo ci) {
        WorldGenRegistries.func_243664_a(WorldGenRegistries.field_243657_i, SpatialStorageDimensionIds.BIOME_KEY.func_240901_a_(),
                SpatialStorageBiome.INSTANCE);
    }

}
