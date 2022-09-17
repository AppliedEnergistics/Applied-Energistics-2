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

package appeng.core.features.registries.cell;


import appeng.api.storage.*;
import appeng.api.storage.data.IAEStack;
import appeng.me.storage.BasicCellInventory;
import appeng.me.storage.BasicCellInventoryHandler;
import net.minecraft.item.ItemStack;


public class BasicCellHandler implements ICellHandler {

    @Override
    public boolean isCell(final ItemStack is) {
        return BasicCellInventory.isCell(is);
    }

    @Override
    public <T extends IAEStack<T>> ICellInventoryHandler<T> getCellInventory(final ItemStack is, final ISaveProvider container, final IStorageChannel<T> channel) {
        final ICellInventory<T> inv = BasicCellInventory.createInventory(is, container);
        if (inv == null || inv.getChannel() != channel) {
            return null;
        }
        return new BasicCellInventoryHandler<>(inv, channel);
    }

}
