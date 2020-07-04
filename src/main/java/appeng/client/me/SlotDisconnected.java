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

package appeng.client.me;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import appeng.container.slot.AppEngSlot;
import appeng.items.misc.EncodedPatternItem;
import appeng.util.Platform;

public class SlotDisconnected extends AppEngSlot {

    private final ClientDCInternalInv mySlot;

    public SlotDisconnected(final ClientDCInternalInv me, final int which, final int x, final int y) {
        super(me.getInventory(), which, x, y);
        this.mySlot = me;
    }

    @Override
    public boolean canInsert(final ItemStack par1ItemStack) {
        return false;
    }

    @Override
    public void setStack(final ItemStack par1ItemStack) {

    }

    @Override
    public boolean canTakeItems(final PlayerEntity par1PlayerEntity) {
        return false;
    }

    @Override
    public ItemStack getDisplayStack() {
        if (Platform.isClient()) {
            final ItemStack is = super.getStack();
            if (!is.isEmpty() && is.getItem() instanceof EncodedPatternItem) {
                final EncodedPatternItem iep = (EncodedPatternItem) is.getItem();
                final ItemStack out = iep.getOutput(MinecraftClient.getInstance().world, is);
                if (!out.isEmpty()) {
                    return out;
                }
            }
        }
        return super.getStack();
    }

    @Override
    public boolean hasStack() {
        return !this.getStack().isEmpty();
    }

    @Override
    public int getMaxStackAmount() {
        return 0;
    }

    @Override
    public ItemStack takeStack(final int par1) {
        return ItemStack.EMPTY;
    }

    public ClientDCInternalInv getSlot() {
        return this.mySlot;
    }
}
