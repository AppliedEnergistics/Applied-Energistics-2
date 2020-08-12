package appeng.mixins.structure;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeatures;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

@Mixin(ConfiguredStructureFeatures.class)
public interface ConfiguredStructureFeaturesAccessor {

    @Invoker("register")
    static <FC extends FeatureConfig, F extends StructureFeature<FC>> ConfiguredStructureFeature<FC, F> register(
            String id, ConfiguredStructureFeature<FC, F> configuredStructureFeature) {
        throw new AssertionError();
    }

}
