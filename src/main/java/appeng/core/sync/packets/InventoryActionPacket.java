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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.menu.AEBaseMenu;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;

public class InventoryActionPacket extends BasePacket {

    private final InventoryAction action;
    private final int slot;
    private final long id;
    private final ItemStack slotItem;

    public InventoryActionPacket(final FriendlyByteBuf stream) {
        this.action = InventoryAction.values()[stream.readInt()];
        this.slot = stream.readInt();
        this.id = stream.readLong();
        this.slotItem = stream.readItem();
    }

    // api
    public InventoryActionPacket(final InventoryAction action, final int slot, final ItemStack slotItem) {

        if (Platform.isClient() && action != InventoryAction.SET_FILTER) {
            throw new IllegalStateException("invalid packet, client cannot post inv actions with stacks.");
        }

        this.action = action;
        this.slot = slot;
        this.id = 0;
        this.slotItem = slotItem.copy();

        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeInt(action.ordinal());
        data.writeInt(slot);
        data.writeLong(this.id);
        data.writeItem(this.slotItem);

        this.configureWrite(data);
    }

    // api
    public InventoryActionPacket(final InventoryAction action, final int slot, final long id) {
        this.action = action;
        this.slot = slot;
        this.id = id;
        this.slotItem = null;

        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeInt(action.ordinal());
        data.writeInt(slot);
        data.writeLong(id);
        data.writeItem(ItemStack.EMPTY);

        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final Player player) {
        final ServerPlayer sender = (ServerPlayer) player;
        if (sender.containerMenu instanceof AEBaseMenu baseMenu) {
            if (action == InventoryAction.SET_FILTER) {
                baseMenu.setFilter(this.slot, this.slotItem);
            } else {
                baseMenu.doAction(sender, this.action, this.slot, this.id);
            }
        }
    }

}
