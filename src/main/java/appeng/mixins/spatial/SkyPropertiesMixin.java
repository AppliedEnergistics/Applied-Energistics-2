package appeng.mixins.spatial;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.SkyProperties;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionType;

import appeng.spatial.SpatialStorageDimensionIds;
import appeng.spatial.SpatialStorageSkyProperties;

@Mixin(SkyProperties.class)
public class SkyPropertiesMixin {

    @Inject(method = "byDimensionType", at = @At("HEAD"), cancellable = true)
    private static void byDimensionType(Optional<RegistryKey<DimensionType>> optional,
            CallbackInfoReturnable<SkyProperties> ci) {
        if (optional.orElse(null) == SpatialStorageDimensionIds.DIMENSION_TYPE_ID) {
            ci.setReturnValue(SpatialStorageSkyProperties.INSTANCE);
        }
    }

}
