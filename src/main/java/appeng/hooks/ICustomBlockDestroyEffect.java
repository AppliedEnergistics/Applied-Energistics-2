package appeng.hooks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ICustomBlockDestroyEffect {
    @Environment(EnvType.CLIENT)
    boolean addDestroyEffects(
            BlockState state,
            Level level,
            BlockPos pos,
            ParticleEngine effectRenderer);
}
