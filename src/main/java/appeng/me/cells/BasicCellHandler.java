/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.me.cells;

import net.minecraft.world.item.ItemStack;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.data.IAEStack;

/**
 * Cell handler that manages all normal storage cells (items, fluids).
 */
public class BasicCellHandler implements ICellHandler {
    public static final BasicCellHandler INSTANCE = new BasicCellHandler();

    @Override
    public boolean isCell(ItemStack is) {
        return BasicCellInventory.isCell(is);
    }

    @Override
    public <T extends IAEStack> BasicCellInventoryHandler<T> getCellInventory(ItemStack is,
            ISaveProvider container,
            IStorageChannel<T> channel) {
        var inv = BasicCellInventory.createInventory(is, container, channel);
        if (inv == null || inv.getChannel() != channel) {
            return null;
        }
        // This cast is safe because we check the channel of the inventory
        return new BasicCellInventoryHandler<>(inv, channel);
    }
}
