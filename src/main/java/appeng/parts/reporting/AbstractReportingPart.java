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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.IModelData;

import appeng.api.implementations.parts.IMonitorPart;
import appeng.api.networking.GridFlags;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
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
public abstract class AbstractReportingPart extends AEBasePart implements IMonitorPart {

    private byte spin = 0; // 0-3
    private int opacity = -1;

    protected AbstractReportingPart(IPartItem<?> partItem, boolean requireChannel) {
        super(partItem);

        if (requireChannel) {
            this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
            this.getMainNode().setIdlePowerUsage(1.0 / 2.0);
        } else {
            this.getMainNode().setIdlePowerUsage(1.0 / 16.0); // lights drain less
        }
    }

    @Override
    public final void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(4, 4, 13, 12, 12, 14);
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (pos.relative(this.getSide()).equals(neighbor)) {
            this.opacity = -1;
            this.getHost().markForUpdate();
        }
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.spin = data.getByte("spin");
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putByte("spin", this.getSpin());
    }

    @Override
    public void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);

        data.writeByte(this.getSpin());
    }

    @Override
    public boolean readFromStream(FriendlyByteBuf data) {
        var changed = super.readFromStream(data);
        var oldSpin = this.spin;

        this.spin = data.readByte();

        return changed || oldSpin != spin;
    }

    @Override
    public final int getLightLevel() {
        return this.blockLight(this.isPowered() ? this.isLightSource() ? 15 : 9 : 0);
    }

    @Override
    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
        if (InteractionUtil.canWrenchRotate(player.getInventory().getSelected())) {
            if (!isClientSide()) {
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
    public final void onPlacement(Player player) {
        super.onPlacement(player);

        final byte rotation = (byte) (Mth.floor(player.getYRot() * 4F / 360F + 2.5D) & 3);
        if (getSide() == Direction.UP || getSide() == Direction.DOWN) {
            this.spin = rotation;
        }
    }

    private int blockLight(int emit) {
        if (this.opacity == -1) {
            var te = getHost().getBlockEntity();
            Level level = te.getLevel();
            var pos = te.getBlockPos().relative(this.getSide());
            this.opacity = level.getBlockState(pos).getLightBlock(level, pos);
        }

        return Math.max(0, emit - opacity);
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
    public IModelData getModelData() {
        return new ReportingModelData(getSpin());
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
