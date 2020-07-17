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

package appeng.container.slot;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.core.Api;
import appeng.core.sync.BasePacket;
import appeng.core.sync.packets.PatternSlotPacket;
import appeng.helpers.IContainerCraftingPacket;

public class PatternTermSlot extends CraftingTermSlot {

    private final int groupNum;
    private final IOptionalSlotHost host;

    public PatternTermSlot(final PlayerEntity player, final IActionSource mySrc, final IEnergySource energySrc,
                           final IStorageMonitorable storage, final FixedItemInv cMatrix, final FixedItemInv secondMatrix,
                           final FixedItemInv output, final int x, final int y, final IOptionalSlotHost h, final int groupNumber,
                           final IContainerCraftingPacket c) {
        super(player, mySrc, energySrc, storage, cMatrix, secondMatrix, output, x, y, c);

        this.host = h;
        this.groupNum = groupNumber;
    }

    public BasePacket getRequest(final boolean shift) {
        return new PatternSlotPacket(this.getPattern(),
                Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(this.getStack()),
                shift);
    }

    @Override
    public ItemStack getStack() {
        if (!this.isSlotEnabled()) {
            if (!this.getDisplayStack().isEmpty()) {
                this.clearStack();
            }
        }

        return super.getStack();
    }

    @Override
    public boolean isSlotEnabled() {
        if (this.host == null) {
            return false;
        }

        return this.host.isSlotEnabled(this.groupNum);
    }
}
