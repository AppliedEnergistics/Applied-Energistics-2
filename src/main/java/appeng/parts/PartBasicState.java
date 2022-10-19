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

package appeng.parts;


import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.me.GridAccessException;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;

import java.io.IOException;


public abstract class PartBasicState extends AEBasePart implements IPowerChannelState {

    protected static final int POWERED_FLAG = 1;
    protected static final int CHANNEL_FLAG = 2;

    private int clientFlags = 0; // sent as byte.

    public PartBasicState(final ItemStack is) {
        super(is);
        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @MENetworkEventSubscribe
    public void chanRender(final MENetworkChannelsChanged c) {
        this.getHost().markForUpdate();
    }

    @MENetworkEventSubscribe
    public void powerRender(final MENetworkPowerStatusChange c) {
        this.getHost().markForUpdate();
    }

    @MENetworkEventSubscribe
    public void bootingRender(final MENetworkBootingStatusChange bs) {
        this.getHost().markForUpdate();
    }

    @Override
    public void writeToStream(final ByteBuf data) throws IOException {
        super.writeToStream(data);

        this.setClientFlags(0);

        try {
            if (this.getProxy().getEnergy().isNetworkPowered()) {
                this.setClientFlags(this.getClientFlags() | POWERED_FLAG);
            }

            if (this.getProxy().getNode().meetsChannelRequirements()) {
                this.setClientFlags(this.getClientFlags() | CHANNEL_FLAG);
            }

            this.setClientFlags(this.populateFlags(this.getClientFlags()));
        } catch (final GridAccessException e) {
            // meh
        }

        data.writeByte((byte) this.getClientFlags());
    }

    protected int populateFlags(final int cf) {
        return cf;
    }

    @Override
    public boolean readFromStream(final ByteBuf data) throws IOException {
        final boolean eh = super.readFromStream(data);

        final int old = this.getClientFlags();
        this.setClientFlags(data.readByte());

        return eh || old != this.getClientFlags();
    }

    @Override
    public boolean isPowered() {
        return (this.getClientFlags() & POWERED_FLAG) == POWERED_FLAG;
    }

    @Override
    public boolean isActive() {
        return (this.getClientFlags() & CHANNEL_FLAG) == CHANNEL_FLAG;
    }

    public int getClientFlags() {
        return this.clientFlags;
    }

    private void setClientFlags(final int clientFlags) {
        this.clientFlags = clientFlags;
    }

}
