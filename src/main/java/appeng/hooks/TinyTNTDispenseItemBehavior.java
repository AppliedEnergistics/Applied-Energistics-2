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
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import appeng.entity.TinyTNTPrimedEntity;

public final class TinyTNTDispenseItemBehavior extends ItemDispenserBehavior {

    @Override
    protected ItemStack dispenseSilently(final BlockPointer dispenser, final ItemStack dispensedItem) {
        final Direction Direction = dispenser.getBlockState().get(DispenserBlock.FACING);
        final World world = dispenser.getWorld();
        final int i = dispenser.getBlockPos().getX() + Direction.getOffsetX();
        final int j = dispenser.getBlockPos().getY() + Direction.getOffsetY();
        final int k = dispenser.getBlockPos().getZ() + Direction.getOffsetZ();
        final TinyTNTPrimedEntity primedTinyTNTEntity = new TinyTNTPrimedEntity(world, i + 0.5F, j + 0.5F, k + 0.5F,
                null);
        world.spawnEntity(primedTinyTNTEntity);
        dispensedItem.setCount(dispensedItem.getCount() - 1);
        return dispensedItem;
    }
}
