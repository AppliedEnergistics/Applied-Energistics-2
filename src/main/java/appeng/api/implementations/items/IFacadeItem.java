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

package appeng.api.implementations.items;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.parts.IFacadePart;

/**
 * Implemented on a {@link Item}
 */
public interface IFacadeItem {

    /**
     * creates an IFacadePart from the facade ItemStack and the Direction
     *
     * @param is   the facade ItemStack you want the IFacadePart for
     * @param side the Direction the IFacadePart is for
     * @return the created IFacadePart
     */
    IFacadePart createPartFromItemStack(ItemStack is, Direction side);

    /**
     * get the ItemStack texture the facade was created with
     *
     * @param is the facade ItemStack you want the texture from
     * @return the ItemStack texture
     */
    ItemStack getTextureItem(ItemStack is);

    /**
     * get the BlockState texture the ItemStack stores
     *
     * @param is the facade ItemStack you want the texture from
     * @return the texture as BlockState
     */
    BlockState getTextureBlockState(ItemStack is);

}
