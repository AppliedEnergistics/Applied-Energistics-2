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

package appeng.hooks;

import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public final class BlockToolDispenseItemBehavior extends DefaultDispenseItemBehavior {

    @Override
    protected ItemStack execute(final IBlockSource dispenser, final ItemStack dispensedItem) {
        final Item i = dispensedItem.getItem();
        if (i instanceof IBlockTool) {
            final Direction direction = dispenser.getBlockState().getValue(DispenserBlock.FACING);
            final IBlockTool tm = (IBlockTool) i;

            final World w = dispenser.getLevel();
            if (w instanceof ServerWorld) {
                ItemUseContext context = new DirectionalPlaceContext(w, dispenser.getPos().relative(direction),
                        direction, dispensedItem, direction.getOpposite());
                tm.onItemUse(context);
            }
        }
        return dispensedItem;
    }
}
