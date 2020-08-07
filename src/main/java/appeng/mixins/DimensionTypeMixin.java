package appeng.mixins;

import java.util.OptionalLong;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.IDynamicRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.DimensionType;

import appeng.spatial.SpatialDimensionManager;

/**
 * Adds the storage cell world dimension type as a built-in dimension type.
 */
@Mixin(value = DimensionType.class)
public class DimensionTypeMixin {

    @Inject(method = "func_236027_a_", at = @At("TAIL"))
    private static void addRegistryDefaults(IDynamicRegistries.Impl registryTracker, CallbackInfoReturnable<?> cir) {

        registryTracker.func_239774_a_(SpatialDimensionManager.STORAGE_DIMENSION_TYPE,
                new DimensionType(OptionalLong.of(12000), false, false, false, false, false, false, true, false, false,
                        256, BlockTags.INFINIBURN_OVERWORLD.getName(), 1.0f));

    }

}
