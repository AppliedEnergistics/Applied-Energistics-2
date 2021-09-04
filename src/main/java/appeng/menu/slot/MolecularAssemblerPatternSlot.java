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

import appeng.api.implementations.blockentities.InternalInventory;
import appeng.menu.implementations.MolecularAssemblerMenu;

public class MolecularAssemblerPatternSlot extends AppEngSlot {

    private final MolecularAssemblerMenu mac;

    public MolecularAssemblerPatternSlot(final MolecularAssemblerMenu mac, final InternalInventory inv,
            final int invSlot) {
        super(inv, invSlot);
        this.mac = mac;
    }

    @Override
    public boolean mayPlace(final ItemStack stack) {
        return this.mac.isValidItemForSlot(this.getSlotIndex(), stack);
    }

    @Override
    protected boolean getCurrentValidationState() {
        ItemStack stack = getItem();
        return stack.isEmpty() || mayPlace(stack);
    }

}
