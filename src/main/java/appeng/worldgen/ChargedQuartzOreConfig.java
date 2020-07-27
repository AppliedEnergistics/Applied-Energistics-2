package appeng.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;

/**
 * Extends a {@link OreFeatureConfig} with a chance.
 */
public class ChargedQuartzOreConfig implements FeatureConfig {

    public static final Codec<ChargedQuartzOreConfig> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(BlockState.CODEC.fieldOf("target").forGetter((config) -> config.target),
                    BlockState.CODEC.fieldOf("state").forGetter((config) -> config.state),
                    Codec.FLOAT.fieldOf("chance").withDefault(0f).forGetter((config) -> config.chance))
            .apply(instance, ChargedQuartzOreConfig::new));

    public final BlockState target;
    public final BlockState state;
    public final float chance;

    public ChargedQuartzOreConfig(BlockState target, BlockState state, float chance) {
        this.target = target;
        this.state = state;
        this.chance = chance;
    }

}
