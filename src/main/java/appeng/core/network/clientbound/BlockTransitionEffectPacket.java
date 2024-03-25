
package appeng.core.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.registries.GameData;

import appeng.client.render.effects.EnergyParticleData;
import appeng.core.AELog;
import appeng.core.AppEngClient;
import appeng.core.network.ClientboundPacket;

/**
 * Plays the block breaking or fluid pickup sound and a transition particle effect into the supplied direction. Used
 * primarily by annihilation planes.
 */
public record BlockTransitionEffectPacket(BlockPos pos,
        BlockState blockState,
        Direction direction,
        SoundMode soundMode) implements ClientboundPacket {

    public enum SoundMode {
        BLOCK, FLUID, NONE
    }

    @Override
    public void write(FriendlyByteBuf data) {
        data.writeBlockPos(pos);
        int blockStateId = GameData.getBlockStateIDMap().getId(blockState);
        if (blockStateId == -1) {
            AELog.warn("Failed to find numeric id for block state %s", blockState);
        }
        data.writeInt(blockStateId);
        data.writeEnum(direction);
        data.writeEnum(soundMode);
    }

    public static BlockTransitionEffectPacket decode(FriendlyByteBuf data) {

        var pos = data.readBlockPos();
        int blockStateId = data.readInt();
        BlockState blockState = GameData.getBlockStateIDMap().byId(blockStateId);
        if (blockState == null) {
            AELog.warn("Received invalid blockstate id %d from server", blockStateId);
            blockState = Blocks.AIR.defaultBlockState();
        }
        var direction = data.readEnum(Direction.class);
        var soundMode = data.readEnum(SoundMode.class);
        return new BlockTransitionEffectPacket(pos, blockState, direction, soundMode);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleOnClient(Player player) {
        spawnParticles(player.level());

        playBreakOrPickupSound();
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnParticles(Level level) {

        EnergyParticleData data = new EnergyParticleData(false, direction);
        for (int zz = 0; zz < 32; zz++) {
            if (AppEngClient.instance().shouldAddParticles(level.getRandom())) {
                // Distribute the spawn point across the entire block's area
                double x = pos.getX() + level.getRandom().nextFloat();
                double y = pos.getY() + level.getRandom().nextFloat();
                double z = pos.getZ() + level.getRandom().nextFloat();
                double speedX = 0.1f * this.direction.getStepX();
                double speedY = 0.1f * this.direction.getStepY();
                double speedZ = 0.1f * this.direction.getStepZ();

                Minecraft.getInstance().particleEngine.createParticle(data, x, y, z, speedX, speedY, speedZ);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playBreakOrPickupSound() {

        SoundEvent soundEvent;
        float volume;
        float pitch;
        if (soundMode == SoundMode.FLUID) {
            // This code is based on what BucketItem does
            Fluid fluid = blockState.getFluidState().getType();
            soundEvent = fluid.getFluidType().getSound(SoundActions.BUCKET_FILL);
            if (soundEvent == null) {
                if (fluid.is(FluidTags.LAVA)) {
                    soundEvent = SoundEvents.BUCKET_FILL_LAVA;
                } else {
                    soundEvent = SoundEvents.BUCKET_FILL;
                }
            }
            volume = 1;
            pitch = 1;
        } else if (soundMode == SoundMode.BLOCK) {
            SoundType soundType = blockState.getSoundType();
            soundEvent = soundType.getBreakSound();
            volume = soundType.volume;
            pitch = soundType.pitch;
        } else {
            return;
        }

        SimpleSoundInstance sound = new SimpleSoundInstance(soundEvent, SoundSource.BLOCKS, (volume + 1.0F) / 2.0F,
                pitch * 0.8F,
                SoundInstance.createUnseededRandom(),
                pos);
        Minecraft.getInstance().getSoundManager().play(sound);
    }

}
