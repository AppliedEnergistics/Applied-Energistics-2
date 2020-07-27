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

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;

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
    }

    @Override
    public void chanRender(final MENetworkChannelsChanged c) {
        this.onTunnelNetworkChange();
        super.chanRender(c);
    }

    @Override
    public void powerRender(final MENetworkPowerStatusChange c) {
        this.onTunnelNetworkChange();
        super.powerRender(c);
    }

    @Override
    public void writeToStream(final PacketByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeInt(this.isOutput() ? this.lastValue : 0);
        data.writeInt(this.opacity);
    }

    @Override
    public boolean readFromStream(final PacketByteBuf data) throws IOException {
        super.readFromStream(data);
        final int oldValue = this.lastValue;
        final int oldOpacity = this.opacity;

        this.lastValue = data.readInt();
        this.opacity = data.readInt();

        this.setOutput(this.lastValue > 0);
        return this.lastValue != oldValue || oldOpacity != this.opacity;
    }

    private boolean doWork() {
        if (this.isOutput()) {
            return false;
        }

        final BlockEntity te = this.getTile();
        final World w = te.getWorld();

        final int newLevel = w.getLightLevel(te.getPos().offset(this.getSide().getFacing()));

        if (this.lastValue != newLevel && this.getProxy().isActive()) {
            this.lastValue = newLevel;
            try {
                for (final LightP2PTunnelPart out : this.getOutputs()) {
                    out.setLightLevel(this.lastValue);
                }
            } catch (final GridAccessException e) {
                // :P
            }
            return true;
        }
        return false;
    }

    @Override
    public void onneighborUpdate(BlockView w, BlockPos pos, BlockPos neighbor) {
        if (this.isOutput() && pos.offset(this.getSide().getFacing()).equals(neighbor)) {
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
            final BlockEntity te = this.getTile();
            this.opacity = 255 - te.getWorld().getLightLevel(te.getPos().offset(this.getSide().getFacing()));
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
            if (src != null && src.getProxy().isActive()) {
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

    public float getPowerDrainPerTick() {
        return 0.5f;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

}
