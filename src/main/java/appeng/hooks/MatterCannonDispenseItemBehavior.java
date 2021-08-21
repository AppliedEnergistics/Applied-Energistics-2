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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

import appeng.items.tools.powered.MatterCannonItem;
import appeng.util.Platform;

public final class MatterCannonDispenseItemBehavior extends DefaultDispenseItemBehavior {

    @Override
    protected ItemStack execute(final BlockSource dispenser, ItemStack dispensedItem) {
        final Item i = dispensedItem.getItem();
        if (i instanceof MatterCannonItem tm) {
            var direction = dispenser.getBlockState().getValue(DispenserBlock.FACING);
            Direction dir = null;
            for (var d : Direction.values()) {
                if (direction.getStepX() == d.getStepX() && direction.getStepY() == d.getStepY()
                        && direction.getStepZ() == d.getStepZ()) {
                    dir = d;
                }
            }

            var level = dispenser.getLevel();
            var p = Platform.getPlayer(level);
            Platform.configurePlayer(p, dir, dispenser.getEntity());

            p.setPos(p.getX() + dir.getStepX(), p.getY() + dir.getStepY(), p.getZ() + dir.getStepZ());

            dispensedItem = tm.use(level, p, null).getObject();
        }
        return dispensedItem;
    }
}
