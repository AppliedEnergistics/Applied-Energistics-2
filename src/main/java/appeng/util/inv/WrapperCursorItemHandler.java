/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.util.inv;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Wraps the ItemStack that can be held by the player under their mouse in the current menu.
 */
public class WrapperCursorItemHandler extends ItemStackHandler {
    private final AbstractContainerMenu menu;

    public WrapperCursorItemHandler(AbstractContainerMenu menu) {
        super(1);

        this.menu = menu;
        this.setStackInSlot(0, menu.getCarried());
    }

    @Override
    protected void onContentsChanged(int slot) {
        this.menu.setCarried(this.getStackInSlot(slot));
    }
}
