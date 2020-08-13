package appeng.mixins.structure;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureFeatures;

@Mixin(StructureFeatures.class)
public interface ConfiguredStructureFeaturesAccessor {

    @Invoker("func_244162_a")
    static <FC extends IFeatureConfig, F extends Structure<FC>> StructureFeature<FC, F> register(String id,
            StructureFeature<FC, F> configuredStructureFeature) {
        throw new AssertionError();
    }

}
