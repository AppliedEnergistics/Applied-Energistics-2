package appeng.mixins;

import appeng.hooks.RegisterDimensionTypeCallback;
import net.minecraft.util.registry.RegistryTracker;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionType.class)
public class DimensionTypeMixin {

    @Inject(method="addRegistryDefaults", at=@At("TAIL"))
    private static void addRegistryDefaults(RegistryTracker.Modifiable registryTracker, CallbackInfoReturnable<?> cir) {
        RegisterDimensionTypeCallback.EVENT.invoker().addDimensionTypes(registryTracker);
    }

}
