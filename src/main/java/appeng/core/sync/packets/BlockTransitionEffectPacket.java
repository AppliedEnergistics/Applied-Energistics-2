/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.core.sync.packets;

import io.netty.buffer.Unpooled;

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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.registries.GameData;

import appeng.client.render.effects.EnergyParticleData;
import appeng.core.AELog;
import appeng.core.AppEngClient;
import appeng.core.sync.BasePacket;
import appeng.util.Platform;

/**
 * Plays the block breaking or fluid pickup sound and a transition particle effect into the supplied direction. Used
 * primarily by annihilation planes.
 */
public class BlockTransitionEffectPacket extends BasePacket {

    private final BlockPos pos;
    private final BlockState blockState;
    private final Direction direction;
    private final SoundMode soundMode;

    public enum SoundMode {
        BLOCK, FLUID, NONE
    }

    public BlockTransitionEffectPacket(BlockPos pos, BlockState blockState, Direction direction,
            SoundMode soundMode) {
        this.pos = pos;
        this.blockState = blockState;
        this.direction = direction;
        this.soundMode = soundMode;

        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeBlockPos(pos);
        int blockStateId = GameData.getBlockStateIDMap().getId(blockState);
        if (blockStateId == -1) {
            AELog.warn("Failed to find numeric id for block state %s", blockState);
        }
        data.writeInt(blockStateId);
        data.writeByte(this.direction.ordinal());
        data.writeByte((byte) soundMode.ordinal());
        this.configureWrite(data);
    }

    public BlockTransitionEffectPacket(FriendlyByteBuf stream) {

        this.pos = stream.readBlockPos();
        int blockStateId = stream.readInt();
        BlockState blockState = GameData.getBlockStateIDMap().byId(blockStateId);
        if (blockState == null) {
            AELog.warn("Received invalid blockstate id %d from server", blockStateId);
            blockState = Blocks.AIR.defaultBlockState();
        }
        this.blockState = blockState;
        this.direction = Direction.values()[stream.readByte()];
        this.soundMode = SoundMode.values()[stream.readByte()];
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientPacketData(Player player) {
        spawnParticles();

        playBreakOrPickupSound();
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnParticles() {

        EnergyParticleData data = new EnergyParticleData(false, direction);
        for (int zz = 0; zz < 32; zz++) {
            if (AppEngClient.instance().shouldAddParticles(Platform.getRandom())) {
                // Distribute the spawn point across the entire block's area
                double x = pos.getX() + Platform.getRandomFloat();
                double y = pos.getY() + Platform.getRandomFloat();
                double z = pos.getZ() + Platform.getRandomFloat();
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
