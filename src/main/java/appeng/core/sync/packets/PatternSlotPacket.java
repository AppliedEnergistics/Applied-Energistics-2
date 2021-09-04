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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import appeng.api.implementations.blockentities.InternalInventory;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.menu.me.items.PatternTermMenu;
import appeng.util.item.AEItemStack;

public class PatternSlotPacket extends BasePacket {

    public final IAEItemStack slotItem;

    public final IAEItemStack[] pattern = new IAEItemStack[9];

    public final boolean shift;

    public PatternSlotPacket(final FriendlyByteBuf stream) {

        this.shift = stream.readBoolean();

        this.slotItem = this.readItem(stream);

        for (int x = 0; x < 9; x++) {
            this.pattern[x] = this.readItem(stream);
        }
    }

    private IAEItemStack readItem(final FriendlyByteBuf stream) {
        final boolean hasItem = stream.readBoolean();

        if (hasItem) {
            return AEItemStack.fromPacket(stream);
        }

        return null;
    }

    // api
    public PatternSlotPacket(final InternalInventory pat, final IAEItemStack slotItem, final boolean shift) {

        this.slotItem = slotItem;
        this.shift = shift;

        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());

        data.writeBoolean(shift);

        this.writeItem(slotItem, data);
        for (int x = 0; x < 9; x++) {
            this.pattern[x] = StorageChannels.items()
                    .createStack(pat.getStackInSlot(x));
            this.writeItem(this.pattern[x], data);
        }

        this.configureWrite(data);
    }

    private void writeItem(final IAEItemStack slotItem, final FriendlyByteBuf data) {
        if (slotItem == null) {
            data.writeBoolean(false);
        } else {
            data.writeBoolean(true);
            slotItem.writeToPacket(data);
        }
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final ServerPlayer player) {
        final ServerPlayer sender = (ServerPlayer) player;
        if (sender.containerMenu instanceof PatternTermMenu patternTerminal) {
            patternTerminal.craftOrGetItem(this);
        }
    }
}
