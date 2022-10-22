package appeng.mixins.datagen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;

import appeng.init.worldgen.InitDimensionTypes;

@Mixin(DimensionTypes.class)
public class DimensionTypesMixin {
    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void addDimensionTypes(BootstapContext<DimensionType> context, CallbackInfo ci) {
        InitDimensionTypes.init(context);
    }
}
