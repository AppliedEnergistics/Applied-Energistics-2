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

package appeng.parts.reporting;


import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.parts.IPartMonitor;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.util.AEPartLocation;
import appeng.me.GridAccessException;
import appeng.parts.AEBasePart;
import appeng.util.Platform;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;

import java.io.IOException;


/**
 * The most basic class for any part reporting information, like terminals or monitors. This can also include basic
 * panels which just provide light.
 * <p>
 * It deals with the most basic functionalities like network data, grid registration or the rotation of the actual part.
 * <p>
 * The direct abstract subclasses are usually a better entry point for adding new concrete ones.
 * But this might be an ideal starting point to completely new type, which does not resemble any existing one.
 *
 * @author AlgorithmX2
 * @author yueh
 * @version rv3
 * @since rv3
 */
public abstract class AbstractPartReporting extends AEBasePart implements IPartMonitor, IPowerChannelState {

    protected static final int POWERED_FLAG = 4;
    protected static final int CHANNEL_FLAG = 16;
    private static final int BOOTING_FLAG = 8;

    private byte spin = 0; // 0-3
    private int clientFlags = 0; // sent as byte.
    private int opacity = -1;

    public AbstractPartReporting(final ItemStack is) {
        this(is, false);
    }

    protected AbstractPartReporting(final ItemStack is, final boolean requireChannel) {
        super(is);

        if (requireChannel) {
            this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
            this.getProxy().setIdlePowerUsage(1.0 / 2.0);
        } else {
            this.getProxy().setIdlePowerUsage(1.0 / 16.0); // lights drain a little bit.
        }
    }

    @MENetworkEventSubscribe
    public final void bootingRender(final MENetworkBootingStatusChange c) {
        if (!this.isLightSource()) {
            this.getHost().markForUpdate();
        }
    }

    @MENetworkEventSubscribe
    public final void powerRender(final MENetworkPowerStatusChange c) {
        this.getHost().markForUpdate();
    }

    @Override
    public final void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(4, 4, 13, 12, 12, 14);
    }

    @Override
    public void onNeighborChanged(IBlockAccess w, BlockPos pos, BlockPos neighbor) {
        if (pos.offset(this.getSide().getFacing()).equals(neighbor)) {
            this.opacity = -1;
            this.getHost().markForUpdate();
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.spin = data.getByte("spin");
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setByte("spin", this.getSpin());
    }

    @Override
    public void writeToStream(final ByteBuf data) throws IOException {
        super.writeToStream(data);
        this.clientFlags = this.getSpin() & 3;

        try {
            if (this.getProxy().getEnergy().isNetworkPowered()) {
                this.clientFlags = this.getClientFlags() | AbstractPartReporting.POWERED_FLAG;
            }

            if (this.getProxy().getPath().isNetworkBooting()) {
                this.clientFlags = this.getClientFlags() | AbstractPartReporting.BOOTING_FLAG;
            }

            if (this.getProxy().getNode().meetsChannelRequirements()) {
                this.clientFlags = this.getClientFlags() | AbstractPartReporting.CHANNEL_FLAG;
            }
        } catch (final GridAccessException e) {
            // um.. nothing.
        }

        data.writeByte((byte) this.getClientFlags());
        data.writeInt(this.opacity);
    }

    @Override
    public boolean readFromStream(final ByteBuf data) throws IOException {
        super.readFromStream(data);
        final int oldFlags = this.getClientFlags();
        final int oldOpacity = this.opacity;

        this.clientFlags = data.readByte();
        this.opacity = data.readInt();

        this.spin = (byte) (this.getClientFlags() & 3);
        return this.getClientFlags() != oldFlags || this.opacity != oldOpacity;
    }

    @Override
    public final int getLightLevel() {
        return this.blockLight(this.isPowered() ? (this.isLightSource() ? 15 : 9) : 0);
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        final TileEntity te = this.getTile();

        if (Platform.isWrench(player, player.inventory.getCurrentItem(), te.getPos())) {
            if (Platform.isServer()) {
                if (this.getSpin() > 3) {
                    this.spin = 0;
                }

                switch (this.getSpin()) {
                    case 0:
                        this.spin = 1;
                        break;
                    case 1:
                        this.spin = 3;
                        break;
                    case 2:
                        this.spin = 0;
                        break;
                    case 3:
                        this.spin = 2;
                        break;
                }

                this.getHost().markForUpdate();
                this.saveChanges();
            }
            return true;
        } else {
            return super.onPartActivate(player, hand, pos);
        }
    }

    @Override
    public final void onPlacement(final EntityPlayer player, final EnumHand hand, final ItemStack held, final AEPartLocation side) {
        super.onPlacement(player, hand, held, side);

        final byte rotation = (byte) (MathHelper.floor((player.rotationYaw * 4F) / 360F + 2.5D) & 3);
        if (side == AEPartLocation.UP) {
            this.spin = rotation;
        } else if (side == AEPartLocation.DOWN) {
            this.spin = rotation;
        }
    }

    private final int blockLight(final int emit) {
        if (this.opacity < 0) {
            final TileEntity te = this.getTile();
            this.opacity = 255 - te.getWorld().getBlockLightOpacity(te.getPos().offset(this.getSide().getFacing()));
        }

        return (int) (emit * (this.opacity / 255.0f));
    }

    @Override
    public final boolean isPowered() {
        try {
            if (Platform.isServer()) {
                return this.getProxy().getEnergy().isNetworkPowered();
            } else {
                return ((this.getClientFlags() & PartPanel.POWERED_FLAG) == PartPanel.POWERED_FLAG);
            }
        } catch (final GridAccessException e) {
            return false;
        }
    }

    @Override
    public final boolean isActive() {
        if (!this.isLightSource()) {
            return ((this.getClientFlags() & (PartPanel.CHANNEL_FLAG | PartPanel.POWERED_FLAG)) == (PartPanel.CHANNEL_FLAG | PartPanel.POWERED_FLAG));
        } else {
            return this.isPowered();
        }
    }

    protected IPartModel selectModel(IPartModel offModels, IPartModel onModels, IPartModel hasChannelModels) {
        if (this.isActive()) {
            return hasChannelModels;
        } else if (this.isPowered()) {
            return onModels;
        } else {
            return offModels;
        }
    }

    public final int getClientFlags() {
        return this.clientFlags;
    }

    public final byte getSpin() {
        return this.spin;
    }

    /**
     * Should the part emit light. This actually only affects the light level, light source use a level of 15 and non
     * light source 9.
     */
    public abstract boolean isLightSource();

}
