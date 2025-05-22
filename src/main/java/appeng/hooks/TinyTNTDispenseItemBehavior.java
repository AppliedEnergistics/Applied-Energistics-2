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

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

import appeng.entity.TinyTNTPrimedEntity;

public final class TinyTNTDispenseItemBehavior extends DefaultDispenseItemBehavior {

    @Override
    protected ItemStack execute(BlockSource dispenser, ItemStack dispensedItem) {
        final Direction Direction = dispenser.getBlockState().getValue(DispenserBlock.FACING);
        final Level level = dispenser.getLevel();
        final int i = dispenser.getPos().getX() + Direction.getStepX();
        final int j = dispenser.getPos().getY() + Direction.getStepY();
        final int k = dispenser.getPos().getZ() + Direction.getStepZ();
        final TinyTNTPrimedEntity primedTinyTNTEntity = new TinyTNTPrimedEntity(level, i + 0.5F, j + 0.5F, k + 0.5F,
                null);
        level.addFreshEntity(primedTinyTNTEntity);
        dispensedItem.setCount(dispensedItem.getCount() - 1);
        return dispensedItem;
    }
}
