package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.dimension.DimensionType;

import appeng.hooks.RegisterDimensionTypeCallback;

@Mixin(DimensionType.class)
public class DimensionTypeMixin {

    @Inject(method = "addRegistryDefaults", at = @At("TAIL"))
    private static void addRegistryDefaults(DynamicRegistryManager.Impl registryTracker,
            CallbackInfoReturnable<?> cir) {
        RegisterDimensionTypeCallback.EVENT.invoker().addDimensionTypes(registryTracker);
    }

}
