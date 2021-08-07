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

package appeng.helpers;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;

public interface IContainerCraftingPacket {

    /**
     * @return gain access to network infrastructure.
     */
    IGridNode getNetworkNode();

    /**
     * @param string name of inventory
     * @return the inventory of the part/block entity by name.
     */
    IItemHandler getInventoryByName(String string);

    /**
     * @return who are we?
     */
    IActionSource getActionSource();

    /**
     * @return consume items?
     */
    boolean useRealItems();

    /**
     * @return array of view cells. can contain empty itemstacks.
     */
    List<ItemStack> getViewCells();
}
