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
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.util.AEPartLocation;
import appeng.client.render.effects.EnergyFx;
import appeng.core.AppEng;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.Platform;

public class PacketTransitionEffect extends AppEngPacket {

    private final boolean mode;
    private final double x;
    private final double y;
    private final double z;
    private final AEPartLocation d;

    public PacketTransitionEffect(final PacketBuffer stream) {
        this.x = stream.readFloat();
        this.y = stream.readFloat();
        this.z = stream.readFloat();
        this.d = AEPartLocation.fromOrdinal(stream.readByte());
        this.mode = stream.readBoolean();
    }

    // api
    public PacketTransitionEffect(final double x, final double y, final double z, final AEPartLocation dir,
            final boolean wasBlock) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.d = dir;
        this.mode = wasBlock;

        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeFloat((float) x);
        data.writeFloat((float) y);
        data.writeFloat((float) z);
        data.writeByte(this.d.ordinal());
        data.writeBoolean(wasBlock);

        this.configureWrite(data);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientPacketData(final INetworkInfo network, final PlayerEntity player) {
        final World world = AppEng.proxy.getWorld();

        for (int zz = 0; zz < (this.mode ? 32 : 8); zz++) {
            if (AppEng.proxy.shouldAddParticles(Platform.getRandom())) {
                double x = this.x + (this.mode ? (Platform.getRandomInt() % 100) * 0.01
                        : (Platform.getRandomInt() % 100) * 0.005 - 0.25);
                double y = this.y + (this.mode ? (Platform.getRandomInt() % 100) * 0.01
                        : (Platform.getRandomInt() % 100) * 0.005 - 0.25);
                double z = this.z + (this.mode ? (Platform.getRandomInt() % 100) * 0.01
                        : (Platform.getRandomInt() % 100) * 0.005 - 0.25);
                double speedX = -0.1f * this.d.xOffset;
                double speedY = -0.1f * this.d.yOffset;
                double speedZ = -0.1f * this.d.zOffset;

                EnergyFx fx = (EnergyFx) Minecraft.getInstance().particles.addParticle(EnergyFx.TYPE, x, y, z, speedX,
                        speedY, speedZ);
                // FIXME: *sigh* custom particle data for this one thing :|
                if (!this.mode) {
                    fx.fromItem(this.d);
                }
            }
        }

        if (this.mode) {
            final BlockPos pos = new BlockPos((int) this.x, (int) this.y, (int) this.z);
            final BlockState state = world.getBlockState(pos);
            final SoundType sound = state.getSoundType(world, pos, null);

            Minecraft.getInstance().getSoundHandler()
                    .play(new SimpleSound(sound.getBreakSound(), SoundCategory.BLOCKS,
                            (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F, (float) this.x + 0.5F,
                            (float) this.y + 0.5F, (float) this.z + 0.5F));
        }
    }
}
