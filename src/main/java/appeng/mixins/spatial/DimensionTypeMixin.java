package appeng.mixins.spatial;

import appeng.spatial.SpatialStorageDimensionIds;
import net.minecraft.server.IDynamicRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.DimensionType;
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
@Mixin(value = DimensionType.class)
public class DimensionTypeMixin {

    @Inject(method = "func_236027_a_", at = @At("TAIL"))
    private static void addRegistryDefaults(IDynamicRegistries.Impl registryTracker, CallbackInfoReturnable<?> cir) {

        registryTracker.func_239774_a_(SpatialStorageDimensionIds.DIMENSION_TYPE_ID,
                new DimensionType(OptionalLong.of(12000), false, false, false, false, false, false, true, false, false,
                        256, BlockTags.INFINIBURN_OVERWORLD.getName(), 1.0f));

    }

}
