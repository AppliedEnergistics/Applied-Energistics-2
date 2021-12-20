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

import javax.annotation.OverridingMethodsMustInvokeSuper;

import net.minecraft.network.FriendlyByteBuf;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.parts.IPartItem;

/**
 * Provides a simple way of synchronizing up to 8 flags of state to the client. By default, it includes the power and
 * channel state of the connected grid node. See {@link IGridNode#isPowered()} and
 * {@link IGridNode#meetsChannelRequirements()}.
 */
public abstract class BasicStatePart extends AEBasePart implements IPowerChannelState {

    protected static final int POWERED_FLAG = 1;
    protected static final int CHANNEL_FLAG = 2;

    private int clientFlags = 0; // sent as byte.

    public BasicStatePart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    protected IManagedGridNode createMainNode() {
        return GridHelper.createManagedNode(this, NodeListener.INSTANCE);
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (calculateClientFlags() != getClientFlags()) {
            getHost().markForUpdate();
        }
    }

    @Override
    public void writeToStream(final FriendlyByteBuf data) {
        super.writeToStream(data);

        var flags = this.calculateClientFlags();
        this.setClientFlags(flags);
        data.writeByte((byte) flags);
    }

    @OverridingMethodsMustInvokeSuper
    protected int calculateClientFlags() {
        var flags = 0;

        var node = getMainNode().getNode();
        if (node != null) {
            if (node.isPowered()) {
                flags |= POWERED_FLAG;
            }
            if (node.meetsChannelRequirements()) {
                flags |= CHANNEL_FLAG;
            }
        }

        return flags;
    }

    @Override
    public boolean readFromStream(final FriendlyByteBuf data) {
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
