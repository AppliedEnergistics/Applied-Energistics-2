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

    private final MolecularAssemblerMenu menu;

    public MolecularAssemblerPatternSlot(MolecularAssemblerMenu menu, InternalInventory inv,
            int invSlot) {
        super(inv, invSlot);
        this.menu = menu;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return super.mayPlace(stack) && this.menu.isValidItemForSlot(this.getSlotIndex(), stack);
    }

    @Override
    protected boolean getCurrentValidationState() {
        var stack = getItem();
        return stack.isEmpty() || this.menu.isSlotValid(getSlotIndex());
    }

    @Override
    public boolean isRenderDisabled() {
        return true; // The background image does not include a slot background
    }

    @Override
    public boolean isSlotEnabled() {
        // Always enabled when there's an item in the inventory (otherwise you can't take it out...)
        if (!getInventory().getStackInSlot(getSlotIndex()).isEmpty()) {
            return true;
        }

        return getSlotIndex() >= 0 && getSlotIndex() < 9 && menu.isInputSlotEnabled(getSlotIndex());
    }

    @Override
    public Point getBackgroundPos() {
        return new Point(x - 1, y - 1);
    }
}
