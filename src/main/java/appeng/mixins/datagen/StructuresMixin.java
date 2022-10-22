package appeng.mixins.datagen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.Structures;
import net.minecraft.world.level.levelgen.structure.Structure;

import appeng.init.worldgen.InitStructures;

@Mixin(Structures.class)
public class StructuresMixin {
    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void addStructures(BootstapContext<Structure> context, CallbackInfo ci) {
        InitStructures.initDatagenStructures(context);
    }
}
