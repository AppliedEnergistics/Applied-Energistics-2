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

package appeng.menu.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class InaccessibleSlot extends AppEngSlot {

    private ItemStack dspStack = ItemStack.EMPTY;

    public InaccessibleSlot(final IItemHandler i, final int invSlot) {
        super(i, invSlot);
    }

    @Override
    public boolean mayPlace(final ItemStack stack) {
        return false;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.dspStack = ItemStack.EMPTY;
    }

    @Override
    public boolean mayPickup(final Player player) {
        return false;
    }

    @Override
    public ItemStack getDisplayStack() {
        if (this.dspStack.isEmpty()) {
            final ItemStack dsp = super.getDisplayStack();
            if (!dsp.isEmpty()) {
                this.dspStack = dsp.copy();
            }
        }
        return this.dspStack;
    }
}
