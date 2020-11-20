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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;

import appeng.api.util.AEPartLocation;
import appeng.client.render.effects.EnergyParticleData;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.Platform;

/**
 * Plays the block breaking or fluid pickup sound and a transition particle effect into the supplied direction. Used
 * primarily by annihilation planes.
 */
public class BlockTransitionEffectPacket extends BasePacket {

    private final BlockPos pos;
    private final BlockState blockState;
    private final AEPartLocation direction;
    private final SoundMode soundMode;

    public enum SoundMode {
        BLOCK, FLUID, NONE
    }

    public BlockTransitionEffectPacket(BlockPos pos, BlockState blockState, AEPartLocation direction,
            SoundMode soundMode) {
        this.pos = pos;
        this.blockState = blockState;
        this.direction = direction;
        this.soundMode = soundMode;

        final PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeBlockPos(pos);
        int blockStateId = Block.getRawIdFromState(blockState);
        if (blockStateId == -1) {
            AELog.warn("Failed to find numeric id for block state %s", blockState);
        }
        data.writeInt(blockStateId);
        data.writeByte(this.direction.ordinal());
        data.writeByte((byte) soundMode.ordinal());
        this.configureWrite(data);
    }

    public BlockTransitionEffectPacket(final PacketByteBuf stream) {

        this.pos = stream.readBlockPos();
        int blockStateId = stream.readInt();
        BlockState blockState = Block.getStateFromRawId(blockStateId);
        if (blockState == null) {
            AELog.warn("Received invalid blockstate id %d from server", blockStateId);
            blockState = Blocks.AIR.getDefaultState();
        }
        this.blockState = blockState;
        this.direction = AEPartLocation.fromOrdinal(stream.readByte());
        this.soundMode = SoundMode.values()[stream.readByte()];
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void clientPacketData(final INetworkInfo network, final PlayerEntity player) {
        spawnParticles();

        playBreakOrPickupSound();
    }

    @Environment(EnvType.CLIENT)
    private void spawnParticles() {

        EnergyParticleData data = new EnergyParticleData(false, direction);
        for (int zz = 0; zz < 32; zz++) {
            if (AppEng.instance().shouldAddParticles(Platform.getRandom())) {
                // Distribute the spawn point across the entire block's area
                double x = pos.getX() + Platform.getRandomFloat();
                double y = pos.getY() + Platform.getRandomFloat();
                double z = pos.getZ() + Platform.getRandomFloat();
                double speedX = 0.1f * this.direction.xOffset;
                double speedY = 0.1f * this.direction.yOffset;
                double speedZ = 0.1f * this.direction.zOffset;

                MinecraftClient.getInstance().particleManager.addParticle(data, x, y, z, speedX, speedY, speedZ);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private void playBreakOrPickupSound() {

        SoundEvent soundEvent;
        float volume;
        float pitch;
        if (soundMode == SoundMode.FLUID) {
            // This code is based on what BucketItem does
            Fluid rawFluid = blockState.getFluidState().getFluid();
            if (rawFluid.isIn(FluidTags.LAVA)) {
                soundEvent = SoundEvents.ITEM_BUCKET_FILL_LAVA;
            } else {
                soundEvent = SoundEvents.ITEM_BUCKET_FILL;
            }
            volume = 1;
            pitch = 1;
        } else if (soundMode == SoundMode.BLOCK) {
            BlockSoundGroup soundType = blockState.getSoundGroup();
            soundEvent = soundType.getBreakSound();
            volume = soundType.volume;
            pitch = soundType.pitch;
        } else {
            return;
        }

        PositionedSoundInstance sound = new PositionedSoundInstance(soundEvent, SoundCategory.BLOCKS,
                (volume + 1.0F) / 2.0F, pitch * 0.8F, pos);
        MinecraftClient.getInstance().getSoundManager().play(sound);
    }

}
