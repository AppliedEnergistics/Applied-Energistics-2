package appeng.mixins.datagen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.StructureSets;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import appeng.init.worldgen.InitStructures;

@Mixin(StructureSets.class)
public interface StructureSetsMixin {
    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void addStructureSets(BootstapContext<StructureSet> context, CallbackInfo ci) {
        InitStructures.initDatagenStructureSets(context);
    }
}
