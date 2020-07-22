package appeng.worldgen;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.ReplaceBlockConfig;

import appeng.core.AEConfig;

/**
 * Extends a {@link ReplaceBlockConfig} with a chance.
 */
public class ChargedQuartzOreConfig implements IFeatureConfig {

    public final BlockState target;
    public final BlockState state;
    public final float chance;

    public ChargedQuartzOreConfig(BlockState target, BlockState state, float chance) {
        this.target = target;
        this.state = state;
        this.chance = chance;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
        return new Dynamic<>(ops,
                ops.createMap(
                        ImmutableMap.of(ops.createString("target"), BlockState.serialize(ops, this.target).getValue(),
                                ops.createString("state"), BlockState.serialize(ops, this.state).getValue(),
                                ops.createString("chance"), ops.createFloat(this.chance))));
    }

    public static <T> ChargedQuartzOreConfig deserialize(Dynamic<T> p_214657_0_) {
        BlockState target = p_214657_0_.get("target").map(BlockState::deserialize).orElse(Blocks.AIR.getDefaultState());
        BlockState state = p_214657_0_.get("state").map(BlockState::deserialize).orElse(Blocks.AIR.getDefaultState());
        float chance = p_214657_0_.get("chance").asFloat(AEConfig.instance().getSpawnChargedChance());
        return new ChargedQuartzOreConfig(target, state, chance);
    }

}
