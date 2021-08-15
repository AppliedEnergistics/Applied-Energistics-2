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

package appeng.parts.p2p;

import java.io.IOException;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;

public class LightP2PTunnelPart extends P2PTunnelPart<LightP2PTunnelPart> implements IGridTickable {

    private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_light");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private int lastValue = 0;
    private int opacity = -1;

    public LightP2PTunnelPart(final ItemStack is) {
        super(is);
        getMainNode().addService(IGridTickable.class, this);
    }

    @Override
    protected float getPowerDrainPerTick() {
        return 0.5f;
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        this.onTunnelNetworkChange();
    }

    @Override
    public void writeToStream(final FriendlyByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeInt(this.isOutput() ? this.lastValue : 0);
        data.writeInt(this.opacity);
    }

    @Override
    public boolean readFromStream(final FriendlyByteBuf data) throws IOException {
        boolean changed = super.readFromStream(data);
        final int oldValue = this.lastValue;
        final int oldOpacity = this.opacity;

        this.lastValue = data.readInt();
        this.opacity = data.readInt();

        this.setOutput(this.lastValue > 0);
        return changed || this.lastValue != oldValue || oldOpacity != this.opacity;
    }

    private boolean doWork() {
        if (this.isOutput()) {
            return false;
        }

        final BlockEntity te = this.getBlockEntity();
        final Level level = te.getLevel();

        final int newLevel = level.getMaxLocalRawBrightness(te.getBlockPos().relative(this.getSide().getDirection()));

        if (this.lastValue != newLevel && this.getMainNode().isActive()) {
            this.lastValue = newLevel;
            for (final LightP2PTunnelPart out : this.getOutputs()) {
                out.setLightLevel(this.lastValue);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (this.isOutput() && pos.relative(this.getSide().getDirection()).equals(neighbor)) {
            this.opacity = -1;
            this.getHost().markForUpdate();
        } else {
            this.doWork();
        }
    }

    @Override
    public int getLightLevel() {
        if (this.isOutput() && this.isPowered()) {
            return this.blockLight(this.lastValue);
        }

        return 0;
    }

    private void setLightLevel(final int out) {
        this.lastValue = out;
        this.getHost().markForUpdate();
    }

    private int blockLight(final int emit) {
        if (this.opacity < 0) {
            final BlockEntity te = this.getBlockEntity();
            this.opacity = 255
                    - te.getLevel().getMaxLocalRawBrightness(te.getBlockPos().relative(this.getSide().getDirection()));
        }

        return (int) (emit * (this.opacity / 255.0f));
    }

    @Override
    public void readFromNBT(final CompoundTag tag) {
        super.readFromNBT(tag);
        this.lastValue = tag.getInt("lastValue");
    }

    @Override
    public void writeToNBT(final CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putInt("lastValue", this.lastValue);
    }

    @Override
    public void onTunnelConfigChange() {
        this.onTunnelNetworkChange();
    }

    @Override
    public void onTunnelNetworkChange() {
        if (this.isOutput()) {
            final LightP2PTunnelPart src = this.getInput();
            if (src != null && src.getMainNode().isActive()) {
                this.setLightLevel(src.lastValue);
            } else {
                this.getHost().markForUpdate();
            }
        } else {
            this.doWork();
        }
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(TickRates.LightTunnel.getMin(), TickRates.LightTunnel.getMax(), false, false);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        return this.doWork() ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

}
