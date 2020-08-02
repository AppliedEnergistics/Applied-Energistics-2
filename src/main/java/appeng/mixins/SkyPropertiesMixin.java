package appeng.mixins;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.SkyProperties;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionType;

import appeng.spatial.SpatialDimensionManager;
import appeng.spatial.StorageSkyProperties;

@Mixin(SkyProperties.class)
public class SkyPropertiesMixin {

    @Inject(method = "byDimensionType", at = @At("HEAD"), cancellable = true)
    private static void byDimensionType(Optional<RegistryKey<DimensionType>> optional,
            CallbackInfoReturnable<SkyProperties> ci) {
        if (optional.orElse(null) == SpatialDimensionManager.STORAGE_DIMENSION_TYPE) {
            ci.setReturnValue(StorageSkyProperties.INSTANCE);
        }
    }

}
