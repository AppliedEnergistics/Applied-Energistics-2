package appeng.mixins;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.DimensionType;

import appeng.spatial.SpatialDimensionManager;
import appeng.spatial.StorageSkyProperties;

@Mixin(DimensionRenderInfo.class)
public class SkyPropertiesMixin {

    @Inject(method = "func_239215_a_", at = @At("HEAD"), cancellable = true)
    private static void byDimensionType(Optional<RegistryKey<DimensionType>> optional,
            CallbackInfoReturnable<DimensionRenderInfo> ci) {
        if (optional.orElse(null) == SpatialDimensionManager.STORAGE_DIMENSION_TYPE) {
            ci.setReturnValue(StorageSkyProperties.INSTANCE);
        }
    }

}
