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
import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.menu.me.items.PatternTermMenu;

/**
 * Grabs or crafts an item from network content and puts it into the players hand. Used for crafting in the pattern
 * terminal, since the ingredients are not _really_ stored locally in the terminal due to the crafting matrix being
 * composed of fake slots.
 */
public class PatternSlotPacket extends BasePacket {
    public final ItemStack what;
    public final ItemStack[] pattern = new ItemStack[9];

    /**
     * Move/Craft into player inventory rather than cursor.
     */
    public final boolean intoPlayerInv;

    public PatternSlotPacket(FriendlyByteBuf stream) {

        this.intoPlayerInv = stream.readBoolean();
        this.what = stream.readItem();

        for (int x = 0; x < 9; x++) {
            this.pattern[x] = stream.readItem();
        }
    }

    // api
    public PatternSlotPacket(InternalInventory pat, ItemStack what, boolean intoPlayerInv) {

        this.what = what;
        this.intoPlayerInv = intoPlayerInv;

        var data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());

        data.writeBoolean(intoPlayerInv);

        data.writeItem(this.what);

        for (int x = 0; x < 9; x++) {
            data.writeItem(pat.getStackInSlot(x));
        }

        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(INetworkInfo manager, ServerPlayer player) {
        if (player.containerMenu instanceof PatternTermMenu patternTerminal) {
            patternTerminal.craftOrGetItem(this);
        }
    }
}
