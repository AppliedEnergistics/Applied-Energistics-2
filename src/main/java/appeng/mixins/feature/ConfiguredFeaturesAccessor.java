package appeng.mixins.feature;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.IFeatureConfig;

@Mixin(Features.class)
public interface ConfiguredFeaturesAccessor {

    @Invoker("register")
    static <FC extends IFeatureConfig> ConfiguredFeature<FC, ?> register(String id,
            ConfiguredFeature<FC, ?> configuredFeature) {
        throw new AssertionError();
    }

}
