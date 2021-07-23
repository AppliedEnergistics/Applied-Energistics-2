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

import java.io.IOException;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.parts.IMonitorPart;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.util.AEPartLocation;
import appeng.parts.AEBasePart;
import appeng.util.InteractionUtil;

/**
 * The most basic class for any part reporting information, like terminals or monitors. This can also include basic
 * panels which just provide light.
 * <p>
 * It deals with the most basic functionalities like network data, grid registration or the rotation of the actual part.
 * <p>
 * The direct abstract subclasses are usually a better entry point for adding new concrete ones. But this might be an
 * ideal starting point to completely new type, which does not resemble any existing one.
 *
 * @author AlgorithmX2
 * @author yueh
 * @version rv3
 * @since rv3
 */
public abstract class AbstractReportingPart extends AEBasePart implements IMonitorPart, IPowerChannelState {

    protected static final int POWERED_FLAG = 4;
    protected static final int CHANNEL_FLAG = 16;
    private static final int BOOTING_FLAG = 8;

    private byte spin = 0; // 0-3
    private int clientFlags = 0; // sent as byte.
    private int opacity = -1;

    public AbstractReportingPart(final ItemStack is) {
        this(is, false);
    }

    protected AbstractReportingPart(final ItemStack is, final boolean requireChannel) {
        super(is);

        if (requireChannel) {
            this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
            this.getMainNode().setIdlePowerUsage(1.0 / 2.0);
        } else {
            this.getMainNode().setIdlePowerUsage(1.0 / 16.0); // lights drain a little bit.
        }
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT || !this.isLightSource()) {
            this.getHost().markForUpdate();
        }
    }

    @Override
    public final void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(4, 4, 13, 12, 12, 14);
    }

    @Override
    public void onNeighborChanged(IBlockReader w, BlockPos pos, BlockPos neighbor) {
        if (pos.relative(this.getSide().getDirection()).equals(neighbor)) {
            this.opacity = -1;
            this.getHost().markForUpdate();
        }
    }

    @Override
    public void readFromNBT(final CompoundNBT data) {
        super.readFromNBT(data);
        this.spin = data.getByte("spin");
    }

    @Override
    public void writeToNBT(final CompoundNBT data) {
        super.writeToNBT(data);
        data.putByte("spin", this.getSpin());
    }

    @Override
    public void writeToStream(final PacketBuffer data) throws IOException {
        super.writeToStream(data);
        this.clientFlags = this.getSpin() & 3;

        var node = getMainNode().getNode();
        if (node != null) {
            if (node.isPowered()) {
                this.clientFlags = this.getClientFlags() | AbstractReportingPart.POWERED_FLAG;
            }

            if (!node.hasGridBooted()) {
                this.clientFlags = this.getClientFlags() | AbstractReportingPart.BOOTING_FLAG;
            }

            if (node.meetsChannelRequirements()) {
                this.clientFlags = this.getClientFlags() | AbstractReportingPart.CHANNEL_FLAG;
            }
        }

        data.writeByte((byte) this.getClientFlags());
        data.writeInt(this.opacity);
    }

    @Override
    public boolean readFromStream(final PacketBuffer data) throws IOException {
        super.readFromStream(data);
        final int oldFlags = this.getClientFlags();
        final int oldOpacity = this.opacity;

        this.clientFlags = data.readByte();
        this.opacity = data.readInt();

        this.spin = (byte) (this.getClientFlags() & 3);
        if (this.getClientFlags() == oldFlags && this.opacity == oldOpacity) {
            return false;
        }
        return true;
    }

    @Override
    public final int getLightLevel() {
        return this.blockLight(this.isPowered() ? this.isLightSource() ? 15 : 9 : 0);
    }

    @Override
    public boolean onPartActivate(final PlayerEntity player, final Hand hand, final Vector3d pos) {
        final TileEntity te = this.getTile();

        if (InteractionUtil.isWrench(player, player.inventory.getSelected(), te.getBlockPos())) {
            if (!isRemote()) {
                this.spin = (byte) ((this.spin + 1) % 4);
                this.getHost().markForUpdate();
                this.getHost().markForSave();
            }
            return true;
        } else {
            return super.onPartActivate(player, hand, pos);
        }
    }

    @Override
    public final void onPlacement(final PlayerEntity player, final Hand hand, final ItemStack held,
            final AEPartLocation side) {
        super.onPlacement(player, hand, held, side);

        final byte rotation = (byte) (MathHelper.floor(player.yRot * 4F / 360F + 2.5D) & 3);
        if (side == AEPartLocation.UP || side == AEPartLocation.DOWN) {
            this.spin = rotation;
        }
    }

    private int blockLight(final int emit) {
        if (this.opacity < 0) {
            final TileEntity te = this.getTile();
            World world = te.getLevel();
            BlockPos pos = te.getBlockPos().relative(this.getSide().getDirection());
            this.opacity = 255 - world.getBlockState(pos).getLightBlock(world, pos);
        }

        return (int) (emit * (this.opacity / 255.0f));
    }

    @Override
    public final boolean isPowered() {
        if (!isRemote()) {
            var node = getMainNode().getNode();
            return node != null && node.isPowered();
        } else {
            return (this.getClientFlags() & PanelPart.POWERED_FLAG) == PanelPart.POWERED_FLAG;
        }
    }

    @Override
    public final boolean isActive() {
        if (!this.isLightSource()) {
            return (this.getClientFlags()
                    & (PanelPart.CHANNEL_FLAG | PanelPart.POWERED_FLAG)) == (PanelPart.CHANNEL_FLAG
                            | PanelPart.POWERED_FLAG);
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

    @Override
    @Nonnull
    public IModelData getModelData() {
        return new ReportingModelData(getSpin());
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
