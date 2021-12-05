package appeng.mixins.structure;

import java.util.function.BiConsumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;

import appeng.api.features.AEWorldGenType;
import appeng.init.worldgen.InitBiomeModifications;
import appeng.worldgen.meteorite.MeteoriteStructure;

@Mixin(StructureFeatures.class)
public class StructureFeaturesMixin {
    @Inject(method = "registerStructures", at = @At("TAIL"))
    private static void registerStructures(BiConsumer<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> consumer,
            CallbackInfo ci) {
        for (var entry : BuiltinRegistries.BIOME.entrySet()) {
            var key = entry.getKey();
            var category = entry.getValue().getBiomeCategory();
            if (InitBiomeModifications.shouldGenerateIn(key.location(), true, AEWorldGenType.METEORITES, category)) {
                consumer.accept(MeteoriteStructure.CONFIGURED_INSTANCE, key);
            }
        }
    }
}
