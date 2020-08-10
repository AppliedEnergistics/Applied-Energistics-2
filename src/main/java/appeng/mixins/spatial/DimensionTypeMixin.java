package appeng.mixins.spatial;

import appeng.spatial.SpatialStorageDimensionIds;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalLong;

/**
 * Adds the storage cell world dimension type as a built-in dimension type. This
 * can be registered as a JSON file as well, but doing so will trigger an
 * experimental feature warning when the world is being loaded.
 */
@Mixin(DimensionType.class)
public class DimensionTypeMixin {

    @Inject(method = "addRegistryDefaults", at = @At("TAIL"))
    private static void addRegistryDefaults(DynamicRegistryManager.Impl registryTracker, CallbackInfoReturnable<?> cir) {
        DimensionType dimensionType = new DimensionType(OptionalLong.of(12000), false, false, false, false, false,
                false, true, false, false, 256, BlockTags.INFINIBURN_OVERWORLD.getId(), 1.0f);

        Registry.register(registryTracker.getDimensionTypes(),
                SpatialStorageDimensionIds.DIMENSION_TYPE_ID.getValue(), dimensionType);
    }

}
