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

import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;
import appeng.client.Point;
import appeng.menu.implementations.MolecularAssemblerMenu;

public class MolecularAssemblerPatternSlot extends AppEngSlot implements IOptionalSlot {

    private final MolecularAssemblerMenu mac;

    public MolecularAssemblerPatternSlot(MolecularAssemblerMenu mac, InternalInventory inv,
            int invSlot) {
        super(inv, invSlot);
        this.mac = mac;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return super.mayPlace(stack) && this.mac.isValidItemForSlot(this.getSlotIndex(), stack);
    }

    @Override
    protected boolean getCurrentValidationState() {
        ItemStack stack = getItem();
        return stack.isEmpty() || mayPlace(stack);
    }

    @Override
    public boolean isRenderDisabled() {
        return true; // The background image does not include a slot background
    }

    @Override
    public boolean isSlotEnabled() {
        // Always enabled when there's an item in the inventory (otherwise you can't take it out...)
        if (!getInventory().getStackInSlot(slot).isEmpty()) {
            return true;
        }

        var pattern = mac.getHost().getCurrentPattern();
        return slot >= 0 && slot < 9 && pattern != null && pattern.isSlotEnabled(slot);
    }

    @Override
    public Point getBackgroundPos() {
        return new Point(x - 1, y - 1);
    }
}
