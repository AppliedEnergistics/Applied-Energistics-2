package appeng.mixins.datagen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.world.level.biome.Biome;

import appeng.init.worldgen.InitBiomes;

@Mixin(Biomes.class)
public class BiomesMixin {
    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void addBiomes(BootstapContext<Biome> context, CallbackInfo ci) {
        InitBiomes.init(context);
    }
}
