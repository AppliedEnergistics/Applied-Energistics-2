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

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.GameData;

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

        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());

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

    public BlockTransitionEffectPacket(final PacketBuffer stream) {

        this.pos = stream.readBlockPos();
        int blockStateId = stream.readInt();
        BlockState blockState = GameData.getBlockStateIDMap().getByValue(blockStateId);
        if (blockState == null) {
            AELog.warn("Received invalid blockstate id %d from server", blockStateId);
            blockState = Blocks.AIR.getDefaultState();
        }
        this.blockState = blockState;
        this.direction = AEPartLocation.fromOrdinal(stream.readByte());
        this.soundMode = SoundMode.values()[stream.readByte()];
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientPacketData(final INetworkInfo network, final PlayerEntity player) {
        spawnParticles();

        playBreakOrPickupSound();
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnParticles() {

        EnergyParticleData data = new EnergyParticleData(false, direction);
        for (int zz = 0; zz < 32; zz++) {
            if (AppEng.proxy.shouldAddParticles(Platform.getRandom())) {
                // Distribute the spawn point across the entire block's area
                double x = pos.getX() + Platform.getRandomFloat();
                double y = pos.getY() + Platform.getRandomFloat();
                double z = pos.getZ() + Platform.getRandomFloat();
                double speedX = 0.1f * this.direction.xOffset;
                double speedY = 0.1f * this.direction.yOffset;
                double speedZ = 0.1f * this.direction.zOffset;

                Minecraft.getInstance().particles.addParticle(data, x, y, z, speedX, speedY, speedZ);
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
            Fluid fluid = blockState.getFluidState().getFluid();
            soundEvent = fluid.getAttributes().getFillSound();
            if (soundEvent == null) {
                if (fluid.isIn(FluidTags.LAVA)) {
                    soundEvent = SoundEvents.ITEM_BUCKET_FILL_LAVA;
                } else {
                    soundEvent = SoundEvents.ITEM_BUCKET_FILL;
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

        SimpleSound sound = new SimpleSound(soundEvent, SoundCategory.BLOCKS, (volume + 1.0F) / 2.0F, pitch * 0.8F,
                pos);
        Minecraft.getInstance().getSoundHandler().play(sound);
    }

}
